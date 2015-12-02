package com.psgod.ui.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Color;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.WeakReferenceHandler;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.EditAskDescRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.ui.activity.CarouselPhotoDetailActivity;
import com.psgod.ui.activity.SinglePhotoDetail;
import com.psgod.ui.fragment.InprogressPageAskFragment;
import com.psgod.ui.widget.dialog.CarouselPhotoDetailDialog;
import com.psgod.ui.widget.dialog.InprogressShareMoreDialog;

public class InprogressPageAskAdapter extends BaseAdapter implements
		android.os.Handler.Callback {

	private static final String TAG = InprogressPageAskAdapter.class
			.getSimpleName();
	private List<PhotoItem> mPhotoItems;
	private Context mContext;

	private InprogressShareMoreDialog inprogressShareDialog;

	// UIL配置
	private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS;
	private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

	// TODO 根据接口 更换photoitem类型为新的类型
	public InprogressPageAskAdapter(Context context, List<PhotoItem> mItems) {
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
					R.layout.item_my_profile_asks, null);
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
			viewHolder.descEdit = (EditText) convertView
					.findViewById(R.id.item_user_profile_asks_bottom_desc_edit);
			viewHolder.mEditImage = (ImageView) convertView
					.findViewById(R.id.item_edit_text);
			viewHolder.mEditView = (TextView) convertView
					.findViewById(R.id.item_edit_text_enable);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.timeTv.setText(photoItem.getUpdateTimeStr());
		viewHolder.descEdit.setText(photoItem.getDesc());
		viewHolder.descEdit.setTag(photoItem);
		viewHolder.mEditView.setTag(viewHolder);

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
		convertView.setTag(R.id.inprogress_item, photoItem);
		viewHolder.imagePanel.removeAllViews();
		ArrayList<PhotoItem> mReplyItems = (ArrayList<PhotoItem>) photoItem
				.getReplyItems();
		int mSize = mReplyItems.size();
		viewHolder.replyCount.setText("已有" + photoItem.getReplyCount() + "个作品");
		if (mSize > 0) {
			for (int i = 0; i < mSize; i++) {
				final PhotoItem replyPhotoItem = mReplyItems.get(i);

				ImageView mReplyIv = new ImageView(mContext);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						Utils.dpToPx(mContext, 84), Utils.dpToPx(mContext, 84));
				params.setMargins(Utils.dpToPx(mContext, 10), 0, 0, 0);
				mReplyIv.setLayoutParams(params);
				mReplyIv.setScaleType(ImageView.ScaleType.CENTER);
				mReplyIv.setTag(replyPhotoItem);
				// mReplyIv点击跳转到对应作品对应的详情页
				mReplyIv.setOnClickListener(replyClick);

				imageLoader.displayImage(mReplyItems.get(i).getImageURL(),
						mReplyIv, mOptions);
				viewHolder.imagePanel.addView(mReplyIv);

			}
			if (photoItem.getReplyCount() > mSize) {
				final PhotoItem replyPhotoItem = mReplyItems.get(mSize-1);

				LinearLayout.LayoutParams mTipParams = new LinearLayout.LayoutParams(
						Utils.dpToPx(mContext, 84), Utils.dpToPx(mContext, 84));
				mTipParams.setMargins(Utils.dpToPx(mContext, 10), 0, 0, 0);
				TextView mTipMoreText = new TextView (mContext);
				mTipMoreText.setText("查看更多");
				mTipMoreText.setTextColor(Color.parseColor("#7F4A4A4A"));
				mTipMoreText.setTextSize(11);
				mTipMoreText.setLayoutParams(mTipParams);
				mTipMoreText.setGravity(Gravity.CENTER);
				mTipMoreText.setTag(replyPhotoItem);
				mTipMoreText.setOnClickListener(replyClick);
				viewHolder.imagePanel.addView(mTipMoreText);

			}
			convertView.setOnClickListener(carouselSkipClick);
		} else {
			convertView.setOnClickListener(singleSkipClick);
		}

		viewHolder.mEditImage.setVisibility(View.VISIBLE);
		viewHolder.descEdit.setVisibility(View.VISIBLE);
		viewHolder.mEditView.setVisibility(View.GONE);
		viewHolder.descEdit.setEnabled(false);
		viewHolder.descEdit.setFocusableInTouchMode(false);

		// 点击编辑按钮
		viewHolder.mEditImage.setTag(viewHolder);
		viewHolder.mEditImage.setOnClickListener(editClick);

		// 点击确定
		viewHolder.mEditView.setOnClickListener(sureClick);

		convertView.setOnLongClickListener(itemLongClick);

		return convertView;
	}

	private OnLongClickListener itemLongClick = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View view) {
			PhotoItem photoItem = (PhotoItem) view.getTag(R.id.inprogress_item);
			if (inprogressShareDialog == null) {
				inprogressShareDialog = new InprogressShareMoreDialog(mContext);
			}
			inprogressShareDialog.setPhotoItem(photoItem,
					InprogressShareMoreDialog.SHARE_TYPE_ASK);
			if (inprogressShareDialog.isShowing()) {
				inprogressShareDialog.dismiss();
			} else {
				inprogressShareDialog.show();
			}
			return false;
		}
	};

	private OnClickListener editClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			final ViewHolder viewHolder = (ViewHolder) v.getTag();
			viewHolder.mEditImage.setVisibility(View.GONE);
			viewHolder.mEditView.setVisibility(View.VISIBLE);
			viewHolder.descEdit.setEnabled(true);
			viewHolder.descEdit.setFocusableInTouchMode(true);
			viewHolder.descEdit.requestFocus();
			viewHolder.descEdit.setSelection(viewHolder.descEdit.getText()
					.toString().length());

			// 唤起输入键盘 并输入框取得焦点
			InputMethodManager imm = (InputMethodManager) mContext
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(viewHolder.descEdit, 0);

			fixedThreadPool.execute(new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(300);
						Message msg = mHandler.obtainMessage();
						msg.obj = viewHolder.descEdit;
						mHandler.sendMessage(msg);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
	};

	private OnClickListener singleSkipClick = new OnClickListener() {

		@Override
		public void onClick(View view) {
			PhotoItem photoItem = (PhotoItem) view.getTag(R.id.inprogress_item);
			SinglePhotoDetail.startActivity(mContext, photoItem);

		}
	};

	private OnClickListener carouselSkipClick = new OnClickListener() {

		@Override
		public void onClick(View view) {
			PhotoItem photoItem = (PhotoItem) view.getTag(R.id.inprogress_item);
//			CarouselPhotoDetailActivity.startActivity(mContext, photoItem);
			new CarouselPhotoDetailDialog(mContext,photoItem.getAskId(),photoItem.getPid()).show();
		}
	};

	private OnClickListener replyClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			PhotoItem replyPhotoItem = (PhotoItem) v.getTag();
			CarouselPhotoDetailActivity.startActivity(mContext,
					replyPhotoItem.getAskId(), replyPhotoItem.getPid());
		}
	};

	private OnClickListener sureClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			ViewHolder holder = (ViewHolder) v.getTag();
			PhotoItem item = (PhotoItem) holder.descEdit.getTag();
			holder.mEditImage.setVisibility(View.VISIBLE);
			holder.mEditView.setVisibility(View.GONE);
			holder.descEdit.setEnabled(false);
			holder.descEdit.setFocusableInTouchMode(false);
			if (!holder.descEdit.getText().toString().equals(item.getDesc())) {
				EditAskDescRequest.Builder builder = new EditAskDescRequest.Builder()
						.setAskId(item.getAskId())
						.setDesc(holder.descEdit.getText().toString())
						.setListener(listener).setErrorListener(errorListener);
				EditAskDescRequest request = builder.build();
				request.setTag(TAG);

				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						mContext).getRequestQueue();
				requestQueue.add(request);
			}
		}
	};

	private Listener<Boolean> listener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			if (response) {
				Toast.makeText(mContext, "修改成功", Toast.LENGTH_SHORT).show();

			}
		}
	};

	private PSGodErrorListener errorListener = new PSGodErrorListener(
			InprogressPageAskFragment.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {

		}
	};

	private static class ViewHolder {
		TextView timeTv;
		TextView replyCount;
		LinearLayout imagePanel;
		RelativeLayout originPanelFirst;
		RelativeLayout originPanelSecond;
		ImageView originImageFirst;
		ImageView originImageSecond;
		EditText descEdit;
		ImageView mEditImage;
		TextView mEditView;
	}

	private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);
	private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);

	@Override
	public boolean handleMessage(Message msg) {
		EditText edit = (EditText) msg.obj;
		edit.setEnabled(true);
		edit.setFocusableInTouchMode(true);
		edit.requestFocus();
		return false;
	}

}
