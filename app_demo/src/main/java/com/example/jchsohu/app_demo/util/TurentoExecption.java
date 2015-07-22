package com.example.jchsohu.app_demo.util;

/**
 * Created by jchsohu on 15-7-7.
 */
public class TurentoExecption extends Exception {

    protected static String eMsg = "Turento erro";

    public TurentoExecption(String detailMessage) {
        super(detailMessage);
    }
}

