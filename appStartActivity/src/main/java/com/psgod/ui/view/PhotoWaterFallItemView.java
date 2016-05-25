package com.psgod.ui.view;

/**
 * 瀑布流图片展示 v2.0
 * 
 * @author ZouMengyuan
 *
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dodowaterfall.widget.ScaleImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.psgod.PsGodImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.WeakReferenceHandler;
import com.psgod.model.PhotoItem;
import com.psgod.model.User;
import com.psgod.ui.activity.CommentListActivity;
import com.psgod.ui.activity.PhotoBrowserActivity;
import com.psgod.ui.activity.SinglePhotoDetail;
import com.psgod.ui.widget.AvatarImageView;
import com.psgod.ui.widget.OriginImageLayout;
import com.psgod.ui.widget.dialog.CarouselPhotoDetailDialog;
import com.psgod.ui.widget.dialog.PSDialog;
import com.psgod.ui.widget.dialog.ShareMoreDialog;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PhotoWaterFallItemView extends RelativeLayout implements Handler.Callback {
	private static final String TAG = PhotoWaterFallItemView.class
			.getSimpleName();

	private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS_SMALL;
	private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

	private AnimateFirstDisplayListener mAnimateFirstListener;

	private int type = 0;

	private Context mContext;
	private PhotoItem mPhotoItem;
	private PhotoWaterFallListType mType;

	private AvatarImageView mAvatarIv;
	private TextView mNameTv;
	private TextView mNameRecentTv;
	private TextView mTimeRecentTv;
	private TextView mTimeTv;
	private ScaleImageView imageView;
	private TextView askDescTv;
	private ImageView replyImage;
	private TextView likeTextView;
	private TextView workLikeTextView;
	private ImageView mRecentAskMultiSignImage;

	private RelativeLayout mUserInfoLayout;
	private RelativeLayout mDescLayout;
	private RelativeLayout mReplyLayout;
	private RelativeLayout mLikeLayout;
	private RelativeLayout mLikeWhitelayout;

	private PSDialog mPsDialog;

	private TextView mWaterFallComment;
//	private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);
//	private ViewEnabledRunnable mViewEnabledRunnable = new ViewEnabledRunnable(
//			this);

	private RelativeLayout mImageArea;
	private ImageView mImageViewLeft;
	private ImageView mImageViewRight;
	private ShareMoreDialog mShareMoreDialog;

	@Override
	public boolean handleMessage(Message msg) {
		return false;
	}

	/**
	 * photowaterfallitemview类型：
	 * 
	 * INPROGRESS_ASK 进行中—求P INPROGRESS_COMPLETE 进行中-已完成
	 * 
	 */
	// TODO 根据功能区分
	public static enum PhotoWaterFallListType {
		INPROGRESS_COMPLETE, RECENT_ASK, USER_PROFILE_WORKS, ALL_WORK, USER_PROFILE_ASK
	}

	public PhotoWaterFallItemView(Context context) {
		super(context);
		mContext = context;
	}

	public PhotoWaterFallItemView(Context context, AttributeSet attribute) {
		this(context, attribute, 0);
		mContext = context;
	}

	public PhotoWaterFallItemView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	public void initPhotoWaterFallListType(PhotoWaterFallListType type) {
		mType = type;
		initViews();
		setViewByListType(type);
		initListener();
	}

	/**
	 * 初始化视图
	 */
	private void initViews() {
		mAvatarIv = (AvatarImageView) this
				.findViewById(R.id.item_avatar_imageview);
		mNameTv = (TextView) this.findViewById(R.id.nickname_tv);
		mNameRecentTv = (TextView) this.findViewById(R.id.nickname_tv_recent);
		mTimeRecentTv = (TextView) this.findViewById(R.id.time_tv_recent);
		mUserInfoLayout = (RelativeLayout) this
				.findViewById(R.id.user_info_layout);
		imageView = (ScaleImageView) this
				.findViewById(R.id.inprogress_ask_item_image);
		likeTextView = (TextView) this.findViewById(R.id.like_count_text);
		askDescTv = (TextView) this.findViewById(R.id.ask_desc_tv);
		mDescLayout = (RelativeLayout) this.findViewById(R.id.desc_layout);
		replyImage = (ImageView) this.findViewById(R.id.reply_image);
		mReplyLayout = (RelativeLayout) this.findViewById(R.id.reply_layout);
		workLikeTextView = (TextView) this.findViewById(R.id.like_count_tv);
		mLikeLayout = (RelativeLayout) this.findViewById(R.id.work_like_layout);
		mLikeWhitelayout = (RelativeLayout) this
				.findViewById(R.id.like_count_layout);
		mRecentAskMultiSignImage = (ImageView) this.findViewById(R.id.recent_multi_image_sign);

		mWaterFallComment = (TextView) this.findViewById(R.id.water_fall_simple_type_photo_item_comment_tv);
		mWaterFallComment.setOnClickListener(commentListener);

		mImageArea = (RelativeLayout) this
				.findViewById(R.id.photo_item_image_area);
	}

	// 配置图片显示细节,更新数据
	public void setPhotoItem(PhotoItem photoItem, PhotoWaterFallListType type) {
		mPhotoItem = photoItem;
		mAvatarIv.setUser(new User(mPhotoItem));

		// 更新图片
		final PsGodImageLoader imageLoader = PsGodImageLoader.getInstance();
		imageLoader.displayImage(mPhotoItem.getAvatarURL(), mAvatarIv,
				mAvatarOptions, mAnimateFirstListener);
		imageView.setImageWidth(photoItem.getImageWidth());
		imageView.setImageHeight(photoItem.getImageHeight());
		imageLoader.displayImage(mPhotoItem.getImageURL(), imageView, mOptions,
				mAnimateFirstListener);

		mNameTv.setText(mPhotoItem.getNickname());
		mNameRecentTv.setText(mPhotoItem.getNickname());
		mTimeRecentTv.setText(mPhotoItem.getUpdateTimeStr());
		askDescTv.setText(photoItem.getDesc());
		likeTextView.setText(Integer.toString(mPhotoItem.getLikeCount()));
		workLikeTextView.setText(Integer.toString(mPhotoItem.getLikeCount()));

		if ((type == PhotoWaterFallListType.RECENT_ASK) || (type == PhotoWaterFallListType.USER_PROFILE_ASK)) {
			if (mPhotoItem.getUploadImagesList().size() == 2) {
				mRecentAskMultiSignImage.setVisibility(View.VISIBLE);
			} else {
				mRecentAskMultiSignImage.setVisibility(View.GONE);
			}
		}

		String textCommentCount = Utils.getCountDisplayText(mPhotoItem
				.getCommentCount());
		mWaterFallComment.setText(textCommentCount);


	}

	/**
	 * 根据不同的复用类型设置View的显示(进行中已完成)
	 */
	private void setViewByListType(PhotoWaterFallListType type) {
		switch (type) {
		case INPROGRESS_COMPLETE:
			mLikeWhitelayout.setVisibility(View.VISIBLE);
			break;
		case USER_PROFILE_WORKS:
			mLikeWhitelayout.setVisibility(View.VISIBLE);
			break;
		case RECENT_ASK:
			mUserInfoLayout.setVisibility(View.VISIBLE);
			mNameRecentTv.setVisibility(View.VISIBLE);
			mTimeRecentTv.setVisibility(View.VISIBLE);
			mDescLayout.setVisibility(View.VISIBLE);
			mReplyLayout.setVisibility(View.VISIBLE);
			break;
		case ALL_WORK:
			mUserInfoLayout.setVisibility(View.VISIBLE);
			mNameTv.setVisibility(View.VISIBLE);
			mLikeLayout.setVisibility(View.VISIBLE);
			break;
		case USER_PROFILE_ASK:
			break;
		}
	}

	private void initListener() {
		if (type == 0) {
			this.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// 拥有回复作品
					boolean havingReplies = (mPhotoItem != null && mPhotoItem
							.getReplyCount() > 0);

					if (havingReplies) {
						Utils.skipByObject(mContext,mPhotoItem);
//						new CarouselPhotoDetailDialog(mContext,mPhotoItem.getAskId(),
//								mPhotoItem.getPid(),mPhotoItem.getCategoryId()).show();
					}

					if (!havingReplies) {
						SinglePhotoDetail.startActivity(mContext, mPhotoItem);
					}

				}
			});

			replyImage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mPsDialog == null) {
						mPsDialog = new PSDialog(mContext);
					}

					mPsDialog.setPhotoItem(mPhotoItem);
					if (mPsDialog.isShowing()) {
						mPsDialog.dismiss();
					} else {
						mPsDialog.show();
					}
				}
			});
		} else if (type == 1) {
			this.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// 拥有回复作品
					boolean havingReplies = (mPhotoItem != null && mPhotoItem
							.getReplyCount() > 0);

//					SinglePhotoDetail.startActivity(mContext, mPhotoItem);
					Utils.skipByObject(mContext,mPhotoItem);
//					new CarouselPhotoDetailDialog(mContext,mPhotoItem.getAskId(),mPhotoItem.getPid()
//					,mPhotoItem.getCategoryId()).show();
				}
			});

			replyImage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mPsDialog == null) {
						mPsDialog = new PSDialog(mContext);
					}

					mPsDialog.setPhotoItem(mPhotoItem);
					if (mPsDialog.isShowing()) {
						mPsDialog.dismiss();
					} else {
						mPsDialog.show();
					}
				}
			});
		}

	}

	// 评论 跳转到全部评论页
	private OnClickListener commentListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(mContext, CommentListActivity.class);
			intent.putExtra(Constants.IntentKey.PHOTO_ITEM, mPhotoItem);
			mContext.startActivity(intent);
			setEnabled(false);
			//mHandler.postDelayed(mViewEnabledRunnable, 1000);
		}
	};

	// 点击图片的跳转
	/**
	 * 非详情页时 （有作品）跳转到CarouselPhotoDetailActivity
	 * （没有作品）跳转到RecentPhotoDetailActivity
	 */
