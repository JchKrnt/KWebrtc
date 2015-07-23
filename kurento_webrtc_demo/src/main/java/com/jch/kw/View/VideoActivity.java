package com.jch.kw.View;

import android.app.Activity;
import android.os.Bundle;

import com.jch.kw.R;

import org.webrtc.SessionDescription;

public class VideoActivity extends Activity implements KWEvnent {

    public static final String paramKey = "IntentKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
    }


    @Override
    public void portError(String msg) {

    }

    @Override
    public void onRemoteAnswer(String sdp) {

    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onMessage(String msg) {

    }

    @Override
    public void onLocalSdp(SessionDescription localsdp) {

    }
}
