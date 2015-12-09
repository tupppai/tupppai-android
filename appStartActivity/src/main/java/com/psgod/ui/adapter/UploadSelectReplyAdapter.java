package com.psgod.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.MyInProgressDeleteRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.ui.activity.MainActivity;
import com.psgod.ui.activity.MultiImageSelectActivity;
import com.psgod.ui.widget.AvatarImageView;
import com.psgod.ui.widget.dialog.CustomDialog;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.List;

public class UploadSelectReplyAdapter extends BaseAdapter {
	private final static String TAG = UploadSelectReplyAdapter.class
			.getSimpleName();

	private Context mContext;
	private List<PhotoItem> mPhotoItems = new ArrayList<PhotoItem>();
	private String mChannelId;

	public void setmChannelId(String mChannelId) {
		this.mChannelId = mChannelId;
	}

	// UIL配置
	private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS_SMALL;
	private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

	public UploadSelectReplyAdapter(Context context, List<PhotoItem> photoItems) {
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
					R.layout.item_upload_select_reply, null);
			mViewHolder.avatarIv = (AvatarImageView) convertView
					.findViewById(R.id.avatar_imgview);
			mViewHolder.mNicknaemTv = (TextView) convertView
					.findViewById(R.id.nickname_tv);
			mViewHolder.mTimeTv = (TextView) convertView
					.findViewById(R.id.item_time);
			mViewHolder.mImageView = (ImageView) convertView
					.findViewById(R.id.reply_imageview);
			mViewHolder.mAskDesc = (HtmlTextView) convertView
					.findViewById(R.id.ask_desc_tv);
			mViewHolder.mReplyCountTv = (TextView) convertView
					.findViewById(R.id.reply_count_tv);
			mViewHolder.mDetailIv = (ImageView) convertView
					.findViewById(R.id.image_detail);
			mViewHolder.mDeleteIv = (ImageView) convertView
					.findViewById(R.id.delete_image);
			convertView.setTag(mViewHolder);
		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}
		ImageLoader imageLoader = ImageLoader.getInstance();
		imageLoader.displayImage(photoItem.getAvatarURL(),
				mViewHolder.avatarIv, mAvatarOptions);
		mViewHolder.avatarIv.setUserId(photoItem.getUid()); // 设置点击头像跳转
		imageLoader.displayImage(photoItem.getImageURL(),
				mViewHolder.mImageView, mOptions);

		mViewHolder.mNicknaemTv.setText(photoItem.getNickname());
		mViewHolder.mTimeTv.setText(photoItem.getUpdateTimeStr());
		mViewHolder.mAskDesc.setHtmlFromString(photoItem.getDesc(),true);
		mViewHolder.mReplyCountTv.setText("已有" + photoItem.getReplyCount()
				+ "个帮P，马上参与PK!");
		mViewHolder.mDeleteIv.setVisibility(View.GONE);

		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext,
						MultiImageSelectActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("SelectType",
						MultiImageSelectActivity.TYPE_REPLY_SELECT);
				bundle.putLong("AskId", photoItem.getAskId());
				bundle.putString("channel_id",mChannelId);
				intent.putExtras(bundle);
				mContext.startActivity(intent);
			}
		});

		mViewHolder.mDetailIv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, MainActivity.class);
				intent.putExtra(
						MainActivity.IntentParams.KEY_FRAGMENT_ID,
						MainActivity.IntentParams.VALUE_FRAGMENT_ID_INPROGRESSING);
				intent.putExtra(MainActivity.IntentParams.KEY_INPROGRESS_ID,
						MainActivity.IntentParams.VALUE_INPROGRESS_ID_REPLY);
				mContext.startActivity(intent);
			}
		});

		mViewHolder.mDeleteIv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				(new CustomDialog.Builder(mContext))
						.setMessage("你确定删除该条记录?")
						.setLeftButton("取消", null)
						.setRightButton("确定",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// 先删除本地的记录
										mPhotoItems.remove(position);
										UploadSelectReplyAdapter.this
												.notifyDataSetChanged();

										// 删除服务器的数据
										MyInProgressDeleteRequest.Builder builder = new MyInProgressDeleteRequest.Builder()
												.setId(photoItem.getAskId())
												.setListener(deleteListener)
												.setErrorListener(errorListener);

										MyInProgressDeleteRequest request = builder
												.build();
										request.setTag(TAG);
										RequestQueue requestQueue = PSGodRequestQueue
												.getInstance(
														mContext.getApplicationContext())
												.getRequestQueue();
										requestQueue.add(request);

									}
								}).create().show();
			}
		});

		return convertView;
	}

	private Listener<Boolean> deleteListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			if (response == true) {
				Toast.makeText(mContext, "删除成功", Toast.LENGTH_SHORT).show();
			}
		}
	};

	private PSGodErrorListener errorListener = new PSGodErrorListener(
			MyInProgressDeleteRequest.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {
			// TODO
		}
	};

	private static class ViewHolder {
		AvatarImageView avatarIv;
		TextView mNicknaemTv;
		TextView mTimeTv;
		ImageView mImageView;
		HtmlTextView mAskDesc;
		TextView mReplyCountTv;
		ImageView mDetailIv;
		ImageView mDeleteIv;
	}

}
