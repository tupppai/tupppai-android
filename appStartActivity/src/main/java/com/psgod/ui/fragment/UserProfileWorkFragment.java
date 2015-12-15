package com.psgod.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.UserDetailRequest;
import com.psgod.network.request.UserDetailRequest.UserDetailResult;
import com.psgod.ui.adapter.PageAdapterTab;
import com.psgod.ui.adapter.PhotoWaterFallListAdapter;
import com.psgod.ui.view.PhotoWaterFallItemView.PhotoWaterFallListType;
import com.psgod.ui.view.PullToRefreshStaggeredGridView;

import java.util.ArrayList;
import java.util.List;

public class UserProfileWorkFragment extends ScrollTabHolderFragment {
	private static final String TAG = UserProfileWorkFragment.class
			.getSimpleName();
	private static final int WORK_ITEMS = 2;

	private PullToRefreshStaggeredGridView mGridview;
	private View placeHolderView;

	private PhotoWaterFallListAdapter adapter;
	private List<PhotoItem> mItems;

	private long mUid;
	private int mPage;

	private boolean canLoadMore = true;
	private View mFooterView;

	private int listViewHeight = 0; // listview的高度

	public UserProfileWorkFragment() {
		this.setFragmentId(PageAdapterTab.PAGE_TAB1.fragmentId);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mUid = Constants.CURRENT_OTHER_USER_ID;
		// 初始化数组
		mItems = new ArrayList<PhotoItem>();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_user_profile_works,
				container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		initViews();
	}

	@SuppressLint("InflateParams")
	private void initViews() {
		mGridview = (PullToRefreshStaggeredGridView) getView().findViewById(
				R.id.page_tab_listview);
		mGridview.setMode(Mode.DISABLED);
		mFooterView = LayoutInflater.from(getActivity()).inflate(
				R.layout.footer_load_more, null);
		mGridview.getRefreshableView().addFooterView(mFooterView);
		mFooterView.setVisibility(View.GONE);

		// 设置listView监听器
		setListViewListener();
		// 添加listview header
		listViewAddHeader();

		// 初始化listview
		adapter = new PhotoWaterFallListAdapter(getActivity(), mItems,
				PhotoWaterFallListType.USER_PROFILE_WORKS);
		mGridview.setAdapter(adapter);

		refresh();
	}

