package com.aigo.analysis.dispatcher;

import com.aigo.analysis.TrackMe;

/**
 * @Description: 负责向服务器发送数据包，采用间隔发送模式
 * @author: Eknow
 * @date: 2021/8/18 11:00
 */
public interface Dispatcher {

    /**
     * 默认连接超时时间 5s
     */
    int DEFAULT_CONNECTION_TIMEOUT = 5 * 1000;

    /**
     * 默认发送数据包时间间隔 2min
     */
    long DEFAULT_DISPATCH_INTERVAL = 120 * 1000;

    /**
     * 获取连接超时时间，建立连接超时和读取响应的超时
     *
     * @return 毫秒
     */
    int getConnectionTimeOut();

    /**
     * 设置连接超时时间
     *
     * @param timeOut 毫秒
     */
    void setConnectionTimeOut(int timeOut);

    /**
     * 获取发送数据包的时间间隔
     * 数据包是分批收集和分发的，这是两个批次之间的时间间隔
     *
     * @return 毫秒
     */
    long getDispatchInterval();

    /**
     * 设置发送数据包的时间间隔
     *
     * @param dispatchInterval 毫秒
     */
    void setDispatchInterval(long dispatchInterval);

    /**
     * 获取是否开启 GZip 压缩
     *
     * @return true 开启，false 关闭
     */
    boolean getDispatchGzipped();

    /**
     * 开启 GZip 压缩
     *
     * @param dispatchGzipped true 开启，false 关闭
     */
    void setDispatchGzipped(boolean dispatchGzipped);

    /**
     * 获取任务调度模式
     *
     * @return {@link DispatchMode}
     */
    DispatchMode getDispatchMode();

    /**
     * 设置任务调度模式
     *
     * @param dispatchMode {@link DispatchMode}
     */
    void setDispatchMode(DispatchMode dispatchMode);

    /**
     * 使得任务马上分发执行
     * <p>
     * 如果调度器没有在工作，唤起一个新的，返回 true
     * 如果正在工作，跳过一次时间间隔，返回 false
     *
     * @return
     */
    boolean forceDispatch();

    /**
     * 异常情况下使得任务马上分发执行，
     * 因为应用程序可能在重新抛出异常后死亡，并阻塞，直到分派完成
     */
    void forceDispatchBlocking();

    /**
     * 清空调度任务
     */
    void clear();

    /**
     * 提交传输任务数据
     *
     * @param trackMe
     */
    void submit(TrackMe trackMe);
}
