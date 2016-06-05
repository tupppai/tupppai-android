package com.pires.wesee.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.pires.wesee.PsGodImageLoader;
import com.pires.wesee.Constants;
import com.pires.wesee.R;
import com.pires.wesee.Utils;
import com.pires.wesee.model.PhotoItem;
import com.pires.wesee.ui.activity.SinglePhotoDetail;

import java.util.List;

/**
 * 
 * @author Rayal
 * 
 */
public class AskGridAdapter extends BaseAdapter {
	public static final byte TYPE_ASK = 1;
	public static final byte TYPE_REPLY = 2;

	private Context mContext;
	private List<PhotoItem> mPhotoItems;

	// UIL配置
	private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS_SMALL;
	private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

	public AskGridAdapter(Context context, List<PhotoItem> photoItems) {
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
		AskGridViewHolder viewHolder;
		Object child = getItem(position);
		if (!(child instanceof PhotoItem)) {
			return null;
		}

		final PhotoItem photoItem = (PhotoItem) child;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_ask_grid, null);
			viewHolder = new AskGridViewHolder();
			viewHolder.mCountTextView = (TextView) convertView
					.findViewById(R.id.item_ask_count_tag);
			viewHolder.mImageView = (ImageView) convertView
					.findViewById(R.id.item_ask_imageview);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (AskGridViewHolder) convertView.getTag();
		}

		PsGodImageLoader imageLoader = PsGodImageLoader.getInstance();
		imageLoader.displayImage(photoItem.getImageURL(),
				viewHolder.mImageView, mOptions);
		viewHolder.mCountTextView.setVisibility(View.INVISIBLE);

		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (photoItem.getType() == PhotoItem.TYPE_ASK) {
					if (photoItem.getReplyCount() == 0) {
						SinglePhotoDetail.startActivity(mContext, photoItem);
					} else {
						Utils.skipByObject(mContext,photoItem);
//						new CarouselPhotoDetailDialog(mContext,photoItem.getAskId(), photoItem.getPid()).show();
					}
				} else if (photoItem.getType() == PhotoItem.TYPE_REPLY) {
					Utils.skipByObject(mContext,photoItem);

//					new CarouselPhotoDetailDialog(mContext,photoItem.getAskId(), photoItem.getPid()).show();
				}
			}
		});

		return convertView;
	}

	private static class AskGridViewHolder {
		TextView mCountTextView;
		ImageView mImageView;
	}
}
