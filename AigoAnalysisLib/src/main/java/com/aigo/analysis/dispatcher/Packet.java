package com.aigo.analysis.dispatcher;

import androidx.annotation.Nullable;

import com.aigo.analysis.QueryParams;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * @Description: 通过Dispatcher发送到后端API的数据
 * @author: Eknow
 * @date: 2021/5/14 10:19
 */
public class Packet implements Serializable {

    private final String mTargetURL;
    private final JSONObject mPostData;
    private final long mTimeStamp;
    private final int mEventCount;

    /**
     * GET 请求构造器
     *
     * @param targetURL 请求地址
     */
    public Packet(String targetURL) {
        this(targetURL, null, 1);
    }

    /**
     * POST 请求构造器
     *
     * @param targetURL
     * @param postData
     */
    public Packet(String targetURL, @Nullable JSONObject postData) {
        this(targetURL, postData, 1);
    }

    /**
     * POST 请求构造器（批量事件上报）
     *
     * @param targetURL  请求地址
     * @param postData   POST请求数据
     * @param eventCount 事件计数
     */
    public Packet(String targetURL, @Nullable JSONObject postData, int eventCount) {
        this.mTargetURL = targetURL;
        this.mPostData = postData;
        if (mPostData != null && mPostData.has(QueryParams.TARGET_API_URL.toString())) {
            mPostData.remove(QueryParams.TARGET_API_URL.toString());
        }
        this.mEventCount = eventCount;
        this.mTimeStamp = System.currentTimeMillis();
    }

    public String getTargetURL() {
        return mTargetURL;
    }

    public JSONObject getPostData() {
        return mPostData;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public int getEventCount() {
        return mEventCount;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Packet------>(");
        if (mPostData != null) {
            sb.append("type=POST, data=").append(mPostData);
        } else {
            sb.append("type=GET, data=").append(mTargetURL);
        }
        return sb.append(")").toString();
    }
}
