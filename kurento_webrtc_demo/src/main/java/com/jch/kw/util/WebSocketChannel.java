package com.jch.kw.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import de.tavendo.autobahn.WebSocket;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;

/**
 * Created by jingbiaowang on 2015/7/21.
 * <p/>
 * Websocket channel.
 */
public class WebSocketChannel {


    private WebSocketConnection wsc;
    private LooperExecutor executor;
    private WebSocketEvents wsEvents;
    private String wsUrl;

    private ArrayList<String> msges = new ArrayList<>();

    private Object waitLock = new Object();
    private static final int WAIT_TIME = 1000;
    private boolean lockEvent;

    /**
     * webSocket state.
     */
    private WebSocketState state;


    public interface WebSocketEvents {

        void onError(String e);

        void onConnected();

        void onMessage(String msg);

        void onClosed(String msg);
    }

    enum WebSocketState {
        NEW, CONNECTED, CLOSED, ERROR
    }


    public WebSocketChannel(LooperExecutor executor) {
        this.wsc = new WebSocketConnection();
        state = WebSocketState.NEW;
        this.executor = executor;

    }

    /**
     * 链接 .
     *
     * @param urlStr websocket url.
     */
    public void connect(String urlStr, WebSocketEvents wsEvents) {
        this.wsUrl = urlStr;
        this.wsEvents = wsEvents;
        checkvalidThreadMethod(new ValidThreadCall() {
            @Override
            public void onValidThread() {
                try {
                    wsc.connect(new URI(wsUrl), new KWebSocketObserver());
                } catch (WebSocketException e) {
                    e.printStackTrace();
                    WebSocketChannel.this.wsEvents.onError(e.getMessage());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    WebSocketChannel.this.wsEvents.onError(e.getMessage());
                }

            }
        });

    }

    public void sendMsg(final String msg) {

        checkvalidThreadMethod(new ValidThreadCall() {
            @Override
            public void onValidThread() {
                sendMsgs(msg);
            }
        });

    }

    /**
     * Store the msg if the websocket hasn't connected.
     *
     * @param msg
     */
    private void sendMsgs(String msg) {

        switch (state) {

            case NEW: {

                msges.add(msg);
                break;
            }

            case CONNECTED: {

                for (String sendMsg : msges) {

                    wsc.sendTextMessage(sendMsg);
                }

                msges.clear();
                wsc.sendTextMessage(msg);
                break;
            }

            case CLOSED:
            case ERROR: {

                if (!wsc.isConnected()) {
                    LogCat.e("the websocket isn't opening. you can't send any sth.");
                    break;
                }
            }

        }

    }

    public void disconnect(final boolean waitForComplete) {

        //TODO send server to disconnect server.

        checkvalidThreadMethod(new ValidThreadCall() {
            @Override
            public void onValidThread() {
                if (wsc.isConnected()) {
                    wsc.disconnect();

                    if (waitForComplete) {

                        synchronized (waitLock) {
                            while (!lockEvent) {
                                try {
                                    waitLock.wait(WAIT_TIME);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                }
            }
        });

    }


    class KWebSocketObserver implements WebSocket.WebSocketConnectionObserver {


        @Override
        public void onOpen() {
            state = WebSocketState.CONNECTED;
            lockEvent = false;
            wsEvents.onConnected();
        }

        @Override
        public void onClose(WebSocketCloseNotification webSocketCloseNotification, final String s) {
            state = WebSocketState.CLOSED;

            synchronized (waitLock) {
                lockEvent = true;
                waitLock.notify();
            }
            checkvalidThreadMethod(new ValidThreadCall() {
                @Override
                public void onValidThread() {
                    wsEvents.onClosed(s);
                }
            });

        }

        @Override
        public void onTextMessage(String s) {
            wsEvents.onMessage(s);
        }

        @Override
        public void onRawTextMessage(byte[] bytes) {

        }

        @Override
        public void onBinaryMessage(byte[] bytes) {

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
