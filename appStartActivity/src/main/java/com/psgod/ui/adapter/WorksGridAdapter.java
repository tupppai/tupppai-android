package com.psgod.ui.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.model.PhotoItem;
import com.psgod.ui.activity.CarouselPhotoDetailActivity;

/**
 * 
 * @author Rayal
 * 
 */
public class WorksGridAdapter extends BaseAdapter {
	private Context mContext;
	private List<PhotoItem> mPhotoItems;

	// UIL配置
	private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS_SMALL;
	private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

	public WorksGridAdapter(Context context, List<PhotoItem> photoItems) {
		mContext = context;
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		final PhotoItem mPhotoItem = mPhotoItems.get(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_ask_grid, null);
			viewHolder = new ViewHolder();
			viewHolder.mCountTextView = (TextView) convertView
					.findViewById(R.id.item_ask_count_tag);
			viewHolder.mImageView = (ImageView) convertView
					.findViewById(R.id.item_ask_imageview);
			viewHolder.mTextView = (TextView) convertView
					.findViewById(R.id.item_like_count_text);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		ImageLoader imageLoader = ImageLoader.getInstance();
		imageLoader.displayImage(mPhotoItem.getImageURL(),
				viewHolder.mImageView, mOptions);

		viewHolder.mTextView
				.setText(Integer.toString(mPhotoItem.getLikeCount()));
		viewHolder.mTextView.setVisibility(View.VISIBLE);

		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				CarouselPhotoDetailActivity.startActivity(mContext, mPhotoItem);
			}
		});

		return convertView;
	}

	private static class ViewHolder {
		TextView mCountTextView;
		ImageView mImageView;
		TextView mTextView;
	}
}
