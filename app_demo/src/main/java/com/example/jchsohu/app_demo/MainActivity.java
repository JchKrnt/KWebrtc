/*
 * (C) Copyright 2014 Kurento (http://kurento.org/)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package com.example.jchsohu.app_demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.jchsohu.app_demo.bean.UserType;
import com.example.jchsohu.app_demo.util.LogCat;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity implements View.OnClickListener, WebsocketChannelClient.WebSocketCallback {


    private TextView msgTv;
    private Button viewerBtn, broadcastBtn;

    /**
     * 访问类型。
     */
    public enum CallType {
        BROADCAST("master"), VIEWER("viewer");

        private String value;

        private CallType(String type) {
            this.value = type;
        }

        public String getTypeValue() {
            return value;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_main);

        initialize();

    }

    private boolean enableAudio = false;

    public void stopAudio(View view) {
        enableAudio = !enableAudio;
    }

    private boolean enableVideo = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        sessionA.finish();
//        sessionB.finish();
    }

    private void initialize() {

        viewerBtn = (Button) findViewById(R.id.btn_viewer);
        broadcastBtn = (Button) findViewById(R.id.btn_broadcast);
        msgTv = (TextView) findViewById(R.id.msg_tv);

        viewerBtn.setOnClickListener(this);
        broadcastBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        Intent intent = new Intent(MainActivity.this, VideoActivity.class);

        switch (v.getId()) {
            case R.id.btn_broadcast: {

                intent.putExtra(VideoActivity.USERTYPE_KEY, UserType.BROADCAST.getTypeValue());
                break;
            }
            case R.id.btn_viewer: {
                intent.putExtra(VideoActivity.USERTYPE_KEY, UserType.VIEWER.getTypeValue());
                break;
            }

            default: {
                //todo exception.
            }
        }

        startActivity(intent);

    }


    @Override
    public String onTestMsg(String msg) {
        return null;
    }

}

