package com.stormstreaming.stormlibrary.exception;

public class EmptyMediaItemsListException extends Exception{

    public EmptyMediaItemsListException(){
        super("You have to add at least one StormMediaItem");
    }

}
