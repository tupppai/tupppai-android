package com.psgod.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.eventbus.MyPageRefreshEvent;
import com.psgod.model.LoginUser;
import com.psgod.model.PhotoItem;
import com.psgod.network.NetworkUtil;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.UserPhotoRequest;
import com.psgod.ui.adapter.InprogressPageReplyAdapter;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class InprogressPageReplyFragment extends Fragment {
	private static final String TAG = InprogressPageReplyFragment.class
			.getSimpleName();

	private Context mContext;
	private ViewHolder mViewHolder;
	private List<PhotoItem> mPhotoItems = new ArrayList<PhotoItem>();
	private InprogressPageReplyAdapter mReplyAdapter;
	private MyInProgressListener mInProgressListener;

	public static final int MY_INPROGRESS = 2;
	private int mPage = 1;

	private String mSpKey;
	private static final long DEFAULT_LAST_REFRESH_TIME = -1;
	private long mLastUpdatedTime;
	// 控制是否可以加载下一页
	private boolean canLoadMore = false;

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
				R.layout.fragment_inprogress_page_reply, parentview, true);
		mViewHolder.mListView = (PullToRefreshListView) mViewHolder.mView
				.findViewById(R.id.inprogress_reply_listview);
		mViewHolder.mListView.setMode(Mode.PULL_FROM_START);
		mReplyAdapter = new InprogressPageReplyAdapter(mContext, mPhotoItems);
		mViewHolder.mListView.getRefreshableView().setAdapter(mReplyAdapter);

		mInProgressListener = new MyInProgressListener(mContext);
		mViewHolder.mListView.setOnRefreshListener(mInProgressListener);
		mViewHolder.mListView.setOnLastItemVisibleListener(mInProgressListener);
		mViewHolder.mListView.setScrollingWhileRefreshingEnabled(true);

		mViewHolder.mEmptyView = mViewHolder.mView
				.findViewById(R.id.inprogress_fragment_reply_empty_view);

		mViewHolder.mFootView = LayoutInflater.from(mContext).inflate(
				R.layout.footer_load_more, null);
		mViewHolder.mFootView.setVisibility(View.GONE);
		mViewHolder.mListView.getRefreshableView().addFooterView(
				mViewHolder.mFootView);

		// 如果当前未使用手机号登录，则不刷新，否则会弹出两次绑定手机号activity
		if ((NetworkUtil.getNetworkType() != NetworkUtil.NetworkType.NONE) && !LoginUser.getInstance().getPhoneNum().equals("0")) {
			mViewHolder.mListView.setRefreshing(true);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	public void onEventMainThread(MyPageRefreshEvent event) {
		if (event.getType() == MyPageRefreshEvent.REPLY) {
			refresh();
		}
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

	// 触发自动刷新
	public void setRefreshing() {
		mViewHolder.mListView.setRefreshing(true);
	}

	private class MyInProgressListener implements OnLastItemVisibleListener,
			OnRefreshListener {
		private Context mContext;

		public MyInProgressListener(Context context) {
			mContext = context;

			SharedPreferences sp = mContext.getSharedPreferences(
					Constants.SharedPreferencesKey.NAME, Context.MODE_PRIVATE);
			mSpKey = Constants.SharedPreferencesKey.INPROGRESS_REPLY_LIST_LAST_REFRESH_TIME;
			mLastUpdatedTime = sp.getLong(mSpKey, DEFAULT_LAST_REFRESH_TIME);
		}

		@Override
		public void onLastItemVisible() {
			if (canLoadMore) {
				mPage = mPage + 1;
				mViewHolder.mFootView.setVisibility(View.VISIBLE);
				// 上拉加载更多
				UserPhotoRequest.Builder builder = new UserPhotoRequest.Builder()
						.setType(MY_INPROGRESS).setPage(mPage)
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

	// 刷新操作
	private void refresh() {
		mPage = 1;
		canLoadMore = false;

		if (mLastUpdatedTime == DEFAULT_LAST_REFRESH_TIME) {
			mLastUpdatedTime = System.currentTimeMillis();
		}

		UserPhotoRequest.Builder builder = new UserPhotoRequest.Builder()
				.setType(MY_INPROGRESS).setPage(mPage)
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
			mPhotoItems.clear();
			mPhotoItems.addAll(items);
			mReplyAdapter.notifyDataSetChanged();
			mViewHolder.mListView.onRefreshComplete();

			mViewHolder.mListView.getRefreshableView().setEmptyView(
					mViewHolder.mEmptyView);

			if (items.size() < 15) {
				canLoadMore = false;
			} else {
				canLoadMore = true;
			}

			// 保存本次刷新时间到sp
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
			if (items.size() > 0) {
				mPhotoItems.addAll(items);
			}
			mReplyAdapter.notifyDataSetChanged();
			mViewHolder.mListView.onRefreshComplete();

			mViewHolder.mFootView.setVisibility(View.INVISIBLE);

			if (items.size() < 15) {
				canLoadMore = false;
			} else {
				canLoadMore = true;
			}
		}
	};

	private PSGodErrorListener errorListener = new PSGodErrorListener(
			UserPhotoRequest.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {
			// TODO
			mViewHolder.mListView.onRefreshComplete();
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
		View mFootView;
	}

}
