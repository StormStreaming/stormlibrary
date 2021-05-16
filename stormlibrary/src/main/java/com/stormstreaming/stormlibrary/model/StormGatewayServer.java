package com.stormstreaming.stormlibrary.model;

import java.net.URI;
import java.net.URISyntaxException;

public class StormGatewayServer {

    private String host;
    private String application;
    private int port;
    private boolean isSSL;

    private boolean connectionFailed = false;
    private String groupName;

    public StormGatewayServer(String host, String application, int port, boolean isSSL){
        this.host = host;
        this.application = application;
        this.port = port;
        this.isSSL = isSSL;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public boolean isConnectionFailed() {
        return connectionFailed;
    }

    public void setConnectionFailed(boolean connectionFailed) {
        this.connectionFailed = connectionFailed;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isSSL() {
        return isSSL;
    }

    public void setSSL(boolean SSL) {
        isSSL = SSL;
    }

    public URI getURI(){
        try {
            return new URI((isSSL() ? "wss" : "ws") + "://" + getHost() + ":" + getPort() + "/gateway/"+getApplication()+"/"+getGroupName()+"?encoding=text&");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }
}
