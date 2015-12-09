package com.psgod.ui.fragment;

/**
 * 首页热门Fragment
 *
 * @author brandwang
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.psgod.Constants;
import com.psgod.Logger;
import com.psgod.R;
import com.psgod.ThreadManager;
import com.psgod.WeakReferenceHandler;
import com.psgod.db.DatabaseHelper;
import com.psgod.eventbus.CommentEvent;
import com.psgod.eventbus.RefreshEvent;
import com.psgod.model.BannerData;
import com.psgod.model.Comment;
import com.psgod.model.PhotoItem;
import com.psgod.network.NetworkUtil;
import com.psgod.network.request.HomePageGetBannerRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.PhotoListRequest;
import com.psgod.ui.activity.CommentListActivity;
import com.psgod.ui.adapter.HomePageHotAdapter;
import com.psgod.ui.view.PhotoItemView;
import com.psgod.ui.widget.EditPopupWindow;
import com.psgod.ui.widget.EditPopupWindow.OnResponseListener;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

public class HomePageHotFragment extends BaseFragment implements Callback {
    private static final String TAG = HomePageHotFragment.class.getSimpleName();

    private Context mContext;
    private ViewHolder mViewHolder;
    private List<PhotoItem> mHotPhotoItems;
    private DatabaseHelper mDatabaseHelper = null;
    private Dao<PhotoItem, Long> mPhotoItemDao;
    private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);
    private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);

    // 带评论
    private HomePageHotAdapter mAdapter;
    private PhotoListListener mListener;
    private View mFollowListFooter;
    private int mPage = 1;

    // 上次刷新时间
    private long mLastUpdatedTime;
    // 列表的类型
    private String mSpKey;

    // 控制是否可以加载下一页
    private boolean canLoadMore = true;

    private EditPopupWindow editWindow;
    //  显示的banner
    private List<BannerData> bannerList = new ArrayList<BannerData>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.logMethod(TAG, "onCreate");
        EventBus.getDefault().register(this);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Logger.logMethod(TAG, "onCreateView");
        mContext = getActivity();
        FrameLayout parentView = new FrameLayout(mContext);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        parentView.setLayoutParams(params);

        mViewHolder = new ViewHolder();
        mViewHolder.mParentView = parentView;
        mViewHolder.mView = LayoutInflater.from(mContext).inflate(
                R.layout.fragment_homepage_hot, parentView, true);
        mViewHolder.mPhotoListView = (PullToRefreshExpandableListView) mViewHolder.mView
                .findViewById(R.id.fragment_homepage_hot_lv);

        mFollowListFooter = LayoutInflater.from(mContext).inflate(
                R.layout.footer_load_more, null);
        mViewHolder.mPhotoListView.getRefreshableView().addFooterView(
                mFollowListFooter);
        mFollowListFooter.setVisibility(View.GONE);

        mHotPhotoItems = new ArrayList<PhotoItem>();
        mAdapter = new HomePageHotAdapter(mContext,
                PhotoItemView.PhotoListType.HOT_FOCUS_REPLY, mHotPhotoItems,
                bannerList);
        mViewHolder.mPhotoListView.getRefreshableView().setAdapter(mAdapter);

        mListener = new PhotoListListener(mContext);
        mViewHolder.mPhotoListView.setOnRefreshListener(mListener);
        mViewHolder.mPhotoListView.setOnLastItemVisibleListener(mListener);

        mViewHolder.mParent = (RelativeLayout) mViewHolder.mView
                .findViewById(R.id.fragment_homepage_hot_parent);

        if (NetworkUtil.getNetworkType() != NetworkUtil.NetworkType.NONE) {
            mViewHolder.mPhotoListView.setRefreshing(true);
        }

        mViewHolder.mPhotoListView.setScrollingWhileRefreshingEnabled(true);

        int groupCount = mAdapter.getGroupCount();
        for (int ix = 0; ix < groupCount; ++ix) {
            mViewHolder.mPhotoListView.getRefreshableView().expandGroup(ix);
        }

        // 设置点击分组不可展开
        mViewHolder.mPhotoListView.getRefreshableView()
                .setOnGroupClickListener(new OnGroupClickListener() {
                    @Override
                    public boolean onGroupClick(ExpandableListView parent,
                                                View v, int groupPosition, long id) {
                        return true;
                    }
                });

        // TODO 检测耗时
        try {
            mDatabaseHelper = OpenHelperManager.getHelper(mContext,
                    DatabaseHelper.class);
            mPhotoItemDao = mDatabaseHelper.getDao(PhotoItem.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        editWindow = new EditPopupWindow(mComment, getActivity(), mPhotoItem,
                mViewHolder.mParent);

        loadDataAsync();
        loadBannerData(); // 加载banner数据
        return parentView;
    }

    private void loadBannerData() {
        HomePageGetBannerRequest.Builder builder = new HomePageGetBannerRequest.Builder()
                .setListener(mBannerListener).setErrorListener(
                        bannerErrorListener);
        HomePageGetBannerRequest request = builder.build();
        request.setTag(TAG);
        RequestQueue requestQueue = PSGodRequestQueue.getInstance(mContext)
                .getRequestQueue();
        requestQueue.add(request);
    }

    private Listener<List<BannerData>> mBannerListener = new Listener<List<BannerData>>() {
        @Override
        public void onResponse(List<BannerData> bannerItems) {
            bannerList.addAll(bannerItems);
            mAdapter.notifyDataSetChanged();
        }
    };

    private ErrorListener bannerErrorListener = new PSGodErrorListener() {
        @Override
        public void handleError(VolleyError error) {
            // TODO Auto-generated method stub
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

    /**
     * TODO 没有被调用
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
                "onDestroy");

        // Step2: 释放内存
        if (mDatabaseHelper != null) {
            OpenHelperManager.releaseHelper();
            mDatabaseHelper = null;
        }

        mPhotoItemDao = null;
        EventBus.getDefault().unregister(this);
    }

    private Comment mComment;
    private PhotoItem mPhotoItem;


    // =============== 需重构 ===================
    public void onEventMainThread(CommentEvent event) {

        if (event != null) {
            mComment = event.comment;
            mPhotoItem = event.photoItem;
            editWindow.setComment(mComment);
            editWindow.setPhotoItem(mPhotoItem);
        }
        editWindow.setOnResponseListener(onResponseListener);
        editWindow.show(-2);
    }

    private OnResponseListener onResponseListener = new OnResponseListener() {

        @Override
        public void onResponse(Long response, EditPopupWindow window) {
            Intent intent = new Intent(getActivity(), CommentListActivity.class);
            intent.putExtra(Constants.IntentKey.PHOTO_ITEM,
                    window.getPhotoItem());
            mContext.startActivity(intent);

        }

        @Override
        public void onErrorResponse(VolleyError error, EditPopupWindow window) {
            Intent intent = new Intent(getActivity(), CommentListActivity.class);
            intent.putExtra(Constants.IntentKey.PHOTO_ITEM,
                    window.getPhotoItem());
            mContext.startActivity(intent);
        }
    };

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
                            .eq("from", PhotoItem.TYPE_HOME_HOT).query();
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
            mHotPhotoItems.clear();
            List<PhotoItem> items = (List<PhotoItem>) msg.obj;
            mHotPhotoItems.addAll(items);
            mAdapter.notifyDataSetChanged();
            if (NetworkUtil.getNetworkType() != NetworkUtil.NetworkType.NONE) {
                mViewHolder.mPhotoListView.setRefreshing(true);
            }
        }
        return true;
    }

    private class PhotoListListener implements OnLastItemVisibleListener,
            OnRefreshListener {
        private Context mContext;
        private static final long DEFAULT_LAST_REFRESH_TIME = -1;

        public PhotoListListener(Context context) {
            mContext = context;
            SharedPreferences sp = mContext.getSharedPreferences(
                    Constants.SharedPreferencesKey.NAME, Context.MODE_PRIVATE);
            mSpKey = Constants.SharedPreferencesKey.HOT_PHOTO_LIST_LAST_REFRESH_TIME;

            mLastUpdatedTime = sp.getLong(mSpKey, DEFAULT_LAST_REFRESH_TIME);
        }

        @Override
        public void onLastItemVisible() {
            if (canLoadMore) {
                mPage += 1;
                mFollowListFooter.setVisibility(View.VISIBLE);
                PhotoListRequest.Builder builder = new PhotoListRequest.Builder()
                        .setPage(mPage).setLastUpdated(mLastUpdatedTime)
                        .setType(PhotoItem.TYPE_HOME_HOT)
                        .setListener(loadMoreListener)
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
            mPage = 1;
            // 上次刷新时间
            if (mLastUpdatedTime == DEFAULT_LAST_REFRESH_TIME) {
                mLastUpdatedTime = System.currentTimeMillis();
            }

            PhotoListRequest.Builder builder = new PhotoListRequest.Builder()
                    .setPage(mPage).setLastUpdated(mLastUpdatedTime)
                    .setType(PhotoItem.TYPE_HOME_HOT)
                    .setListener(refreshListener)
                    .setErrorListener(errorListener);

            PhotoListRequest request = builder.build();
            request.setTag(TAG);
            RequestQueue requestQueue = PSGodRequestQueue.getInstance(mContext)
                    .getRequestQueue();
            requestQueue.add(request);
        }
    }

    private ErrorListener errorListener = new PSGodErrorListener() {
        @Override
        public void handleError(VolleyError error) {
            // TODO Auto-generated method stub
            mViewHolder.mPhotoListView.onRefreshComplete();
        }
    };

    private Listener<List<PhotoItem>> loadMoreListener = new Listener<List<PhotoItem>>() {
        @Override
        public void onResponse(final List<PhotoItem> items) {
            mHotPhotoItems.addAll(items);
            mAdapter.notifyDataSetChanged();
            mFollowListFooter.setVisibility(View.INVISIBLE);

            if (items.size() < 15) {
                canLoadMore = false;
            } else {
                canLoadMore = true;
            }
        }
    };

    private Listener<List<PhotoItem>> refreshListener = new Listener<List<PhotoItem>>() {
        @Override
        public void onResponse(List<PhotoItem> items) {
            mHotPhotoItems.clear();
            mHotPhotoItems.addAll(items);
            mAdapter.notifyDataSetChanged();

            mViewHolder.mPhotoListView.onRefreshComplete();

            // 展开所有分组
            int groupCount = mAdapter.getGroupCount();
            for (int ix = 0; ix < groupCount; ++ix) {
                mViewHolder.mPhotoListView.getRefreshableView().expandGroup(ix);
            }

            PhotoItem.savePhotoList(getActivity(), mPhotoItemDao, items,
                    PhotoItem.TYPE_HOME_HOT);

            if (items.size() < 15) {
                canLoadMore = false;
            } else {
                canLoadMore = true;
            }
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

    /**
     * 保存视图组件，避免视图的重复加载
     *
     */
    private static class ViewHolder {
        ViewGroup mParentView;
        View mView;
        PullToRefreshExpandableListView mPhotoListView;
        RelativeLayout mParent;
    }

}
