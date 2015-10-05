package de.unisaarland.UniApp.news.model;

import java.io.Serializable;
import java.util.Date;


public class NewsModel implements Serializable{
    private final String newsTitle;
    private final String newsDescription;
    private final Date publicationDate;
    private final String link;

    public NewsModel(String newsTitle, String newsDescription, Date publicationDate, String link) {
        this.newsTitle = newsTitle;
        this.newsDescription = newsDescription;
        this.publicationDate = publicationDate;
        this.link = link;
    }

    public String getNewsTitle() {
        return newsTitle;
    }
    public String getNewsDescription() {
        return newsDescription;
    }
    public Date getPublicationDate() {
        return publicationDate;
    }
    public String getLink() {
        return link;
    }
}
