package com.stormstreaming.stormlibrary.exoplayer.datasource;

import android.net.Uri;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.BaseDataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.stormstreaming.stormlibrary.StormLibrary;
import com.stormstreaming.stormlibrary.socket.StormWebSocketConnection;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import static java.lang.Math.min;

public class StormDataSource extends BaseDataSource{

    private StormLibrary stormLibrary;
    private StormWebSocketConnection connection;

    private URI uri;

    protected StormDataSource(StormLibrary stormLibrary) {
        super(true);
        this.stormLibrary = stormLibrary;
    }


    @Override
    public long open(DataSpec dataSpec) throws IOException {

        transferInitializing(dataSpec);

        String url = dataSpec.uri.toString();
        long startTime = this.stormLibrary.getStreamStartTime();
        if(startTime != 0)
            url += "seekStart="+this.stormLibrary.getStreamStartTime()+"&";

        try {
            this.uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        connection = new StormWebSocketConnection(this.stormLibrary, this.uri);
        if(this.uri.toString().startsWith("wss")) {
            try {
                SSLContext sslContext = SSLContext.getDefault();
                SSLSocketFactory factory = sslContext.getSocketFactory();
                connection.setSocketFactory(factory);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

        }
        try {
            connection.connectBlocking();
        } catch (InterruptedException e) {
            throw new IOException(e.toString());
        }

        transferStarted(dataSpec);
        return C.LENGTH_UNSET;
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        if (readLength == 0) {
            return 0;
        } else {

            int bytesRead = connection.read(buffer, offset, readLength);

            bytesTransferred(bytesRead);

            if (bytesRead == -1)
                return C.RESULT_END_OF_INPUT;

            return bytesRead;
        }
    }

    @Override
    public Uri getUri() {
        return Uri.parse(this.uri.toString());
    }

    @Override
    public void close() throws IOException {
        if(connection != null) {
            connection.close();
            connection = null;
        }
        transferEnded();
    }


}
