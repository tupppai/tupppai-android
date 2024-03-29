package com.psgod.ui.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;

import com.psgod.ui.fragment.ScrollTabHolder;
import com.psgod.ui.fragment.ScrollTabHolderFragment;

import java.util.List;

public class SlidingPageMyAdapter extends FragmentPagerAdapter {
	protected final ScrollTabHolderFragment[] fragments;

	protected final Context context;

	private SparseArrayCompat<ScrollTabHolder> mScrollTabHolders;
	private ScrollTabHolder mListener;

	public int getCacheCount() {
		return PageAdapterMyTab.values().length;
	}

	public SlidingPageMyAdapter(FragmentManager fm, Context context,
			ViewPager pager) {
		super(fm);
		this.context = context;
		fragments = new ScrollTabHolderFragment[PageAdapterMyTab.values().length];
		mScrollTabHolders = new SparseArrayCompat<ScrollTabHolder>();
		init(fm);
	}

	private void init(FragmentManager fm) {
		for (PageAdapterMyTab tab : PageAdapterMyTab.values()) {
			try {
				ScrollTabHolderFragment fragment = null;

				List<Fragment> fs = fm.getFragments();
				if (fs != null) {
					for (Fragment f : fs) {
						if (f.getClass() == tab.clazz) {
							fragment = (ScrollTabHolderFragment) f;
							break;
						}
					}
				}

				if (fragment == null) {
					fragment = (ScrollTabHolderFragment) tab.clazz
							.newInstance();
				}

				fragments[tab.tabIndex] = fragment;
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public void setTabHolderScrollingListener(ScrollTabHolder listener) {
		mListener = listener;
	}

	@Override
	public ScrollTabHolderFragment getItem(int pos) {
		ScrollTabHolderFragment fragment = fragments[pos];
		mScrollTabHolders.put(pos, fragment);
		if (mListener != null) {
			fragment.setScrollTabHolder(mListener);
		}
		return fragment;
	}

	public SparseArrayCompat<ScrollTabHolder> getScrollTabHolders() {
		return mScrollTabHolders;
	}

	@Override
	public int getCount() {
		return PageAdapterMyTab.values().length;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		PageAdapterMyTab tab = PageAdapterMyTab.fromTabIndex(position);
		if (tab != null) {
			return tab.name;
		} else {
			return "";
		}
	}

}
