package com.stormstreaming.stormlibrary.socket;

import android.util.Log;

import com.stormstreaming.stormlibrary.StormGateway;
import com.stormstreaming.stormlibrary.model.StormGatewayServer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;

public class StormGatewayWebsocketConnection extends WebSocketClient {

    private StormGateway stormGateway;
    private StormGatewayServer stormGatewayServer;

    public StormGatewayWebsocketConnection(StormGateway stormGateway, StormGatewayServer stormGatewayServer){
        super(stormGatewayServer.getURI());
        this.stormGateway = stormGateway;
        this.stormGatewayServer = stormGatewayServer;
    }

    public StormGatewayServer getStormGatewayServer() {
        return stormGatewayServer;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.i("Websocket (Gateway)", "Connected");
    }

    @Override
    public void onMessage(String message) {
        Log.i("Websocket (Gateway)", "Message: "+message);
        try {
            stormGateway.parseData(new JSONObject(message));
        } catch (JSONException e) {
            Log.e("Websocket (Gateway)", "Error parsing data");
            stormGateway.reconnect(stormGatewayServer);
        }
        close();
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i("Websocket (Gateway)", "Closed " + reason);
        stormGateway.reconnect(stormGatewayServer);
    }

    @Override
    public void onError(Exception ex) {
        Log.e("Websocket (Gateway)", "Error " + ex.getMessage());
        stormGateway.reconnect(stormGatewayServer);
    }
}
