package com.psgod.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.WeakReferenceHandler;
import com.psgod.ui.activity.MainActivity;
import com.psgod.ui.adapter.InprogressPageAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 进行中tab v2.0
 * 
 * @author ZouMengyuan
 * 
 */
public class InprogressPageFragment extends Fragment implements
		Handler.Callback {

	private static final String TAG = InprogressPageFragment.class
			.getSimpleName();

	private Context mContext;
	private ViewHolder mViewHolder;
	private InprogressPageAdapter mPageInprogressAdapter;

	private final int COUNT_OF_FRAGMENTS = 3;
	private final int[] TAB_RADIO_BUTTONS_ID = {
			R.id.fragment_inprogress_ask_radio_btn,
			R.id.fragment_inprogress_reply_radio_btn,
			R.id.fragment_inprogress_complete_radio_btn };

	// 游标偏移距离
	private int mCurSorOffset;
	// 游标宽度
	private int mCursorWidth;
	private int mCursorone; // 页卡1 -> 页卡2 偏移量
	private int mCursortwo; // 页卡1 -> 页卡3 偏移量

	private WeakReferenceHandler handler = new WeakReferenceHandler(this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();

		FrameLayout parentview = new FrameLayout(getActivity());
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		parentview.setLayoutParams(params);

		mViewHolder = new ViewHolder();
		mViewHolder.mParentView = parentview;
		mViewHolder.mView = LayoutInflater.from(getActivity()).inflate(
				R.layout.fragment_inprogress_page, parentview, true);
		mViewHolder.viewPager = (ViewPager) mViewHolder.mView
				.findViewById(R.id.fragment_inprogress_view_pager);
		mViewHolder.tabRadioGroup = (RadioGroup) mViewHolder.mView
				.findViewById(R.id.fragment_inprogress_tab_radio_group);
		mViewHolder.askRadioButton = (RadioButton) mViewHolder.mView
				.findViewById(R.id.fragment_inprogress_ask_radio_btn);
		mViewHolder.replyRadioButton = (RadioButton) mViewHolder.mView
				.findViewById(R.id.fragment_inprogress_reply_radio_btn);
		mViewHolder.completeRadioButton = (RadioButton) mViewHolder.mView
				.findViewById(R.id.fragment_inprogress_complete_radio_btn);
		mViewHolder.mCursor = (ImageView) mViewHolder.mView
				.findViewById(R.id.fragment_inprogress_cursor);

		initCursor();

		List<Fragment> fragments = new ArrayList<Fragment>();
		if (mViewHolder.homePageInprogressAskFragment == null) {
			mViewHolder.homePageInprogressAskFragment = new InprogressPageAskFragment();
		}
		if (mViewHolder.homePageInprogressReplyFragment == null) {
			mViewHolder.homePageInprogressReplyFragment = new InprogressPageReplyFragment();
		}
		if (mViewHolder.homePageInprogressCompleteFragment == null) {
			mViewHolder.homePageInprogressCompleteFragment = new InprogressPageCompleteFragment();
		}
		fragments.add(mViewHolder.homePageInprogressAskFragment);
		fragments.add(mViewHolder.homePageInprogressReplyFragment);
		fragments.add(mViewHolder.homePageInprogressCompleteFragment);

		mPageInprogressAdapter = new InprogressPageAdapter(getActivity()
				.getSupportFragmentManager(), fragments);
		mViewHolder.viewPager.setAdapter(mPageInprogressAdapter);
		mViewHolder.viewPager.setCurrentItem(0);
		mViewHolder.tabRadioGroup.check(TAB_RADIO_BUTTONS_ID[0]);

		initListeners();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		FrameLayout parentview = new FrameLayout(getActivity());
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		parentview.setLayoutParams(params);

		mViewHolder.mParentView.removeView(mViewHolder.mView);
		parentview.addView(mViewHolder.mView);

		mViewHolder.mParentView = parentview;
		return parentview;
	}

	// 初始化顶部RadioGroup游标
	private void initCursor() {
		mCursorWidth = Utils.dpToPx(getActivity(), 55);
		// 游标左侧偏移量
		mCurSorOffset = (Constants.WIDTH_OF_SCREEN / 3 - mCursorWidth) / 2;
		mCursorone = mCurSorOffset * 2 + mCursorWidth;
		mCursortwo = mCursorone * 2;
		Matrix matrix = new Matrix();
		matrix.setTranslate(mCurSorOffset, 0);
		mViewHolder.mCursor.setImageMatrix(matrix);
	}

	public void onNewIntent(Intent intent) {
		if (intent == null) {
			// TODO 输出日志
			return;
		}

		int id = intent.getIntExtra(
				MainActivity.IntentParams.KEY_INPROGRESS_ID, -1);
		if (id == MainActivity.IntentParams.VALUE_INPROGRESS_ID_ASK) {
			mViewHolder.viewPager.setCurrentItem(0);
		} else if (id == MainActivity.IntentParams.VALUE_INPROGRESS_ID_REPLY) {
			if (mViewHolder == null) {
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						mViewHolder.viewPager.setCurrentItem(1);
					}
				}, 500);
			} else {
				mViewHolder.viewPager.setCurrentItem(1);
			}

		} else if (id == MainActivity.IntentParams.VALUE_INPROGRESS_ID_COMPLETE) {
			mViewHolder.viewPager.setCurrentItem(2);
		}

		// 触发自动下拉刷新
		switch (id) {
		case MainActivity.IntentParams.VALUE_INPROGRESS_ID_ASK:
			mViewHolder.homePageInprogressAskFragment.setRefreshing();
			break;

		case MainActivity.IntentParams.VALUE_INPROGRESS_ID_REPLY:
			mViewHolder.homePageInprogressReplyFragment.setRefreshing();
			break;

		case MainActivity.IntentParams.VALUE_INPROGRESS_ID_COMPLETE:
			mViewHolder.homePageInprogressCompleteFragment.setRefreshing();
			break;

		default:
			break;
		}

	}

	private void initListeners() {
		mViewHolder.tabRadioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						for (int ix = 0; ix < COUNT_OF_FRAGMENTS; ++ix) {
							if (TAB_RADIO_BUTTONS_ID[ix] == checkedId) {
								mViewHolder.viewPager.setCurrentItem(ix);
								((RadioButton) mViewHolder.mView
										.findViewById(TAB_RADIO_BUTTONS_ID[ix]))
										.setTextColor(Color
												.parseColor("#FF000000"));
							} else {
								((RadioButton) mViewHolder.mView
										.findViewById(TAB_RADIO_BUTTONS_ID[ix]))
										.setTextColor(Color
												.parseColor("#99000000"));
							}
						}
					}
				});

		mViewHolder.viewPager
				.setOnPageChangeListener(new OnPageChangeListener() {

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
							if (Constants.CURRENT_INPROGRESS_TAB == 1) {
								animation = new TranslateAnimation(mCursorone,
										0, 0, 0);
								animation.setFillAfter(true);
								animation.setDuration(300);
								mViewHolder.mCursor.setAnimation(animation);

							} else if (Constants.CURRENT_INPROGRESS_TAB == 2) {
								animation = new TranslateAnimation(mCursortwo,
										0, 0, 0);
								animation.setFillAfter(true);
								animation.setDuration(300);
								mViewHolder.mCursor.setAnimation(animation);
							}
							break;

						case 1:
							if (Constants.CURRENT_INPROGRESS_TAB == 0) {
								animation = new TranslateAnimation(
										mCurSorOffset, mCursorone, 0, 0);
								animation.setFillAfter(true);
								animation.setDuration(300);
								mViewHolder.mCursor.setAnimation(animation);
							} else if (Constants.CURRENT_INPROGRESS_TAB == 2) {
								animation = new TranslateAnimation(mCursortwo,
										mCursorone, 0, 0);
								animation.setFillAfter(true);
								animation.setDuration(300);
								mViewHolder.mCursor.setAnimation(animation);
							}
							break;
						case 2:
							if (Constants.CURRENT_INPROGRESS_TAB == 0) {
								animation = new TranslateAnimation(
										mCurSorOffset, mCursortwo, 0, 0);
								animation.setFillAfter(true);
								animation.setDuration(300);
								mViewHolder.mCursor.setAnimation(animation);
							} else if (Constants.CURRENT_INPROGRESS_TAB == 1) {
								animation = new TranslateAnimation(mCursorone,
										mCursortwo, 0, 0);
								animation.setFillAfter(true);
								animation.setDuration(300);
								mViewHolder.mCursor.setAnimation(animation);
							}
							break;
						default:
							break;
						}
						// 设置进行中当前tab
						Constants.CURRENT_INPROGRESS_TAB = index;
					}
				});
	}

	private static class ViewHolder {
		ViewGroup mParentView;
		View mView;
		ViewPager viewPager;
		InprogressPageAskFragment homePageInprogressAskFragment;
		InprogressPageReplyFragment homePageInprogressReplyFragment;
		InprogressPageCompleteFragment homePageInprogressCompleteFragment;
		RadioGroup tabRadioGroup;
		RadioButton askRadioButton;
		RadioButton replyRadioButton;
		RadioButton completeRadioButton;
		ImageView mCursor;
	}

	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		return false;
	}

	public void test() {
		// TODO Auto-generated method stub
		mViewHolder.viewPager.setCurrentItem(1);
	}

}
