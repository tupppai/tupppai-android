package com.psgod.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshListView.OnHeaderScrollListener;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.eventbus.MyPageRefreshEvent;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.UserPhotoRequest;
import com.psgod.ui.adapter.AskGridAdapter;
import com.psgod.ui.adapter.GridAdapter;
import com.psgod.ui.adapter.PageAdapterMyTab;
import com.psgod.ui.adapter.PhotoWaterFallListAdapter;
import com.psgod.ui.view.PullToRefreshStaggeredGridView;
import com.psgod.ui.view.PhotoWaterFallItemView.PhotoWaterFallListType;

import de.greenrobot.event.EventBus;

public class MyPageAskFragment extends ScrollTabHolderFragment {

	private static final String TAG = MyPageAskFragment.class.getSimpleName();

	private static final int MY_PAGE_ASK = 5;
	private Context mContext;
	private ViewHolder mViewHolder;
	private PhotoWaterFallListAdapter adapter;
	private List<PhotoItem> mItems;

	private String mSpKey;
	private static final long DEFAULT_LAST_REFRESH_TIME = -1;
	private long mLastUpdatedTime;
	private int mPage;
	private boolean canLoadMore = true;

	private int listViewHeight = 0; // listview的高度

	public MyPageAskFragment() {
		this.setFragmentId(PageAdapterMyTab.PAGE_TAB1.fragmentId);
	}

