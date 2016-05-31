package org.indywidualni.centrumfm.rest.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.NamespaceList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import java.util.Date;
import java.util.List;

@NamespaceList({
        @Namespace(reference = "http://www.w3.org/2005/Atom", prefix = "atom")
})

@SuppressWarnings("UnusedDeclaration")
@Root(strict = false)
public class Channel {

    @ElementList(name = "item", required = true, inline = true)
    private List<Item> itemList;

    public List<Item> getItems() {
        return itemList;
    }

    @Root(name = "item", strict = false)
    public static class Item implements Comparable<Item>, Parcelable {

        public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
            @Override
            public Item createFromParcel(Parcel source) {
                return new Item(source);
            }

            @Override
            public Item[] newArray(int size) {
                return new Item[size];
            }
        };
        @Element(name = "title", required = true)
        private String title;
        @Element(name = "link", required = true)
        private String link;
        @Element(name = "description", required = true)
        private String description;
        @ElementList(entry = "category", inline = true, required = false)
        private List<String> categories;
        @Path("enclosure")
        @Attribute(name = "url", required = false)
        private String enclosureUrl;
        @Element(name = "guid", required = false)
        private String guid;
        @Element(name = "pubDate", required = false)
        private String pubDate;
        // it's filled manually
        private String category;
        // it's filled manually
        private Date date;
        // it's filled manually
        private boolean expanded;

        public Item() {
        }

        protected Item(Parcel in) {
            this.title = in.readString();
            this.link = in.readString();
            this.description = in.readString();
            this.categories = in.createStringArrayList();
            this.enclosureUrl = in.readString();
            this.guid = in.readString();
            this.pubDate = in.readString();
            this.category = in.readString();
            long tmpDate = in.readLong();
            this.date = tmpDate == -1 ? null : new Date(tmpDate);
            this.expanded = in.readByte() != 0;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getCategories() {
            return categories;
        }

        public String getEnclosureUrl() {
            return enclosureUrl;
        }

        public void setEnclosureUrl(String enclosureUrl) {
            this.enclosureUrl = enclosureUrl;
        }

        public String getGuid() {
            return guid;
        }

        public void setGuid(String guid) {
            this.guid = guid;
        }

        public String getPubDate() {
            return pubDate;
        }

        public void setPubDate(String pubDate) {
            this.pubDate = pubDate;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public boolean isExpanded() {
            return expanded;
        }

        public void setExpanded(boolean expanded) {
            this.expanded = expanded;
        }

        @Override
        public int compareTo(@NonNull Item o) {
            if (getDate() == null || o.getDate() == null)
                return 0;
            return o.getDate().compareTo(getDate());
        }

        @Override
        public String toString() {
            return "Item{" +
                    "title='" + title + '\'' +
                    ", link='" + link + '\'' +
                    ", description='" + description + '\'' +
                    ", category='" + categories + '\'' +
                    ", enclosure='" + enclosureUrl + '\'' +
                    ", guid='" + guid + '\'' +
                    ", pubDate='" + pubDate + '\'' +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.title);
            dest.writeString(this.link);
            dest.writeString(this.description);
            dest.writeStringList(this.categories);
            dest.writeString(this.enclosureUrl);
            dest.writeString(this.guid);
            dest.writeString(this.pubDate);
            dest.writeString(this.category);
            dest.writeLong(this.date != null ? this.date.getTime() : -1);
            dest.writeByte((byte) (this.expanded ? 1 : 0));
        }
    }

}
