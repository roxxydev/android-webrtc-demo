package com.devdroid.webrtcandroid.manager;

import android.app.Application;
import android.hardware.Camera;
import android.util.Log;

import com.devdroid.webrtcandroid.manager.entity.IceServerDetail;
import com.devdroid.webrtcandroid.manager.media.RtcMedia;
import com.devdroid.webrtcandroid.manager.plugin.RtcPlugin;
import com.devdroid.webrtcandroid.util.StringUtils;

import org.json.JSONException;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide methods for setting up Peer connection.
 */
public class RtcManager {

    private static final String TAG = RtcManager.class.getSimpleName();

    private static final String VIDEO_TRACK_ID = "devdroidv0";
    private static final String AUDIO_TRACK_ID = "devdroida0";

    private List<PeerConnection.IceServer> mArrIceServer = new ArrayList<>();
    private List<RtcPlugin> mArrRtcPlugin = new ArrayList<>();;

    private RtcMedia rtcMedia = new RtcMedia();

    private RtcPlugin.OnIceCandidateListener iceCandidateListener;

    private PeerConnectionFactory mPeerFactory;

    private VideoCapturerAndroid mVideoCapturerAndroid;
    private VideoCapturer mVidCapturer;
    private AudioTrack mAudioTrack;
    private VideoTrack mVideoTrack;
    private AudioSource mSrcAudio;
    private VideoSource mSrcVideo;

    /** Callback when switching camera is done*/
    public interface OnCameraSwitch {
        void onSwitchCameraDone(boolean isSuccess);
    }

    /**
     * Initialize libjingle Rtc on Application context
     * create which will load its native libraries.
     * @param app The Application context.
     */
    public static void initOnApplicationContext(Application app) {
        PeerConnectionFactory.initializeAndroidGlobals(app,
                true, true, true);
    }

    /**
     * Initialize RtcManager and the IceServer to which it will connect to.
     * @param arrIceServerDetail List of ICE server details to use in initializing peer connection.
     * @param iceCandidateListener The RtcPlugin.OnIceCandidateListener for storing ice candidates.
     */
    public void init(ArrayList<IceServerDetail> arrIceServerDetail,
                     RtcPlugin.OnIceCandidateListener iceCandidateListener) {
        Log.d(TAG, "init called");

        for (IceServerDetail iceServerDetail : arrIceServerDetail) {
            String uri = iceServerDetail.getUrl();
            String username = iceServerDetail.getUsername();
            String password = iceServerDetail.getPassword();
            if (StringUtils.isValid(username)) {
                PeerConnection.IceServer iceServer = new PeerConnection.IceServer(uri, username, password);
                mArrIceServer.add(iceServer);
                Log.d(TAG, "init added ICE server: " + uri + " " + username + " " + password);
            } else {
                PeerConnection.IceServer iceServer = new PeerConnection.IceServer(uri);
                mArrIceServer.add(iceServer);
                Log.d(TAG, "init added ICE server: " + uri);
            }
        }

        mPeerFactory = new PeerConnectionFactory();
        this.iceCandidateListener = iceCandidateListener;
    }

