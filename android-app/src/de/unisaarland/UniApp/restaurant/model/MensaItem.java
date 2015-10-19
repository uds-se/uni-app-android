package de.unisaarland.UniApp.restaurant.model;

import java.io.Serializable;
import java.util.Date;

public class MensaItem implements Serializable {

    private final String category;
    private final String desc;
    private final String title;
    private final Date tag;
    private final String[] labels;
    private final int preis1;
    private final int preis2;
    private final int preis3;
    private final int color;

    public MensaItem(String category, String desc, String title, Date tag, String[] labels,
                     int preis1, int preis2, int preis3, int color) {
        this.category = category;
        this.desc = desc;
        this.title = title;
        this.tag = tag;
        this.labels = labels;
        this.preis1 = preis1;
        this.preis2 = preis2;
        this.preis3 = preis3;
        this.color = color;
    }

    public String getCategory() {
        return category;
    }

    public String getDesc() {
        return desc;
    }

    public String getTitle() {
        return title;
    }

    public Date getTag() {
        return tag;
    }

    public String[] getLabels() {
        return labels;
    }

    public int getPreis1() {
        return preis1;
    }

    public int getPreis2() {
        return preis2;
    }

    public int getPreis3() {
        return preis3;
    }

    public int getColor() {
        return color;
    }
}
