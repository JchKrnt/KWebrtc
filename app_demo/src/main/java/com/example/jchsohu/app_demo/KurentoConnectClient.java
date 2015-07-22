package com.example.jchsohu.app_demo;

import com.example.jchsohu.app_demo.bean.UserType;
import com.example.jchsohu.app_demo.util.Constant;
import com.example.jchsohu.app_demo.util.LogCat;
import com.example.jchsohu.app_demo.util.LooperExecutor;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.lang.reflect.Type;

/**
 * Created by jchsohu on 15-7-8.
 * <p/>
 * connect to server client .
 * <p/>
 * Deal with interaction to server.
 */
public class KurentoConnectClient implements AppRtcConnectClient, WebsocketChannelClient.WebsocketChannelEvent {

    private LooperExecutor executor = null;

    private WebsocketChannelClient wcc = null;
    private KurentoConnectdEvents events;

    public KurentoConnectClient(KurentoConnectdEvents events) {
        this.executor = new LooperExecutor();
        executor.requestStart();
        this.events = events;
        wcc = new WebsocketChannelClient(executor, this);
        wcc.connect(Constant.HostUrl);
    }

    @Override
    public void sendOfferSdp(final String userType, final SessionDescription sdp) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                String sdpStr = sdp.description;

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("id", userType);
                    jsonObject.put("sdpOffer", sdp.description);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                wcc.sendTextMasseg(jsonObject.toString());
            }
        });

    }

    @Override
    public void sendMasterOfferSdp(SessionDescription sdp) {

    }

    @Override
    public void sendViewerOfferSdp(SessionDescription sdp) {

    }

    @Override
    public void sendAnswerSdp(SessionDescription sdp) {

    }

    @Override
    public void sendLocalIceCandidate(IceCandidate candidate) {

    }

    @Override
    public void sendstop() {
        String stopMsg = "{id:'stop'}";
        wcc.sendTextMasseg(stopMsg);
        //TODO client stop.
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void onWebSocketMessage(String msg) {
        //TODO  parse json msg.
        try {
            LogCat.debug("receive msg : " + msg);
            JSONObject jsonObject = new JSONObject(msg);
            String idStr = jsonObject.getString("id");
            if (idStr != null && (idStr.equals("masterResponse") || idStr.equals("viewerResponse"))) {
                onResponse(jsonObject);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    /**
     * 服务器返回数据。
     *
     * @param responseObj
     */
    private void onResponse(JSONObject responseObj) {
        try {
            if (responseObj.has("response") && "accepted".equals(responseObj.getString("response"))) {
                SessionDescription answerSdp = new SessionDescription(SessionDescription.Type.ANSWER, responseObj.getString("sdpAnswer"));
                events.onRemoteDesciption(answerSdp);
            } else {
                events.onChannelError("'Call not accepted for the following reason: " + responseObj.getString("message"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWebSocketClose() {

    }

    @Override
    public void onWebSocketError(String msg) {
        LogCat.e(msg);
    }
}
