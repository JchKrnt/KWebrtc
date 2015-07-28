package com.example.jchsohu.app_demo;

import android.content.Context;
import android.opengl.EGLContext;
import android.util.Log;

import com.example.jchsohu.app_demo.bean.UserType;
import com.example.jchsohu.app_demo.util.LogCat;
import com.example.jchsohu.app_demo.util.LooperExecutor;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaCodecVideoEncoder;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.PeerConnectionFactory.Options;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by jchsohu on 15-7-8.
 */
public class AppRtcSession {

    /**
     * Peer connection parameters.
     */
    public static class PeerConnectionParameters {
        public final boolean videoCallEnabled;
        public final int videoWidth;
        public final int videoHeight;
        public final int videoFps;
        public final int videoStartBitrate;
        public final String videoCodec;
        public final boolean videoCodecHwAcceleration;
        public final int audioStartBitrate;
        public final String audioCodec;
        public final boolean cpuOveruseDetection;
        public final UserType userType;

        public PeerConnectionParameters(
                boolean videoCallEnabled,
                int videoWidth, int videoHeight, int videoFps, int videoStartBitrate,
                String videoCodec, boolean videoCodecHwAcceleration,
                int audioStartBitrate, String audioCodec,
                boolean cpuOveruseDetection, UserType userType) {
            this.videoCallEnabled = videoCallEnabled;
            this.videoWidth = videoWidth;
            this.videoHeight = videoHeight;
            this.videoFps = videoFps;
            this.videoStartBitrate = videoStartBitrate;
            this.videoCodec = videoCodec;
            this.videoCodecHwAcceleration = videoCodecHwAcceleration;
            this.audioStartBitrate = audioStartBitrate;
            this.audioCodec = audioCodec;
            this.cpuOveruseDetection = cpuOveruseDetection;
            this.userType = userType;
        }
    }

    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public static final String AUDIO_TRACK_ID = "ARDAMSa0";
    private static final String TAG = "PCRTCClient";
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

    private PeerConnection peerConnection = null;
    private AppRtcPeerConnectionEvents rtcPeerEvent;
    private PeerConnectionParameters peerConnectionParameters;
    private final PCObserver pcObserver = new PCObserver();
    private final SDPObserver sdpObserver = new SDPObserver();
    private final LooperExecutor executor;
    private boolean videoCallEnabled;
    private boolean preferIsac;
    private boolean preferH264;
    private boolean videoSourceStopped;
    private boolean isError;
    private boolean sdpInital;

    private MediaStream mediaStream;
    private int numberOfCameras;
    private VideoCapturerAndroid videoCapturer;
    private LinkedList<IceCandidate> queuedRemoteCandidates;
    private VideoRenderer.Callbacks render;
    private PeerConnectionFactory factory;
    public PeerState state = PeerState.NEN;

    enum PeerState {
        /**
         * offer state  of  peerceonnection.
         */
        NEN, LOCALOFFER_STATE, REMOTEOFFER_STATE
    }

    // enableVideo is set to true if video should be rendered and sent.
    private boolean renderVideo;
    private VideoTrack localVideoTrack;
    private VideoTrack remoteVideoTrack;

    VideoRenderer.Callbacks remoteRender;

    private MediaConstraints pcConstraints;

    public Options options;

    private static AppRtcSession instance;

    public static AppRtcSession getInstance() {

        if (instance == null)
            instance = new AppRtcSession();

        return instance;
    }


    private AppRtcSession() {
        this.executor = new LooperExecutor();
        // Looper thread is started once in private ctor and is used for all
        // peer connection API calls to ensure new peer connection factory is
        // created on the same thread as previously destroyed factory.
        this.executor.requestStart();
    }

    public void setPeerConnectionFactoryOptions(Options options) {
        this.options = options;
    }

    public void createPeerConnectionFactory(
            final Context context,
            final EGLContext renderEGLContext,
            final PeerConnectionParameters connectionParameters,
            final AppRtcPeerConnectionEvents events) {

        this.peerConnectionParameters = connectionParameters;
        this.rtcPeerEvent = events;

        videoCallEnabled = peerConnectionParameters.videoCallEnabled;
        // Reset variables to initial states.
        factory = null;
        peerConnection = null;
        preferIsac = false;
        preferH264 = false;
        videoSourceStopped = false;
        isError = false;
        queuedRemoteCandidates = null;
        mediaStream = null;
        videoCapturer = null;
        renderVideo = true;
        localVideoTrack = null;
        remoteVideoTrack = null;

        executor.execute(new Runnable() {
            @Override
            public void run() {
                createPeerConnectionFactoryInternal(context, renderEGLContext);
            }
        });
    }


