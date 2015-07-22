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
package com.kurento.android.sdk.mscontrol.webrtc;

import com.example.jchsohu.app_demo.util.LogCat;

import org.webrtc.AudioSource;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaConstraints.KeyValuePair;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;

public class PeerConnectionFactorySingleton extends PeerConnectionFactory {

    private static PeerConnectionFactorySingleton instance = null;

    private PeerConnectionFactorySingleton() {
    }

    public synchronized static PeerConnectionFactorySingleton getInstance() {
        if (instance == null) {
            instance = new PeerConnectionFactorySingleton();
        }

        return instance;
    }

    /* Video */
    private VideoSource videoSource;

    /**
     * 获取图像资源。
     *
     * @param useFrontFacing 是否使用前置摄像头。
     * @return
     */
    public synchronized VideoSource getVideoSource(boolean useFrontFacing) {
        PeerConnectionFactory peerConnectionFactory = PeerConnectionFactorySingleton
                .getInstance();
        VideoCapturer videoCapturer = null;

        String[] cameraFacing = {"front", "back"};
        if (!useFrontFacing) {
            cameraFacing[0] = "back";
            cameraFacing[1] = "front";
        }

        int[] cameraIndex = {0, 1};
        int[] cameraOrientation = {0, 90, 180, 270};
        for (String facing : cameraFacing) {
            for (int index : cameraIndex) {
                for (int orientation : cameraOrientation) {
                    String name = "Camera " + index + ", Facing " + facing +
                            ", Orientation " + orientation;
                    videoCapturer = VideoCapturer.create(name);
                    if (videoCapturer != null) {
                        LogCat.debug("Using camera: " + name);
                        break;
                    }
                }
                if (videoCapturer != null) {
                    break;
                }
            }
            if (videoCapturer != null) {
                break;
            }
        }
        if (videoCapturer == null) {
            LogCat.debug("No camera allowed");
            return null;
        }

        if (videoSource != null) {
            videoSource.stop();
            videoSource.dispose();
        }

        MediaConstraints vc = new MediaConstraints();
        //TODO: Find a way to set these media constraints.
        //Now are disabled because if not, it does not work.

        vc.optional.add(new KeyValuePair("maxWidth", "640"));
        vc.optional.add(new KeyValuePair("maxHeight", "480"));
        vc.optional.add(new KeyValuePair("maxFrameRate", "15"));

        videoSource = peerConnectionFactory
                .createVideoSource(videoCapturer, vc);

        return videoSource;
    }

    synchronized void disposeVideoSource() {
        if (videoSource != null) {
            videoSource.stop();
            videoSource.dispose();
            videoSource = null;
        }
    }

    static AudioSource createAudioSource() {
        MediaConstraints ac = new MediaConstraints();
        ac.optional.add(new KeyValuePair("googEchoCancellation", "true"));
        ac.optional.add(new KeyValuePair("googEchoCancellation2", "false"));
        ac.optional.add(new KeyValuePair("googAutoGainControl", "true"));
        ac.optional.add(new KeyValuePair("googAutoGainControl2", "true"));
        ac.optional.add(new KeyValuePair("googNoiseSuppression", "true"));
        ac.optional.add(new KeyValuePair("googNoiseSuppression2", "false"));
        ac.optional.add(new KeyValuePair("googHighpassFilter", "true"));
        ac.optional.add(new KeyValuePair("googTypingNoiseDetection", "true"));

        return PeerConnectionFactorySingleton.getInstance().createAudioSource(
                ac);
    }

}
