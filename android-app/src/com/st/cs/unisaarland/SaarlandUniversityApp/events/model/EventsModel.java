package com.st.cs.unisaarland.SaarlandUniversityApp.events.model;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/2/13
 * Time: 12:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class EventsModel implements Serializable{
    private String eventTitle;
    private String eventDescription;
    private String publicationDate;
    private String link;

    public String getEventTitle() {
        return eventTitle;
    }
    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }
    public String getEventDescription() {
        return eventDescription;
    }
    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }
    public String getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
