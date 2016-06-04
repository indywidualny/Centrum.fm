package org.indywidualni.centrumfm.rest.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@SuppressWarnings("UnusedDeclaration")
@Root
public class Rss {

    @Attribute
    private String version;

    @Element
    private Channel channel;

    public String getVersion() {
        return version;
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "Rss{version='" + version + '\'' +
                ", channel=" + channel + '}';
    }

}