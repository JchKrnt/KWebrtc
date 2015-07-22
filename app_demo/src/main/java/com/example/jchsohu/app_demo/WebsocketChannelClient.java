package com.example.jchsohu.app_demo;

import com.example.jchsohu.app_demo.util.LogCat;
import com.example.jchsohu.app_demo.util.LooperExecutor;

import java.net.URI;
import java.util.ArrayList;

import de.tavendo.autobahn.WebSocket;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;

/**
 * Created by jchsohu on 15-7-3.
 * <p/>
 * WebSocket,Connect to webServer in looper thread.
 */
public class WebsocketChannelClient {

    enum WebsocketState {
        NEWED, CONNECTED, CLOSED, ERORR;
    }

    private ArrayList<String> msgStore = new ArrayList<>();

    public interface WebsocketChannelEvent {
        public void onWebSocketMessage(final String msg);

        public void onWebSocketClose();

        public void onWebSocketError(final String msg);
    }

    private WebsocketState state = WebsocketState.NEWED;
    /**
     * 循环线程。运行Websocket.
     */
    private LooperExecutor executor = null;

    private WebSocketConnection wsc;

    private WebsocketChannelEvent wscEvent;

    public interface WebSocketCallback {
        public String onTestMsg(String msg);
    }

    public WebsocketChannelClient(LooperExecutor executor, WebsocketChannelEvent wscEvent) {
        this.executor = executor;
        this.wsc = new WebSocketConnection();
        this.wscEvent = wscEvent;
    }

    public void connect(final String url) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    wsc.connect(URI.create(url), wsObserver);
                } catch (WebSocketException e) {
                    e.printStackTrace();
                    wscEvent.onWebSocketError(e.getMessage());
                }
            }
        });

    }

    WebSocket.WebSocketConnectionObserver wsObserver = new WebSocket.WebSocketConnectionObserver() {
        @Override
        public void onOpen() {
            LogCat.debug("WebSocket opened");
            state = WebsocketState.CONNECTED;
        }

        @Override
        public void onClose(WebSocketCloseNotification webSocketCloseNotification, String s) {
            state = WebsocketState.CLOSED;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    wscEvent.onWebSocketClose();
                }
            });
        }

        @Override
        public void onTextMessage(final String s) {

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    wscEvent.onWebSocketMessage(s);
                }
            });
        }

        @Override
        public void onRawTextMessage(byte[] bytes) {

        }

        @Override
        public void onBinaryMessage(byte[] bytes) {

        }
    };

    /**
     * 发送信息。
     *
     * @param msg
     */
    public void sendTextMasseg(final String msg) {

        switch (state) {
            case NEWED: {
                msgStore.add(msg);
                break;
            }

            case CONNECTED: {       //确保每条数据是在建立链接后发送的。
                checkvalidThreadMethod(new ValidThreadCall() {
                    @Override
                    public void onValidThread() {
                        if (msgStore.size() > 0) {
                            msgStore.add(msg);
                            for (String amsg :
                                    msgStore) {

                                LogCat.debug("send msg : " + amsg);
                                wsc.sendTextMessage(amsg);
                            }
                            msgStore.clear();
                        } else {
                            LogCat.debug("send msg : " + msg);
                            wsc.sendTextMessage(msg);
                        }
                    }
                });

            }
            case ERORR:
            case CLOSED: {
                LogCat.debug("websocket is closed. You can't send any msg.");
            }


        }
    }

    interface ValidThreadCall {

        public void onValidThread();
    }

    /**
     * make sure the method run in looper thread.
     *
     * @param callback
     */
    private void checkvalidThreadMethod(final ValidThreadCall callback) {
        if (executor.checkOnLooperThread()) {
            callback.onValidThread();
        } else {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    callback.onValidThread();
                }
            });
        }
    }


}
