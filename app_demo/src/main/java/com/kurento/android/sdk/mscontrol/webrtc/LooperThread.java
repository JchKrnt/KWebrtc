package com.kurento.android.sdk.mscontrol.webrtc;


import android.os.Handler;
import android.os.Looper;

import com.example.jchsohu.app_demo.util.LogCat;

public class LooperThread extends Thread {

//    private static final Logger log = LoggerFactory
//            .getLogger(LooperThread.class.getSimpleName());

    private Handler mHandler;
    private Object initControl = new Object();
    private boolean initiated = false;
    private boolean quit = false;

    @Override
    public void run() {
        Looper.prepare();
        synchronized (this) {
            mHandler = new Handler();
            if (quit) {
                quit();
            }
        }
        synchronized (initControl) {
            initiated = true;
            initControl.notifyAll();
        }
        Looper.loop();
    }

    public boolean post(Runnable r) {
        try {
            synchronized (initControl) {
                if (!initiated) {
                    initControl.wait();
                }
            }
            return mHandler.post(r);
        } catch (InterruptedException e) {
            LogCat.e("Cannot run", e.getMessage());
            return false;
        }
    }

    public synchronized void quit() {
        quit = true;
        if (mHandler != null) {
            mHandler.getLooper().quit();
        }
    }
}
