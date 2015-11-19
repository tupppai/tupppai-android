package com.psgod.ui.adapter;

/**
 * HomePage Adapter
 * @author brandwang
 */

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class HomePageAdapter extends FragmentPagerAdapter {
	private static final String TAG = HomePageAdapter.class.getSimpleName();

	private Context mContext;
	private List<Fragment> mFragments = new ArrayList<Fragment>();

	public HomePageAdapter(FragmentManager fm, List<Fragment> fragments) {
		super(fm);
		mFragments = fragments;
	}

	@Override
	public Fragment getItem(int arg0) {
		return mFragments.get(arg0);
	}

	@Override
	public int getCount() {
		return mFragments.size();
	}

}
