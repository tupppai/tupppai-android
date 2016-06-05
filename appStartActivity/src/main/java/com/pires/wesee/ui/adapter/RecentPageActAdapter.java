package com.pires.wesee.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pires.wesee.R;
import com.pires.wesee.model.PhotoItem;
import com.pires.wesee.ui.view.PhotoItemView;

import java.util.List;

/**
 * Created by remilia on 2015/11/18.
 */
public class RecentPageActAdapter extends MyBaseAdapter<PhotoItem> {
    public RecentPageActAdapter(Context context, List<PhotoItem> list) {
        super(context, list);
    }

    @Override
    View initView(int position, View convertView, ViewGroup parent) {

        PhotoItemView photoItemView;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.widget_photo_item, null);
            photoItemView = (PhotoItemView) convertView;
            // photoItemView.initialize(mPhotoListType);
        } else {
            photoItemView = (PhotoItemView) convertView;
        }
        photoItemView.setIsRecentAct(true);
        photoItemView.initialize(PhotoItemView.PhotoListType.RECENT_REPLY);

        photoItemView.setShowOrigin(false);

        photoItemView.setPhotoItem((PhotoItem) getItem(position));

        return convertView;
    }
}
