package com.psgod.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2016/2/16 0016.
 */
public class SinglePhotoItem implements Serializable  {

    private PhotoItem mPhotoItem;
    private PhotoItem mAskPhotoItems;
    private List<PhotoItem> mReplyPhotoItems;

    public SinglePhotoItem(){}

    public SinglePhotoItem(PhotoItem mPhotoItem,
                           PhotoItem mAskPhotoItems,
                           List<PhotoItem> mReplyPhotoItems) {
        this.mPhotoItem = mPhotoItem;
        this.mAskPhotoItems = mAskPhotoItems;
        this.mReplyPhotoItems = mReplyPhotoItems;
    }

    public PhotoItem getPhotoItem() {
        return mPhotoItem;
    }

    public void setPhotoItem(PhotoItem mPhotoItem) {
        this.mPhotoItem = mPhotoItem;
    }

    public PhotoItem getAskPhotoItems() {
        return mAskPhotoItems;
    }

    public void setAskPhotoItems(PhotoItem mAskPhotoItems) {
        this.mAskPhotoItems = mAskPhotoItems;
    }

    public List<PhotoItem> getReplyPhotoItems() {
        return mReplyPhotoItems;
    }

    public void setReplyPhotoItems(List<PhotoItem> mReplyPhotoItems) {
        this.mReplyPhotoItems = mReplyPhotoItems;
    }
}
