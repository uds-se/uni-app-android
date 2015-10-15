package de.unisaarland.UniApp.rssViews.model;

import java.io.Serializable;
import java.util.Date;

public class RSSItem implements Serializable {
    private final String title;
    private final String description;
    private final Date publicationDate;
    private final String link;

    public RSSItem(String title, String description, Date publicationDate, String link) {
        this.title = title;
        this.description = description;
        this.publicationDate = publicationDate;
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public String getLink() {
        return link;
    }
}
