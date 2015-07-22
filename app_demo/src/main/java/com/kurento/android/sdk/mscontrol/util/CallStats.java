package com.kurento.android.sdk.mscontrol.util;

import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

import org.webrtc.PeerConnection;
import org.webrtc.StatsObserver;
import org.webrtc.StatsReport;

import java.util.HashMap;
import java.util.Map;

public class CallStats {

//    private static final Logger log = LoggerFactory
//            .getLogger(CallStats.class.getSimpleName());

    public static void getCallStats(PeerConnection peerConnection, final TextView hudView) {
        final StringBuilder builder = new StringBuilder();

        if (peerConnection == null) {
            return;
        }

        boolean success = peerConnection.getStats(new StatsObserver() {
            public void onComplete(final StatsReport[] reports) {
                Handler mainHandler = new Handler(hudView.getContext().getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        builder.append(getCallStats(reports));
                        builder.append(getMediaStats(reports));

                        Spanned builderHtlm = Html.fromHtml(builder.toString());
                        hudView.setText(builderHtlm);
                    }
                };

                mainHandler.post(myRunnable);
            }
        }, null);
        if (!success) {
            Handler mainHandler = new Handler(hudView.getContext().getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    Spanned builderHtlm = Html.fromHtml("<b>Error: Get stats return false</b><br/>");
                    hudView.setText(builderHtlm);
                }
            };

            mainHandler.post(myRunnable);
        }
    }

    private static String getCallStats(StatsReport[] reports) {
        StringBuilder builder = new StringBuilder();

        builder.append("<b><u>Call stats:</u></b>");
        builder.append("<br/>");

        //Call stats
        for (StatsReport report : reports) {
            if (report.id.equals("bweforvideo")) {
                for (StatsReport.Value value : report.values) {
                    if ((value.name.equals("googAvailableSendBandwidth"))
                            || (value.name.equals("googAvailableReceiveBandwidth"))
                            || (value.name.equals("googTargetEncBitrate"))
                            || (value.name.equals("googActualEncBitrate"))
                            || (value.name.equals("googTransmitBitrate"))) {

                        String name = value.name.replace("goog", "");
                        builder.append(name).append("=").append(value.value);
                        builder.append("<br/>");
                    }
                }
            }
        }
        builder.append("<br/>");

        return builder.toString();
    }

    private static String getMediaStats(StatsReport[] reports) {
        StringBuilder builder = new StringBuilder();

        builder.append(getSendMediaStats(reports));
        builder.append(getRecvMediaStats(reports));

        return builder.toString();
    }

    private static String getSendMediaStats(StatsReport[] reports) {
        StringBuilder builder = new StringBuilder();

        builder.append("<b><u>Send media stats:</u></b>");
        builder.append("<br/>");

        //Send media stats
        for (StatsReport report : reports) {
            if (report.type.equals("ssrc")
                    && report.id.contains("ssrc")
                    && report.id.contains("send")) {
                Map<String, String> reportMap = getReportMap(report);
                if (reportMap.containsKey("googFrameWidthSent")) {
                    //We are on send video
                    builder.append("<b>->Send video:</b>");
                    builder.append("<br/>");
                    for (StatsReport.Value value : report.values) {
                        if ((value.name.equals("bytesSent"))
                                || (value.name.equals("packetsSent"))
                                || (value.name.equals("packetsLost"))
                                || (value.name.equals("googFrameWidthSent"))
                                || (value.name.equals("googFrameHeightSent"))
                                || (value.name.equals("googFrameRateSent"))
                                || (value.name.equals("googCodecName"))
                                || (value.name.equals("googCaptureJitterMs"))
                                || (value.name.equals("googEncodeUsagePercent"))) {

                            String name = value.name.replace("goog", "");
                            builder.append(name).append("=").append(value.value);
                            builder.append("<br/>");
                        }
                    }
                } else if (reportMap.containsKey("audioInputLevel")) {
                    //We are on send audio
                    builder.append("<b>->Send audio:</b>");
                    builder.append("<br/>");
                    for (StatsReport.Value value : report.values) {
                        if ((value.name.equals("audioInputLevel"))
                                || (value.name.equals("bytesSent"))
                                || (value.name.equals("packetsSent"))
                                || (value.name.equals("packetsLost"))
                                || (value.name.equals("googJitterReceived"))
                                || (value.name.equals("googEchoCancellationReturnLoss"))
                                || (value.name.equals("googCodecName"))) {

                            String name = value.name.replace("goog", "");
                            builder.append(name).append("=").append(value.value);
                            builder.append("<br/>");
                        }
                    }
                }
            }
        }

        builder.append("<br/>");

        return builder.toString();
    }

    private static String getRecvMediaStats(StatsReport[] reports) {
        StringBuilder builder = new StringBuilder();

        builder.append("<b><u>Recv media stats:</u></b>");
        builder.append("<br/>");

        //Receive media stats
        for (StatsReport report : reports) {
            if (report.type.equals("ssrc")
                    && report.id.contains("ssrc")
                    && report.id.contains("recv")) {
                Map<String, String> reportMap = getReportMap(report);
                if (reportMap.containsKey("googFrameWidthReceived")) {
                    //We are on recv video
                    builder.append("<b>->Recv video:</b>");
                    builder.append("<br/>");
                    for (StatsReport.Value value : report.values) {
                        if ((value.name.equals("bytesReceived"))
                                || (value.name.equals("packetsReceived"))
                                || (value.name.equals("packetsLost"))
                                || (value.name.equals("googFrameWidthReceived"))
                                || (value.name.equals("googFrameHeightReceived"))
                                || (value.name.equals("googFrameRateReceived"))
                                || (value.name.equals("googDecodeMs"))
                                || (value.name.equals("googCurrentDelayMs"))
                                || (value.name.equals("googJitterBufferMs"))
                                || (value.name.equals("googRenderDelayMs"))) {

                            String name = value.name.replace("goog", "");
                            builder.append(name).append("=").append(value.value);
                            builder.append("<br/>");
                        }
                    }
                } else if (reportMap.containsKey("audioOutputLevel")) {
                    //We are on recv audio
                    builder.append("<b>->Recv audio:</b>");
                    builder.append("<br/>");
                    for (StatsReport.Value value : report.values) {
                        if ((value.name.equals("audioOutputLevel"))
                                || (value.name.equals("googJitterReceived"))
                                || (value.name.equals("googJitterBufferMs"))
                                || (value.name.equals("googCurrentDelayMs"))
                                || (value.name.equals("packetsReceived"))
                                || (value.name.equals("packetsLost"))
                                || (value.name.equals("googCodecName"))) {

                            String name = value.name.replace("goog", "");
                            builder.append(name).append("=").append(value.value);
                            builder.append("<br/>");
                        }
                    }
                }
            }
        }

        return builder.toString();
    }

    private static Map<String, String> getReportMap(StatsReport report) {
        Map<String, String> reportMap = new HashMap<String, String>();
        for (StatsReport.Value value : report.values) {
            reportMap.put(value.name, value.value);
        }
        return reportMap;
    }
}
