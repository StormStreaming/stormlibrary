package com.stormstreaming.stormlibrary.exoplayer;

import android.content.Context;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioListener;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.ui.PlayerView;
import com.stormstreaming.stormlibrary.StormLibrary;
import com.stormstreaming.stormlibrary.exoplayer.datasource.StormDataSourceFactory;

public class ExoPlayerImpl implements Player.EventListener, AudioListener{

    private StormLibrary stormLibrary;
    private SimpleExoPlayer exoPlayer;

    public ExoPlayerImpl(StormLibrary stormLibrary, Context context, PlayerView exoPlayerView){
        this.stormLibrary = stormLibrary;
        StormDataSourceFactory dataSourceFactory = new StormDataSourceFactory(stormLibrary);

        /*
        https://blogs.akamai.com/2019/10/enhancing-video-streaming-quality-for-exoplayer---part-2-exoplayers-buffering-strategy-how-to-lower-.html
         */

        DefaultLoadControl.Builder builder = new DefaultLoadControl.Builder();
        builder.setBufferDurationsMs(DefaultLoadControl.DEFAULT_MIN_BUFFER_MS, DefaultLoadControl.DEFAULT_MAX_BUFFER_MS, 1000, DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS);
        DefaultLoadControl loadControl = builder.build();

        exoPlayer = new SimpleExoPlayer.Builder(context).setMediaSourceFactory(
                new DefaultMediaSourceFactory(dataSourceFactory)
                        .setLiveTargetOffsetMs(1000)
                        //.setLiveMaxOffsetMs(1000)
                )
                .setLoadControl(loadControl)
                .build();
        exoPlayerView.setPlayer(exoPlayer);

        exoPlayer.addListener(this);
        exoPlayer.addAudioListener(this);
    }

    public SimpleExoPlayer getExoPlayer() {
        return exoPlayer;
    }

    public void setMediaItem(MediaItem stormMediaItem){
        exoPlayer.setMediaItem(stormMediaItem);
    }

    public void play(){
        exoPlayer.play();
    }

    public void prepare(){
        exoPlayer.prepare();
    }

    public void pause(){
        exoPlayer.pause();
        this.stormLibrary.getListeners().dispatchEvent(listener -> listener.onVideoPause());
    }

    public void setVolume(float volume){
        this.exoPlayer.setVolume(volume);
    }

    public float getVolume(){
        return this.exoPlayer.getVolume();
    }

    public void stop(){
        exoPlayer.stop();
        this.stormLibrary.getListeners().dispatchEvent(listener -> listener.onVideoPause());
    }

    /*
    ExoPlayer events
     */

    @Override
    public void onIsPlayingChanged(boolean isPlaying){
        this.stormLibrary.setPlaying(isPlaying);
        if(isPlaying)
            this.stormLibrary.getListeners().dispatchEvent(listener -> listener.onVideoPlay());
    }

    @Override
    public void onVolumeChanged(float volume){
        this.stormLibrary.getListeners().dispatchEvent(listener -> listener.onVolumeChanged(volume));
    }

    @Override
    public void onPlaybackStateChanged(@Player.State int state) {

        switch(state){
            case Player.STATE_IDLE:

                break;
            case Player.STATE_BUFFERING:

                break;
            case Player.STATE_READY:

                break;
            case Player.STATE_ENDED:
                break;
            default:
                break;
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }
}
