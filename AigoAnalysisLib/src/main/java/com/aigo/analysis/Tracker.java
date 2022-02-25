package com.aigo.analysis;

import android.content.ComponentCallbacks2;
import android.content.SharedPreferences;
import android.os.Looper;

import com.aigo.analysis.dispatcher.DispatchMode;
import com.aigo.analysis.dispatcher.Dispatcher;
import com.aigo.analysis.tools.DateUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * @Description: 数据埋点跟踪器，主要的数据包跟踪类
 * 每个跟踪器都有自己的目标服务器地址
 * @author: Eknow
 * @date: 2021/5/17 14:19
 */
public class Tracker {

    private static final String TAG = AigoAnalysis.tag(Tracker.class);

    /**
     * Android 设备的 OAID，获取到存储到 sp
     */
    protected static final String PREF_KEY_TRACKER_DEVICE_ID = "tracker.deviceid";

    /**
     * 登录用户 ID，获取到存储到 sp
     */
    protected static final String PREF_KEY_TRACKER_USER_ID = "tracker.userid";

    /**
     * 设备注册到后台后拿到的自增 ID，获取到存储到 sp
     */
    protected static final String PREF_KEY_TRACKER_CLIENT_AUTO_ID = "tracker.clientautoid";

    /**
     * 设备注册到后台后拿到的数据版本，获取到存储到 sp
     */
    protected static final String PREF_KEY_TRACKER_DATA_VERSION = "tracker.dataversion";

    /**
     * 缓存文件时效
     */
    protected static final String PREF_KEY_OFFLINE_CACHE_AGE = "tracker.cache.age";

    /**
     * 缓存文件大小
     */
    protected static final String PREF_KEY_OFFLINE_CACHE_SIZE = "tracker.cache.size";

    /**
     * 调度器分发模式
     */
    protected static final String PREF_KEY_DISPATCHER_MODE = "tracker.dispatcher.mode";

    /**
     * 校验url合法性
     */
    private static final Pattern VALID_URLS = Pattern.compile("^(\\w+)(?:://)(.+?)$");

    /**
     *
     */
    private final AigoAnalysis mAigoAnalysis;

    /**
     * 数据上报服务端地址
     */
    private final String mApiUrl;

    /**
     * 数据上报站点id，对应平台: 1.安卓 2.苹果 3.H5 4.PAD
     */
    private final int mSiteId;

    /**
     * 服务器租户 id
     */
    private final String mTenantId;

    /**
     * 被上报应用的应用基础 URI 地址
     */
    private final String mDefaultApplicationBaseUrl;

    /**
     * 任务调度器
     */
    private final Dispatcher mDispatcher;

    /**
     * 数据上报跟踪器名称，默认'Default Tracker'
     */
    private final String mName;

    /**
     * 默认的要上报的数据
     */
    private final TrackMe mDefaultTrackMe = new TrackMe();

    private SharedPreferences mPreferences;

    /**
     * 任务调度模式
     */
    private DispatchMode mDispatchMode;


    protected Tracker(AigoAnalysis aigoAnalysis, TrackerBuilder config) {
        mAigoAnalysis = aigoAnalysis;
        mApiUrl = config.getApiUrl();
        mSiteId = config.getSiteId();
        mTenantId = config.getTenantId();
        mName = config.getTrackerName();
        mDefaultApplicationBaseUrl = config.getApplicationBaseUrl();

        mDispatcher = mAigoAnalysis.getDispatcherFactory().build(this);
        mDispatcher.setDispatchMode(getDispatchMode());

        //取出部分缓存数据
        mDefaultTrackMe.set(QueryParams.DEVICE_ID, getPreferences().getString(PREF_KEY_TRACKER_DEVICE_ID, ""));
        mDefaultTrackMe.set(QueryParams.USER_ID, getPreferences().getInt(PREF_KEY_TRACKER_USER_ID, 0));
        mDefaultTrackMe.set(QueryParams.CLIENT_AUTO_ID, getPreferences().getInt(PREF_KEY_TRACKER_CLIENT_AUTO_ID, 0));
        mDefaultTrackMe.set(QueryParams.DATA_VERSION, getPreferences().getInt(PREF_KEY_TRACKER_DATA_VERSION, 0));
        //赋值手机系统的数据
        mDefaultTrackMe.set(QueryParams.APP_VERSION, mAigoAnalysis.getDeviceHelper().getUserVersionName());
        mDefaultTrackMe.set(QueryParams.DEVICE_MODEL, mAigoAnalysis.getDeviceHelper().getAndroidDeviceModel());
        mDefaultTrackMe.set(QueryParams.SYSTEM_VERSION, mAigoAnalysis.getDeviceHelper().getSystemVersion());
        mDefaultTrackMe.set(QueryParams.COUNTRY, mAigoAnalysis.getDeviceHelper().getUserCountry());
        mDefaultTrackMe.set(QueryParams.PLATFORM, mSiteId);
        mDefaultTrackMe.set(QueryParams.TENANT_ID, mTenantId);
    }

