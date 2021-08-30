package com.aigo.analysis;

/**
 * @Description: 查询参数
 * @author: Eknow
 * @date: 2021/8/18 10:09
 */
public enum QueryParams {

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
     * 设备名称
     */
    DEVICE("device"),
    DEVICE_OS("os"),

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
     * 数据版本，设备从服务端获取到的数据版本标识，每次上报都要传
     */
    DATA_VERSION("data_version"),

    /**
     * 用户 id
     */
    USER_ID("user_id"),

    /**
     * UTC时间戳
     */
    TIME("time"),

    /**
     * 客户端本地时间戳
     */
    LOCAL_TIME("local_time"),

    /**
     * 页面节点统计上报参数
     */
    SECONDS("seconds"),
    PAGE_NAME("page_name"),
    BACK_PAGE_NAME("back_page_name"),

    /**
     * 自定义事件统计上报参数
     */
    COUNT("count"),
    BASE_EVENT("base_event"),
    EVENT("event"),
    EXTENSION("extension"),
    EVENT_NAME("event_name"),
    EVENT_VALUE("event_value"),

    /**
     * 批量事件接口参数
     */
    BATCH_DETAIL("batch_detail");

    private final String value;

    QueryParams(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
