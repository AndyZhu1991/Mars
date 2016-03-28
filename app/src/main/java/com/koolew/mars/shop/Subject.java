package com.koolew.mars.shop;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jinchangzhu on 3/25/16.
 */
public class Subject {

    @SerializedName("subject_id")
    private String subjectId;
    private float price;
    private String desc;
    private String icon;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }
}
