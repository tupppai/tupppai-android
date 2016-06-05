package com.pires.wesee.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.pires.wesee.Constants;
import com.pires.wesee.eventbus.RefreshEvent;
import com.pires.wesee.model.notification.NotificationMessage;
import com.pires.wesee.network.request.MessageListRequest;
import com.pires.wesee.network.request.PSGodRequestQueue;
import com.pires.wesee.ui.adapter.MessageListAdapter;
import com.pires.wesee.ui.widget.dialog.CustomProgressingDialog;
import com.pires.wesee.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pires on 16/1/8.
 */
public class MessageCommentActivity extends PSGodBaseActivity {

    private static final String TAG = MessageCommentActivity.class.getSimpleName();
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

    private Context mContext;

    // 控制是否可以加载下一页
    private boolean canLoadMore = true;
    private View mMessageListFooter;

    private int MESSAGE_TYPE = 2;

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_comment);
        mContext = this;

        mMessages = new ArrayList<NotificationMessage>();
        mListView = (PullToRefreshListView) this
                .findViewById(R.id.activity_new_message_comment_list_listview);
        mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

        mMessageListFooter = LayoutInflater.from(MessageCommentActivity.this)
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
                    MessageCommentActivity.this);
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }

        refresh();
    }


    private class MessageListener implements PullToRefreshBase.OnRefreshListener,
            PullToRefreshBase.OnLastItemVisibleListener {
        private Context mContext;

        public MessageListener(Context context) {
            mContext = context;

            SharedPreferences sp = mContext.getSharedPreferences(
                    Constants.SharedPreferencesKey.NAME, Context.MODE_PRIVATE);
            mSpKey = Constants.SharedPreferencesKey.SETTING_COMMEND_LIST_LAST_REFRESH_TIME;
            mLastUpdateTime = sp.getLong(mSpKey, DEFAULT_LAST_REFRESH_TIME);
        }

        @Override
        public void onLastItemVisible() {
            // TODO Auto-generated method stub
            if (canLoadMore) {
                mMessageListFooter.setVisibility(View.VISIBLE);
                mPage += 1;
                MessageListRequest.Builder builder = new MessageListRequest.Builder()
                        .setPage(mPage).setType(MESSAGE_TYPE)
                        .setErrorListener(errorListener)
                        .setListener(loadMoreListener);
                MessageListRequest request = builder.build();
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
        MessageListRequest.Builder builder = new MessageListRequest.Builder()
                .setPage(mPage).setType(MESSAGE_TYPE)
                .setLastUpdated(mLastUpdateTime)
                .setErrorListener(errorListener).setListener(refreshListener);
        MessageListRequest request = builder.build();
        request.setTag(TAG);
        RequestQueue requestQueue = PSGodRequestQueue.getInstance(mContext)
                .getRequestQueue();
        requestQueue.add(request);
    }

    private Response.Listener<List<NotificationMessage>> refreshListener = new Response.Listener<List<NotificationMessage>>() {
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

            mEmptyView = MessageCommentActivity.this.findViewById(R.id.activity_message_comment_list_empty_view);
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

    private Response.Listener<List<NotificationMessage>> loadMoreListener = new Response.Listener<List<NotificationMessage>>() {
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

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(MessageCommentActivity.this, error.getMessage(),
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
