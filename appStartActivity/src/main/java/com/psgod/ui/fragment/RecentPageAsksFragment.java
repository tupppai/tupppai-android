package com.psgod.ui.fragment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler.Callback;
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
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.psgod.Constants;
import com.psgod.Logger;
import com.psgod.R;
import com.psgod.ThreadManager;
import com.psgod.WeakReferenceHandler;
import com.psgod.db.DatabaseHelper;
import com.psgod.eventbus.RefreshEvent;
import com.psgod.model.PhotoItem;
import com.psgod.network.NetworkUtil;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.PhotoListRequest;
import com.psgod.ui.activity.CommentListActivity;
import com.psgod.ui.adapter.PhotoWaterFallListAdapter;
import com.psgod.ui.view.PhotoWaterFallItemView.PhotoWaterFallListType;
import com.psgod.ui.view.PullToRefreshStaggeredGridView;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import de.greenrobot.event.EventBus;

public class RecentPageAsksFragment extends BaseFragment implements Callback {
	private static final String TAG = RecentPageAsksFragment.class
			.getSimpleName();

	private Context mContext;
	private ViewHolder mViewHolder;
	private PhotoWaterFallListAdapter mAskAdapter;
	private List<PhotoItem> mPhotoItems = new ArrayList<PhotoItem>();
	private RecentPageAsksListener mRecentPageAsksListener;
	private DatabaseHelper mDatabaseHelper = null;
	private Dao<PhotoItem, Long> mPhotoItemDao;
	private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);

	private String mSpKey;
	private static final long DEFAULT_LAST_REFRESH_TIME = -1;
	private long mLastUpdatedTime;

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
				R.layout.fragment_recent_page_ask, parentview, true);
		mViewHolder.mGridView = (PullToRefreshStaggeredGridView) mViewHolder.mView
				.findViewById(R.id.fragment_inprogress_ask_staggered_gridview);
		mViewHolder.mGridView.setMode(Mode.PULL_FROM_START);

		mAskAdapter = new PhotoWaterFallListAdapter(mContext, mPhotoItems,
				PhotoWaterFallListType.RECENT_ASK);
		mViewHolder.mGridView.setAdapter(mAskAdapter);

		mViewHolder.mFooterView = LayoutInflater.from(mContext).inflate(
				R.layout.footer_load_more, null);
		mViewHolder.mGridView.getRefreshableView().addFooterView(
				mViewHolder.mFooterView);
		mViewHolder.mFooterView.setVisibility(View.GONE);

		// 初始化listener
		mRecentPageAsksListener = new RecentPageAsksListener();
		mViewHolder.mGridView.setOnRefreshListener(mRecentPageAsksListener);
		mViewHolder.mGridView
				.setOnLastItemVisibleListener(mRecentPageAsksListener);
		mViewHolder.mGridView.setScrollingWhileRefreshingEnabled(true);

		if (NetworkUtil.getNetworkType() != NetworkUtil.NetworkType.NONE) {
			mViewHolder.mGridView.setRefreshing(true);
		}

		// TODO 检测耗时
		try {
			mDatabaseHelper = OpenHelperManager.getHelper(mContext,
					DatabaseHelper.class);
			mPhotoItemDao = mDatabaseHelper.getDao(PhotoItem.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		dialog = new CustomProgressingDialog(getActivity());

		loadDataAsync();

		refresh(1);

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

	// 触发下拉自动刷新
	private void setRefreshing() {
//		mViewHolder.mGridView.getRefreshableView().smoothScrollToPositionFromTop(0,0);//By(0,0);
		mViewHolder.mGridView.setRefreshing(true);
	}

	public void onEventMainThread(RefreshEvent event) {
		if(event.className.equals(this.getClass().getName())){
			setRefreshing();
		}
	}

	private class RecentPageAsksListener implements OnRefreshListener,
			OnLastItemVisibleListener {

		public RecentPageAsksListener() {
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

				PhotoListRequest.Builder builder = new PhotoListRequest.Builder()
						.setPage(mPage).setLastUpdated(mLastUpdatedTime)
						.setType(PhotoItem.TYPE_RECENT_ASK)
						.setListener(loadMoreListener)
						.setErrorListener(errorListener);

				PhotoListRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						mContext).getRequestQueue();
				requestQueue.add(request);
			}
		}

		@Override
		public void onRefresh(PullToRefreshBase refreshView) {
			refresh(0);
		}
	}

	private CustomProgressingDialog dialog;

	public void refresh(int i) {
		if (i == 1 && dialog != null) {
			dialog.show();
		}
		canLoadMore = false;
		mPage = 1;

		if (mLastUpdatedTime == DEFAULT_LAST_REFRESH_TIME) {
			mLastUpdatedTime = System.currentTimeMillis();
		}

		PhotoListRequest.Builder builder = new PhotoListRequest.Builder()
				.setPage(mPage).setLastUpdated(mLastUpdatedTime)
				.setType(PhotoItem.TYPE_RECENT_ASK)
				.setListener(refreshListener).setErrorListener(errorListener);

		PhotoListRequest request = builder.build();
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
				canLoadMore = false;
			} else {
				canLoadMore = true;
			}

			mViewHolder.mEmptyView = mViewHolder.mView
					.findViewById(R.id.fragment_ask_empty_view);
			mViewHolder.mGridView.setEmptyView(mViewHolder.mEmptyView);

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
			PhotoItem.savePhotoList(getActivity(), mPhotoItemDao, items,
					PhotoItem.TYPE_RECENT_ASK);
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
		}
	};

	private Listener<List<PhotoItem>> loadMoreListener = new Listener<List<PhotoItem>>() {
		@Override
		public void onResponse(List<PhotoItem> items) {
			if (items.size() > 0) {
				mPhotoItems.addAll(items);
				mAskAdapter.notifyDataSetChanged();
				mViewHolder.mGridView.onRefreshComplete();
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
			RecentPageAsksFragment.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {
			mViewHolder.mGridView.onRefreshComplete();
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
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

	@Override
	public void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	private static class ViewHolder {
		ViewGroup mParentView;
		View mView;
		PullToRefreshStaggeredGridView mGridView;
		View mEmptyView;
		View mFooterView;
	}

	private void loadDataAsync() {

		ThreadManager.executeOnFileThread(new Runnable() {
			@Override
			public void run() {
				try {
					// mDbPhotoItems = mPhotoItemDao.queryForAll();
					List<PhotoItem> items = mPhotoItemDao.queryBuilder()
							.orderBy("update_time", false).where()
							.eq("from", PhotoItem.TYPE_RECENT_ASK).query();
					// mPhotoItems.addAll(items);

					Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR,
							TAG, "loadDataAsync(): size=" + items.size());

					Message msg = mHandler.obtainMessage();
					msg.obj = items;
					msg.sendToTarget();
				} catch (SQLException e) {
					// Log.e(LOG_TAG, "Database exception", e);
					// tv.setText("Database exeption: " + e);
					return;
				}
			}
		});
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.obj instanceof List<?>) {
			mPhotoItems.clear();
			List<PhotoItem> items = (List<PhotoItem>) msg.obj;
			mPhotoItems.addAll(items);
			mAskAdapter.notifyDataSetChanged();
			if (NetworkUtil.getNetworkType() != NetworkUtil.NetworkType.NONE) {
				mViewHolder.mGridView.setRefreshing(true);
			}
		}
		return true;
	}
}
