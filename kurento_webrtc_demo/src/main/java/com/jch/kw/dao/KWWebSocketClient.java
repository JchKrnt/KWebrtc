package com.jch.kw.dao;

import com.jch.kw.util.LooperExecutor;
import com.jch.kw.util.WebSocketChannel;

/**
 * Created by jingbiaowang on 2015/7/22.
 */
public class KWWebSocketClient implements WebSocketChannel.WebSocketEvents, KWWebSocket {


    private WebSocketChannel webSocketChannel;
    public LooperExecutor executor = new LooperExecutor();

    private static KWWebSocketClient instance;

    private KWWebSocketClient() {
        instance = this;
        webSocketChannel = new WebSocketChannel(executor);
    }

    public static KWWebSocketClient getInstance() {
        if (instance == null)
            instance = new KWWebSocketClient();

        return instance;
    }

    public void connect(String urlStr) {
        webSocketChannel.connect(urlStr, this);
    }

    @Override
    public void onError(String e) {

    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onMessage(String msg) {

    }

    @Override
    public void onClosed(String msg) {

    }

    @Override
    public void sendMsg(String msg) {

    }

    @Override
    public void registerRoom(String name) {

    }

    @Override
    public void sendSdp(String userType, String sdp) {

    }
}
