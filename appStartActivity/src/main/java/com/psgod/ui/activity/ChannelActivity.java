package com.psgod.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.psgod.Constants;
import com.psgod.CustomToast;
import com.psgod.R;
import com.psgod.model.Channel;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.ChannelRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.ui.adapter.ChannelHeadAdapter;
import com.psgod.ui.adapter.PhotoListAdapter;
import com.psgod.ui.view.PhotoItemView;
import com.psgod.ui.widget.FloatScrollHelper;
import com.psgod.ui.widget.dialog.CameraPopupwindow;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import java.util.ArrayList;
import java.util.List;

public class ChannelActivity extends PSGodBaseActivity {

    private static final String TAG = ChannelActivity.class
            .getSimpleName();

    public static final String INTENT_ID = "id";
    public static final String INTENT_TITLE = "title";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        initView();
        refresh();
        initListener();
    }

    private TextView mTitleName;
    private ImageView mTitleFinish;
    private PullToRefreshListView mList;
    private View mFollowListFooter;

    private TextView mHeadMore;
    private RecyclerView mHeadList;

    private List<PhotoItem> heads;
    private ChannelHeadAdapter headAdapter;

    private List<PhotoItem> photoItems;
    private PhotoListAdapter mAdapter;

    private String id;
    private long mLastUpdatedTime;
    // 列表的类型
    private String mSpKey;
    private boolean canLoadMore = true;
    private int page = 1;

    private CustomProgressingDialog progressingDialog;

    //    private LinearLayout mEmptyView;
//    private TextView mEmptyTxt;
    private RelativeLayout mParent;
    private ImageView mUpload;

    private void initView() {
        progressingDialog = new CustomProgressingDialog(this);
        progressingDialog.show();
        mParent = (RelativeLayout) findViewById(R.id.activity_channal_parent);
        mTitleName = (TextView) findViewById(R.id.activity_channal_title_name);
        mTitleFinish = (ImageView) findViewById(R.id.activity_channal_title_finish);
        mList = (PullToRefreshListView) findViewById(R.id.activity_channal_list);
        photoItems = new ArrayList<PhotoItem>();
        mAdapter = new PhotoListAdapter(this, PhotoItemView.PhotoListType.RECENT_REPLY, photoItems);
        mList.setAdapter(mAdapter);
        View head = LayoutInflater.from(this).inflate(R.layout.view_activity_channel_head, null);
        mList.getRefreshableView().addHeaderView(head);
        mHeadList = (RecyclerView) head.findViewById(R.id.activity_channal_head_list);
        mHeadMore = (TextView) head.findViewById(R.id.activity_channal_head_more);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mHeadList.setLayoutManager(layoutManager);
        heads = new ArrayList<PhotoItem>();
        headAdapter = new ChannelHeadAdapter(this, heads);
        mHeadList.setAdapter(headAdapter);
        mFollowListFooter = LayoutInflater.from(this).inflate(
                R.layout.footer_load_more, null);
        mList.getRefreshableView().addFooterView(
                mFollowListFooter);
        mFollowListFooter.setVisibility(View.GONE);
//        mEmptyTxt = (TextView) findViewById(R.id.activity_channal_empty_text);
//        mEmptyView = (LinearLayout) findViewById(R.id.activity_channal_empty_view);
        Intent intent = getIntent();
        id = intent.getStringExtra(INTENT_ID);
        mTitleName.setText(intent.getStringExtra(INTENT_TITLE) == null ? ""
                : intent.getStringExtra(INTENT_TITLE));
        mUpload = new ImageView(this);
        mUpload.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mUpload.setImageDrawable(getResources().getDrawable(R.mipmap.floating_btn));
        FloatScrollHelper helper = new FloatScrollHelper(mList, mParent, mUpload, this);
        helper.setViewHeight(80);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            helper.setViewMarginsV19(12);
//        } else {
        helper.setViewMargins(12);
//        }
        helper.init();

        mSpKey = Constants.SharedPreferencesKey.CHANNEL_LIST_LAST_REFRESH_TIME;

        SharedPreferences sp = getSharedPreferences(
                Constants.SharedPreferencesKey.NAME, Context.MODE_PRIVATE);

        mLastUpdatedTime = sp.getLong(mSpKey, -1);
    }

    private void refresh() {
        // 上次刷新时间
        if (mLastUpdatedTime == -1) {
            mLastUpdatedTime = System.currentTimeMillis();
        }
        page = 1;
        ChannelRequest request = new ChannelRequest.Builder().setListener(refreshListener).
                setErrorListener(errorListener).setPage(page).
                setLastUpdated(mLastUpdatedTime).setId(id).setTargetType("reply").build();
        ChannelRequest headRequest = new ChannelRequest.Builder().setListener(refreshHeadListener).
                setId(id).setTargetType("ask").
                setLastUpdated(mLastUpdatedTime).build();
        RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                this).getRequestQueue();
        requestQueue.add(request);
        requestQueue.add(headRequest);

    }

    Response.Listener<Channel> refreshHeadListener = new Response.Listener<Channel>() {
        @Override
        public void onResponse(Channel response) {
            // 保存本次刷新时间到sp
            if(heads.size() > 0){
                heads.clear();
            }
            heads.addAll(response.getData());
            headAdapter.notifyDataSetChanged();
        }
    };

    Response.Listener<Channel> refreshListener = new Response.Listener<Channel>() {
        @Override
        public void onResponse(Channel response) {
            // 保存本次刷新时间到sp
            mLastUpdatedTime = System.currentTimeMillis();
            if (android.os.Build.VERSION.SDK_INT >= 9) {
                ChannelActivity.this
                        .getSharedPreferences(
                                Constants.SharedPreferencesKey.NAME,
                                Context.MODE_PRIVATE).edit()
                        .putLong(mSpKey, mLastUpdatedTime).apply();
            } else {
                ChannelActivity.this.getSharedPreferences(
                        Constants.SharedPreferencesKey.NAME,
                        Context.MODE_PRIVATE).edit()
                        .putLong(mSpKey, mLastUpdatedTime).commit();
            }
            mList.onRefreshComplete();
            if (photoItems.size() > 0) {
                photoItems.clear();
            }
            mAdapter.notifyDataSetChanged();
            photoItems.addAll(response.getData());
            mAdapter.notifyDataSetChanged();
            if (response.getData().size() < 15) {
                canLoadMore = false;
            } else {
                canLoadMore = true;
            }
//            initEmpty(photoItems.size());
            if (progressingDialog != null && progressingDialog.isShowing()) {
                progressingDialog.dismiss();
            }
        }
    };

    PSGodErrorListener errorListener = new PSGodErrorListener() {
        @Override
        public void handleError(VolleyError error) {
            mList.onRefreshComplete();
            if (progressingDialog != null && progressingDialog.isShowing()) {
                progressingDialog.dismiss();
            }
            mFollowListFooter.setVisibility(View.INVISIBLE);
        }
    };

    Response.Listener<Channel> loadMoreListener = new Response.Listener<Channel>() {
        @Override
        public void onResponse(Channel response) {

            if (response.getData().size() < 15) {
                canLoadMore = false;
            } else {
                canLoadMore = true;
            }
            photoItems.addAll(response.getData());
            mAdapter.notifyDataSetChanged();
            mFollowListFooter.setVisibility(View.INVISIBLE);
//            initEmpty(photoItems.size());
        }
    };

