package com.stormstreaming.stormlibrary;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.stormstreaming.stormlibrary.exception.EmptyMediaItemsListException;
import com.stormstreaming.stormlibrary.exception.EmptyStormGatewayServerListException;
import com.stormstreaming.stormlibrary.exception.StormGatewayConnectionFailedException;
import com.stormstreaming.stormlibrary.model.StormGatewayServer;
import com.stormstreaming.stormlibrary.model.StormMediaItem;
import com.stormstreaming.stormlibrary.socket.StormGatewayWebsocketConnection;
import com.stormstreaming.stormlibrary.socket.StormWebSocketConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class StormGateway {

    private StormLibrary stormLibrary;

    private String groupName;
    private List<StormGatewayServer> stormGatewayServers = new ArrayList<StormGatewayServer>();

    private boolean autostartAfter = false;
    private StormGatewayWebsocketConnection currentWebsocketconnection;

    /*
    If Gateway job is done
     */
    private boolean finished = false;

    public StormGateway(StormLibrary stormLibrary, String groupName){
        this.stormLibrary = stormLibrary;
        this.groupName = groupName;
    }

    public void addStormGatewayServer(StormGatewayServer stormGatewayServer){
        stormGatewayServer.setGroupName(groupName);
        stormGatewayServers.add(stormGatewayServer);
    }

    public void prepare(boolean autostart){
        this.autostartAfter = autostart;
        this.connect();
    }

    public void reconnect(StormGatewayServer failedServer){
        failedServer.setConnectionFailed(true);
        if(!finished) {
            Log.i("StormGateway", "Reconnecting");
            connect();
        }
    }

    public void parseData(JSONObject data){
        finished = true;

        try {
            if(data.getString("status").equals("failed")){
                Log.e("StormGateway", "Given groupName was not found");
                this.stormLibrary.getListeners().dispatchEvent(listener -> listener.onGatewayGroupNameNotFound());
            }else{
                List<StormMediaItem> stormMediaItems = new ArrayList<StormMediaItem>();

                JSONObject stream = data.getJSONObject("stream");

                JSONArray serverList = stream.getJSONArray("serverList");
                JSONArray sourceList = stream.getJSONArray("sourceList");

                JSONObject serverList0 = serverList.getJSONObject(0);

                for(int i=0; i<sourceList.length();i++){
                    JSONObject source = sourceList.getJSONObject(i);
                    JSONObject streamInfo = source.getJSONObject("streamInfo");

                    StormMediaItem stormMediaItem = null;
                    if(source.getString("protocol").equals("rtmp"))
                        stormMediaItem = new StormMediaItem(serverList0.getString("host"), serverList0.getInt("port"), serverList0.getBoolean("ssl"), source.getString("application"), source.getString("streamName"), streamInfo.getString("label"), source.getString("rtmpHost"), source.getString("rtmpApplication"));
                    else
                        stormMediaItem = new StormMediaItem(serverList0.getString("host"), serverList0.getInt("port"), serverList0.getBoolean("ssl"), source.getString("application"), source.getString("streamName"), streamInfo.getString("label"));

                    if(source.has("isDefault") && source.getBoolean("isDefault"))
                        stormMediaItem.setSelected(true);

                    stormMediaItems.add(stormMediaItem);
                }

                this.stormLibrary.getListeners().dispatchEvent(listener -> listener.onGatewayStormMediaItems(stormMediaItems));

                putStormMediaItemsToLibrary(stormMediaItems);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(currentWebsocketconnection != null)
            currentWebsocketconnection.close();
    }
    public void connect(){

        this.stormLibrary.getListeners().dispatchEvent(listener -> listener.onGatewayConnecting());

        if(stormGatewayServers.size() == 0){
            Log.e("StormGateway", "The gateway server list is empty");
            this.stormLibrary.getListeners().dispatchEvent(listener -> listener.onGatewayConnectionError(new EmptyStormGatewayServerListException()));
            return;
        }

        if(currentWebsocketconnection != null)
            currentWebsocketconnection.close();

        /*
        wybieramy nastepny z listy gdzie !isConnectionFailed()
         */
        StormGatewayServer srv = null;

        for(int i=0;i<stormGatewayServers.size();i++){
            if(!stormGatewayServers.get(i).isConnectionFailed()){
                srv = stormGatewayServers.get(i);
                break;
            }
        }

        if(srv == null){
            Log.e("StormGateway", "Could not connect to any servers on the gateway server list");
            this.stormLibrary.getListeners().dispatchEvent(listener -> listener.onGatewayConnectionError(new StormGatewayConnectionFailedException()));
            return;
        }

        URI uri = srv.getURI();

        currentWebsocketconnection = new StormGatewayWebsocketConnection(this, srv);
        if(uri.toString().startsWith("wss")) {
            try {
                SSLContext sslContext = SSLContext.getDefault();
                SSLSocketFactory factory = sslContext.getSocketFactory();
                currentWebsocketconnection.setSocketFactory(factory);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

        }

        try {
            Log.i("Websocket (Gateway)", "Connecting to: "+uri);
            currentWebsocketconnection.connectBlocking();
        } catch (InterruptedException e) {
            reconnect(currentWebsocketconnection.getStormGatewayServer());
        }
    }

    private void putStormMediaItemsToLibrary(List<StormMediaItem> stormMediaItems){

        Handler mainHandler = new Handler(Looper.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {

                stormLibrary.clearStormMediaItems();
                for(int i=0;i<stormMediaItems.size();i++) {
                    stormLibrary.addMediaItem(stormMediaItems.get(i), stormMediaItems.get(i).isSelected());
                }

                try {
                    stormLibrary.prepare(autostartAfter);
                } catch (EmptyMediaItemsListException e) {
                    e.printStackTrace();
                }
            }
        };
        mainHandler.post(myRunnable);


    }

}
