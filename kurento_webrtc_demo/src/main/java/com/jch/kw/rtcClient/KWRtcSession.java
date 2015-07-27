package com.jch.kw.rtcClient;

import android.content.Context;
import android.opengl.EGLContext;
import android.util.Log;

import com.jch.kw.View.KWEvnent;
import com.jch.kw.bean.SettingsBean;
import com.jch.kw.bean.UserType;
import com.jch.kw.util.LogCat;
import com.jch.kw.util.LooperExecutor;

import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaCodecVideoEncoder;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jingbiaowang on 2015/7/24.
 */
public class KWRtcSession implements KWSessionEvent {

    private LooperExecutor executor;
    private static KWRtcSession instance;
    private PeerConnectionFactory.Options options;
    private SettingsBean sessionParams;
    private KWEvnent evnent;
    private PeerConnectionFactory factory;

    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public static final String AUDIO_TRACK_ID = "ARDAMSa0";
    private static final String FIELD_TRIAL_VP9 = "WebRTC-SupportVP9/Enabled/";
    private static final String VIDEO_CODEC_VP8 = "VP8";
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String VIDEO_CODEC_H264 = "H264";
    private static final String AUDIO_CODEC_OPUS = "opus";
    private static final String AUDIO_CODEC_ISAC = "ISAC";
    private static final String VIDEO_CODEC_PARAM_START_BITRATE =
            "x-google-start-bitrate";
    private static final String AUDIO_CODEC_PARAM_BITRATE = "maxaveragebitrate";
    private static final String MAX_VIDEO_WIDTH_CONSTRAINT = "maxWidth";
    private static final String MIN_VIDEO_WIDTH_CONSTRAINT = "minWidth";
    private static final String MAX_VIDEO_HEIGHT_CONSTRAINT = "maxHeight";
    private static final String MIN_VIDEO_HEIGHT_CONSTRAINT = "minHeight";
    private static final String MAX_VIDEO_FPS_CONSTRAINT = "maxFrameRate";
    private static final String MIN_VIDEO_FPS_CONSTRAINT = "minFrameRate";
    private static final String DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT = "DtlsSrtpKeyAgreement";
    private static final int HD_VIDEO_WIDTH = 1280;
    private static final int HD_VIDEO_HEIGHT = 720;
    private static final int MAX_VIDEO_WIDTH = 1280;
    private static final int MAX_VIDEO_HEIGHT = 1280;
    private static final int MAX_VIDEO_FPS = 30;
    //保持現成同步。
    private boolean isError;
    private boolean preferH264;
    private boolean preferIsac;
    private MediaConstraints pcConstraints;
    private int numberOfCameras;
    private MediaConstraints localMediaConstraints;
    private MediaConstraints localAudioConstraints;
    private MediaConstraints sdpMediaConstraints;
    private PeerConnection peerConnection;
    private KWPeerConnectionObserver pcObserver;
    private MediaStream mediaStream;
    private VideoCapturerAndroid videoCapturer;
    private VideoSource videoSource;
    private VideoTrack localVideoTrack;
    private VideoTrack remoteVideoTrack;
    //控制是否显示localVideo, remoteVideo?
    private boolean localRenderVideo;
    private boolean remoteRenderVideo;
    private VideoRenderer.Callbacks localRender;
    private VideoRenderer.Callbacks remoteRender;
    private KWSdpObserver sdpObserver;

    private KWRtcSession() {
        executor = new LooperExecutor();
        executor.requestStart();
        sdpObserver = new KWSdpObserver();
    }

    public static KWRtcSession getInstance() {
        if (instance == null) {
            instance = new KWRtcSession();
        }

        return instance;
    }

    public void setPeerConnectionFactoryOptions(PeerConnectionFactory.Options options) {
        this.options = options;
        this.evnent = evnent;
    }

    public void createPeerConnectionFactory(final VideoRenderer.Callbacks localRender,
                                            final VideoRenderer.Callbacks remoteRender, SettingsBean sessionParams, final Context context, final EGLContext reanderEGLContext, KWEvnent evnent) {
        this.sessionParams = sessionParams;
        numberOfCameras = 0;
        preferH264 = false;
        preferIsac = false;
        pcConstraints = null;
        localMediaConstraints = null;
        localAudioConstraints = null;
        sdpMediaConstraints = null;
        isError = false;
        peerConnection = null;
        mediaStream = null;
        videoCapturer = null;
        localVideoTrack = null;
        remoteVideoTrack = null;
        localRenderVideo = sessionParams.getUserType() == UserType.MASTER ? true : false;
        remoteRenderVideo = !localRenderVideo;
        this.remoteRender = remoteRender;
        this.localRender = localRender;
        this.evnent = evnent;

        executor.execute(new Runnable() {
            @Override
            public void run() {
                createPeerConnectionFactoryInternal(context, reanderEGLContext);
            }
        });
    }