//    private void initEmpty(int length) {
//        if (length == 0) {
//            mEmptyView.setVisibility(View.VISIBLE);
//            mEmptyTxt.setText("敬请期待");
//        } else {
//            mEmptyView.setVisibility(View.GONE);
//        }
//    }


    private void initListener() {
        mList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                refresh();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });

        mList.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                if (canLoadMore) {
                    mFollowListFooter.setVisibility(View.VISIBLE);
                    page++;
                    ChannelRequest request = new ChannelRequest.Builder().setListener(loadMoreListener).
                            setErrorListener(errorListener).setPage(page).setId(id).
                            setLastUpdated(mLastUpdatedTime).setTargetType("reply").build();

                    RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                            ChannelActivity.this).getRequestQueue();
                    requestQueue.add(request);
                }
            }
        });

        mHeadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChannelActivity.this, RecentAsksActivity.class);
                intent.putExtra("channel_id", id);
                startActivity(intent);
            }
        });

        mUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraPopupwindow popupwindow = new CameraPopupwindow(ChannelActivity.this, id);
                popupwindow.showCameraPopupwindow(mParent);
//                Intent intent = new Intent(ChannelActivity.this,
//                        UploadSelectReplyListActivity.class);
//                intent.putExtra("channel_id" , id);
//                startActivity(intent);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        RequestQueue requestQueue = PSGodRequestQueue.getInstance(this)
                .getRequestQueue();
        requestQueue.cancelAll(TAG);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra("isRefresh", false)) {
            refresh();
        }
    }

}
