package com.psgod.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.SettingLikedRequest;
import com.psgod.network.request.UserPhotoRequest;
import com.psgod.ui.adapter.AskGridAdapter;
import com.psgod.ui.adapter.GridAdapter;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import java.util.ArrayList;
import java.util.List;

public class SettingLikedActivity extends PSGodBaseActivity {

	private static final String TAG = SettingLikedActivity.class
			.getSimpleName();
	private Context mContext;
	private CustomProgressingDialog mProgressDialog;

	private PullToRefreshListView mListView;
	private List<PhotoItem> mPhotoItems;
	private AskGridAdapter mAdapter;
	private View mFooterView;
	private SettingLikedGridListener mLikedListener;

	private int mPage = 1;
	private boolean canLoadMore = true;

	private String mSpKey;
	private static final long DEFAULT_LAST_REFRESH_TIME = -1;
	private long mLastUpdatedTime;

	private View mEmptyView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;

		setContentView(R.layout.activity_setting_liked);
		mPhotoItems = new ArrayList<PhotoItem>();
		mListView = (PullToRefreshListView) findViewById(R.id.activity_setting_liked_grid_listview);
		mListView.setMode(Mode.DISABLED);

		mFooterView = LayoutInflater.from(this).inflate(
				R.layout.footer_load_more, null);
		mListView.getRefreshableView().addFooterView(mFooterView);
		mFooterView.setVisibility(View.GONE);

		mAdapter = new AskGridAdapter(this, mPhotoItems);
		// 将原始的adapter转化为每行三个
		GridAdapter<AskGridAdapter> adapter = new GridAdapter<AskGridAdapter>(
				this, mAdapter);
		adapter.setNumColumns(3);
		mListView.getRefreshableView().setAdapter(adapter);

		mLikedListener = new SettingLikedGridListener(mContext);
		mListView.setOnLastItemVisibleListener(mLikedListener);
		mListView.setScrollingWhileRefreshingEnabled(true);
		// 显示等待对话框
		if (mProgressDialog == null) {
			mProgressDialog = new CustomProgressingDialog(this);
		}
		if (!mProgressDialog.isShowing()) {
			mProgressDialog.show();
		}

		refresh();
	}

	private class SettingLikedGridListener implements OnLastItemVisibleListener {
		private Context mContext;

		public SettingLikedGridListener(Context context) {
			mContext = context;

			SharedPreferences sp = mContext.getSharedPreferences(
					Constants.SharedPreferencesKey.NAME, Context.MODE_PRIVATE);
			mSpKey = Constants.SharedPreferencesKey.SETTING_LIKED_LIST_LAST_REFRESH_TIME;
			mLastUpdatedTime = sp.getLong(mSpKey, DEFAULT_LAST_REFRESH_TIME);
		}

		@Override
		public void onLastItemVisible() {
			if (canLoadMore) {
				mFooterView.setVisibility(View.VISIBLE);
				mPage = mPage + 1;
				// 上拉加载更多
				SettingLikedRequest.Builder builder = new SettingLikedRequest.Builder()
						.setPage(mPage).setListener(loadMoreListener)
						.setErrorListener(errorListener);
				SettingLikedRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						mContext).getRequestQueue();
				requestQueue.add(request);
			}
		}
	}

	// 刷新操作
	private void refresh() {
		if (mLastUpdatedTime == DEFAULT_LAST_REFRESH_TIME) {
			mLastUpdatedTime = System.currentTimeMillis();
		}
		mPage = 1;
		SettingLikedRequest.Builder builder = new SettingLikedRequest.Builder()
				.setPage(mPage).setListener(refreshListener)
				.setErrorListener(errorListener);

		SettingLikedRequest request = builder.build();
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
			mAdapter.notifyDataSetChanged();
			mListView.onRefreshComplete();

			mEmptyView = findViewById(R.id.activity_liked_emptyview);
			mListView.setEmptyView(mEmptyView);

			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}

			if (items.size() < 15) {
				canLoadMore = false;
			} else {
				canLoadMore = true;
			}

			// 保存本次刷新时间到sp
			mLastUpdatedTime = System.currentTimeMillis();
			if (android.os.Build.VERSION.SDK_INT >= 9) {
				mContext.getSharedPreferences(
						Constants.SharedPreferencesKey.NAME,
						Context.MODE_PRIVATE).edit()
						.putLong(mSpKey, mLastUpdatedTime).apply();
			} else {
				mContext.getSharedPreferences(
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
			mAdapter.notifyDataSetChanged();
			mListView.onRefreshComplete();

			mFooterView.setVisibility(View.INVISIBLE);
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
		RequestQueue requestQueue = PSGodRequestQueue.getInstance(this)
				.getRequestQueue();
		requestQueue.cancelAll(TAG);
	}
}
