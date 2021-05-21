package com.aigo.analysis;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @Description: 数据埋点跟踪器，基础参数配置
 * @author: Eknow
 * @date: 2021/5/17 14:48
 */
public class TrackerBuilder {

    /**
     * 数据分析服务端 API 地址
     */
    private String mApiUrl;

    /**
     * 上报站点id，由后端设置的值，例如：移动端-1，H5-2
     */
    private int mSiteId;

    /**
     * 此跟踪器的唯一名称。用于存储独立于API地址和站点id的跟踪器配置更改。
     */
    private String mTrackerName;

    /**
     * 用户生成请求参数URL的域名
     * 默认值，https://{packagename}/
     */
    private String mApplicationBaseUrl;

    /**
     * @param apiUrl
     * @param siteId
     * @return
     */
    public static TrackerBuilder createDefault(String apiUrl, int siteId) {
        return new TrackerBuilder(apiUrl, siteId, "Default Tracker");
    }

    /**
     * @param apiUrl
     * @param siteId
     * @param trackerName
     */
    public TrackerBuilder(String apiUrl, int siteId, String trackerName) {
        try {
            new URL(apiUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        mApiUrl = apiUrl;
        mSiteId = siteId;
        mTrackerName = trackerName;
    }

    public String getApiUrl() {
        return mApiUrl;
    }

    public int getSiteId() {
        return mSiteId;
    }

    public TrackerBuilder setTrackerName(String trackerName) {
        mTrackerName = trackerName;
        return this;
    }

    public String getTrackerName() {
        return mTrackerName;
    }

    public TrackerBuilder setApplicationBaseUrl(String domain) {
        mApplicationBaseUrl = domain;
        return this;
    }

    public String getApplicationBaseUrl() {
        return mApplicationBaseUrl;
    }

    public Tracker build(AigoAnalysis aigoAnalysis) {
        if (mApplicationBaseUrl == null) {
            mApplicationBaseUrl = String.format("https://%s/", aigoAnalysis.getContext().getPackageName());
        }
        return new Tracker(aigoAnalysis, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TrackerBuilder that = (TrackerBuilder) o;

        return mSiteId == that.mSiteId && mApiUrl.equals(that.mApiUrl) && mTrackerName.equals(that.mTrackerName);
    }

    @Override
    public int hashCode() {
        int result = mApiUrl.hashCode();
        result = 31 * result + mSiteId;
        result = 31 * result + mTrackerName.hashCode();
        return result;
    }
}
