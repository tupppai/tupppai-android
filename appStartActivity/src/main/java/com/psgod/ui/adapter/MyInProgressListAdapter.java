package com.psgod.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.psgod.PsGodImageLoader;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.MyInProgressDeleteRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.ui.activity.SinglePhotoDetail;
import com.psgod.ui.widget.AvatarImageView;
import com.psgod.ui.widget.dialog.CarouselPhotoDetailDialog;
import com.psgod.ui.widget.dialog.CustomDialog;

import java.util.List;

/**
 * 
 * @author Rayal
 * 
 */
public class MyInProgressListAdapter extends BaseAdapter {

	private static final String TAG = MyInProgressListAdapter.class
			.getSimpleName();
	private Context mContext;
	private List<PhotoItem> mPhotoItemList;

	// UIL配置
	private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS;
	private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

	public MyInProgressListAdapter(Context context, List<PhotoItem> photoItems) {
		mContext = context;
		mPhotoItemList = photoItems;
	}

	@Override
	public int getCount() {
		return mPhotoItemList.size();
	}

	@Override
	public Object getItem(int position) {
		if ((position < 0) || (position >= mPhotoItemList.size())) {
			return null;
		} else {
			return mPhotoItemList.get(position);
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
		// final View finalConvertView;
		Object child = getItem(position);
		if (!(child instanceof PhotoItem)) {
			return null;
		}

		final PhotoItem photoItem = (PhotoItem) child;

		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_my_in_progress_list, null);
			viewHolder.avatarIv = (AvatarImageView) convertView
					.findViewById(R.id.item_my_in_progress_avatar_imgview);
			viewHolder.nameTv = (TextView) convertView
					.findViewById(R.id.item_my_in_progress_name_tv);
			viewHolder.timeTv = (TextView) convertView
					.findViewById(R.id.item_my_in_progress_time_tv);
			viewHolder.uploadWorkBtn = (ImageView) convertView
					.findViewById(R.id.item_my_in_progress_upload_work_btn);
			viewHolder.imageView = (ImageView) convertView
					.findViewById(R.id.item_my_in_progress_imageview);
			viewHolder.deleteBtn = (ImageView) convertView
					.findViewById(R.id.item_my_in_progress_delete_btn);
			viewHolder.deleteArea = (LinearLayout) convertView
					.findViewById(R.id.item_my_in_progress_delete_area);
			viewHolder.uploadArea = (LinearLayout) convertView
					.findViewById(R.id.item_my_in_progress_upload_area);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		// finalConvertView = convertView;

		PsGodImageLoader imageLoader = PsGodImageLoader.getInstance();
		imageLoader.displayImage(photoItem.getAvatarURL(), viewHolder.avatarIv,
				mAvatarOptions);
		imageLoader.displayImage(photoItem.getImageURL(), viewHolder.imageView,
				mOptions);

		viewHolder.nameTv.setText(photoItem.getNickname());
		viewHolder.timeTv.setText(photoItem.getUpdateTimeStr());

		// 点击上传作品
		viewHolder.uploadArea.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Bundle extras = new Bundle();

				// extras.putLong(Constants.IntentKey.PHOTO_ITEM_ID,
				// photoItem.getAskId());
				// extras.putInt(UploadImageActivity.INTENT_KEY_ACTIVITY_TYPE,
				// UploadImageActivity.TYPE_UPLOAD_REPLY);
				// ChoosePhotoActivity.startActivity(
				// (Activity)mContext,
				// ChoosePhotoActivity.FROM_ALBUM,
				// UploadImageActivity.class.getName(), extras);
			}
		});

		// 删除进行中
		viewHolder.deleteArea.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				(new CustomDialog.Builder(mContext))
						.setMessage("你确定删除该条记录?")
						.setLeftButton("取消", null)
						.setRightButton("确定",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// 先删除本地的记录
										mPhotoItemList.remove(position);
										MyInProgressListAdapter.this
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

										// //删除动画
										// AnimationSet animationSet = new
										// AnimationSet(true);
										// AlphaAnimation deleteAnimation = new
										// AlphaAnimation(1.0f, 0.2f);
										// deleteAnimation.setDuration(1000);
										// animationSet.addAnimation(deleteAnimation);
										// finalConvertView.startAnimation(animationSet);
										// deleteAnimation.setAnimationListener(new
										// AnimationListener() {
										// @Override
										// public void
										// onAnimationStart(Animation arg0) {
										// // TODO Auto-generated method stub
										//
										// }
										//
										// @Override
										// public void
										// onAnimationRepeat(Animation arg0) {
										// // TODO Auto-generated method stub
										//
										// }
										//
										// @Override
										// public void onAnimationEnd(Animation
										// arg0) {
										// // TODO Auto-generated method stub
										// // 动画结束时 先删除本地的记录
										// mPhotoItemList.remove(position);
										// MyInProgressListAdapter.this.notifyDataSetChanged();
										//
										// // TODO 删除服务器的数据
										// MyInProgressDeleteRequest.Builder
										// builder = new
										// MyInProgressDeleteRequest.Builder()
										// .setId(photoItem.getAskId())
										// .setListener(deleteListener)
										// .setErrorListener(errorListener);
										//
										// MyInProgressDeleteRequest request =
										// builder.build();
										// request.setTag(TAG);
										// RequestQueue requestQueue =
										// PSGodRequestQueue.getInstance(mContext.getApplicationContext())
										// .getRequestQueue();
										// requestQueue.add(request);
										// }
										// });
									}
								}).create().show();
			}
		});

		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (photoItem.getReplyCount() == 0) {
					SinglePhotoDetail.startActivity(mContext, photoItem);
				} else {
					Utils.skipByObject(mContext,photoItem);
//					new CarouselPhotoDetailDialog(mContext,
//							photoItem.getAskId(),photoItem.getPid()).show();
				}
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
		TextView nameTv;
		TextView timeTv;
		ImageView uploadWorkBtn;
		ImageView deleteBtn;
		ImageView imageView;
		LinearLayout deleteArea;
		LinearLayout uploadArea;
		// DeleteButtonListener deleteBtnListener;
	}

	// private class DeleteButtonListener implements OnClickListener {
	// int position;
	//
	// @Override
	// public void onClick(View view) {
	// (new
	// CustomDialog.Builder(mContext)).setMessage("你确定删除该条记录?").setLeftButton("取消",
	// null).setRightButton("确定", new DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// // 先删除本地的记录
	// // mPhotoItemList.remove(position);
	// // MyInProgressListAdapter.this.notifyDataSetChanged();
	//
	// // TODO 删除服务器的数据
	// }
	// }).create().show();
	// }
	// }
}
