package com.psgod.ui.fragment;

/**
 * 关注Tab
 */
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.psgod.Constants;
import com.psgod.Logger;
import com.psgod.R;
import com.psgod.ThreadManager;
import com.psgod.WeakReferenceHandler;
import com.psgod.db.DatabaseHelper;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.FollowDynamicListRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.ui.activity.MainActivity;
import com.psgod.ui.activity.RecommendFocusActivity;
import com.psgod.ui.adapter.PhotoExpandableListAdapter;
import com.psgod.ui.view.PhotoItemView;

public class FocusFragment extends BaseFragment implements Callback {
	private static final String TAG = FocusFragment.class.getSimpleName();

	private ViewHolder mViewHolder;
	private List<PhotoItem> mFocusPhotoItems;
	private DatabaseHelper mDatabaseHelper = null;
	private Dao<PhotoItem, Long> mPhotoItemDao;
	private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);

	// 带评论
	private PhotoExpandableListAdapter mAdapter;
	private PhotoListListener mListener;
	private View mFollowListFooter;
	private int mPage;

	// 上次刷新时间
	private long mLastUpdatedTime;
	// 列表的类型
	private String mSpKey;

	private View mEmptyView;
	private Button mRecommendBtn;

	// 控制是否可以加载下一项
	private boolean canLoadMore = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logger.logMethod(TAG, "onCreate");

		// 我的关注页面创建标示
		Constants.IS_FOCUS_FRAGMENT_CREATED = true;

		Context context = getActivity();
		FrameLayout parentView = new FrameLayout(context);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		parentView.setLayoutParams(params);

		mViewHolder = new ViewHolder();
		mViewHolder.mParentView = parentView;
		mViewHolder.mView = LayoutInflater.from(context).inflate(
				R.layout.fragment_focus, parentView, true);
		mViewHolder.mPhotoListView = (PullToRefreshExpandableListView) mViewHolder.mView
				.findViewById(R.id.fragment_focus_photo_lv);
		mEmptyView = mViewHolder.mView
				.findViewById(R.id.fragment_focus_list_empty_view);
		mRecommendBtn = (Button) mViewHolder.mView
				.findViewById(R.id.recommend_focus_btn);

		mFollowListFooter = LayoutInflater.from(getActivity()).inflate(
				R.layout.footer_load_more, null);
		mViewHolder.mPhotoListView.getRefreshableView().addFooterView(
				mFollowListFooter);
		mFollowListFooter.setVisibility(View.GONE);

		mFocusPhotoItems = new ArrayList<PhotoItem>();
		mAdapter = new PhotoExpandableListAdapter(context,
				PhotoItemView.PhotoListType.HOT_FOCUS_REPLY, mFocusPhotoItems);
		mViewHolder.mPhotoListView.getRefreshableView().setAdapter(mAdapter);

		mListener = new PhotoListListener(context);
		mViewHolder.mPhotoListView.setOnRefreshListener(mListener);
		mViewHolder.mPhotoListView.setOnLastItemVisibleListener(mListener);

		mViewHolder.mPhotoListView.setRefreshing(true);
		mViewHolder.mPhotoListView.setScrollingWhileRefreshingEnabled(true);

		initListeners();

		// TODO 检测耗时
		try {
			mDatabaseHelper = OpenHelperManager.getHelper(context,
					DatabaseHelper.class);
			mPhotoItemDao = mDatabaseHelper.getDao(PhotoItem.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		loadDataAsync();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Logger.logMethod(TAG, "onCreateView");
		FrameLayout parentView = new FrameLayout(getActivity());
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		parentView.setLayoutParams(params);
		mViewHolder.mParentView.removeView(mViewHolder.mView);
		parentView.addView(mViewHolder.mView);
		mViewHolder.mParentView = parentView;
		return parentView;
	}

	public void onNewIntent(Intent intent) {
		if (intent == null) {
			return;
		}

		int id = intent.getIntExtra(MainActivity.IntentParams.KEY_FRAGMENT_ID,
				-1);
		if (id == MainActivity.IntentParams.VALUE_FRAGMENT_ID_RECENT) {
			// 触发下拉刷新
			setRefreshing();
		}
	}

	// 触发下拉自动刷新
	public void setRefreshing() {
		mViewHolder.mPhotoListView.setRefreshing(true);

		// 还原标志位
		Constants.IS_FOLLOW_NEW_USER = false;
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
			mFocusPhotoItems.clear();
			List<PhotoItem> items = (List<PhotoItem>) msg.obj;
			mFocusPhotoItems.addAll(items);
			mAdapter.notifyDataSetChanged();
			mViewHolder.mPhotoListView.setRefreshing(true);

		}
		return true;
	}

	/**
	 * TODO 设置各个按钮的动作监听器
	 */
	private void initListeners() {
		mRecommendBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getActivity(),
						RecommendFocusActivity.class);
				startActivity(intent);
			}
		});
	}

	private class PhotoListListener implements
			OnRefreshListener<ExpandableListView>, OnLastItemVisibleListener {
		private Context mContext;
		private static final long DEFAULT_LAST_REFRESH_TIME = -1;

		public PhotoListListener(Context context) {
			mContext = context;
			SharedPreferences sp = mContext.getSharedPreferences(
					Constants.SharedPreferencesKey.NAME, Context.MODE_PRIVATE);
			mSpKey = Constants.SharedPreferencesKey.FOCUS_PHOTO_LIST_LAST_REFRESH_TIME;

			mLastUpdatedTime = sp.getLong(mSpKey, DEFAULT_LAST_REFRESH_TIME);

		}

		@Override
		public void onLastItemVisible() {
			if (canLoadMore) {
				mPage += 1;
				mFollowListFooter.setVisibility(View.VISIBLE);
				FollowDynamicListRequest.Builder builder = new FollowDynamicListRequest.Builder()
						.setPage(mPage).setListener(loadMoreListener)
						.setErrorListener(errorListener);

				FollowDynamicListRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						mContext).getRequestQueue();
				requestQueue.add(request);
			}
		}

		@Override
		public void onRefresh(PullToRefreshBase<ExpandableListView> refreshView) {
			canLoadMore = false;
			mPage = 1;
			// 上次刷新时间
			if (mLastUpdatedTime == DEFAULT_LAST_REFRESH_TIME) {
				mLastUpdatedTime = System.currentTimeMillis();
			}

			FollowDynamicListRequest.Builder builder = new FollowDynamicListRequest.Builder()
					.setPage(mPage).setLastUpdated(mLastUpdatedTime)
					.setListener(refreshListener)
					.setErrorListener(errorListener);

			FollowDynamicListRequest request = builder.build();
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
			mFocusPhotoItems.addAll(items);
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

			mFocusPhotoItems.clear();
			mFocusPhotoItems.addAll(items);
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
				getActivity()
						.getSharedPreferences(
								Constants.SharedPreferencesKey.NAME,
								Context.MODE_PRIVATE).edit()
						.putLong(mSpKey, mLastUpdatedTime).apply();
			} else {
				getActivity()
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
	 * @author Rayal
	 * 
	 */
	private static class ViewHolder {
		ViewGroup mParentView;
		View mView;
		PullToRefreshExpandableListView mPhotoListView;
	}
}
