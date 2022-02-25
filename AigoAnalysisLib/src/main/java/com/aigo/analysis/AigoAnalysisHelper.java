package com.aigo.analysis;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import com.aigo.analysis.extra.InitAigoClient;
import com.aigo.analysis.extra.TrackHelper;
import com.aigo.analysis.extra.TrackLifecycle;

import java.util.HashMap;

import timber.log.Timber;

/**
 * @Description: 简易的跟踪器帮助类，初始化一个服务器的地址
 * 如果有多个不同的服务器上报地址，需要创建更多的 Tracker
 * @author: Eknow
 * @date: 2021/5/18 11:59
 */
public class AigoAnalysisHelper {

    private static AigoAnalysisHelper mInstance;

    private Context mContext;
    private String apiUrl;
    private Integer siteId;
    private String tenantId;

    /**
     * 默认单一的跟踪器
     */
    private Tracker tracker;

    public static AigoAnalysisHelper getInstance() {
        if (mInstance == null) {
            synchronized (AigoAnalysisHelper.class) {
                if (mInstance == null) {
                    mInstance = new AigoAnalysisHelper();
                }
            }
        }
        return mInstance;
    }

    /**
     * 获取单一跟踪器
     *
     * @return
     */
    public synchronized Tracker getTracker() {
        if (tracker == null) {
            tracker = TrackerBuilder.createDefault(apiUrl, siteId, tenantId)
                    .build(AigoAnalysis.getInstance(mContext));
        }
        return tracker;
    }

    /**
     * 配置目标地址和站点
     *
     * @param context
     * @param apiUrl  服务地址
     * @param siteId  客户端平台站点，安卓 = 1,苹果 = 2, H5 = 3,Pad = 4
     * @param tenantId 服务端租户id
     * @return
     */
    public AigoAnalysisHelper config(Context context, @NonNull String apiUrl, @NonNull Integer siteId, @NonNull String tenantId) {
        this.mContext = context;
        this.apiUrl = apiUrl;
        this.siteId = siteId;
        this.tenantId = tenantId;
        return this;
    }

    /**
     * 打印日志
     *
     * @param show
     */
    public AigoAnalysisHelper showLog(boolean show) {
        if (show) {
            Timber.plant(new Timber.DebugTree());
        }
        return this;
    }

    /**
     * 自动上报 activity
     *
     * @return
     */
    public AigoAnalysisHelper autoActivityPage() {
        if (mContext != null) {
            TrackLifecycle.with((Application) mContext);
        }
        return this;
    }

    /**
     * 调用服务端的初始化逻辑
     */
    public void init() {
        InitAigoClient client = new InitAigoClient(mContext, getTracker());
        client.launch();
    }

    /**
     * 页面节点统计上报事件
     *
     * @param pageName     页面
     * @param backPageName 上级页面，首页=0
     * @param seconds      停留时间 （秒）
     */
    public void screen(String pageName, String backPageName, Long seconds) {
        TrackHelper.track()
                .screen(pageName)
                .backPageName(backPageName)
                .seconds(seconds)
                .with(getTracker());
    }


    private final HashMap<String, Long> PAGE_NAME_MAP = new HashMap<>();

    /**
     * fragment 页面上报，开始计时
     *
     * @param pageName
     */
    public void onFragmentStart(String pageName) {
        synchronized (AigoAnalysisHelper.class) {
            PAGE_NAME_MAP.put(pageName, System.currentTimeMillis());
        }
    }

    /**
     * fragment 页面上报，结束计时，开始上报
     *
     * @param pageName
     */
    public void onFragmentEnd(String pageName) {
        synchronized (AigoAnalysisHelper.class) {
            long startTime = 0;
            if (PAGE_NAME_MAP.containsKey(pageName)) {
                startTime = PAGE_NAME_MAP.get(pageName);
            }
            long stopTime = (System.currentTimeMillis() - startTime) / 1000;
            if (stopTime > 0 && startTime > 0) {
                AigoAnalysisHelper.getInstance().screen(pageName, "This is a fragment", stopTime);
            }
        }
    }
}
