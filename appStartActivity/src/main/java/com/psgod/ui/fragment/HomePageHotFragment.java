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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.etsy.android.grid.StaggeredGridView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.psgod.Constants;
import com.psgod.Logger;
import com.psgod.PsGodImageLoader;
import com.psgod.R;
import com.psgod.ThreadManager;
import com.psgod.WeakReferenceHandler;
import com.psgod.db.DatabaseHelper;
import com.psgod.eventbus.RefreshEvent;
import com.psgod.model.BannerData;
import com.psgod.model.PhotoItem;
import com.psgod.model.Tupppai;
import com.psgod.network.NetworkUtil;
import com.psgod.network.request.BaseRequest;
import com.psgod.network.request.HomePageGetBannerRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.PhotoListRequest;
import com.psgod.network.request.TupppaiRequest;
import com.psgod.ui.activity.ChannelActivity;
import com.psgod.ui.activity.MainActivity;
import com.psgod.ui.activity.RecentActActivity;
import com.psgod.ui.activity.TupppaiActivity;
import com.psgod.ui.activity.WebBrowserActivity;
import com.psgod.ui.adapter.PhotoListAdapter;
import com.psgod.ui.adapter.PhotoWaterFallListAdapter;
import com.psgod.ui.adapter.UserProfileAsksAdapter;
import com.psgod.ui.view.PhotoItemView;
import com.psgod.ui.view.PhotoWaterFallItemView;
import com.psgod.ui.view.PullToRefreshStaggeredGridView;
import com.psgod.ui.widget.EditPopupWindow;
import com.psgod.ui.widget.EditPopupWindow.OnResponseListener;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.trinea.android.view.autoscrollviewpager.AutoScrollViewPager;
import de.greenrobot.event.EventBus;
import m.framework.utils.Utils;

public class HomePageHotFragment extends BaseFragment implements Callback {
    private static final String TAG = HomePageHotFragment.class.getSimpleName();
    private static final long DEFAULT_LAST_REFRESH_TIME = -1;

    private int page = 1;
    private List<Tupppai> mtupppais = new ArrayList<Tupppai>();
    private LinearLayout channelPanel;
    private FragmentManager mFragmentManager;

    private Context mContext;
    private ViewHolder mViewHolder;
    private List<PhotoItem> mHotPhotoItems;
    private DatabaseHelper mDatabaseHelper = null;
    private Dao<PhotoItem, Long> mPhotoItemDao;
    private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);
    private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);

    // 带评论
    //private PhotoListAdapter mAdapter;
    private PhotoListListener mListener;
    private View mFollowListFooter;
    private int mPage = 1;

    // 上次刷新时间
    private long mLastUpdatedTime;
    // 列表的类型
    private String mSpKey;

    // 控制是否可以加载下一页
    private boolean canLoadMore = true;

    //  显示的banner
    private List<BannerData> mBannerItems = new ArrayList<BannerData>();
    private View headerView;
    private View channelView;
    private AutoScrollViewPager mBannerViewPager;
    private BannerOnPageChangeListener bannerListener = new BannerOnPageChangeListener();
    private List<ImageView> mScrollImages = new ArrayList<ImageView> ();

    private LinearLayout mScrollLayout;

    private DisplayImageOptions mOptions = Constants.DISPLAY_BANNER_OPTIONS;

    private Boolean HasAddBanner = false;
    private Boolean HasAddChannel = false;
    private TextView mChannel;
    private PhotoWaterFallListAdapter mAdapter;

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
        //中间下拉刷新界面
        mViewHolder.mView = LayoutInflater.from(mContext).inflate(
                R.layout.fragment_homepage_hot, parentView, true);
        mViewHolder.mPhotoListView = (PullToRefreshStaggeredGridView) mViewHolder.mView
                .findViewById(R.id.fragment_homepage_hot_lv);
        mViewHolder.mPhotoListView.setMode(PullToRefreshBase.Mode.BOTH);

        mFollowListFooter = LayoutInflater.from(mContext).inflate(
                R.layout.footer_load_more, null);
        //添加底部上拉刷新界面
        mViewHolder.mPhotoListView.getRefreshableView().addFooterView(
                mFollowListFooter);
        mFollowListFooter.setVisibility(View.GONE);

        //照片列表适配
        mHotPhotoItems = new ArrayList<PhotoItem>();
//        mAdapter = new PhotoListAdapter(mContext,
//                PhotoItemView.PhotoListType.HOT_FOCUS_ASK, mHotPhotoItems);
        mAdapter = new PhotoWaterFallListAdapter(mContext, mHotPhotoItems,
                PhotoWaterFallItemView.PhotoWaterFallListType.USER_PROFILE_WORKS);
        //去掉时间
        //mAdapter.setIsHomePageHot(true);
        mViewHolder.mPhotoListView.getRefreshableView().setAdapter(mAdapter);

        //照片列表监听器
        mListener = new PhotoListListener(mContext);
        mViewHolder.mPhotoListView.setOnRefreshListener(mListener);
        mViewHolder.mPhotoListView.setOnLastItemVisibleListener(mListener);

        mViewHolder.mParent = (RelativeLayout) mViewHolder.mView
                .findViewById(R.id.fragment_homepage_hot_parent);

