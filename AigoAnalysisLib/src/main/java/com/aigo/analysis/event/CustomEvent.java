package com.aigo.analysis.event;

import com.aigo.analysis.QueryParams;
import com.aigo.analysis.TrackMe;
import com.aigo.analysis.extra.TrackHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @author: Eknow
 * @date: 2021/8/26 17:04
 */
public class CustomEvent extends BaseEvent {

    private final String mBaseEvent;
    private final String mEvent;
    private Long mTime;
    private Long mLocalTime;
    private Long mCount = 1L;
    private Map<String, Object> mExtension;

    /**
     *
     * @param baseBuilder
     * @param baseEvent 一级事件
     * @param event 二级事件
     */
    public CustomEvent(TrackHelper baseBuilder, String baseEvent, String event) {
        super(baseBuilder);
        mBaseEvent = baseEvent;
        mEvent = event;
    }

    public CustomEvent time(Long time) {
        mTime = time;
        return this;
    }

    public CustomEvent localTime(Long localTime) {
        mLocalTime = localTime;
        return this;
    }

    public CustomEvent count(Long count) {
        mCount = count;
        return this;
    }

    /**
     * 添加拓展参数
     *
     * @param name 参数名
     * @param value 参数值
     * @return
     */
    public CustomEvent setExtension(String name, Object value) {
        if (mExtension == null) {
            mExtension = new HashMap<>();
        }
        mExtension.put(name, value);
        return this;
    }

    @Override
    public TrackMe build() {
        TrackMe trackMe = new TrackMe(getBaseTrackMe())
                .set(QueryParams.TARGET_API_URL, "v1.0/nodes/event")
                .set(QueryParams.BASE_EVENT, mBaseEvent)
                .set(QueryParams.EVENT, mEvent)
                .set(QueryParams.COUNT, mCount)
                .set(QueryParams.TIME, mTime)
                .set(QueryParams.LOCAL_TIME, mLocalTime);

        if (mExtension != null && !mExtension.isEmpty()) {
            List<HashMap<String, Object>> list = new ArrayList<>();
            for (String key : mExtension.keySet()) {
                HashMap<String, Object> map = new HashMap<>();
                map.put(QueryParams.EVENT_NAME.toString(), key);
                map.put(QueryParams.EVENT_VALUE.toString(), mExtension.get(key));
                list.add(map);
            }
            trackMe.set(QueryParams.EXTENSION, list);
        }
        return trackMe;
    }
}
