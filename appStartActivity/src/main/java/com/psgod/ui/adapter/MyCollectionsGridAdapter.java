package com.psgod.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.psgod.PsGodImageLoader;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.model.PhotoItem;
import com.psgod.ui.activity.SinglePhotoDetail;
import com.psgod.ui.view.CircleImageView;
import com.psgod.ui.widget.dialog.CarouselPhotoDetailDialog;

import java.util.List;

/**
 * 
 * @author Rayal
 * 
 */
public class MyCollectionsGridAdapter extends BaseAdapter {
	public static final byte TYPE_ASK = 1;
	public static final byte TYPE_REPLY = 2;

	private Context mContext;
	private List<PhotoItem> mPhotoItems;

	// UIL配置
	private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS_SMALL;
	private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

	public MyCollectionsGridAdapter(Context context, List<PhotoItem> photoItems) {
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
		Object child = getItem(position);
		if (!(child instanceof PhotoItem)) {
			return null;
		}

		final PhotoItem photoItem = (PhotoItem) child;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_my_collection_grid, null);
			viewHolder.avatarIv = (CircleImageView) convertView
					.findViewById(R.id.item_my_collection_avatar_imageview);
			viewHolder.nameTv = (TextView) convertView
					.findViewById(R.id.item_my_collection_name_textview);
			viewHolder.imageView = (ImageView) convertView
					.findViewById(R.id.item_my_collection_imageview);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		PsGodImageLoader imageLoader = PsGodImageLoader.getInstance();
		imageLoader.displayImage(photoItem.getAvatarURL(), viewHolder.avatarIv,
				mAvatarOptions);
		imageLoader.displayImage(photoItem.getImageURL(), viewHolder.imageView,
				mOptions);

		viewHolder.nameTv.setText(photoItem.getNickname());

		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (photoItem.getType() == TYPE_ASK) {
					if (photoItem.getReplyCount() == 0) {
						SinglePhotoDetail.startActivity(mContext, photoItem);
					} else {
						new CarouselPhotoDetailDialog(mContext,
								photoItem.getAskId(),photoItem.getPid()).show();
					}
				} else if (photoItem.getType() == TYPE_REPLY) {
					// PhotoDetailActivity.startActivity(mContext,
					// photoItem.getAskId());
				}
			}
		});

		return convertView;
	}

	private static class ViewHolder {
		CircleImageView avatarIv;
		TextView nameTv;
		ImageView imageView;
	}
}
