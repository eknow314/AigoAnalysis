package com.aigo.analysis;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 发送数据包里面的基本数据
 * @author: Eknow
 * @date: 2021/5/20 10:22
 */
public class TrackMe {

    private static final int DEFAULT_QUERY_CAPACITY = 14;
    private final HashMap<String, String> mQueryParams = new HashMap<>(DEFAULT_QUERY_CAPACITY);

    public TrackMe() {
    }

    public TrackMe(TrackMe trackMe) {
        mQueryParams.putAll(trackMe.mQueryParams);
    }

    public TrackMe putAll(@NonNull TrackMe trackMe) {
        mQueryParams.putAll(trackMe.toMap());
        return this;
    }

    public synchronized TrackMe set(@NonNull String key, String value) {
        if (value == null) {
            mQueryParams.remove(key);
        } else if (value.length() > 0) {
            mQueryParams.put(key, value);
        }
        return this;
    }

    @Nullable
    public synchronized String get(@NonNull String queryParams) {
        return mQueryParams.get(queryParams);
    }

    public synchronized TrackMe set(@NonNull BaseParams key, String value) {
        set(key.toString(), value);
        return this;
    }

    public synchronized TrackMe set(@NonNull BaseParams key, int value) {
        set(key, Integer.toString(value));
        return this;
    }

    public synchronized TrackMe set(@NonNull BaseParams key, float value) {
        set(key, Float.toString(value));
        return this;
    }

    public synchronized TrackMe set(@NonNull BaseParams key, long value) {
        set(key, Long.toString(value));
        return this;
    }

    public synchronized Map<String, String> toMap() {
        return new HashMap<>(mQueryParams);
    }

    public synchronized String get(@NonNull BaseParams queryParams) {
        return mQueryParams.get(queryParams.toString());
    }

    public synchronized boolean isEmpty() {
        return mQueryParams.isEmpty();
    }
}
