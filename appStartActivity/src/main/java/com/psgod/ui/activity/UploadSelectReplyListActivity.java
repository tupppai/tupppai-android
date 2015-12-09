package com.psgod.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.psgod.R;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.UserPhotoRequest;
import com.psgod.ui.adapter.UploadSelectReplyAdapter;

import java.util.ArrayList;
import java.util.List;

public class UploadSelectReplyListActivity extends PSGodBaseActivity {
	private Context mContext;
	private PullToRefreshListView mReplyList;
	private List<PhotoItem> mPhotoItems = new ArrayList<PhotoItem>();
	private UploadSelectReplyAdapter mReplyAdapter;
	public static final int MY_INPROGRESS = 2;
	private int mPage = 1;
	private View mEmptyView;
	private String mChannelid;

	private static final String TAG = UploadSelectReplyListActivity.class
			.getSimpleName();

	private MyUploadSelectListener mSelectListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_upload_reply_list);
		mContext = this;

		mReplyList = (PullToRefreshListView) this
				.findViewById(R.id.select_reply_listview);
		mReplyList.setMode(Mode.PULL_FROM_START);

		mReplyAdapter = new UploadSelectReplyAdapter(mContext, mPhotoItems);
		mReplyList.getRefreshableView().setAdapter(mReplyAdapter);

		mSelectListener = new MyUploadSelectListener(mContext);
		mReplyList.setOnRefreshListener(mSelectListener);
		mReplyList.setOnLastItemVisibleListener(mSelectListener);
		mReplyList.setScrollingWhileRefreshingEnabled(true);
		
		mEmptyView = (View) findViewById(R.id.inprogress_fragment_reply_empty_view);

		Intent intent = getIntent();
		mChannelid = intent.getStringExtra("channel_id");
		refresh();
	}

	private class MyUploadSelectListener implements OnLastItemVisibleListener,
			OnRefreshListener {
		private Context mContext;

		public MyUploadSelectListener(Context context) {
			mContext = context;

		}

		@Override
		public void onLastItemVisible() {
			mPage++;
			// 上拉加载更多
			UserPhotoRequest.Builder builder = new UserPhotoRequest.Builder()
					.setType(MY_INPROGRESS).setPage(mPage)
					.setListener(loadMoreListener)
					.setErrorListener(errorListener);

			if(mChannelid!=null && !mChannelid.equals("")){
				builder.setChannelId(mChannelid);
			}

			UserPhotoRequest request = builder.build();
			request.setTag(TAG);
			RequestQueue requestQueue = PSGodRequestQueue.getInstance(mContext)
					.getRequestQueue();
			requestQueue.add(request);
		}

		@Override
		public void onRefresh(PullToRefreshBase refreshView) {
			refresh();
		}
	}

	// 刷新操作
	private void refresh() {
		mPage = 1;

		UserPhotoRequest.Builder builder = new UserPhotoRequest.Builder()
				.setType(MY_INPROGRESS).setPage(mPage)
				.setListener(refreshListener).setErrorListener(errorListener);

		if(mChannelid!=null && !mChannelid.equals("")){
			builder.setChannelId(mChannelid);
		}

		UserPhotoRequest request = builder.build();
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
			mReplyAdapter.notifyDataSetChanged();
			mReplyList.onRefreshComplete();
			
			mReplyList.getRefreshableView().setEmptyView(
					mEmptyView);

		}
	};

	private Listener<List<PhotoItem>> loadMoreListener = new Listener<List<PhotoItem>>() {
		@Override
		public void onResponse(List<PhotoItem> items) {
			if (items.size() > 0) {
				mPhotoItems.addAll(items);
			}
			mReplyAdapter.notifyDataSetChanged();
			mReplyList.onRefreshComplete();

		}
	};

	private PSGodErrorListener errorListener = new PSGodErrorListener(
			UserPhotoRequest.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {
			// TODO
			mReplyList.onRefreshComplete();
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
