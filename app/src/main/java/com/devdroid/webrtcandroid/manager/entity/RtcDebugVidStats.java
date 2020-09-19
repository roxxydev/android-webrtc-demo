package com.devdroid.webrtcandroid.manager.entity;

import org.webrtc.StatsReport;

import java.lang.reflect.Field;

/**
 * Values for debug video stats report.
 */
public class RtcDebugVidStats {

    private String codec_name;

    private long bytes_rcv;
    private long bytes_sent;
    private int packets_rcv;
    private int packets_sent;
    private int packets_lost;
    private int stream_delay;

    // Video Stats Report for Listener
    private String frame_width_rcv;
    private String frame_height_rcv;
    private String frame_rate_rcv;

    // Video Stats Report for Publisher
    private String frame_width_input;
    private String frame_width_sent;
    private String frame_height_input;
    private String frame_height_sent;
    private String frame_rate_input;
    private String frame_rate_sent;
    private boolean is_bandwidth_limited_res;
    private boolean is_cpu_limited_res;
    private int encode_usage_percent;

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

    public int getEncodeUsagePercent() {
        return encode_usage_percent;
    }

    public void setEncodeUsagePercent(int encodeUsagePercent) {
        this.encode_usage_percent = encodeUsagePercent;
    }

    public String getFrameHeightInput() {
        return frame_height_input;
    }

    public void setFrameHeightInput(String frameHeightInput) {
        this.frame_height_input = frameHeightInput;
    }

    public String getFrameHeightRcv() {
        return frame_height_rcv;
    }

    public void setFrameHeightRcv(String frameHeightRcv) {
        this.frame_height_rcv = frameHeightRcv;
    }

    public String getFrameHeightSent() {
        return frame_height_sent;
    }

    public void setFrameHeightSent(String frameHeightSent) {
        this.frame_height_sent = frameHeightSent;
    }

    public String getFrameRateInput() {
        return frame_rate_input;
    }

    public void setFrameRateInput(String frameRateInput) {
        this.frame_rate_input = frameRateInput;
    }

    public String getFrameRateRcv() {
        return frame_rate_rcv;
    }

    public void setFrameRateRcv(String frameRateRcv) {
        this.frame_rate_rcv = frameRateRcv;
    }

    public String getFrameRateSent() {
        return frame_rate_sent;
    }

    public void setFrameRateSent(String frameRateSent) {
        this.frame_rate_sent = frameRateSent;
    }

    public String getFrameWidthInput() {
        return frame_width_input;
    }

    public void setFrameWidthInput(String frameWidthInput) {
        this.frame_width_input = frameWidthInput;
    }

    public String getFrameWidthRcv() {
        return frame_width_rcv;
    }

    public void setFrameWidthRcv(String frameWidthRcv) {
        this.frame_width_rcv = frameWidthRcv;
    }

    public String getFrameWidthSent() {
        return frame_width_sent;
    }

    public void setFrameWidthSent(String frameWidthSent) {
        this.frame_width_sent = frameWidthSent;
    }

    public boolean isBandwidthLimitedResolution() {
        return is_bandwidth_limited_res;
    }

    public void setIsBandwidthLimitedResolution(boolean isBandwidthLimitedResolution) {
        this.is_bandwidth_limited_res = isBandwidthLimitedResolution;
    }

    public boolean isCpuLimitedResolution() {
        return is_cpu_limited_res;
    }

    public void setIsCpuLimitedResolution(boolean isCpuLimitedResolution) {
        this.is_cpu_limited_res = isCpuLimitedResolution;
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

        if (value.name.equals(RtcDebugKeyNames.FRAME_WIDTH_INPUT))
            setFrameWidthInput(value.value);
        if (value.name.equals(RtcDebugKeyNames.FRAME_WIDTH_SENT))
            setFrameWidthSent(value.value);
        if (value.name.equals(RtcDebugKeyNames.FRAME_HEIGHT_INPUT))
            setFrameHeightInput(value.value);
        if (value.name.equals(RtcDebugKeyNames.FRAME_HEIGHT_SENT))
            setFrameHeightSent(value.value);
        if (value.name.equals(RtcDebugKeyNames.FRAME_RATE_INPUT))
            setFrameRateInput(value.value);
        if (value.name.equals(RtcDebugKeyNames.FRAME_RATE_SENT))
            setFrameRateSent(value.value);
        if (value.name.equals(RtcDebugKeyNames.BANDWIDTH_LIMITED_RES))
            setIsBandwidthLimitedResolution(
                    Boolean.parseBoolean(value.value));
        if (value.name.equals(RtcDebugKeyNames.CPU_LIMITED_RES))
            setIsCpuLimitedResolution(
                    Boolean.parseBoolean(value.value));
        if (value.name.equals(RtcDebugKeyNames.ENCODE_CPU_USAGE))
            setIsCpuLimitedResolution(
                    Boolean.parseBoolean(value.value));
        if (value.name.equals(RtcDebugKeyNames.FRAME_WIDTH_RCV))
            setFrameWidthRcv(value.value);
        if (value.name.equals(RtcDebugKeyNames.FRAME_HEIGHT_RCV))
            setFrameHeightRcv(value.value);
        if (value.name.equals(RtcDebugKeyNames.FRAME_RATE_RCV))
            setFrameRateRcv(value.value);
    }

    /** Get log String of video stats report */
    public String getLogStats(RtcDebugVidStats rtcDebugVidStats) {
        String log = "";
        try {
            Field[] fieldArr = rtcDebugVidStats.getClass().getDeclaredFields();
            for (Field field : fieldArr) {
                String logValue = "---";
                Object fieldValue = field.get(rtcDebugVidStats);
                if (fieldValue != null) {
                    logValue = String.valueOf(field.get(rtcDebugVidStats));
                }
                log = log.concat(field.getName() + ": " + logValue + "\n");
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return log;
    }

}
