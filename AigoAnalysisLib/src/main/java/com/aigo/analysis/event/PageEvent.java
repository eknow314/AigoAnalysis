package com.aigo.analysis.event;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;

import com.aigo.analysis.AigoAnalysis;
import com.aigo.analysis.Tracker;
import com.aigo.analysis.work.PageWork;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Date;

/**
 * @Description: 页面打点上报，页面名称和停留时间
 * @author: Eknow
 * @date: 2021/5/18 19:26
 */
public class PageEvent extends BaseEvent implements IWorkRequestEvent {

    private String pageName;
    private String backPageName;
    private long seconds;

    /**
     * @param pageName     activity/fragment的完整名称
     * @param backPageName 上级页面名称
     * @param seconds      页面停留秒数
     */
    public PageEvent(String pageName, String backPageName, long seconds) {
        this.pageName = pageName;
        this.backPageName = backPageName;
        this.seconds = seconds;
    }

    @Override
    public OneTimeWorkRequest send(Tracker tracker) {
        DateTime nowUTC = new DateTime(DateTimeZone.UTC);
        DateTime nowLocal = nowUTC.withZone(DateTimeZone.getDefault());
        Date dLocal = nowLocal.toLocalDateTime().toDate();
        Date dUTC = nowUTC.toLocalDateTime().toDate();

        Data data = commonData(tracker)
                .putLong("time", dUTC.getTime())
                .putLong("local_time", dLocal.getTime())
                .putInt("type", 0)
                .putInt("tag_id", 0)
                .putLong("seconds", seconds)
                .putString("back_page_name", backPageName)
                .putString("page_name", pageName)
                .build();

        return new OneTimeWorkRequest.Builder(PageWork.class)
                .setInputData(data)
                .addTag(AigoAnalysis.tag(PageEvent.class))
                .build();
    }
}
