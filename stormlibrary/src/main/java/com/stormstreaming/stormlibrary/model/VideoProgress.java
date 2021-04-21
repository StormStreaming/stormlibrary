package com.stormstreaming.stormlibrary.model;

public class VideoProgress {

    private long streamDuration;  // how long does our stream works
    private long sourceDuration;  // how long is the source broadcasting
    private long sourceStartTime; // when the source was started
    private long streamStartTime; // when our stream was started
    private long dvrCacheSize;    // how many ms does the DVR Cache hold at this point

    public VideoProgress(){

    }

    public long getStreamDuration() {
        return streamDuration;
    }

    public void setStreamDuration(long streamDuration) {
        this.streamDuration = streamDuration;
    }

    public long getSourceDuration() {
        return sourceDuration;
    }

    public void setSourceDuration(long sourceDuration) {
        this.sourceDuration = sourceDuration;
    }

    public long getSourceStartTime() {
        return sourceStartTime;
    }

    public void setSourceStartTime(long sourceStartTime) {
        this.sourceStartTime = sourceStartTime;
    }

    public long getStreamStartTime() {
        return streamStartTime;
    }

    public void setStreamStartTime(long streamStartTime) {
        this.streamStartTime = streamStartTime;
    }

    public long getDvrCacheSize() {
        return dvrCacheSize;
    }

    public void setDvrCacheSize(long dvrCacheSize) {
        this.dvrCacheSize = dvrCacheSize;
    }

    public String toString(){
        return "streamDuration: "+getStreamDuration()+"\r\n" +
                "sourceDuration: "+getSourceDuration()+"\r\n" +
                "sourceStartTime: "+getSourceStartTime()+"\r\n" +
                "streamStartTime: "+getSourceStartTime()+"\r\n" +
                "dvrCacheSize: "+getDvrCacheSize();
    }
}
