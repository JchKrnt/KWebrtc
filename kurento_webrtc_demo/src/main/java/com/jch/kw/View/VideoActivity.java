package com.jch.kw.View;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.jch.kw.R;
import com.jch.kw.bean.SettingsBean;
import com.jch.kw.execption.UnhandledExceptionHandler;
import com.jch.kw.rtcClient.AppRTCAudioManager;

import org.webrtc.SessionDescription;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoRendererGui.ScalingType;

public class VideoActivity extends Activity implements KWEvnent {

    public static final String paramKey = "IntentKey";
    private GLSurfaceView videosf;
    private SettingsBean settingsBean;

    private AppRTCAudioManager audioManager = null;

    private ScalingType scalingType;
    // Remote video screen position
    private static final int REMOTE_X = 0;
    private static final int REMOTE_Y = 0;
    private static final int REMOTE_WIDTH = 100;
    private static final int REMOTE_HEIGHT = 100;
    private VideoRenderer.Callbacks remoteRender;
    private VideoRenderer.Callbacks localRender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new UnhandledExceptionHandler(this));

        settingsBean = getIntent().getParcelableExtra(paramKey);
        // Set window styles for fullscreen-window size. Needs to be done before
        // adding content.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        setContentView(R.layout.activity_video);
        initialize();

        scalingType = ScalingType.SCALE_ASPECT_FILL;
        VideoRendererGui.setView(videosf, new Runnable() {
            @Override
            public void run() {

                //TODO run peerConnection.

            }
        });

        remoteRender = VideoRendererGui.create(
                REMOTE_X, REMOTE_Y,
                REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, false);
        localRender = VideoRendererGui.create(
                REMOTE_X, REMOTE_Y,
                REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, true);

        //创建Audiomanager
        initAudioManager();
    }

    /**
     * 创建peerConnectionFactory, 初始化peerConnection.
     */
    private void createPeerConnectionFactory() {

    }

    /**
     * Create and audio manager that will take care of audio routing,
     * audio modes, audio device enumeration etc.
     */
    private void initAudioManager() {
        audioManager = AppRTCAudioManager.create(this, new Runnable() {
                    // This method will be called each time the audio state (number and
                    // type of devices) has been changed.
                    @Override
                    public void run() {
                        onAudioManagerChangedState();
                    }
                }
        );
        audioManager.init();
    }

    private void onAudioManagerChangedState() {
        // TODO(henrika): disable video if AppRTCAudioManager.AudioDevice.EARPIECE
        // is active.
        if (audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.EARPIECE) {

        }
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

    @Override
    public void onClientPrepareComplete() {

    }

    private void initialize() {

        videosf = (GLSurfaceView) findViewById(R.id.glsf_view);

    }
}