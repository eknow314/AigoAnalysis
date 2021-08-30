package com.aigo.analysis.dispatcher;

/**
 * @Description: 数据包发送结果回调
 * @author: Eknow
 * @date: 2021/8/18 15:45
 */
public interface PacketSenderCallback {

    /**
     * 成功回调
     *
     * @param json 响应 json 数据
     */
    void onSuccess(String json);

    /**
     * 错误回调
     *
     * @param errorCode http 状态码
     * @param errorMsg 错误提示信息
     */
    void onError(int errorCode, String errorMsg);
}
