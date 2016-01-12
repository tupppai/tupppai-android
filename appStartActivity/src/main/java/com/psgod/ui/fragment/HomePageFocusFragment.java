package com.psgod.ui.fragment;

/**
 * 首页关注 
 * Fragment
 * @author brandwang
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
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
import com.psgod.ui.adapter.PhotoListAdapter;
import com.psgod.ui.view.PhotoItemView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class HomePageFocusFragment extends BaseFragment implements Callback {
	private static final String TAG = RecentPageAsksFragment.class
			.getSimpleName();

	private Context mContext;
	private ViewHolder mViewHolder;
	private List<PhotoItem> mHotPhotoItems;
	private DatabaseHelper mDatabaseHelper = null;
	private Dao<PhotoItem, Long> mPhotoItemDao;
	private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);

	// 带评论
	private PhotoListAdapter mAdapter;
	private PhotoListListener mListener;
	private View mFollowListFooter;
	private int mPage = 1;

	// 上次刷新时间
	private long mLastUpdatedTime;
	// 列表的类型
	private String mSpKey;

	private View mEmptyView;

	// 控制是否可以加载下一页
	private boolean canLoadMore = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logger.logMethod(TAG, "onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		EventBus.getDefault().register(this);

		mContext = getActivity();
		FrameLayout parentView = new FrameLayout(mContext);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		parentView.setLayoutParams(params);

		mViewHolder = new ViewHolder();
		mViewHolder.mParentView = parentView;
		mViewHolder.mView = LayoutInflater.from(mContext).inflate(
				R.layout.fragment_homepage_focus, parentView, true);
		mViewHolder.mPhotoListView = (PullToRefreshListView) mViewHolder.mView
				.findViewById(R.id.fragment_homepage_focus_lv);
		mEmptyView = mViewHolder.mView
				.findViewById(R.id.fragment_homepage_focus_emptyview);

		mFollowListFooter = LayoutInflater.from(mContext).inflate(
				R.layout.footer_load_more, null);
		mViewHolder.mPhotoListView.getRefreshableView().addFooterView(
				mFollowListFooter);
		mFollowListFooter.setVisibility(View.GONE);

		mHotPhotoItems = new ArrayList<PhotoItem>();
		mAdapter = new PhotoListAdapter(mContext,
				PhotoItemView.PhotoListType.HOT_FOCUS_ASK, mHotPhotoItems);
		mAdapter.setIsHomePageFouce(true);
		mViewHolder.mPhotoListView.getRefreshableView().setAdapter(mAdapter);

		mListener = new PhotoListListener(mContext);
		mViewHolder.mPhotoListView.setOnRefreshListener(mListener);
		mViewHolder.mPhotoListView.setOnLastItemVisibleListener(mListener);

		if (NetworkUtil.getNetworkType() != NetworkUtil.NetworkType.NONE) {
			mViewHolder.mPhotoListView.setRefreshing(true);
		}

		mViewHolder.mPhotoListView.setScrollingWhileRefreshingEnabled(true);

		// TODO 检测耗时
		try {
			mDatabaseHelper = OpenHelperManager.getHelper(mContext,
					DatabaseHelper.class);
			mPhotoItemDao = mDatabaseHelper.getDao(PhotoItem.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		loadDataAsync();
		return parentView;
	}

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

	/**
	 * TODO 没有被调用
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
				"onDestroy");
		EventBus.getDefault().unregister(this);
		// Step2: 释放内存
		if (mDatabaseHelper != null) {
			OpenHelperManager.releaseHelper();
			mDatabaseHelper = null;
		}

		mPhotoItemDao = null;
	}

	// 触发自动下拉刷新
	private void setRefreshing() {
		mViewHolder.mPhotoListView.setRefreshing(true);
	}

	public void onEventMainThread(RefreshEvent event) {
		if(event.className.equals(this.getClass().getName())){
			try {
				setRefreshing();
			} catch (NullPointerException nu) {
			} catch (Exception e) {
			}
		}
	}

	private void loadDataAsync() {
		ThreadManager.executeOnFileThread(new Runnable() {
			@Override
			public void run() {
				try {
					// mDbPhotoItems = mPhotoItemDao.queryForAll();
					List<PhotoItem> items = mPhotoItemDao.queryBuilder()
							.orderBy("update_time", false).where()
							.eq("from", PhotoItem.TYPE_HOME_FOCUS).query();
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
			mHotPhotoItems.clear();
			List<PhotoItem> items = (List<PhotoItem>) msg.obj;
			mHotPhotoItems.addAll(items);
			mAdapter.notifyDataSetChanged();
			if (NetworkUtil.getNetworkType() != NetworkUtil.NetworkType.NONE) {
				mViewHolder.mPhotoListView.setRefreshing(true);
			}
		}
		return true;
	}

	private class PhotoListListener implements OnLastItemVisibleListener,
			OnRefreshListener<ListView> {
		private Context mContext;
		private static final long DEFAULT_LAST_REFRESH_TIME = -1;

		public PhotoListListener(Context context) {
			mContext = context;
			SharedPreferences sp = mContext.getSharedPreferences(
					Constants.SharedPreferencesKey.NAME, Context.MODE_PRIVATE);
			mSpKey = Constants.SharedPreferencesKey.HOT_PHOTO_LIST_LAST_REFRESH_TIME;

			mLastUpdatedTime = sp.getLong(mSpKey, DEFAULT_LAST_REFRESH_TIME);
		}

		@Override
		public void onLastItemVisible() {
			if (canLoadMore) {
				mPage += 1;
				mFollowListFooter.setVisibility(View.VISIBLE);
				PhotoListRequest.Builder builder = new PhotoListRequest.Builder()
						.setPage(mPage).setLastUpdated(mLastUpdatedTime)
						.setType(PhotoItem.TYPE_HOME_FOCUS)
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
		public void onRefresh(PullToRefreshBase<ListView> refreshView) {
			mPage = 1;
			// 上次刷新时间
			if (mLastUpdatedTime == DEFAULT_LAST_REFRESH_TIME) {
				mLastUpdatedTime = System.currentTimeMillis();
			}

			PhotoListRequest.Builder builder = new PhotoListRequest.Builder()
					.setPage(mPage).setLastUpdated(mLastUpdatedTime)
					.setType(PhotoItem.TYPE_HOME_FOCUS)
					.setListener(refreshListener)
					.setErrorListener(errorListener);

			PhotoListRequest request = builder.build();
			request.setTag(TAG);
			RequestQueue requestQueue = PSGodRequestQueue.getInstance(mContext)
					.getRequestQueue();
			requestQueue.add(request);
		}
	}

	private ErrorListener errorListener = new PSGodErrorListener() {
		@Override
		public void handleError(VolleyError error) {
			// TODO Auto-generated method stub
			mViewHolder.mPhotoListView.onRefreshComplete();
		}
	};

	private Listener<List<PhotoItem>> loadMoreListener = new Listener<List<PhotoItem>>() {
		@Override
		public void onResponse(final List<PhotoItem> items) {
			mHotPhotoItems.addAll(items);
			mAdapter.notifyDataSetChanged();
			mFollowListFooter.setVisibility(View.INVISIBLE);

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
			mHotPhotoItems.clear();
			mHotPhotoItems.addAll(items);
			mAdapter.notifyDataSetChanged();

			mViewHolder.mPhotoListView.onRefreshComplete();

			PhotoItem.savePhotoList(getActivity(), mPhotoItemDao, items,
					PhotoItem.TYPE_HOME_FOCUS);

			mViewHolder.mPhotoListView.setEmptyView(mEmptyView);

			if (items.size() < 15) {
				canLoadMore = false;
			} else {
				canLoadMore = true;
			}
			// 保存本次刷新时间到sp
			mLastUpdatedTime = System.currentTimeMillis();
			if (android.os.Build.VERSION.SDK_INT >= 9) {
				mContext
						.getSharedPreferences(
								Constants.SharedPreferencesKey.NAME,
								Context.MODE_PRIVATE).edit()
						.putLong(mSpKey, mLastUpdatedTime).apply();
			} else {
				mContext
						.getSharedPreferences(
								Constants.SharedPreferencesKey.NAME,
								Context.MODE_PRIVATE).edit()
						.putLong(mSpKey, mLastUpdatedTime).commit();
			}
		}
	};

	/**
	 * 保存视图组件，避免视图的重复加载
	 * 
	 */
	private static class ViewHolder {
		ViewGroup mParentView;
		View mView;
		PullToRefreshListView mPhotoListView;
	}
}
