package com.psgod.model;

import java.util.List;

/**
 * Created by remilia on 2015/11/18.
 */
public class Activities {
    private ActivitiesAct activity;
    private List<PhotoItem> replies;

    public ActivitiesAct getActs() {
        return activity;
    }

    public void setActs(ActivitiesAct activity) {
        this.activity = activity;
    }

    public List<PhotoItem> getReplies() {
        return replies;
    }

    public void setReplies(List<PhotoItem> replies) {
        this.replies = replies;
    }
}
