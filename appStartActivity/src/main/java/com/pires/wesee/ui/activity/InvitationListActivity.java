package com.pires.wesee.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;
import com.pires.wesee.Constants;
import com.pires.wesee.R;
import com.pires.wesee.model.User;
import com.pires.wesee.network.request.MyFollowingListRequest;
import com.pires.wesee.network.request.MyFollowingListRequest.FollowingListWrapper;
import com.pires.wesee.network.request.PSGodErrorListener;
import com.pires.wesee.network.request.PSGodRequestQueue;
import com.pires.wesee.ui.adapter.InvitationExpandableListAdapter;
import com.pires.wesee.ui.widget.ActionBar;
import com.pires.wesee.ui.widget.dialog.CustomProgressingDialog;

import java.util.ArrayList;
import java.util.List;

public class InvitationListActivity extends PSGodBaseActivity {
	private final static String TAG = InvitationListActivity.class
			.getSimpleName();

	public static final int JUMP_FROM_UPLOAD_ASK = 1000;
	private PullToRefreshExpandableListView mInvitationLv;
	private InvitationExpandableListAdapter mAdapter;

	private ActionBar mActionBar;
	private Long mAskId;
	// 推荐大神列表
	private List<User> godList;
	// 我的关注人列表
	private List<User> friendList;

	private int mPage = 1;
	private CustomProgressingDialog mProgressDialog;
	private InvitationListListener mInvitationListListner;

	private int activity_jump_from = 0;

