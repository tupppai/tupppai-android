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

public class CountryAdapter extends AbstractWheelTextAdapter {
	private Context mContext;
	// Countries names
	// private String countries[] = AddressData.PROVINCES;
	private String countries[] = CityInfo.getProvinceName();

	/**
	 * Constructor
	 */
	public CountryAdapter(Context context) {
		super(context, R.layout.country_layout, TEXT_VIEW_ITEM_RESOURCE);
		mContext = context;
		setItemTextResource(R.id.country_name);
	}

	@Override
	public View getItem(int index, View cachedView, ViewGroup parent) {
		View view = super.getItem(index, cachedView, parent);
		return view;
	}

	@Override
	public int getItemsCount() {
		return countries.length;
	}

	@Override
	protected CharSequence getItemText(int index) {
		return countries[index];
	}
}
