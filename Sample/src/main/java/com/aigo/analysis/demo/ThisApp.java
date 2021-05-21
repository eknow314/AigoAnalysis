package com.aigo.analysis.demo;

import android.app.Application;

import com.aigo.analysis.TrackerHelper;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

/**
 * @Description:
 * @author: Eknow
 * @date: 2021/5/18 10:05
 */
public class ThisApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //友盟海外版
        UMConfigure.init(this, "60a46cd0c9aacd3bd4db2c99", "GooglePlay", UMConfigure.DEVICE_TYPE_PHONE, "");
        UMConfigure.setLogEnabled(BuildConfig.DEBUG);
        //页面自动采集上报
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);


        //自研数据统计上报
        TrackerHelper.getInstance().init(this, "https://test.smartapi.aigostar.com:3443/analytics/", true);
    }
}
