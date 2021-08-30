package com.aigo.analysis.event;

import com.aigo.analysis.TrackMe;
import com.aigo.analysis.Tracker;
import com.aigo.analysis.extra.TrackHelper;

import timber.log.Timber;

/**
 * @Description: 抽象事件类
 * @author: Eknow
 * @date: 2021/8/26 16:33
 */
public abstract class BaseEvent {

    private final TrackHelper mBaseBuilder;

    public BaseEvent(TrackHelper baseBuilder) {
        mBaseBuilder = baseBuilder;
    }

    public TrackMe getBaseTrackMe() {
        return mBaseBuilder.getBaseTrackMe();
    }

    /**
     * 构建事件数据包
     */
    public abstract TrackMe build();

    public void with(Tracker tracker) {
        TrackMe trackMe = build();
        tracker.track(trackMe);
    }

    /**
     * 获取任务事件是否被添加进调度器
     *
     * @param tracker
     * @return
     */
    public boolean safelyWith(Tracker tracker) {
        try {
            TrackMe trackMe = build();
            tracker.track(trackMe);
        } catch (IllegalArgumentException e) {
            Timber.e(e);
            return false;
        }
        return true;
    }

}
