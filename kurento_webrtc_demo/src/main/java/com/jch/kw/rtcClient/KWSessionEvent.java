package com.jch.kw.rtcClient;

import org.webrtc.SessionDescription;

/**
 * Created by jingbiaowang on 2015/7/24.
 */
public interface KWSessionEvent {

    void createOffer();

    void processAnwser(SessionDescription anwser);

    /**
     * stop.
     */
    public void dispose();

}
