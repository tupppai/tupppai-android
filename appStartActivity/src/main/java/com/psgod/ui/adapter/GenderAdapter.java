package com.psgod.ui.adapter;

/**
 * 城市选择器 country adapter
 * @author brandwang
 */

import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.psgod.R;

public class GenderAdapter extends AbstractWheelTextAdapter {
	// Countries names
	private String genders[] = { "男", "女" };

	/**
	 * Constructor
	 */
	public GenderAdapter(Context context) {
		super(context, R.layout.country_layout, TEXT_VIEW_ITEM_RESOURCE);
		setItemTextResource(R.id.country_name);
	}

	@Override
	public View getItem(int index, View cachedView, ViewGroup parent) {
		View view = super.getItem(index, cachedView, parent);
		return view;
	}

	@Override
	public int getItemsCount() {
		return genders.length;
	}

	@Override
	protected CharSequence getItemText(int index) {
		return genders[index];
	}
}
