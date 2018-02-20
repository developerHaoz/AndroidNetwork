package com.developerhaoz.androidnetwork.mvptest;

import com.google.gson.annotations.SerializedName;

/**
 * @author Haoz
 * @date 2018/1/29.
 */
public class MeiziBean {

    @SerializedName("_id")
    private String id;
    @SerializedName("url")
    private String imageUrl;
    @SerializedName("who")
    private String who;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }

    public MeiziBean(String imageUrl){
        this.imageUrl = imageUrl;
    }

    public MeiziBean(){
    }
}
