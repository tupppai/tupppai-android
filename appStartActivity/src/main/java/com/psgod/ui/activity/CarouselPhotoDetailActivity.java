package com.psgod.ui.activity;

/**
 * v2.0
 * 轮播 照片详情页 有作品回复
 * @author brandwang
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.psgod.BitmapUtils;
import com.psgod.Constants;
import com.psgod.CustomToast;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.WeakReferenceHandler;
import com.psgod.eventbus.ImgRefreshEvent;
import com.psgod.eventbus.MyPageRefreshEvent;
import com.psgod.eventbus.NetEvent;
import com.psgod.model.ImageData;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.ActionLikeRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.PhotoReplyRequest;
import com.psgod.ui.adapter.HotPhotoDetailAdapter;
import com.psgod.ui.fragment.PhotoDetailFragment;
import com.psgod.ui.view.BackGroundImage;
import com.psgod.ui.widget.AvatarImageView;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;
import com.psgod.ui.widget.dialog.PSDialog;

import de.greenrobot.event.EventBus;

public class CarouselPhotoDetailActivity extends PSGodBaseActivity implements
		Handler.Callback {
	private static final String TAG = CarouselPhotoDetailActivity.class
			.getSimpleName();

	private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS;
	private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;
	private Context mContext;

	private LinearLayout mTabLayoutLeft;
	private LinearLayout mTabLayoutRight;
	private TextView mTabNumLeft;
	private TextView mTabNumRight;
	private TextView mTabTxtLeft;
	private TextView mTabTxtRight;
	private FrameLayout mPhotoContainer;
	private ViewPager mViewPager;
	private ImageView mCursor;

	// 顶部tab宽度
	private int mTabItemWidth = 0;
	// 上方被选中tab index
	private int mTabSelectedIndex = 1;
	private List<PhotoItem> mPhotoItems;
	private PhotoItem mClickPhotoitem = null;
	private PhotoItem mOriginPhotoitem = null;
	private PhotoItem mCurrentPhotoitem = null;
	// 照片轮播详情fragments
	private ArrayList<PhotoDetailFragment> mPhotoDetailFragments = new ArrayList<PhotoDetailFragment>();
	// 原帖id
	private long mId = -1;
	// 当前查看的作品的id
	private long mCurrentWorkId = -1;
	// 原图数量
	private int mAskImageCount = 0;
	// 当前展示的location
	private int mCurrentPosition = 0;

	// 图片信息
	private RelativeLayout mUserInfoRelativeLayout;
	private AvatarImageView mUserAvatar;
	private TextView mUserNickName;
	private TextView mCreated;
	private HtmlTextView mPhotoDesc;
	private ImageView mBackBtnIv;
	private ImageButton mLikeBtn;
	private ImageButton mBangBtn;

	private BackGroundImage mBackgroundImg;
	private Drawable[] mDrawableLists;

	HotPhotoDetailAdapter mAdapter;

	private PSDialog mPsDialog;

	private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);
	private ExecutorService threadPool = Executors.newFixedThreadPool(1);

	// 传photoitem参数的启动函数
	public static void startActivity(Context context, PhotoItem photoItem) {
		if (photoItem != null) {
			Intent intent = new Intent(context,
					CarouselPhotoDetailActivity.class);
			intent.putExtra(Constants.IntentKey.PHOTO_ITEM, photoItem);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
	}

	// 传askid replyid参数的启动函数
	public static void startActivity(Context context, Long askId, Long replyId) {
		if (askId != null) {
			Intent intent = new Intent(context,
					CarouselPhotoDetailActivity.class);
			intent.putExtra(Constants.IntentKey.ASK_ID, askId);
			intent.putExtra(Constants.IntentKey.REPLY_ID, replyId);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
	}

	boolean isOnScroll = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hot_photo_detail);
		EventBus.getDefault().register(this);
		mContext = this;

		mTabItemWidth = Constants.WIDTH_OF_SCREEN / 6;
		mPhotoItems = new ArrayList<PhotoItem>();

		// 传递photoitem进行启动
		if (getIntent().hasExtra(Constants.IntentKey.PHOTO_ITEM)) {
			Object object = getIntent().getSerializableExtra(
					Constants.IntentKey.PHOTO_ITEM);
			if (!(object instanceof PhotoItem)) {
				Utils.showDebugToast(TAG + ".onCreate() photo item is null");
				return;
			}

			// 获取点击的图片
			mClickPhotoitem = (PhotoItem) object;
			// 获取点击的图片求P的id
			mId = mClickPhotoitem.getAskId();
			// 获取当前查看作品的id
			if (mClickPhotoitem.getType() == PhotoItem.TYPE_ASK) {
				mCurrentWorkId = -1;
			} else {
				mCurrentWorkId = mClickPhotoitem.getPid();
			}
		} else {
			// 传递ask id/reply id 进行启动
			Long askid = getIntent().getLongExtra(Constants.IntentKey.ASK_ID,
					-1);
			if (askid == -1) {
				return;
			}

			mId = askid;
			mCurrentWorkId = getIntent().getLongExtra(
					Constants.IntentKey.REPLY_ID, -1);
		}

		mTabLayoutLeft = (LinearLayout) findViewById(R.id.hot_photo_detail_tab1_layout);
		mTabLayoutRight = (LinearLayout) findViewById(R.id.hot_photo_detail_tab2_layout);
		mTabNumLeft = (TextView) findViewById(R.id.hot_photo_detail_tab1_num_tv);
		mTabNumRight = (TextView) findViewById(R.id.hot_photo_detail_tab2_num_tv);
		mTabTxtLeft = (TextView) findViewById(R.id.hot_photo_detail_tab1_tv);
		mTabTxtRight = (TextView) findViewById(R.id.hot_photo_detail_tab2_tv);

		mPhotoContainer = (FrameLayout) findViewById(R.id.hot_photo_detail_photo_info);

		mViewPager = (ViewPager) findViewById(R.id.hot_photo_detail_viewpager);
		mViewPager.setOffscreenPageLimit(3);
		mViewPager.setPageMargin(Utils.dpToPx(getApplicationContext(), 5));

		mCursor = (ImageView) findViewById(R.id.hot_photo_detail_cursor);

		mPhotoContainer.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_MOVE){
					isOnScroll = true;
				}else{
					isOnScroll = false;
				}
				return mViewPager.dispatchTouchEvent(event);
			}
		});

		mUserInfoRelativeLayout = (RelativeLayout) findViewById(R.id.fragment_hot_detail_photo_info);
		mUserAvatar = (AvatarImageView) findViewById(R.id.fragment_hot_detail_user_avatar);
		mUserNickName = (TextView) findViewById(R.id.fragment_hot_detail_user_nickname);
		mCreated = (TextView) findViewById(R.id.fragment_hot_detail_created);
		mPhotoDesc = (HtmlTextView) findViewById(R.id.fragment_hot_detail_desc);
		mBackBtnIv = (ImageView) findViewById(R.id.hot_photo_detail_btn_back);
		mLikeBtn = (ImageButton) findViewById(R.id.fragment_hot_like_btn);
		mBangBtn = (ImageButton) findViewById(R.id.fragment_bang_btn);
		mBackgroundImg = (BackGroundImage) findViewById(R.id.backgraound_img);

		dialog = new CustomProgressingDialog(this);

		// 初始化数据
		initData();
		// 初始化事件监听
		initListener();

	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
		RequestQueue requestQueue = PSGodRequestQueue.getInstance(this)
				.getRequestQueue();
		requestQueue.cancelAll(TAG);
	}

	private void initListener() {
		// 回退按钮
		mBackBtnIv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		// 原图 帮按钮
		mBangBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mPsDialog == null) {
					mPsDialog = new PSDialog(mContext);
				}

				mPsDialog.setPhotoItem(mCurrentPhotoitem);
				if (mPsDialog.isShowing()) {
					mPsDialog.dismiss();
				} else {
					mPsDialog.show();
				}
			}
		});
	}

	private CustomProgressingDialog dialog;

	// TODO 分页
	private void initData() {
		if (dialog != null && !dialog.isShowing()) {
			dialog.show();
		}

		int mPage = 1;

		PhotoReplyRequest.Builder builder = new PhotoReplyRequest.Builder()
				.setId(mId).setPid(mCurrentWorkId).setPage(mPage)
				.setListener(initDataListener).setErrorListener(errorListener);

		PhotoReplyRequest request = builder.build();
		request.setTag(TAG);
		RequestQueue requestQueue = PSGodRequestQueue.getInstance(
				getApplicationContext()).getRequestQueue();
		requestQueue.add(request);
	}

	ErrorListener errorListener = new ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
			CustomToast.showError(mContext, "数据请求失败", Toast.LENGTH_LONG);
		}
	};

	// 初始化数据函数回调
	Listener<List<PhotoItem>> initDataListener = new Listener<List<PhotoItem>>() {
		@Override
		public void onResponse(List<PhotoItem> items) {
			mPhotoItems.clear();
			mAskImageCount = 0;

			// 如果是求助需要做一些处理
			mOriginPhotoitem = items.get(0);
			if (mOriginPhotoitem.getType() == PhotoItem.TYPE_ASK) {
				for (int i = 0; i < mOriginPhotoitem.getUploadImagesList()
						.size(); i++) {
					PhotoItem tmpPhotoItem = mOriginPhotoitem.clone();

					ImageData image = mOriginPhotoitem.getUploadImagesList()
							.get(i);
					tmpPhotoItem.setImageURL(image.mImageUrl);
					tmpPhotoItem.setImageHeight(image.mImageHeight);
					tmpPhotoItem.setImageWidth(image.mImageWidth);

					mPhotoItems.add(tmpPhotoItem);
					mAskImageCount++;
				}

				items.remove(0);
			}
			mPhotoItems.addAll(items);
			final int length = mPhotoItems.size();

			ImageLoader imageLoader = ImageLoader.getInstance();
			mDrawableLists = new BitmapDrawable[length];
			for (int i = 0; i < length; i++) {
				final String mImagePath = mPhotoItems.get(i).getImageURL();
				if (i == 0) {
					// 将图片毛玻璃化处理后作为背景图
					imageLoader.loadImage(mImagePath,
							new ImageLoadingListener() {
								@Override
								public void onLoadingStarted(String arg0,
										View arg1) {
									// TODO Auto-generated method stub
								}

								@Override
								public void onLoadingFailed(String arg0,
										View arg1, FailReason arg2) {
									// TODO Auto-generated method stub
								}

								@SuppressWarnings("deprecation")
								@Override
								public void onLoadingComplete(String arg0,
										View arg1, Bitmap bitmap) {
									Drawable drawable = new BitmapDrawable(
											getResources(), BitmapUtils
													.getBlurBitmap(bitmap));
									mBackgroundImg
											.setBackgroundDrawable(drawable);
									mDrawableLists[0] = drawable;
								}

								@Override
								public void onLoadingCancelled(String arg0,
										View arg1) {
									// TODO Auto-generated method stub
								}
							});
				} else {
					// 将图片毛玻璃化处理后作为背景图
					imageLoader.loadImage(mImagePath,
							new ImageLoadingListener() {
								@Override
								public void onLoadingStarted(String arg0,
										View arg1) {
									// TODO Auto-generated method stub
								}

								@Override
								public void onLoadingFailed(String arg0,
										View arg1, FailReason arg2) {
									// TODO Auto-generated method stub
								}

								@SuppressWarnings("deprecation")
								@Override
								public void onLoadingComplete(String arg0,
										View arg1, Bitmap bitmap) {
									Drawable drawable = new BitmapDrawable(
											getResources(), BitmapUtils
													.getBlurBitmap(bitmap));
									for (int j = 0; j < length; j++) {
										if (mPhotoItems.get(j).getImageURL()
												.equals(arg0)) {
											mDrawableLists[j] = drawable;
										}
									}
									mBackgroundImg
											.setmDrawableLists(mDrawableLists);

								}

								@Override
								public void onLoadingCancelled(String arg0,
										View arg1) {
									// TODO Auto-generated method stub
								}
							});
				}
			}

			// 根据初始化数据对页面进行渲染
			modifyPageView();
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
		}
	};

	// 渲染页面 包括scrollview,viewpager等
	private void modifyPageView() {
		// 初始化渲染scollview页面
		initScrollView();
		// 初始化下方滚动fragments
		initFragments();
		// 初始化样式
		initPhotoItemInfo();
		// 初始化游标
		initCursor();
	}

	private void initPhotoItemInfo() {

		if (mCurrentWorkId == -1) {
			mViewPager.setCurrentItem(1);
			mViewPager.setCurrentItem(0);
		} else {
			mViewPager.setCurrentItem(1);
		}
	}

	private int mAskNum = 0;
	private int mReplyNum = 0;
	// 游标偏移距离
	private int mCurSorOffset;
	// 游标宽度
	private int mCursorWidth;

	// 初始化顶部RadioGroup游标
	private void initCursor() {
		// mCursorWidth = Utils.dpToPx(this, 38);
		// // 游标左侧偏移量
		// mCurSorOffset = Constants.WIDTH_OF_SCREEN / 2 - mCursorWidth
		// - Utils.dpToPx(this, 40);
		// Matrix matrix = new Matrix();
		// matrix.setTranslate(mCurSorOffset, 0);
		// mCursor.setImageMatrix(matrix);
	}

	private void initScrollView() {
		for (PhotoItem item : mPhotoItems) {
			if (item.getType() == PhotoItem.TYPE_ASK) {
				mAskNum++;
			} else if (item.getType() == PhotoItem.TYPE_REPLY) {
				mReplyNum++;
			}
		}

		mTabNumLeft.setText(String.format("（%s/%s）", 1, mAskNum));
		mTabNumRight.setText(String.format("（%s/%s）", 1, mReplyNum));
		mTabTxtRight.setTextColor(Color.parseColor("#66000000"));
		mTabNumRight.setTextColor(Color.parseColor("#66000000"));

		mTabLayoutLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mViewPager.setCurrentItem(0);
			}
		});

		mTabLayoutRight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mViewPager.setCurrentItem(0 + mAskNum);
			}
		});

		// mScrollTab.removeAllViews();
		//
		// int originTabIndex = 0;
		// int tabCount = mPhotoItems.size();
		//
		// for (int i = 0; i < tabCount; i++) {
		// LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		// LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		// params.leftMargin = Utils.dpToPx(getApplicationContext(), 15);
		// params.rightMargin = Utils.dpToPx(getApplicationContext(), 15);
		//
		// TextView tabTextView = new TextView(this);
		// tabTextView.setTextAppearance(this,
		// R.style.hot_photo_detail_scrollview_tv);
		// tabTextView.setGravity(Gravity.CENTER);
		// tabTextView.setTextColor(getResources().getColorStateList(
		// R.color.selector_hot_photo_item_scrollbar_btn));
		// tabTextView.setId(i);
		//
		// // 上方滚动scrollview文案
		// if (mPhotoItems.get(i).getType() == PhotoItem.TYPE_ASK) {
		// tabTextView
		// .setText("原图" + Integer.toString(originTabIndex + 1));
		// originTabIndex++;
		// } else if (mPhotoItems.get(i).getType() == PhotoItem.TYPE_REPLY) {
		// tabTextView.setText("作品"
		// + Integer.toString(i - mAskImageCount + 1));
		// }
		//
		// tabTextView.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// for (int i = 0; i < mScrollTab.getChildCount(); i++) {
		// View localView = mScrollTab.getChildAt(i);
		// if (localView != v) {
		// localView.setSelected(false);
		// } else {
		// localView.setSelected(true);
		// mViewPager.setCurrentItem(i);
		// }
		// }
		// }
		// });
		//
		// mScrollTab.addView(tabTextView, i, params);
		// }
		//
		// if (mClickPhotoitem != null
		// && mClickPhotoitem.getType() == PhotoItem.TYPE_REPLY) {
		// mCurrentPosition = mAskImageCount;
		// }
		//
		// // TODO 消息页面点击直接跳转
		// if (mClickPhotoitem == null) {
		// mCurrentPosition = mAskImageCount;
		// }
		//
		// selectScrollTab(mCurrentPosition);
	}

	private boolean mIsScrollRight = true;
	private boolean mIsScroll = false;

	private void initFragments() {
		int fragmentsCount = mPhotoItems.size();

		for (int i = 0; i < fragmentsCount; i++) {
			Bundle bundle = new Bundle();
			bundle.putSerializable(Constants.IntentKey.PHOTO_ITEM,
					mPhotoItems.get(i));

			PhotoDetailFragment photoDetailFragment = new PhotoDetailFragment();
			photoDetailFragment.setArguments(bundle);
			mPhotoDetailFragments.add(photoDetailFragment);
		}

		mAdapter = new HotPhotoDetailAdapter(getSupportFragmentManager(),
				mPhotoDetailFragments);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			int mlastScroll = 0;

			@Override
			public void onPageSelected(int position) {
				mViewPager.setCurrentItem(position);
				// 同步顶部scrollview位置
				selectScrollTab(position);
				// 同步图像显示区域背景图像
				// modifyPhotosContainerBg(position);
				// 同步底部图片信息数据变化
				modifyPhotoItemInfo(position);

			}

			@Override
			public void onPageScrolled(int i, float v, int arg2) {
				mBackgroundImg.setmDegree(v);

				mBackgroundImg.setmPosition(i);

				mBackgroundImg.invalidate();// 刷新

				if (mlastScroll - arg2 > 0) {
					mIsScrollRight = false;
				} else {
					mIsScrollRight = true;
				}

				mlastScroll = arg2;

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stu
			}
		});
		mViewPager.setCurrentItem(mCurrentPosition);

		mLikeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {

				// 设置点赞的缩放动画,没点赞过进入动画
				if (!mCurrentPhotoitem.isLiked()) {
					AnimatorSet animatorZoomSet = new AnimatorSet();
					animatorZoomSet.setDuration(800);

					ObjectAnimator zoomX = ObjectAnimator.ofFloat(mLikeBtn,
							"scaleX", 1f, 1.5f, 1f);
					ObjectAnimator zoomY = ObjectAnimator.ofFloat(mLikeBtn,
							"scaleY", 1f, 1.5f, 1f);
					animatorZoomSet.playTogether(zoomX, zoomY);
					animatorZoomSet.start();
				}

				// 点赞网络请求
				mLikeBtn.setClickable(false);

				int mStatus = mCurrentPhotoitem.isLiked() ? 0 : 1;
				ActionLikeRequest.Builder builder = new ActionLikeRequest.Builder()
						.setPid(mCurrentPhotoitem.getPid())
						.setType(mCurrentPhotoitem.getType())
						.setListener(mActionLikeListener).setStatus(mStatus)
						.setErrorListener(mActionLikeErrorListener);
				ActionLikeRequest request = builder.build();
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						getApplicationContext()).getRequestQueue();
				requestQueue.add(request);
			}
		});
	}

	/**
	 * 根据用户是否点赞，更新点赞按钮
	 */
	public void updateLikeView() {
		if (mCurrentPhotoitem.isLiked()) {
			mLikeBtn.setImageResource(R.drawable.hot_detail_like_selected);
		} else {
			mLikeBtn.setImageResource(R.drawable.hot_detail_like_normal);
		}
	}

	// 点赞成功回调函数
	private Listener<Boolean> mActionLikeListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			if (response) {
				mCurrentPhotoitem
						.setLikeCount(mCurrentPhotoitem.isLiked() ? mCurrentPhotoitem
								.getLikeCount() - 1 : mCurrentPhotoitem
								.getLikeCount() + 1);
				mCurrentPhotoitem
						.setIsLiked(mCurrentPhotoitem.isLiked() ? false : true);
				updateLikeView();
			}
			mLikeBtn.setClickable(true);
		}
	};

	// 点赞失败回调函数
	private PSGodErrorListener mActionLikeErrorListener = new PSGodErrorListener(
			ActionLikeRequest.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {
			mLikeBtn.setClickable(true);
		}
	};

	// 根据viewpager的变化对图片展示区域对背景做更新
	private void modifyPhotosContainerBg(int position) {
		ImageLoader imageLoader = ImageLoader.getInstance();

		String mImagePath = mPhotoItems.get(position).getImageURL();
		// 将图片毛玻璃化处理后作为背景图
		imageLoader.loadImage(mImagePath, new ImageLoadingListener() {
			@Override
			public void onLoadingStarted(String arg0, View arg1) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
				// TODO Auto-generated method stub
			}

			@SuppressWarnings("deprecation")
			@Override
			public void onLoadingComplete(String arg0, View arg1, Bitmap bitmap) {
				mBackgroundImg.setBackgroundDrawable(new BitmapDrawable(
						getResources(), BitmapUtils.getBlurBitmap(bitmap)));
			}

			@Override
			public void onLoadingCancelled(String arg0, View arg1) {
				// TODO Auto-generated method stub
			}
		});
	}

	// 根据viewpager的变化对底部栏photoItem的信息进行更新
	private void modifyPhotoItemInfo(int position) {
		ImageLoader imageLoader = ImageLoader.getInstance();

		// 同步用户头像
		String mAvatarPath = mPhotoItems.get(position).getAvatarURL();
		imageLoader.displayImage(mAvatarPath, mUserAvatar, mAvatarOptions);
		// 同步用户昵称
		mUserNickName.setText(mPhotoItems.get(position).getNickname());
		// 同步用户uid
		mUserAvatar.setUserId(mPhotoItems.get(position).getUid());
		// 同步时间
		mCreated.setText(mPhotoItems.get(position).getUpdateTimeStr());
		// 同步作品描述
		mPhotoDesc.setHtmlFromString(mPhotoItems.get(position).getDesc(), true);
	}

	private boolean mIsCursorLeft = true;

	private void selectScrollTab(int position) {
		int mCursorMoving = Utils.dpToPx(CarouselPhotoDetailActivity.this, 135);
		Animation animation = null;
		mTabSelectedIndex = position;
		if (mPhotoItems.get(position).getType() == PhotoItem.TYPE_ASK) {
			mLikeBtn.setVisibility(View.GONE);
			mBangBtn.setVisibility(View.VISIBLE);
			mTabTxtLeft.setTextColor(Color.BLACK);
			mTabNumLeft.setTextColor(Color.BLACK);
			mTabTxtRight.setTextColor(Color.parseColor("#66000000"));
			mTabNumRight.setTextColor(Color.parseColor("#66000000"));
			mTabNumLeft
					.setText(String.format("（%s/%s）", position + 1, mAskNum));
			mTabNumRight.setText(String.format("（%s/%s）", 1, mReplyNum));

			if ((!mIsScrollRight && (position == mAskNum - 1))
					|| !mIsCursorLeft) {
				mIsCursorLeft = true;
				animation = new TranslateAnimation(mCursorMoving, 0, 0, 0);
				animation.setFillAfter(true);
				animation.setDuration(200);
				mCursor.setAnimation(animation);

			}

		} else {
			mLikeBtn.setVisibility(View.VISIBLE);
			mBangBtn.setVisibility(View.GONE);
			mTabTxtLeft.setTextColor(Color.parseColor("#66000000"));
			mTabNumLeft.setTextColor(Color.parseColor("#66000000"));
			mTabTxtRight.setTextColor(Color.BLACK);
			mTabNumRight.setTextColor(Color.BLACK);
			mTabNumLeft.setText(String.format("（%s/%s）", 1, mAskNum));
			mTabNumRight.setText(String.format("（%s/%s）", position - mAskNum
					+ 1, mReplyNum));

			if ((mIsScrollRight && position == mAskNum) || mIsCursorLeft) {
				mIsCursorLeft = false;
				animation = new TranslateAnimation(0, mCursorMoving, 0, 0);
				animation.setFillAfter(true);
				animation.setDuration(200);
				mCursor.setAnimation(animation);

			}
		}

		mCurrentPhotoitem = mPhotoItems.get(position);
		updateLikeView();
	}

	private void refreshErrorImg() {
		try {
			mAdapter.notifyDataSetChanged();
			mBackgroundImg.invalidate();
		} catch (Exception e) {
		}
	}

	public void onEventMainThread(ImgRefreshEvent event) {
		refreshErrorImg();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}
}
