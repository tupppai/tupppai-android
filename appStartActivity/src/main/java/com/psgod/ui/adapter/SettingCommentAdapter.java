package com.psgod.ui.adapter;

import java.util.ArrayList;
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
import com.psgod.ui.activity.SinglePhotoDetail;

public class SettingCommentAdapter extends BaseAdapter {
	private static final String TAG = SettingCommentAdapter.class
			.getSimpleName();
	private Context mContext;
	private List<PhotoItem> mPhotoItems = new ArrayList<PhotoItem>();

	// UIL配置
	private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS_SMALL;

	public SettingCommentAdapter(Context context, List<PhotoItem> photoItems) {
		mContext = context;
		mPhotoItems = photoItems;
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
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder mViewHolder;
		final PhotoItem photoItem = mPhotoItems.get(position);
		if (convertView == null) {
			mViewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_setting_comment, null);
			mViewHolder.mNicknaemTv = (TextView) convertView
					.findViewById(R.id.setting_commend_nickname);
			mViewHolder.mTimeTv = (TextView) convertView
					.findViewById(R.id.setting_commend_time);
			mViewHolder.mImageView = (ImageView) convertView
					.findViewById(R.id.setting_commend_image);
			mViewHolder.mCommendContent = (TextView) convertView
					.findViewById(R.id.setting_commend_content);
			convertView.setTag(mViewHolder);
		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}
		ImageLoader imageLoader = ImageLoader.getInstance();
		imageLoader.displayImage(photoItem.getImageURL(),
				mViewHolder.mImageView, mOptions);

		mViewHolder.mNicknaemTv.setText(photoItem.getNickname());
		mViewHolder.mTimeTv.setText(photoItem.getUpdateTimeStr());
		mViewHolder.mCommendContent.setText("您评论了ta: " + photoItem.getCommentContent());

		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SinglePhotoDetail.startActivity(mContext, photoItem);
			}
		});

		return convertView;
	}

	private static class ViewHolder {
		TextView mNicknaemTv;
		TextView mTimeTv;
		ImageView mImageView;
		TextView mCommendContent;
	}

}
