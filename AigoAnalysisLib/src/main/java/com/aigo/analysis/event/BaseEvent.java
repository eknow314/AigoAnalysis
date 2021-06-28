package com.aigo.analysis.event;

import androidx.work.Data;

import com.aigo.analysis.BaseParams;
import com.aigo.analysis.Tracker;
import com.aigo.analysis.TrackerHelper;

/**
 * @Description: 将跟踪器里面设置的公共值传入
 * @author: Eknow
 * @date: 2021/5/18 15:29
 */
public class BaseEvent {

    public Data.Builder commonData(Tracker tracker) {
        return new Data.Builder()
                .putString(BaseParams.TARGET_API_URL.toString(), tracker.getApiUrl())
                .putInt(BaseParams.CLIENT_AUTO_ID.toString(), Integer.parseInt(tracker.getDefaultTrackMe().get(BaseParams.CLIENT_AUTO_ID)))
                .putString(BaseParams.APP_VERSION.toString(), tracker.getDefaultTrackMe().get(BaseParams.APP_VERSION))
                .putString(BaseParams.COUNTRY.toString(), tracker.getDefaultTrackMe().get(BaseParams.COUNTRY))
                .putInt(BaseParams.PLATFORM.toString(), tracker.getSiteId())
                .putInt(BaseParams.DATA_VERSION.toString(), Integer.parseInt(tracker.getDefaultTrackMe().get(BaseParams.DATA_VERSION)))
                .putInt(BaseParams.USER_ID.toString(), Integer.parseInt(tracker.getDefaultTrackMe().get(BaseParams.USER_ID)));
    }
}
