package de.unisaarland.UniApp.bus.model;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/2/13
 * Time: 3:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class PointOfInterest implements Comparable{
    String title;
    String subtitle;
    int canShowLeftCallOut;
    int canShowRightCallOut;
    String website;
    int color;
    float latitude;
    float longitude;
    int ID;
    int categoryID;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public int isCanShowLeftCallOut() {
        return canShowLeftCallOut;
    }

    public void setCanShowLeftCallOut(int canShowLeftCallOut) {
        this.canShowLeftCallOut = canShowLeftCallOut;
    }

    public int isCanShowRightCallOut() {
        return canShowRightCallOut;
    }

    public void setCanShowRightCallOut(int canShowRightCallOut) {
        this.canShowRightCallOut = canShowRightCallOut;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    @Override
    public int compareTo(Object another) {
        return getTitle().compareTo(((PointOfInterest)another).getTitle());
    }
}
