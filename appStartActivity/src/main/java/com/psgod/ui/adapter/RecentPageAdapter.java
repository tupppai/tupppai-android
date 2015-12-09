package com.psgod.ui.adapter;

/**
 * RecentPage Adapter
 * @author brandwang
 */

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class RecentPageAdapter extends FragmentPagerAdapter {
	private static final String TAG = HomePageAdapter.class.getSimpleName();

	private Context mContext;
	private List<Fragment> mFragments = new ArrayList<Fragment>();

	public RecentPageAdapter(FragmentManager fm, List<Fragment> fragments) {
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
