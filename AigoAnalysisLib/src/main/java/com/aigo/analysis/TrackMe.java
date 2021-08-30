package com.aigo.analysis;

import androidx.annotation.NonNull;

import com.aigo.analysis.tools.GsonHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 发送数据包里面的数据
 * @author: Eknow
 * @date: 2021/5/20 10:22
 */
public class TrackMe {

    private static final int DEFAULT_QUERY_CAPACITY = 14;
    private final HashMap<String, Object> mQueryParams = new HashMap<>(DEFAULT_QUERY_CAPACITY);

    public TrackMe() {
    }

    public TrackMe(TrackMe trackMe) {
        mQueryParams.putAll(trackMe.mQueryParams);
    }

    public TrackMe putAll(@NonNull TrackMe trackMe) {
        mQueryParams.putAll(trackMe.toMap());
        return this;
    }

    public synchronized TrackMe set(@NonNull String key, Object value) {
        if (value == null) {
            mQueryParams.remove(key);
        } else {
            mQueryParams.remove(key);
            mQueryParams.put(key, value);
        }
        return this;
    }

    public synchronized TrackMe set(@NonNull QueryParams key, Object value) {
        set(key.toString(), value);
        return this;
    }

    public synchronized TrackMe trySet(@NonNull QueryParams key, Object value) {
        if (!has(key)) {
            set(key, value);
        }
        return this;
    }

    public synchronized boolean has(@NonNull QueryParams queryParams) {
        return mQueryParams.containsKey(queryParams.toString());
    }

    public synchronized Map<String, Object> toMap() {
        return new HashMap<>(mQueryParams);
    }

    public synchronized String toJson() {
        return GsonHelper.object2JsonStr(mQueryParams);
    }

    public synchronized Object get(@NonNull QueryParams queryParams) {
        return mQueryParams.get(queryParams.toString());
    }

    public synchronized String getString(@NonNull QueryParams queryParams, String s) {
        Object obj = get(queryParams);
        if (obj == null) {
            return s;
        } else {
            return String.valueOf(obj);
        }
    }

    public synchronized Integer getInt(@NonNull QueryParams queryParams, int i) {
        Object obj = get(queryParams);
        if (obj instanceof Integer) {
            return (Integer) obj;
        } else {
            return i;
        }
    }

    public synchronized Long getLong(@NonNull QueryParams queryParams, long l) {
        Object obj = get(queryParams);
        if (obj instanceof Long) {
            return (Long) obj;
        } else {
            return l;
        }
    }

    public synchronized Object get(@NonNull String queryParams) {
        return mQueryParams.get(queryParams);
    }

    public synchronized boolean isEmpty() {
        return mQueryParams.isEmpty();
    }
}