//        if (NetworkUtil.getNetworkType() != NetworkUtil.NetworkType.NONE) {
//            mViewHolder.mPhotoListView.setRefreshing(true);
//        }

        // 加载首页数据
        refresh();

        // 在刷新时允许继续滑动
        mViewHolder.mPhotoListView.setScrollingWhileRefreshingEnabled(true);

        // TODO 检测耗时
        try {
            mDatabaseHelper = OpenHelperManager.getHelper(mContext,
                    DatabaseHelper.class);
            mPhotoItemDao = mDatabaseHelper.getDao(PhotoItem.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        loadDataAsync();

        return parentView;
    }

    public View getHeaderView () {
        headerView = LayoutInflater.from(mContext).inflate(
                R.layout.homepage_hot_banner_view, null);
        channelPanel = (LinearLayout) headerView.findViewById(R.id.chanel_item);
        mChannel = (TextView) headerView.findViewById(R.id.foryou);
        mScrollLayout = (LinearLayout) headerView.findViewById(R.id.scroll_layout);

        mBannerViewPager = (AutoScrollViewPager) headerView
                .findViewById(R.id.hot_banner_viewpager);
        LinearLayout.LayoutParams scrollLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);



        mChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setClass(mContext, TupppaiActivity.class);
//                mContext.startActivity(intent);
                getActivity().findViewById(R.id.psgod_linear_tab).setVisibility(View.GONE);
                getActivity().findViewById(R.id.psgod_rg_tab_tips).setVisibility(View.GONE);
                getActivity().findViewById(R.id.middle).setVisibility(View.GONE);

                if (getActivity() instanceof MainActivity) {
                    MainActivity fca = (MainActivity) getActivity();
                    fca.showFragment(R.id.middle);
                }


            }
        });

        for (int i = 0,length = mBannerItems.size(); i < length ; i++) {
            ImageView scrollImage = new ImageView(mContext);
            scrollImage.setScaleType(ImageView.ScaleType.FIT_XY);
            if (i == 0) {
                scrollImage.setBackgroundResource(R.drawable.shape_scroll_banner_select);
                scrollLayoutParams.setMargins(0,0,0,0);
            } else {
                scrollImage.setBackgroundResource(R.drawable.shape_scroll_banner_unselect);
                scrollLayoutParams.setMargins(Utils.dipToPx(mContext, 5),0,0,0);
            }
            scrollImage.setLayoutParams(scrollLayoutParams);

            mScrollLayout.addView(scrollImage);
            mScrollImages.add(scrollImage);

        }

        if (mBannerViewPager != null) {
            mBannerViewPager.setOnPageChangeListener(null);
        }

        initAdapter();
        mBannerViewPager.setOnPageChangeListener(bannerListener);
        mBannerViewPager.setInterval(3000);  // 设置自动滚动的间隔时间，单位为毫秒
        mBannerViewPager.startAutoScroll();  // 启动自动滚动



        PsGodImageLoader imageLoader = PsGodImageLoader.getInstance();

        channelPanel.removeAllViews();
        int mSize = mtupppais.size();

        System.out.println("mtupppais.size " + mtupppais.size());
        if (mSize > 0) {
            for (int i = 0; i < mSize; i++) {
                ImageView mchannelIv = new ImageView(mContext);
                mchannelIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        com.psgod.Utils.dpToPx(mContext, 84), com.psgod.Utils.dpToPx(mContext, 84));
                params.setMargins(com.psgod.Utils.dpToPx(mContext, 10), 0, 0, 0);
                mchannelIv.setLayoutParams(params);
                mchannelIv.setScaleType(ImageView.ScaleType.CENTER_CROP);

                imageLoader.displayImage(mtupppais.get(i).getUrl(),
                        mchannelIv, mOptions);
                imageLoader.displayImage(mtupppais.get(i).getApp_pic(),mchannelIv,mOptions);
                channelPanel.addView(mchannelIv);

                final Intent intent = new Intent();
                intent.putExtra("id", mtupppais.get(i).getId());
                mchannelIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        intent.setClass(mContext, ChannelActivity.class);
                        mContext.startActivity(intent);
                    }
                });

            }
        }
        return headerView;
    }

    // banner下方的小圆点
    public class BannerOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageSelected(int page) {
            for (int i = 0 ,length = mBannerItems.size(); i < length; i++) {
                if (page == i) {
                    mScrollImages.get(i).setBackgroundResource(R.drawable.shape_scroll_banner_select);
                } else {
                    mScrollImages.get(i).setBackgroundResource(R.drawable.shape_scroll_banner_unselect);
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // TODO Auto-generated method stub

        }
    }

    private void initAdapter() {
        // viewpager滚动页面
        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                Utils.dipToPx(mContext, 166));

        PsGodImageLoader imageLoader = PsGodImageLoader.getInstance();
        final ArrayList<View> bannerListViews = new ArrayList<View>();
        for (int i = 0 ,length = mBannerItems.size(); i < length; i++) {

            final BannerData bannerData = mBannerItems.get(i);
            ImageView bannerImage = new ImageView(mContext);
            bannerImage.setLayoutParams(imageParams);
            bannerImage.setScaleType(ImageView.ScaleType.FIT_XY);
            imageLoader.displayImage(bannerData.getSmall_pic(), bannerImage, mOptions);
            bannerListViews.add(bannerImage);

            bannerImage.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    com.psgod.Utils.skipByUrl(getActivity(),bannerData.getUrl(),bannerData.getDesc());
                }
            });

        }

        PagerAdapter mPagerAdapter = new PagerAdapter() {

            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            @Override
            public int getCount() {
                return bannerListViews.size();
            }

            @Override
            public void destroyItem(View container, int position, Object object) {
                ((AutoScrollViewPager) container).removeView(bannerListViews
                        .get(position));
            }

            @Override
            public Object instantiateItem(View container, int position) {
                ((AutoScrollViewPager) container).addView(bannerListViews
                        .get(position));
                return bannerListViews.get(position);
            }
        };

        mBannerViewPager.setAdapter(mPagerAdapter);
    }

    // 更新上方滚动banner
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
            mBannerItems.clear();
            mBannerItems.addAll(bannerItems);
            if ((mtupppais.size() > 0) && (mBannerItems.size() > 0) && HasAddBanner == false && HasAddChannel == false ) {
                mViewHolder.mPhotoListView.getRefreshableView().addHeaderView(getHeaderView());
                HasAddBanner = true;
            }
        }
    };

    private ErrorListener bannerErrorListener = new PSGodErrorListener(this) {
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

    // 照片列表监听
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
                refresh();  //强制刷新
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
            refresh();
        }
    }

    // 刷新方法
    private void refresh() {

        loadChannelData();
        loadBannerData();         // 下拉刷新时，加载banner

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


    private void loadChannelData() {
        page = 1;
        TupppaiRequest.Builder builder = new TupppaiRequest.Builder()
                .setListener(channelListener).setErrorListener(errorListener);
        TupppaiRequest request = builder.build();

        RequestQueue requestQueue = PSGodRequestQueue.getInstance(mContext).getRequestQueue();
        requestQueue.add(request);
    }

    private Listener<List<Tupppai>> channelListener = new Response.Listener<List<Tupppai>>() {
        @Override
        public void onResponse(List<Tupppai> response) {

            if(mtupppais.size() > 0){
                mtupppais.clear();
            }
            if(response.size() < 10){
                canLoadMore = false;
            }else{
                canLoadMore = true;
            }
            mtupppais.addAll(response);
            System.out.println("tupppais " + mtupppais.size());

            if ((mtupppais.size() > 0) && (mBannerItems.size() > 0) && HasAddBanner == false && HasAddChannel == false ) {
                mViewHolder.mPhotoListView.getRefreshableView().addHeaderView(getHeaderView());
                HasAddChannel = true;
            }
        }
    };




    private ErrorListener errorListener = new PSGodErrorListener(this) {
        @Override
        public void handleError(VolleyError error) {
            // TODO Auto-generated method stub
            mViewHolder.mPhotoListView.onRefreshComplete();
        }
    };

    // 上拉加载监听
    private Listener<List<PhotoItem>> loadMoreListener = new Listener<List<PhotoItem>>() {
        @Override
        public void onResponse(final List<PhotoItem> items) {
            mHotPhotoItems.addAll(items);
            mAdapter.notifyDataSetChanged();
            mFollowListFooter.setVisibility(View.INVISIBLE);

            if (items.size() < 15) {
                canLoadMore = true;
            } else {
                canLoadMore = true;
            }
        }
    };

    // 下拉刷新监听
    private Listener<List<PhotoItem>> refreshListener = new Listener<List<PhotoItem>>() {
        @Override
        public void onResponse(List<PhotoItem> items) {
            mHotPhotoItems.clear();
            mHotPhotoItems.addAll(items);
            mAdapter.notifyDataSetChanged();

            mViewHolder.mPhotoListView.onRefreshComplete();

//            PhotoItem.savePhotoList(getActivity(), mPhotoItemDao, items,
//                    PhotoItem.TYPE_HOME_HOT);

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
        PullToRefreshStaggeredGridView mPhotoListView;
        RelativeLayout mParent;
    }

}
