package com.aigo.analysis;

import android.content.SharedPreferences;

import java.util.regex.Pattern;

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
     * 被上报应用的应用基础 URI 地址
     */
    private final String mDefaultApplicationBaseUrl;

    /**
     * 数据上报跟踪器名称，默认'Default Tracker'
     */
    private final String mName;

    /**
     * 默认的要上报的数据
     */
    private final TrackMe mDefaultTrackMe = new TrackMe();

    private SharedPreferences mPreferences;


    protected Tracker(AigoAnalysis aigoAnalysis, TrackerBuilder config) {
        mAigoAnalysis = aigoAnalysis;
        mApiUrl = config.getApiUrl();
        mSiteId = config.getSiteId();
        mName = config.getTrackerName();
        mDefaultApplicationBaseUrl = config.getApplicationBaseUrl();

        //取出部分缓存数据
        mDefaultTrackMe.set(BaseParams.DEVICE_ID, getPreferences().getString(PREF_KEY_TRACKER_DEVICE_ID, ""));
        mDefaultTrackMe.set(BaseParams.USER_ID, getPreferences().getInt(PREF_KEY_TRACKER_USER_ID, 0));
        mDefaultTrackMe.set(BaseParams.CLIENT_AUTO_ID, getPreferences().getInt(PREF_KEY_TRACKER_CLIENT_AUTO_ID, 0));
        mDefaultTrackMe.set(BaseParams.DATA_VERSION, getPreferences().getInt(PREF_KEY_TRACKER_DATA_VERSION, 0));
        //赋值手机系统的数据
        mDefaultTrackMe.set(BaseParams.APP_VERSION, mAigoAnalysis.getDeviceHelper().getUserVersionName());
        mDefaultTrackMe.set(BaseParams.DEVICE_MODEL, mAigoAnalysis.getDeviceHelper().getAndroidDeviceModel());
        mDefaultTrackMe.set(BaseParams.SYSTEM_VERSION, mAigoAnalysis.getDeviceHelper().getSystemVersion());
        mDefaultTrackMe.set(BaseParams.COUNTRY, mAigoAnalysis.getDeviceHelper().getUserCountry());
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

    public String getName() {
        return mName;
    }

    public String getDefaultApplicationBaseUrl() {
        return mDefaultApplicationBaseUrl;
    }

    public void saveInitialParams(String deviceId, int userId, int clientAutoId, int dataVersion) {
        synchronized (getPreferences()) {
            getPreferences().edit().putString(PREF_KEY_TRACKER_DEVICE_ID, deviceId).apply();
            mDefaultTrackMe.set(BaseParams.DEVICE_ID, deviceId);
        }
        synchronized (getPreferences()) {
            getPreferences().edit().putInt(PREF_KEY_TRACKER_USER_ID, userId).apply();
            mDefaultTrackMe.set(BaseParams.USER_ID, userId);
        }
        synchronized (getPreferences()) {
            getPreferences().edit().putInt(PREF_KEY_TRACKER_CLIENT_AUTO_ID, clientAutoId).apply();
            mDefaultTrackMe.set(BaseParams.CLIENT_AUTO_ID, clientAutoId);
        }
        synchronized (getPreferences()) {
            getPreferences().edit().putInt(PREF_KEY_TRACKER_DATA_VERSION, clientAutoId).apply();
            mDefaultTrackMe.set(BaseParams.DATA_VERSION, clientAutoId);
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
}