    /**
     * Init peerConnection global, create peerconnection factory.
     *
     * @param context
     * @param renderEGLContext
     */
    private void createPeerConnectionFactoryInternal(Context context, EGLContext renderEGLContext) {

        LogCat.debug("Create peer connection factory with EGLContext "
                + renderEGLContext + ". Use video: "
                + sessionParams.isVideoCallEnable());
//        isError = false;
        // Check if VP9 is used by default.
        if (sessionParams.isVideoCallEnable() && sessionParams.getVideoCode() != null
                && sessionParams.getVideoCode().equals(VIDEO_CODEC_VP9)) {
            PeerConnectionFactory.initializeFieldTrials(FIELD_TRIAL_VP9);
        } else {
            PeerConnectionFactory.initializeFieldTrials(null);
        }
        // Check if H.264 is used by default.
        preferH264 = false;
        if (sessionParams.isVideoCallEnable() && sessionParams.getVideoCode() != null
                && sessionParams.getVideoCode().equals(VIDEO_CODEC_H264)) {
            preferH264 = true;
        }
        // Check if ISAC is used by default.
        preferIsac = false;
        if (sessionParams.getAudioCode() != null
                && sessionParams.getAudioCode().equals(AUDIO_CODEC_ISAC)) {
            preferIsac = true;
        }

        //init PeerConnection global.
        if (!PeerConnectionFactory.initializeAndroidGlobals(
                context, true, true,
                sessionParams.isHwCodeEnable(), renderEGLContext)) {
            evnent.portError("Failed to initializeAndroidGlobals");
        }
        factory = new PeerConnectionFactory();
        if (options != null) {
            LogCat.debug("Factory networkIgnoreMask option: " + options.networkIgnoreMask);
            factory.setOptions(options);
        }
        LogCat.debug("Peer connection factory created.");

    }

    /**
     * create mediaConstraints , 创建peerConnection.
     */
    public void createPeerConnection() {

        executor.execute(new Runnable() {
            @Override
            public void run() {

                createMediaConstraintsInternal();
                createPeerConnectionInternal();
            }
        });
    }


