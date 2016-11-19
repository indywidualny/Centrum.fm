package org.indywidualni.centrumfm.rest;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import rx.schedulers.Schedulers;

public class RestClient {

    private static final String BASE_URL = "http://indywidualni.org";
    private static final String RSS_URL = "http://feeds.feedburner.com";

    private static final RxJavaCallAdapterFactory rxAdapter;
    
    private static final Object LOCK_JSON = new Object();
    private static final Object LOCK_XML = new Object();
    private static final Object LOCK_RSS = new Object();
    
    private static volatile ApiEndpointInterface apiInterfaceJson;
    private static volatile ApiEndpointInterface apiInterfaceXml;
    private static volatile ApiEndpointInterface apiInterfaceRss;

    static {
        rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());
    }

    private RestClient() {
    }

    public static ApiEndpointInterface getClientJson() {
        if (apiInterfaceJson == null) {
            synchronized (LOCK_JSON) {
                if (apiInterfaceJson == null) {
                    Retrofit client = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .addCallAdapterFactory(rxAdapter)
                            .build();
                    apiInterfaceJson = client.create(ApiEndpointInterface.class);
                }
            }
        }
        return apiInterfaceJson;
    }

    public static ApiEndpointInterface getClientXml() {
        if (apiInterfaceXml == null) {
            synchronized (LOCK_XML) {
                if (apiInterfaceXml == null) {
                    Retrofit client = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(SimpleXmlConverterFactory.create())
                            .addCallAdapterFactory(rxAdapter)
                            .build();
                    apiInterfaceXml = client.create(ApiEndpointInterface.class);
                }
            }
        }
        return apiInterfaceXml;
    }

    public static ApiEndpointInterface getClientRss() {
        if (apiInterfaceRss == null) {
            synchronized (LOCK_RSS) {
                if (apiInterfaceRss == null) {
                    Retrofit client = new Retrofit.Builder()
                            .baseUrl(RSS_URL)
                            .addConverterFactory(SimpleXmlConverterFactory.create())
                            .addCallAdapterFactory(rxAdapter)
                            .build();
                    apiInterfaceRss = client.create(ApiEndpointInterface.class);
                }
            }
        }
        return apiInterfaceRss;
    }

}