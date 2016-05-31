package org.indywidualni.centrumfm.rest.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class Song implements Parcelable, Comparable<Song> {

    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
    private String id;
    private String sum;
    private String title;
    private String duration;
    private String played;
    private String artist;
    private int dbId;

    public Song() {
    }

    protected Song(Parcel in) {
        this.id = in.readString();
        this.sum = in.readString();
        this.title = in.readString();
        this.duration = in.readString();
        this.played = in.readString();
        this.artist = in.readString();
        this.dbId = in.readInt();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSum() {
        return sum;
    }

    public void setSum(String sum) {
        this.sum = sum;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPlayed() {
        return played;
    }

    public void setPlayed(String played) {
        this.played = played;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getDbId() {
        return dbId;
    }

    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.sum);
        dest.writeString(this.title);
        dest.writeString(this.duration);
        dest.writeString(this.played);
        dest.writeString(this.artist);
        dest.writeInt(this.dbId);
    }

    public String getSearchableData() {
        return artist + " " + title + " " + sum + " " + duration + " " + played;
    }

    @Override
    public int compareTo(@NonNull Song o) {
        if (getDbId() > o.getDbId())
            return -1;
        else if (getDbId() < o.getDbId())
            return 1;
        else
            return 0;
    }

    @Override
    public String toString() {
        return "ClassPojo [id = " + id + ", sum = " + sum + ", title = " + title + ", " +
                "duration = " + duration + ", played = " + played + ", artist = " + artist + ", dbId = " + dbId + "]";
    }

}