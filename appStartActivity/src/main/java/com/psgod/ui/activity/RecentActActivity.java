package com.psgod.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.psgod.PsGodImageLoader;
import com.psgod.Constants;
import com.psgod.LoadUtils;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.eventbus.RefreshEvent;
import com.psgod.model.Activities;
import com.psgod.model.ActivitiesAct;
import com.psgod.model.Channel;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.ActivitiesActRequest;
import com.psgod.network.request.ChannelRequest;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.PhotoActRequest;
import com.psgod.ui.adapter.RecentPageActAdapter;
import com.psgod.ui.widget.FloatScrollHelper;
import com.psgod.ui.widget.ImageCategoryWindow;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class RecentActActivity extends PSGodBaseActivity {
    private static final String TAG = RecentActActivity.class
            .getSimpleName();

    public static final String INTENT_ID = "id";
    public static final String OBJ_ACTIVITY = "Activity";

    private PullToRefreshListView mListView;
    private View mHeadView;
    private ImageView mHeadImg;
    private RelativeLayout mHeadTxtArea;
    private RecentPageActAdapter mAdapter;
    private List<PhotoItem> mPhotoItems;
    private ActivitiesAct mAct = new ActivitiesAct();
    private View mFollowListFooter;
    private TextView mTitle;
    private ImageView mFinish;
    private RelativeLayout mParent;
    private ImageView mUpLoad;

    private int mPage = 1;
    private boolean canLoadMore = false;

    // 上次刷新时间
    private long mLastUpdatedTime;
    // 列表的类型
    private String mSpKey;
    private LoadUtils loadUtils;
    private View mEmptyView;

    private String id;

    private CustomProgressingDialog progressingDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_recent_act);

        Intent intent = getIntent();
        id = intent.getStringExtra(INTENT_ID);
        initView();
        initEvent();
        initData();
    }

    private void initAct(ActivitiesAct mAct) {
        if (mAct != null) {
            if (mAct.getPost_btn() != null && !mAct.getPost_btn().equals("")) {
                PsGodImageLoader.getInstance().displayImage(mAct.getPost_btn(),
                        mUpLoad, Constants.DISPLAY_IMAGE_OPTIONS_SMALL);
            }
            if (mHeadView.getVisibility() == View.GONE) {
                mHeadView.setVisibility(View.VISIBLE);
            }
            mTitle.setText(mAct.getName());
            PsGodImageLoader.getInstance().
                    displayImage(mAct.getBanner_pic(),
                            mHeadImg, Constants.DISPLAY_IMAGE_OPTIONS);

        } else {
            mHeadView.setVisibility(View.GONE);
            mListView.setEmptyView(mEmptyView);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initView() {
        mParent = (RelativeLayout) findViewById(R.id.activity_act_parent);
        mListView = (PullToRefreshListView) findViewById(R.id.fragment_recentpage_act_list);
        mTitle = (TextView) findViewById(R.id.activity_act_title_name);
        mHeadView = LayoutInflater.from(this).inflate(R.layout.header_recent_page_act, null);
        mHeadImg = (ImageView) mHeadView.findViewById(R.id.header_recentpage_act_img);
        mHeadTxtArea = (RelativeLayout) mHeadView.findViewById(R.id.header_recentpage_act_area);
        mHeadTxtArea.setVisibility(View.GONE);
        mEmptyView = findViewById(R.id.recent_fragment_act_empty_view);
        mPhotoItems = new ArrayList<PhotoItem>();
        mAdapter = new RecentPageActAdapter(RecentActActivity.this, mPhotoItems);
        mListView.setAdapter(mAdapter);
        mFollowListFooter = LayoutInflater.from(RecentActActivity.this).inflate(
                R.layout.footer_load_more, null);
        mFollowListFooter.setVisibility(View.INVISIBLE);

        mListView.getRefreshableView().addHeaderView(mHeadView);
        mListView.getRefreshableView().addFooterView(mFollowListFooter);

        listListener = new PhotoListListener(RecentActActivity.this);

        loadUtils = new LoadUtils(RecentActActivity.this);
        progressingDialog = new CustomProgressingDialog(this);
        progressingDialog.show();

        mUpLoad = new ImageView(this);
        mUpLoad.setImageDrawable(getResources().getDrawable(R.mipmap.btn_act_reply));
        mUpLoad.setScaleType(ImageView.ScaleType.FIT_XY);
        FloatScrollHelper helper = new FloatScrollHelper(mListView, mParent, mUpLoad, this);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            helper.setViewMarginsV19(17);
//        } else {
        helper.setViewMargins(17);
//        }
        helper.setViewHeight(48);
        helper.setViewParams((int) (120 * Utils.getWidthScale(this)),
                (int) (32 * Utils.getWidthScale(this)));
        helper.init();
    }

    private class PhotoListListener implements PullToRefreshBase.OnLastItemVisibleListener,
            PullToRefreshBase.OnRefreshListener<ListView> {
        private Context mContext;
        private static final long DEFAULT_LAST_REFRESH_TIME = -1;

        public PhotoListListener(Context context) {
            mContext = context;
            SharedPreferences sp = mContext.getSharedPreferences(
                    Constants.SharedPreferencesKey.NAME, Context.MODE_PRIVATE);
            mSpKey = Constants.SharedPreferencesKey.RECENT_PHOTO_LIST_LAST_REFRESH_TIME;


            mLastUpdatedTime = sp.getLong(mSpKey, DEFAULT_LAST_REFRESH_TIME);
        }

        @Override
        public void onLastItemVisible() {
            if (canLoadMore) {
                mPage += 1;
                mFollowListFooter.setVisibility(View.VISIBLE);

                ChannelRequest.Builder builder = new ChannelRequest.Builder()
                        .setPage(mPage).setId(id).setTargetType("reply").
                                setLastUpdated(mLastUpdatedTime)
                        .setListener(loadMoreListener)
                        .setErrorListener(errorListener);

                ChannelRequest request = builder.build();
                request.setTag(TAG);
                RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                        mContext).getRequestQueue();
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

            ChannelRequest.Builder builder = new ChannelRequest.Builder()
                    .setPage(mPage).setId(id).setTargetType("reply").
                            setLastUpdated(mLastUpdatedTime)
                    .setListener(refreshListener)
                    .setErrorListener(errorListener);
            ActivitiesActRequest actRequest = new ActivitiesActRequest.Builder().
                    setCategoryId(id).setListener(actListener).build();

            ChannelRequest request = builder.build();
            request.setTag(TAG);
            RequestQueue requestQueue = PSGodRequestQueue.getInstance(mContext)
                    .getRequestQueue();
            requestQueue.add(request);
            requestQueue.add(actRequest);
        }
    }

    private PhotoListListener listListener;

    private void initEvent() {
        mListView.setOnRefreshListener(listListener);
        mListView.setOnLastItemVisibleListener(listListener);
        mHeadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.skipByUrl(RecentActActivity.this, mAct.getUrl(), mAct.getName());
            }
        });
        mUpLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAct != null) {
//                    loadUtils.upLoad(mActs.get(0).getType(), Long.parseLong(mActs.get(0).getAsk_id()));
//                    Intent intent = new Intent(RecentActActivity.this, MultiImageSelectActivity.class);
//                    intent.putExtra("AskId", Long.parseLong(mAct.getAsk_id()));
//                    intent.putExtra("ActivityId", mAct.getId());
//                    intent.putExtra("SelectType", "TypeReplySelect");
//                    new LoadUtils(RecentActActivity.this).isSimple(true).
//                            setCategory_id(Long.parseLong(mAct.getId())).upLoad(1,
//                            Long.parseLong(mAct.getAsk_id().equals("") ? "0" : mAct.getAsk_id()));
//
//                    startActivity(intent);
                    ImageCategoryWindow imageCategoryWindow = new ImageCategoryWindow(RecentActActivity.this,
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                    imageCategoryWindow.showAtLocation(mParent, Gravity.BOTTOM,0,0);
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
        RequestQueue requestQueue = PSGodRequestQueue.getInstance(RecentActActivity.this)
                .getRequestQueue();
        requestQueue.cancelAll(TAG);
    }

    private void initData() {
        mListView.setRefreshing(true);
    }

    private Response.Listener<ActivitiesAct> actListener = new Response.Listener<ActivitiesAct>() {
        @Override
        public void onResponse(ActivitiesAct act) {
            if (act != null) {
                mAct = act;
                initAct(act);
            }
        }
    };

    private Response.Listener<Channel> refreshListener = new Response.Listener<Channel>() {
        @Override
        public void onResponse(Channel response) {
            mPhotoItems.clear();
            if (response.getData().size() > 0) {
                mPhotoItems.addAll(response.getData());
            }
            mAdapter.notifyDataSetChanged();
            mListView.onRefreshComplete();

            if (response.getData().size() < 15) {
                canLoadMore = false;
            } else {
                canLoadMore = true;
            }
            // 保存本次刷新时间到sp
            mLastUpdatedTime = System.currentTimeMillis();
            if (android.os.Build.VERSION.SDK_INT >= 9) {
                RecentActActivity.this
                        .getSharedPreferences(
                                Constants.SharedPreferencesKey.NAME,
                                Context.MODE_PRIVATE).edit()
                        .putLong(mSpKey, mLastUpdatedTime).apply();
            } else {
                RecentActActivity.this
                        .getSharedPreferences(
                                Constants.SharedPreferencesKey.NAME,
                                Context.MODE_PRIVATE).edit()
                        .putLong(mSpKey, mLastUpdatedTime).commit();
            }
            if (progressingDialog.isShowing()) {
                progressingDialog.dismiss();
            }
        }
    };


    private Response.Listener<Channel> loadMoreListener = new Response.Listener<Channel>() {
        @Override
        public void onResponse(final Channel response) {
            mPhotoItems.addAll(response.getData());
            mAdapter.notifyDataSetChanged();
            mListView.onRefreshComplete();

            mFollowListFooter.setVisibility(View.INVISIBLE);

            if (response.getData().size() < 15) {
                canLoadMore = false;
            } else {
                canLoadMore = true;
            }

            if (progressingDialog.isShowing()) {
                progressingDialog.dismiss();
            }
        }
    };


    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            mListView.onRefreshComplete();
            mFollowListFooter.setVisibility(View.INVISIBLE);

            if (progressingDialog.isShowing()) {
                progressingDialog.dismiss();
            }
        }
    };

    public void onEventMainThread(RefreshEvent event) {
        if (event.className.equals(this.getClass().getName())) {
            try {
                mListView.setRefreshing(true);
            } catch (NullPointerException ne) {
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getBooleanExtra("isRefresh", false)) {
            listListener.onRefresh(mListView);
        }
    }

}
