package com.psgod.ui.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.model.notification.NotificationMessage;
import com.psgod.network.request.MessageListRequest;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.ui.adapter.MessageListAdapter;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

public class MessageSystemActivity extends PSGodBaseActivity {

	private static final String TAG = MessageSystemActivity.class
			.getSimpleName();

	private PullToRefreshListView mListView;
	private View mEmptyView;
	private MessageListener mMessageListener;
	private MessageListAdapter mMessageListAdapter;
	private List<NotificationMessage> mMessages;

	private CustomProgressingDialog mProgressDialog;
	private long mLastUpdateTime;
	private String mSpKey;
	private static final long DEFAULT_LAST_REFRESH_TIME = -1;
	private int mPage;

	private Context mContext;

	// 控制是否可以加载下一页
	private boolean canLoadMore = true;
	private View mMessageListFooter;

	private int MESSAGE_TYPE = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_system);

		mContext = this;

		mMessages = new ArrayList<NotificationMessage>();
		mListView = (PullToRefreshListView) this
				.findViewById(R.id.activity_new_message_system_list_listview);
		mListView.setMode(Mode.PULL_FROM_START);

		mMessageListFooter = LayoutInflater.from(MessageSystemActivity.this)
				.inflate(R.layout.footer_load_more, null);
		mListView.getRefreshableView().addFooterView(mMessageListFooter);
		mMessageListFooter.setVisibility(View.GONE);

		mMessageListAdapter = new MessageListAdapter(this, mMessages);
		mListView.getRefreshableView().setAdapter(mMessageListAdapter);

		mMessageListener = new MessageListener(this);
		mListView.setOnRefreshListener(mMessageListener);
		mListView.setOnLastItemVisibleListener(mMessageListener);
		mListView.setScrollingWhileRefreshingEnabled(true);

		// 显示等待对话框
		if (mProgressDialog == null) {
			mProgressDialog = new CustomProgressingDialog(
					MessageSystemActivity.this);
		}
		if (!mProgressDialog.isShowing()) {
			mProgressDialog.show();
		}

		refresh();
	}


	private class MessageListener implements OnRefreshListener,
			OnLastItemVisibleListener {
		private Context mContext;

		public MessageListener(Context context) {
			mContext = context;

			SharedPreferences sp = mContext.getSharedPreferences(
					Constants.SharedPreferencesKey.NAME, Context.MODE_PRIVATE);
			mSpKey = Constants.SharedPreferencesKey.MESSAGE_SYSTEM_LIST_LAST_REFRESH_TIME;
			mLastUpdateTime = sp.getLong(mSpKey, DEFAULT_LAST_REFRESH_TIME);
		}

		@Override
		public void onLastItemVisible() {
			// TODO Auto-generated method stub
			if (canLoadMore) {
				mMessageListFooter.setVisibility(View.VISIBLE);
				mPage += 1;
				MessageListRequest.Builder builder = new MessageListRequest.Builder()
						.setPage(mPage).setType(MESSAGE_TYPE)
						.setErrorListener(errorListener)
						.setListener(loadMoreListener);
				MessageListRequest request = builder.build();
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
		MessageListRequest.Builder builder = new MessageListRequest.Builder()
				.setPage(mPage).setType(MESSAGE_TYPE)
				.setLastUpdated(mLastUpdateTime)
				.setErrorListener(errorListener).setListener(refreshListener);
		MessageListRequest request = builder.build();
		request.setTag(TAG);
		RequestQueue requestQueue = PSGodRequestQueue.getInstance(mContext)
				.getRequestQueue();
		requestQueue.add(request);
	}

	private Listener<List<NotificationMessage>> refreshListener = new Listener<List<NotificationMessage>>() {
		@Override
		public void onResponse(List<NotificationMessage> items) {
			mMessages.clear();
			mMessages.addAll(items);
			mMessageListAdapter.notifyDataSetChanged();

			mListView.onRefreshComplete();

			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}

			if (items.size() < 10) {
				canLoadMore = false;
			} else {
				canLoadMore = true;
			}

			mEmptyView = MessageSystemActivity.this
					.findViewById(R.id.activity_message_system_list_empty_view);
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

	private Listener<List<NotificationMessage>> loadMoreListener = new Listener<List<NotificationMessage>>() {
		@Override
		public void onResponse(List<NotificationMessage> items) {
			if (items.size() > 0) {
				mMessages.addAll(items);
				mMessageListAdapter.notifyDataSetChanged();
				mListView.onRefreshComplete();
			}

			mMessageListFooter.setVisibility(View.INVISIBLE);

			if (items.size() < 10) {
				canLoadMore = false;
			} else {
				canLoadMore = true;
			}
		}
	};

	private ErrorListener errorListener = new ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {
			Toast.makeText(MessageSystemActivity.this, error.getMessage(),
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
