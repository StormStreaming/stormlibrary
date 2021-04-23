package com.stormstreaming.stormlibrary.socket;

import android.util.Log;

import com.stormstreaming.stormlibrary.StormLibrary;
import com.stormstreaming.stormlibrary.model.VideoMetaData;
import com.stormstreaming.stormlibrary.model.VideoProgress;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.nio.ByteBuffer;

public class StormWebSocketConnection extends WebSocketClient{


    private PipedInputStream inputStream = new PipedInputStream(1024*8);
    private PipedOutputStream pipedOutputStream = new PipedOutputStream();
    private StormLibrary stormLibrary;


    public StormWebSocketConnection(StormLibrary stormLibrary, URI uri) {
        super(uri);
        this.stormLibrary = stormLibrary;
        try {
            inputStream.connect(pipedOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean connectBlocking() throws InterruptedException {
        Log.i("WebSocket", "Connecting with: "+uri.toString());
        this.stormLibrary.getListeners().dispatchEvent(listener -> listener.onVideoConnecting());
        return super.connectBlocking();
    }

    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        if(inputStream == null)
            return -1;
        return inputStream.read(buffer, offset, readLength);
    }

    @Override
    public void close(){

        this.stormLibrary.setCurrentWebSocketConnection(null);
        super.close();
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                inputStream = null;
            }
        }

        if(pipedOutputStream != null){
            try {
                pipedOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                pipedOutputStream = null;
            }
        }

    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.i("Websocket", "Connected");
        this.stormLibrary.setCurrentWebSocketConnection(this);
    }

    @Override
    public void onMessage(String message) {
        try {

            JSONObject json = new JSONObject(message);

            try {

                switch(json.getString("packetType")){
                    case "serverData":

                        int serverProtocolVersion = json.getJSONObject("data").getInt("playerProtocol");

                        Log.i("Websocket", "Server: "+json.getJSONObject("data").getString("serverName")+" | Version: "+json.getJSONObject("data").getString("serverVersion")+" | PlayerProtocolVersion: "+ StormLibrary.PLAYER_PROTOCOL_VERSION+" | ServerProtocolVersion: "+serverProtocolVersion);

                        if(serverProtocolVersion != StormLibrary.PLAYER_PROTOCOL_VERSION){
                            this.stormLibrary.getListeners().dispatchEvent(listener -> listener.onIncompatiblePlayerProtocol(StormLibrary.PLAYER_PROTOCOL_VERSION, serverProtocolVersion));
                        }

                        break;
                    case "timeData":

                        /*
                        Calculating offset from press pause to real time progress bar update
                         */
                        long realTimeOffset = this.stormLibrary.getLastPauseTime() != 0 ? System.currentTimeMillis()-this.stormLibrary.getLastPauseTime() : 0;

                        VideoProgress videoProgress = new VideoProgress();
                        videoProgress.setStreamDuration(json.getJSONObject("data").getLong("streamDuration") - this.stormLibrary.getStreamDurationOffset() - realTimeOffset);
                        videoProgress.setSourceDuration(json.getJSONObject("data").getLong("sourceDuration"));
                        videoProgress.setSourceStartTime(json.getJSONObject("data").getLong("sourceStartTime"));
                        videoProgress.setStreamStartTime(json.getJSONObject("data").getLong("streamStartTime"));
                        videoProgress.setDvrCacheSize(json.getJSONObject("data").getLong("dvrCacheSize"));


                        this.stormLibrary.setStreamStartTime(videoProgress.getStreamStartTime()+videoProgress.getStreamDuration());
                        this.stormLibrary.getListeners().dispatchEvent(listener -> listener.onVideoProgress(videoProgress));

                        break;
                    case "metaData":
                        VideoMetaData videoMetaData = new VideoMetaData();
                        videoMetaData.setVideoWidth(json.getJSONObject("data").getInt("videoWidth"));
                        videoMetaData.setVideoHeight(json.getJSONObject("data").getInt("videoHeight"));
                        videoMetaData.setVideoTimeScale(json.getJSONObject("data").getInt("videoTimeScale"));
                        videoMetaData.setEncoder(json.getJSONObject("data").getString("encoder"));
                        videoMetaData.setVideoCodec(json.getJSONObject("data").getString("videoCodec"));
                        videoMetaData.setAudioCodec(json.getJSONObject("data").getString("audioCodec"));
                        videoMetaData.setAudioChannels(json.getJSONObject("data").getInt("audioChannels"));
                        videoMetaData.setAudioDataRate(json.getJSONObject("data").getInt("audioDataRate"));
                        videoMetaData.setAudioSampleRate(json.getJSONObject("data").getInt("audioSampleRate"));
                        videoMetaData.setNominalFPS(json.getJSONObject("data").getInt("nominalFPS"));
                        this.stormLibrary.getListeners().dispatchEvent(listener -> listener.onVideoMetaData(videoMetaData));
                        break;
                    case "event":

                        switch(json.getJSONObject("data").getString("eventName")){
                            case "StreamNotFound":
                                this.close();
                                this.stormLibrary.getListeners().dispatchEvent(listener -> listener.onVideoNotFound());
                                break;
                            case "StreamUnpublished":
                                this.close();
                                this.stormLibrary.getListeners().dispatchEvent(listener -> listener.onVideoStop());
                                break;
                            case "newVideo":

                                break;
                            default:
                                break;
                        }

                        break;

                }
            } catch (JSONException e) {
                Log.e("Websocket", "Message JSON parser error", e);
            }

            //listeners.dispatchEvent(listener -> listener.onMessage(json));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onMessage( ByteBuffer buf ) {
        byte[] arr = new byte[buf.remaining()];
        buf.get(arr);
        try {
            pipedOutputStream.write(arr);
        } catch (Exception e) {
            this.close();
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i("Websocket", "Closed " + reason);
    }

    @Override
    public void onError(Exception ex) {
        Log.e("Websocket", "Error " + ex.getMessage());
        if(ex instanceof NullPointerException == false)
            this.stormLibrary.getListeners().dispatchEvent(listener -> listener.onVideoConnectionError(ex));

    }

    public final static void printByteArrayAsHEX(byte[] buff) {

        int k = 0;
        for (int i = 0; i < buff.length; i++) {
            System.out.print(String.format("%02X", buff[i]) + " ");
            k++;
            if (k == 16) {
                System.out.print("\r");
                k = 0;
            }
        }
        System.out.println("/n/r");
    }

}
