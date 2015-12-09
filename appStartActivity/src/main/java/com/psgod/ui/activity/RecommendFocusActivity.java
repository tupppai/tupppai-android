package com.psgod.ui.activity;

/**
 * 推荐关注大神
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.model.User;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.RecommendFollowRequest;
import com.psgod.ui.adapter.FollowerListAdapter;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import java.util.ArrayList;
import java.util.List;

public class RecommendFocusActivity extends PSGodBaseActivity {
	private static final String TAG = RecommendFocusActivity.class
			.getSimpleName();

	private PullToRefreshListView mRecommendListView;
	private View mEmptyView;
	private List<User> mRecommendList;
	private FollowerListAdapter mRecommendListAdapter;

	private long mLastUpdateTime;
	private String mSpKey;
	private static final long DEFAULT_LAST_REFRESH_TIME = -1;
	private int mPage;

	private CustomProgressingDialog mProgressDialog;
	private RecommendListener mRecommendListener;

	// 控制是否可以加载下一页
	private boolean canLoadMore = true;
	private View mFollowingListFooter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recommend_focus);

		mRecommendList = new ArrayList<User>();
		mRecommendListView = (PullToRefreshListView) this
				.findViewById(R.id.activity_recommend_focus);
		mRecommendListView.setMode(Mode.PULL_FROM_START);
		mRecommendListView.getRefreshableView().setDivider(null);

		mFollowingListFooter = LayoutInflater.from(RecommendFocusActivity.this)
				.inflate(R.layout.footer_load_more, null);
		mRecommendListView.getRefreshableView().addFooterView(
				mFollowingListFooter);
		mFollowingListFooter.setVisibility(View.GONE);

		mRecommendListAdapter = new FollowerListAdapter(this, mRecommendList);
		mRecommendListView.getRefreshableView().setAdapter(
				mRecommendListAdapter);

		mRecommendListener = new RecommendListener(this);
		mRecommendListView.setOnRefreshListener(mRecommendListener);
		mRecommendListView.setOnLastItemVisibleListener(mRecommendListener);
		mRecommendListView.setScrollingWhileRefreshingEnabled(true);

		// 显示等待对话框
		if (mProgressDialog == null) {
			mProgressDialog = new CustomProgressingDialog(
					RecommendFocusActivity.this);
		}
		if (!mProgressDialog.isShowing()) {
			mProgressDialog.show();
		}

		refresh();
	}

	private class RecommendListener implements OnRefreshListener,
			OnLastItemVisibleListener {
		private Context mContext;

		public RecommendListener(Context context) {
			mContext = context;

			SharedPreferences sp = mContext.getSharedPreferences(
					Constants.SharedPreferencesKey.NAME, Context.MODE_PRIVATE);
			mSpKey = Constants.SharedPreferencesKey.RECOMMEND_FOCUS_LIST_LAST_REFRESH_TIME;
			mLastUpdateTime = sp.getLong(mSpKey, DEFAULT_LAST_REFRESH_TIME);
		}

		@Override
		public void onRefresh(PullToRefreshBase refreshView) {
			// TODO Auto-generated method stub
			refresh();
		}

		@Override
		public void onLastItemVisible() {
			// TODO Auto-generated method stub
			if (canLoadMore) {
				mPage += 1;
				mFollowingListFooter.setVisibility(View.VISIBLE);
				RecommendFollowRequest.Builder builder = new RecommendFollowRequest.Builder()
						.setPage(mPage).setListener(loadMoreListener)
						.setErrorListener(errorListener);
				RecommendFollowRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						mContext).getRequestQueue();
				requestQueue.add(request);
			}
		}

	}

	// 刷新操作
	private void refresh() {
		canLoadMore = false;

		if (mLastUpdateTime == DEFAULT_LAST_REFRESH_TIME) {
			mLastUpdateTime = System.currentTimeMillis();
		}

		mPage = 1;
		RecommendFollowRequest.Builder builder = new RecommendFollowRequest.Builder()
				.setPage(mPage).setLastUpdated(mLastUpdateTime)
				.setListener(refreshListener).setErrorListener(errorListener);

		RecommendFollowRequest request = builder.build();
		request.setTag(TAG);
		RequestQueue requestQueue = PSGodRequestQueue.getInstance(
				RecommendFocusActivity.this).getRequestQueue();
		requestQueue.add(request);
	}

	private Listener<List<User>> refreshListener = new Listener<List<User>>() {
		@Override
		public void onResponse(List<User> items) {
			mRecommendList.clear();
			mRecommendList.addAll(items);
			mRecommendListAdapter.notifyDataSetChanged();

			mRecommendListView.onRefreshComplete();

			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}

			if (items.size() < 15) {
				canLoadMore = false;
			} else {
				canLoadMore = true;
			}

			mEmptyView = RecommendFocusActivity.this
					.findViewById(R.id.activity_recommend_list_empty_view);
			mRecommendListView.setEmptyView(mEmptyView);

			// 保存本次刷新时间到sp
			mLastUpdateTime = System.currentTimeMillis();
			if (android.os.Build.VERSION.SDK_INT >= 9) {
				getApplicationContext()
						.getSharedPreferences(
								Constants.SharedPreferencesKey.NAME,
								Context.MODE_PRIVATE).edit()
						.putLong(mSpKey, mLastUpdateTime).apply();
			} else {
				getApplicationContext()
						.getSharedPreferences(
								Constants.SharedPreferencesKey.NAME,
								Context.MODE_PRIVATE).edit()
						.putLong(mSpKey, mLastUpdateTime).commit();
			}
		}
	};

	private Listener<List<User>> loadMoreListener = new Listener<List<User>>() {
		@Override
		public void onResponse(List<User> items) {
			mRecommendList.addAll(items);
			mRecommendListAdapter.notifyDataSetChanged();
			mRecommendListView.onRefreshComplete();

			mFollowingListFooter.setVisibility(View.INVISIBLE);

			if (items.size() < 15) {
				canLoadMore = false;
			} else {
				canLoadMore = true;
			}
		}
	};

	private ErrorListener errorListener = new ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {
			Toast.makeText(RecommendFocusActivity.this, error.getMessage(),
					Toast.LENGTH_SHORT).show();
			mRecommendListView.onRefreshComplete();
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
