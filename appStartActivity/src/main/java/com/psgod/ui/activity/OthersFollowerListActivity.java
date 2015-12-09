package com.psgod.ui.activity;

/**
 * 其用户的粉丝、关注列表界面
 * @author brandwang
 */

import android.content.Context;
import android.content.Intent;
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
import com.psgod.Logger;
import com.psgod.R;
import com.psgod.model.User;
import com.psgod.network.request.OthersFollowListRequest;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.ui.adapter.FollowerListAdapter;
import com.psgod.ui.widget.ActionBar;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import java.util.ArrayList;
import java.util.List;

public class OthersFollowerListActivity extends PSGodBaseActivity {
	private static final String TAG = OthersFollowerListActivity.class
			.getSimpleName();
	private static final int TYPE_FOLLOWER = 0;
	private static final int TYPE_FOLLOWING = 1;

	private ActionBar mActionBar;
	private PullToRefreshListView mListView;
	private View mEmptyView;
	private FollowerListener mFollowerListener;
	private FollowerListAdapter mFollowerListAdapter;
	private List<User> mFollowers;

	private CustomProgressingDialog mProgressDialog;
	private long mLastUpdateTimeFollowing;
	private long mLastUpdateTimeFollower;
	private int mPage = 1;
	private TextView mEmptyTextView;

	// 用户id
	private long mUid;
	// 查看类型 0 粉丝 1 关注
	private int mType;
	// 用户姓名
	private String mNick;

