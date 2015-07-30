package com.jch.kw.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.jch.kw.View.KWEvent;
import com.jch.kw.util.LogCat;
import com.jch.kw.util.LooperExecutor;
import com.jch.kw.util.WebSocketChannel;

import org.json.JSONException;

/**
 * Created by jingbiaowang on 2015/7/22.
 */
public class KWWebSocketClient implements WebSocketChannel.WebSocketEvents, KWWebSocket {


    private WebSocketChannel webSocketChannel;
    public LooperExecutor executor = new LooperExecutor();

    private static KWWebSocketClient instance;
    private KWEvent event;
    Gson gson = new GsonBuilder().create();

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
        this.event = event;
        executor.requestStart();
        webSocketChannel.connect(urlStr, this);
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

        LogCat.debug("receive msg : " + msg);

        JsonObject jsonMsg = gson.fromJson(msg, JsonObject.class);
        String idStr = jsonMsg.get("id").getAsString();
        if (idStr != null && ((idStr.equals("masterResponse") || idStr.equals("viewerResponse")))) {
            onResponse(jsonMsg);
        } else if (idStr != null && "stopCommunication".equals(idStr)) {
            event.onDisconnect();
        }

    }


    /**
     * 服务器返回数据。
     *
     * @param responseObj
     */

    private void onResponse(JsonObject responseObj) {
        if (responseObj.has("response") && "accepted".equals(responseObj.get("response").getAsString())) {
            event.onRemoteAnswer(responseObj.get("sdpAnswer").getAsString());
        } else {
            event.portError("'Call not accepted for the following reason: " + responseObj.get("message").getAsString());
        }
    }

    @Override
    public void onClosed(String msg) {

        event.onDisconnect();
    }

    private void sendStop() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                String stopMsg = "{id:'stop'}";
                webSocketChannel.sendMsg(stopMsg);
            }
        });

        //TODO client stop.
    }

    public void disconnect() {

        sendStop();
        webSocketChannel.disconnect(false);
        executor.requestStop();
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
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", userType);
                jsonObject.addProperty("sdpOffer", sdp);

                webSocketChannel.sendMsg(jsonObject.toString());
            }
        });


    }
}
