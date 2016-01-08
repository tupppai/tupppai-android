package com.psgod.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.psgod.R;

import java.util.ArrayList;

/**
 * 第一次启动滚动页面
 * 
 * @author brandwang
 */
public class WelcomeScrollActivity extends PSGodBaseActivity {
	private ViewPager mViewPager;
	private int mCurrentIndex = 0;
	// viewpager下方滚动的点
	private ImageView mSrcollDot0;
	private ImageView mScrollDot1;
	private ImageView mScrollDot2;
	private ImageView mScrollDot3;
	
	private TextView mEnterText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome_scroll);

		// 初始化view
		mViewPager = (ViewPager) findViewById(R.id.welcome_scroll_viewpager);
		mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		// viewpager下方滚动点
		mSrcollDot0 = (ImageView) findViewById(R.id.scroll_dot0);
		mScrollDot1 = (ImageView) findViewById(R.id.scroll_dot1);
		mScrollDot2 = (ImageView) findViewById(R.id.scroll_dot2);
		mScrollDot3 = (ImageView) findViewById(R.id.scroll_dot3);
		
		mEnterText = (TextView) findViewById(R.id.enter_text);

		// viewpager滚动页面
		LayoutInflater mLi = LayoutInflater.from(this);
		View view0 = mLi.inflate(R.layout.viewpager_welcome_0, null);
		View view1 = mLi.inflate(R.layout.viewpager_welcome_1, null);
		View view2 = mLi.inflate(R.layout.viewpager_welcome_2, null);
		View view3 = mLi.inflate(R.layout.viewpager_welcome_3, null);

		final ArrayList<View> views = new ArrayList<View>();
		views.add(view0);
		views.add(view1);
		views.add(view2);
		views.add(view3);

		PagerAdapter mPagerAdapter = new PagerAdapter() {

			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}

			@Override
			public int getCount() {
				return views.size();
			}

			@Override
			public void destroyItem(View container, int position, Object object) {
				((ViewPager) container).removeView(views.get(position));
			}

			@Override
			public Object instantiateItem(View container, int position) {
				((ViewPager) container).addView(views.get(position));
				return views.get(position);
			}
		};

		mViewPager.setAdapter(mPagerAdapter);
		
		mEnterText.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = getIntent();
				intent.setClass(WelcomeScrollActivity.this,
						NewLoginInputPhoneActivity.class);
				startActivity(intent);
				WelcomeScrollActivity.this.finish();
			}
		});

	}

	// viewpager changed listener
	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int page) {
			switch (page) {
			case 0:
				mSrcollDot0.setImageDrawable(getResources().getDrawable(
						R.drawable.shape_scroll_select));
				mScrollDot1.setImageDrawable(getResources().getDrawable(
						R.drawable.shape_scroll_unselect));
				mScrollDot2.setImageDrawable(getResources().getDrawable(
						R.drawable.shape_scroll_unselect));
				mScrollDot3.setImageDrawable(getResources().getDrawable(
						R.drawable.shape_scroll_unselect));
				mEnterText.setVisibility(View.GONE);
				break;

			case 1:
				mScrollDot1.setImageDrawable(getResources().getDrawable(
						R.drawable.shape_scroll_select));
				mSrcollDot0.setImageDrawable(getResources().getDrawable(
						R.drawable.shape_scroll_unselect));
				mScrollDot2.setImageDrawable(getResources().getDrawable(
						R.drawable.shape_scroll_unselect));
				mScrollDot3.setImageDrawable(getResources().getDrawable(
						R.drawable.shape_scroll_unselect));
				mEnterText.setVisibility(View.GONE);
				break;

			case 2:
				mScrollDot2.setImageDrawable(getResources().getDrawable(
						R.drawable.shape_scroll_select));
				mScrollDot1.setImageDrawable(getResources().getDrawable(
						R.drawable.shape_scroll_unselect));
				mSrcollDot0.setImageDrawable(getResources().getDrawable(
						R.drawable.shape_scroll_unselect));
				mScrollDot3.setImageDrawable(getResources().getDrawable(
						R.drawable.shape_scroll_unselect));
				mEnterText.setVisibility(View.GONE);
				break;

			case 3:
				mScrollDot3.setImageDrawable(getResources().getDrawable(
						R.drawable.shape_scroll_select));
				mSrcollDot0.setImageDrawable(getResources().getDrawable(
						R.drawable.shape_scroll_unselect));
				mScrollDot1.setImageDrawable(getResources().getDrawable(
						R.drawable.shape_scroll_unselect));
				mScrollDot2.setImageDrawable(getResources().getDrawable(
						R.drawable.shape_scroll_unselect));
				mEnterText.setVisibility(View.VISIBLE);

				break;

			default:
				break;
			}
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}
	}
}
