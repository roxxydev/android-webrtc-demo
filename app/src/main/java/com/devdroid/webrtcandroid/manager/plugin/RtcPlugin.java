package com.devdroid.webrtcandroid.manager.plugin;

import android.util.Log;

import com.devdroid.webrtcandroid.manager.entity.IceCandidates;
import com.devdroid.webrtcandroid.manager.entity.RtcDebugAudioStats;
import com.devdroid.webrtcandroid.manager.entity.RtcDebugInfo;
import com.devdroid.webrtcandroid.manager.entity.RtcDebugVidStats;
import com.devdroid.webrtcandroid.manager.media.RtcMedia;

import com.devdroid.webrtcandroid.util.StringUtils;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.StatsObserver;
import org.webrtc.StatsReport;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RtcPlugin represents one P2P connection or one call.
 */
public class RtcPlugin implements SdpObserver, PeerConnection.Observer {
    
    private static final String TAG = RtcPlugin.class.getSimpleName();

    private String mId;

    private List<PeerConnection.IceServer> mArrIceServer;
    private OnIceCandidateListener iceCandidateListener;
    private PeerConnectionFactory mPeerConnectionFactory;
    private MediaStream mLocalMediaStream;
    private PeerConnection mPeerConn;
    private RtcMedia mRtcMedia;
    
    private OnStreamCallbackListener mOnStreamCallbackListener;
    private OnSdpEventListener mOnSdpEventListener;
    private OnIceStateListener mOnIceStateListener;

    private RtcDebugInfo rtcDebugInfo;

    private boolean isSendingAudio = false;
    private boolean isSendingVideo = false;

    private boolean isReceivingAudio = false;
    private boolean isReceivingVideo = false;

    // Flag if RtcPlugin object has been destoyed and cannot be used anymore
    private boolean isDestroyed;

    public enum IcePeerState {
        CONNECTED,
        DISONNECTED,
        FAILED
    }

    /** Callback for Peer Offer or Answer created. */
    public interface OnSdpEventListener {
        /**
         * Callback when offer or answer was successfully.
         * @param sdp The String SDP value of the user.
         */
        void onSuccess(String sdp);

        /**
         * Callback when offer or answer got an error during creation.
         */
        void onError();
    }

    /** Listener when Peer ICE connection state changes. */
    public interface OnIceStateListener {
        void onIceState(IcePeerState peerState, PeerConnection.IceConnectionState iceConnectionState);
    }

    /** Listener when local/remote stream is ready. */
    public interface OnStreamCallbackListener {
        /**
         * Callback when stream of local Peer created.
         */
        void onLocalStream(MediaStream localStream);

        /** Callback when stream of remote Peer received. */
        void onRemoteStream(MediaStream remoteStream);
    }

    /** Listener when PeerConnection IceCandidate has been created. */
    public interface OnIceCandidateListener {
        /**
         * Callback when IceCandidate is created.
         */
        void onIceCandidateCreated(IceCandidates iceCandidate);
    }


    public RtcPlugin() {
        mId = UUID.randomUUID().toString();
    }


    /** Get the unique id the RtcPlugin. */
    public String getId() {
        return mId;
    }

    /** Get the RtcDebugInfo for showing debug info of WebRTC Stats Report. */
    public RtcDebugInfo getRtcDebugInfo() {
        if (rtcDebugInfo == null) {
            Log.w(TAG, "getRtcDebugInfo has returned new instance of RtcDebugInfo");
            return new RtcDebugInfo();
        }
        return this.rtcDebugInfo;
    }

    public boolean isReceivingStream() {
        return (isReceivingAudio && isReceivingVideo);
    }

    public boolean isSendingStream() {
        return (isSendingAudio && isSendingVideo);
    }

    /**
     * Return true if this RtcPlugin has been closed and disposed already which means
     * it cannot be used anymore.
     */
    public boolean isDestroyed() {
        return isDestroyed;
    }

    /** Set listener for Peer ICE connection state change. */
    public void setOnIceStateListener(OnIceStateListener iceStateListener) {
        this.mOnIceStateListener = iceStateListener;
    }

    /** MUST call this before any transaction of offer or answer. */
    public void setUp(List<PeerConnection.IceServer> arrIceServer,
                      OnIceCandidateListener iceCandidateListener,
                      PeerConnectionFactory peerConnectionFactory,
                      MediaStream localMediaStream,
                      RtcMedia rtcMedia) {
        this.rtcDebugInfo = new RtcDebugInfo();
        this.mArrIceServer = arrIceServer;
        this.iceCandidateListener = iceCandidateListener;
        this.mPeerConnectionFactory = peerConnectionFactory;
        this.mLocalMediaStream = localMediaStream;
        this.mRtcMedia = rtcMedia;

        this.mPeerConn = mPeerConnectionFactory.createPeerConnection(mArrIceServer,
                mRtcMedia.createIceConstraints(), this);
    }

