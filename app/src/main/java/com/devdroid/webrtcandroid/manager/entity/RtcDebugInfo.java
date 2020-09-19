package com.devdroid.webrtcandroid.manager.entity;

import org.webrtc.PeerConnection;

import java.lang.reflect.Field;

/**
 * Contains WebRTC Stats Report for debug info.
 */
public class RtcDebugInfo {

    private PeerConnection.IceConnectionState state;
    private RtcDebugVidStats video_stats;
    private RtcDebugAudioStats audio_stats;

    public PeerConnection.IceConnectionState getIceConnectionState() {
        return state;
    }

    public void setIceConnectionState(PeerConnection.IceConnectionState iceConnectionState) {
        this.state = iceConnectionState;
    }

    public RtcDebugAudioStats getAudStats() {
        return audio_stats;
    }

    public void setAudStats(RtcDebugAudioStats audStats) {
        this.audio_stats = audStats;
    }

    public RtcDebugVidStats getVidStats() {
        return video_stats;
    }

    public void setVidStats(RtcDebugVidStats vidStats) {
        this.video_stats = vidStats;
    }

    /** Get log String of video stats report */
    public String getLogStats(RtcDebugInfo rtcDebugInfo) {
        String log = "";
        String stateLog = "", vidLog = "", audioLog = "";
        try {
            Field[] fieldArr = rtcDebugInfo.getClass().getDeclaredFields();

            for (Field field : fieldArr) {
                if (field.getName().equals("state")) {
                    stateLog = field.getName() + ": "
                            + String.valueOf(field.get(rtcDebugInfo)) + "\n";
                } else if (field.getName().equals("video_stats")) {
                    if (field.get(rtcDebugInfo) != null) {
                        RtcDebugVidStats vidStats = (RtcDebugVidStats) field.get(rtcDebugInfo);
                        vidLog = "--- VIDEO ---\n" + vidStats.getLogStats(vidStats);
                    }
                } else if (field.getName().equals("audio_stats")) {
                    if (field.get(rtcDebugInfo) != null) {
                        RtcDebugAudioStats audioStats = (RtcDebugAudioStats) field.get(rtcDebugInfo);
                        audioLog = "--- AUDIO ---\n" + audioStats.getLogStats(audioStats);
                    }
                }
            }

            log = log.concat(stateLog + audioLog + vidLog);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return log;
    }

}
