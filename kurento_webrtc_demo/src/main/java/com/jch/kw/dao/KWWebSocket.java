package com.jch.kw.dao;

/**
 * Created by jingbiaowang on 2015/7/22.
 */
public interface KWWebSocket {

    /**
     * 发送聊天信息。
     *
     * @param msg
     */
    public void sendMsg(String msg);

    /**
     * 注册房间号
     *
     * @param name
     */
    public void registerRoom(String name);

    public void sendSdp(String userType, String sdp);

}
