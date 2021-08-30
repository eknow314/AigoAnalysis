package com.aigo.analysis.tools;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.aigo.analysis.AigoAnalysis;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * @Description:
 * @author: Eknow
 * @date: 2021/5/18 11:04
 */
public class DeviceHelper {

    private static final String TAG = AigoAnalysis.tag(DeviceHelper.class);

    private final Context mContext;

    public DeviceHelper(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 获取系统用户语言
     */
    public String getUserLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * 获取系统用户国家
     */
    public String getUserCountry() {
        return Locale.getDefault().getCountry();
    }

    /**
     * 获取应用版本号
     */
    public String getUserVersionName() {
        PackageManager manager = mContext.getPackageManager();
        String name = "";
        try {
            PackageInfo info = manager.getPackageInfo(mContext.getPackageName(), 0);
            name = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }

    /**
     * 获取手机厂商_型号
     */
    public String getAndroidDeviceModel() {
        return Build.BRAND + " " + Build.MODEL;
    }

    /**
     * 获取系统版本号
     */
    public String  getSystemVersion() {
        return "Android_" + Build.VERSION.RELEASE;
    }

    /**
     * 获取 Android 系统用户代理
     */
    public String getUserAgent() {
        String httpAgent = System.getProperty("http.agent");
        if (httpAgent == null || httpAgent.startsWith("Apache-HttpClient/UNAVAILABLE (java")) {
            String dalvik = System.getProperty("java.vm.version");
            if (dalvik == null) {
                dalvik = "0.0.0";
            }
            String android = Build.VERSION.RELEASE;
            String model = Build.MODEL;
            String build = Build.ID;
            httpAgent = String.format(Locale.US,
                    "Dalvik/%s (Linux; U; Android %s; %s Build/%s)",
                    dalvik, android, model, build
            );
        }
        return httpAgent;
    }

    /**
     * 获取最准确的设备分辨率。
     * 在低于API17的设备上，分辨率可能不考虑状态栏/软键。
     * 当前项目可能用不到
     *
     * @return [width, height]
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public int[] getResolution() {
        int width = -1, height = -1;

        Display display;
        try {
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            display = wm.getDefaultDisplay();
        } catch (NullPointerException e) {
            return null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics dm = new DisplayMetrics();
            display.getRealMetrics(dm);
            width = dm.widthPixels;
            height = dm.heightPixels;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            try {
                Method getRawWidth = Display.class.getMethod("getRawWidth");
                Method getRawHeight = Display.class.getMethod("getRawHeight");
                width = (int) getRawWidth.invoke(display);
                height = (int) getRawHeight.invoke(display);
            } catch (Exception e) {
            }
        }

        if (width == -1 || height == -1) {
            DisplayMetrics dm = new DisplayMetrics();
            display.getMetrics(dm);
            width = dm.widthPixels;
            height = dm.heightPixels;
        }

        return new int[]{width, height};
    }
}
