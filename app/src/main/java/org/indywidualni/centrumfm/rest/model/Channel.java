package org.indywidualni.centrumfm.rest.model;

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
    public static class Item implements Comparable<Item> {

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

        public String getTitle() {
            return title;
        }

        public String getLink() {
            return link;
        }

        public String getDescription() {
            return description;
        }

        public List<String> getCategories() {
            return categories;
        }

        public String getEnclosureUrl() {
            return enclosureUrl;
        }

        public String getGuid() {
            return guid;
        }

        public String getPubDate() {
            return pubDate;
        }

        public String getCategory() {
            return category;
        }

        public Date getDate() {
            return date;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setEnclosureUrl(String enclosureUrl) {
            this.enclosureUrl = enclosureUrl;
        }

        public void setGuid(String guid) {
            this.guid = guid;
        }

        public void setPubDate(String pubDate) {
            this.pubDate = pubDate;
        }

        public void setCategory(String category) {
            this.category = category;
        }
        
        public void setDate(Date date) {
            this.date = date;
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

    }

}
