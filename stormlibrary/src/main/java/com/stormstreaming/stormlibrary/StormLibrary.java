package com.stormstreaming.stormlibrary;

import android.content.Context;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.stormstreaming.stormlibrary.events.Listeners;
import com.stormstreaming.stormlibrary.exception.EmptyMediaItemsListException;
import com.stormstreaming.stormlibrary.exoplayer.ExoPlayerImpl;
import com.stormstreaming.stormlibrary.model.StormMediaItem;
import com.stormstreaming.stormlibrary.model.VideoMetaData;
import com.stormstreaming.stormlibrary.model.VideoProgress;
import com.stormstreaming.stormlibrary.socket.StormWebSocketConnection;

import java.util.ArrayList;
import java.util.List;

public class StormLibrary {

    public interface EventListener {

        default public void onVideoConnecting() {
        }

        default public void onVideoMetaData(VideoMetaData videoMetaData) {
        }

        default public void onVideoConnectionError(Exception e) {
        }

        default public void onVideoNotFound() {
        }

        default public void onVideoProgress(VideoProgress videoProgress) {
        }

        default public void onVideoStop() {
        }

        default public void onVideoSeek(long streamSeekUnixTime){

        }

        default public void onVideoPlay() {
        }

        default public void onVideoPause() {
        }


        default public void onIncompatiblePlayerProtocol(int playerProtocolVersion, int serverProtocolVersion) {
        }

        default public void onVolumeChanged(float volume) {
        }

        default public void onStormMediaItemAdded(StormMediaItem stormMediaItem){}

        default public void onStormMediaItemRemoved(StormMediaItem stormMediaItem){}

        default public void onStormMediaItemSelect(StormMediaItem stormMediaItem){}

        default public void onStormMediaItemPlay(StormMediaItem stormMediaItem){}

        default public void onGatewayConnecting(){}

        default public void onGatewayConnectionError(Exception e){}

        default public void onGatewayGroupNameNotFound(){}

        default public void onGatewayStormMediaItems(List<StormMediaItem> stormMediaItems){}

    }

    public static final int PLAYER_PROTOCOL_VERSION = 1;

    private ExoPlayerImpl exoPlayerImpl;
    private List<StormMediaItem> stormMediaItems = new ArrayList<StormMediaItem>();
    private Listeners<EventListener> listeners = new Listeners<StormLibrary.EventListener>();
    private StormWebSocketConnection currentWebSocketConnection;
    private StormGateway stormGateway;

    /*
    When should stream start after connecting
     */
    private volatile long streamStartTime = 0;

    /*
    To calculate current position on progress bar after pause
     */
    private volatile long streamDurationOffset = 0;
    private volatile boolean isPlaying = false;
    private volatile long lastPauseTime = 0;

    public StormLibrary() {

    }

    public StormGateway initStormGateway(String groupName){
        this.stormGateway = new StormGateway(this, groupName);
        return this.stormGateway;
    }

    public void initExoPlayer(Context context, PlayerView exoPlayerView) {
        this.exoPlayerImpl = new ExoPlayerImpl(this, context, exoPlayerView);
    }

    public void play() {
        this.exoPlayerImpl.play();
    }

    public void pause() {
        this.exoPlayerImpl.pause();
    }

    public void stop() {
        this.streamStartTime = 0;
        this.streamDurationOffset = 0;
        this.exoPlayerImpl.stop();
    }

    public void setVolume(float volume){
        this.exoPlayerImpl.setVolume(volume);
    }

    public float getVolume(){
        return this.exoPlayerImpl.getVolume();
    }

    public void seekTo(long streamSeekUnixTime) {

        if (this.currentWebSocketConnection != null) {
            this.stop();
            this.streamStartTime = streamSeekUnixTime;
            this.exoPlayerImpl.prepare();
            this.exoPlayerImpl.play();

            listeners.dispatchEvent(listener -> listener.onVideoSeek(streamSeekUnixTime));
        }

    }

    public void addMediaItem(StormMediaItem stormMediaItem) {
        this.addMediaItem(stormMediaItem, false);
    }

    public void addMediaItem(StormMediaItem stormMediaItem, boolean setAsSelected) {
        this.stormMediaItems.add(stormMediaItem);
        this.listeners.dispatchEvent(listener -> listener.onStormMediaItemAdded(stormMediaItem));
        if(setAsSelected)
            this.selectMediaItem(stormMediaItem);

    }

