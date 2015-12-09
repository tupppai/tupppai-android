package com.psgod.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import com.psgod.network.request.UserPhotoRequest;
import com.psgod.ui.adapter.GridAdapter;
import com.psgod.ui.adapter.MyCollectionsGridAdapter;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 我的收藏界面
 * 
 * @author Rayal
 * 
 */
public class MyCollectionsActivity extends PSGodBaseActivity implements
		Handler.Callback {
	private static final String TAG = MyCollectionsActivity.class
			.getSimpleName();
	private static final int MY_COLLECTION = 3;

	private PullToRefreshListView mListView;
	private MyCollectionsListener mCollectionsListener;
	private MyCollectionsGridAdapter mAdapter;
	private View mEmptyView;
	private List<PhotoItem> mPhotoItems;

	private CustomProgressingDialog mProgressDialog;
	private String mSpKey;
	private static final long DEFAULT_LAST_REFRESH_TIME = -1;
	private long mLastUpdatedTime;
	private int mPage = 1;

	private boolean canLoadMore = true;
	private View mFooterView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_collections);

		mListView = (PullToRefreshListView) this
				.findViewById(R.id.activity_my_collection_listview);
		mListView.setMode(Mode.DISABLED);

		mFooterView = LayoutInflater.from(MyCollectionsActivity.this).inflate(
				R.layout.footer_load_more, null);
		mListView.getRefreshableView().addFooterView(mFooterView);
		mFooterView.setVisibility(View.GONE);

		mPhotoItems = new ArrayList<PhotoItem>();
		mAdapter = new MyCollectionsGridAdapter(this, mPhotoItems);
		// 将原始的adapter转化为每行三个
		GridAdapter<MyCollectionsGridAdapter> adapter = new GridAdapter<MyCollectionsGridAdapter>(
				MyCollectionsActivity.this, mAdapter);
		adapter.setNumColumns(2);
		mListView.getRefreshableView().setAdapter(adapter);

		// 初始化listener
		mCollectionsListener = new MyCollectionsListener(
				MyCollectionsActivity.this);
		mListView.setOnLastItemVisibleListener(mCollectionsListener);
		mListView.setScrollingWhileRefreshingEnabled(true);

		// 显示等待对话框
		if (mProgressDialog == null) {
			mProgressDialog = new CustomProgressingDialog(
					MyCollectionsActivity.this);
		}
		if (!mProgressDialog.isShowing()) {
			mProgressDialog.show();
		}

		refresh();
	}

	private class MyCollectionsListener implements OnLastItemVisibleListener {
		private Context mContext;

		public MyCollectionsListener(Context context) {
			mContext = context;

			SharedPreferences sp = mContext.getSharedPreferences(
					Constants.SharedPreferencesKey.NAME, Context.MODE_PRIVATE);
			mSpKey = Constants.SharedPreferencesKey.MY_COLLECTION_PHOTO_LIST_LAST_REFRESH_TIME;
			mLastUpdatedTime = sp.getLong(mSpKey, DEFAULT_LAST_REFRESH_TIME);
		}

		@Override
		public void onLastItemVisible() {
			if (canLoadMore) {
				mFooterView.setVisibility(View.VISIBLE);
				mPage = mPage + 1;
				// 上拉加载更多
				UserPhotoRequest.Builder builder = new UserPhotoRequest.Builder()
						.setType(MY_COLLECTION).setPage(mPage)
						.setListener(loadMoreListener)
						.setErrorListener(errorListener);
				UserPhotoRequest request = builder.build();
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
		UserPhotoRequest.Builder builder = new UserPhotoRequest.Builder()
				.setType(MY_COLLECTION).setPage(mPage)
				.setLastUpdated(mLastUpdatedTime).setListener(refreshListener)
				.setErrorListener(errorListener);

		UserPhotoRequest request = builder.build();
		request.setTag(TAG);
		RequestQueue requestQueue = PSGodRequestQueue.getInstance(
				MyCollectionsActivity.this).getRequestQueue();
		requestQueue.add(request);
	}

	private Listener<List<PhotoItem>> refreshListener = new Listener<List<PhotoItem>>() {
		@Override
		public void onResponse(List<PhotoItem> items) {
			mPhotoItems.clear();
			mPhotoItems.addAll(items);
			mAdapter.notifyDataSetChanged();

			mListView.onRefreshComplete();

			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}

			if (items.size() < 15) {
				canLoadMore = false;
			} else {
				canLoadMore = true;
			}

			mEmptyView = MyCollectionsActivity.this
					.findViewById(R.id.activity_my_collection_empty_view);
			mListView.setEmptyView(mEmptyView);

			// 保存本次刷新时间到sp
			mLastUpdatedTime = System.currentTimeMillis();
			if (android.os.Build.VERSION.SDK_INT >= 9) {
				getApplicationContext()
						.getSharedPreferences(
								Constants.SharedPreferencesKey.NAME,
								Context.MODE_PRIVATE).edit()
						.putLong(mSpKey, mLastUpdatedTime).apply();
			} else {
				getApplicationContext()
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
