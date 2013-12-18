package com.st.cs.unisaarland.SaarlandUniversityApp.news.model;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 11/28/13
 * Time: 7:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class NewsModel implements Serializable{
    private String newsTitle;
    private String newsDescription;
    private String publicationDate;
    private String link;

    public String getNewsTitle() {
        return newsTitle;
    }
    public void setNewsTitle(String newsTitle) {
        this.newsTitle = newsTitle;
    }
    public String getNewsDescription() {
        return newsDescription;
    }
    public void setNewsDescription(String newsDescription) {
        this.newsDescription = newsDescription;
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