    /**
     * Initialize Audio and Video track for MediaStream to be used by PeerConnection. This is
     * important to avoid reinitialization of VideoCapturer which will throw error "Capturer can
     * only be taken once". This should be called once before creating any RtcPlugin.
     */
    public void initAudioVideoSource(boolean isCamBack) {
        Log.d(TAG, "initAudioVideoSource back camera used: " + isCamBack);
        mSrcAudio = mPeerFactory.createAudioSource(
                rtcMedia.createAudioConstraints());
        if (mSrcAudio != null) {
            mAudioTrack = mPeerFactory.createAudioTrack(AUDIO_TRACK_ID, mSrcAudio);
            mAudioTrack.setEnabled(true);
        }

        MediaConstraints vidConstraints = rtcMedia.createSuggestedVideoConstraints(
                CameraEnumerationAndroid.getSupportedFormats(getCameraId(isCamBack)));
//        MediaConstraints vidConstraints = rtcMedia.createStandardVideoConstraints();
//        MediaConstraints vidConstraints = rtcMedia.createBestVideoConstraints(
//                CameraEnumerationAndroid.getSupportedFormats(getCameraId(isCamBack)));

        try {
            Log.d(TAG, "initAudioVideoSource camera supported formats for BACK: " +
                    CameraEnumerationAndroid.getSupportedFormatsAsJson(getCameraId(true)));
            Log.d(TAG, "initAudioVideoSource camera supported formats for FRONT: " +
                    CameraEnumerationAndroid.getSupportedFormatsAsJson(getCameraId(false)));
        } catch (JSONException e) {
            Log.e(TAG, "initAudioVideoSource ERROR " + e.toString());
        }

        mVidCapturer = getVideoCapturer(isCamBack);
        if (mVidCapturer != null) {
            mSrcVideo = mPeerFactory.createVideoSource(
                    mVidCapturer, vidConstraints);
            String trackExtension = isCamBack ? "backFacing" : "frontFacing";
            String videoTrackId = VIDEO_TRACK_ID + trackExtension;
            mVideoTrack = mPeerFactory.createVideoTrack(videoTrackId, mSrcVideo);
            mVideoTrack.setEnabled(true);
        }
    }

    /** Release all RTC resources. Close all PeerConnection. */
    public void release() {
        Log.d(TAG, "release called");
        if (mArrIceServer != null) {
            mArrIceServer.clear();
        }

        if (mArrRtcPlugin != null && !mArrRtcPlugin.isEmpty()) {
            for (RtcPlugin rtcPlugin : mArrRtcPlugin) {
                if (rtcPlugin != null) {
                    rtcPlugin.destroyPlugin();
                }
            }
            mArrRtcPlugin.clear();
        }

        if (mSrcVideo != null) {
            mSrcVideo.stop();
        }
        if (mVideoCapturerAndroid != null) {
            mVideoCapturerAndroid.dispose();
        }
        if (mVidCapturer != null) {
            mVidCapturer.dispose();
        }

        if (mPeerFactory != null) {
            mPeerFactory.dispose();
        }
    }

    /**
     * Create RtcPlugin and prepare to use for a full P2P connection for offer and answer to create.
     * This RtcPlugin instance will represent one call. Before calling this, be sure that
     * VideoRenderer, StreamListener, are initialized in order.
     */
    public RtcPlugin createRtcPlugin(boolean isRcvOnly) {
        Log.d(TAG, "createPlugin called");
        RtcPlugin rtcPlugin = new RtcPlugin();

        MediaStream mLocalMediaStream = mPeerFactory.createLocalMediaStream("devdroid");
        if (!isRcvOnly) {
            // only add localmediastream audio track and video track is publisher
            if (mAudioTrack != null) {
                mLocalMediaStream.addTrack(mAudioTrack);
            }
            if (mVideoTrack != null) {
                mLocalMediaStream.addTrack(mVideoTrack);
            }
        }

        rtcPlugin.setUp(mArrIceServer, iceCandidateListener, mPeerFactory, mLocalMediaStream, rtcMedia);

        mArrRtcPlugin.add(rtcPlugin);

        return rtcPlugin;
    }

    /** Get the RtcPlugin associated with id. */
    public RtcPlugin getRtcPlugin(String id) {
        for (RtcPlugin rtcPlugin: mArrRtcPlugin) {
            if (rtcPlugin.getId().equals(id)) {
                return rtcPlugin;
            }
        }
        return null;
    }

