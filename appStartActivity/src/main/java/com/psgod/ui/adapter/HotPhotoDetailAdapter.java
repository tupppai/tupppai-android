package com.psgod.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;

import com.psgod.ui.fragment.PhotoDetailFragment;

import java.util.ArrayList;

public class HotPhotoDetailAdapter extends FragmentPagerAdapter {
	private ArrayList<PhotoDetailFragment> mFragments;
	private FragmentManager mFragmentManager;

	public HotPhotoDetailAdapter(FragmentManager fm) {
		super(fm);
		this.mFragmentManager = fm;
	}

	public HotPhotoDetailAdapter(FragmentManager fm,
			ArrayList<PhotoDetailFragment> fragments) {
		super(fm);

		this.mFragmentManager = fm;
		this.mFragments = fragments;
	}

	@Override
	public Fragment getItem(int position) {
		return mFragments.get(position);
	}

	@Override
	public int getCount() {
		return mFragments.size();
	}

	public void setFragments(ArrayList<PhotoDetailFragment> fragments) {
		if (this.mFragments != null) {
			FragmentTransaction ft = mFragmentManager.beginTransaction();
			for (Fragment f : this.mFragments) {
				ft.remove(f);
			}
			ft.commit();
			ft = null;
			mFragmentManager.executePendingTransactions();
		}
		this.mFragments = fragments;
		notifyDataSetChanged();
	}

	@Override
	public Object instantiateItem(ViewGroup container, final int position) {
		Object obj = super.instantiateItem(container, position);
		return obj;
	}
}
