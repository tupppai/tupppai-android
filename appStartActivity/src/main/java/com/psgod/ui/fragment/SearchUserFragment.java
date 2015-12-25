package com.psgod.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.psgod.R;
import com.psgod.eventbus.SearchEvent;
import com.psgod.model.SearchUserData;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.SearchUserRequest;
import com.psgod.ui.adapter.SearchUserAdapter;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;

public class SearchUserFragment extends BaseFragment {
	private static final String TAG = UserProfileAskFragment.class
			.getSimpleName();

	private PullToRefreshListView listView;

	private SearchUserAdapter adapter;
	private List<SearchUserData> mItems = new ArrayList<SearchUserData>();

	private boolean canLoadMore = false;
	private View mFooterView;
	private int mPage = 1;
	private CustomProgressingDialog dialog;
	private String name = "";

	private TextView mEmptyTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);

	}

	public void onEventMainThread(SearchEvent event) {
		name = event.getSearch();
		dialog.show();
		initData();
	}

	private void initData() {
		if (name == null || name.equals("")) {

		} else {
			listView.setMode(Mode.PULL_FROM_START);
			SearchUserRequest request = new SearchUserRequest.Builder()
					.setListener(new Listener<List<SearchUserData>>() {

						@Override
						public void onResponse(List<SearchUserData> response) {
							mEmptyTextView.setText("抱歉！暂时没有找到你想要的！");

							if (mItems.size() > 0 && response != null) {
								mItems.clear();
							}
							mItems.addAll(response);
							adapter.notifyDataSetChanged();
							listView.onRefreshComplete();
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
							new PSGodErrorListener(SearchUserFragment.class
									.getSimpleName()) {
								@Override
								public void handleError(VolleyError error) {
									listView.onRefreshComplete();
									if (dialog.isShowing()) {
										dialog.dismiss();
									}
								}
							}).build();
			request.setTag(TAG);
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("name", name);
			params.put("page", String.valueOf(1));
			request.setParams(params);
			RequestQueue requestQueue = PSGodRequestQueue.getInstance(
					getActivity()).getRequestQueue();
			requestQueue.add(request);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_search_user, null);

		initView(view);
		initListener();

		return view;
	}

	private void initListener() {
		listView.setOnRefreshListener(new OnRefreshListener2() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase refreshView) {
				initData();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase refreshView) {

			}
		});
		listView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
			@Override
			public void onLastItemVisible() {
				if (canLoadMore) {
					mFooterView.setVisibility(View.VISIBLE);
					mPage = mPage + 1;
					SearchUserRequest request = new SearchUserRequest.Builder()
							.setListener(new Listener<List<SearchUserData>>() {
								@Override
								public void onResponse(
										List<SearchUserData> response) {
									mItems.addAll(response);
									adapter.notifyDataSetChanged();
									listView.onRefreshComplete();
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
											SearchUserFragment.class
													.getSimpleName()) {
										@Override
										public void handleError(
												VolleyError error) {
											listView.onRefreshComplete();
										}
									}).build();
					request.setTag(TAG);
					HashMap<String, String> params = new HashMap<String, String>();
					params.put("name", name);
					params.put("page", String.valueOf(mPage));
					request.setParams(params);
					RequestQueue requestQueue = PSGodRequestQueue.getInstance(
							getActivity()).getRequestQueue();
					requestQueue.add(request);
				}
			}
		});

		listView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView arg0, int arg1) {
				InputMethodManager imm = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(listView.getWindowToken(), 0);
			}

			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {

			}
		});

	}

	private void initView(View view) {
		listView = (PullToRefreshListView) view
				.findViewById(R.id.page_tab_listview);
		listView.setMode(Mode.DISABLED);
		mFooterView = LayoutInflater.from(getActivity()).inflate(
				R.layout.footer_load_more, null);
		listView.getRefreshableView().addFooterView(mFooterView);
		mFooterView.setVisibility(View.GONE);

		// 初始化listview
		adapter = new SearchUserAdapter(getActivity(), mItems);
		listView.setAdapter(adapter);

		View emptyView = view
				.findViewById(R.id.fragment_search_user_list_empty_view);
		mEmptyTextView = (TextView) view.findViewById(R.id.empty_text);
		mEmptyTextView.setText("");
		listView.setEmptyView(emptyView);

		dialog = new CustomProgressingDialog(getActivity());

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

}
