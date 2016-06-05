package com.pires.wesee.model;

import java.io.Serializable;

/**
 * Created by remilia on 2015/11/18.
 */
public class ActivitiesAct implements Serializable {

    private String id;
    private String image_url;
    private String banner_pic;
    private String url;
    private String ask_id;
    private String display_name;
    private String post_btn;
    private int type;

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getPost_btn() {
        return post_btn;
    }

    public void setPost_btn(String post_btn) {
        this.post_btn = post_btn;
    }

    public String getBanner_pic() {
        return banner_pic;
    }

    public void setBanner_pic(String banner_pic) {
        this.banner_pic = banner_pic;
    }

    public String getName() {
        return display_name;
    }

//    public void setName(String name) {
//        this.name = name;
//    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getAsk_id() {
        return ask_id;
    }

    public void setAsk_id(String ask_id) {
        this.ask_id = ask_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setId(String id) {
        this.id = id;
    }

//    public void setDisplay_name(String display_name) {
//        this.display_name = display_name;
//    }
//
//    public void setPc_pic(String pc_pic) {
//        this.pc_pic = pc_pic;
//    }
//
//    public void setApp_pic(String app_pic) {
//        this.app_pic = app_pic;
//    }

    public String getId() {
        return id;
    }


//    public String getDisplay_name() {
//        return display_name;
//    }
//
//    public String getPc_pic() {
//        return pc_pic;
//    }
//
//    public String getApp_pic() {
//        return app_pic;
//    }
}
