package com.psgod.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.psgod.R;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.PhotoListRequest;
import com.psgod.network.request.UserPhotoRequest;
import com.psgod.ui.adapter.PhotoWaterFallListAdapter;
import com.psgod.ui.view.PhotoWaterFallItemView.PhotoWaterFallListType;
import com.psgod.ui.view.PullToRefreshStaggeredGridView;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 全部作品页面
 *
 * @author ZouMengyuan
 */
public class WorksListActivity extends PSGodBaseActivity {
    private final static String TAG = WorksListActivity.class.getSimpleName();

    private Context mContext;
    private List<PhotoItem> mPhotoItems = new ArrayList<PhotoItem>();
    private PullToRefreshStaggeredGridView mWorkdListView = null;
    private View mFootView;
    private ImageButton mBackButton;
    private PhotoWaterFallListAdapter mWorksAdapter = null;
    private CustomProgressingDialog mProgressDialog;
    private WorkListListener mWorkListListener;

    private int mPage = 1;
    // 控制是否可以加载下一页
    private boolean canLoadMore = false;
    private Long askId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_works_list);
        mContext = this;

        Intent intent = getIntent();
        askId = intent.getLongExtra("ASKID", 0);

        initViews();
        initListeners();

        if (mProgressDialog == null) {
            mProgressDialog = new CustomProgressingDialog(mContext);
        }

        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }

        refresh();
    }

    public void initViews() {
        mWorkdListView = (PullToRefreshStaggeredGridView) findViewById(R.id.list_work);
        mWorkdListView.setMode(Mode.PULL_FROM_START);
        mBackButton = (ImageButton) findViewById(R.id.btn_back);

        mWorksAdapter = new PhotoWaterFallListAdapter(mContext, mPhotoItems,
                PhotoWaterFallListType.ALL_WORK);
        mWorksAdapter.setType(1);
        mWorkdListView.setAdapter(mWorksAdapter);

        mFootView = LayoutInflater.from(mContext).inflate(
                R.layout.footer_load_more, null);
        mFootView.setVisibility(View.GONE);
        mWorkdListView.getRefreshableView().addFooterView(mFootView);

        mWorkListListener = new WorkListListener(mContext);
        mWorkdListView.setOnRefreshListener(mWorkListListener);
        mWorkdListView.setOnLastItemVisibleListener(mWorkListListener);
        mWorkdListView.setScrollingWhileRefreshingEnabled(true);

    }

    public void initListeners() {
        mBackButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

//		mWorkdListView.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View view,
//					int position, long vid) {
//				new CarouselPhotoDetailDialog(mContext,mPhotoItems.get(position - 1).getAskId(),
//						mPhotoItems.get(position - 1).getPid()).show();
//
//
//				SinglePhotoDetail.startActivity(WorksListActivity.this,
//						mPhotoItems.get(position - 1));
//			}
//		});
    }

    private class WorkListListener implements OnLastItemVisibleListener,
            OnRefreshListener {
        private Context mContext;

        public WorkListListener(Context context) {
            mContext = context;
        }

        @Override
        public void onLastItemVisible() {
            if (canLoadMore) {
                mPage = mPage + 1;
                mFootView.setVisibility(View.VISIBLE);
                // 上拉加载更多
                PhotoListRequest.Builder builder = new PhotoListRequest.Builder()
                        .setPage(mPage).setType(PhotoItem.TYPE_RECENT_WORK)
                        .setAskId(askId).setListener(loadMoreListener)
                        .setErrorListener(errorListener);

                PhotoListRequest request = builder.build();
                request.setTag(TAG);
                RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                        mContext).getRequestQueue();
                requestQueue.add(request);
            }
        }

        @Override
        public void onRefresh(PullToRefreshBase refreshView) {
            refresh();
        }
    }

    // 刷新操作
    private void refresh() {
        mPage = 1;
        canLoadMore = false;

        PhotoListRequest.Builder builder = new PhotoListRequest.Builder()
                .setPage(mPage).setType(PhotoItem.TYPE_RECENT_WORK)
                .setAskId(askId).setListener(refreshListener)
                .setErrorListener(errorListener);

        PhotoListRequest request = builder.build();
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
            mWorksAdapter.notifyDataSetChanged();
            mWorkdListView.onRefreshComplete();

            if (items.size() < 15) {
                canLoadMore = false;
            } else {
                canLoadMore = true;
            }

            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            mWorkdListView.setEmptyView(
                    LayoutInflater.from(WorksListActivity.this).inflate(R.layout.view_empty, null));

        }
    };

    private Listener<List<PhotoItem>> loadMoreListener = new Listener<List<PhotoItem>>() {
        @Override
        public void onResponse(List<PhotoItem> items) {
            if (items.size() > 0) {
                mPhotoItems.addAll(items);
            }
            mWorksAdapter.notifyDataSetChanged();
            mWorkdListView.onRefreshComplete();

            mFootView.setVisibility(View.INVISIBLE);

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
            mWorkdListView.onRefreshComplete();
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
