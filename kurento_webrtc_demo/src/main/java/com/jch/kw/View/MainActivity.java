package com.jch.kw.View;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jch.kw.R;
import com.jch.kw.bean.SettingsBean;
import com.jch.kw.bean.UserType;
import com.jch.kw.dao.KWWebSocketClient;
import com.jch.kw.util.Constant;
import com.jch.kw.util.LogCat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button masterbtn;
    private Button viewerbtn;
    private ListView roomlist;

    private String keyprefVideoCallEnabled;
    private String keyprefResolution;
    private String keyprefFps;
    private String keyprefVideoBitrateType;
    private String keyprefVideoBitrateValue;
    private String keyprefVideoCodec;
    private String keyprefAudioBitrateType;
    private String keyprefAudioBitrateValue;
    private String keyprefAudioCodec;
    private String keyprefHwCodecAcceleration;
    private String keyprefCpuUsageDetection;
    private String keyprefDisplayHud;
    private String keyprefRoomServerUrl;
    private String keyprefRoom;
    private String keyprefRoomList;
    private ProgressBar socketPgb;
    private KWWebSocketClient socketClient;
    private boolean WebSocketOk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, false);
        initSettingKey();

        initialize();

        socketClient = KWWebSocketClient.getInstance();
        socketClient.setListListener(new ListListener());
        socketClient.connect(Constant.HostUrl);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socketClient != null) {
            socketClient.close();
            socketClient = null;
        }
    }

    private SettingsBean getSettingsValues() {

        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences(this);
        SettingsBean sBean = new SettingsBean();
        String roomUrl = spf.getString(
                keyprefRoomServerUrl,
                getString(R.string.pref_room_server_url_default));
        sBean.setServerUrl(roomUrl);

        // Video call enabled flag.
        boolean videoCallEnabled = spf.getBoolean(keyprefVideoCallEnabled,
                Boolean.valueOf(getString(R.string.pref_videocall_default)));
        sBean.setVideoCallEnable(videoCallEnabled);

        // Get default codecs.
        String videoCodec = spf.getString(keyprefVideoCodec,
                getString(R.string.pref_videocodec_default));
        sBean.setVideoCode(videoCodec);
        String audioCodec = spf.getString(keyprefAudioCodec,
                getString(R.string.pref_audiocodec_default));
        sBean.setAudioCode(audioCodec);

        // Check HW codec flag.
        boolean hwCodec = spf.getBoolean(keyprefHwCodecAcceleration,
                Boolean.valueOf(getString(R.string.pref_hwcodec_default)));
        sBean.setHwCodeEnable(hwCodec);

        // Get video resolution from settings.
        int videoWidth = 0;
        int videoHeight = 0;
        String resolution = spf.getString(keyprefResolution,
                getString(R.string.pref_resolution_default));
        String[] dimensions = resolution.split("[ x]+");
        if (dimensions.length == 2) {
            try {
                videoWidth = Integer.parseInt(dimensions[0]);
                videoHeight = Integer.parseInt(dimensions[1]);
            } catch (NumberFormatException e) {
                videoWidth = 0;
                videoHeight = 0;
                LogCat.e("Wrong video resolution setting: " + resolution);
            }
        }
        sBean.setVideoWidth(videoWidth);
        sBean.setVideoHeight(videoHeight);

        // Get camera fps from settings.
        int cameraFps = 0;
        String fps = spf.getString(keyprefFps,
                getString(R.string.pref_fps_default));
        String[] fpsValues = fps.split("[ x]+");
        if (fpsValues.length == 2) {
            try {
                cameraFps = Integer.parseInt(fpsValues[0]);
            } catch (NumberFormatException e) {
                LogCat.e("Wrong camera fps setting: " + fps);
            }
        }

        sBean.setFps(cameraFps);

        // Get video and audio start bitrate.
        int videoStartBitrate = 0;
        String bitrateTypeDefault = getString(
                R.string.pref_startvideobitrate_default);
        String bitrateType = spf.getString(
                keyprefVideoBitrateType, bitrateTypeDefault);
        sBean.setStartVidoBitrate(bitrateType);
        if (!bitrateType.equals(bitrateTypeDefault)) {
            String bitrateValue = spf.getString(keyprefVideoBitrateValue,
                    getString(R.string.pref_startvideobitratevalue_default));
            videoStartBitrate = Integer.parseInt(bitrateValue);
        }
        sBean.setStartVidoBitrateValue(videoStartBitrate);

        int audioStartBitrate = 0;
        bitrateTypeDefault = getString(R.string.pref_startaudiobitrate_default);
        bitrateType = spf.getString(
                keyprefAudioBitrateType, bitrateTypeDefault);
        sBean.setAudioBitrate(bitrateType);
        if (!bitrateType.equals(bitrateTypeDefault)) {
            String bitrateValue = spf.getString(keyprefAudioBitrateValue,
                    getString(R.string.pref_startaudiobitratevalue_default));
            audioStartBitrate = Integer.parseInt(bitrateValue);
        }
        sBean.setAudioBitrateValue(audioStartBitrate);
        // Test if CpuOveruseDetection should be disabled. By default is on.
        boolean cpuOveruseDetection = spf.getBoolean(
                keyprefCpuUsageDetection,
                Boolean.valueOf(
                        getString(R.string.pref_cpu_usage_detection_default)));
        sBean.setCpuUsageDetection(cpuOveruseDetection);
        // Check statistics display option.
        boolean displayHud = spf.getBoolean(keyprefDisplayHud,
                Boolean.valueOf(getString(R.string.pref_displayhud_default)));
        sBean.setDisplayHud(displayHud);
        return sBean;
    }

    private void initSettingKey() {
        keyprefResolution = getString(R.string.pref_resolution_key);
        keyprefFps = getString(R.string.pref_fps_key);
        keyprefVideoBitrateType = getString(R.string.pref_startvideobitrate_key);
        keyprefVideoBitrateValue = getString(R.string.pref_startvideobitratevalue_key);
        keyprefVideoCodec = getString(R.string.pref_videocodec_key);
        keyprefHwCodecAcceleration = getString(R.string.pref_hwcodec_key);
        keyprefAudioBitrateType = getString(R.string.pref_startaudiobitrate_key);
        keyprefAudioBitrateValue = getString(R.string.pref_startaudiobitratevalue_key);
        keyprefAudioCodec = getString(R.string.pref_audiocodec_key);
        keyprefCpuUsageDetection = getString(R.string.pref_cpu_usage_detection_key);
        keyprefDisplayHud = getString(R.string.pref_displayhud_key);
        keyprefRoomServerUrl = getString(R.string.pref_room_server_url_key);
        keyprefRoom = getString(R.string.pref_room_key);
        keyprefRoomList = getString(R.string.pref_room_list_key);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items.
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        String masterId = null;
        switch (id) {
            case R.id.viewer_btn: {
                int selectedId = roomlist.getSelectedItemPosition();
                if (selectedId >= 0) {
                    masterId = ((ArrayAdapter<String>) (roomlist.getAdapter())).getItem(selectedId);
                }

            }
            case R.id.master_btn: {
                UserType userType = UserType.fromCanonicalForm(((Button) v).getText().toString());
                start(userType, masterId);
            }

        }

    }

    private void start(UserType userType, String masterId) {

        if (!WebSocketOk) {
            Toast.makeText(getApplicationContext(), "network isn't avalibe.", Toast.LENGTH_LONG).show();
        }

        SettingsBean settingsBean = getSettingsValues();
        settingsBean.setUserType(userType);
        Intent intent = new Intent(MainActivity.this, VideoActivity.class);
        intent.putExtra(VideoActivity.paramIntentKey, settingsBean);
        if (userType == UserType.VIEWER)
            intent.putExtra(VideoActivity.paramMastIdKey, masterId);
        startActivity(intent);
    }

    private void initialize() {

        masterbtn = (Button) findViewById(R.id.master_btn);
        viewerbtn = (Button) findViewById(R.id.viewer_btn);
        roomlist = (ListView) findViewById(R.id.roomlist);
        socketPgb = (ProgressBar) findViewById(R.id.list_pgb);

        masterbtn.setOnClickListener(this);
        viewerbtn.setOnClickListener(this);
        roomlist.setOnItemClickListener(new ListClickListener());
    }

    private void showMasters(ArrayList<String> masters) {

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, masters);
        roomlist.setAdapter(arrayAdapter);

    }

    private class ListListener implements KWWebSocketClient.OnListListener {
        @Override
        public void onListListener(final ArrayList<String> masters) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    socketPgb.setVisibility(View.GONE);
                    showMasters(masters);
                }
            });

        }

        @Override
        public void onConnectionOpened() {
            WebSocketOk = true;
            socketClient.sendListerRequest();
        }

        @Override
        public void onError(final String msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    socketPgb.setVisibility(View.GONE);
                }
            });
        }
    }

    private class ListClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            String masterId = ((ArrayAdapter<String>) (parent.getAdapter())).getItem(position);
            start(UserType.VIEWER, masterId);
        }
    }
}
