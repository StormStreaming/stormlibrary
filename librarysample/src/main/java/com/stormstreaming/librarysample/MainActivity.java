package com.stormstreaming.librarysample;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.stormstreaming.stormlibrary.StormGateway;
import com.stormstreaming.stormlibrary.StormLibrary;
import com.stormstreaming.stormlibrary.model.StormGatewayServer;
import com.stormstreaming.stormlibrary.model.StormMediaItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /*
        BASIC EMBED EXAMPLE:
         */

        StormLibrary stormLibrary = new StormLibrary();

        stormLibrary.initExoPlayer(this, findViewById(R.id.exoPlayerView));

        StormMediaItem stormMediaItem = new StormMediaItem("sub1.mydomain.com",443,true,"my_stream_320","320p");
        stormLibrary.addMediaItem(stormMediaItem);

        stormMediaItem = new StormMediaItem("sub1.mydomain.com",443,true,"my_stream_720","720p");
        stormLibrary.addMediaItem(stormMediaItem, true);

        try {
            stormLibrary.prepare(true);
        } catch(Exception e){
            e.printStackTrace();
        }


        /*
        GATEWAY EXAMPLE:
         */

        /*
        StormLibrary stormLibrary = new StormLibrary();
        stormLibrary.initExoPlayer(this, findViewById(R.id.exoPlayerView));

        StormGateway stormGateway = stormLibrary.initStormGateway("test");

        StormGatewayServer server = new StormGatewayServer("sub1.domain.com","live", 443, true);
        stormGateway.addStormGatewayServer(server);

        StormGatewayServer server2 = new StormGatewayServer("sub2.domain.com","live", 443, true);
        stormGateway.addStormGatewayServer(server2);

        try {
            stormLibrary.prepare(true);
        } catch(Exception e){
            e.printStackTrace();
        }
        */

    }
}