    private void createPeerConnectionFactoryInternal(
            Context context, EGLContext renderEGLContext) {
        Log.d(TAG, "Create peer connection factory with EGLContext "
                + renderEGLContext + ". Use video: "
                + peerConnectionParameters.videoCallEnabled);
        isError = false;
        // Check if VP9 is used by default.
        if (videoCallEnabled && peerConnectionParameters.videoCodec != null
                && peerConnectionParameters.videoCodec.equals(VIDEO_CODEC_VP9)) {
            PeerConnectionFactory.initializeFieldTrials(FIELD_TRIAL_VP9);
        } else {
            PeerConnectionFactory.initializeFieldTrials(null);
        }
        // Check if H.264 is used by default.
        preferH264 = false;
        if (videoCallEnabled && peerConnectionParameters.videoCodec != null
                && peerConnectionParameters.videoCodec.equals(VIDEO_CODEC_H264)) {
            preferH264 = true;
        }
        // Check if ISAC is used by default.
        preferIsac = false;
        if (peerConnectionParameters.audioCodec != null
                && peerConnectionParameters.audioCodec.equals(AUDIO_CODEC_ISAC)) {
            preferIsac = true;
        }
        if (!PeerConnectionFactory.initializeAndroidGlobals(
                context, true, true,
                peerConnectionParameters.videoCodecHwAcceleration, renderEGLContext)) {
            rtcPeerEvent.onPeerConnectionError("Failed to initializeAndroidGlobals");
        }
        factory = new PeerConnectionFactory();
        if (options != null) {
            Log.d(TAG, "Factory networkIgnoreMask option: " + options.networkIgnoreMask);
            factory.setOptions(options);
        }
        Log.d(TAG, "Peer connection factory created.");
    }

    private void createPeerConnection() {
        // Create peer connection constraints.
        MediaConstraints pcConstraints = new MediaConstraints();
        // Enable DTLS for normal calls and disable for loopback calls.
        pcConstraints.optional.add(
                new MediaConstraints.KeyValuePair(DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT, "true"));

        PeerConnection.RTCConfiguration rtcConfig =
                new PeerConnection.RTCConfiguration(getIces());
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        peerConnection = factory.createPeerConnection(
                rtcConfig, pcConstraints, pcObserver);

        queuedRemoteCandidates = new LinkedList<>();
    }

    private void prepareLocalStream(VideoRenderer.Callbacks localRenderS) {

        // Check if there is a camera on device and disable video call if not.
        numberOfCameras = VideoCapturerAndroid.getDeviceCount();
        if (numberOfCameras == 0) {
            Log.w(TAG, "No camera on device. Switch to audio only call.");
            videoCallEnabled = false;
        }

        MediaConstraints localVideoConstraints = createLocalVideoMediaConstraintsInternal();

        // Create audio constraints.
        MediaConstraints audioConstraints = new MediaConstraints();

        /**
         *  local stream.
         */
        mediaStream = factory.createLocalMediaStream("ARDAMS");
        if (videoCallEnabled) {
            String cameraDeviceName = VideoCapturerAndroid.getDeviceName(0);
            String frontCameraDeviceName =
                    VideoCapturerAndroid.getNameOfFrontFacingDevice();
            if (numberOfCameras > 1 && frontCameraDeviceName != null) {
                cameraDeviceName = frontCameraDeviceName;
            }
            Log.d(TAG, "Opening camera: " + cameraDeviceName);
            videoCapturer = VideoCapturerAndroid.create(cameraDeviceName, null);
            if (videoCapturer == null) {
//                reportError("Failed to open camera");
                return;
            }

            VideoSource videoSource = factory.createVideoSource(videoCapturer, localVideoConstraints);

            localVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
            localVideoTrack.setEnabled(renderVideo);
            localVideoTrack.addRenderer(new VideoRenderer(localRenderS));

            mediaStream.addTrack(localVideoTrack);
        }

        mediaStream.addTrack(factory.createAudioTrack(
                AUDIO_TRACK_ID,
                factory.createAudioSource(audioConstraints)));
        peerConnection.addStream(mediaStream);

    }


