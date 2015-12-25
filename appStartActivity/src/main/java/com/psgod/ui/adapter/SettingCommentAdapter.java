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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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
		PsGodImageLoader imageLoader = PsGodImageLoader.getInstance();
		imageLoader.displayImage(photoItem.getImageURL(),
				mViewHolder.mImageView, mOptions);

		mViewHolder.mNicknaemTv.setText(photoItem.getNickname());
		mViewHolder.mTimeTv.setText(getCommentTimeStr(photoItem.getCommentTime()));
		mViewHolder.mCommendContent.setText("你评论了ta: " + photoItem.getCommentContent());

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

	private String toLocalTime(String unix) {
		// Long timestamp = Long.parseLong(unix) * 1000;
		String date = new SimpleDateFormat("MM月dd日 HH:mm")
				.format(new java.util.Date(Long.parseLong(unix + "000")));
		return date;
	}

	private String getCommentTimeStr(Long mCommentTime) {
		StringBuffer sb = new StringBuffer();
		long time = System.currentTimeMillis() - (mCommentTime * 1000);
		long mill = (long) Math.ceil(time / 1000);// 秒前
		long minute = (long) Math.ceil(time / 60 / 1000.0f);// 分钟前
		long hour = (long) Math.ceil(time / 60 / 60 / 1000.0f);// 小时
		long day = (long) Math.ceil(time / 24 / 60 / 60 / 1000.0f);// 天前

		if (day - 1 > 0) {
			if (day > 7) {
				sb.append(toLocalTime(Long.toString(mCommentTime)));
			} else {
				sb.append(day + "天");
			}
		} else if (hour - 1 > 0) {
			if (hour >= 24) {
				sb.append("1天");
			} else {
				sb.append(hour + "小时");
			}
		} else if (minute - 1 > 0) {
			if (minute == 60) {
				sb.append("1小时");
			} else {
				sb.append(minute + "分钟");
			}
		} else if (mill - 1 > 0) {
			if (mill == 60) {
				sb.append("1分钟");
			} else {
				sb.append(mill + "秒");
			}
		} else {
			sb.append("刚刚");
		}
		if (!sb.toString().equals("刚刚") && (day <= 7)) {
			sb.append("前");
		}
		return sb.toString();
	}

}