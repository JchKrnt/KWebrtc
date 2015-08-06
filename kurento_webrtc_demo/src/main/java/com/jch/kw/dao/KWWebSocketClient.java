package com.jch.kw.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.jch.kw.View.KWEvent;
import com.jch.kw.bean.UserType;
import com.jch.kw.util.LogCat;
import com.jch.kw.util.LooperExecutor;
import com.jch.kw.util.WebSocketChannel;

import org.json.JSONException;

import java.lang.reflect.Type;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * Created by jingbiaowang on 2015/7/22.
 */
public class KWWebSocketClient implements WebSocketChannel.WebSocketEvents, KWWebSocket {

    public interface OnListListener {
        void onListListener(ArrayList<String> masters);

        void onConnectionOpened();

        void onError(String msg);

        void onClose(String msg);
    }

    private WebSocketChannel webSocketChannel;
    private LooperExecutor executor;

    public void setListListener(OnListListener listListener) {
        this.listListener = listListener;
    }

    public void setEvent(KWEvent event) {
        this.event = event;
    }

    private static KWWebSocketClient instance;
    private KWEvent event;

    private OnListListener listListener;
    Gson gson = new GsonBuilder().create();

    public KWWebSocketClient() {
        webSocketChannel = new WebSocketChannel();

    }

    public static KWWebSocketClient getInstance() {
        if (instance == null) {

            LogCat.v("KWWebSocketClient new instance");
            instance = new KWWebSocketClient();
        } else {
            LogCat.v("KWWebSocketClient old instance");
        }
        return instance;
    }

    public void setExecutor(LooperExecutor executor) {
        this.executor = executor;
        webSocketChannel.setExecutor(executor);
    }

    public void connect(String urlStr) {
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
        if (listListener != null)
            listListener.onConnectionOpened();
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
        } else if (idStr != null && "listResponse".equals(idStr)) {
            onListResponse(jsonMsg);
        }

    }

    /**
     * 服务器返回list。
     *
     * @param jsonMsg
     */
    private void onListResponse(JsonObject jsonMsg) {
        String reponseFlag = jsonMsg.get("response").getAsString();
        if (reponseFlag != null && reponseFlag.equals("accepted")) {

            LogCat.debug("master id : " + jsonMsg.get("masters").getAsString());

            Type collectionType = new TypeToken<Set<String>>() {
            }.getType();
            Set<String> masters = gson.fromJson(jsonMsg.get("masters").getAsString(), collectionType);
            listListener.onListListener(new ArrayList<>(masters));
        } else {
            listListener.onError(jsonMsg.get("message").getAsString());
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
        if (listListener != null)
            listListener.onClose(msg);
        executor.requestStop();
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
    }

    public void close() {
        webSocketChannel.disconnect(true);
    }

    public void sendListerRequest() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                JsonObject listRequest = new JsonObject();
                listRequest.addProperty("id", "list");
                webSocketChannel.sendMsg(listRequest.toString());
            }
        });
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


    public void sendSdp(final UserType userType, final String sdp, final String masterId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", userType.getVauleStr());
                jsonObject.addProperty("sdpOffer", sdp);
                if (userType == UserType.VIEWER) {
                    jsonObject.addProperty("masterKey", masterId);
                }

                webSocketChannel.sendMsg(jsonObject.toString());
            }
        });

    }

}