    private void createMediaConstraintsInternal() {
        // Create peer connection constraints.
        pcConstraints = new MediaConstraints();
        // Enable DTLS for normal calls and disable for loopback calls.
        pcConstraints.optional.add(
                new MediaConstraints.KeyValuePair(DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT, "true"));
        //create local Constraints
        if (sessionParams.getUserType() == UserType.MASTER)
            createLocalMediaConstraintsInernal();

        // Create SDP constraints.
        sdpMediaConstraints = new MediaConstraints();

        if (sessionParams.getUserType() == UserType.MASTER) {       //master.
            sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    "OfferToReceiveVideo", "false"));
            sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    "OfferToReceiveAudio", "false"));
        } else {            //viewer.
            sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    "OfferToReceiveVideo", "true"));
            sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    "OfferToReceiveAudio", "true"));
        }
    }

    private void createLocalMediaConstraintsInernal() {
        // Check if there is a camera on device and disable video call if not.
        numberOfCameras = VideoCapturerAndroid.getDeviceCount();
        if (numberOfCameras == 0) {
            LogCat.debug("No camera on device. Switch to audio only call.");
            sessionParams.setVideoCallEnable(false);
        }
        // Create video constraints if video call is enabled.
        if (sessionParams.isVideoCallEnable()) {
            localMediaConstraints = new MediaConstraints();
            int videoWidth = sessionParams.getVideoWidth();
            int videoHeight = sessionParams.getVideoHeight();

            // If VP8 HW video encoder is supported and video resolution is not
            // specified force it to HD.
            if ((videoWidth == 0 || videoHeight == 0)
                    && sessionParams.isHwCodeEnable()
                    && MediaCodecVideoEncoder.isVp8HwSupported()) {
                videoWidth = HD_VIDEO_WIDTH;
                videoHeight = HD_VIDEO_HEIGHT;
            }

            // Add video resolution constraints.
            if (videoWidth > 0 && videoHeight > 0) {
                videoWidth = Math.min(videoWidth, MAX_VIDEO_WIDTH);
                videoHeight = Math.min(videoHeight, MAX_VIDEO_HEIGHT);
                localMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                        MIN_VIDEO_WIDTH_CONSTRAINT, Integer.toString(videoWidth)));
                localMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                        MAX_VIDEO_WIDTH_CONSTRAINT, Integer.toString(videoWidth)));
                localMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                        MIN_VIDEO_HEIGHT_CONSTRAINT, Integer.toString(videoHeight)));
                localMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                        MAX_VIDEO_HEIGHT_CONSTRAINT, Integer.toString(videoHeight)));
            }

            // Add fps constraints.
            int videoFps = Integer.getInteger(sessionParams.getFps());
            if (videoFps > 0) {
                videoFps = Math.min(videoFps, MAX_VIDEO_FPS);
                localMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                        MIN_VIDEO_FPS_CONSTRAINT, Integer.toString(videoFps)));
                localMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                        MAX_VIDEO_FPS_CONSTRAINT, Integer.toString(videoFps)));
            }
        }

        // Create audio constraints.
        localAudioConstraints = new MediaConstraints();
    }

    private void createPeerConnectionInternal() {

        if (factory == null || isError) {
            LogCat.e("Peerconnection factory is not created");
            return;
        }
        LogCat.debug("Create peer connection");
        LogCat.debug("PCConstraints: " + pcConstraints.toString());
        if (localMediaConstraints != null) {
            LogCat.debug("VideoConstraints: " + localMediaConstraints.toString());
        }

        PeerConnection.RTCConfiguration rtcConfig =
                new PeerConnection.RTCConfiguration(getIces());
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;

        peerConnection = factory.createPeerConnection(
                rtcConfig, pcConstraints, pcObserver);

        // Set default WebRTC tracing and INFO libjingle logging.
        // NOTE: this _must_ happen while |factory| is alive!
        Logging.enableTracing(
                "logcat:",
                EnumSet.of(Logging.TraceLevel.TRACE_DEFAULT),
                Logging.Severity.LS_INFO);

        createLocalMediaStream();

    }

    /**
     * 创建 local mediaStream.
     */
    private void createLocalMediaStream() {
        mediaStream = factory.createLocalMediaStream("ARDAMS");
        if (sessionParams.getUserType() == UserType.MASTER) {
            if (sessionParams.isVideoCallEnable()) {
                String cameraDeviceName = VideoCapturerAndroid.getDeviceName(0);
                String frontCameraDeviceName =
                        VideoCapturerAndroid.getNameOfFrontFacingDevice();
                if (numberOfCameras > 1 && frontCameraDeviceName != null) {
                    cameraDeviceName = frontCameraDeviceName;
                }
                LogCat.debug("Opening camera: " + cameraDeviceName);
                videoCapturer = VideoCapturerAndroid.create(cameraDeviceName, null);
                if (videoCapturer == null) {
                    reportError("Failed to open camera");
                    return;
                }

                videoSource = factory.createVideoSource(videoCapturer, localMediaConstraints);

                localVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);

                localVideoTrack.setEnabled(localRenderVideo);

                localVideoTrack.addRenderer(new VideoRenderer(localRender));

                mediaStream.addTrack(localVideoTrack);
            }
            AudioTrack audioTrack = factory.createAudioTrack(
                    AUDIO_TRACK_ID,
                    factory.createAudioSource(localAudioConstraints));
            audioTrack.setEnabled(localRenderVideo);

            mediaStream.addTrack(audioTrack);
        }

        peerConnection.addStream(mediaStream);

    }

    /**
     * @param enable
     */
    public void setLocalVideoEnabled(final boolean enable) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                localRenderVideo = enable;
                if (localVideoTrack != null) {
                    localVideoTrack.setEnabled(localRenderVideo);
                }

            }
        });
    }

    public void setRomoteVideoEnabled(final boolean enable) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                remoteRenderVideo = enable;
                if (remoteVideoTrack != null) {
                    remoteVideoTrack.setEnabled(remoteRenderVideo);
                }
            }
        });
    }

    @Override
    public void createOffer() {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (peerConnection != null)
                    peerConnection.createOffer(sdpObserver, sdpMediaConstraints);
                else reportError("peerConnection is null oncreateOffer.");
            }
        });
    }

    private void reportError(final String msg) {
        if (!isError)
            isError = true;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                evnent.portError(msg);

            }
        });
    }


    @Override
    public void processAnwser(SessionDescription anwser) {


    }

    @Override
    public void dispose() {

    }

    private class KWPeerConnectionObserver implements PeerConnection.Observer {

        @Override
        public void onSignalingChange(PeerConnection.SignalingState newState) {

        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState newState) {

        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState newState) {

        }

        @Override
        public void onIceCandidate(IceCandidate candidate) {

        }

        @Override
        public void onAddStream(MediaStream stream) {

        }

        @Override
        public void onRemoveStream(MediaStream stream) {

        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {

        }

        @Override
        public void onRenegotiationNeeded() {

        }
    }

//    private class KWSdpObserver implements

    private List<PeerConnection.IceServer> getIces() {
        String[] stunAddresses = new String[]{
                "stun.l.google.com:19302",
                "stun1.l.google.com:19302",
                "stun2.l.google.com:19302",
                "stun3.l.google.com:19302",
                "stun4.l.google.com:19302",
                "stun.ekiga.net",
                "stun.ideasip.com",
                "stun.rixtelecom.se",
                "stun.schlund.de",
                "stun.stunprotocol.org:3478",
                "stun.voiparound.com",
                "stun.voipbuster.com",
                "stun.voipstunt.com",
                "stun.voxgratia.org",
                "stun.services.mozilla.com"
        };

        List<PeerConnection.IceServer> iceServers = new ArrayList<PeerConnection.IceServer>();
        StringBuilder stunAddress = new StringBuilder();
        for (String address : stunAddresses) {
            stunAddress.append("stun:").append(address);
            PeerConnection.IceServer iceServer = new PeerConnection.IceServer(stunAddress.toString(), "", "");
            iceServers.add(iceServer);
            stunAddress.delete(0, stunAddress.length());
        }

        return iceServers;
    }

    private class KWSdpObserver implements SdpObserver {
        @Override
        public void onCreateSuccess(SessionDescription origSdp) {

            if (peerConnection.getLocalDescription() != null) {
                reportError("Multiple SDP create.");
                return;
            }
            String sdpDescription = origSdp.description;
            if (preferIsac) {
                sdpDescription = preferCodec(sdpDescription, AUDIO_CODEC_ISAC, true);
            }
            if (preferH264) {
                sdpDescription = preferCodec(sdpDescription, VIDEO_CODEC_H264, false);
            }
            final SessionDescription sdp = new SessionDescription(
                    origSdp.type, sdpDescription);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (peerConnection != null && !isError) {
                        LogCat.debug("Set local SDP from " + sdp.type);
                        peerConnection.setLocalDescription(sdpObserver, sdp);
                    }
                }
            });
        }

        @Override
        public void onSetSuccess() {



        }

        @Override
        public void onCreateFailure(String error) {

        }

        @Override
        public void onSetFailure(String error) {

        }
    }

    private static String preferCodec(
            String sdpDescription, String codec, boolean isAudio) {
        String[] lines = sdpDescription.split("\r\n");
        int mLineIndex = -1;
        String codecRtpMap = null;
        // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
        String regex = "^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$";
        Pattern codecPattern = Pattern.compile(regex);
        String mediaDescription = "m=video ";
        if (isAudio) {
            mediaDescription = "m=audio ";
        }
        for (int i = 0; (i < lines.length)
                && (mLineIndex == -1 || codecRtpMap == null); i++) {
            if (lines[i].startsWith(mediaDescription)) {
                mLineIndex = i;
                continue;
            }
            Matcher codecMatcher = codecPattern.matcher(lines[i]);
            if (codecMatcher.matches()) {
                codecRtpMap = codecMatcher.group(1);
                continue;
            }
        }
        if (mLineIndex == -1) {
            LogCat.debug("No " + mediaDescription + " line, so can't prefer " + codec);
            return sdpDescription;
        }
        if (codecRtpMap == null) {
            LogCat.debug("No rtpmap for " + codec);
            return sdpDescription;
        }
        LogCat.debug("Found " + codec + " rtpmap " + codecRtpMap + ", prefer at "
                + lines[mLineIndex]);
        String[] origMLineParts = lines[mLineIndex].split(" ");
        if (origMLineParts.length > 3) {
            StringBuilder newMLine = new StringBuilder();
            int origPartIndex = 0;
            // Format is: m=<media> <port> <proto> <fmt> ...
            newMLine.append(origMLineParts[origPartIndex++]).append(" ");
            newMLine.append(origMLineParts[origPartIndex++]).append(" ");
            newMLine.append(origMLineParts[origPartIndex++]).append(" ");
            newMLine.append(codecRtpMap);
            for (; origPartIndex < origMLineParts.length; origPartIndex++) {
                if (!origMLineParts[origPartIndex].equals(codecRtpMap)) {
                    newMLine.append(" ").append(origMLineParts[origPartIndex]);
                }
            }
            lines[mLineIndex] = newMLine.toString();
            LogCat.debug("Change media description: " + lines[mLineIndex]);
        } else {
            LogCat.debug("Wrong SDP media description format: " + lines[mLineIndex]);
        }
        StringBuilder newSdpDescription = new StringBuilder();
        for (String line : lines) {
            newSdpDescription.append(line).append("\r\n");
        }
        return newSdpDescription.toString();
    }


}
