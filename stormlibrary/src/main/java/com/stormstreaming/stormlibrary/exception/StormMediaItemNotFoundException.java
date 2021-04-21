package com.stormstreaming.stormlibrary.exception;

public class StormMediaItemNotFoundException extends Exception{

    public StormMediaItemNotFoundException(){
        super("StormMediaItem has not been found");
    }

}
