package com.aigo.analysis.extra;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aigo.analysis.AigoAnalysisHelper;

/**
 * @Description: 实现自动页面上报
 * @author: Eknow
 * @date: 2021/8/26 15:33
 */
public class TrackLifecycle implements Application.ActivityLifecycleCallbacks {

    private static int node = 0;
    private static int resumed = 0;
    private static int paused = 0;
    private static long pageStartTime = 0;
    private static String backPageName = "";

    public static void with(Application application) {
        application.registerActivityLifecycleCallbacks(new TrackLifecycle());
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        //页面打点 开始 计时
        ++resumed;
        pageStartTime = System.currentTimeMillis();
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        //页面打点 结束 开始上报
        ++paused;
        long stopTime = (System.currentTimeMillis() - pageStartTime) / 1000;
        if (stopTime > 0 && pageStartTime > 0) {
            AigoAnalysisHelper.getInstance()
                    .screen(activity.getComponentName().getClassName(), backPageName, stopTime);
        }
        pageStartTime = 0;
        backPageName = activity.getComponentName().getClassName();
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}
