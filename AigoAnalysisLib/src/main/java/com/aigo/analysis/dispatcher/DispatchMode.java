package com.aigo.analysis.dispatcher;

import androidx.annotation.Nullable;

/**
 * @Description: 任务调度模式
 * @author: Eknow
 * @date: 2021/8/18 11:08
 */
public enum DispatchMode {

    /**
     * 有网络即派发
     */
    ALWAYS("always"),

    /**
     * 仅限 WIFI 连接下派发
     */
    WIFI_ONLY("wifi_only"),

    /**
     * 当程序异常情况下，将数据包缓存
     */
    EXCEPTION("exception");

    private final String key;

    DispatchMode(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }

    @Nullable
    public static DispatchMode fromString(String raw) {
        for (DispatchMode mode : DispatchMode.values()) {
            if (mode.key.equals(raw)) {
                return mode;
            }
        }
        return null;
    }
}
