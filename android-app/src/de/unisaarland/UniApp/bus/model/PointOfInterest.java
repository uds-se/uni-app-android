package de.unisaarland.UniApp.bus.model;


import android.support.annotation.NonNull;

import java.io.Serializable;

public class PointOfInterest implements Comparable<PointOfInterest>, Serializable {
    private final String title;
    private final String subtitle;
    private final int canShowLeftCallOut;
    private final int canShowRightCallOut;
    private final String website;
    private final int color;
    private final float latitude;
    private final float longitude;
    private final int id;
    private final int categoryID;

    public PointOfInterest(String title, String subtitle, int canShowLeftCallOut,
                           int canShowRightCallOut, String website, int color, float latitude,
                           float longitude, int id, int categoryID) {
        this.title = title;
        this.subtitle = subtitle;
        this.canShowLeftCallOut = canShowLeftCallOut;
        this.canShowRightCallOut = canShowRightCallOut;
        this.website = website;
        this.color = color;
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;
        this.categoryID = categoryID;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public int isCanShowLeftCallOut() {
        return canShowLeftCallOut;
    }

    public int isCanShowRightCallOut() {
        return canShowRightCallOut;
    }

    public String getWebsite() {
        return website;
    }

    public int getColor() {
        return color;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public int getId() {
        return id;
    }

    public int getCategoryID() {
        return categoryID;
    }

    @Override
    public int compareTo(@NonNull PointOfInterest another) {
        int cmp = getTitle().compareTo(another.getTitle());
        if (cmp == 0) {
            // on API >= 19:
            // cmp = Integer.compare(getId(), another.getId());
            if (getId() < another.getId())
                cmp = -1;
            else if (getId() > another.getId())
                cmp = 1;
        }
        return cmp;
    }
}
