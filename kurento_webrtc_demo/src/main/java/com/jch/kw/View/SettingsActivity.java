package com.jch.kw.View;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import com.jch.kw.R;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: SettingsBean</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">SettingsBean
 * API Guide</a> for more information on developing a SettingsBean UI.
 */
public class SettingsActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private MyPreferenceFragment pfm;

    private String keyprefVideoCall;
    private String keyprefResolution;
    private String keyprefFps;
    private String keyprefStartVideoBitrateType;
    private String keyprefStartVideoBitrateValue;
    private String keyPrefVideoCodec;
    private String keyprefHwCodec;

    private String keyprefStartAudioBitrateType;
    private String keyprefStartAudioBitrateValue;
    private String keyPrefAudioCodec;

    private String keyprefCpuUsageDetection;
    private String keyPrefRoomServerUrl;
    private String keyPrefDisplayHud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        keyprefVideoCall = getString(R.string.pref_videocall_key);
        keyprefResolution = getString(R.string.pref_resolution_key);
        keyprefFps = getString(R.string.pref_fps_key);
        keyprefStartVideoBitrateType = getString(R.string.pref_startvideobitrate_key);
        keyprefStartVideoBitrateValue = getString(R.string.pref_startvideobitratevalue_key);
        keyPrefVideoCodec = getString(R.string.pref_videocodec_key);
        keyprefHwCodec = getString(R.string.pref_hwcodec_key);

        keyprefStartAudioBitrateType = getString(R.string.pref_startaudiobitrate_key);
        keyprefStartAudioBitrateValue = getString(R.string.pref_startaudiobitratevalue_key);
        keyPrefAudioCodec = getString(R.string.pref_audiocodec_key);

        keyprefCpuUsageDetection = getString(R.string.pref_cpu_usage_detection_key);
        keyPrefRoomServerUrl = getString(R.string.pref_room_server_url_key);
        keyPrefDisplayHud = getString(R.string.pref_displayhud_key);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        pfm = new MyPreferenceFragment();
        ft.replace(android.R.id.content, pfm).commit();


    }


    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences spf = pfm.getPreferenceScreen().getSharedPreferences();
        spf.registerOnSharedPreferenceChangeListener(this);

        updateSummaryB(spf, keyprefVideoCall);
        updateSummary(spf, keyprefResolution);
        updateSummary(spf, keyprefFps);
        updateSummary(spf, keyprefStartVideoBitrateType);
        updateSummaryBitrate(spf, keyprefStartVideoBitrateValue);
        setVideoBitrateEnable(spf);
        updateSummary(spf, keyPrefVideoCodec);
        updateSummaryB(spf, keyprefHwCodec);

        updateSummary(spf, keyprefStartAudioBitrateType);
        updateSummaryBitrate(spf, keyprefStartAudioBitrateValue);
        setAudioBitrateEnable(spf);
        updateSummary(spf, keyPrefAudioCodec);

        updateSummaryB(spf, keyprefCpuUsageDetection);
        updateSummary(spf, keyPrefRoomServerUrl);
        updateSummaryB(spf, keyPrefDisplayHud);

    }

    @Override
    protected void onPause() {
        SharedPreferences spf = pfm.getPreferenceScreen().getSharedPreferences();
        spf.unregisterOnSharedPreferenceChangeListener(this);

        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences spf, String key) {

        if (key.equals(keyprefResolution)
                || key.equals(keyprefFps)
                || key.equals(keyprefStartVideoBitrateType)
                || key.equals(keyPrefVideoCodec)
                || key.equals(keyprefStartAudioBitrateType)
                || key.equals(keyPrefAudioCodec)
                || key.equals(keyPrefRoomServerUrl)) {
            updateSummary(spf, key);
        } else if (key.equals(keyprefStartVideoBitrateValue)
                || key.equals(keyprefStartAudioBitrateValue)) {
            updateSummaryBitrate(spf, key);
        } else if (key.equals(keyprefVideoCall)
                || key.equals(keyprefHwCodec)
                || key.equals(keyprefCpuUsageDetection)
                || key.equals(keyPrefDisplayHud)) {
            updateSummaryB(spf, key);
        }
        if (key.equals(keyprefStartVideoBitrateType)) {
            setVideoBitrateEnable(spf);
        }
        if (key.equals(keyprefStartAudioBitrateType)) {
            setAudioBitrateEnable(spf);
        }

    }

    private void updateSummary(SharedPreferences spf, String key) {

        Preference preference = pfm.findPreference(key);
        preference.setSummary(spf.getString(key, ""));
    }

    private void updateSummaryBitrate(
            SharedPreferences spf, String key) {
        Preference updatedPref = pfm.findPreference(key);
        updatedPref.setSummary(spf.getString(key, "") + " kbps");
    }

    private void updateSummaryB(SharedPreferences spf, String key) {
        Preference updatedPref = pfm.findPreference(key);
        updatedPref.setSummary(spf.getBoolean(key, true)
                ? getString(R.string.pref_value_enabled)
                : getString(R.string.pref_value_disabled));
    }

    private void setVideoBitrateEnable(SharedPreferences spf) {
        Preference bitratePreferenceValue =
                pfm.findPreference(getString(R.string.pref_startvideobitratevalue_key));
        String bitrateTypeDefault = getString(R.string.pref_startvideobitrate_default);
        String bitrateType = spf.getString(
                getString(R.string.pref_startvideobitrate_key), bitrateTypeDefault);
        if (bitrateType.equals(bitrateTypeDefault)) {
            bitratePreferenceValue.setEnabled(false);
        } else {
            bitratePreferenceValue.setEnabled(true);
        }
    }

    private void setAudioBitrateEnable(SharedPreferences spf) {
        Preference bitratePreferenceValue =
                pfm.findPreference(keyprefStartAudioBitrateValue);
        String bitrateTypeDefault = getString(R.string.pref_startaudiobitrate_default);
        String bitrateType = spf.getString(
                keyprefStartAudioBitrateType, bitrateTypeDefault);
        if (bitrateType.equals(bitrateTypeDefault)) {
            bitratePreferenceValue.setEnabled(false);
        } else {
            bitratePreferenceValue.setEnabled(true);
        }
    }


    public static class MyPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_settings);
        }


    }
}