	private void setListViewListener() {
		mGridview.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
			@Override
			public void onLastItemVisible() {
				if (canLoadMore) {
					mFooterView.setVisibility(View.VISIBLE);
					mPage = mPage + 1;
					UserDetailRequest.Builder builder = new UserDetailRequest.Builder()
							.setType(WORK_ITEMS).setPage(mPage).setUserId(mUid)
							.setListener(loadMoreListener)
							.setErrorListener(errorListener);

					UserDetailRequest request = builder.build();
					request.setTag(TAG);
					RequestQueue requestQueue = PSGodRequestQueue.getInstance(
							getActivity()).getRequestQueue();
					requestQueue.add(request);
				}
			}
		});

		mGridview.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (scrollTabHolder != null) {
					scrollTabHolder.onScroll(view, firstVisibleItem,
							visibleItemCount, totalItemCount, getFragmentId());
				}
			}
		});

		// TODO
		// mGridview.setOnHeaderScrollListener(new OnHeaderScrollListener() {
		// @Override
		// public void onHeaderScroll(boolean isRefreashing, boolean istop, int
		// value) {
		// if (scrollTabHolder != null && istop) {
		// scrollTabHolder.onHeaderScroll(isRefreashing, value,
		// getFragmentId());
		// }
		// }
		// });
	}

	private void listViewAddHeader() {
		placeHolderView = new LinearLayout(getActivity());
		AbsListView.LayoutParams params = new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				getResources().getDimensionPixelSize(
						R.dimen.max_header_height_profile));
		placeHolderView.setLayoutParams(params);
		placeHolderView.setBackground(getResources().getDrawable(R.color.transparent));

		mGridview.getRefreshableView().addHeaderView(placeHolderView);
	}

	private void refresh() {
		mPage = 1;
		UserDetailRequest.Builder builder = new UserDetailRequest.Builder()
				.setType(WORK_ITEMS).setPage(mPage).setUserId(mUid)
				.setListener(refreshListener).setErrorListener(errorListener);

		UserDetailRequest request = builder.build();
		request.setTag(TAG);
		RequestQueue requestQueue = PSGodRequestQueue
				.getInstance(getActivity()).getRequestQueue();
		requestQueue.add(request);
	}

	private Listener<UserDetailResult> loadMoreListener = new Listener<UserDetailResult>() {
		@Override
		public void onResponse(UserDetailResult items) {
			if (items != null) {
				mItems.addAll(items.getWorkItems());
			}
			adapter.notifyDataSetChanged();
			mGridview.onRefreshComplete();

			mFooterView.setVisibility(View.INVISIBLE);
			if (items.getWorkItems().size() < 15) {
				canLoadMore = false;
			} else {
				canLoadMore = true;
			}
		}
	};

	private Listener<UserDetailResult> refreshListener = new Listener<UserDetailResult>() {
		@Override
		public void onResponse(UserDetailResult items) {
			if (items != null) {
				mItems.clear();
				mItems.addAll(items.getWorkItems());
			}

			adapter.notifyDataSetChanged();
			mGridview.onRefreshComplete();

			if (getView() != null) {
				View emptyView = getView().findViewById(
						R.id.fragment_profile_reply_list_empty_view);
				mGridview.setEmptyView(emptyView);
			}

			// if (items.getWorkItems().size() > 0) {
			// listViewHeight = getTotalHeightofListView();
			// if (listViewHeight < Constants.HEIGHT_OF_SCREEN) {
			// View footView = new LinearLayout(getActivity());
			// AbsListView.LayoutParams params = new LayoutParams(
			// AbsListView.LayoutParams.MATCH_PARENT,
			// (Constants.HEIGHT_OF_SCREEN-
			// listViewHeight-Utils.dpToPx(getActivity(),
			// 48)-getStatusBarHeight()));
			// footView.setLayoutParams(params);
			//
			// mGridview.getRefreshableView().addFooterView(footView);
			// }
			// }

			if (items.getWorkItems().size() < 15) {
				canLoadMore = false;
			} else {
				canLoadMore = true;
			}
		}
	};

	// 获取状态栏的高度
	public int getStatusBarHeight() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height",
				"dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	// 获取listview的高度
	public int getTotalHeightofListView() {
		int totalHeight = 0;
		for (int i = 0; i < adapter.getCount(); i++) {
			View mView = adapter.getView(i, null, mGridview);
			mView.measure(
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			totalHeight += mView.getMeasuredHeight();
		}
		return totalHeight;
	}

	private PSGodErrorListener errorListener = new PSGodErrorListener(
			UserProfileWorkFragment.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {
			// TODO
			mGridview.onRefreshComplete();
		}
	};

	@Override
	public void adjustScroll(int scrollHeight) {
		if (scrollHeight == 0
				&& mGridview.getRefreshableView().getFirstVisiblePosition() >= 2) {
			return;
		}

		// 切换后回到初始位置
		// mGridview.getRefreshableView().setSelectionFromTop(1, getResources()
		// .getDimensionPixelSize(R.dimen.max_header_height_profile));
	}

	public int getScrollY(AbsListView view) {
		View c = view.getChildAt(0);
		if (c == null) {
			return 0;
		}
		int top = c.getTop();
		int firstVisiblePosition = view.getFirstVisiblePosition();
		if (firstVisiblePosition == 0) {
			return -top;
		} else if (firstVisiblePosition == 1) {
			return top;
		} else {
			return -top + (firstVisiblePosition - 2) * c.getHeight() + 683;
		}
	}
}