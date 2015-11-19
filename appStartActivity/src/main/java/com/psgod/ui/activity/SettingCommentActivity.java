package com.psgod.ui.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

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
import com.psgod.model.PhotoItem;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.SettingCommentRequest;
import com.psgod.network.request.UserPhotoRequest;
import com.psgod.ui.adapter.SettingCommentAdapter;

public class SettingCommentActivity extends PSGodBaseActivity {
	private static final String TAG = SettingCommentActivity.class
			.getSimpleName();
	private Context mContext;
	public static final int MY_INPROGRESS = 2;

	private List<PhotoItem> mPhotoItems = new ArrayList<PhotoItem>();
	private SettingCommentAdapter mCommendAdapter;
	private SettingCommendListener mCommendListener;

	private int mPage = 1;

	private String mSpKey;
	private static final long DEFAULT_LAST_REFRESH_TIME = -1;
	private long mLastUpdatedTime;
	// 控制是否可以加载下一页
	private boolean canLoadMore = false;

	private PullToRefreshListView mListView;
	private View mFootView;

	private View mEmptyView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_commend);

		mContext = this;
		mListView = (PullToRefreshListView) findViewById(R.id.setting_commend_listview);
		mListView.setMode(Mode.PULL_FROM_START);

		mCommendAdapter = new SettingCommentAdapter(mContext, mPhotoItems);
		mListView.getRefreshableView().setAdapter(mCommendAdapter);

		mCommendListener = new SettingCommendListener(mContext);
		mListView.setOnRefreshListener(mCommendListener);
		mListView.setOnLastItemVisibleListener(mCommendListener);
		mListView.setScrollingWhileRefreshingEnabled(true);

		mEmptyView = findViewById(R.id.activity_commend_emptyview);
		mListView.setEmptyView(mEmptyView);

		mFootView = LayoutInflater.from(mContext).inflate(
				R.layout.footer_load_more, null);
		mFootView.setVisibility(View.GONE);
		mListView.getRefreshableView().addFooterView(mFootView);

		refresh();

	}

	private class SettingCommendListener implements OnLastItemVisibleListener,
			OnRefreshListener {
		private Context mContext;

		public SettingCommendListener(Context context) {
			mContext = context;

			SharedPreferences sp = mContext.getSharedPreferences(
					Constants.SharedPreferencesKey.NAME, Context.MODE_PRIVATE);
			mSpKey = Constants.SharedPreferencesKey.SETTING_COMMEND_LIST_LAST_REFRESH_TIME;
			mLastUpdatedTime = sp.getLong(mSpKey, DEFAULT_LAST_REFRESH_TIME);
		}

		@Override
		public void onLastItemVisible() {
			if (canLoadMore) {
				mPage = mPage + 1;
				mFootView.setVisibility(View.VISIBLE);
				// 上拉加载更多
				SettingCommentRequest.Builder builder = new SettingCommentRequest.Builder()
						.setPage(mPage).setListener(loadMoreListener)
						.setErrorListener(errorListener);
				SettingCommentRequest request = builder.build();
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

		SettingCommentRequest.Builder builder = new SettingCommentRequest.Builder()
				.setPage(mPage).setListener(refreshListener)
				.setErrorListener(errorListener);

		SettingCommentRequest request = builder.build();
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
			mCommendAdapter.notifyDataSetChanged();
			mListView.onRefreshComplete();

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
			mCommendAdapter.notifyDataSetChanged();
			mListView.onRefreshComplete();

			mFootView.setVisibility(View.INVISIBLE);

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
			mListView.onRefreshComplete();
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

}
