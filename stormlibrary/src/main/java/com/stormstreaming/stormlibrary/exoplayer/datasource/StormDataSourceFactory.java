package com.stormstreaming.stormlibrary.exoplayer.datasource;

import com.google.android.exoplayer2.upstream.DataSource;
import com.stormstreaming.stormlibrary.StormLibrary;

public class StormDataSourceFactory implements DataSource.Factory {

    private StormLibrary stormLibrary;

    public StormDataSourceFactory(StormLibrary stormLibrary){
        this.stormLibrary = stormLibrary;
    }

    @Override
    public DataSource createDataSource() {
        return new StormDataSource(stormLibrary);
    }

}
