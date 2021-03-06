package com.jch.kw.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jingbiaowang on 2015/7/23.
 */
public class SettingsBean implements Parcelable {

    private boolean videoCallEnable;
    private int videoWidth;
    private int videoHeight;
    private int fps;
    /**
     * 是否手动设置视频频率。
     */
    private String startVidoBitrate;
    private int startVidoBitrateValue;
    private String videoCode;
    /**
     * 视频引见加速。
     */
    private boolean hwCodeEnable;
    /**
     * 是否手动设置音频频率。
     */
    private String audioBitrate;
    private int audioBitrateValue;
    private String audioCode;
    private boolean cpuUsageDetection;
    private String serverUrl;
    private boolean displayHud;
    private UserType userType;


    public SettingsBean(boolean videoCallEnable, int videoWidth, int videoHeight, int fps, String startVidoBitrate, int startVidoBitrateValue, String videoCode, boolean hwCodeEnable, String audioBitrate, int audioBitrateValue, String audioCode, boolean cpuUsageDetection, String serverUrl, boolean displayHud, UserType userType) {
        this.videoCallEnable = videoCallEnable;
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.fps = fps;
        this.startVidoBitrate = startVidoBitrate;
        this.startVidoBitrateValue = startVidoBitrateValue;
        this.videoCode = videoCode;
        this.hwCodeEnable = hwCodeEnable;
        this.audioBitrate = audioBitrate;
        this.audioBitrateValue = audioBitrateValue;
        this.audioCode = audioCode;
        this.cpuUsageDetection = cpuUsageDetection;
        this.serverUrl = serverUrl;
        this.displayHud = displayHud;
        this.userType = userType;
    }

    public boolean isVideoCallEnable() {
        return videoCallEnable;
    }

    public void setVideoCallEnable(boolean videoCallEnable) {
        this.videoCallEnable = videoCallEnable;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public void setVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public String getStartVidoBitrate() {
        return startVidoBitrate;
    }

    public void setStartVidoBitrate(String startVidoBitrate) {
        this.startVidoBitrate = startVidoBitrate;
    }

    public int getStartVidoBitrateValue() {
        return startVidoBitrateValue;
    }

    public void setStartVidoBitrateValue(int startVidoBitrateValue) {
        this.startVidoBitrateValue = startVidoBitrateValue;
    }

    public String getVideoCode() {
        return videoCode;
    }

    public void setVideoCode(String videoCode) {
        this.videoCode = videoCode;
    }

    public boolean isHwCodeEnable() {
        return hwCodeEnable;
    }

    public void setHwCodeEnable(boolean hwCodeEnable) {
        this.hwCodeEnable = hwCodeEnable;
    }

    public String getAudioBitrate() {
        return audioBitrate;
    }

    public void setAudioBitrate(String audioBitrate) {
        this.audioBitrate = audioBitrate;
    }

    public int getAudioBitrateValue() {
        return audioBitrateValue;
    }

    public void setAudioBitrateValue(int audioBitrateValue) {
        this.audioBitrateValue = audioBitrateValue;
    }

    public String getAudioCode() {
        return audioCode;
    }

    public void setAudioCode(String audioCode) {
        this.audioCode = audioCode;
    }

    public boolean isCpuUsageDetection() {
        return cpuUsageDetection;
    }

    public void setCpuUsageDetection(boolean cpuUsageDetection) {
        this.cpuUsageDetection = cpuUsageDetection;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public boolean isDisplayHud() {
        return displayHud;
    }

    public void setDisplayHud(boolean displayHud) {
        this.displayHud = displayHud;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(videoCallEnable ? (byte) 1 : (byte) 0);
        dest.writeInt(this.videoWidth);
        dest.writeInt(this.videoHeight);
        dest.writeInt(this.fps);
        dest.writeString(this.startVidoBitrate);
        dest.writeInt(this.startVidoBitrateValue);
        dest.writeString(this.videoCode);
        dest.writeByte(hwCodeEnable ? (byte) 1 : (byte) 0);
        dest.writeString(this.audioBitrate);
        dest.writeInt(this.audioBitrateValue);
        dest.writeString(this.audioCode);
        dest.writeByte(cpuUsageDetection ? (byte) 1 : (byte) 0);
        dest.writeString(this.serverUrl);
        dest.writeByte(displayHud ? (byte) 1 : (byte) 0);
        dest.writeInt(this.userType == null ? -1 : this.userType.ordinal());
    }

    public SettingsBean() {
    }

    protected SettingsBean(Parcel in) {
        this.videoCallEnable = in.readByte() != 0;
        this.videoWidth = in.readInt();
        this.videoHeight = in.readInt();
        this.fps = in.readInt();
        this.startVidoBitrate = in.readString();
        this.startVidoBitrateValue = in.readInt();
        this.videoCode = in.readString();
        this.hwCodeEnable = in.readByte() != 0;
        this.audioBitrate = in.readString();
        this.audioBitrateValue = in.readInt();
        this.audioCode = in.readString();
        this.cpuUsageDetection = in.readByte() != 0;
        this.serverUrl = in.readString();
        this.displayHud = in.readByte() != 0;
        int tmpUserType = in.readInt();
        this.userType = tmpUserType == -1 ? null : UserType.values()[tmpUserType];
    }

    public static final Parcelable.Creator<SettingsBean> CREATOR = new Parcelable.Creator<SettingsBean>() {
        public SettingsBean createFromParcel(Parcel source) {
            return new SettingsBean(source);
        }

        public SettingsBean[] newArray(int size) {
            return new SettingsBean[size];
        }
    };
}
