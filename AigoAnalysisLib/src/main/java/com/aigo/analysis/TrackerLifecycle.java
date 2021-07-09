package com.aigo.analysis;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.aigo.analysis.event.GetInitDataEvent;
import com.aigo.analysis.event.PageEvent;

/**
 * @Description: 自动实现页面上报
 * @author: Eknow
 * @date: 2021/5/18 14:58
 */
public class TrackerLifecycle implements Application.ActivityLifecycleCallbacks {

    private static int node = 0;
    private static int resumed = 0;
    private static int paused = 0;
    private static long pageStartTime = 0;
    private static String backPageName = "";

    static void with(Application application) {
        application.registerActivityLifecycleCallbacks(new com.aigo.analysis.TrackerLifecycle());
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        ++node;
        if (node == 1) {
            //调用初始化上报
            TrackerHelper.getInstance().with(new GetInitDataEvent());
        }
        // TODO: 2021/5/18 是否要去做 activity 里面的 fragment 的统计
//        if (activity instanceof FragmentActivity) {
//            ((FragmentActivity) activity).getSupportFragmentManager()
//                    .registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, true);
//        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        // TODO: 2021/5/18 页面打点 开始 计时
        ++resumed;
        pageStartTime = System.currentTimeMillis();
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        // TODO: 2021/5/18 页面打点 结束 开始上报
        ++paused;
        long stopTime = (System.currentTimeMillis() - pageStartTime) / 1000;
        if (stopTime > 0 && pageStartTime > 0) {
            TrackerHelper.getInstance().with(new PageEvent(activity.getComponentName().getClassName(),
                    backPageName,
                    stopTime));
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

    private final FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {

        private long pageStartTime = 0;

        @Override
        public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
            super.onFragmentViewCreated(fm, f, v, savedInstanceState);
            pageStartTime = System.currentTimeMillis();
        }

        @Override
        public void onFragmentViewDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
            super.onFragmentViewDestroyed(fm, f);
            long stopTime = (System.currentTimeMillis() - pageStartTime) / 1000;
            if (stopTime > 0) {

            }
            pageStartTime = 0;
        }
    };
}
