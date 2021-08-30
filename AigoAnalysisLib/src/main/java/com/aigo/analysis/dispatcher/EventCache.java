package com.aigo.analysis.dispatcher;

import com.aigo.analysis.AigoAnalysis;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.LinkedBlockingDeque;

import timber.log.Timber;

/**
 * @Description: 用一个双向并发阻塞队列实现事件的缓存逻辑处理
 * 使得一个事件资源只会被一个发送任务调度，避免重复上报
 *
 * @author: Eknow
 * @date: 2021/8/18 18:05
 */
public class EventCache {
    private static final String TAG = AigoAnalysis.tag(EventCache.class);
    private final LinkedBlockingDeque<Event> mQueue = new LinkedBlockingDeque<>();
    private final EventDiskCache mDiskCache;

    public EventCache(EventDiskCache cache) {
        mDiskCache = cache;
    }

    /**
     * 将新事件插入队列末尾
     *
     * @param event
     */
    public void add(Event event) {
        mQueue.add(event);
    }

    /**
     * 将队列中所有事件取出，然后插入到待发送事件列表
     *
     * @param drainedEvents
     */
    public void drainTo(List<Event> drainedEvents) {
        mQueue.drainTo(drainedEvents);
    }

    public void clear() {
        mDiskCache.uncache();
        mQueue.clear();
    }

    public boolean isEmpty() {
        return mQueue.isEmpty() && mDiskCache.isEmpty();
    }

    public boolean updateState(boolean online) {
        if (online) {
            //取出缓存，上报
            final List<Event> uncache = mDiskCache.uncache();
            ListIterator<Event> it = uncache.listIterator(uncache.size());
            while (it.hasPrevious()) {
                mQueue.offerFirst(it.previous());
            }
            Timber.tag(TAG).d("Switched state to ONLINE, uncached %d events from disk.", uncache.size());
        } else if (!mQueue.isEmpty()) {
            //离线，并且事件队列不为空，先缓存到硬盘
            List<Event> toCache = new ArrayList<>();
            mQueue.drainTo(toCache);
            mDiskCache.cache(toCache);
            Timber.tag(TAG).d("Switched state to OFFLINE, caching %d events to disk.", toCache.size());
        }
        return online && !mQueue.isEmpty();
    }

    public void requeue(List<Event> events) {
        for (Event e : events) {
            mQueue.offerFirst(e);
        }
    }
}
