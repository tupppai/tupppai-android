package com.psgod.model;

/**
 * Created by remilia on 2015/11/18.
 */
public class ActivitiesAct {

    private String id;
    private String display_name;
    private String pc_pic;
    private String app_pic;
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public void setPc_pic(String pc_pic) {
        this.pc_pic = pc_pic;
    }

    public void setApp_pic(String app_pic) {
        this.app_pic = app_pic;
    }

    public String getId() {
        return id;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public String getPc_pic() {
        return pc_pic;
    }

    public String getApp_pic() {
        return app_pic;
    }
}
