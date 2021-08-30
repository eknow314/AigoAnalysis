package com.aigo.analysis.dispatcher;

/**
 * @Description: 数据包发送器
 * @author: Eknow
 * @date: 2021/8/18 14:57
 */
public interface PacketSender {

    /**
     * 发送数据包
     *
     * @param packet
     * @param callback
     * @return
     */
    boolean send(Packet packet, PacketSenderCallback callback);

    /**
     * 设置超时时间
     *
     * @param timeout
     */
    void setTimeout(long timeout);

    /**
     * 设置数据压缩
     *
     * @param gzip
     */
    void setGzipData(boolean gzip);
}
