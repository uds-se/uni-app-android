package de.unisaarland.UniApp.staff;


import java.io.Serializable;

public class SearchResult implements Serializable {

    private final String name;
    private final String url;

    public SearchResult(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
