package com.psgod.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.PhotoItem;
import com.psgod.ui.activity.SinglePhotoDetail;
import com.psgod.ui.widget.dialog.CarouselPhotoDetailDialog;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.List;

public class UserProfileAsksAdapter extends BaseAdapter {

	private static final String TAG = UserProfileAsksAdapter.class
			.getSimpleName();
	private List<PhotoItem> mPhotoItems;
	private Context mContext;

	// UIL配置
	private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS;
	private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

	// TODO 根据接口 更换photoitem类型为新的类型
	public UserProfileAsksAdapter(Context context, List<PhotoItem> mItems) {
		mPhotoItems = mItems;
		mContext = context;
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
			return photoItem.getUid();
		}
		return -1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;

		final PhotoItem photoItem = mPhotoItems.get(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_user_profile_asks, null);
			viewHolder = new ViewHolder();
			viewHolder.timeTv = (TextView) convertView
					.findViewById(R.id.item_user_profile_asks_time);
			viewHolder.replyCount = (TextView) convertView
					.findViewById(R.id.item_user_profile_asks_reply_count);
			viewHolder.imagePanel = (LinearLayout) convertView
					.findViewById(R.id.item_user_profile_asks_middle_panel);
			viewHolder.originPanelFirst = (RelativeLayout) convertView
					.findViewById(R.id.origin_layout_first);
			viewHolder.originImageFirst = (ImageView) convertView
					.findViewById(R.id.item_user_profile_asks_origin_pic_first);
			viewHolder.originPanelSecond = (RelativeLayout) convertView
					.findViewById(R.id.origin_layout_second);
			viewHolder.originImageSecond = (ImageView) convertView
					.findViewById(R.id.item_user_profile_asks_origin_pic_second);
			viewHolder.descEdit = (HtmlTextView) convertView
					.findViewById(R.id.item_user_profile_asks_bottom_desc_edit);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.timeTv.setText(photoItem.getUpdateTimeStr());
		viewHolder.descEdit.setHtmlFromString(photoItem.getDesc(), true);

		ImageLoader imageLoader = ImageLoader.getInstance();
		if (photoItem.getUploadImagesList().size() == 1) {
			imageLoader.displayImage(
					photoItem.getUploadImagesList().get(0).mImageUrl,
					viewHolder.originImageFirst, mOptions);
			viewHolder.originPanelSecond.setVisibility(View.GONE);
		} else if (photoItem.getUploadImagesList().size() == 2) {
			viewHolder.originPanelSecond.setVisibility(View.VISIBLE);
			imageLoader.displayImage(
					photoItem.getUploadImagesList().get(0).mImageUrl,
					viewHolder.originImageFirst, mOptions);
			imageLoader.displayImage(
					photoItem.getUploadImagesList().get(1).mImageUrl,
					viewHolder.originImageSecond, mOptions);
		} else {
			// 出错
		}

		viewHolder.imagePanel.removeAllViews();
		final ArrayList<PhotoItem> mReplyItems = (ArrayList<PhotoItem>) photoItem
				.getReplyItems();
		int mSize = mReplyItems.size();
		viewHolder.replyCount.setText("已有" + Integer.toString(mSize) + "个作品");
		// 添加求p对应的作品
		if (mSize > 0) {
			for (int i = 0; i < mSize; i++) {
				final PhotoItem replyPhotoItem = mReplyItems.get(i);

				ImageView mReplyIv = new ImageView(mContext);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						Utils.dpToPx(mContext, 84), Utils.dpToPx(mContext, 84));
				params.setMargins(Utils.dpToPx(mContext, 10), 0, 0, 0);
				mReplyIv.setLayoutParams(params);
				mReplyIv.setScaleType(ImageView.ScaleType.CENTER);

				// mReplyIv点击跳转到对应作品对应的详情页
				mReplyIv.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						new CarouselPhotoDetailDialog(mContext,
								replyPhotoItem.getAskId(),
								replyPhotoItem.getReplyId()).show();
					}
				});

				imageLoader.displayImage(mReplyItems.get(i).getImageURL(),
						mReplyIv, mOptions);
				viewHolder.imagePanel.addView(mReplyIv);
			}

			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					new CarouselPhotoDetailDialog(mContext,
							photoItem.getAskId(),photoItem.getPid()).show();
				}
			});
		} else {
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					SinglePhotoDetail.startActivity(mContext, photoItem);
				}
			});
		}

		return convertView;
	}

	private static class ViewHolder {
		TextView timeTv;
		TextView replyCount;
		LinearLayout imagePanel;
		RelativeLayout originPanelFirst;
		RelativeLayout originPanelSecond;
		ImageView originImageFirst;
		ImageView originImageSecond;
		HtmlTextView descEdit;
	}
}
