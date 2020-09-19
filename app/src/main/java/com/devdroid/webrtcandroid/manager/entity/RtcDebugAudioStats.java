package com.devdroid.webrtcandroid.manager.entity;

import org.webrtc.StatsReport;

import java.lang.reflect.Field;

/**
 * Values for debug video stats report.
 */
public class RtcDebugAudioStats {

    private String codec_name;

    private long bytes_rcv;
    private long bytes_sent;
    private int packets_rcv;
    private int packets_sent;
    private int packets_lost;
    private int stream_delay;

    // Audio Stats Report for Listener
    private int audio_output_level;

    // Audio Stats Report for Publisher
    private int audio_input_level;

    public String getCodecName() {
        return codec_name;
    }

    public void setCodecName(String codecName) {
        this.codec_name = codecName;
    }

    public long getBytesSent() {
        return bytes_sent;
    }

    public void setBytesSent(long bytesSent) {
        this.bytes_sent = bytesSent;
    }

    public long getBytesRcv() {
        return bytes_rcv;
    }

    public void setBytesRcv(long bytesRcv) {
        this.bytes_rcv = bytesRcv;
    }

    public int getPacketsRcv() {
        return packets_rcv;
    }

    public void setPacketsRcv(int packetsRcv) {
        this.packets_rcv = packetsRcv;
    }

    public int getPacketsSent() {
        return packets_sent;
    }

    public void setPacketsSent(int packetsSent) {
        this.packets_sent = packetsSent;
    }

    public int getPacketsLost() {
        return packets_lost;
    }

    public void setPacketsLost(int packetsLost) {
        this.packets_lost = packetsLost;
    }

    public int getCurrentDelayMs() {
        return stream_delay;
    }

    public void setCurrentDelayMs(int currentDelayMs) {
        this.stream_delay = currentDelayMs;
    }

    public int getAudioInputLevel() {
        return audio_input_level;
    }

    public void setAudioInputLevel(int audioInputLevel) {
        this.audio_input_level = audioInputLevel;
    }

    public int getAudioOutputLevel() {
        return audio_output_level;
    }

    public void setAudioOutputLevel(int audioOutputLevel) {
        this.audio_output_level = audioOutputLevel;
    }

    /** Put values to specific stats field. */
    public void putValues(StatsReport.Value value) {
        if (value.name.equals(RtcDebugKeyNames.BYTES_SENT))
            setBytesSent(Long.parseLong(value.value));
        if (value.name.equals(RtcDebugKeyNames.BYTES_RCV))
            setBytesRcv(Long.parseLong(value.value));
        if (value.name.equals(RtcDebugKeyNames.PACKETS_SENT))
            setPacketsSent(Integer.parseInt(value.value));
        if (value.name.equals(RtcDebugKeyNames.PACKETS_RCV))
            setPacketsRcv(Integer.parseInt(value.value));
        if (value.name.equals(RtcDebugKeyNames.PACKETS_LOST))
            setPacketsLost(Integer.parseInt(value.value));
        if (value.name.equals(RtcDebugKeyNames.STREAM_DELAY))
            setCurrentDelayMs(Integer.parseInt(value.value));

        if (value.name.equals(RtcDebugKeyNames.CODEC_NAME))
            setCodecName(value.value);

        if (value.name.equals(RtcDebugKeyNames.AUDIO_INPUT_LEVEL))
            setAudioInputLevel(Integer.parseInt(value.value));
        if (value.name.equals(RtcDebugKeyNames.AUDIO_OUTPUT_LEVEL))
            setAudioOutputLevel(Integer.parseInt(value.value));
    }

    /** Get log String of audio stats report */
    public String getLogStats(RtcDebugAudioStats rtcDebugAudioStats) {
        String log = "";
        try {
            Field[] fieldArr = rtcDebugAudioStats.getClass().getDeclaredFields();
            for (Field field : fieldArr) {
                String logValue = "---";
                Object fieldValue = field.get(rtcDebugAudioStats);
                if (fieldValue != null) {
                    logValue = String.valueOf(field.get(rtcDebugAudioStats));
                }
                log = log.concat(field.getName() + ": " + logValue + "\n");
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return log;
    }
}
