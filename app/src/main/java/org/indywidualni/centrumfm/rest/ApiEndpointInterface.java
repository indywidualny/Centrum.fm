package org.indywidualni.centrumfm.rest;

import org.indywidualni.centrumfm.rest.model.Rds;
import org.indywidualni.centrumfm.rest.model.Rss;
import org.indywidualni.centrumfm.rest.model.Schedule;
import org.indywidualni.centrumfm.rest.model.Server;
import org.indywidualni.centrumfm.rest.model.Song;

import java.util.List;
import java.util.Map;

import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rx.Observable;

@SuppressWarnings("UnusedDeclaration")
public interface ApiEndpointInterface {

    @GET("/centrum/")
    Observable<Server> getServerStatus();

    @GET("/centrum/rds.php")
    Observable<List<Rds>> getRds();

    @GET("/centrum/ramowka.xml")
    Observable<Schedule> getSchedule();

    @GET("/RadioCentrum")
    Observable<Rss> getRss();

    @FormUrlEncoded
    @POST("/centrum/songs.py")
    Observable<List<Song>> getSongs(@FieldMap Map<String, String> params);

    String SONGS_FROM = "from";
    String SONGS_TO = "to";
    String SONGS_LIMIT = "limit";
    String SONGS_SKIP = "skip";
    String SONGS_POPULAR = "popular";
    String SONGS_COUNT = "count";

}