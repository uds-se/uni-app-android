package de.unisaarland.UniApp.rssViews.model;


import java.io.Serializable;

public class RSSArticle implements Serializable {

    private final String baseHref;
    private final String date;
    private final String head;
    private final String subTitle;
    private final String body;

    public RSSArticle(String baseHref, String date, String head, String subTitle, String body) {
        this.baseHref = baseHref;
        this.date = date;
        this.head = head;
        this.subTitle = subTitle;
        this.body = body;
    }

    public String getBaseHref() {
        return baseHref;
    }

    public String getDate() {
        return date;
    }

    public String getHead() {
        return head;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public String getBody() {
        return body;
    }
}
