package com.stormstreaming.stormlibrary.exception;

public class StormGatewayConnectionFailedException extends Exception{

    public StormGatewayConnectionFailedException(){
        super("Could not connect to any servers on the gateway server list");
    }

}
