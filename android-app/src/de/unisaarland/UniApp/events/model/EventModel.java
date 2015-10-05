package de.unisaarland.UniApp.events.model;

import java.io.Serializable;
import java.util.Date;

public class EventModel implements Serializable {
    private final String eventTitle;
    private final String eventDescription;
    private final Date publicationDate;
    private final String link;

    public EventModel(String eventTitle, String eventDescription, Date publicationDate, String link) {
        this.eventTitle = eventTitle;
        this.eventDescription = eventDescription;
        this.publicationDate = publicationDate;
        this.link = link;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public String getLink() {
        return link;
    }
}
