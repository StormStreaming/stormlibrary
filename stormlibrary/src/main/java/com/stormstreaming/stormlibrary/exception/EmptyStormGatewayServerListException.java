package com.stormstreaming.stormlibrary.exception;

public class EmptyStormGatewayServerListException extends Exception{

    public EmptyStormGatewayServerListException(){
        super("The gateway server list is empty");
    }

}
