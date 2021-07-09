package com.aigo.analysis.event;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;

import com.aigo.analysis.AigoAnalysis;
import com.aigo.analysis.Tracker;
import com.aigo.analysis.tools.DateUtil;
import com.aigo.analysis.tools.MapUtil;
import com.aigo.analysis.work.CustomEventWork;

import java.util.HashMap;

/**
 * @Description: 自定义事件上报
 * @author: Eknow
 * @date: 2021/7/8 19:16
 */
public class CustomEvent extends BaseEvent implements IWorkRequestEvent {

    private int count = 1;
    private String baseEvent;
    private String event;
    private HashMap<String, Object> extension;

    /**
     *
     * @param baseEvent 一级事件， 必传
     * @param event 二级事件
     * @param count 统计值
     */
    public CustomEvent(String baseEvent, String event, int count) {
        this.baseEvent = baseEvent;
        this.event = event;
        this.count = count;
    }

    /**
     *
     * @param baseEvent 一级事件， 必传
     * @param event 二级事件
     */
    public CustomEvent(String baseEvent, String event) {
        this(baseEvent, event, 1);
    }

    /**
     * 添加拓展参数
     *
     * @param name 参数名
     * @param value 参数值
     * @return
     */
    public CustomEvent setExtension(String name, Object value) {
        if (extension == null) {
            extension = new HashMap<>();
        }
        extension.put(name, value);
        return this;
    }

    @Override
    public OneTimeWorkRequest send(Tracker tracker) {
        Data data = commonData(tracker)
                .putLong("time", System.currentTimeMillis())
                .putLong("local_time", DateUtil.getLocalUnixTimestamp())
                .putInt("count", count)
                .putString("base_event", baseEvent)
                .putString("event", event)
                .putString("extension", MapUtil.getMapToString(extension))
                .build();

        return new OneTimeWorkRequest.Builder(CustomEventWork.class)
                .setInputData(data)
                .addTag(AigoAnalysis.tag(CustomEvent.class))
                .build();
    }
}
