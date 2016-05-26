package com.psgod.ui.adapter;

import android.support.v4.app.Fragment;

import com.psgod.ui.fragment.UserProfileAskFragment;
import com.psgod.ui.fragment.UserProfileWorkFragment;

public enum PageAdapterTab {
	PAGE_TAB1(0, UserProfileWorkFragment.class, "跟帖"), PAGE_TAB2(1,
			UserProfileAskFragment.class, "ta的动态");

	public final int tabIndex;
	public final Class<? extends Fragment> clazz;
	public String name;
	public final int fragmentId;

	// 设置Tab1的文字
	public static void setTAB1(String name) {
		PAGE_TAB1.name = name;
	}

	// 设置Tab2的文字
	public static void setTAB2(String name) {
		PAGE_TAB2.name = name;
	}

	PageAdapterTab(int index, Class<? extends Fragment> clazz, String name) {
		this.tabIndex = index;
		this.clazz = clazz;
		this.name = name;
		this.fragmentId = index;
	}

	public static final PageAdapterTab fromTabIndex(int tabIndex) {
		for (PageAdapterTab value : PageAdapterTab.values()) {
			if (value.tabIndex == tabIndex) {
				return value;
			}
		}
		return null;
	}
}
