package com.psgod.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
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
import com.psgod.network.request.MyFollowersListRequest;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.ui.adapter.FollowerListAdapter;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 粉丝列表界面
 * 
 * @author Rayal
 * 
 */
public class FollowerListActivity extends PSGodBaseActivity {
	private static final String TAG = FollowerListActivity.class
			.getSimpleName();

	private PullToRefreshListView mListView;
	private View mEmptyView;
	private TextView mEmptyViewText;
	private FollowerListener mFollowerListener;
	private FollowerListAdapter mFollowerListAdapter;
	private List<User> mFollowers;

	private CustomProgressingDialog mProgressDialog;
	private long mLastUpdateTime;
	private String mSpKey;
	private static final long DEFAULT_LAST_REFRESH_TIME = -1;
	private int mPage;

	// 控制是否可以加载下一页
	private boolean canLoadMore = true;
	private View mFollowerListFooter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_follower_list);

		mFollowers = new ArrayList<User>();
		mListView = (PullToRefreshListView) this
				.findViewById(R.id.activity_follower_list_listview);
		mListView.setMode(Mode.PULL_FROM_START);

		mEmptyViewText = (TextView) this.findViewById(R.id.empty_text);
		mEmptyViewText.setText("快去活跃一下就有粉丝咯～");

		mFollowerListFooter = LayoutInflater.from(FollowerListActivity.this)
				.inflate(R.layout.footer_load_more, null);
		mListView.getRefreshableView().addFooterView(mFollowerListFooter);
		mFollowerListFooter.setVisibility(View.GONE);

		mFollowerListAdapter = new FollowerListAdapter(this, mFollowers);
		mListView.getRefreshableView().setAdapter(mFollowerListAdapter);

		mFollowerListener = new FollowerListener(this);
		mListView.setOnRefreshListener(mFollowerListener);
		mListView.setOnLastItemVisibleListener(mFollowerListener);
		mListView.setScrollingWhileRefreshingEnabled(true);

		// 显示等待对话框
		if (mProgressDialog == null) {
			mProgressDialog = new CustomProgressingDialog(
					FollowerListActivity.this);
		}
		if (!mProgressDialog.isShowing()) {
			mProgressDialog.show();
		}

		refresh();
	}

	private class FollowerListener implements OnRefreshListener,
			OnLastItemVisibleListener {
		private Context mContext;

		public FollowerListener(Context context) {
			mContext = context;

			SharedPreferences sp = mContext.getSharedPreferences(
					Constants.SharedPreferencesKey.NAME, Context.MODE_PRIVATE);
			mSpKey = Constants.SharedPreferencesKey.FOLLOWER_LIST_LAST_REFRESH_TIME;
			mLastUpdateTime = sp.getLong(mSpKey, DEFAULT_LAST_REFRESH_TIME);
		}

		@Override
		public void onLastItemVisible() {
			// TODO Auto-generated method stub
			if (canLoadMore) {
				mFollowerListFooter.setVisibility(View.VISIBLE);
				mPage += 1;
				MyFollowersListRequest.Builder builder = new MyFollowersListRequest.Builder()
						.setPage(mPage).setListener(loadMoreListener)
						.setErrorListener(errorListener);
				MyFollowersListRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						mContext).getRequestQueue();
				requestQueue.add(request);
			}
		}

		@Override
		public void onRefresh(PullToRefreshBase refreshView) {
			// TODO Auto-generated method stub
			refresh();
		}
	}

	// 刷新操作
	private void refresh() {
		canLoadMore = false;

		if (mLastUpdateTime == DEFAULT_LAST_REFRESH_TIME) {
			mLastUpdateTime = System.currentTimeMillis();
		}

		mPage = 1;
		MyFollowersListRequest.Builder builder = new MyFollowersListRequest.Builder()
				.setPage(mPage).setLastUpdated(mLastUpdateTime)
				.setListener(refreshListener).setErrorListener(errorListener);

		MyFollowersListRequest request = builder.build();
		request.setTag(TAG);
		RequestQueue requestQueue = PSGodRequestQueue.getInstance(
				FollowerListActivity.this).getRequestQueue();
		requestQueue.add(request);
	}

	private Listener<List<User>> refreshListener = new Listener<List<User>>() {
		@Override
		public void onResponse(List<User> items) {
			mFollowers.clear();
			mFollowers.addAll(items);
			mFollowerListAdapter.notifyDataSetChanged();

			mListView.onRefreshComplete();

			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}

			if (items.size() < 15) {
				canLoadMore = false;
			} else {
				canLoadMore = true;
			}

			mEmptyView = FollowerListActivity.this
					.findViewById(R.id.activity_follower_list_empty_view);
			mListView.setEmptyView(mEmptyView);

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
			if (items.size() > 0) {
				mFollowers.addAll(items);
				mFollowerListAdapter.notifyDataSetChanged();
				mListView.onRefreshComplete();
			}

			mFollowerListFooter.setVisibility(View.INVISIBLE);

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
			Toast.makeText(FollowerListActivity.this, error.getMessage(),
					Toast.LENGTH_SHORT).show();
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
