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
import com.psgod.Utils;
import com.psgod.model.PhotoItem;
import com.psgod.model.User;
import com.psgod.ui.activity.SinglePhotoDetail;
import com.psgod.ui.widget.AvatarImageView;
import com.psgod.ui.widget.dialog.CarouselPhotoDetailDialog;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.List;

public class MyPageCollectionAdapter extends BaseAdapter {
	private static final String TAG = MyPageCollectionAdapter.class
			.getSimpleName();
	private Context mContext;
	private List<PhotoItem> mPhotoItems = new ArrayList<PhotoItem>();

	// UIL配置
	private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS_SMALL;
	private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

	public MyPageCollectionAdapter(Context context, List<PhotoItem> photoItems) {
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
		final PhotoItem photoItem = mPhotoItems.get(position);
		final ViewHolder mViewHolder;
		if (convertView == null) {
			mViewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_my_collection, null);
			mViewHolder.imageType = (ImageView) convertView
					.findViewById(R.id.image_type);
			mViewHolder.avatarIv = (AvatarImageView) convertView
					.findViewById(R.id.avatar_imgview);
			mViewHolder.mNicknaemTv = (TextView) convertView
					.findViewById(R.id.nickname_text);
			mViewHolder.mImageView = (ImageView) convertView
					.findViewById(R.id.item_image);
			mViewHolder.mAskDesc = (HtmlTextView) convertView
					.findViewById(R.id.desc_text);
			mViewHolder.mReplyCountTv = (TextView) convertView
					.findViewById(R.id.reply_count);
			mViewHolder.mLikeText = (TextView) convertView
					.findViewById(R.id.item_like_count_tv);
			convertView.setTag(mViewHolder);
		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}

		PsGodImageLoader imageLoader = PsGodImageLoader.getInstance();
		imageLoader.displayImage(photoItem.getAvatarURL(),
				mViewHolder.avatarIv, mAvatarOptions);
		mViewHolder.avatarIv.setUser(new User(photoItem)); // 设置点击头像跳转
		imageLoader.displayImage(photoItem.getImageURL(),
				mViewHolder.mImageView, mOptions);

		mViewHolder.mNicknaemTv.setText(photoItem.getNickname());
		mViewHolder.mAskDesc.setHtmlFromString(photoItem.getDesc(), true);
		mViewHolder.mReplyCountTv.setText("已有" + photoItem.getReplyCount()
				+ "个帮P，马上参与PK!");
		mViewHolder.mReplyCountTv.setVisibility(View.GONE);
		mViewHolder.mLikeText
				.setText(Integer.toString(photoItem.getLikeCount()));

		if (photoItem.getType() == PhotoItem.TYPE_ASK) {
			mViewHolder.imageType.setBackgroundResource(R.mipmap.top_yuantu);
			// mViewHolder.mReplyCountTv.setVisibility(View.VISIBLE);
		} else {
			mViewHolder.imageType.setBackgroundResource(R.mipmap.top_zuopin);
			// mViewHolder.mReplyCountTv.setVisibility(View.GONE);
		}

		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (photoItem.getType() == PhotoItem.TYPE_REPLY) {
					Utils.skipByObject(mContext,photoItem);
//					new CarouselPhotoDetailDialog(mContext,
//							photoItem.getAskId(), photoItem.getReplyId()).show();
				} else {
					if (photoItem.getReplyCount() > 0) {
						Utils.skipByObject(mContext,photoItem);
//						new CarouselPhotoDetailDialog(mContext,
//								photoItem.getAskId(),photoItem.getPid()).show();
					} else {
						SinglePhotoDetail.startActivity(mContext, photoItem);
					}
				}
			}
		});

		return convertView;
	}

	private static class ViewHolder {
		ImageView imageType;
		AvatarImageView avatarIv;
		TextView mNicknaemTv;
		ImageView mImageView;
		HtmlTextView mAskDesc;
		TextView mReplyCountTv;
		TextView mLikeText;
	}
}