    private MediaConstraints createLocalVideoMediaConstraintsInternal() {
        // Create video constraints if video call is enabled.
        MediaConstraints videoConstraints = null;
        if (videoCallEnabled) {
            videoConstraints = new MediaConstraints();
            int videoWidth = peerConnectionParameters.videoWidth;
            int videoHeight = peerConnectionParameters.videoHeight;

            // If VP8 HW video encoder is supported and video resolution is not
            // specified force it to HD.
            if ((videoWidth == 0 || videoHeight == 0)
                    && peerConnectionParameters.videoCodecHwAcceleration
                    && MediaCodecVideoEncoder.isVp8HwSupported()) {
                videoWidth = HD_VIDEO_WIDTH;
                videoHeight = HD_VIDEO_HEIGHT;
            }

            // Add video resolution constraints.
            if (videoWidth > 0 && videoHeight > 0) {
                videoWidth = Math.min(videoWidth, MAX_VIDEO_WIDTH);
                videoHeight = Math.min(videoHeight, MAX_VIDEO_HEIGHT);
                videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                        MIN_VIDEO_WIDTH_CONSTRAINT, Integer.toString(videoWidth)));
                videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                        MAX_VIDEO_WIDTH_CONSTRAINT, Integer.toString(videoWidth)));
                videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                        MIN_VIDEO_HEIGHT_CONSTRAINT, Integer.toString(videoHeight)));
                videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                        MAX_VIDEO_HEIGHT_CONSTRAINT, Integer.toString(videoHeight)));
            }

            // Add fps constraints.
            int videoFps = peerConnectionParameters.videoFps;
            if (videoFps > 0) {
                videoFps = Math.min(videoFps, MAX_VIDEO_FPS);
                videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                        MIN_VIDEO_FPS_CONSTRAINT, Integer.toString(videoFps)));
                videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                        MAX_VIDEO_FPS_CONSTRAINT, Integer.toString(videoFps)));
            }
        }

        return videoConstraints;
    }

    private MediaConstraints createSdpConstraint(UserType type) {
        // Create SDP constraints.
        MediaConstraints sdpMediaConstraints = new MediaConstraints();

        if (type != UserType.BROADCAST) {
            sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    "OfferToReceiveAudio", "false"));
            sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    "OfferToReceiveVideo", "false"));
//        if (videoCallEnabled || peerConnectionParameters.loopback) {
//            sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
//                    "OfferToReceiveVideo", "true"));
//        } else {
        } else {
            sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    "OfferToReceiveAudio", "true"));
            sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    "OfferToReceiveVideo", "true"));
