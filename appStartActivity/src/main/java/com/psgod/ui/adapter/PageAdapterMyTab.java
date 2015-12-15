package com.psgod.ui.adapter;

import android.support.v4.app.Fragment;

//import com.psgod.ui.fragment.MyPageAskFragment;
import com.psgod.ui.fragment.MyPageCollectionFragment;
import com.psgod.ui.fragment.MyPageWorkFragment;

public enum PageAdapterMyTab {

//	PAGE_TAB1(0, MyPageAskFragment.class, "求P"), PAGE_TAB2(1,
//			MyPageWorkFragment.class, "作品"), PAGE_TAB3(2,
//			MyPageCollectionFragment.class, "收藏");

	PAGE_TAB2(0, MyPageWorkFragment.class, "作品"),
	PAGE_TAB3(1, MyPageCollectionFragment.class, "收藏");

	public final int tabIndex;
	public final Class<? extends Fragment> clazz;
	public String name;
	public final int fragmentId;

//	// 设置Tab1的文字
//	public static void setTAB1(String name) {
//		PAGE_TAB1.name = name;
//	}

	// 设置Tab2的文字
	public static void setTAB2(String name) {
		PAGE_TAB2.name = name;
	}

	// 设置Tab3的文字
	public static void setTAB3(String name) {
		PAGE_TAB3.name = name;
	}

	PageAdapterMyTab(int index, Class<? extends Fragment> clazz, String name) {
		this.tabIndex = index;
		this.clazz = clazz;
		this.name = name;
		this.fragmentId = index;
	}

	public static final PageAdapterMyTab fromTabIndex(int tabIndex) {
		for (PageAdapterMyTab value : PageAdapterMyTab.values()) {
			if (value.tabIndex == tabIndex) {
				return value;
			}
		}
		return null;
	}

}
