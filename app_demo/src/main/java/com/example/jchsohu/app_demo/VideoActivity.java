package com.example.jchsohu.app_demo;

import android.app.Activity;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.example.jchsohu.app_demo.bean.UserType;
import com.example.jchsohu.app_demo.util.LogCat;

import org.webrtc.IceCandidate;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoRendererGui.ScalingType;

public class VideoActivity extends Activity implements PeerConnectionClient.PeerConnectionEvents, AppRtcConnectClient.KurentoConnectdEvents, AppRtcPeerConnectionEvents {

    public static final String USERTYPE_KEY = "user_typ_key";

    // Peer connection statistics callback period in ms.
    private static final int STAT_CALLBACK_PERIOD = 1000;
    // Local preview screen position before call is connected.
    private static final int LOCAL_X_CONNECTING = 0;
    private static final int LOCAL_Y_CONNECTING = 0;
    private static final int LOCAL_WIDTH_CONNECTING = 100;
    private static final int LOCAL_HEIGHT_CONNECTING = 100;
    // Local preview screen position after call is connected.
    private static final int LOCAL_X_CONNECTED = 72;
    private static final int LOCAL_Y_CONNECTED = 72;
    private static final int LOCAL_WIDTH_CONNECTED = 25;
    private static final int LOCAL_HEIGHT_CONNECTED = 25;
    // Remote video screen position
    private static final int REMOTE_X = 0;
    private static final int REMOTE_Y = 0;
    private static final int REMOTE_WIDTH = 100;
    private static final int REMOTE_HEIGHT = 100;
    private GLSurfaceView videosf;
    private ScalingType scalingType;
    private PeerConnectionClient clientSession;
    private AppRTCAudioManager audioManager;
    private UserType userType = null;
    private VideoRenderer.Callbacks remoteRender;
    private VideoRenderer.Callbacks localRender;
    private KurentoConnectClient connectClient = null;

    private PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new UnhandledExceptionHandler(this));

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN
                | LayoutParams.FLAG_KEEP_SCREEN_ON
                | LayoutParams.FLAG_DISMISS_KEYGUARD
                | LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.activity_video);

        Intent intent = getIntent();
        String temp_usrType = intent.getStringExtra(USERTYPE_KEY);
        if (temp_usrType == null || temp_usrType.equals(""))
            throw new IllegalArgumentException("usertype erro");

        userType = UserType.BROADCAST.getTypeValue().endsWith(temp_usrType) ? UserType.BROADCAST : UserType.VIEWER;
        connectClient = new KurentoConnectClient(this);
        initialize();

        scalingType = ScalingType.SCALE_ASPECT_FILL;

        VideoRendererGui.setView(videosf, new Runnable() {
            @Override
            public void run() {
                createPeerConnectionFactory();
            }
        });
        remoteRender = VideoRendererGui.create(
                REMOTE_X, REMOTE_Y,
                REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, false);
        localRender = VideoRendererGui.create(
                REMOTE_X, REMOTE_Y,
                REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, true);
        initRtcParams();
        audioManager = AppRTCAudioManager.create(this, new Runnable() {
                    // This method will be called each time the audio state (number and
                    // type of devices) has been changed.
                    @Override
                    public void run() {
                        onAudioManagerChangedState();
                    }
                }
        );
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        LogCat.debug("Initializing the audio manager...");
        audioManager.init();

    }


    /**
     * 测试数据。
     */
    private void initRtcParams() {
        peerConnectionParameters = new PeerConnectionClient.PeerConnectionParameters(
                true, false, 0, 0, 30, 1000, "VP8", true, 0, "OPUS", true, userType);
    }


    // Create peer connection factory when EGL context is ready.
    private void createPeerConnectionFactory() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (clientSession == null) {
                    clientSession = PeerConnectionClient.getInstance();
                    PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
                    options.networkIgnoreMask = 0;
                    clientSession.setPeerConnectionFactoryOptions(options);
                    clientSession.createPeerConnectionFactory(VideoActivity.this, VideoRendererGui.getEGLContext(), peerConnectionParameters, VideoActivity.this);

                }

                onConnectedToRoomInternal();
            }
        });
    }

    private void onConnectedToRoomInternal() {

//        if (userType == UserType.BROADCAST) {
//            rtcSession.masterPeerConnection(localRender);
//        } else if (userType == UserType.VIEWER) {
//            rtcSession.viwerPeerConnnection(remoteRender);
//        }
        clientSession.createPeerConnection(localRender, remoteRender);

        clientSession.createOffer();

    }


    private void onAudioManagerChangedState() {
        // TODO(henrika): disable video if AppRTCAudioManager.AudioDevice.EARPIECE
        // is active.
    }

    @Override
    public void onRemoteDesciption(SessionDescription remoteAnswer) {
        clientSession.setRemoteDescription(remoteAnswer);
    }

    @Override
    public void onRemoteIceCandidate(IceCandidate candidate) {

    }


    @Override
    public void onChannelClose() {

    }

    @Override
    public void onChannelError(String description) {

    }


    @Override
    public void onLocalDescription(final SessionDescription sdp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectClient.sendOfferSdp(userType.getTypeValue(), sdp);
            }
        });
    }

    @Override
    public void onIceCandidate(IceCandidate candidate) {

    }

    @Override
    public void onIceConnected() {

    }

    @Override
    public void onIceDisconnected() {

    }

    @Override
    public void onPeerConnectionClosed() {

    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] reports) {

    }

    @Override
    public void onPeerConnectionError(String description) {

    }

    private void initialize() {

        videosf = (GLSurfaceView) findViewById(R.id.video_sf);

    }

    @Override
    protected void onStop() {
        connectClient.sendstop();
        super.onStop();
    }
}
