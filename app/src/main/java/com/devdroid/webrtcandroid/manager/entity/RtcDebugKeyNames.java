package com.devdroid.webrtcandroid.manager.entity;

/**
 * Key value name mapping for WebRTC Stats Report.
 */
public class RtcDebugKeyNames {

    public static final String TRACK_ID = "googTrackId";

    public static final String CODEC_NAME = "googCodecName";

    public static final String BYTES_RCV = "bytesReceived";
    public static final String BYTES_SENT = "bytesSent";
    public static final String PACKETS_RCV = "packetsReceived";
    public static final String PACKETS_SENT = "packetsSent";
    public static final String PACKETS_LOST = "packetsLost";
    public static final String STREAM_DELAY = "googCurrentDelayMs";

    public static final String FRAME_WIDTH_INPUT = "googFrameWidthInput";
    public static final String FRAME_WIDTH_SENT = "googFrameWidthSent";
    public static final String FRAME_HEIGHT_INPUT = "googFrameHeightInput";
    public static final String FRAME_HEIGHT_SENT = "googFrameHeightSent";
    public static final String FRAME_RATE_INPUT = "googFrameRateInput";
    public static final String FRAME_RATE_SENT = "googFrameRateSent";
    public static final String BANDWIDTH_LIMITED_RES = "googBandwidthLimitedResolution";
    public static final String CPU_LIMITED_RES = "googCpuLimitedResolution";
    public static final String ENCODE_CPU_USAGE = "googEncodeUsagePercent";

    public static final String FRAME_WIDTH_RCV = "googFrameWidthReceived";
    public static final String FRAME_HEIGHT_RCV = "googFrameHeightReceived";
    public static final String FRAME_RATE_RCV = "googFrameRateReceived";

    public static final String AUDIO_INPUT_LEVEL = "audioInputLevel";
    public static final String AUDIO_OUTPUT_LEVEL = "audioOutputLevel";

}
