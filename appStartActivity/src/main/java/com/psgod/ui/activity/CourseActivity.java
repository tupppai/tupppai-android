package com.psgod.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.ActivitiesAct;
import com.psgod.model.Channel;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.ActivitiesActRequest;
import com.psgod.network.request.ChannelRequest;
import com.psgod.network.request.CourseRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.ui.adapter.ChannelHeadAdapter;
import com.psgod.ui.adapter.ChannelListAdapter;
import com.psgod.ui.adapter.CourseAdapter;
import com.psgod.ui.view.PhotoItemView;
import com.psgod.ui.widget.FloatScrollHelper;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;
import com.psgod.ui.widget.dialog.ImageSelectDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 教程列表
 */

public class CourseActivity extends PSGodBaseActivity {

    private static final String TAG = CourseActivity.class
            .getSimpleName();

    public static final String INTENT_ID = "id";
//    public static final String INTENT_TITLE = "title";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        initView();
        refresh();
        initListener();
    }

    private TextView mTitleName;
    private ImageView mTitleFinish;
    private PullToRefreshListView mList;
    private View mFollowListFooter;

    private List<PhotoItem> photoItems;
    private CourseAdapter mAdapter;

    private String id;
    private long mLastUpdatedTime;
    // 列表的类型
    private String mSpKey;
    private boolean canLoadMore = true;
    private int page = 1;

    private CustomProgressingDialog progressingDialog;

    private RelativeLayout mParent;

    private ActivitiesAct mAct;

    private void initView() {
        progressingDialog = new CustomProgressingDialog(this);
        progressingDialog.show();
        mParent = (RelativeLayout) findViewById(R.id.activity_course_parent);
        mTitleName = (TextView) findViewById(R.id.activity_course_title_name);
        mTitleFinish = (ImageView) findViewById(R.id.activity_course_title_finish);
        mList = (PullToRefreshListView) findViewById(R.id.activity_course_list);
        mList.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        photoItems = new ArrayList<PhotoItem>();
        mAdapter = new CourseAdapter(this, photoItems);
        mList.setAdapter(mAdapter);

        mFollowListFooter = LayoutInflater.from(this).inflate(
                R.layout.footer_load_more, null);
        mList.getRefreshableView().addFooterView(
                mFollowListFooter);
        mFollowListFooter.setVisibility(View.GONE);
        Intent intent = getIntent();
        id = intent.getStringExtra(INTENT_ID);

        mSpKey = Constants.SharedPreferencesKey.COURSE_LIST_LAST_REFRESH_TIME;

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
        CourseRequest request = new CourseRequest.Builder().setListener(refreshListener).
                setErrorListener(errorListener).setPage(page).
                setLastUpdated(mLastUpdatedTime).setId(id).setTargetType("ask").build();
//        ActivitiesActRequest actRequest = new ActivitiesActRequest.Builder().setCategoryId(id).
//                setListener(actListener).setErrorListener(errorListener).build();
        RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                this).getRequestQueue();
        requestQueue.add(request);
//        requestQueue.add(actRequest);

    }

    Response.Listener<List<PhotoItem>> refreshListener = new Response.Listener<List<PhotoItem>>() {
        @Override
        public void onResponse(List<PhotoItem> response) {
            // 保存本次刷新时间到sp
            mLastUpdatedTime = System.currentTimeMillis();
            if (Build.VERSION.SDK_INT >= 9) {
                CourseActivity.this
                        .getSharedPreferences(
                                Constants.SharedPreferencesKey.NAME,
                                Context.MODE_PRIVATE).edit()
                        .putLong(mSpKey, mLastUpdatedTime).apply();
            } else {
                CourseActivity.this.getSharedPreferences(
                        Constants.SharedPreferencesKey.NAME,
                        Context.MODE_PRIVATE).edit()
                        .putLong(mSpKey, mLastUpdatedTime).commit();
            }
            mList.onRefreshComplete();
            if (photoItems.size() > 0) {
                photoItems.clear();
            }
            mAdapter.notifyDataSetChanged();
            photoItems.addAll(response);
            mAdapter.notifyDataSetChanged();
            if (response.size() < 15) {
                canLoadMore = false;
            } else {
                canLoadMore = true;
            }
            if (progressingDialog != null && progressingDialog.isShowing()) {
                progressingDialog.dismiss();
            }
        }
    };

    PSGodErrorListener errorListener = new PSGodErrorListener(this) {
        @Override
        public void handleError(VolleyError error) {
            mList.onRefreshComplete();
            if (progressingDialog != null && progressingDialog.isShowing()) {
                progressingDialog.dismiss();
            }
            mFollowListFooter.setVisibility(View.INVISIBLE);
        }
    };

    Response.Listener<List<PhotoItem>> loadMoreListener = new Response.Listener<List<PhotoItem>>() {
        @Override
        public void onResponse(List<PhotoItem> response) {

            if (response.size() < 15) {
                canLoadMore = false;
            } else {
                canLoadMore = true;
            }
            photoItems.addAll(response);
            mAdapter.notifyDataSetChanged();
            mFollowListFooter.setVisibility(View.INVISIBLE);
        }
    };


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
                    CourseRequest request = new CourseRequest.Builder().setListener(loadMoreListener).
                            setErrorListener(errorListener).setPage(page).setId(id).
                            setLastUpdated(mLastUpdatedTime).setTargetType("reply").build();

                    RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                            CourseActivity.this).getRequestQueue();
                    requestQueue.add(request);
                }
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