	private String mSpKeyFollowing;
	private String mSpKeyFollower;
	private static final long DEFAULT_LAST_REFRESH_TIME = -1;
	private View mFooterLoadMoreView;
	// 控制是否可以加载下一页
	private boolean canLoadMore = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_follower_list);

		Intent intent = getIntent();
		mUid = intent.getLongExtra(Constants.IntentKey.USER_ID, -1);
		mType = intent.getIntExtra("list_type", -1);
		mNick = intent.getStringExtra(Constants.IntentKey.USER_NICKNAME);

		if (mUid == -1 || mType == -1) {
			finish();
		}

		mActionBar = (ActionBar) findViewById(R.id.actionbar);
		mActionBar.setTitle(mNick + "的" + ((mType == 0) ? "粉丝" : "关注"));

		mEmptyTextView = (TextView) findViewById(R.id.empty_text);
		mEmptyTextView.setText((mType == 0) ? "ta还没有粉丝，可以做ta的第一个粉丝哦"
				: "他还没有关注的人");

		mFollowers = new ArrayList<User>();
		mListView = (PullToRefreshListView) this
				.findViewById(R.id.activity_follower_list_listview);
		mListView.setMode(Mode.PULL_FROM_START);

		mFollowerListAdapter = new FollowerListAdapter(this, mFollowers);
		mListView.getRefreshableView().setAdapter(mFollowerListAdapter);

		mFooterLoadMoreView = LayoutInflater.from(
				OthersFollowerListActivity.this).inflate(
				R.layout.footer_load_more, null);
		mListView.getRefreshableView().addFooterView(mFooterLoadMoreView);
		mFooterLoadMoreView.setVisibility(View.GONE);

		mFollowerListener = new FollowerListener(this);
		mListView.setOnRefreshListener(mFollowerListener);
		mListView.setOnLastItemVisibleListener(mFollowerListener);
		mListView.setScrollingWhileRefreshingEnabled(true);

		// 显示等待对话框
		if (mProgressDialog == null) {
			mProgressDialog = new CustomProgressingDialog(
					OthersFollowerListActivity.this);
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
			if (mType == 0) {
				mSpKeyFollowing = Constants.SharedPreferencesKey.OTHERS_FOLLOWING_LIST_LAST_REFRESH_TIME;
				mLastUpdateTimeFollowing = sp.getLong(mSpKeyFollowing,
						DEFAULT_LAST_REFRESH_TIME);
			} else {
				mSpKeyFollower = Constants.SharedPreferencesKey.OTHERS_FOLLOWER_LIST_LAST_REFRESH_TIME;
				mLastUpdateTimeFollower = sp.getLong(mSpKeyFollower,
						DEFAULT_LAST_REFRESH_TIME);
			}

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
				mPage++;
				mFooterLoadMoreView.setVisibility(View.VISIBLE);

				OthersFollowListRequest.Builder builder = new OthersFollowListRequest.Builder()
						.setPage(mPage).setUid(mUid).setListType(mType)
						.setListener(loadMoreListener)
						.setErrorListener(errorListener);
				OthersFollowListRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						mContext).getRequestQueue();
				requestQueue.add(request);
			}
		}

	}

	// 刷新操作
	private void refresh() {
		mPage = 1;
		canLoadMore = false;
		if (mType == 0) {
			if (mLastUpdateTimeFollowing == DEFAULT_LAST_REFRESH_TIME) {
				mLastUpdateTimeFollowing = System.currentTimeMillis();
			}
		} else {
			if (mLastUpdateTimeFollower == DEFAULT_LAST_REFRESH_TIME) {
				mLastUpdateTimeFollower = System.currentTimeMillis();
			}
		}
		OthersFollowListRequest.Builder builder = new OthersFollowListRequest.Builder()
				.setPage(mPage).setUid(mUid).setListType(mType)
				.setListener(refreshListener).setErrorListener(errorListener);
		if (mType == 0) {
			builder.setLastUpdated(mLastUpdateTimeFollowing);
		} else {
			builder.setLastUpdated(mLastUpdateTimeFollower);
		}

		OthersFollowListRequest request = builder.build();
		request.setTag(TAG);
		RequestQueue requestQueue = PSGodRequestQueue.getInstance(
				OthersFollowerListActivity.this).getRequestQueue();
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

			mEmptyView = OthersFollowerListActivity.this
					.findViewById(R.id.activity_follower_list_empty_view);
			mListView.setEmptyView(mEmptyView);

			if (items.size() < 15) {
				canLoadMore = false;
			} else {
				canLoadMore = true;
			}

			// 保存本次刷新时间到sp
			if (mType == 0) {
				mLastUpdateTimeFollowing = System.currentTimeMillis();
				if (android.os.Build.VERSION.SDK_INT >= 9) {
					getApplicationContext()
							.getSharedPreferences(
									Constants.SharedPreferencesKey.NAME,
									Context.MODE_PRIVATE).edit()
							.putLong(mSpKeyFollowing, mLastUpdateTimeFollowing)
							.apply();
				} else {
					getApplicationContext()
							.getSharedPreferences(
									Constants.SharedPreferencesKey.NAME,
									Context.MODE_PRIVATE).edit()
							.putLong(mSpKeyFollowing, mLastUpdateTimeFollowing)
							.commit();
				}
			} else {
				mLastUpdateTimeFollower = System.currentTimeMillis();
				if (android.os.Build.VERSION.SDK_INT >= 9) {
					getApplicationContext()
							.getSharedPreferences(
									Constants.SharedPreferencesKey.NAME,
									Context.MODE_PRIVATE).edit()
							.putLong(mSpKeyFollower, mLastUpdateTimeFollower)
							.apply();
				} else {
					getApplicationContext()
							.getSharedPreferences(
									Constants.SharedPreferencesKey.NAME,
									Context.MODE_PRIVATE).edit()
							.putLong(mSpKeyFollower, mLastUpdateTimeFollower)
							.commit();
				}
			}
		}
	};

	private Listener<List<User>> loadMoreListener = new Listener<List<User>>() {
		@Override
		public void onResponse(List<User> items) {
			mFollowers.addAll(items);
			mFollowerListAdapter.notifyDataSetChanged();
			mListView.onRefreshComplete();

			mFooterLoadMoreView.setVisibility(View.INVISIBLE);

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
			mListView.onRefreshComplete();
			Toast.makeText(OthersFollowerListActivity.this, error.getMessage(),
					Toast.LENGTH_SHORT).show();
			Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_COLOR, TAG,
					error.getMessage());
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
