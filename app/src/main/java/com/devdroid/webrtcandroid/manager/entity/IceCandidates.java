package com.devdroid.webrtcandroid.manager.entity;

public class IceCandidates {

    private String sdpMid;
    private String sdpMLineIndex;
    private String candidate;

    public String getCandidate() {
        return candidate;
    }

    public void setCandidate(String candidate) {
        this.candidate = candidate;
    }

    public String getSdpMid() {
        return sdpMid;
    }

    public void setSdpMid(String sdpMid) {
        this.sdpMid = sdpMid;
    }

    public String getSdpMLineIndex() {
        return sdpMLineIndex;
    }

    public void setSdpMLineIndex(String sdpMLineIndex) {
        this.sdpMLineIndex = sdpMLineIndex;
    }
}
