package com.example.jchsohu.app_demo.util;

public class TurentoNetWorkExecption extends TurentoExecption {


    public TurentoNetWorkExecption(String detailMessage) {

        super(eMsg + ":can't connect service. " + detailMessage);
    }
}
