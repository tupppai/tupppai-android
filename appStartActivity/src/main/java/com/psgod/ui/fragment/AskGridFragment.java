package com.psgod.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.psgod.Constants;
import com.psgod.Logger;
import com.psgod.R;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.UserPhotoRequest;
import com.psgod.ui.adapter.AskGridAdapter;
import com.psgod.ui.adapter.GridAdapter;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

public class AskGridFragment extends BaseFragment {
	private static final String TAG = AskGridFragment.class.getSimpleName();
	private static final int MY_ASK = 0;

	private AskGridViewHolder mViewHolder;



	private int mPage = 1;
	private String mSpKey;
	private static final long DEFAULT_LAST_REFRESH_TIME = -1;
	private long mLastUpdatedTime;

	private CustomProgressingDialog mProgressDialog;
	private boolean canLoadMore = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FrameLayout parentView = new FrameLayout(getActivity());
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		parentView.setLayoutParams(params);

		mViewHolder = new AskGridViewHolder();
		mViewHolder.parentView = parentView;
		mViewHolder.view = LayoutInflater.from(getActivity()).inflate(
				R.layout.fragment_ask_grid, parentView, true);
		mViewHolder.listView = (PullToRefreshListView) mViewHolder.view
				.findViewById(R.id.fragment_ask_grid_listview);
		mViewHolder.listView.setMode(Mode.DISABLED);

		mViewHolder.mFooterView = LayoutInflater.from(getActivity()).inflate(
				R.layout.footer_load_more, null);
		mViewHolder.listView.getRefreshableView().addFooterView(
				mViewHolder.mFooterView);
		mViewHolder.mFooterView.setVisibility(View.GONE);

		mViewHolder.mPhotoItems = new ArrayList<PhotoItem>();
		mViewHolder.mAdapter = new AskGridAdapter(getActivity(),
				mViewHolder.mPhotoItems);
		// 将原始的adapter转化为每行三个
		GridAdapter<AskGridAdapter> adapter = new GridAdapter<AskGridAdapter>(
				getActivity(), mViewHolder.mAdapter);
		adapter.setNumColumns(3);
		mViewHolder.listView.getRefreshableView().setAdapter(adapter);

		mViewHolder.mAskGridListener = new AskGridListener(getActivity());
		mViewHolder.listView
				.setOnLastItemVisibleListener(mViewHolder.mAskGridListener);
		mViewHolder.listView.setScrollingWhileRefreshingEnabled(true);

		// 显示等待对话框
		if (mProgressDialog == null) {
			mProgressDialog = new CustomProgressingDialog(getActivity());
		}
		if (!mProgressDialog.isShowing()) {
			mProgressDialog.show();
		}

		refresh();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Logger.logMethod(TAG, "onCreateView");
		FrameLayout parentView = new FrameLayout(getActivity());
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		parentView.setLayoutParams(params);
		mViewHolder.parentView.removeView(mViewHolder.view);
		parentView.addView(mViewHolder.view);
		mViewHolder.parentView = parentView;
		return parentView;
	}

	/**
	 * 保存视图组件，避免视图的重复加载
	 * 
	 * @author Rayal
	 * 
	 */
	private static class AskGridViewHolder {
		ViewGroup parentView;
		View view;
		PullToRefreshListView listView;
		AskGridListener mAskGridListener;
		List<PhotoItem> mPhotoItems;
		AskGridAdapter mAdapter;
		View mFooterView;
	}

	private class AskGridListener implements OnLastItemVisibleListener {
		private Context mContext;

		public AskGridListener(Context context) {
			mContext = context;

			SharedPreferences sp = mContext.getSharedPreferences(
					Constants.SharedPreferencesKey.NAME, Context.MODE_PRIVATE);
			mSpKey = Constants.SharedPreferencesKey.MY_ASK_PHOTO_LIST_LAST_REFRESH_TIME;
			mLastUpdatedTime = sp.getLong(mSpKey, DEFAULT_LAST_REFRESH_TIME);
		}

		@Override
		public void onLastItemVisible() {
			if (canLoadMore) {
				mViewHolder.mFooterView.setVisibility(View.VISIBLE);
				mPage = mPage + 1;
				UserPhotoRequest.Builder builder = new UserPhotoRequest.Builder()
						.setType(MY_ASK).setPage(mPage)
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
				.setType(MY_ASK).setPage(mPage)
				.setLastUpdated(mLastUpdatedTime).setListener(refreshListener)
				.setErrorListener(errorListener);

		UserPhotoRequest request = builder.build();
		request.setTag(TAG);
		RequestQueue requestQueue = PSGodRequestQueue
				.getInstance(getActivity()).getRequestQueue();
		requestQueue.add(request);
	}

	private Listener<List<PhotoItem>> refreshListener = new Listener<List<PhotoItem>>() {
		@Override
		public void onResponse(List<PhotoItem> items) {
			mViewHolder.mPhotoItems.clear();
			mViewHolder.mPhotoItems.addAll(items);
			mViewHolder.mAdapter.notifyDataSetChanged();
			mViewHolder.listView.onRefreshComplete();

			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();

			}

			if (items.size() < 15) {
				canLoadMore = false;
			} else {
				canLoadMore = true;
			}

			View emptyView = mViewHolder.view
					.findViewById(R.id.fragment_ask_grid_empty_view);
			mViewHolder.listView.setEmptyView(emptyView);

			// 保存本次刷新时间到sp
			mLastUpdatedTime = System.currentTimeMillis();
			if (android.os.Build.VERSION.SDK_INT >= 9) {
				getActivity()
						.getSharedPreferences(
								Constants.SharedPreferencesKey.NAME,
								Context.MODE_PRIVATE).edit()
						.putLong(mSpKey, mLastUpdatedTime).apply();
			} else {
				getActivity()
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
				mViewHolder.mPhotoItems.addAll(items);
			}
			mViewHolder.mAdapter.notifyDataSetChanged();
			mViewHolder.listView.onRefreshComplete();

			mViewHolder.mFooterView.setVisibility(View.INVISIBLE);
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
			mViewHolder.listView.onRefreshComplete();
		}
	};
}