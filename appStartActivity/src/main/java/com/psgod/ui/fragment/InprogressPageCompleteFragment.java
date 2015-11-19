package com.psgod.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.eventbus.MyPageRefreshEvent;
import com.psgod.model.PhotoItem;
import com.psgod.network.NetworkUtil;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.UserPhotoRequest;
import com.psgod.ui.adapter.PhotoWaterFallListAdapter;
import com.psgod.ui.view.PhotoWaterFallItemView.PhotoWaterFallListType;
import com.psgod.ui.view.PullToRefreshStaggeredGridView;

import de.greenrobot.event.EventBus;

public class InprogressPageCompleteFragment extends BaseFragment {
	private static final String TAG = InprogressPageCompleteFragment.class
			.getSimpleName();

	private Context mContext;
	private ViewHolder mViewHolder;
	private List<PhotoItem> mPhotoItems = new ArrayList<PhotoItem>();
	private PhotoWaterFallListAdapter mCompleteAdapter;
	private InProgressPageCompleteListener mCompleteListener;
	private static final int MY_WORKS = 1;
	private int mPage = 1;
	private Boolean canloadMore = false;

	private String mSpKey;
	private static final long DEFAULT_LAST_REFRESH_TIME = -1;
	private long mLastUpdatedTime;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		EventBus.getDefault().register(this);

		FrameLayout parentview = new FrameLayout(getActivity());
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		parentview.setLayoutParams(params);

		mViewHolder = new ViewHolder();
		mViewHolder.mParentView = parentview;
		mViewHolder.mView = LayoutInflater.from(getActivity()).inflate(
				R.layout.fragment_inprogress_page_complete, parentview, true);
		mViewHolder.mGridView = (PullToRefreshStaggeredGridView) mViewHolder.mView
				.findViewById(R.id.fragment_inprogress_complete_gridview);
		mViewHolder.mGridView.setMode(Mode.PULL_FROM_START);
		mCompleteAdapter = new PhotoWaterFallListAdapter(mContext, mPhotoItems,
				PhotoWaterFallListType.INPROGRESS_COMPLETE);
		mViewHolder.mGridView.setAdapter(mCompleteAdapter);

		mViewHolder.mFooterView = LayoutInflater.from(mContext).inflate(
				R.layout.footer_load_more, null);
		mViewHolder.mGridView.getRefreshableView().addFooterView(
				mViewHolder.mFooterView);
		mViewHolder.mFooterView.setVisibility(View.GONE);

		// 初始化listener
		mCompleteListener = new InProgressPageCompleteListener();
		mViewHolder.mGridView.setOnRefreshListener(mCompleteListener);
		mViewHolder.mGridView.setOnLastItemVisibleListener(mCompleteListener);
		mViewHolder.mGridView.setScrollingWhileRefreshingEnabled(true);

		if (NetworkUtil.getNetworkType() != NetworkUtil.NetworkType.NONE) {
			mViewHolder.mGridView.setRefreshing(true);
		}

		refresh();
	}

	public void onEventMainThread(MyPageRefreshEvent event) {
		if (event.getType() == MyPageRefreshEvent.WORK) {
			refresh();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		FrameLayout parentview = new FrameLayout(getActivity());
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		parentview.setLayoutParams(params);

		mViewHolder.mParentView.removeView(mViewHolder.mView);
		parentview.addView(mViewHolder.mView);

		mViewHolder.mParentView = parentview;
		return parentview;
	}

	// 触发自动下拉刷新
	public void setRefreshing() {
		mViewHolder.mGridView.setRefreshing(true);
	}

	private class InProgressPageCompleteListener implements OnRefreshListener,
			OnLastItemVisibleListener {

		public InProgressPageCompleteListener() {
			SharedPreferences sp = mContext.getSharedPreferences(
					Constants.SharedPreferencesKey.NAME, Context.MODE_PRIVATE);
			mSpKey = Constants.SharedPreferencesKey.INPROGRESS_COMPLETE_LIST_LAST_REFRESH_TIME;
			mLastUpdatedTime = sp.getLong(mSpKey, DEFAULT_LAST_REFRESH_TIME);
		}

		@Override
		public void onLastItemVisible() {
			if (canloadMore) {
				mPage++;
				mViewHolder.mFooterView.setVisibility(View.VISIBLE);
				UserPhotoRequest.Builder builder = new UserPhotoRequest.Builder()
						.setType(MY_WORKS).setPage(mPage)
						.setListener(loadMoreListener)
						.setErrorListener(errorListener);
				UserPhotoRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						mContext).getRequestQueue();
				requestQueue.add(request);
			}
		}

		@Override
		public void onRefresh(PullToRefreshBase refreshView) {
			refresh();
		}

	}

	public void refresh() {
		canloadMore = false;
		mPage = 1;

		if (mLastUpdatedTime == DEFAULT_LAST_REFRESH_TIME) {
			mLastUpdatedTime = System.currentTimeMillis();
		}

		UserPhotoRequest.Builder builder = new UserPhotoRequest.Builder()
				.setType(MY_WORKS).setPage(mPage)
				.setLastUpdated(mLastUpdatedTime).setListener(refreshListener)
				.setErrorListener(errorListener);
		UserPhotoRequest request = builder.build();
		request.setTag(TAG);
		RequestQueue requestQueue = PSGodRequestQueue.getInstance(mContext)
				.getRequestQueue();
		requestQueue.add(request);
	}

	private Listener<List<PhotoItem>> refreshListener = new Listener<List<PhotoItem>>() {
		@Override
		public void onResponse(List<PhotoItem> items) {
			mViewHolder.mGridView.onRefreshComplete();

			if (items.size() < 15) {
				canloadMore = false;
			} else {
				canloadMore = true;
			}

			mViewHolder.mEmptyView = mViewHolder.mView
					.findViewById(R.id.inprogress_fragment_complete_empty_view);
			mViewHolder.mGridView.setEmptyView(mViewHolder.mEmptyView);

			mPhotoItems.clear();
			mPhotoItems.addAll(items);
			mCompleteAdapter.notifyDataSetChanged();

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

		}
	};

	private Listener<List<PhotoItem>> loadMoreListener = new Listener<List<PhotoItem>>() {
		@Override
		public void onResponse(List<PhotoItem> items) {
			mPhotoItems.addAll(items);
			mViewHolder.mGridView.onRefreshComplete();
			mCompleteAdapter.notifyDataSetChanged();

			mViewHolder.mFooterView.setVisibility(View.INVISIBLE);

			if (items.size() < 15) {
				canloadMore = false;
			} else {
				canloadMore = true;
			}
		}
	};

	private PSGodErrorListener errorListener = new PSGodErrorListener(
			InprogressPageAskFragment.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {
			mViewHolder.mGridView.onRefreshComplete();
		}
	};

	/**
	 * 暂停所有的下载
	 */
	@Override
	public void onStop() {
		super.onStop();
		RequestQueue requestQueue = PSGodRequestQueue.getInstance(mContext)
				.getRequestQueue();
		requestQueue.cancelAll(TAG);
	}

	private static class ViewHolder {
		ViewGroup mParentView;
		View mView;
		PullToRefreshStaggeredGridView mGridView;
		View mEmptyView;
		View mFooterView;
	}

}
