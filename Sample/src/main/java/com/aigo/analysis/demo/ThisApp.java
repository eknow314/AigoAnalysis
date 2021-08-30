package com.aigo.analysis.demo;

import android.app.Application;

import com.aigo.analysis.AigoAnalysisHelper;
import com.aigo.analysis.BuildConfig;
import com.hjq.permissions.XXPermissions;

/**
 * @Description:
 * @author: Eknow
 * @date: 2021/5/18 10:05
 */
public class ThisApp extends Application {

    private static ThisApp mInstance;

    public static ThisApp getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        XXPermissions.setScopedStorage(true);
//        Timber.plant(new Timber.DebugTree());
        AigoAnalysisHelper.getInstance()
                //配置目标地址和站点，安卓客户端填 1
                .config(this, "https://test.smartapi.aigostar.com:3443/analytics/", 1)
                //是否打印日志
                .showLog(BuildConfig.DEBUG)
                //自动上报 activity
                .autoActivityPage()
                //初始化，调用服务端逻辑
                .init();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        AigoAnalysisHelper.getInstance().getTracker().onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        AigoAnalysisHelper.getInstance().getTracker().onTrimMemory(level);
    }
}
