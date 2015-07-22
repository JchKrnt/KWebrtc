package com.example.jchsohu.app_demo;

import android.app.Application;

/**
 * Created by jchsohu on 15-7-7.
 */
public class TurentoApp extends Application {

    private static TurentoApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static TurentoApp getInstance() {
        return instance;
    }
}
