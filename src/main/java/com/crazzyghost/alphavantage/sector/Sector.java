package com.crazzyghost.alphavantage.sector;

import java.io.IOException;

import com.crazzyghost.alphavantage.AlphaVantageException;
import com.crazzyghost.alphavantage.Config;
import com.crazzyghost.alphavantage.Fetcher;
import com.crazzyghost.alphavantage.UrlExtractor;
import com.crazzyghost.alphavantage.parser.Parser;
import com.crazzyghost.alphavantage.sector.response.SectorResponse;
import com.crazzyghost.alphavantage.sector.request.SectorRequest;

import okhttp3.Call;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Access to Sector Performance Data
 * @author crazzyghost
 * @since 1.4.0
 */
public final class Sector implements Fetcher {

    private Config config;
    private SectorRequest.Builder builder;
    private Fetcher.SuccessCallback<SectorResponse> successCallback;
    private Fetcher.FailureCallback failureCallback;

    public Sector(Config config) {
        this.config = config;
        this.builder = new SectorRequest.Builder();
    }

    /**
     * Set the success callback
     * @param callback successful fetch handler
     * @return current instance of {@link SectorResponse}
     */
    public Sector onSuccess(Fetcher.SuccessCallback<SectorResponse> callback){
        this.successCallback = callback;
        return this;
    }

    /**
     * Set the failure callback
     * @param callback failed fetch handler
     * @return current instance of {@link SectorResponse}
     */
    public Sector onFailure(Fetcher.FailureCallback callback){
        this.failureCallback= callback;
        return this;
    }

    @Override
    public void fetch() {

        Config.checkNotNullOrKeyEmpty(config);

        config.getOkHttpClient().newCall(UrlExtractor.extract(builder.build(), config.getKey())).enqueue(new okhttp3.Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                if(failureCallback != null) failureCallback.onFailure(new AlphaVantageException());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    try(ResponseBody body = response.body()){
                        SectorResponse sectorResponse = SectorResponse.of(Parser.parseJSON(body.string()));
                        if(sectorResponse.getErrorMessage() != null && failureCallback != null) failureCallback.onFailure(new AlphaVantageException(sectorResponse.getErrorMessage()));
                        if(successCallback != null) successCallback.onSuccess(sectorResponse);
                    }
                }else{
                    if(failureCallback != null) failureCallback.onFailure(new AlphaVantageException());
                }
            }
            
        });

    }

}