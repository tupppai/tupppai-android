package com.psgod.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.psgod.R;
import com.psgod.eventbus.SearchEvent;
import com.psgod.model.SearchWork;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.SearchWorkRequest;
import com.psgod.ui.adapter.SearchWaterFallListAdapter;
import com.psgod.ui.view.PullToRefreshStaggeredGridView;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;

public class SearchWorkFragment extends BaseFragment {
	private static final String TAG = UserProfileAskFragment.class
			.getSimpleName();

	private String desc = "";
	private SearchWaterFallListAdapter adapter;
	private PullToRefreshStaggeredGridView mGridView;
	private List<SearchWork.Data> mItems = new ArrayList<SearchWork.Data>();;

	private View mEmptyView;
	private View mFooterView;

	private boolean canLoadMore = false;
	private int mPage = 1;

	private CustomProgressingDialog dialog;
	private TextView mEmptyTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
	}

	public void onEventMainThread(SearchEvent event) {
		desc = event.getSearch();
		dialog.show();
		initData();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_search_details,
				container, false);

		initView(view);
		initListener();
		return view;
	}

	private void initListener() {
		mGridView.setOnRefreshListener(new OnRefreshListener2() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase refreshView) {
				initData();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase refreshView) {

			}
		});

		mGridView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
			@Override
			public void onLastItemVisible() {
				if (canLoadMore) {
					mFooterView.setVisibility(View.VISIBLE);
					mPage = mPage + 1;
					SearchWorkRequest request = new SearchWorkRequest.Builder()
							.setListener(new Listener<List<SearchWork.Data>>() {

								@Override
								public void onResponse(
										List<SearchWork.Data> response) {

									mItems.addAll(response);
									adapter.notifyDataSetChanged();
									mGridView.onRefreshComplete();
									mFooterView.setVisibility(View.GONE);
									if (response.size() < 15) {
										canLoadMore = false;
									} else {
										canLoadMore = true;
									}
								}
							})
							.setErrorListener(
									new PSGodErrorListener(
											SearchWorkFragment.class
													.getSimpleName()) {
										@Override
										public void handleError(
												VolleyError error) {
											mGridView.onRefreshComplete();
										}
									}).build();
					request.setTag(TAG);
					HashMap<String, String> params = new HashMap<String, String>();
					params.put("desc", desc);
					params.put("page", String.valueOf(mPage));
					request.setParams(params);
					RequestQueue requestQueue = PSGodRequestQueue.getInstance(
							getActivity()).getRequestQueue();
					requestQueue.add(request);
				}
			}
		});
	}

	private void initView(View view) {
		mGridView = (PullToRefreshStaggeredGridView) view
				.findViewById(R.id.page_tab_listview);
		mGridView.setMode(Mode.DISABLED);
		mFooterView = LayoutInflater.from(getActivity()).inflate(
				R.layout.footer_load_more, null);
		mGridView.getRefreshableView().addFooterView(mFooterView);
		mFooterView.setVisibility(View.GONE);

		// 初始化listview
		adapter = new SearchWaterFallListAdapter(getActivity(), mItems);
		mGridView.setAdapter(adapter);

		View emptyView = view
				.findViewById(R.id.fragment_search_detail_list_empty_view);
		mEmptyTextView = (TextView) view.findViewById(R.id.empty_text);
		mEmptyTextView.setText("");
		mGridView.setEmptyView(emptyView);

		dialog = new CustomProgressingDialog(getActivity());
	}

	private void initData() {
		if (desc == null || desc.equals("")) {

		} else {
			mGridView.setMode(Mode.PULL_FROM_START);
			SearchWorkRequest request = new SearchWorkRequest.Builder()
					.setListener(new Listener<List<SearchWork.Data>>() {

						@Override
						public void onResponse(List<SearchWork.Data> response) {

							mEmptyTextView.setText("抱歉！暂时没有找到你想要的！");
							if (mItems.size() > 0 && response != null) {
								mItems.clear();
							}
							mItems.addAll(response);
							adapter.notifyDataSetChanged();
							mGridView.onRefreshComplete();
							if (dialog.isShowing()) {
								dialog.dismiss();
							}
							mFooterView.setVisibility(View.GONE);
							if (response.size() < 15) {
								canLoadMore = false;
							} else {
								canLoadMore = true;
							}
						}
					})
					.setErrorListener(
							new PSGodErrorListener(SearchWorkFragment.class
									.getSimpleName()) {
								@Override
								public void handleError(VolleyError error) {
									mGridView.onRefreshComplete();
									if (dialog.isShowing()) {
										dialog.dismiss();
									}
								}
							}).build();
			request.setTag(TAG);
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("desc", desc);
			params.put("page", String.valueOf(1));
			request.setParams(params);
			RequestQueue requestQueue = PSGodRequestQueue.getInstance(
					getActivity()).getRequestQueue();
			requestQueue.add(request);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}
}
