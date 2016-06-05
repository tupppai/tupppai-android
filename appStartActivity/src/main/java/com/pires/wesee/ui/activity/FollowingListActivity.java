package com.pires.wesee.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;
import com.pires.wesee.network.request.MyFollowingListRequest;
import com.pires.wesee.network.request.PSGodRequestQueue;
import com.pires.wesee.ui.adapter.FollowingExpandableListAdapter;
import com.pires.wesee.Constants;
import com.pires.wesee.R;
import com.pires.wesee.model.User;
import com.pires.wesee.network.request.PSGodErrorListener;
import com.pires.wesee.ui.widget.dialog.CustomProgressingDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 关注列表界面
 * 
 * @author Rayal
 * 
 */
public class FollowingListActivity extends PSGodBaseActivity {
	private final static String TAG = FollowingListActivity.class
			.getSimpleName();

	private PullToRefreshExpandableListView mFollowingLv;
	private FollowingExpandableListAdapter mAdapter;
	// 推荐关注
	private List<User> mRecommendUsers;
	// 我的关注
	private List<User> mMyUsers;
	private int mTotalMasters;
	private FollowingListListener mFollowingListListener;
	private View mEmptyView;
	private TextView mEmptyViewText;

	private String mSpKey;
	private static final long DEFAULT_LAST_REFRESH_TIME = -1;
	private long mLastUpdatedTime;
	private int mPage = 1;

	private CustomProgressingDialog mProgressDialog;
	// 控制是否可以加载下一页
	private boolean canLoadMore = true;
	private View mFollowingListFooter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_following_list);

		mFollowingLv = (PullToRefreshExpandableListView) this
				.findViewById(R.id.activity_following_list_elv);
		mFollowingLv.setMode(Mode.PULL_FROM_START);
		mFollowingLv.getRefreshableView().setDivider(null);

		mEmptyViewText = (TextView) this.findViewById(R.id.empty_text);
		mEmptyViewText.setText("还没有关注的人，快去关注些大神吧");

		mFollowingListFooter = LayoutInflater.from(FollowingListActivity.this)
				.inflate(R.layout.footer_load_more, null);
		mFollowingLv.getRefreshableView().addFooterView(mFollowingListFooter);
		mFollowingListFooter.setVisibility(View.GONE);

		mRecommendUsers = new ArrayList<User>();
		mMyUsers = new ArrayList<User>();
		mAdapter = new FollowingExpandableListAdapter(this, mRecommendUsers,
				mMyUsers, mTotalMasters);
		mFollowingLv.getRefreshableView().setAdapter(mAdapter);

		mFollowingListListener = new FollowingListListener(this);
		mFollowingLv.setOnRefreshListener(mFollowingListListener);
		mFollowingLv.setOnLastItemVisibleListener(mFollowingListListener);
		mFollowingLv.setScrollingWhileRefreshingEnabled(true);

		// 设置分组不可点击
		mFollowingLv.getRefreshableView().setOnGroupClickListener(
				new OnGroupClickListener() {
					@Override
					public boolean onGroupClick(ExpandableListView parent,
							View v, int groupPosition, long id) {
						return true;
					}
				});

		// 展开所有分组
		int groupCount = mAdapter.getGroupCount();
		for (int ix = 0; ix < groupCount; ++ix) {
			mFollowingLv.getRefreshableView().expandGroup(ix);
		}

		// 显示等待对话框
		if (mProgressDialog == null) {
			mProgressDialog = new CustomProgressingDialog(
					FollowingListActivity.this);
		}
		if (!mProgressDialog.isShowing()) {
			mProgressDialog.show();
		}
		refresh();
	}

	private void refresh() {
		canLoadMore = false;
		if (mLastUpdatedTime == DEFAULT_LAST_REFRESH_TIME) {
			mLastUpdatedTime = System.currentTimeMillis();
		}
		mPage = 1;

		MyFollowingListRequest.Builder builder = new MyFollowingListRequest.Builder()
				.setPage(mPage).setType(0).setLastUpdated(mLastUpdatedTime)
				.setListener(refreshListener).setErrorListener(errorListener);

		MyFollowingListRequest request = builder.build();
		request.setTag(TAG);
		RequestQueue requestQueue = PSGodRequestQueue.getInstance(
				FollowingListActivity.this).getRequestQueue();
		requestQueue.add(request);
	}

	private Listener<MyFollowingListRequest.FollowingListWrapper> loadMoreListener = new Listener<MyFollowingListRequest.FollowingListWrapper>() {
		@Override
		public void onResponse(MyFollowingListRequest.FollowingListWrapper response) {
			if (response.myUserList.size() > 0) {
				mMyUsers.addAll(response.myUserList);
				mAdapter.notifyDataSetChanged();
				mFollowingLv.onRefreshComplete();
			}
			mFollowingListFooter.setVisibility(View.INVISIBLE);

			if (response.myUserList.size() < 15) {
				canLoadMore = false;
			} else {
				canLoadMore = true;
			}
		}

	};

	private Listener<MyFollowingListRequest.FollowingListWrapper> refreshListener = new Listener<MyFollowingListRequest.FollowingListWrapper>() {
		@Override
		public void onResponse(MyFollowingListRequest.FollowingListWrapper response) {
			mRecommendUsers.clear();
			mRecommendUsers.addAll(response.recommendUserList);
			mMyUsers.clear();
			mMyUsers.addAll(response.myUserList);
			mTotalMasters = response.mTotalMasters;

			mAdapter.notifyDataSetChanged();
			mAdapter.notifyDataSetChanged(mTotalMasters);
			mFollowingLv.onRefreshComplete();

			if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}

			if (response.myUserList.size() < 15) {
				canLoadMore = false;
			} else {
				canLoadMore = true;
			}

			mEmptyView = FollowingListActivity.this
					.findViewById(R.id.activity_following_list_empty_view);
			mFollowingLv.setEmptyView(mEmptyView);

			// 展开所有分组
			int groupCount = mAdapter.getGroupCount();
			for (int ix = 0; ix < groupCount; ++ix) {
				mFollowingLv.getRefreshableView().expandGroup(ix);
			}

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

	private PSGodErrorListener errorListener = new PSGodErrorListener(this) {
		@Override
		public void handleError(VolleyError error) {
			// TODO Auto-generated method stub
			mFollowingLv.onRefreshComplete();
		}
	};

	private class FollowingListListener implements OnRefreshListener,
			OnLastItemVisibleListener {
		private Context mContext;

		public FollowingListListener(Context context) {
			mContext = context;

			SharedPreferences sp = mContext.getSharedPreferences(
					Constants.SharedPreferencesKey.NAME, Context.MODE_PRIVATE);
			mSpKey = Constants.SharedPreferencesKey.MY_FOCUS_PHOTO_LIST_LAST_REFRESH_TIME;
			mLastUpdatedTime = sp.getLong(mSpKey, DEFAULT_LAST_REFRESH_TIME);
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
				mFollowingListFooter.setVisibility(View.VISIBLE);
				mPage = mPage + 1;
				MyFollowingListRequest.Builder builder = new MyFollowingListRequest.Builder()
						.setPage(mPage).setType(0)
						.setListener(loadMoreListener)
						.setErrorListener(errorListener);
				MyFollowingListRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						mContext).getRequestQueue();
				requestQueue.add(request);
			}
		}
	}

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
