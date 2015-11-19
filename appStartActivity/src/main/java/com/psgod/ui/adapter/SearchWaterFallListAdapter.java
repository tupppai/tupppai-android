package com.psgod.ui.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.psgod.R;
import com.psgod.model.SearchWork;
import com.psgod.ui.view.SearchWaterFallItemView;

public class SearchWaterFallListAdapter extends MyBaseAdapter<SearchWork.Data> {

	public SearchWaterFallListAdapter(Context context,
			List<SearchWork.Data> list) {
		super(context, list);
	}

	@Override
	View initView(int position, View view, ViewGroup parent) {
		SearchWaterFallItemView searchWaterFallItemView;
		final SearchWork.Data data = list.get(position);
		if (view == null) {
			view = LayoutInflater.from(context).inflate(
					R.layout.widget_search_water_fall_item_view, null);
			searchWaterFallItemView = (SearchWaterFallItemView) view;
			searchWaterFallItemView.initSearchWaterFallList();
		} else {
			searchWaterFallItemView = (SearchWaterFallItemView) view;
			searchWaterFallItemView.initSearchWaterFallList();
		}
		searchWaterFallItemView.setData(data);

		return view;
	}

}
