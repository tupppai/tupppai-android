package com.psgod.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.psgod.Constants;
import com.psgod.Logger;
import com.psgod.R;
import com.psgod.ThreadManager;
import com.psgod.WeakReferenceHandler;
import com.psgod.db.DatabaseHelper;
import com.psgod.eventbus.RefreshEvent;
import com.psgod.model.PhotoItem;
import com.psgod.network.NetworkUtil;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.PhotoListRequest;
import com.psgod.ui.adapter.PhotoListAdapter;
import com.psgod.ui.view.PhotoItemView;
import com.psgod.ui.widget.FloatScrollHelper;
import com.psgod.ui.widget.dialog.CameraPopupwindow;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2015/12/7 0007.
 */
public class RecentWorkActivity extends PSGodBaseActivity implements Handler.Callback {
    private static final String TAG = RecentWorkActivity.class.getSimpleName();

    private ViewHolder mViewHolder;
    private List<PhotoItem> mRecentPhotoItems;
    private DatabaseHelper mDatabaseHelper = null;
    private Dao<PhotoItem, Long> mPhotoItemDao;
    private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);
    private ImageView finishImg;
    private ImageView mUpload;
    private RelativeLayout mParent;

    // 带评论
    private PhotoListAdapter mAdapter;
    private PhotoListListener mListener;
    private View mFollowListFooter;
    private int mPage = 1;

    // 上次刷新时间
    private long mLastUpdatedTime;
    // 列表的类型
    private String mSpKey;

    private View mEmptyView;

    // 控制是否可以加载下一页
    private boolean canLoadMore = true;

    private CustomProgressingDialog progressingDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.logMethod(TAG, "onCreate");
        EventBus.getDefault().register(this);

        setContentView(R.layout.activity_recent_work);
        mParent = (RelativeLayout) findViewById(R.id.activity_work_parent);
        mViewHolder = new ViewHolder();
        mViewHolder.mPhotoListView = (PullToRefreshListView) this
                .findViewById(R.id.activity_recent_work_lv);
        mEmptyView = this
                .findViewById(R.id.activity_recent_work_emptyview);

        mFollowListFooter = LayoutInflater.from(this).inflate(
                R.layout.footer_load_more, null);
        mViewHolder.mPhotoListView.getRefreshableView().addFooterView(
                mFollowListFooter);
        mFollowListFooter.setVisibility(View.GONE);

        mRecentPhotoItems = new ArrayList<PhotoItem>();
        mAdapter = new PhotoListAdapter(this,
                PhotoItemView.PhotoListType.RECENT_REPLY, mRecentPhotoItems);
        mViewHolder.mPhotoListView.getRefreshableView().setAdapter(mAdapter);

        mListener = new PhotoListListener(this);
        mViewHolder.mPhotoListView.setOnRefreshListener(mListener);
        mViewHolder.mPhotoListView.setOnLastItemVisibleListener(mListener);

        finishImg = (ImageView) findViewById(R.id.activity_work_finish);
        finishImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if (NetworkUtil.getNetworkType() != NetworkUtil.NetworkType.NONE) {
            mViewHolder.mPhotoListView.setRefreshing(true);
        }

        mViewHolder.mPhotoListView.setScrollingWhileRefreshingEnabled(true);

        mUpload = new ImageView(this);
        mUpload.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mUpload.setImageDrawable(getResources().getDrawable(R.mipmap.floating_btn));
        FloatScrollHelper helper = new FloatScrollHelper(
                mViewHolder.mPhotoListView, mParent, mUpload, this);
        helper.setViewHeight(80);
        helper.setViewMargins(12);
        helper.init();

        // TODO 检测耗时
        try {
            mDatabaseHelper = OpenHelperManager.getHelper(this,
                    DatabaseHelper.class);
            mPhotoItemDao = mDatabaseHelper.getDao(PhotoItem.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        progressingDialog = new CustomProgressingDialog(this);
        progressingDialog.show();
        initListener();
        loadDataAsync();
    }

    private void initListener() {
        mUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecentWorkActivity.this,
                        UploadSelectReplyListActivity.class);
                // bundle.putString("SelectType",
                // MultiImageSelectActivity.TYPE_REPLY_SELECT);
                // intent.putExtras(bundle);
                startActivity(intent);
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

    /**
     * TODO 没有被调用
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
                "onDestroy");
        EventBus.getDefault().unregister(this);
        // Step2: 释放内存
        if (mDatabaseHelper != null) {
            OpenHelperManager.releaseHelper();
            mDatabaseHelper = null;
        }

        mPhotoItemDao = null;
    }

    // 触发自动下拉刷新
    private void setRefreshing() {
        mViewHolder.mPhotoListView.setRefreshing(true);
    }

    public void onEventMainThread(RefreshEvent event) {
        if (event.className.equals(this.getClass().getName())) {
            try {
                setRefreshing();
            } catch (NullPointerException nu) {
            } catch (Exception e) {
            }
        }
    }

    private void loadDataAsync() {
        ThreadManager.executeOnFileThread(new Runnable() {
            @Override
            public void run() {
                try {
                    // mDbPhotoItems = mPhotoItemDao.queryForAll();
                    List<PhotoItem> items = mPhotoItemDao.queryBuilder()
                            .orderBy("update_time", false).where()
                            .eq("from", PhotoItem.TYPE_RECENT_WORK).query();
                    // mPhotoItems.addAll(items);

                    Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR,
                            TAG, "loadDataAsync(): size=" + items.size());

                    Message msg = mHandler.obtainMessage();
                    msg.obj = items;
                    msg.sendToTarget();
                } catch (SQLException e) {
                    // Log.e(LOG_TAG, "Database exception", e);
                    // tv.setText("Database exeption: " + e);
                    return;
                }
            }
        });
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.obj instanceof List<?>) {
            mRecentPhotoItems.clear();
            List<PhotoItem> items = (List<PhotoItem>) msg.obj;
            mRecentPhotoItems.addAll(items);
            mAdapter.notifyDataSetChanged();
            if (NetworkUtil.getNetworkType() != NetworkUtil.NetworkType.NONE) {
                mViewHolder.mPhotoListView.setRefreshing(true);
            }
        }
        return true;
    }

    private class PhotoListListener implements PullToRefreshBase.OnLastItemVisibleListener,
            PullToRefreshBase.OnRefreshListener<ListView> {
        private static final long DEFAULT_LAST_REFRESH_TIME = -1;

        public PhotoListListener(Context context) {
            SharedPreferences sp = RecentWorkActivity.this.getSharedPreferences(
                    Constants.SharedPreferencesKey.NAME, Context.MODE_PRIVATE);
            mSpKey = Constants.SharedPreferencesKey.RECENT_PHOTO_LIST_LAST_REFRESH_TIME;

            mLastUpdatedTime = sp.getLong(mSpKey, DEFAULT_LAST_REFRESH_TIME);
        }

        @Override
        public void onLastItemVisible() {
            if (canLoadMore) {
                mPage += 1;
                mFollowListFooter.setVisibility(View.VISIBLE);

                PhotoListRequest.Builder builder = new PhotoListRequest.Builder()
                        .setPage(mPage).setLastUpdated(mLastUpdatedTime)
                        .setType(PhotoItem.TYPE_RECENT_WORK)
                        .setListener(loadMoreListener)
                        .setErrorListener(errorListener);

                PhotoListRequest request = builder.build();
                request.setTag(TAG);
                RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                        RecentWorkActivity.this).getRequestQueue();
                requestQueue.add(request);
            }
        }

        @Override
        public void onRefresh(PullToRefreshBase<ListView> refreshView) {
            mPage = 1;
            // 上次刷新时间
            if (mLastUpdatedTime == DEFAULT_LAST_REFRESH_TIME) {
                mLastUpdatedTime = System.currentTimeMillis();
            }

            PhotoListRequest.Builder builder = new PhotoListRequest.Builder()
                    .setPage(mPage).setLastUpdated(mLastUpdatedTime)
                    .setType(PhotoItem.TYPE_RECENT_WORK)
                    .setListener(refreshListener)
                    .setErrorListener(errorListener);

            PhotoListRequest request = builder.build();
            request.setTag(TAG);
            RequestQueue requestQueue = PSGodRequestQueue.getInstance(RecentWorkActivity.this)
                    .getRequestQueue();
            requestQueue.add(request);
        }
    }

    private Response.ErrorListener errorListener = new PSGodErrorListener() {
        @Override
        public void handleError(VolleyError error) {
            // TODO Auto-generated method stub
            mViewHolder.mPhotoListView.onRefreshComplete();
            mFollowListFooter.setVisibility(View.INVISIBLE);

            if (progressingDialog.isShowing()) {
                progressingDialog.dismiss();
            }
        }
    };

    private Response.Listener<List<PhotoItem>> loadMoreListener = new Response.Listener<List<PhotoItem>>() {
        @Override
        public void onResponse(final List<PhotoItem> items) {
            mRecentPhotoItems.addAll(items);
            mAdapter.notifyDataSetChanged();
            mFollowListFooter.setVisibility(View.INVISIBLE);

            if (items.size() < 15) {
                canLoadMore = false;
            } else {
                canLoadMore = true;
            }

            if (progressingDialog.isShowing()) {
                progressingDialog.dismiss();
            }
        }
    };

    private Response.Listener<List<PhotoItem>> refreshListener = new Response.Listener<List<PhotoItem>>() {
        @Override
        public void onResponse(List<PhotoItem> items) {
            mRecentPhotoItems.clear();
            mRecentPhotoItems.addAll(items);
            mAdapter.notifyDataSetChanged();

            mViewHolder.mPhotoListView.onRefreshComplete();

            PhotoItem.savePhotoList(RecentWorkActivity.this, mPhotoItemDao, items,
                    PhotoItem.TYPE_RECENT_WORK);

            mViewHolder.mPhotoListView.setEmptyView(mEmptyView);

            if (items.size() < 15) {
                canLoadMore = false;
            } else {
                canLoadMore = true;
            }
            // 保存本次刷新时间到sp
            mLastUpdatedTime = System.currentTimeMillis();
            if (android.os.Build.VERSION.SDK_INT >= 9) {
                RecentWorkActivity.this
                        .getSharedPreferences(
                                Constants.SharedPreferencesKey.NAME,
                                Context.MODE_PRIVATE).edit()
                        .putLong(mSpKey, mLastUpdatedTime).apply();
            } else {
                RecentWorkActivity.this.getSharedPreferences(
                        Constants.SharedPreferencesKey.NAME,
                        Context.MODE_PRIVATE).edit()
                        .putLong(mSpKey, mLastUpdatedTime).commit();
            }

            if (progressingDialog.isShowing()) {
                progressingDialog.dismiss();
            }
        }
    };

    /**
     * 保存视图组件，避免视图的重复加载
     */
    private static class ViewHolder {
        View mView;
        PullToRefreshListView mPhotoListView;
    }
}
