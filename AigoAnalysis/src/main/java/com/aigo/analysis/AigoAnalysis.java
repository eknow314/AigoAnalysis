package com.aigo.analysis;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.aigo.analysis.tools.Checksum;
import com.aigo.analysis.tools.DeviceHelper;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * @Description: Aigo 数据分析埋点
 * @author: Eknow
 * @date: 2021/5/14 11:20
 */
public class AigoAnalysis {

    public static final String TEST_SERVER_URL = "https://test.smartapi.aigostar.com:3443/analytics/";

    public static final String LOGGER_PREFIX = "AIGO_ANALYSIS:";
    private static final String TAG = AigoAnalysis.tag(AigoAnalysis.class);
    private static final String BASE_PREFERENCE_FILE = "com.aigo.analysis.sdk";

    @SuppressLint("StaticFieldLeak")
    private static AigoAnalysis sInstance;

    private final Map<Tracker, SharedPreferences> mPreferenceMap = new HashMap<>();
    private final Context mContext;
    private final SharedPreferences mBasePreferences;

    public static synchronized AigoAnalysis getInstance(Context context) {
        if (sInstance == null) {
            synchronized (AigoAnalysis.class) {
                if (sInstance == null) {
                    sInstance = new AigoAnalysis(context);
                }
            }
        }
        return sInstance;
    }

    private AigoAnalysis(Context context) {
        mContext = context.getApplicationContext();
        mBasePreferences = context.getSharedPreferences(BASE_PREFERENCE_FILE, Context.MODE_PRIVATE);
    }

    public Context getContext() {
        return mContext;
    }

    public SharedPreferences getPreferences() {
        return mBasePreferences;
    }

    public SharedPreferences getTrackerPreferences(@NonNull Tracker tracker) {
        synchronized (mPreferenceMap) {
            SharedPreferences newPrefs = mPreferenceMap.get(tracker);
            if (newPrefs == null) {
                String prefName;
                try {
                    prefName = BASE_PREFERENCE_FILE + "_" + Checksum.getMD5Checksum(tracker.getName());
                } catch (Exception e) {
                    Timber.tag(TAG).e(e);
                    prefName = BASE_PREFERENCE_FILE + "_" + tracker.getName();
                }
                newPrefs = getContext().getSharedPreferences(prefName, Context.MODE_PRIVATE);
                mPreferenceMap.put(tracker, newPrefs);
            }
            return newPrefs;
        }
    }

    public DeviceHelper getDeviceHelper() {
        return new DeviceHelper(mContext);
    }

    public static String tag(Class... classes) {
        String[] tags = new String[classes.length];
        for (int i = 0; i < classes.length; i++) {
            tags[i] = classes[i].getSimpleName();
        }
        return tag(tags);
    }

    public static String tag(String... tags) {
        StringBuilder sb = new StringBuilder(LOGGER_PREFIX);
        for (int i = 0; i < tags.length; i++) {
            sb.append(tags[i]);
            if (i < tags.length - 1) {
                sb.append(":");
            }
        }
        return sb.toString();
    }
}