//        }
        }

        return sdpMediaConstraints;
    }


    public void masterPeerConnection(final VideoRenderer.Callbacks localRender) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                createPeerConnection();
                prepareLocalStream(localRender);
            }
        });
    }

    public void viwerPeerConnnection(final VideoRenderer.Callbacks remoteRender) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                createPeerConnection();
                AppRtcSession.this.remoteRender = remoteRender;
            }
        });
    }


    public void communicatePeerConnection(final VideoRenderer.Callbacks localRender, final VideoRenderer.Callbacks remoteRender) {
        executor.execute(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    public void createOffer() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                LogCat.debug("create offer----");
                state = PeerState.LOCALOFFER_STATE;
                if (peerConnection != null) {
                    peerConnection.createOffer(sdpObserver, createSdpConstraint(peerConnectionParameters.userType));
                }
            }
        });
    }

    public void processOffer(final String sdp) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                state = PeerState.REMOTEOFFER_STATE;
                SessionDescription anwDSdp = new SessionDescription(SessionDescription.Type.ANSWER, sdp);
                peerConnection.setRemoteDescription(sdpObserver, anwDSdp);
            }
        });
    }

    // Implementation detail: observe ICE & stream changes and react accordingly.
    private class PCObserver implements PeerConnection.Observer {
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
            executor.execute(new Runnable() {
                @Override
                public void run() {

                }
            });
        }

        @Override
        public void onAddStream(final MediaStream stream) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (peerConnection == null)
                        return;
                    if (stream.audioTracks.size() > 1 || stream.videoTracks.size() > 1) {
                        reportError("Weird-looking stream: " + stream);
                        return;
                    }

                    if (stream.videoTracks.size() == 1 && peerConnectionParameters.userType != UserType.BROADCAST) {
                        remoteVideoTrack = stream.videoTracks.get(0);
                        remoteVideoTrack.setEnabled(renderVideo);
                        remoteVideoTrack.addRenderer(new VideoRenderer(remoteRender));

                    }

                }
            });
        }

        @Override
        public void onRemoveStream(final MediaStream stream) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (peerConnection == null || isError) {
                        return;
                    }
                    remoteVideoTrack = null;
                    if (stream.videoTracks.get(0) != null)
                        stream.videoTracks.get(0).dispose();
                }
            });


        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {

        }

        @Override
        public void onRenegotiationNeeded() {

        }
    }

       private static String setStartBitrate(String codec, boolean isVideoCodec,
                                          String sdpDescription, int bitrateKbps) {
        String[] lines = sdpDescription.split("\r\n");
        int rtpmapLineIndex = -1;
        boolean sdpFormatUpdated = false;
        String codecRtpMap = null;
        // Search for codec rtpmap in format
        // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
        String regex = "^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$";
        Pattern codecPattern = Pattern.compile(regex);
        for (int i = 0; i < lines.length; i++) {
            Matcher codecMatcher = codecPattern.matcher(lines[i]);
            if (codecMatcher.matches()) {
                codecRtpMap = codecMatcher.group(1);
                rtpmapLineIndex = i;
                break;
            }
        }
        if (codecRtpMap == null) {
            Log.w(TAG, "No rtpmap for " + codec + " codec");
            return sdpDescription;
        }
        Log.d(TAG, "Found " + codec + " rtpmap " + codecRtpMap
                + " at " + lines[rtpmapLineIndex]);

        // Check if a=fmtp string already exist in remote SDP for this codec and
        // update it with new bitrate parameter.
        regex = "^a=fmtp:" + codecRtpMap + " \\w+=\\d+.*[\r]?$";
        codecPattern = Pattern.compile(regex);
        for (int i = 0; i < lines.length; i++) {
            Matcher codecMatcher = codecPattern.matcher(lines[i]);
            if (codecMatcher.matches()) {
                Log.d(TAG, "Found " + codec + " " + lines[i]);
                if (isVideoCodec) {
                    lines[i] += "; " + VIDEO_CODEC_PARAM_START_BITRATE
                            + "=" + bitrateKbps;
                } else {
                    lines[i] += "; " + AUDIO_CODEC_PARAM_BITRATE
                            + "=" + (bitrateKbps * 1000);
                }
                Log.d(TAG, "Update remote SDP line: " + lines[i]);
                sdpFormatUpdated = true;
                break;
            }
        }

        StringBuilder newSdpDescription = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            newSdpDescription.append(lines[i]).append("\r\n");
            // Append new a=fmtp line if no such line exist for a codec.
            if (!sdpFormatUpdated && i == rtpmapLineIndex) {
                String bitrateSet;
                if (isVideoCodec) {
                    bitrateSet = "a=fmtp:" + codecRtpMap + " "
                            + VIDEO_CODEC_PARAM_START_BITRATE + "=" + bitrateKbps;
                } else {
                    bitrateSet = "a=fmtp:" + codecRtpMap + " "
                            + AUDIO_CODEC_PARAM_BITRATE + "=" + (bitrateKbps * 1000);
                }
                Log.d(TAG, "Add remote SDP line: " + bitrateSet);
                newSdpDescription.append(bitrateSet).append("\r\n");
            }

        }
        return newSdpDescription.toString();
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
            Log.w(TAG, "No " + mediaDescription + " line, so can't prefer " + codec);
            return sdpDescription;
        }
        if (codecRtpMap == null) {
            Log.w(TAG, "No rtpmap for " + codec);
            return sdpDescription;
        }
        Log.d(TAG, "Found " + codec + " rtpmap " + codecRtpMap + ", prefer at "
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
            Log.d(TAG, "Change media description: " + lines[mLineIndex]);
        } else {
            Log.e(TAG, "Wrong SDP media description format: " + lines[mLineIndex]);
        }
        StringBuilder newSdpDescription = new StringBuilder();
        for (String line : lines) {
            newSdpDescription.append(line).append("\r\n");
        }
        return newSdpDescription.toString();
    }

    // Implementation detail: handle offer creation/signaling and answer setting,
    // as well as adding remote ICE candidates once the answer SDP is set.
    private class SDPObserver implements SdpObserver {

        private SessionDescription localSdp; // either offer or answer SDP

        @Override
        public void onCreateSuccess(SessionDescription origSdp) {
            if (peerConnection == null)
                return;
            if (localSdp != null) {
                reportError("Multiple SDP create");
                return;
            }
            LogCat.debug("peer connect type: " + origSdp.type + "descrption :" + origSdp.description);
            if (state == PeerState.LOCALOFFER_STATE) {
                String sdpDescription = origSdp.description;
                if (preferIsac)
                    sdpDescription = preferCodec(sdpDescription, AUDIO_CODEC_ISAC, true);
                if (videoCallEnabled && preferH264)
                    sdpDescription = preferCodec(sdpDescription, VIDEO_CODEC_H264, false);
                final SessionDescription sdp = new SessionDescription(origSdp.type, sdpDescription);

                localSdp = sdp;
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (peerConnection != null) {
                            peerConnection.setLocalDescription(sdpObserver, sdp);
                        }
                    }
                });
            }
        }

        @Override
        public void onSetSuccess() {

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (peerConnection == null) {
                        return;
                    }
                    if (state == PeerState.LOCALOFFER_STATE) {
                        state = PeerState.REMOTEOFFER_STATE;
                        rtcPeerEvent.onLocalDescription(localSdp);
                        LogCat.debug("Setting Local sdp success!");
                    } else {
                        LogCat.debug("Setting remote sdp success!");
                    }

                }
            });
        }

        @Override
        public void onCreateFailure(String error) {

        }

        @Override
        public void onSetFailure(String error) {

        }
    }

    private void reportError(final String erroMsg) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                rtcPeerEvent.onPeerConnectionError(erroMsg);
            }
        });
    }

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


}
