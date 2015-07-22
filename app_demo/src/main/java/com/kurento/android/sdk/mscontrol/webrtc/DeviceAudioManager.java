package com.kurento.android.sdk.mscontrol.webrtc;


import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;

public class DeviceAudioManager {

    private static AudioManager audioManager;
    private static int savedAudioMode = AudioManager.MODE_INVALID;
    private static boolean savedIsSpeakerPhoneOn = false;
    //
//    private static final Logger log = LoggerFactory
//            .getLogger(DeviceAudioManager.class.getSimpleName());
    final static Handler mHandler = new Handler();

    public static void initAudioManager(Context ctx) {
        audioManager = ((AudioManager) ctx
                .getSystemService(Context.AUDIO_SERVICE));
    }

    public static void enableLoudSpeaker(final boolean enable) {
        audioManager.setSpeakerphoneOn(enable);
    }

    public static void saveAudioState() {
        savedAudioMode = audioManager.getMode();
        savedIsSpeakerPhoneOn = audioManager.isSpeakerphoneOn();

        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    public static void restoreAudioState() {
        audioManager.setSpeakerphoneOn(savedIsSpeakerPhoneOn);
        audioManager.setMode(savedAudioMode);
    }

    public static void riseVolume() {
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_SHOW_UI);
    }

    public static void lowerVolume() {
        audioManager.adjustVolume(AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_SHOW_UI);
    }
}
