package org.indywidualni.centrumfm.rest;

import org.indywidualni.centrumfm.rest.model.RDS;
import org.indywidualni.centrumfm.rest.model.RSS;
import org.indywidualni.centrumfm.rest.model.Schedule;
import org.indywidualni.centrumfm.rest.model.Server;
import org.indywidualni.centrumfm.rest.model.Song;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public class RestClient {

    private static final String BASE_URL = "http://indywidualni.org";
    private static final String RSS_URL = "http://feeds.feedburner.com";
    
    private static final Object LOCK_JSON = new Object();
    private static final Object LOCK_XML = new Object();
    private static final Object LOCK_RSS = new Object();
    
    private static volatile ApiEndpointInterface apiInterfaceJSON;
    private static volatile ApiEndpointInterface apiInterfaceXML;
    private static volatile ApiEndpointInterface apiInterfaceRSS;

    private RestClient() {
    }

    public static ApiEndpointInterface getClientJSON() {
        if (apiInterfaceJSON == null) {
            synchronized (LOCK_JSON) {
                if (apiInterfaceJSON == null) {
                    Retrofit client = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    apiInterfaceJSON = client.create(ApiEndpointInterface.class);
                }
            }
        }
        return apiInterfaceJSON;
    }

    public static ApiEndpointInterface getClientXML() {
        if (apiInterfaceXML == null) {
            synchronized (LOCK_XML) {
                if (apiInterfaceXML == null) {
                    Retrofit client = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(SimpleXmlConverterFactory.create())
                            .build();
                    apiInterfaceXML = client.create(ApiEndpointInterface.class);
                }
            }
        }
        return apiInterfaceXML;
    }

    public static ApiEndpointInterface getClientRSS() {
        if (apiInterfaceRSS == null) {
            synchronized (LOCK_RSS) {
                if (apiInterfaceRSS == null) {
                    Retrofit client = new Retrofit.Builder()
                            .baseUrl(RSS_URL)
                            .addConverterFactory(SimpleXmlConverterFactory.create())
                            .build();
                    apiInterfaceRSS = client.create(ApiEndpointInterface.class);
                }
            }
        }
        return apiInterfaceRSS;
    }

    public interface ApiEndpointInterface {

        @GET("/centrum/")
        Call<Server> getServerStatus();

        @GET("/centrum/rds.php")
        Call<List<RDS>> getRDS();

        @GET("/centrum/ramowka.xml")
        Call<Schedule> getSchedule();

        @GET("/RadioCentrum")
        Call<RSS> getRSS();

        @FormUrlEncoded
        @POST("/centrum/songs.py")
        Call<List<Song>> getSongs(@Field("popular") String popular, @Field("limit") String limit);

    }

}