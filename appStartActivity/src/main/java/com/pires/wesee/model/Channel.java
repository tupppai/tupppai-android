package com.pires.wesee.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/7 0007.
 */
public class Channel implements Serializable{

    private List<PhotoItem> data;

    public Channel() {
        data = new ArrayList<PhotoItem>();
    }

    public List<PhotoItem> getData() {
        return data;
    }

    public void setData(List<PhotoItem> data) {
        this.data = data;
    }
}