	private long mLastUpdatedTime;
	private String mSpKey;
	private static final long DEFAULT_LAST_REFRESH_TIME = -1;
	// 控制是否可以加载下一页
	private boolean canLoadMore = true;
	private View mFollowingListFooter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_invitation_list);

		Intent intent = getIntent();

		mAskId = intent.getLongExtra(Constants.IntentKey.ASK_ID, -1);
		activity_jump_from = intent.getIntExtra(
				Constants.IntentKey.ACTIVITY_JUMP_FROM, 0);

		if (mAskId == -1) {
			finish();
		}

		mActionBar = (ActionBar) this.findViewById(R.id.actionbar);
		if (activity_jump_from == 0) {
			mActionBar.setRightBtnVisibility(View.INVISIBLE);
		}

		mInvitationLv = (PullToRefreshExpandableListView) this
				.findViewById(R.id.activity_invitation_list_lv);
		mInvitationLv.setMode(Mode.PULL_FROM_START);
		mInvitationLv.getRefreshableView().setDivider(null);

		godList = new ArrayList<User>();
		friendList = new ArrayList<User>();
		mAdapter = new InvitationExpandableListAdapter(this, godList,
				friendList, mAskId);
		mInvitationLv.getRefreshableView().setAdapter(mAdapter);

		mFollowingListFooter = LayoutInflater.from(InvitationListActivity.this)
				.inflate(R.layout.footer_load_more, null);
		mInvitationLv.getRefreshableView().addFooterView(mFollowingListFooter);
		mFollowingListFooter.setVisibility(View.GONE);

		mInvitationListListner = new InvitationListListener(this);
		mInvitationLv.setOnRefreshListener(mInvitationListListner);
		mInvitationLv.setOnLastItemVisibleListener(mInvitationListListner);
		mInvitationLv.setScrollingWhileRefreshingEnabled(true);

		// 设置分组不可点击
		mInvitationLv.getRefreshableView().setOnGroupClickListener(
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
			mInvitationLv.getRefreshableView().expandGroup(ix);
		}

		initEvents();
		// 显示等待对话框
		if (mProgressDialog == null) {
			mProgressDialog = new CustomProgressingDialog(
					InvitationListActivity.this);
		}
		if (!mProgressDialog.isShowing()) {
			mProgressDialog.show();
		}
		refresh();
	}

	private class InvitationListListener implements OnRefreshListener,
			OnLastItemVisibleListener {
		private Context mContext;

		public InvitationListListener(Context context) {
			mContext = context;

			SharedPreferences sp = mContext.getSharedPreferences(
					Constants.SharedPreferencesKey.NAME, MODE_PRIVATE);
			mSpKey = Constants.SharedPreferencesKey.INVITATION_LIST_LAST_REFRESH_TIME;
			mLastUpdatedTime = sp.getLong(mSpKey, DEFAULT_LAST_REFRESH_TIME);
		}

		@Override
		public void onRefresh(PullToRefreshBase refreshView) {
			refresh();
		}

		@Override
		public void onLastItemVisible() {
			if (canLoadMore) {
				mPage = mPage + 1;
				mFollowingListFooter.setVisibility(View.VISIBLE);
				MyFollowingListRequest.Builder builder = new MyFollowingListRequest.Builder()
						.setPage(mPage).setType(1).setAskId(mAskId)
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

	private Listener<FollowingListWrapper> loadMoreListener = new Listener<FollowingListWrapper>() {
		@Override
		public void onResponse(FollowingListWrapper response) {
			friendList.addAll(response.myUserList);

			mAdapter.notifyDataSetChanged();
			mInvitationLv.onRefreshComplete();

			mFollowingListFooter.setVisibility(View.INVISIBLE);

			if (response.myUserList.size() < 15) {
				canLoadMore = false;
			} else {
				canLoadMore = true;
			}
		}

	};

	private void refresh() {
		canLoadMore = false;
		mPage = 1;
		if (mLastUpdatedTime == DEFAULT_LAST_REFRESH_TIME) {
			mLastUpdatedTime = System.currentTimeMillis();
		}
		MyFollowingListRequest.Builder builder = new MyFollowingListRequest.Builder()
				.setPage(mPage).setType(1).setAskId(mAskId)
				.setLastUpdated(mLastUpdatedTime).setListener(refreshListener)
				.setErrorListener(errorListener);

		MyFollowingListRequest request = builder.build();
		request.setTag(TAG);
		RequestQueue requestQueue = PSGodRequestQueue.getInstance(
				InvitationListActivity.this).getRequestQueue();
		requestQueue.add(request);
	}

	private PSGodErrorListener errorListener = new PSGodErrorListener(this) {
		@Override
		public void handleError(VolleyError error) {
			// TODO Auto-generated method stub
			mInvitationLv.onRefreshComplete();
		}
	};

	private Listener<FollowingListWrapper> refreshListener = new Listener<FollowingListWrapper>() {
		@Override
		public void onResponse(FollowingListWrapper response) {
			godList.clear();
			godList.addAll(response.recommendUserList);
			friendList.clear();
			friendList.addAll(response.myUserList);

			mAdapter.notifyDataSetChanged();
			mInvitationLv.onRefreshComplete();

			if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}

			// 展开所有分组
			int groupCount = mAdapter.getGroupCount();
			for (int ix = 0; ix < groupCount; ++ix) {
				mInvitationLv.getRefreshableView().expandGroup(ix);
			}

			if (response.myUserList.size() < 15) {
				canLoadMore = false;
			} else {
				canLoadMore = true;
			}

			// 保存本次刷新时间到sp
			mLastUpdatedTime = System.currentTimeMillis();
			if (android.os.Build.VERSION.SDK_INT >= 9) {
				getApplicationContext()
						.getSharedPreferences(
								Constants.SharedPreferencesKey.NAME,
								MODE_PRIVATE).edit()
						.putLong(mSpKey, mLastUpdatedTime).apply();
			} else {
				getApplicationContext()
						.getSharedPreferences(
								Constants.SharedPreferencesKey.NAME,
								MODE_PRIVATE).edit()
						.putLong(mSpKey, mLastUpdatedTime).commit();
			}
		}

	};

	private void initEvents() {
		mActionBar.setRightBtnOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (activity_jump_from == JUMP_FROM_UPLOAD_ASK) {
					Intent intent = new Intent(InvitationListActivity.this,
							MainActivity.class);
					intent.putExtra(
							MainActivity.IntentParams.KEY_FRAGMENT_ID,
							MainActivity.IntentParams.VALUE_FRAGMENT_ID_HOMEPAGE);
					intent.putExtra(MainActivity.IntentParams.KEY_HOMEPAGE_ID,
							MainActivity.IntentParams.VALUE_HOMEPAGE_ID_FOCUS);
					intent.putExtra(MainActivity.IntentParams.KEY_NEED_REFRESH,
							true);
					startActivity(intent);
					finish();
				} else {
					finish();
				}
			}
		});
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
