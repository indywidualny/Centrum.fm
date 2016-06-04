package org.indywidualni.centrumfm.rest;

import org.indywidualni.centrumfm.rest.model.Rds;
import org.indywidualni.centrumfm.rest.model.Rss;
import org.indywidualni.centrumfm.rest.model.Schedule;
import org.indywidualni.centrumfm.rest.model.Server;
import org.indywidualni.centrumfm.rest.model.Song;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public class RestClient {

    private static final String BASE_URL = "http://indywidualni.org";
    private static final String RSS_URL = "http://feeds.feedburner.com";
    
    private static final Object LOCK_JSON = new Object();
    private static final Object LOCK_XML = new Object();
    private static final Object LOCK_RSS = new Object();
    
    private static volatile ApiEndpointInterface apiInterfaceJson;
    private static volatile ApiEndpointInterface apiInterfaceXml;
    private static volatile ApiEndpointInterface apiInterfaceRss;

    private RestClient() {
    }

    public static ApiEndpointInterface getClientJson() {
        if (apiInterfaceJson == null) {
            synchronized (LOCK_JSON) {
                if (apiInterfaceJson == null) {
                    Retrofit client = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
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
                            .build();
                    apiInterfaceRss = client.create(ApiEndpointInterface.class);
                }
            }
        }
        return apiInterfaceRss;
    }

    @SuppressWarnings("UnusedDeclaration")
    public interface ApiEndpointInterface {

        @GET("/centrum/")
        Call<Server> getServerStatus();

        @GET("/centrum/rds.php")
        Call<List<Rds>> getRds();

        @GET("/centrum/ramowka.xml")
        Call<Schedule> getSchedule();

        @GET("/RadioCentrum")
        Call<Rss> getRss();

        @FormUrlEncoded
        @POST("/centrum/songs.py")
        Call<List<Song>> getSongs(@FieldMap Map<String, String> params);

        String SONGS_FROM = "from";
        String SONGS_TO = "to";
        String SONGS_LIMIT = "limit";
        String SONGS_SKIP = "skip";
        String SONGS_POPULAR = "popular";
        String SONGS_COUNT = "count";

    }

}