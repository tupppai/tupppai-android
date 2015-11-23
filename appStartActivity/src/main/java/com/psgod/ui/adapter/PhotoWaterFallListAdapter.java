package com.psgod.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.psgod.R;
import com.psgod.model.PhotoItem;
import com.psgod.ui.view.PhotoWaterFallItemView;
import com.psgod.ui.view.PhotoWaterFallItemView.PhotoWaterFallListType;
import com.psgod.ui.widget.dialog.InprogressShareMoreDialog;

public class PhotoWaterFallListAdapter extends BaseAdapter {
	private Context mContext;
	private List<PhotoItem> mPhotoItems = new ArrayList<PhotoItem>();
	private PhotoWaterFallListType mPhotoType;
	private InprogressShareMoreDialog inprogressShareDialog;
	private int type = 0;

	public PhotoWaterFallListAdapter(Context context,
			List<PhotoItem> photoItems, PhotoWaterFallListType photoType) {
		mContext = context;
		mPhotoItems = photoItems;
		mPhotoType = photoType;
	}

	@Override
	public int getCount() {
		return mPhotoItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mPhotoItems.get(position);
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
		PhotoWaterFallItemView photoWaterFallItemView;
		final PhotoItem photoItem = mPhotoItems.get(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.widget_photo_water_fall_item_view, null);
			photoWaterFallItemView = (PhotoWaterFallItemView) convertView;
			photoWaterFallItemView.setType(type);
			photoWaterFallItemView.initPhotoWaterFallListType(mPhotoType);
		} else {
			photoWaterFallItemView = (PhotoWaterFallItemView) convertView;
			photoWaterFallItemView.setType(type);
			photoWaterFallItemView.initPhotoWaterFallListType(mPhotoType);
		}
		photoWaterFallItemView.setPhotoItem(photoItem);

		if (mPhotoType == PhotoWaterFallListType.INPROGRESS_COMPLETE) {
			photoWaterFallItemView
					.setOnLongClickListener(new OnLongClickListener() {

						@Override
						public boolean onLongClick(View arg0) {
							if (inprogressShareDialog == null) {
								inprogressShareDialog = new InprogressShareMoreDialog(
										mContext);
							}
							inprogressShareDialog
									.setPhotoItem(
											photoItem,
											InprogressShareMoreDialog.SHARE_TYPE_COMPLETE);
							if (inprogressShareDialog.isShowing()) {
								inprogressShareDialog.dismiss();
							} else {
								inprogressShareDialog.show();
							}
							return true;
						}
					});
		}

		return convertView;
	}

	public void setType(int type) {
		this.type = type;
	}

}