    public AigoAnalysis getAigoAnalysis() {
        return mAigoAnalysis;
    }

    public String getApiUrl() {
        return mApiUrl;
    }

    public int getSiteId() {
        return mSiteId;
    }

    public String getTenantId() {
        return mTenantId;
    }

    public Map<String, String> getHeaders() {
        HashMap<String, String> header = new HashMap<>();
        header.put("TenantId", mTenantId);
        return header;
    }

    public String getName() {
        return mName;
    }

    public String getDefaultApplicationBaseUrl() {
        return mDefaultApplicationBaseUrl;
    }

    /**
     * 初始化成功后保存数据
     */
    public void saveInitialParams(String deviceId, int userId, int clientAutoId, int dataVersion) {
        synchronized (getPreferences()) {
            getPreferences().edit().putString(PREF_KEY_TRACKER_DEVICE_ID, deviceId).apply();
            mDefaultTrackMe.set(QueryParams.DEVICE_ID, deviceId);
        }
        synchronized (getPreferences()) {
            getPreferences().edit().putInt(PREF_KEY_TRACKER_USER_ID, userId).apply();
            mDefaultTrackMe.set(QueryParams.USER_ID, userId);
        }
        synchronized (getPreferences()) {
            getPreferences().edit().putInt(PREF_KEY_TRACKER_CLIENT_AUTO_ID, clientAutoId).apply();
            mDefaultTrackMe.set(QueryParams.CLIENT_AUTO_ID, clientAutoId);
        }
        synchronized (getPreferences()) {
            getPreferences().edit().putInt(PREF_KEY_TRACKER_DATA_VERSION, dataVersion).apply();
            mDefaultTrackMe.set(QueryParams.DATA_VERSION, dataVersion);
        }
    }

    public TrackMe getDefaultTrackMe() {
        return mDefaultTrackMe;
    }

    public SharedPreferences getPreferences() {
        if (mPreferences == null) {
            mPreferences = mAigoAnalysis.getTrackerPreferences(this);
        }
        return mPreferences;
    }

    /**
     * 设置离线缓存的事件的回溯时间
     * 默认接收24小时的回溯事件
     * 比设置的时间更老的事件将被抛弃，不会上报到后台
     *
     * @param age 0无限制，-1禁用缓存，>0 回溯时间毫秒数
     */
    public void setOfflineCacheAge(long age) {
        getPreferences().edit().putLong(PREF_KEY_OFFLINE_CACHE_AGE, age).apply();
    }

    /**
     * 获取离线缓存的事件的回溯时间
     *
     * @return 默认接收24小时的回溯事件
     */
    public long getOfflineCacheAge() {
        return getPreferences().getLong(PREF_KEY_OFFLINE_CACHE_AGE, 24 * 60 * 60 * 1000);
    }

    /**
     * 设置离线缓存大小
     * 如果达到限制，最老的文件将首先被删除。
     *
     * @param size 0无限制， >0 byte
     */
    public void setOfflineCacheSize(long size) {
        getPreferences().edit().putLong(PREF_KEY_OFFLINE_CACHE_SIZE, size).apply();
    }

    /**
     * 获取离线缓存大小
     *
     * @return 默认大小 4M
     */
    public long getOfflineCacheSize() {
        return getPreferences().getLong(PREF_KEY_OFFLINE_CACHE_SIZE, 4 * 1024 * 1024);
    }

