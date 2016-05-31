package org.indywidualni.centrumfm.rest.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class Schedule {

    @ElementList(name = "event", inline = true)
    private List<Event> events;

    public List<Event> getEvents() {
        return events;
    }

    @Root(name = "event", strict = false)
    public static class Event implements Comparable<Event>, Parcelable {

        public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
            @Override
            public Event createFromParcel(Parcel source) {
                return new Event(source);
            }

            @Override
            public Event[] newArray(int size) {
                return new Event[size];
            }
        };
        @Attribute(name = "id")
        private int id;
        
        @Element(name = "end_date")
        private String endDate;
        
        @Element(name = "text")
        private String name;
        
        @Element(name = "pasmo")
        private String band;
        
        @Element(name = "rec_type")
        private String weekdays;
        
        @Element(name = "opis", required = false)
        private String description;
        
        @Element(name = "event_pid")
        private String eventPid;
        
        @Element(name = "start_date")
        private String startDate;
        
        @Element(name = "user")
        private String user;
        
        @Element(name = "event_length")
        private int eventLength;
        
        private boolean favourite;

        public Event() {
        }

        protected Event(Parcel in) {
            this.id = in.readInt();
            this.endDate = in.readString();
            this.name = in.readString();
            this.band = in.readString();
            this.weekdays = in.readString();
            this.description = in.readString();
            this.eventPid = in.readString();
            this.startDate = in.readString();
            this.user = in.readString();
            this.eventLength = in.readInt();
            this.favourite = in.readByte() != 0;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getEndDate() {
            return endDate;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBand() {
            return band;
        }

        public void setBand(String band) {
            this.band = band;
        }

        public String getWeekdays() {
            return weekdays.replaceAll("week_.___", "").replace("#no", "");
        }

        public void setWeekdays(String weekdays) {
            this.weekdays = weekdays;
        }

        public String getDescription() {
            return description;
        }

        public String getEventPid() {
            return eventPid;
        }

        public String getStartDate() {
            return startDate.replaceAll(".*( )", "");
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getUser() {
            return user;
        }

        public int getEventLength() {
            return eventLength;
        }

        public void setEventLength(int eventLength) {
            this.eventLength = eventLength;
        }

        public boolean isFavourite() {
            return favourite;
        }

        public void setFavourite(boolean favourite) {
            this.favourite = favourite;
        }

        @Override
        public int compareTo(@NonNull Event o) {
            if (getStartDate() == null || o.getStartDate() == null)
                return 0;
            return getStartDate().compareTo(o.getStartDate());
        }

        @Override
        public String toString() {
            return "ClassPojo [id = " + id + ", endDate = " + endDate + ", name = " + name +
                    ", band = " + band + ", weekdays = " + weekdays + ", description = " +
                    description + ", eventPid = " + eventPid + ", startDate = " + startDate +
                    ", user = " + user + ", eventLength = " + eventLength + "]";
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.id);
            dest.writeString(this.endDate);
            dest.writeString(this.name);
            dest.writeString(this.band);
            dest.writeString(this.weekdays);
            dest.writeString(this.description);
            dest.writeString(this.eventPid);
            dest.writeString(this.startDate);
            dest.writeString(this.user);
            dest.writeInt(this.eventLength);
            dest.writeByte(this.favourite ? (byte) 1 : (byte) 0);
        }
    }

}