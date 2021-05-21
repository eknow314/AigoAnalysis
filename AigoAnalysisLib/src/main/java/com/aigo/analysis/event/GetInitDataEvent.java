package com.aigo.analysis.event;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;

import com.aigo.analysis.BaseParams;
import com.aigo.analysis.Tracker;
import com.aigo.analysis.work.GetInitDataWork;

/**
 * @Description: 获取数据分析系统的设备唯一标识
 * @author: Eknow
 * @date: 2021/5/18 10:27
 */
public class GetInitDataEvent extends BaseEvent implements IWorkRequestEvent {

    public GetInitDataEvent() {

    }

    @Override
    public OneTimeWorkRequest send(Tracker tracker) {
        Data data = commonData(tracker)
                .putString(BaseParams.DEVICE_ID.toString(), tracker.getDefaultTrackMe().get(BaseParams.DEVICE_ID))
                .putString(BaseParams.COUNTRY.toString(), tracker.getDefaultTrackMe().get(BaseParams.COUNTRY))
                .putString(BaseParams.DEVICE_MODEL.toString(), tracker.getDefaultTrackMe().get(BaseParams.DEVICE_MODEL))
                .putString(BaseParams.SYSTEM_VERSION.toString(), tracker.getDefaultTrackMe().get(BaseParams.SYSTEM_VERSION))
                .build();
        return new OneTimeWorkRequest.Builder(GetInitDataWork.class)
                .setInputData(data)
                .build();
    }
}
