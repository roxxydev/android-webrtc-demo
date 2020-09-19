package com.devdroid.webrtcandroid.manager.media;

import android.util.Log;

import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.MediaConstraints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Contains the media constraints for ice server, audio, and video for
 * peer settings to use in RtcPlugin.
 */
public class RtcMedia {

    private static final String TAG = RtcMedia.class.getSimpleName();

    public static int sLeastAllowedWidth = 960;
    public static int sLeastAllowedHeight = 720;
    public static int sLeasetAllowedFramerate = 20;

    public MediaConstraints createIceConstraints() {
        Log.d(TAG, "creating Ice constraints..");

        MediaConstraints iceConstraints = new MediaConstraints();
        iceConstraints.mandatory.add(new MediaConstraints.KeyValuePair("IceTransports", "all"));
        iceConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googHighBitrate", "false"));
        iceConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googVeryHighBitrate", "false"));
        iceConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googImprovedWifiBwe", "true"));
        iceConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googCpuOveruseDetection", "true"));
        iceConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googCpuOveruseEncodeUsage", "true"));
        iceConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googCpuOveruseThreshold", "85"));
        iceConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googCpuUnderuseThreshold", "55"));
        iceConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googScreencastMinBitrate", "400"));
        iceConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googPayloadPadding", "true"));
        iceConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googSkipEncodingUnusedStreams", "true"));
        iceConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));

        return iceConstraints;
    }

    /**
     * Create MediaConstraints for Offer.
     * @param isReceiveAudio wish to receive audio from remote Peer.
     * @param isReceiveVideo wish to receive video from remote Peer.
     * @return MediaConstraints for Answer.
     */
    public MediaConstraints createOfferConstraints(boolean isReceiveAudio, boolean isReceiveVideo) {
        Log.d(TAG, "creating Offer constraints..");

        MediaConstraints offerConstraints = new MediaConstraints();
        /*offerConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio",
                isReceiveAudio ? "true" : "false"));
        offerConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo",
                isReceiveVideo ? "true" : "false"));*/
        offerConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        offerConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

        return offerConstraints;
    }

    /**
     * Create MediaConstraints for Answer.
     * @param isReceiveAudio wish to receive audio from remote Peer.
     * @param isReceiveVideo wish to receive video from remote Peer.
     * @return MediaConstraints for Answer.
     */
    public MediaConstraints createAnswerConstraints(boolean isReceiveAudio, boolean isReceiveVideo) {
        Log.d(TAG, "creating Answer constraints..");

        MediaConstraints offerConstraints = new MediaConstraints();
        /*offerConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio",
                isReceiveAudio ? "true" : "false"));
        offerConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo",
                isReceiveVideo ? "true" : "false"));*/
        offerConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        offerConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

        return offerConstraints;
    }

    public MediaConstraints createAudioConstraints() {
        Log.d(TAG, "creating Audio constraints..");

        MediaConstraints audioConstraints = new MediaConstraints();
        audioConstraints.optional.add(new MediaConstraints.KeyValuePair("googDucking", "false"));
        audioConstraints.optional.add(new MediaConstraints.KeyValuePair("googHighpassFilter", "true"));
        audioConstraints.optional.add(new MediaConstraints.KeyValuePair("googAudioMirroring", "true"));
        audioConstraints.optional.add(new MediaConstraints.KeyValuePair("chromeRenderToAssociatedSink", "true"));
        audioConstraints.optional.add(new MediaConstraints.KeyValuePair("googNoiseSuppression", "true"));
        audioConstraints.optional.add(new MediaConstraints.KeyValuePair("googNoiseSuppression2", "true"));
        audioConstraints.optional.add(new MediaConstraints.KeyValuePair("googEchoCancellation", "true"));
        audioConstraints.optional.add(new MediaConstraints.KeyValuePair("googEchoCancellation2", "true"));
        audioConstraints.optional.add(new MediaConstraints.KeyValuePair("googAutoGainControl", "true"));
        audioConstraints.optional.add(new MediaConstraints.KeyValuePair("googAutoGainControl2", "true"));

        return audioConstraints;
    }

    public MediaConstraints createStandardVideoConstraints() {
        Log.d(TAG, "creating standard Video constraints..");

        String width = String.valueOf(sLeastAllowedHeight);
        String height = String.valueOf(sLeastAllowedHeight);
        String frameRate = String.valueOf(sLeasetAllowedFramerate);

        MediaConstraints videoConstraints = new MediaConstraints();
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", width));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", height));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate", frameRate));
        videoConstraints.optional.add(new MediaConstraints.KeyValuePair("googLeakyBucket", "true"));
        videoConstraints.optional.add(new MediaConstraints.KeyValuePair("googNoiseReduction", "true"));

        Log.d(TAG, "createVideoConstraints maxWidth: " + width + " maxHeight: " + height
                + " maxFrameRate: " + frameRate);

        return videoConstraints;
    }

    public MediaConstraints createBestVideoConstraints(
            List<CameraEnumerationAndroid.CaptureFormat> formats) {
        Log.d(TAG, "creating best Video constraints..");

        if (formats != null) {
            ArrayList<CameraDimension> arrCamDimens = new ArrayList<>();
            for (CameraEnumerationAndroid.CaptureFormat format : formats) {
                CameraDimension camDimen = new CameraDimension();
                camDimen.width = format.width;
                camDimen.height = format.height;
                camDimen.framerate = 20;
                arrCamDimens.add(camDimen);
            }
            Collections.sort(arrCamDimens, new CameraDimension.CamDimenComparator());

            // get the highest the resolution supported by device camera
            CameraDimension camDimen = arrCamDimens.get(arrCamDimens.size() -1);
            sLeastAllowedWidth = camDimen.width;
            sLeastAllowedHeight = camDimen.height;
            int frameRate = 20;

            Log.d(TAG, "createSuggestedVideoConstraints maxWidth: " + sLeastAllowedWidth
                    + " maxHeight: " + sLeastAllowedHeight
                    + " maxFrameRate: " + sLeasetAllowedFramerate);

            MediaConstraints videoConstraints = new MediaConstraints();
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth",
                    String.valueOf(sLeastAllowedWidth)));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight",
                    String.valueOf(sLeastAllowedHeight)));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate",
                    String.valueOf(sLeasetAllowedFramerate)));
            videoConstraints.optional.add(new MediaConstraints.KeyValuePair("googLeakyBucket", "true"));
            videoConstraints.optional.add(new MediaConstraints.KeyValuePair("googNoiseReduction", "true"));

            return videoConstraints;
        } else {
            Log.e(TAG, "Capture format null! defaulting to standard Video constraints..");
            return createStandardVideoConstraints();
        }
    }

    public MediaConstraints createSuggestedVideoConstraints(
            List<CameraEnumerationAndroid.CaptureFormat> formats) {
        Log.d(TAG, "creating suggested Video constraints..");

        int consWidth = sLeastAllowedWidth;
        int consHeight = sLeastAllowedHeight;
        int consFrameRate = sLeasetAllowedFramerate;

        if (formats != null) {
            ArrayList<CameraDimension> arrCamDimens = new ArrayList<>();
            for (CameraEnumerationAndroid.CaptureFormat format : formats) {
                CameraDimension camDimen = new CameraDimension();
                camDimen.width = format.width;
                camDimen.height = format.height;
                camDimen.framerate = 20;
                arrCamDimens.add(camDimen);
            }
            Collections.sort(arrCamDimens, new CameraDimension.CamDimenComparator());

            if (formats.size() > 0) {
                CameraDimension cameraDimen = arrCamDimens.get(0);
                int leastWidth = cameraDimen.width;
                int leastHeight = cameraDimen.height;
                int leastFrameRate = cameraDimen.framerate;

                if (sLeastAllowedWidth >= leastWidth && sLeastAllowedHeight >= leastHeight
                        && sLeasetAllowedFramerate >= leastFrameRate) {
                    consWidth = sLeastAllowedWidth;
                    consHeight = sLeastAllowedHeight;
                    consFrameRate = sLeasetAllowedFramerate;
                } else {
                    consWidth = leastWidth;
                    consHeight = leastHeight;
                    consFrameRate = leastFrameRate;
                }
            }

            Log.d(TAG, "createSuggestedVideoConstraints maxWidth: " + consWidth + " maxHeight: " + consHeight
                    + " maxFrameRate: " + consFrameRate);

            MediaConstraints videoConstraints = new MediaConstraints();
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth",
                    String.valueOf(consWidth)));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight",
                    String.valueOf(consHeight)));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate",
                    String.valueOf(consFrameRate)));
            videoConstraints.optional.add(new MediaConstraints.KeyValuePair("googLeakyBucket", "true"));
            videoConstraints.optional.add(new MediaConstraints.KeyValuePair("googNoiseReduction", "true"));

            return videoConstraints;
        } else {
            Log.e(TAG, "Capture format null! defaulting to standard Video constraints..");
            return createStandardVideoConstraints();
        }
    }

    private static class CameraDimension {
        int width;
        int height;
        int framerate;

        /** Arrange from lowest to highest (0 to n)*/
        public static class CamDimenComparator implements Comparator<CameraDimension> {
            @Override
            public int compare(CameraDimension lhs, CameraDimension rhs) {
                if (lhs != null && rhs != null) {
                    if (lhs.width < rhs.width) {
                        return -1;
                    } else if (lhs.width > rhs.width) {
                        return 1;
                    }
                }
                return 0;
            }
        }
    }

}
