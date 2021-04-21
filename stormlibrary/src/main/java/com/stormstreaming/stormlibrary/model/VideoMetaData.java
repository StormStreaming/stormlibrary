package com.stormstreaming.stormlibrary.model;

public class VideoMetaData {

    private int videoWidth;
    private int videoHeight;
    private int videoTimeScale;
    private String encoder;
    private String videoCodec;
    private String audioCodec;
    private int audioChannels;
    private int audioDataRate;
    private int audioSampleRate;
    private int nominalFPS;

    public VideoMetaData(){

    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public void setVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;
    }

    public int getVideoTimeScale() {
        return videoTimeScale;
    }

    public void setVideoTimeScale(int videoTimeScale) {
        this.videoTimeScale = videoTimeScale;
    }

    public String getEncoder() {
        return encoder;
    }

    public void setEncoder(String encoder) {
        this.encoder = encoder;
    }

    public String getVideoCodec() {
        return videoCodec;
    }

    public void setVideoCodec(String videoCodec) {
        this.videoCodec = videoCodec;
    }

    public String getAudioCodec() {
        return audioCodec;
    }

    public void setAudioCodec(String audioCodec) {
        this.audioCodec = audioCodec;
    }

    public int getAudioChannels() {
        return audioChannels;
    }

    public void setAudioChannels(int audioChannels) {
        this.audioChannels = audioChannels;
    }

    public int getAudioDataRate() {
        return audioDataRate;
    }

    public void setAudioDataRate(int audioDataRate) {
        this.audioDataRate = audioDataRate;
    }

    public int getAudioSampleRate() {
        return audioSampleRate;
    }

    public void setAudioSampleRate(int audioSampleRate) {
        this.audioSampleRate = audioSampleRate;
    }

    public int getNominalFPS() {
        return nominalFPS;
    }

    public void setNominalFPS(int nominalFPS) {
        this.nominalFPS = nominalFPS;
    }

    public String toString(){
        return "videoWidth: "+getVideoWidth()+"\r\n" +
                "videoHeight: "+getVideoHeight()+"\r\n" +
                "videoTimeScale: "+getVideoTimeScale()+"\r\n" +
                "encoder: "+getEncoder()+"\r\n" +
                "videoCodec: "+getVideoCodec()+"\r\n" +
                "audioCodec: "+getAudioCodec()+"\r\n" +
                "audioChannels: "+getAudioChannels()+"\r\n" +
                "audioDataRate: "+getAudioDataRate()+"\r\n" +
                "audioSampleRate: "+getAudioSampleRate()+"\r\n" +
                "nominalFPS: "+getNominalFPS();
    }
}
