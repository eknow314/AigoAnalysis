package com.aigo.analysis.extra;

import androidx.annotation.Nullable;

import com.aigo.analysis.AigoAnalysis;
import com.aigo.analysis.TrackMe;
import com.aigo.analysis.event.CustomEvent;
import com.aigo.analysis.event.ScreenEvent;

/**
 * @Description: 事件跟踪器帮助类，封装常用方法
 * @author: Eknow
 * @date: 2021/8/26 15:27
 */
public class TrackHelper {

    private static final String TAG = AigoAnalysis.tag(TrackHelper.class);
    protected final TrackMe mBaseTrackMe;

    private TrackHelper() {
        this(null);
    }

    private TrackHelper(@Nullable TrackMe baseTrackMe) {
        if (baseTrackMe == null) {
            baseTrackMe = new TrackMe();
        }
        mBaseTrackMe = baseTrackMe;
    }

    public static TrackHelper track() {
        return new TrackHelper();
    }

    public static TrackHelper track(@Nullable TrackMe base) {
        return new TrackHelper(base);
    }

    public TrackMe getBaseTrackMe() {
        return mBaseTrackMe;
    }

    /**
     *
     * @param pageName
     * @return
     */
    public ScreenEvent screen(String pageName) {
        return new ScreenEvent(this, pageName);
    }

    /**
     *
     * @param baseEvent
     * @param event
     * @return
     */
    public CustomEvent custom(String baseEvent, String event) {
        return new CustomEvent(this, baseEvent, event);
    }
}
