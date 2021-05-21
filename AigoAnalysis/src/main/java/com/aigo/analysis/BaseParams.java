package com.aigo.analysis;

/**
 * @Description:
 * @author: Eknow
 * @date: 2021/5/18 15:41
 */
public enum BaseParams {

    /**
     * 目标地址
     */
    TARGET_API_URL("api_url"),

    /**
     * 设备从服务端获取到的唯一标识
     */
    CLIENT_AUTO_ID("client_auto_id"),

    /**
     * 安卓设备的唯一 ID
     */
    DEVICE_ID("device_id"),

    /**
     * APP 版本号
     */
    APP_VERSION("ver"),

    /**
     * 国家
     */
    COUNTRY("country"),

    /**
     * 手机厂商_型号
     */
    DEVICE_MODEL("device_model"),

    /**
     * 手机系统版本
     */
    SYSTEM_VERSION("system_version"),

    /**
     * 客户端平台，安卓 = 1,苹果 = 2, H5 = 3,Pad = 4
     */
    PLATFORM("platform"),

    /**
     * 用户 id
     */
    USER_ID("user_id");

    private final String value;

    BaseParams(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
