package com.aigo.analysis.dispatcher;

import com.aigo.analysis.AigoAnalysis;
import com.aigo.analysis.TrackMe;
import com.aigo.analysis.tools.Connectivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * @Description: 默认实现一个发送数据包任务调度器
 * @author: Eknow
 * @date: 2021/8/23 17:08
 */
public class DefaultDispatcher implements Dispatcher {
    private static final String TAG = AigoAnalysis.tag(DefaultDispatcher.class);
    private final Object mThreadControl = new Object();
    private final EventCache mEventCache;
    private final Semaphore mSleepToken = new Semaphore(0);
    private final Connectivity mConnectivity;
    private final PacketFactory mPacketFactory;
    private final PacketSender mPacketSender;
    private volatile int mTimeOut = DEFAULT_CONNECTION_TIMEOUT;
    private volatile long mDispatchInterval = DEFAULT_DISPATCH_INTERVAL;
    private volatile int mRetryCounter = 0;
    private volatile boolean mForcedBlocking = false;

    private boolean mDispatchGzipped = false;
    private volatile DispatchMode mDispatchMode = DispatchMode.ALWAYS;
    private volatile boolean mRunning = false;
    private volatile Thread mDispatchThread = null;

    public DefaultDispatcher(EventCache eventCache, Connectivity connectivity, PacketFactory packetFactory, PacketSender packetSender) {
        mConnectivity = connectivity;
        mEventCache = eventCache;
        mPacketFactory = packetFactory;
        mPacketSender = packetSender;
        packetSender.setGzipData(mDispatchGzipped);
        packetSender.setTimeout(mTimeOut);
    }

    @Override
    public int getConnectionTimeOut() {
        return mTimeOut;
    }

    @Override
    public void setConnectionTimeOut(int timeOut) {
        mTimeOut = timeOut;
        mPacketSender.setTimeout(timeOut);
    }

    @Override
    public long getDispatchInterval() {
        return mDispatchInterval;
    }

    @Override
    public void setDispatchInterval(long dispatchInterval) {
        mDispatchInterval = dispatchInterval;
        if (mDispatchInterval != -1) {
            launch();
        }
    }

    @Override
    public boolean getDispatchGzipped() {
        return mDispatchGzipped;
    }

    @Override
    public void setDispatchGzipped(boolean dispatchGzipped) {
        mDispatchGzipped = dispatchGzipped;
        mPacketSender.setGzipData(mDispatchGzipped);
    }

    @Override
    public DispatchMode getDispatchMode() {
        return mDispatchMode;
    }

    @Override
    public void setDispatchMode(DispatchMode dispatchMode) {
        mDispatchMode = dispatchMode;
    }

    @Override
    public boolean forceDispatch() {
        if (!launch()) {
            mRetryCounter = 0;
            mSleepToken.release();
            return false;
        }
        return true;
    }

    @Override
    public void forceDispatchBlocking() {
        synchronized (mThreadControl) {
            mForcedBlocking = true;
        }

        if (forceDispatch()) {
            mSleepToken.release();
        }

        Thread dispatchThread = mDispatchThread;

        if (dispatchThread != null) {
            try {
                dispatchThread.join();
            } catch (InterruptedException e) {
                Timber.tag(TAG).d("Interrupted while waiting for dispatch thread to complete");
            }
        }

        synchronized (mThreadControl) {
            mForcedBlocking = false;
        }
    }

    @Override
    public void clear() {
        mEventCache.clear();
        if (mRunning) {
            forceDispatch();
        }
    }

    @Override
    public void submit(TrackMe trackMe) {
        mEventCache.add(new Event(trackMe.toJson()));
        if (mDispatchInterval != -1) {
            launch();
        }
    }

    private boolean launch() {
        synchronized (mThreadControl) {
            if (!mRunning) {
                mRunning = true;
                Thread thread = new Thread(mLoop);
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.setName("AigoAnalysis-default-dispatcher");
                mDispatchThread = thread;
                thread.start();
                return true;
            }
        }
        return false;
    }

    private final Runnable mLoop = new Runnable() {
        @Override
        public void run() {
            mRetryCounter = 0;
            while (mRunning) {
                try {
                    long sleepTime = mDispatchInterval;
                    if (mRetryCounter > 1) {
                        sleepTime += Math.min(mRetryCounter * mDispatchInterval, 5 * mDispatchInterval);
                    }
                    mSleepToken.tryAcquire(sleepTime, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Timber.tag(TAG).e(e);
                }

                if (mEventCache.updateState(isOnline())) {
                    int count = 0;
                    List<Event> drainedEvents = new ArrayList<>();
                    mEventCache.drainTo(drainedEvents);
                    Timber.tag(TAG).d("Drained %s events.", drainedEvents.size());
                    for (Packet packet : mPacketFactory.buildPackets(drainedEvents)) {

                        boolean success = mPacketSender.send(packet, null);

                        if (success) {
                            count += packet.getEventCount();
                            mRetryCounter = 0;
                        } else {
                            Timber.tag(TAG).d("Failure while trying to send packet");
                            mRetryCounter++;
                            break;
                        }

                        if (!isOnline()) {
                            Timber.tag(TAG).d("Disconnected during dispatch loop");
                            break;
                        }
                    }

                    Timber.tag(TAG).d("Dispatched %d events.", count);
                    if (count < drainedEvents.size()) {
                        Timber.tag(TAG).d("Unable to send all events, requeueing %d events", drainedEvents.size() - count);
                        mEventCache.requeue(drainedEvents.subList(count, drainedEvents.size()));
                        mEventCache.updateState(isOnline());
                    }
                }

                synchronized (mThreadControl) {
                    if (mForcedBlocking || mEventCache.isEmpty() || mDispatchInterval < 0) {
                        mRunning = false;
                        break;
                    }
                }
            }
        }
    };

    private boolean isOnline() {
        if (!mConnectivity.isConnected()) {
            return false;
        }
        switch (mDispatchMode) {
            case EXCEPTION:
                return false;
            case ALWAYS:
                return true;
            case WIFI_ONLY:
                return mConnectivity.getType() == Connectivity.Type.WIFI;
            default:
                return false;
        }
    }
}