    public void removeMediaItem(StormMediaItem stormMediaItem){
        this.stormMediaItems.remove(stormMediaItem);
        this.listeners.dispatchEvent(listener -> listener.onStormMediaItemRemoved(stormMediaItem));
    }

    public void playMediaItem(StormMediaItem stormMediaItem) {
        this.playMediaItem(stormMediaItem, true);
    }

    public void playMediaItem(StormMediaItem stormMediaItem, boolean resetSeekPosition) {

        if(!this.stormMediaItems.contains(stormMediaItem))
            this.addMediaItem(stormMediaItem);

        this.selectMediaItem(stormMediaItem);

        if(resetSeekPosition) {
            this.streamStartTime = 0;
            this.streamDurationOffset = 0;
        }

        this.listeners.dispatchEvent(listener -> listener.onStormMediaItemPlay(stormMediaItem));
        this.exoPlayerImpl.setMediaItem(stormMediaItem.getMediaItem());
        this.exoPlayerImpl.stop();
        this.exoPlayerImpl.prepare();
        this.exoPlayerImpl.play();
    }

    public void selectMediaItem(StormMediaItem stormMediaItem){
        for(int i=0;i<this.stormMediaItems.size();i++)
            this.stormMediaItems.get(i).setSelected(false);

        this.exoPlayerImpl.setMediaItem(stormMediaItem.getMediaItem());
        stormMediaItem.setSelected(true);
        this.listeners.dispatchEvent(listener -> listener.onStormMediaItemSelect(stormMediaItem));
    }

    public void clearStormMediaItems(){
        this.stormMediaItems.clear();
        for(int i=0;i<this.stormMediaItems.size();i++){
            this.removeMediaItem(this.stormMediaItems.get(i));
        }
    }

    public StormMediaItem getSelectedStormMediaItem(){
        for(int i=0;i<this.stormMediaItems.size();i++) {
            if (this.stormMediaItems.get(i).isSelected())
                return this.stormMediaItems.get(i);
        }
        return null;
    }

    public List<StormMediaItem> getStormMediaItems(){
        return this.stormMediaItems;
    }

    public void prepare(boolean autostart) throws EmptyMediaItemsListException {

        if(this.stormGateway != null && this.stormMediaItems.size() == 0){
            this.stormGateway.prepare(autostart);
            return;
        }

        StormMediaItem stormMediaItem = this.getSelectedStormMediaItem();

        if (stormMediaItem == null) {
            if (stormMediaItems.size() == 0)
                throw new EmptyMediaItemsListException();
            else {
                stormMediaItem = stormMediaItems.get(0);
                this.selectMediaItem(stormMediaItem);
            }
        }

        if (autostart) {
            this.exoPlayerImpl.prepare();
            this.exoPlayerImpl.play();
        }


    }

    public Listeners<StormLibrary.EventListener> getListeners() {
        return listeners;
    }

    public void addEventListener(StormLibrary.EventListener e) {
        listeners.addEventListener(e);
    }

    public void removeEventListener(StormLibrary.EventListener e) {
        listeners.removeEventListener(e);
    }

    public StormWebSocketConnection getCurrentWebSocketConnection() {
        return currentWebSocketConnection;
    }

    public void setCurrentWebSocketConnection(StormWebSocketConnection currentWebSocketConnection) {
        this.currentWebSocketConnection = currentWebSocketConnection;
    }

    public long getStreamStartTime() {
        return streamStartTime;
    }

    public void setStreamStartTime(long streamStartTime) {
        this.streamStartTime = streamStartTime;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;

        if(!playing)
            this.lastPauseTime = System.currentTimeMillis();
        else{
            if(this.lastPauseTime != 0)
                this.streamDurationOffset += System.currentTimeMillis()-this.lastPauseTime;
            this.lastPauseTime = 0;
        }
    }

    public long getStreamDurationOffset() {
        return streamDurationOffset;
    }

    public long getLastPauseTime() {
        return lastPauseTime;
    }

    public SimpleExoPlayer getExoPlayer(){
        return this.exoPlayerImpl.getExoPlayer();
    }

    public StormGateway getStormGateway(){
        return this.stormGateway;
    }
}
