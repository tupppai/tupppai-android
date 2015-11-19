package com.psgod.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.nineoldandroids.view.ViewHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.psgod.BitmapUtils;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.LoginUser;
import com.psgod.model.User;
import com.psgod.network.request.ActionFollowRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.UserDetailRequest;
import com.psgod.network.request.UserDetailRequest.UserDetailResult;
import com.psgod.ui.adapter.SlidingPagerAdapter;
import com.psgod.ui.fragment.ScrollTabHolder;
import com.psgod.ui.view.PagerSlidingTabStrip;
import com.psgod.ui.widget.ActionBar;
import com.psgod.ui.widget.AvatarImageView;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;
import com.psgod.ui.widget.dialog.ImageDialog;

public class UserProfileActivity extends PSGodBaseActivity implements
		OnPageChangeListener, ScrollTabHolder {
	private final static String TAG = UserProfileActivity.class.getSimpleName();
	private Context mContext;

	public static final int TYPE_FOLLOW = 0;
	public static final int TYPE_UNFOLLOW = 1;
	// 用户id
	private Long mUid;
	// 左右滑动tab
	private PagerSlidingTabStrip mTabsTrips;
	private ViewPager viewPager;
	// viewpager adapter
	private SlidingPagerAdapter adapter;
	// 头部区域
	private LinearLayout mLinearHeader;

	private int scrollY;

	private int headerHeight;
	private int headerTranslationDis;
	private CustomProgressingDialog mProgressDialog;

	private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

	// public static int[] icons =
	// {R.drawable.ic_ask_selected,R.drawable.ic_work_normal};

	// 页面中元素
	private ActionBar mActionBar;
	private AvatarImageView mAvatarImageView;
	// private CircleImageView mGenderImageView;
	private TextView mFollowingCountTv;
	private TextView mFollowingText;
	private TextView mFollowerCountTv;
	private TextView mFollowerText;
	private TextView mLikedCount;
	private Button mFollowBtn;

	private RelativeLayout mFollowingLayout;
	private RelativeLayout mFollowerLayout;
	private RelativeLayout mParent;

	private LinearLayout mLinerHeaderOut;

	// 用户姓名
	private String mNickName;

	// 0 未关注 1 已关注
	private int isFollowed = 0;
	// 是否为当前用户自己
	private boolean isUserOwn = false;
	// 当前用户
	private User user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_profile);

		mContext = this;

		// 获取用户uid
		Intent intent = getIntent();
		mUid = intent.getLongExtra(Constants.IntentKey.USER_ID, -1);

		if (mUid == -1) {
			finish();
		}
		// TODO
		Constants.CURRENT_OTHER_USER_ID = mUid;

		// TODO 判断是否为用户自己
		LoginUser user = LoginUser.getInstance();
		if (mUid == user.getUid()) {
			isUserOwn = true;
		}

		initViews();
		initListeners();

		// 显示等待对话框
		if (mProgressDialog == null) {
			mProgressDialog = new CustomProgressingDialog(
					UserProfileActivity.this);
		}
		if (!mProgressDialog.isShowing()) {
			mProgressDialog.show();
		}

		initUserProfileData();
	}

	// 设置事件监听
	private void initListeners() {
		// 点击粉丝和关注列表跳转
		OnClickListener followerClickListener = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(UserProfileActivity.this,
						OthersFollowerListActivity.class);
				intent.putExtra(Constants.IntentKey.USER_ID, mUid);
				intent.putExtra("list_type", 0);
				intent.putExtra(Constants.IntentKey.USER_NICKNAME, mNickName);
				startActivity(intent);
			}
		};
		mFollowerLayout.setOnClickListener(followerClickListener);

		OnClickListener followingClickListener = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(UserProfileActivity.this,
						OthersFollowerListActivity.class);
				intent.putExtra(Constants.IntentKey.USER_ID, mUid);
				intent.putExtra("list_type", 1);
				intent.putExtra(Constants.IntentKey.USER_NICKNAME, mNickName);
				startActivity(intent);
			}
		};
		mFollowingLayout.setOnClickListener(followingClickListener);

		mFollowBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// 请求网络时设置不可点击
				mFollowBtn.setClickable(false);
				int mCurrentType = isFollowed == 1 ? TYPE_UNFOLLOW
						: TYPE_FOLLOW;

				ActionFollowRequest.Builder builder = new ActionFollowRequest.Builder()
						.setType(mCurrentType).setUid(mUid)
						.setErrorListener(errorListener)
						.setListener(actionFollowListener);

				ActionFollowRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						getApplication()).getRequestQueue();
				requestQueue.add(request);
			}
		});

		mAvatarImageView.setOnClickListener(new OnClickListener() {
			ImageView imageView = new ImageView(mContext);

			@Override
			public void onClick(View view) {
				ImageDialog dialog = new ImageDialog(UserProfileActivity.this,
						(ImageView) view);
				dialog.show();
			}
		});
	}

	// 关注 取消关注 listener
	private Listener<Boolean> actionFollowListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			if (response == true) {
				mFollowBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);
				if (isFollowed == 1) {
					mFollowBtn.setBackgroundResource(R.drawable.btn_unfollow);
					mFollowBtn.setText("+ 关注");
					Toast.makeText(UserProfileActivity.this, "取消关注成功",
							Toast.LENGTH_SHORT).show();
					isFollowed = 0;

					// 取消关注后 粉丝数 减一
					user.setFollowerCount(user.getFollowerCount() - 1);
					mFollowerCountTv.setText(Integer.toString(user
							.getFollowerCount()));
				} else {
					mFollowBtn.setBackgroundResource(R.drawable.btn_follow);
					if (user.isFollowed() == 1) {
						mFollowBtn.setText("互相关注");
						mFollowBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
					} else {
						mFollowBtn.setText("已关注");
					}
					Toast.makeText(UserProfileActivity.this, "关注成功",
							Toast.LENGTH_SHORT).show();
					isFollowed = 1;

					Animation followerCountAddAnimation = AnimationUtils
							.loadAnimation(UserProfileActivity.this,
									R.anim.following_count);
					mFollowerCountTv.startAnimation(followerCountAddAnimation);
					followerCountAddAnimation
							.setAnimationListener(animationListener);
				}

				mFollowBtn.setClickable(true);

				// 关注用户有变化 需要刷新关注页面
				Constants.IS_FOLLOW_NEW_USER = true;
			}
		}
	};

	private AnimationListener animationListener = new AnimationListener() {
		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			user.setFollowerCount(user.getFollowerCount() + 1);
			mFollowerCountTv.setText(Integer.toString(user.getFollowerCount()));
		}
	};

	// 初始化用户数据
	private void initUserProfileData() {
		int mPage = 1;
		UserDetailRequest.Builder builder = new UserDetailRequest.Builder()
				.setUserId(mUid).setPage(mPage).setListener(initDataListener)
				.setErrorListener(errorListener);

		UserDetailRequest request = builder.build();
		request.setTag(TAG);
		RequestQueue requestQueue = PSGodRequestQueue.getInstance(this)
				.getRequestQueue();
		requestQueue.add(request);
	}

	private Listener<UserDetailResult> initDataListener = new Listener<UserDetailResult>() {
		@Override
		public void onResponse(UserDetailResult response) {
			if (response != null) {
				user = response.getUserInfo();

				// 初始化用户信息后 初始化viewpager和tabs
				setupPager();
				setupTabs();

				ImageLoader imageLoader = ImageLoader.getInstance();
				// imageLoader.displayImage(user.getAvatarImageUrl(),
				// mAvatarImageView, mAvatarOptions);
				imageLoader.displayImage(user.getAvatarImageUrl(),
						mAvatarImageView, mAvatarOptions, imageLoadingListener);

				// 设置性别
				Resources res = getApplicationContext().getResources();
				// int genderDrawableId = (user.getGender() == 0) ?
				// R.drawable.ic_female
				// : R.drawable.ic_male;
				// mGenderImageView.setImageDrawable(res.getDrawable(genderDrawableId));

				mNickName = user.getNickname();
				mActionBar.setTitle(user.getNickname());
				mFollowerCountTv.setText(Integer.toString(user
						.getFollowerCount()));
				mFollowingCountTv.setText(Integer.toString(user
						.getFollowingCount()));
				mLikedCount.setText(Integer.toString(user.getLikedCount()));

				isFollowed = user.isFollowing();
				mFollowBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);
				if (isFollowed == 1) {
					mFollowBtn.setBackgroundResource(R.drawable.btn_follow);
					if (user.isFollowed() == 1) {
						mFollowBtn.setText("互相关注");
						mFollowBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
					} else {
						mFollowBtn.setText("已关注");
					}
				} else {
					mFollowBtn.setBackgroundResource(R.drawable.btn_unfollow);
					mFollowBtn.setText("+ 关注");
				}
			}
		}
	};

	private PSGodErrorListener errorListener = new PSGodErrorListener(
			ActionFollowRequest.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {
			// 初始化完毕 去掉菊花
			if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		}
	};

	private void initViews() {
		headerHeight = getResources().getDimensionPixelSize(
				R.dimen.max_header_height_profile);
		headerTranslationDis = -getResources().getDimensionPixelSize(
				R.dimen.header_offset_dis_profile);

		mTabsTrips = (PagerSlidingTabStrip) findViewById(R.id.user_profile_tabs);
		viewPager = (ViewPager) findViewById(R.id.user_profile_view_pager);
		mLinearHeader = (LinearLayout) findViewById(R.id.user_profile_header);

		// 初始化页面元素
		mActionBar = (ActionBar) findViewById(R.id.actionbar);
		mAvatarImageView = (AvatarImageView) findViewById(R.id.user_profile_avatar);
		// mGenderImageView = (CircleImageView) findViewById(
		// R.id.user_profile_gender);
		mFollowingCountTv = (TextView) findViewById(R.id.user_profile_user_following_count);
		mFollowingText = (TextView) findViewById(R.id.user_profile_user_following);
		mFollowerCountTv = (TextView) findViewById(R.id.user_profile_user_followers_count);
		mFollowerText = (TextView) findViewById(R.id.user_profile_user_followers);
		mLikedCount = (TextView) findViewById(R.id.user_profile_user_like_count);
		mFollowBtn = (Button) findViewById(R.id.activity_user_profile_follow_btn);
		mFollowingLayout = (RelativeLayout) findViewById(R.id.layout_following_profile);
		mFollowerLayout = (RelativeLayout) findViewById(R.id.layout_followers_profile);
		mParent = (RelativeLayout) findViewById(R.id.avatar_parent);
		mLinerHeaderOut = (LinearLayout) findViewById(R.id.user_profile_header_out);
		if (isUserOwn) {
			mFollowBtn.setVisibility(View.INVISIBLE);
		}
	}

	private void setupPager() {
		adapter = new SlidingPagerAdapter(getSupportFragmentManager(), this,
				viewPager);
		adapter.setTabHolderScrollingListener(this);
		viewPager.setOffscreenPageLimit(adapter.getCacheCount());
		viewPager.setAdapter(adapter);
		viewPager.setOnPageChangeListener(this);
	}

	private void setupTabs() {
		mTabsTrips.setShouldExpand(true);
		mTabsTrips.setUnderlineHeight(5);
		mTabsTrips.setIndicatorHeight(10);
		mTabsTrips.setCheckedTextColorResource(R.color.black);
		mTabsTrips.setTextSize(Utils.dpToPx(mContext, 15));
		mTabsTrips.setViewPager(viewPager);

		// 初始化完毕 去掉菊花
		if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}

	// 继承实现OnPageChangeListener方法
	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
		mTabsTrips.onPageScrolled(position, positionOffset,
				positionOffsetPixels);
	}

	@Override
	public void onPageSelected(int position) {
		mTabsTrips.onPageSelected(position);

		reLocation = true;
		SparseArrayCompat<ScrollTabHolder> scrollTabHolders = adapter
				.getScrollTabHolders();
		ScrollTabHolder currentHolder = scrollTabHolders.valueAt(position);

		if (NEED_RELAYOUT) {
			// 修正滚出去的偏移量
			currentHolder.adjustScroll(mLinearHeader.getHeight() + headerTop);
		} else {
			currentHolder
					.adjustScroll((int) (mLinearHeader.getHeight() + ViewHelper
							.getTranslationY(mLinearHeader)));
			ViewHelper.setTranslationY(mLinearHeader, 0);
			currentHolder.adjustScroll(scrollY);
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		mTabsTrips.onPageScrollStateChanged(state);
	}

	@Override
	public void adjustScroll(int scrollHeight) {

	}

	private boolean reLocation = false;

	private int headerScrollSize = 0;

	public static final boolean NEED_RELAYOUT = Integer.valueOf(
			Build.VERSION.SDK).intValue() < Build.VERSION_CODES.HONEYCOMB; // 是否超过3.0版本

	private int headerTop = 0;

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount, int pagePosition) {
		if (viewPager.getCurrentItem() != pagePosition) {
			return;
		}
		if (headerScrollSize == 0 && reLocation) {
			reLocation = false;
			return;
		}
		reLocation = false;
		scrollY = Math.max(-getScrollY(view), headerTranslationDis);
		if (NEED_RELAYOUT) {
			headerTop = scrollY;
			mLinearHeader.post(new Runnable() {
				@Override
				public void run() {
					mLinearHeader.layout(0, headerTop,
							mLinearHeader.getWidth(),
							headerTop + mLinearHeader.getHeight());
				}
			});
		} else {
			ViewHelper.setTranslationY(mLinearHeader, scrollY);
		}
	}

	boolean once = true;
	View c1 = null;
	View c2 = null;

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	public int getScrollY(AbsListView view) {
		// ListView中

		int top = 0;
		if (view instanceof ListView) {
			c1 = view.getChildAt(0);
			if (c1 == null) {
				return 0;
			}
			top = c1.getTop();
			int firstVisiblePosition = view.getFirstVisiblePosition();

			// 索引从刷新头部开始算起
			if (firstVisiblePosition == 0) {
				return -top + headerScrollSize;
			} else if (firstVisiblePosition == 1) {
				return -top;
			} else {
				return -top + (firstVisiblePosition - 2) * c1.getHeight()
						+ headerHeight;
			}
		} else {
			if (once) {
				c2 = view.getChildAt(0);
				once = false;
				top = c2.getTop();
			}
			if (c2 == null) {
				return 0;
			}
			top = c2.getTop();
			int firstVisiblePosition = view.getFirstVisiblePosition();

			// 索引从刷新头部开始算起
			if (firstVisiblePosition == 0) {
				return -top + headerScrollSize;
			} else if (firstVisiblePosition == 1) {
				return -top;
			} else {
				return -top + (firstVisiblePosition - 2) * c2.getHeight()
						+ headerHeight;
			}
		}

	}

	@Override
	public void onHeaderScroll(boolean isRefreashing, int value,
			int pagePosition) {
		if (viewPager.getCurrentItem() != pagePosition) {
			return;
		}
		headerScrollSize = value;
		if (NEED_RELAYOUT) {
			mLinearHeader.post(new Runnable() {

				@Override
				public void run() {
					Log.e("Main", "scorry=" + (-headerScrollSize));
					mLinearHeader.layout(0, -headerScrollSize,
							mLinearHeader.getWidth(), -headerScrollSize
									+ mLinearHeader.getHeight());
				}
			});
		} else {
			ViewHelper.setTranslationY(mLinearHeader, -value);
		}
	}

	/**
	 * 图片加载回调 将图片毛玻璃化处理后作为背景
	 */
	private ImageLoadingListener imageLoadingListener = new ImageLoadingListener() {
		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			mLinerHeaderOut.setBackground(new BitmapDrawable(getResources(),
					BitmapUtils.getBlurBitmap(loadedImage)));
		}

		@Override
		public void onLoadingCancelled(String arg0, View arg1) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onLoadingStarted(String arg0, View arg1) {
			// TODO Auto-generated method stub
		}
	};

	/**
	 * 暂停所有的下载
	 */
	@Override
	public void onStop() {
		super.onStop();
		RequestQueue requestQueue = PSGodRequestQueue.getInstance(this)
				.getRequestQueue();
		requestQueue.cancelAll(TAG);
	}

}
