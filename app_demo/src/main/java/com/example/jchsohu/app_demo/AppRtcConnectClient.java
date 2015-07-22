package com.example.jchsohu.app_demo;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

/**
 * Created by jchsohu on 15-7-8.
 */
public interface AppRtcConnectClient {

    /**
     * Send offer SDP to the other participant.
     */
    public void sendOfferSdp(String userType, final SessionDescription sdp);

    /**
     * Send offer SDP of master to the other participant.
     */
    public void sendMasterOfferSdp(final SessionDescription sdp);

    /**
     * Send offer SDP of viewer to the other participant.
     */
    public void sendViewerOfferSdp(final SessionDescription sdp);

    /**
     * Send answer SDP to the other participant.
     */
    public void sendAnswerSdp(final SessionDescription sdp);

    /**
     * Send Ice candidate to the other participant.
     */
    public void sendLocalIceCandidate(final IceCandidate candidate);

    /***
     * 取消链接。
     */
    public void sendstop();

    /**
     * disconnect from websocket.
     */
    public void disconnect();


    interface KurentoConnectdEvents {

        public void onRemoteDesciption(SessionDescription sdp);

        /**
         * Callback fired once remote Ice candidate is received.
         */
        public void onRemoteIceCandidate(final IceCandidate candidate);

        /**
         * Callback fired once channel is closed.
         */
        public void onChannelClose();

        /**
         * Callback fired once channel error happened.
         */
        public void onChannelError(final String description);
    }
}