	public void onEventMainThread(MyPageRefreshEvent event) {
		if (event.getType() == 0) {
			refresh();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
		mContext = getActivity();
		mItems = new ArrayList<PhotoItem>();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater
				.inflate(R.layout.fragment_my_page_ask, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		initViews();
	}

	// 触发下拉自动刷新
	public void setRefreshing() {
		mViewHolder.listView.setRefreshing(true);
	}

	@SuppressLint("InflateParams")
	private void initViews() {
		mViewHolder = new ViewHolder();
		mViewHolder.listView = (PullToRefreshStaggeredGridView) getView()
				.findViewById(R.id.fragment_my_page_ask_grid_listview);
		mViewHolder.listView.setMode(Mode.DISABLED);
		mViewHolder.mFooterView = LayoutInflater.from(getActivity()).inflate(
				R.layout.footer_load_more, null);
		mViewHolder.listView.getRefreshableView().addFooterView(
				mViewHolder.mFooterView);
		mViewHolder.mFooterView.setVisibility(View.GONE);

		// 设置listView监听器
		setListViewListener();
		// 添加listview header
		listViewAddHeader();

		// 初始化listview
		adapter = new PhotoWaterFallListAdapter(mContext, mItems,
				PhotoWaterFallListType.USER_PROFILE_ASK);
		mViewHolder.listView.getRefreshableView().setAdapter(adapter);
		mViewHolder.listView.getRefreshableView().setTag("MyPageAsk");

		SharedPreferences sp = mContext.getSharedPreferences(
				Constants.SharedPreferencesKey.NAME, Context.MODE_PRIVATE);
		mSpKey = Constants.SharedPreferencesKey.MY_ASK_LIST_LAST_REFRESH_TIME;
		mLastUpdatedTime = sp.getLong(mSpKey, DEFAULT_LAST_REFRESH_TIME);

		refresh();
	}

	private void setListViewListener() {
		mViewHolder.listView
				.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
					@Override
					public void onLastItemVisible() {
						if (canLoadMore) {
							mViewHolder.mFooterView.setVisibility(View.VISIBLE);
							mPage = mPage + 1;
							UserPhotoRequest.Builder builder = new UserPhotoRequest.Builder()
									.setType(MY_PAGE_ASK).setPage(mPage)
									.setListener(loadMoreListener)
									.setErrorListener(errorListener);

							UserPhotoRequest request = builder.build();
							request.setTag(TAG);
							RequestQueue requestQueue = PSGodRequestQueue
									.getInstance(mContext).getRequestQueue();
							requestQueue.add(request);
						}
					}
				});

		mViewHolder.listView.setOnScrollListener(new OnScrollListener() {
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

		// mViewHolder.listView
		// .setOnHeaderScrollListener(new OnHeaderScrollListener() {
		// @Override
		// public void onHeaderScroll(boolean isRefreashing,
		// boolean istop, int value) {
		// if (scrollTabHolder != null && istop) {
		// scrollTabHolder.onHeaderScroll(isRefreashing,
		// value, getFragmentId());
		// }
		// }
		// });
	}

	private void listViewAddHeader() {
		mViewHolder.placeHolderView = new LinearLayout(getActivity());
		AbsListView.LayoutParams params = new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				getResources().getDimensionPixelSize(R.dimen.max_header_height));
		mViewHolder.placeHolderView.setLayoutParams(params);

		mViewHolder.listView.getRefreshableView().addHeaderView(
				mViewHolder.placeHolderView);
	}

	private void refresh() {
		canLoadMore = false;
		mPage = 1;

		if (mLastUpdatedTime == DEFAULT_LAST_REFRESH_TIME) {
			mLastUpdatedTime = System.currentTimeMillis();
		}

		UserPhotoRequest.Builder builder = new UserPhotoRequest.Builder()
				.setType(MY_PAGE_ASK).setPage(mPage)
				.setLastUpdated(mLastUpdatedTime).setListener(refreshListener)
				.setErrorListener(errorListener);

		UserPhotoRequest request = builder.build();
		request.setTag(TAG);
		RequestQueue requestQueue = PSGodRequestQueue.getInstance(mContext)
				.getRequestQueue();
		requestQueue.add(request);
	}

	private Listener<List<PhotoItem>> loadMoreListener = new Listener<List<PhotoItem>>() {
		@Override
		public void onResponse(List<PhotoItem> items) {
			if (items != null) {
				mItems.addAll(items);
			}
			adapter.notifyDataSetChanged();
			mViewHolder.listView.onRefreshComplete();

			mViewHolder.mFooterView.setVisibility(View.INVISIBLE);
			if (items.size() < 15) {
				canLoadMore = false;
			} else {
				canLoadMore = true;
			}
		}
	};

	private Listener<List<PhotoItem>> refreshListener = new Listener<List<PhotoItem>>() {
		@Override
		public void onResponse(List<PhotoItem> items) {
			if (items != null) {
				mItems.clear();
				mItems.addAll(items);
			}

			adapter.notifyDataSetChanged();
			mViewHolder.listView.onRefreshComplete();

			View emptyView = getView()
					.findViewById(R.id.my_page_ask_empty_view);
			mViewHolder.listView.setEmptyView(emptyView);

			// if (items.size() > 0) {
			// listViewHeight = getTotalHeightofListView();
			// if (listViewHeight < Constants.HEIGHT_OF_SCREEN) {
			// View footView = new LinearLayout(getActivity());
			// AbsListView.LayoutParams params = new LayoutParams(
			// android.view.ViewGroup.LayoutParams.MATCH_PARENT,
			// (Constants.HEIGHT_OF_SCREEN - listViewHeight
			// - Utils.dpToPx(getActivity(), 48) - getStatusBarHeight()));
			// footView.setLayoutParams(params);
			//
			// mViewHolder.listView.getRefreshableView().addFooterView(
			// footView);
			// }
			// }

			mLastUpdatedTime = System.currentTimeMillis();
			if (android.os.Build.VERSION.SDK_INT >= 9) {
				mContext.getApplicationContext()
						.getSharedPreferences(
								Constants.SharedPreferencesKey.NAME,
								Context.MODE_PRIVATE).edit()
						.putLong(mSpKey, mLastUpdatedTime).apply();
			} else {
				mContext.getApplicationContext()
						.getSharedPreferences(
								Constants.SharedPreferencesKey.NAME,
								Context.MODE_PRIVATE).edit()
						.putLong(mSpKey, mLastUpdatedTime).commit();
			}

			if (items.size() < 15) {
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
			View mView = adapter.getView(i, null, mViewHolder.listView);
			mView.measure(
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			totalHeight += mView.getMeasuredHeight();
		}
		return totalHeight;
	}

	private PSGodErrorListener errorListener = new PSGodErrorListener(
			UserProfileAskFragment.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {
			// TODO
			mViewHolder.listView.onRefreshComplete();
		}
	};

	@Override
	public void adjustScroll(int scrollHeight) {
		if (scrollHeight == 0
				&& mViewHolder.listView.getRefreshableView()
						.getFirstVisiblePosition() >= 2) {
			return;
		}

		// 切换后回到初始位置
		// mViewHolder.listView.getRefreshableView()
		// .setSelectionFromTop(
		// 1,
		// getResources().getDimensionPixelSize(
		// R.dimen.max_header_height));
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

	private static class ViewHolder {
		private PullToRefreshStaggeredGridView listView;
		private View placeHolderView;
		private View mFooterView;
	}
}
