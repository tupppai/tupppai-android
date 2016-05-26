package com.psgod.ui.fragment;

import android.content.Intent;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.psgod.Constants;
import com.psgod.Logger;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.eventbus.RefreshEvent;
import com.psgod.ui.activity.MainActivity;
import com.psgod.ui.activity.SearchActivity;
import com.psgod.ui.adapter.HomePageAdapter;
import com.psgod.ui.widget.dialog.CameraPopupwindow;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 首页精选tab v2.0
 * 
 * @author brandwang
 * 
 */
public class HomePageFragment extends Fragment {
	private static final String TAG = HomePageFragment.class.getSimpleName();

	public static final int REQUEST_TAKE_PHOTO = 0x770;
	public static final int REQUEST_CHOOSE_PHOTO = 0x771;

	private final int COUNT_OF_FRAGMENTS = 2;
	private final int[] TAB_RADIO_BUTTONS_ID = {
			R.id.fragment_homepage_hot_radio_btn,
			R.id.fragment_homepage_focus_radio_btn };

	private ViewHolder mViewHolder;
	private HomePageAdapter mPhotoListPagerAdapter;

	// 游标偏移距离
	private int mCurSorOffset;
	// 游标宽度
	private int mCursorWidth;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void onNewIntent(Intent intent) {
		if (intent == null) {
			// TODO 输出日志
			return;
		}

		int id = intent.getIntExtra(MainActivity.IntentParams.KEY_HOMEPAGE_ID,
				-1);
		if (id == MainActivity.IntentParams.VALUE_HOMEPAGE_ID_HOT) {
			mViewHolder.viewPager.setCurrentItem(0);
		} else if (id == MainActivity.IntentParams.VALUE_HOMEPAGE_ID_FOCUS) {
			mViewHolder.viewPager.setCurrentItem(1);
		}

		// 触发自动下拉刷新
		switch (id) {
		case MainActivity.IntentParams.VALUE_HOMEPAGE_ID_HOT:
			EventBus.getDefault().post(new RefreshEvent(HomePageHotFragment.class.getName()));
			break;

		case MainActivity.IntentParams.VALUE_HOMEPAGE_ID_FOCUS:
			EventBus.getDefault().post(new RefreshEvent(HomePageFocusFragment.class.getName()));
			break;

		default:
			break;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Logger.logMethod(TAG, "onCreateView");
		FrameLayout parentView = new FrameLayout(getActivity());
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		parentView.setLayoutParams(params);

		mViewHolder = new ViewHolder();
		mViewHolder.mParentView = parentView;
		mViewHolder.mView = LayoutInflater.from(getActivity()).inflate(
				R.layout.fragment_home_page, parentView, true);
		// mViewHolder.mSearchBtn = (ImageButton) mViewHolder.mView
		// .findViewById(R.id.fragment_homepage_search_btn);
		mViewHolder.viewPager = (ViewPager) mViewHolder.mView
				.findViewById(R.id.fragment_homepage_view_pager);
		mViewHolder.tabRadioGroup = (RadioGroup) mViewHolder.mView
				.findViewById(R.id.fragment_homepage_tab_radio_group);

		mViewHolder.tabRadioButtonLeft = (RadioButton) mViewHolder.mView
				.findViewById(R.id.fragment_homepage_hot_radio_btn);
		mViewHolder.tabRadioButtonRight = (RadioButton) mViewHolder.mView
				.findViewById(R.id.fragment_homepage_focus_radio_btn);

		mViewHolder.mCursor = (ImageView) mViewHolder.mView
				.findViewById(R.id.fragment_homepage_cursor);

		mViewHolder.mSearchImg = (ImageView) mViewHolder.mView
				.findViewById(R.id.fragment_homepage_search_img);

		initCursor();

		List<Fragment> fragments = new ArrayList<Fragment>();
		if (mViewHolder.mHomePageHotFragment == null) {
			mViewHolder.mHomePageHotFragment = new HomePageHotFragment();
		}
//		if (mViewHolder.mHomePageFocusFragment == null) {
//			mViewHolder.mHomePageFocusFragment = new HomePageFocusFragment();
//		}
		if (mViewHolder.mHomePageDynamicFragement == null) {
			mViewHolder.mHomePageDynamicFragement = new HomePageDynamicFragment();
		}

		fragments.add(mViewHolder.mHomePageHotFragment);
//		fragments.add(mViewHolder.mHomePageFocusFragment);
		fragments.add(mViewHolder.mHomePageDynamicFragement);
		mPhotoListPagerAdapter = new HomePageAdapter(getActivity()
				.getSupportFragmentManager(), fragments);

		mViewHolder.viewPager.setAdapter(mPhotoListPagerAdapter);
		initListeners();
		return parentView;
	}

	// 初始化顶部RadioGroup游标
	private void initCursor() {
		mCursorWidth = Utils.dpToPx(getActivity(), 38);
		// 游标左侧偏移量
		mCurSorOffset = Constants.WIDTH_OF_SCREEN / 2 - mCursorWidth
				- Utils.dpToPx(getActivity(), 40);
		Matrix matrix = new Matrix();
		matrix.setTranslate(mCurSorOffset, 0);
		mViewHolder.mCursor.setImageMatrix(matrix);
	}

	private void initListeners() {
		// 双击顶部radio button自动回顶部刷新 热门
		mViewHolder.tabRadioButtonLeft
				.setOnTouchListener(new OnTouchListener() {
					int count = 0;
					int firClick = 0;
					int secClick = 0;

					@Override
					public boolean onTouch(View view, MotionEvent event) {
						if (MotionEvent.ACTION_DOWN == event.getAction()) {
							count++;
							if (count == 1) {
								firClick = (int) System.currentTimeMillis();
							} else if (count == 2) {
								secClick = (int) System.currentTimeMillis();
								if (secClick - firClick < 1000) {
									// 双击事件 下拉刷新首页热门列表
//									Intent intent = new Intent(getActivity(),
//											MainActivity.class);
//									intent.putExtra(
//											MainActivity.IntentParams.KEY_FRAGMENT_ID,
//											MainActivity.IntentParams.VALUE_FRAGMENT_ID_HOMEPAGE);
//									intent.putExtra(
//											MainActivity.IntentParams.KEY_HOMEPAGE_ID,
//											MainActivity.IntentParams.VALUE_HOMEPAGE_ID_HOT);
//									intent.putExtra(
//											MainActivity.IntentParams.KEY_NEED_REFRESH,
//											true);
//									startActivity(intent);
									EventBus.getDefault().post(new RefreshEvent(HomePageHotFragment.class.getName()));
								}
								count = 0;
								firClick = 0;
								secClick = 0;
							}
						}
						return false;
					}
				});

		// 双击顶部radio button自动回顶部刷新 关注
		mViewHolder.tabRadioButtonRight
				.setOnTouchListener(new OnTouchListener() {
					int count = 0;
					int firClick = 0;
					int secClick = 0;

					@Override
					public boolean onTouch(View view, MotionEvent event) {
						if (MotionEvent.ACTION_DOWN == event.getAction()) {
							count++;

							if (count == 1) {
								firClick = (int) System.currentTimeMillis();
							} else if (count == 2) {
								secClick = (int) System.currentTimeMillis();
								if (secClick - firClick < 1000) {
									// 双击事件 下拉刷新首页热门列表
//									Intent intent = new Intent(getActivity(),
//											MainActivity.class);
//									intent.putExtra(
//											MainActivity.IntentParams.KEY_FRAGMENT_ID,
//											MainActivity.IntentParams.VALUE_FRAGMENT_ID_HOMEPAGE);
//									intent.putExtra(
//											MainActivity.IntentParams.KEY_HOMEPAGE_ID,
//											MainActivity.IntentParams.VALUE_HOMEPAGE_ID_FOCUS);
//									intent.putExtra(
//											MainActivity.IntentParams.KEY_NEED_REFRESH,
//											true);
//									startActivity(intent);
									//EventBus.getDefault().post(new RefreshEvent(HomePageFocusFragment.class.getName()));
									EventBus.getDefault().post(new RefreshEvent(HomePageDynamicFragment.class.getName()));
								}
								count = 0;
								firClick = 0;
								secClick = 0;
							}
						}
						return false;
					}
				});

		// mViewHolder.mSearchBtn.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		//
		// showCameraPopupwindow(v); //弹出选择上传求P还是作品的PopupWindow
		//
		// // if (mViewHolder.mCameraDialog == null) {
		// // mViewHolder.mCameraDialog = new CameraDialog(getActivity());
		// // }
		// //
		// // if (mViewHolder.mCameraDialog.isShowing()) {
		// // mViewHolder.mCameraDialog.dismiss();
		// // } else {
		// // mViewHolder.mCameraDialog.show();
		// //
		// // }
		// }
		// });

		mViewHolder.tabRadioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						for (int ix = 0; ix < COUNT_OF_FRAGMENTS; ++ix) {
							if (TAB_RADIO_BUTTONS_ID[ix] == checkedId) {
								mViewHolder.viewPager.setCurrentItem(ix);
							}
						}
					}
				});

		mViewHolder.viewPager
				.setOnPageChangeListener(new OnPageChangeListener() {
					int mCursorMoving = Utils.dpToPx(getActivity(), 108);

					@Override
					public void onPageScrollStateChanged(int index) {
						// Do Nothing
					}

					@Override
					public void onPageScrolled(int arg0, float arg1, int arg2) {
						// Do Nothing
					}

					@Override
					public void onPageSelected(int index) {
						mViewHolder.tabRadioGroup
								.check(TAB_RADIO_BUTTONS_ID[index]);

						Animation animation = null;

						switch (index) {
						case 0:
							if (Constants.CURRENT_HOMEPAGE_TAB == 1) {
								animation = new TranslateAnimation(
										mCursorMoving, 0, 0, 0);
								animation.setFillAfter(true);
								animation.setDuration(300);
								mViewHolder.mCursor.setAnimation(animation);
							}
							break;

						case 1:
							if (Constants.CURRENT_HOMEPAGE_TAB == 0) {
								animation = new TranslateAnimation(0,
										mCursorMoving, 0, 0);
								animation.setFillAfter(true);
								animation.setDuration(300);
								mViewHolder.mCursor.setAnimation(animation);
							}
							break;
						default:
							break;
						}
						// 设置首页当前tab
						Constants.CURRENT_HOMEPAGE_TAB = index;
					}
				});

		mViewHolder.mSearchImg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getActivity(), SearchActivity.class);
				startActivity(intent);
			}
		});
	}

	/**
	 * 保存视图组件，避免视图的重复加载
	 * 
	 * @author Rayal
	 * 
	 */
	private static class ViewHolder {
		ViewGroup mParentView;
		View mView;
		HomePageHotFragment mHomePageHotFragment;
		HomePageFocusFragment mHomePageFocusFragment;
		HomePageDynamicFragment mHomePageDynamicFragement;

		// 选择发表作品或求助的弹出框
		CameraPopupwindow cameraPopupwindow;

		ViewPager viewPager;
		RadioGroup tabRadioGroup;
		RadioButton tabRadioButtonLeft;
		RadioButton tabRadioButtonRight;

		ImageView mSearchImg;
		// RadioGroup下方游标
		ImageView mCursor;
	}
}
