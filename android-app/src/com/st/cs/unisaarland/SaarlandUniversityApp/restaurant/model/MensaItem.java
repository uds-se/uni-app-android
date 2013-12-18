package com.st.cs.unisaarland.SaarlandUniversityApp.restaurant.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/6/13
 * Time: 12:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class MensaItem implements Serializable {
    private String category;
    private String desc;
    private String title;
    private Date tag;
    private String kennzeichnungen;
    private String beilagen;
    private String preis1;
    private String preis2;
    private String preis3;
    private String color;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getTag() {
        return tag;
    }

    public void setTag(Date tag) {
        this.tag = tag;
    }

    public String getKennzeichnungen() {
        return kennzeichnungen;
    }

    public void setKennzeichnungen(String kennzeichnungen) {
        this.kennzeichnungen = kennzeichnungen;
    }

    public String getBeilagen() {
        return beilagen;
    }

    public void setBeilagen(String beilagen) {
        this.beilagen = beilagen;
    }

    public String getPreis1() {
        return preis1;
    }

    public void setPreis1(String preis1) {
        this.preis1 = preis1;
    }

    public String getPreis2() {
        return preis2;
    }

    public void setPreis2(String preis2) {
        this.preis2 = preis2;
    }

    public String getPreis3() {
        return preis3;
    }

    public void setPreis3(String preis3) {
        this.preis3 = preis3;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