    /**
     * 设置任务调度模式
     *
     * @param mode {@link DispatchMode}
     */
    public void setDispatchMode(DispatchMode mode) {
        mDispatchMode = mode;
        if (mode != DispatchMode.EXCEPTION) {
            getPreferences().edit().putString(PREF_KEY_DISPATCHER_MODE, mode.toString()).apply();
        }
        mDispatcher.setDispatchMode(mode);
    }

    /**
     * 获取任务调度模式
     *
     * @return {@link DispatchMode}
     */
    public DispatchMode getDispatchMode() {
        if (mDispatchMode == null) {
            String raw = getPreferences().getString(PREF_KEY_DISPATCHER_MODE, null);
            mDispatchMode = DispatchMode.fromString(raw);
            if (mDispatchMode == null) {
                mDispatchMode = DispatchMode.ALWAYS;
            }
        }
        return mDispatchMode;
    }

    /**
     * 压缩发送数据
     *
     * @param dispatchGzipped
     * @return
     */
    public Tracker setDispatchGzipped(boolean dispatchGzipped) {
        mDispatcher.setDispatchGzipped(dispatchGzipped);
        return this;
    }

    /**
     * 设置发送超时时间
     *
     * @param timeout 默认 {@link Dispatcher#DEFAULT_CONNECTION_TIMEOUT}
     * @return
     */
    public Tracker setDispatchTimeout(int timeout) {
        mDispatcher.setConnectionTimeOut(timeout);
        return this;
    }

    /**
     * 获取发送超时时间
     *
     * @return
     */
    public int getDispatchTimeout() {
        return mDispatcher.getConnectionTimeOut();
    }

    /**
     * 设置任务上报时间间隔
     *
     * @param dispatchInterval 默认 {@link Dispatcher#DEFAULT_DISPATCH_INTERVAL}
     * @return
     */
    public Tracker setDispatchInterval(long dispatchInterval) {
        mDispatcher.setDispatchInterval(dispatchInterval);
        return this;
    }

    /**
     * 获取任务上报时间间隔
     *
     * @return
     */
    public long getDispatchInterval() {
        return mDispatcher.getDispatchInterval();
    }

    /**
     * 将队列里面所有任务都执行发送
     */
    public void dispatch() {
        mDispatcher.forceDispatch();
    }

    /**
     * 异常情况下使得任务马上分发执行，
     * 因为应用程序可能在重新抛出异常后死亡，并阻塞，直到分派完成
     */
    public void dispatchBlocking() {
        mDispatcher.forceDispatchBlocking();
    }

    /**
     * 低内存的时候，应该立即执行队列中全部任务
     */
    public void onLowMemory() {
        dispatch();
    }

    /**
     * 应用即将被系统杀死之前执行队列中的全部任务
     *
     * @param level
     */
    public void onTrimMemory(int level) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            return;
        }
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN || level == ComponentCallbacks2.TRIM_MEMORY_COMPLETE) {
            dispatch();
        }
    }

    /**
     * 插入公共参数
     *
     * @param trackMe
     */
    private void injectBaseParams(TrackMe trackMe) {
        trackMe.trySet(QueryParams.TIME, System.currentTimeMillis());
        trackMe.trySet(QueryParams.LOCAL_TIME, DateUtil.getLocalUnixTimestamp());
    }

    /**
     * 提交事件进队列
     *
     * @param trackMe
     * @return
     */
    public Tracker track(TrackMe trackMe) {
        if (mAigoAnalysis.isInit()) {
            injectBaseParams(trackMe);
            mDispatcher.submit(trackMe);
            Timber.tag(TAG).d("Event added to the queue: %s", trackMe.toJson());
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Tracker tracker = (Tracker) o;

        if (mSiteId != tracker.mSiteId) {
            return false;
        }
        if (!mApiUrl.equals(tracker.mApiUrl)) {
            return false;
        }
        if (!mTenantId.equals(tracker.mTenantId)) {
            return false;
        }
        return mName.equals(tracker.mName);

    }

    @Override
    public int hashCode() {
        int result = mApiUrl.hashCode();
        result = 31 * result + mSiteId;
        result = 31 * result + mTenantId.hashCode();
        result = 31 * result + mName.hashCode();
        return result;
    }
}