    /** Switch camera to use either front or back. */
    public void switchCamera(final OnCameraSwitch onCameraSwitch) {
        Log.d(TAG, "switchCamera called");
        if (mVideoCapturerAndroid != null) {
            mVideoCapturerAndroid.switchCamera(new VideoCapturerAndroid.CameraSwitchHandler() {
                @Override
                public void onCameraSwitchDone(boolean b) {
                    Log.d(TAG, "switchCamera onCameraSwitchDone: " + b);
                    onCameraSwitch.onSwitchCameraDone(b);
                }

                @Override
                public void onCameraSwitchError(String s) {
                    Log.d(TAG, "switchCamera onCameraSwitchError: " + s);
                    onCameraSwitch.onSwitchCameraDone(false);
                }
            });
        }
    }

    /** Pause/Play Video stream being sent. */
    public void pauseSendingVidStream(boolean isPauseVideoStream) {
        if (mVideoTrack != null) {
            mVideoTrack.setEnabled(isPauseVideoStream);
        }
    }

    /** Pause/Play Audio stream being sent. */
    public void pauseSendingAudStream(boolean isPauseAudioStream) {
        if (mAudioTrack != null) {
            mAudioTrack.setEnabled(isPauseAudioStream);
        }
    }

    /** Pause/Play sending audio and video stream. */
    public void pauseSendingStream(boolean isPauseStream) {
        Log.d(TAG, "pauseSendingStream called: " + isPauseStream);
        pauseSendingVidStream(isPauseStream);
        pauseSendingAudStream(isPauseStream);
    }

    private VideoCapturer getVideoCapturer(boolean isCamBack) {
        Log.d(TAG, "getVideoCaptuere back camera used: " + isCamBack);
        String cameraDeviceName = CameraEnumerationAndroid.getNameOfBackFacingDevice();
        if (!isCamBack) {
            cameraDeviceName = CameraEnumerationAndroid.getNameOfFrontFacingDevice();
        }

        if (StringUtils.isValid(cameraDeviceName)) {
            mVideoCapturerAndroid =
                    (VideoCapturerAndroid) VideoCapturerAndroid.create(cameraDeviceName);
            mVideoCapturerAndroid.changeCaptureFormat(RtcMedia.sLeastAllowedWidth,
                    RtcMedia.sLeastAllowedHeight, RtcMedia.sLeasetAllowedFramerate);
            mVideoCapturerAndroid.onOutputFormatRequest(RtcMedia.sLeastAllowedWidth,
                    RtcMedia.sLeastAllowedHeight, RtcMedia.sLeasetAllowedFramerate);
            return mVideoCapturerAndroid;

        } else {
            cameraDeviceName = CameraEnumerationAndroid.getNameOfBackFacingDevice();
            if (StringUtils.isValid(cameraDeviceName)) {
                mVideoCapturerAndroid =
                        (VideoCapturerAndroid) VideoCapturerAndroid.create(cameraDeviceName);
                mVideoCapturerAndroid.changeCaptureFormat(RtcMedia.sLeastAllowedWidth,
                        RtcMedia.sLeastAllowedHeight, RtcMedia.sLeasetAllowedFramerate);
                mVideoCapturerAndroid.onOutputFormatRequest(RtcMedia.sLeastAllowedWidth,
                        RtcMedia.sLeastAllowedHeight, RtcMedia.sLeasetAllowedFramerate);
                return mVideoCapturerAndroid;
            }
        }

        return mVideoCapturerAndroid;
    }

    private int getCameraId(boolean isCamBack) {
        for(int i = 0; i < Camera.getNumberOfCameras(); ++i) {
            Camera.CameraInfo info = new Camera.CameraInfo();

            try {
                Camera.getCameraInfo(i, info);
                if (isCamBack && info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    Log.d(TAG, "getCameraId used back camera");
                    return i;
                } else if (!isCamBack && info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    Log.d(TAG, "getCameraId used front camera");
                    return i;
                }
            } catch (Exception var3) {
                Log.e(TAG, "getCameraId() failed on index " + i, var3);
            }
        }

        return 0;
    }

}
