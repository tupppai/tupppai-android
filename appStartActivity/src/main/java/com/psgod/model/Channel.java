package com.psgod.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/7 0007.
 */
public class Channel implements Serializable{

    private List<PhotoItem> ask;
    private List<PhotoItem> replies;

    public Channel() {
        ask = new ArrayList<PhotoItem>();
        replies = new ArrayList<PhotoItem>();
    }

    public List<PhotoItem> getReplies() {
        return replies;
    }

    public void setReplies(List<PhotoItem> replies) {
        this.replies = replies;
    }

    public List<PhotoItem> getAsk() {
        return ask;
    }

    public void setAsk(List<PhotoItem> ask) {
        this.ask = ask;
    }
}
