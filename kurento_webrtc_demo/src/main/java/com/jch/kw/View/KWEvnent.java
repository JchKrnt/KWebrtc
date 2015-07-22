package com.jch.kw.View;

import org.webrtc.SessionDescription;

/**
 * Created by jingbiaowang on 2015/7/22.
 */
public interface KWEvnent {

    public void portError(String msg);

    //from server.

    public void onRemoteAnswer(String sdp);

    public void onDisconnect();

    //接受聊天信息。
    public void onMessage(String msg);

    //from peerconnection.
    public void onLocalSdp(SessionDescription localsdp);

}
