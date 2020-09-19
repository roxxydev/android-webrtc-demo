package com.devdroid.webrtcandroid.bus;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class EventBusUtil {

    private static final Bus mBus = new Bus(ThreadEnforcer.ANY);

    public static Bus getInstance() {
        return EventBusUtil.mBus;
    }

    /** Event bus for WebRTC Stats Report evaluated. */
    public static final class OnRtcStatsReportEvaluated {

        public boolean isStreamRcv = false;
        public boolean isStreamSent = false;

        public OnRtcStatsReportEvaluated(boolean isStreamRcv, boolean isStreamSent) {
            this.isStreamRcv = isStreamRcv;
            this.isStreamSent = isStreamSent;
        }
    }

}
