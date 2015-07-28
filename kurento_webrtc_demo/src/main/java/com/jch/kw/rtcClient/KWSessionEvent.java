package com.jch.kw.rtcClient;

import org.webrtc.SessionDescription;

/**
 * Created by jingbiaowang on 2015/7/24.
 */
public interface KWSessionEvent {

    void createOffer();

    void processAnwser(String anwser);

    /**
     * stop.
     */
    public void dispose();

}
