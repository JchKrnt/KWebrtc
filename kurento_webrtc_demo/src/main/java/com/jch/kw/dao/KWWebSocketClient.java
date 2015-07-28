package com.jch.kw.dao;

import com.jch.kw.View.KWEvent;
import com.jch.kw.util.LogCat;
import com.jch.kw.util.LooperExecutor;
import com.jch.kw.util.WebSocketChannel;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.SessionDescription;

/**
 * Created by jingbiaowang on 2015/7/22.
 */
public class KWWebSocketClient implements WebSocketChannel.WebSocketEvents, KWWebSocket {


    private WebSocketChannel webSocketChannel;
    public LooperExecutor executor = new LooperExecutor();

    private static KWWebSocketClient instance;
    private KWEvent event;

    private KWWebSocketClient() {
        instance = this;
        webSocketChannel = new WebSocketChannel(executor);
    }

    public static KWWebSocketClient getInstance() {
        if (instance == null)
            instance = new KWWebSocketClient();

        return instance;
    }

    public void connect(String urlStr, KWEvent event) {
        webSocketChannel.connect(urlStr, this);
        this.event = event;
    }

    @Override
    public void onError(String e) {

        event.portError(e);
    }

    @Override
    public void onConnected() {
        LogCat.debug("websocket connected...");
    }

    @Override
    public void onMessage(String msg) {

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
                event.onRemoteAnswer(responseObj.getString("sdpAnswer"));
            } else {
                event.portError("'Call not accepted for the following reason: " + responseObj.getString("message"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClosed(String msg) {

        event.onDisconnect();
    }

    public void sendStop() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                String stopMsg = "{id:'stop'}";
                webSocketChannel.sendMsg(stopMsg);
            }
        });

        //TODO client stop.
    }

    @Override
    public void sendMsg(final String msg) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                //TODO package msg.
                webSocketChannel.sendMsg(msg);
            }
        });
    }

    @Override
    public void registerRoom(String name) {

    }

    @Override
    public void sendSdp(final String userType, final String sdp) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("id", userType);
                    jsonObject.put("sdpOffer", sdp);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                webSocketChannel.sendMsg(jsonObject.toString());
            }
        });


    }
}
