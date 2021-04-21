package com.stormstreaming.stormlibrary.model;

import android.net.Uri;

import com.google.android.exoplayer2.MediaItem;

public class StormMediaItem {

    private String host;
    private int port;
    private boolean isSSL;
    private String streamName;
    private String label;
    private boolean isSelected = false;

    public StormMediaItem(String host, int port, boolean isSSL, String streamName, String label){
        this.host = host;
        this.port = port;
        this.isSSL = isSSL;
        this.streamName = streamName;
        this.label = label;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isSSL() {
        return isSSL;
    }

    public void setSSL(boolean SSL) {
        isSSL = SSL;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public MediaItem getMediaItem(){
        return MediaItem.fromUri(Uri.parse((isSSL() ? "wss" : "ws") + "://" + getHost() + ":" + getPort() + "/h5live/stream/?url=rtmp%3A%2F%2Fstormdev.web-anatomy.com%3A1935%2Flive&stream=" + getStreamName() + "&"));
    }
}
