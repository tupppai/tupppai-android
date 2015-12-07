package com.psgod.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2015/12/7 0007.
 */
public class Tupppai implements Serializable {

    private String id;
    private String display_name;
    private String pc_pic;
    private String app_pic;
    private String url;
    private String pid;
    private String icon;
    private String post_btn;
    private String description;
    private String category_type;
    private List<?> threads;

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

    public void setUrl(String url) {
        this.url = url;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setPost_btn(String post_btn) {
        this.post_btn = post_btn;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory_type(String category_type) {
        this.category_type = category_type;
    }

    public void setThreads(List<?> threads) {
        this.threads = threads;
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

    public String getUrl() {
        return url;
    }

    public String getPid() {
        return pid;
    }

    public String getIcon() {
        return icon;
    }

    public String getPost_btn() {
        return post_btn;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory_type() {
        return category_type;
    }

    public List<?> getThreads() {
        return threads;
    }
}
