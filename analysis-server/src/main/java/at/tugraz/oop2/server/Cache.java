package at.tugraz.oop2.server;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

// libs for cache
import at.tugraz.oop2.Logger;
import at.tugraz.oop2.data.DataSeries;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class Cache implements Serializable {

    private final LoadingCache<CacheDataQueryParameters, DataSeries> cachedData;

    public Cache(AnalysisServer analysisServer){
        this.cachedData = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).maximumSize(1000).build(new CacheLoader<>() {
            @Override
            public DataSeries load(CacheDataQueryParameters cacheDataQueryParameters) throws Exception {
                Logger.info("Cache miss!");
                DataSeries dataSeries = analysisServer.serverThread.queryData(cacheDataQueryParameters.getDataObject());
                return dataSeries;
            }
        });
    }

    public DataSeries getCachedData(CacheDataQueryParameters ObjectCache) {
        try {
            DataSeries dataSeries = this.cachedData.get(ObjectCache);
            return dataSeries;
        } catch (ExecutionException e) {
            Logger.err("Can't recover data from cache!");
        }

        return null;
    }
}


