package org.indywidualni.centrumfm.rest.model;

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
    public static class Event implements Comparable<Event> {

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

        public int getId() {
            return id;
        }

        public String getEndDate() {
            return endDate;
        }

        public String getName() {
            return name;
        }

        public String getBand() {
            return band;
        }

        public String getWeekdays() {
            return weekdays.replaceAll("week_.___", "").replace("#no", "");
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

        public String getUser() {
            return user;
        }

        public int getEventLength() {
            return eventLength;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setBand(String band) {
            this.band = band;
        }

        public void setWeekdays(String weekdays) {
            this.weekdays = weekdays;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
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

    }

}