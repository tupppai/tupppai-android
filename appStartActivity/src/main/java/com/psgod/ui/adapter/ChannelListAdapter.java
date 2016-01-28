package com.psgod.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.psgod.R;
import com.psgod.model.PhotoItem;
import com.psgod.ui.view.PhotoItemView;
import com.psgod.ui.view.PhotoItemView.PhotoListType;
import com.psgod.ui.widget.FollowImage;

import java.util.List;

/**
 * 不带评论的PhotoListAdapter 主要用于首页（热门和最近）和关注页
 */
public class ChannelListAdapter extends BaseAdapter {
    private Context mContext;
    private PhotoListType mPhotoListType;
    private List<PhotoItem> mPhotoItems;

    private boolean isHomePageFouce = false;
    private boolean isHomePageHot = false;

    public void setIsHomePageFouce(boolean isHomePageFouce) {
        this.isHomePageFouce = isHomePageFouce;
    }

    public void setIsHomePageHot(boolean isHomePageHot) {
        this.isHomePageHot = isHomePageHot;
    }

    public ChannelListAdapter(Context context, PhotoListType photoListType,
                              List<PhotoItem> photoItems) {
        mContext = context;
        mPhotoListType = photoListType;
        mPhotoItems = photoItems;
    }

    @Override
    public int getCount() {
        return mPhotoItems.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        if ((position < 0) || (position >= mPhotoItems.size())) {
            return null;
        } else {
            return mPhotoItems.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        Object obj = getItem(position);
        if (obj instanceof PhotoItem) {
            PhotoItem photoItem = (PhotoItem) obj;
            return photoItem.getPid();
        }
        return -1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 1 : 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PhotoItemView photoItemView = null;
        int type = getItemViewType(position);
        if (convertView == null) {
            switch (type) {
                case 0:
                    convertView = LayoutInflater.from(mContext).inflate(
                            R.layout.widget_photo_item, null);
                    photoItemView = (PhotoItemView) convertView;
                    break;
                case 1:
                    convertView = LayoutInflater.from(mContext).
                            inflate(R.layout.view_channal_head_reply, null);
                    break;
            }
            // photoItemView.initialize(mPhotoListType);
        } else {
            if (type == 0 && convertView instanceof PhotoItemView) {
                photoItemView = (PhotoItemView) convertView;
            }
        }
        if (type == 0 && convertView instanceof PhotoItemView) {
            position -= 1;
            PhotoItem photoItem = (PhotoItem) getItem(position);
            photoItemView.setIsHomePageFocus(isHomePageFouce);
            photoItemView.setIsHomePageHot(isHomePageHot);
            if ((mPhotoListType == PhotoListType.RECENT_REPLY)
                    || (mPhotoListType == PhotoListType.SINGLE_ASK)
                    || (mPhotoListType == PhotoListType.SINGLE_REPLY)) {
                photoItemView.initialize(mPhotoListType);
            } else if (photoItem.getType() == PhotoItem.TYPE_ASK) {
                photoItemView.initialize(PhotoListType.HOT_FOCUS_ASK);
            } else {
                photoItemView.initialize(PhotoListType.HOT_FOCUS_REPLY);
            }

            photoItemView.setPhotoItem((PhotoItem) getItem(position));

            photoItemView.setOnFollowChangeListener(onFollowChangeListener);// 关注接口回调
        }
        return convertView;
    }

    FollowImage.OnFollowChangeListener onFollowChangeListener = new FollowImage.OnFollowChangeListener() {
        @Override
        public void onFocusChange(long uid, boolean focusStatus) {
            for (int i = 0; i < mPhotoItems.size(); i++) {
                if (mPhotoItems.get(i).getUid() == uid) {
                    mPhotoItems.get(i).setmIsFollow(focusStatus);
                }
            }

            ChannelListAdapter.this.notifyDataSetChanged();
        }
    };

}