    /** Get StatsReport from MediaStreamTrack of PeerConnection. */
    public void getStatsReport(final boolean isLocalStream, final boolean isVideo,
                               MediaStreamTrack streamTrack) {
        if (!isDestroyed) {
            StatsObserver statsObserver = new StatsObserver() {
                @Override
                public void onComplete(StatsReport[] statsReports) {
                    for (StatsReport report : statsReports) {
                        fillUpRtcDebugInfo(report);
                        if (StringUtils.isValid(report.id) &&
                                (report.id.contains("send") || report.id.contains("recv"))) {
                            for (StatsReport.Value value : report.values) {
                                if (StringUtils.isValid(value.name) &&
                                        value.name.equalsIgnoreCase("bytesSent")) {
                                    long byteSent = Long.parseLong(value.value);
                                    boolean hasSentStream = byteSent > 0 ? true : false;

                                    //if (!isSendingStream && isLocalStream) {
                                    if (isLocalStream) {
                                        if (isVideo) {
                                            //Log.d(TAG, "getStatsReport video byteSent: " + byteSent);
                                            isSendingVideo = hasSentStream;
                                        } else {
                                            //Log.d(TAG, "getStatsReport audio byteSent: " + byteSent);
                                            isSendingAudio = hasSentStream;
                                        }
                                    }
                                }
                                if (StringUtils.isValid(value.name) &&
                                        value.name.equalsIgnoreCase("bytesReceived")) {
                                    long byteRecv = Long.parseLong(value.value);
                                    boolean hasRcvStream = byteRecv > 0 ? true : false;

                                    //if (!isReceivingStream && !isLocalStream) {
                                    if (!isLocalStream) {
                                        if (isVideo) {
                                            //Log.d(TAG, "getStatsReport video byteRecv: " + byteRecv);
                                            isReceivingVideo = hasRcvStream;
                                        } else {
                                            //Log.d(TAG, "getStatsReport audio byteRecv: " + byteRecv);
                                            isReceivingAudio = hasRcvStream;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            };
            if (streamTrack != null && !isDestroyed) {
                mPeerConn.getStats(statsObserver, streamTrack);
            }
        }
    }

    /**
     * Create offer then send the SDP.
     * @param onStreamCallbackListener The callback for stream when ready.
     * @param onSdpEventListener The callback for sdp offer/answer created.
     */
    public void createOffer(OnStreamCallbackListener onStreamCallbackListener,
                            OnSdpEventListener onSdpEventListener) {
        mOnStreamCallbackListener = onStreamCallbackListener;
        mOnSdpEventListener = onSdpEventListener;

        mPeerConn.addStream(mLocalMediaStream);
        mOnStreamCallbackListener.onLocalStream(mLocalMediaStream);

        // TODO Defaulted to receive and send audio, disabling will be controlled through audio/video track
        mPeerConn.createOffer(this, mRtcMedia.createOfferConstraints(true, true));
    }

    /**
     * Create answer then send the SDP.
     * @param onStreamCallbackListener The callback for stream when ready.
     * @param onSdpEventListener The callback for sdp offer/answer created.
     * @param sdpOffer The sdp offer received by the User from API Server.
     */
    public void createAnswer(OnStreamCallbackListener onStreamCallbackListener,
                             OnSdpEventListener onSdpEventListener,
                             String sdpOffer) {
        mOnStreamCallbackListener = onStreamCallbackListener;
        mOnSdpEventListener = onSdpEventListener;

        mPeerConn.addStream(mLocalMediaStream);
        mOnStreamCallbackListener.onLocalStream(mLocalMediaStream);

        SessionDescription.Type sdpType = SessionDescription.Type
                .fromCanonicalForm("offer");
        SessionDescription sdp = new SessionDescription(sdpType, sdpOffer);
        mPeerConn.setRemoteDescription(this, sdp);

        mPeerConn.createAnswer(this, mRtcMedia.createAnswerConstraints(true, true));
    }

    /**
     * Add sdp offer received from response of API Server.
     * @param sdpOffer The sdp answer to add to PeerConnection Remote.
     */
    public void addSdpOffer(String sdpOffer, OnStreamCallbackListener onStreamCallbackListener) {
        mOnStreamCallbackListener = onStreamCallbackListener;
        SessionDescription.Type sdpType = SessionDescription.Type
                .fromCanonicalForm("offer");
        SessionDescription sdp = new SessionDescription(sdpType, sdpOffer);
        mPeerConn.setRemoteDescription(this, sdp);
    }

    /**
     * Add sdp answer received from response of API Server.
     * @param sdpAnswer The sdp answer to add to PeerConnection Remote.
     */
    public void addSdpAnswer(String sdpAnswer, OnStreamCallbackListener onStreamCallbackListener) {
        mOnStreamCallbackListener = onStreamCallbackListener;
        SessionDescription.Type sdpType = SessionDescription.Type
                .fromCanonicalForm("answer");
        SessionDescription sdp = new SessionDescription(sdpType, sdpAnswer);
        Log.d(TAG, "addSdpAnswer SessionDescription: " + sdp.description);
        mPeerConn.setRemoteDescription(this, sdp);
    }

    /**
     * Destroy all RTC resources. This will close connection PeerConnection instance and dispose it.
     */
    public void destroyPlugin() {
        if (!isDestroyed) {
            isDestroyed = true;
            rtcDebugInfo = null;
            mPeerConn.close();
            mPeerConn.dispose();
        }
    }

    // ****** SdpObserver callbacks ******
    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        mPeerConn.setLocalDescription(this, sessionDescription);
    }

    @Override
    public void onSetSuccess() {
        Log.d(TAG, "onSetSuccess()");
    }

    @Override
    public void onCreateFailure(String s) {
        Log.e(TAG, "onCreateFailure() message: " + s);
    }

    @Override
    public void onSetFailure(String s) {
        Log.e(TAG, "onSetFailure() message: " + s);
    }


    // ****** PeerConnection.Observer callbacks ******
    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        Log.d(TAG, "onSignalingChange() called, signalingState: " + signalingState.name());
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        Log.d(TAG, "onIceConnectionChange() called, iceConnectionState: "
                + iceConnectionState.toString());
        if (iceConnectionState.compareTo(PeerConnection.IceConnectionState.DISCONNECTED) == 0 ||
                iceConnectionState.compareTo(PeerConnection.IceConnectionState.CLOSED) == 0 ||
                iceConnectionState.compareTo(PeerConnection.IceConnectionState.CHECKING) == 0) {
            mOnIceStateListener.onIceState(IcePeerState.DISONNECTED, iceConnectionState);
        } else if (iceConnectionState.compareTo(PeerConnection.IceConnectionState.FAILED) == 0) {
            mOnIceStateListener.onIceState(IcePeerState.FAILED, iceConnectionState);
        } else {
            mOnIceStateListener.onIceState(IcePeerState.CONNECTED, iceConnectionState);
        }
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        Log.i(TAG, "onIceGatheringChange() called, IceGatheringState: " + iceGatheringState.name());
        if (mPeerConn != null && mPeerConn.getLocalDescription() != null &&
                mPeerConn.getLocalDescription().description != null) {
            String offer = mPeerConn.getLocalDescription().description.toString();
            if (iceGatheringState != null &&
                    iceGatheringState.equals(PeerConnection.IceGatheringState.COMPLETE)
                    && StringUtils.isValid(offer)) {
                Log.i(TAG, "onIceGatheringChange sdp: " + offer);
                mOnSdpEventListener.onSuccess(offer);
            }
        }
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        Log.e(TAG, "onIceConnectionReceivingChange() called: " + b);
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        Log.d(TAG, "onIceCandidate() called " + iceCandidate.toString());
        IceCandidates iceCandidates = new IceCandidates();
        iceCandidates.setSdpMid(iceCandidate.sdpMid);
        iceCandidates.setSdpMLineIndex(String.valueOf(iceCandidate.sdpMLineIndex));
        iceCandidates.setCandidate(iceCandidate.toString());
        iceCandidateListener.onIceCandidateCreated(iceCandidates);
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        Log.d(TAG, "onAddStream() called");
        mOnStreamCallbackListener.onRemoteStream(mediaStream);
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        Log.d(TAG, "onRemoveStream() called");
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        Log.d(TAG, "onDataChannel() called: " + dataChannel.bufferedAmount());
    }

    @Override
    public void onRenegotiationNeeded() {
        Log.d(TAG, "onRenegotiationNeeded() called");
    }


    // Put values to of WebRTC StatsReport to RtcDebugInfo
    private void fillUpRtcDebugInfo(StatsReport report) {
        if (report == null)
            return;

        if (!StringUtils.isValid(report.id) ||
                (!report.id.contains("send") && !report.id.contains("recv")))
            return;

        boolean isTrackIdAudio = false;
        boolean isTrackIdVideo = false;
        Pattern pattern = Pattern.compile("\\[googTrackId:\\s(.*?)\\]");
        Matcher matcher = pattern.matcher(report.toString());
        String match = "";
        while(matcher.find()) {
            match = matcher.group(1);
        }
        isTrackIdAudio = match.contains("a0");
        isTrackIdVideo = match.contains("v0");

        for (StatsReport.Value value : report.values) {
            if (isTrackIdAudio) {
                // filters all audio stats report
                if (rtcDebugInfo.getAudStats() == null) {
                    rtcDebugInfo.setAudStats(new RtcDebugAudioStats());
                }
                rtcDebugInfo.getAudStats().putValues(value);
            }

            if (isTrackIdVideo) {
                // filters all video stats report
                if (rtcDebugInfo.getVidStats() == null) {
                    rtcDebugInfo.setVidStats(new RtcDebugVidStats());
                }
                rtcDebugInfo.getVidStats().putValues(value);
            }
        }
    }

}
