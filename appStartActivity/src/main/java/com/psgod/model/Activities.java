package com.psgod.model;

import java.util.List;

/**
 * Created by remilia on 2015/11/18.
 */
public class Activities {
    private List<ActivitiesAct> acts;
    private List<PhotoItem> replies;

    public List<ActivitiesAct> getActs() {
        return acts;
    }

    public void setActs(List<ActivitiesAct> acts) {
        this.acts = acts;
    }

    public List<PhotoItem> getReplies() {
        return replies;
    }

    public void setReplies(List<PhotoItem> replies) {
        this.replies = replies;
    }
}
