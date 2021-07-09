package com.aigo.analysis;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import androidx.work.WorkContinuation;
import androidx.work.WorkManager;

import com.aigo.analysis.event.IWorkRequestEvent;
import com.aigo.analysis.event.PageEvent;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * @Description: 简易的跟踪器帮助类，初始化一个服务器的地址
 * 如果有多个不同的服务器上报地址，需要创建更多的 Tracker
 * @author: Eknow
 * @date: 2021/5/18 11:59
 */
public class TrackerHelper {

    /**
     * 系统定义，安卓端传 1
     */
    public static final Integer PLATFORM = 1;

    private static TrackerHelper mInstance;

    private Context mContext;
    private String apiUrl;
    private Integer siteId;
    private boolean isInit = false;

    /**
     * 默认单一的跟踪器
     */
    private Tracker tracker;

    public static TrackerHelper getInstance() {
        if (mInstance == null) {
            synchronized (TrackerHelper.class) {
                if (mInstance == null) {
                    mInstance = new TrackerHelper();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context, String apiUrl, Integer siteId, boolean showLog) {
        this.mContext = context;
        this.apiUrl = apiUrl;
        this.siteId = siteId;
        showLog(showLog);
        TrackerLifecycle.with((Application) context);
        isInit = true;
    }

    public void init(Context context, String apiUrl, boolean showLog) {
        init(context, apiUrl, 1, showLog);
    }

    public synchronized Tracker getTracker() {
        if (tracker == null) {
            tracker = TrackerBuilder.createDefault(apiUrl, siteId)
                    .build(AigoAnalysis.getInstance(mContext));
        }
        return tracker;
    }

    public void showLog(boolean show) {
        if (show) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    public boolean isInit() {
        return isInit;
    }

    public void setInit(boolean init) {
        isInit = init;
    }

    /**
     * 单次调用一个事件
     * @param event
     */
    public void with(IWorkRequestEvent event) {
        if (isInit) {
            WorkManager.getInstance(mContext).enqueue(event.send(getTracker()));
        }
    }

    /**
     * 链式调用事件，按先后顺序传入
     * @param events
     */
    @SuppressLint("EnqueueWork")
    public void with(IWorkRequestEvent... events) {
        if (isInit && events.length >= 2) {
            WorkContinuation continuation = WorkManager.getInstance(mContext)
                    .beginWith(events[0].send(getTracker()));
            for (int i = 1; i < events.length; i++) {
                continuation.then(events[i].send(getTracker()));
            }
            continuation.enqueue();
        }
    }


    private static final HashMap<String, Long> PAGE_NAME_MAP = new HashMap<>();

    /**
     * fragment 页面上报，开始计时
     * @param pageName
     */
    public static void onFragmentStart(String pageName) {
//        synchronized (TrackerHelper.class) {
//            PAGE_NAME_MAP.put(pageName, System.currentTimeMillis());
//        }
    }

    /**
     * fragment 页面上报，结束计时，开始上报
     * @param pageName
     */
    public static void onFragmentEnd(String pageName) {
//        synchronized (TrackerHelper.class) {
//            long startTime = 0;
//            if (PAGE_NAME_MAP.containsKey(pageName)) {
//                startTime = PAGE_NAME_MAP.get(pageName);
//            }
//            long stopTime = (System.currentTimeMillis() - startTime) / 1000;
//            if (stopTime > 0 && startTime > 0) {
//                TrackerHelper.getInstance().with(new PageEvent(pageName,
//                        "This is a fragment",
//                        stopTime));
//            }
//        }
    }
}
