package com.aigo.analysis.dispatcher;

import com.aigo.analysis.AigoAnalysis;
import com.aigo.analysis.QueryParams;
import com.aigo.analysis.tools.GsonHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import timber.log.Timber;

/**
 * @Description: 统计事件数据，格式为  时间戳 + 参数
 * @author: Eknow
 * @date: 2021/8/18 17:31
 */
public class Event {
    private static final String TAG = AigoAnalysis.tag(Event.class);
    private final long mTimestamp;
    private final String mQuery;
    private JSONObject mEventData;

    public Event(Map<String, Object> eventData) {
        this(GsonHelper.object2JsonStr(eventData));
    }

    public Event(String query) {
        this(System.currentTimeMillis(), query);
    }

    public Event(long timestamp, String query) {
        this.mTimestamp = timestamp;
        this.mQuery = query;
        try {
            mEventData = new JSONObject(mQuery);
        } catch (JSONException e) {
            Timber.tag(TAG).e(e, "Cannot create json object:\n%s", mQuery);
        }
    }

    public long getTimeStamp() {
        return mTimestamp;
    }

    public String getEncodedQuery() {
        return mQuery;
    }

    public JSONObject getJsonQuery() {
        return mEventData;
    }

    public String getFuncApi() {
        String funcApi = "";
        if (mEventData != null) {
            funcApi = mEventData.optString(QueryParams.TARGET_API_URL.toString());
        }
        return funcApi;
    }

    @Override
    public String toString() {
        return getEncodedQuery();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Event event = (Event) o;
        return mTimestamp == event.mTimestamp && mQuery.equals(event.mQuery);

    }

    @Override
    public int hashCode() {
        int result = (int) (mTimestamp ^ (mTimestamp >>> 32));
        result = 31 * result + mQuery.hashCode();
        return result;
    }
}
