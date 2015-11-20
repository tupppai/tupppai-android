package com.psgod.ui.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.psgod.R;
import com.psgod.model.PhotoItem;
import com.psgod.ui.view.PhotoItemView;
import com.psgod.ui.view.PhotoItemView.PhotoListType;

/**
 * 不带评论的PhotoListAdapter 主要用于首页（热门和最近）和关注页
 * 
 */
public class PhotoListAdapter extends BaseAdapter {
	private Context mContext;
	private PhotoListType mPhotoListType;
	private List<PhotoItem> mPhotoItems;

	private boolean isHomePageFouce = false;

	public void setIsHomePageFouce(boolean isHomePageFouce) {
		this.isHomePageFouce = isHomePageFouce;
	}

	public PhotoListAdapter(Context context, PhotoListType photoListType,
			List<PhotoItem> photoItems) {
		mContext = context;
		mPhotoListType = photoListType;
		mPhotoItems = photoItems;
	}

	@Override
	public int getCount() {
		return mPhotoItems.size();
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
	public View getView(int position, View convertView, ViewGroup parent) {
		PhotoItemView photoItemView;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.widget_photo_item, null);
			photoItemView = (PhotoItemView) convertView;
			// photoItemView.initialize(mPhotoListType);
		} else {
			photoItemView = (PhotoItemView) convertView;
		}

		PhotoItem photoItem = (PhotoItem) getItem(position);
		photoItemView.setIsHomePageFocus(isHomePageFouce);
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
		return convertView;
	}
}