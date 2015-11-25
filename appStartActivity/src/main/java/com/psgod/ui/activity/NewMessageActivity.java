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
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.UserPreferences;
import com.psgod.eventbus.RefreshEvent;
import com.psgod.model.notification.NotificationMessage;
import com.psgod.network.NetworkUtil;
import com.psgod.network.request.MyMessageListRequest;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.ui.adapter.MessageListAdapter;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import de.greenrobot.event.EventBus;

public class NewMessageActivity extends PSGodBaseActivity {
    private static final String TAG = NewMessageActivity.class.getSimpleName();

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

    // 控制是否可以加载下一页
    private boolean canLoadMore = true;
    private View mMessageListFooter;

    private RelativeLayout mSystemMessageLayout;
    private RelativeLayout mLikeMessageLayout;
    private View mSystemTipView;
    private View mLikeTipView;

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);
        EventBus.getDefault().register(this);

        mMessages = new ArrayList<NotificationMessage>();
        mSystemMessageLayout = (RelativeLayout) this.findViewById(R.id.system_message_layout);
        mLikeMessageLayout = (RelativeLayout) this.findViewById(R.id.like_message_layout);
        mSystemTipView = this.findViewById(R.id.activity_system_message_tip);
        mLikeTipView = this.findViewById(R.id.activity_like_message_tip);
        updateTipView();

        mListView = (PullToRefreshListView) this
                .findViewById(R.id.activity_new_message_list_listview);
        mListView.setMode(Mode.PULL_FROM_START);

        mMessageListFooter = LayoutInflater.from(NewMessageActivity.this)
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
                    NewMessageActivity.this);
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }

        if (NetworkUtil.getNetworkType() != NetworkUtil.NetworkType.NONE) {
            mListView.setRefreshing(true);
        }

        initListener();
    }

    // 更新小红点
    private void updateTipView() {
        int systemMessage = UserPreferences.PushMessage
                .getPushMessageCount(UserPreferences.PushMessage.PUSH_SYSTEM);
        int likeMessage = UserPreferences.PushMessage
                .getPushMessageCount(UserPreferences.PushMessage.PUSH_LIKE);
        if (systemMessage > 0) {
            mSystemTipView.setVisibility(View.VISIBLE);
        } else {
            mSystemTipView.setVisibility(View.INVISIBLE);
        }

        if (likeMessage > 0) {
            mLikeTipView.setVisibility(View.VISIBLE);
        } else {
            mLikeTipView.setVisibility(View.INVISIBLE);
        }
    }

    private void initListener() {
        mSystemMessageLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mSystemTipView.setVisibility(View.INVISIBLE);
                UserPreferences.PushMessage.setPushMessageCount(
                        UserPreferences.PushMessage.PUSH_SYSTEM, 0);
                Intent intent = new Intent(NewMessageActivity.this, MessageSystemActivity.class);
                startActivity(intent);
            }
        });

        mLikeMessageLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mLikeTipView.setVisibility(View.INVISIBLE);
                UserPreferences.PushMessage.setPushMessageCount(
                        UserPreferences.PushMessage.PUSH_LIKE, 0);
                Intent intent = new Intent(NewMessageActivity.this, MessageLikeActivity.class);
                startActivity(intent);
            }
        });
    }

    private class MessageListener implements OnRefreshListener,
            OnLastItemVisibleListener {
        private Context mContext;

        public MessageListener(Context context) {
            mContext = context;

            SharedPreferences sp = mContext.getSharedPreferences(
                    Constants.SharedPreferencesKey.NAME, Context.MODE_PRIVATE);
            mSpKey = Constants.SharedPreferencesKey.MY_MESSAGE_LIST_LAST_REFRESH_TIME;
            mLastUpdateTime = sp.getLong(mSpKey, DEFAULT_LAST_REFRESH_TIME);
        }

        @Override
        public void onLastItemVisible() {
            // TODO Auto-generated method stub
            if (canLoadMore) {
                mMessageListFooter.setVisibility(View.VISIBLE);
                mPage += 1;
                MyMessageListRequest.Builder builder = new MyMessageListRequest.Builder()
                        .setPage(mPage).setListener(loadMoreListener)
                        .setErrorListener(errorListener);
                MyMessageListRequest request = builder.build();
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

    public void onEventMainThread(RefreshEvent event) {
        Log.e("hehe","hehe");
        if(event.className.equals(this.getClass().getName())){
            try {
                refresh();
            } catch (NullPointerException nu) {
            } catch (Exception e) {
            }
        }
    }

    // 刷新操作
    private void refresh() {
        canLoadMore = false;

        if (mLastUpdateTime == DEFAULT_LAST_REFRESH_TIME) {
            mLastUpdateTime = System.currentTimeMillis();
        }

        mPage = 1;
        MyMessageListRequest.Builder builder = new MyMessageListRequest.Builder()
                .setPage(mPage).setLastUpdated(mLastUpdateTime)
                .setListener(refreshListener).setErrorListener(errorListener);

        MyMessageListRequest request = builder.build();
        request.setTag(TAG);
        RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                NewMessageActivity.this).getRequestQueue();
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

            mEmptyView = NewMessageActivity.this
                    .findViewById(R.id.activity_new_message_list_empty_view);
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
            Toast.makeText(NewMessageActivity.this, error.getMessage(),
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