//	private OnClickListener imageOnClickListener = new OnClickListener() {
//		@Override
//		public void onClick(View v) {
//			if ((mPhotoItem.getType() == 1 && mPhotoItem.getReplyCount() == 0) || isRecentAct) {
//				SinglePhotoDetail.startActivity(mContext, mPhotoItem);
//				setEnabled(false);
//				//mHandler.postDelayed(mViewEnabledRunnable, 1000);
//			} else {
//				Utils.skipByObject(mContext, mPhotoItem);
////                new CarouselPhotoDetailDialog(mContext, mPhotoItem.getAskId(), mPhotoItem.getPid()
////                        , mPhotoItem.getCategoryId()).show();
//			}
//		}
//	};

	//动态页面只跳转到详情
	private OnClickListener imageOnClickListener2 = new OnClickListener() {
		@Override
		public void onClick(View v) {
			SinglePhotoDetail.startActivity(mContext, mPhotoItem);
			setEnabled(false);
			//mHandler.postDelayed(mViewEnabledRunnable, 1000);
		}
	};

	// 长按图片Listener
	private OnLongClickListener imageOnLongClickListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			if (mShareMoreDialog == null) {
				mShareMoreDialog = new ShareMoreDialog(mContext);
			}
			mShareMoreDialog.setPhotoItem(mPhotoItem);
			if (mShareMoreDialog.isShowing()) {
				mShareMoreDialog.dismiss();
			} else {
				mShareMoreDialog.show();
			}
			return true;
		}
	};


	// 详情页，点击图片 预览
	private OnClickListener imageBrowserListener2 = new OnClickListener() {
		@Override
		public void onClick(View view) {
			Intent intent = new Intent(mContext, PhotoBrowserActivity.class);
			intent.putExtra(Constants.IntentKey.PHOTO_PATH, view.getTag()
					.toString());
			intent.putExtra(Constants.IntentKey.ASK_ID, mPhotoItem.getAskId());
			intent.putExtra(Constants.IntentKey.PHOTO_ITEM_ID,
					mPhotoItem.getPid());
//			intent.putExtra(Constants.IntentKey.PHOTO_ITEM_TYPE,
//					(mPhotoItem.getType() == TYPE_ASK) ? "ask" : "reply");
			mContext.startActivity(intent);
		}
	};

	private OnClickListener imageBrowserListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent(mContext, PhotoBrowserActivity.class);
			intent.putExtra(Constants.IntentKey.PHOTO_PATH,
					mPhotoItem.getImageURL());
			intent.putExtra(Constants.IntentKey.ASK_ID, mPhotoItem.getAskId());
			intent.putExtra(Constants.IntentKey.PHOTO_ITEM_ID,
					mPhotoItem.getPid());
//			intent.putExtra(Constants.IntentKey.PHOTO_ITEM_TYPE,
//					(mPhotoItem.getType() == TYPE_ASK) ? "ask" : "reply");
			mContext.startActivity(intent);
		}
	};

//	private static class ViewEnabledRunnable implements Runnable {
//		private WeakReference<PhotoItemView> ref;
//
//		public ViewEnabledRunnable(PhotoWaterFallItemView view) {
//			ref = new WeakReference<PhotoItemView>(view);
//		}
//
//		@Override
//		public void run() {
//			PhotoItemView view = ref.get();
//			if (view != null) {
//				view.setEnabled(true);
//			}
//		}
//	}



	/**
	 * 图片首次出现时的动画
	 * 
	 * @author rayalyuan
	 */
	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {
		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}

	public void setType(int type) {
		this.type = type;
	}

}
