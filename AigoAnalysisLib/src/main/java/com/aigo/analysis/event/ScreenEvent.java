package com.aigo.analysis.event;

import com.aigo.analysis.QueryParams;
import com.aigo.analysis.TrackMe;
import com.aigo.analysis.extra.TrackHelper;

/**
 * @Description: 页面节点统计上报事件
 * @author: Eknow
 * @date: 2021/8/26 16:39
 */
public class ScreenEvent extends BaseEvent {

    private final String mPageName;
    private String mBackPageName;
    private Long mSeconds;
    private Long mTime;
    private Long mLocalTime;

    /**
     *
     * @param baseBuilder
     * @param pageName
     */
    public ScreenEvent(TrackHelper baseBuilder, String pageName) {
        super(baseBuilder);
        mPageName = pageName;
    }

    public ScreenEvent backPageName(String backPageName) {
        mBackPageName = backPageName;
        return this;
    }

    public ScreenEvent seconds(Long seconds) {
        mSeconds = seconds;
        return this;
    }

    public ScreenEvent time(Long time) {
        mTime = time;
        return this;
    }

    public ScreenEvent localTime(Long localTime) {
        mLocalTime = localTime;
        return this;
    }

    @Override
    public TrackMe build() {
        return new TrackMe(getBaseTrackMe())
                .set(QueryParams.TARGET_API_URL, "v1.0/nodes")
                .set(QueryParams.PAGE_NAME, mPageName)
                .set(QueryParams.BACK_PAGE_NAME, mBackPageName)
                .set(QueryParams.SECONDS, mSeconds)
                .set(QueryParams.TIME, mTime)
                .set(QueryParams.LOCAL_TIME, mLocalTime);
    }
}
