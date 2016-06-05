package com.pires.wesee.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pires.wesee.model.SearchWork;
import com.pires.wesee.ui.view.SearchWaterFallItemView;
import com.pires.wesee.R;

import java.util.List;

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
