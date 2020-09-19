package com.devdroid.webrtcandroid;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.devdroid.webrtcandroid.bus.EventBusUtil;
import com.devdroid.webrtcandroid.manager.RtcManager;
import com.devdroid.webrtcandroid.manager.entity.IceCandidates;
import com.devdroid.webrtcandroid.manager.entity.RtcDebugInfo;
import com.devdroid.webrtcandroid.manager.plugin.RtcPlugin;

import org.webrtc.AudioTrack;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RendererCommon;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoTrack;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private FrameLayout mFrameVideo;

    private final String TURN_SERVER = "stun.l.google.com:19302";
    private final String TURN_USERNAME = "";
    private final String TURN_PASSWORD = "";

    private RtcManager mRtcManager = new RtcManager();
    private RtcPlugin mRtcPlugin;

    private ArrayList<IceCandidates> mArrIceCandidates = new ArrayList<>();
    private String mSdpOffer;
    private String mSdpAnswer;

    private IceCandidateListener mIceCandidateListener = new IceCandidateListener();;
    private StreamListener mStreamListener = new StreamListener();
    private AudioStateListener mAudioListener = new AudioStateListener();
    private WebRTCAudioManager mAudioManager;

    private VideoRenderer mLocalVideoRenderer, mRemoteVideoRenderer;
    private RtcManager.OnCameraSwitch onCameraSwitch = new OnCameraSwitch();
    private VideoTrack mLocalVidTrack, mRemoteVidTrack;
    private AudioTrack mLocalAudioTrack, mRemoteAudioTrack;
    private MediaStream mLocalStream, mRemoteStream;

    private RtcPlugin.OnIceStateListener mOnIceStateListener;
    private RtcPlugin.IcePeerState mCurrPeerState = RtcPlugin.IcePeerState.DISONNECTED;
    private PeerConnection.IceConnectionState mIceConnState = PeerConnection.IceConnectionState.NEW;

    private Handler mHandler;
    private Runnable mRunnableLogStats;

    private boolean isBackCamera = true;
    private boolean isAudioInputMuted = false;
    private boolean isAudioOutputMuted = false;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFrameVideo = (FrameLayout) findViewById(R.id.frame_vid_container);

        mHandler = new Handler();

        EventBusUtil.getInstance().register(MainActivity.this);
    }

    @Override
    public void onPause() {
        setAudioOutputMuted(true);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        setAudioOutputMuted(isAudioOutputMuted);
        setViewSpeakerDisplayMuted(isAudioOutputMuted);
        setAudioInputMuted(isAudioInputMuted);
        setViewMicDisplayMuted(isAudioInputMuted);
    }

    @Override
    protected void onDestroy() {
        EventBusUtil.getInstance().register(MainActivity.this);

        if (mHandler != null && mRunnableLogStats != null) {
            mHandler.removeCallbacks(mRunnableLogStats);
        }

        if (mRtcManager != null) {
            mRtcManager.release();
        }

        if (mAudioManager != null) {
            mAudioManager.muteAudioStream(false);
            mAudioManager.setMicrophoneMute(false);
            mAudioManager.close();
            mAudioManager = null;
        }

        super.onDestroy();
    }

    /**
     * Muted mic/earpiece and set the display button of mic overflow menu
     * if user enable/disable audio.
     */
    private void setAudioInputMuted(boolean isAudioMuted) {
        if (mAudioManager != null) {
            mAudioManager.setMicrophoneMute(isAudioMuted);
        }
    }

    /** Mute remote peer audio */
    private void setAudioOutputMuted(boolean isAudioOutputMuted) {
        if (mRemoteAudioTrack != null) {
            mRemoteAudioTrack.setEnabled(!isAudioOutputMuted);
        }
    }

    /** Set the display button of mic if user enable/disable mic. */
    private void setViewMicDisplayMuted(boolean isAudioMuted) {
        // Update views if mic icon for audio input is enabled or disabled
    }

    /** Set the display button of speaker overflow menu if user enable/disable speaker. */
    private void setViewSpeakerDisplayMuted(boolean isAudioOutputMuted) {
        // Update views if speaker icon for audio output is enabled or disabled
    }

    private void setViewCameraDisplay() {
        if (mRtcManager != null) {
            isBackCamera = !isBackCamera;
            mRtcManager.switchCamera(onCameraSwitch);
            if (mLocalVidTrack != null) {
                mLocalVidTrack.removeRenderer(mLocalVideoRenderer);
                boolean isMirror = !isBackCamera;
                createLocalVideoRenderer(isMirror);
                mLocalVidTrack.addRenderer(mLocalVideoRenderer);
            }
        }
    }

    // Create video renderer for streams being broadcasted by user to Peer connection
    private void createLocalVideoRenderer(boolean isMirror) {
        Log.d(TAG, "createLocalVideoRenderer called, mirror: " + isMirror);
        if (mLocalVideoRenderer == null) {
            GLSurfaceView glSurfaceView = (GLSurfaceView) findViewById(R.id.glVideoStreamLocal);

            VideoRendererGui.setView(glSurfaceView, new Runnable() {
                @Override
                public void run() {
                    // Fix for video distortion as mentioned in
                    // https://code.google.com/p/webrtc/issues/detail?id=4482
                    GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
                }
            });
        }

        VideoRenderer.Callbacks r = VideoRendererGui.create(0, 0, 100, 100,
                RendererCommon.ScalingType.SCALE_ASPECT_FILL, isMirror);
        mLocalVideoRenderer = new VideoRenderer(r);
    }

    // Create video renderer for streams being received by user Listener from Peer connection
    private void createRemoteVideoRenderer(boolean isMirror) {
        Log.d(TAG, "createRemoteVideoRenderer called");
        if (mRemoteVideoRenderer == null) {
            GLSurfaceView glSurfaceView = (GLSurfaceView) findViewById(R.id.glVideoStreamRemote);

            VideoRendererGui.setView(glSurfaceView, new Runnable() {
                @Override
                public void run() {
                    // Fix for video distortion as mentioned in
                    // https://code.google.com/p/webrtc/issues/detail?id=4482
                    GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
                }
            });
        }

        VideoRenderer.Callbacks r = VideoRendererGui.create(0, 0, 100, 100,
                RendererCommon.ScalingType.SCALE_ASPECT_FILL, isMirror);
        mRemoteVideoRenderer = new VideoRenderer(r);
    }

    // Log StatsReport every 500ms
    private void startGetStatsReport(boolean startGettingReport) {
        if (mRunnableLogStats != null) {
            mHandler.removeCallbacks(mRunnableLogStats);
        }
        if (startGettingReport) {
            mRunnableLogStats = new Runnable() {
                @Override
                public void run() {
                    if (mRtcPlugin != null) {
                        startDebugRtcLGetStats(false);
                        EventBusUtil.getInstance().post(new EventBusUtil
                                .OnRtcStatsReportEvaluated(mRtcPlugin.isReceivingStream(),
                                mRtcPlugin.isSendingStream()));
                        if (isPublisher) {
                            mRtcPlugin.getStatsReport(true, false, mLocalAudioTrack);
                            mRtcPlugin.getStatsReport(true, true, mLocalVidTrack);
                        } else {
                            mRtcPlugin.getStatsReport(false, false, mRemoteAudioTrack);
                            mRtcPlugin.getStatsReport(false, true, mRemoteVidTrack);
                        }
                    }
                    mHandler.postDelayed(this, 1000);
                }
            };
            mHandler.postDelayed(mRunnableLogStats, 1000);
        }

        if (!startGettingReport)
            startDebugRtcLGetStats(true);
    }

    // Get WebRTC Stats Report which will be shown
    private void startDebugRtcLGetStats(boolean isRtcRelease) {
        String info = "";
        if (!isRtcRelease) {
            if (mRtcPlugin != null) {
                RtcDebugInfo rtcDebugInfo = mRtcPlugin.getRtcDebugInfo();
                rtcDebugInfo.setIceConnectionState(mIceConnState);
                info = rtcDebugInfo.getLogStats(rtcDebugInfo);
            }
        } else {
            RtcDebugInfo rtcDebugInfo = new RtcDebugInfo();
            rtcDebugInfo.setIceConnectionState(mIceConnState);
            info = rtcDebugInfo.getLogStats(rtcDebugInfo);
        }
        final String finalInfo = info;
        Log.d(TAG, "startDebugRtcLGetStats: " + finalInfo);
    }

    // Callback when SDP offer has been created to avoid passing empty/null value sdp offer
    // when calling API of initialize_stream and to avoid displaying "PLAY" button without
    // SDP created yet.
    private interface OnSdpOfferCreated {
        /** Event callback when IceGathering state COMPLETED and has created SDP offer. */
        void onSdpOfferCreated();
    }

    private class IceCandidateListener implements RtcPlugin.OnIceCandidateListener {
        @Override
        public void onIceCandidateCreated(IceCandidates iceCandidate) {
            mArrIceCandidates.add(iceCandidate);
        }
    }

    private class StreamListener implements RtcPlugin.OnStreamCallbackListener {

        public StreamListener() {
            Log.d(TAG, "initialized StreamListener");
        }

        @Override
        public void onLocalStream(final MediaStream localStream) {
            mLocalStream = localStream;
            if (localStream.audioTracks != null && localStream.audioTracks.size() >0
                    && localStream.audioTracks.get(0) != null) {
                mLocalAudioTrack = localStream.audioTracks.get(0);
            }
            if (localStream.videoTracks != null && localStream.videoTracks.size() >0
                    && localStream.videoTracks.get(0) != null) {
                Log.d(TAG, "StreamListener onLocalStream called");
                mLocalVidTrack = localStream.videoTracks.get(0);
                mLocalVidTrack.addRenderer(mLocalVideoRenderer);
            }

            startGetStatsReport(true);
        }

        @Override
        public void onRemoteStream(MediaStream remoteStream) {
            Log.d(TAG, "StreamListener onRemoteStream remoteStream: " + remoteStream.toString());
            mRemoteStream = remoteStream;
            if (remoteStream.audioTracks != null && remoteStream.audioTracks.size() >0
                    && remoteStream.audioTracks.get(0) != null) {
                mRemoteAudioTrack = remoteStream.audioTracks.get(0);
                mRemoteAudioTrack.setEnabled(!isAudioOutputMuted);
                Log.d(TAG, "StreamListener onRemoteStream audioTracks called");
                Log.d(TAG, "StreamListener onRemoteStream audioTracks state: "
                        + remoteStream.audioTracks.get(0).state().name());
            }
            if (remoteStream.videoTracks != null && remoteStream.videoTracks.size() > 0
                    && remoteStream.videoTracks.get(0) != null) {
                mRemoteVidTrack = remoteStream.videoTracks.get(0);
                remoteStream.videoTracks.get(0).addRenderer(mRemoteVideoRenderer);
                Log.d(TAG, "StreamListener onRemoteStream videoTrack state: "
                        + remoteStream.videoTracks.get(0).state().name());
                Log.d(TAG, "StreamListener onRemoteStream videoTracks called");
            }

            startGetStatsReport(true);
        }
    }

    private class AudioStateListener implements WebRTCAudioManager.OnAudioStateChangeListener {
        @Override
        public void onAudioStateChange(WebRTCAudioManager.AudioState audioState) {
            if (audioState != null)
                Log.d(TAG, "onAudioStateChange: " + audioState.name());
        }
    }

    private class OnCameraSwitch implements RtcManager.OnCameraSwitch {
        @Override
        public void onSwitchCameraDone(boolean isSuccess) {
            Log.d(TAG, "onSwitchCameraDone " + isSuccess);
        }
    }

}
