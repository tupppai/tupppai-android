package com.psgod.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.eventbus.MyPageRefreshEvent;
import com.psgod.model.PhotoItem;
import com.psgod.network.NetworkUtil;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.UserPhotoRequest;
import com.psgod.ui.activity.CarouselPhotoDetailActivity;
import com.psgod.ui.activity.SinglePhotoDetail;
import com.psgod.ui.adapter.InprogressPageAskAdapter;

import de.greenrobot.event.EventBus;

/**
 * 进行中 求P Fragment
 * 
 * @author ZouMengyuan
 * 
 */
public class InprogressPageAskFragment extends Fragment {

	private static final String TAG = InprogressPageAskFragment.class
			.getSimpleName();

	private Context mContext;
	private ViewHolder mViewHolder;
	private List<PhotoItem> mPhotoItems = new ArrayList<PhotoItem>();
	private InprogressPageAskAdapter mAskAdapter;
	private InprogressingAskListener mInprogressingAskListener;

	private String mSpKey;
	private static final long DEFAULT_LAST_REFRESH_TIME = -1;
	private long mLastUpdatedTime;

	private static final int MY_ASK = 0;
	private int mPage = 1;
	// 控制是否可以加载下一页
	private boolean canLoadMore = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
		mContext = getActivity();

		FrameLayout parentview = new FrameLayout(getActivity());
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		parentview.setLayoutParams(params);

		mViewHolder = new ViewHolder();
		mViewHolder.mParentView = parentview;
		mViewHolder.mView = LayoutInflater.from(getActivity()).inflate(
				R.layout.fragment_inprogressing_ask, parentview, true);
		mViewHolder.mListView = (PullToRefreshListView) mViewHolder.mView
				.findViewById(R.id.fragment_inprogress_ask_lv);
		mViewHolder.mListView.setMode(Mode.PULL_FROM_START);

		mAskAdapter = new InprogressPageAskAdapter(mContext, mPhotoItems);
		mViewHolder.mListView.setAdapter(mAskAdapter);

		mViewHolder.mFooterView = LayoutInflater.from(mContext).inflate(
				R.layout.footer_load_more, null);
		mViewHolder.mListView.getRefreshableView().addFooterView(
				mViewHolder.mFooterView);
		mViewHolder.mFooterView.setVisibility(View.GONE);

		mInprogressingAskListener = new InprogressingAskListener();
		mViewHolder.mListView.setOnRefreshListener(mInprogressingAskListener);
		mViewHolder.mListView
				.setOnLastItemVisibleListener(mInprogressingAskListener);
		mViewHolder.mListView.setScrollingWhileRefreshingEnabled(true);

		if (NetworkUtil.getNetworkType() != NetworkUtil.NetworkType.NONE) {
			mViewHolder.mListView.setRefreshing(true);
		}

	}

	public void onEventMainThread(MyPageRefreshEvent event) {
		if (event.getType() == MyPageRefreshEvent.ASK) {
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
		mViewHolder.mListView.setRefreshing(true);
	}

	private class InprogressingAskListener implements OnRefreshListener,
			OnLastItemVisibleListener {

		public InprogressingAskListener() {
			SharedPreferences sp = mContext.getSharedPreferences(
					Constants.SharedPreferencesKey.NAME, Context.MODE_PRIVATE);
			mSpKey = Constants.SharedPreferencesKey.INPROGRESS_ASK_LIST_LAST_REFRESH_TIME;
			mLastUpdatedTime = sp.getLong(mSpKey, DEFAULT_LAST_REFRESH_TIME);

		}

		@Override
		public void onLastItemVisible() {
			if (canLoadMore) {
				mPage++;

				mViewHolder.mFooterView.setVisibility(View.VISIBLE);

				UserPhotoRequest.Builder builder = new UserPhotoRequest.Builder()
						.setType(MY_ASK).setPage(mPage)
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
		canLoadMore = false;
		mPage = 1;

		if (mLastUpdatedTime == DEFAULT_LAST_REFRESH_TIME) {
			mLastUpdatedTime = System.currentTimeMillis();
		}

		UserPhotoRequest.Builder builder = new UserPhotoRequest.Builder()
				.setType(MY_ASK).setPage(mPage)
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
			mViewHolder.mListView.onRefreshComplete();

			if (items.size() < 15) {
				canLoadMore = false;
			} else {
				canLoadMore = true;
			}

			mViewHolder.mEmptyView = mViewHolder.mView
					.findViewById(R.id.fragment_ask_empty_view);
			mViewHolder.mListView.setEmptyView(mViewHolder.mEmptyView);

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

			mPhotoItems.clear();
			mPhotoItems.addAll(items);
			mAskAdapter.notifyDataSetChanged();

		}
	};

	private Listener<List<PhotoItem>> loadMoreListener = new Listener<List<PhotoItem>>() {
		@Override
		public void onResponse(List<PhotoItem> items) {
			if (items.size() > 0) {
				mPhotoItems.addAll(items);
				mViewHolder.mListView.onRefreshComplete();
				mAskAdapter.notifyDataSetChanged();
			}

			mViewHolder.mFooterView.setVisibility(View.INVISIBLE);

			if (items.size() < 15) {
				canLoadMore = false;
			} else {
				canLoadMore = true;
			}
		}
	};

	private PSGodErrorListener errorListener = new PSGodErrorListener(
			InprogressPageAskFragment.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {
			mViewHolder.mListView.onRefreshComplete();
			;
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
		PullToRefreshListView mListView;
		View mEmptyView;
		View mFooterView;
	}
}
