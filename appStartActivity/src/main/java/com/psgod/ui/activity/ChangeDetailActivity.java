package com.psgod.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.psgod.R;
import com.psgod.model.Channel;
import com.psgod.model.Transactions;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.TransactionsRequest;
import com.psgod.ui.adapter.TransactionsAdatper;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pires on 16/1/21.
 */
public class ChangeDetailActivity extends PSGodBaseActivity {
    private static final String TAG = ChangeDetailActivity.class.getSimpleName();
    private PullToRefreshListView mListView;
    private List<Transactions> transactionses = new ArrayList<>();
    private TransactionsAdatper mAdatper;

    private View mFootView;
    private View mEmptyView;

    private int page = 1;
    private boolean canLoadMore = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_change_detail);

        initView();
        initListener();

        refresh();
    }

    private void initView() {
        mListView = (PullToRefreshListView) findViewById(R.id.change_detail_listview);
        mAdatper = new TransactionsAdatper(this, transactionses);
        mListView.setAdapter(mAdatper);
        mFootView = LayoutInflater.from(this).inflate(R.layout.footer_load_more, null);
        mFootView.setVisibility(View.INVISIBLE);
        mListView.getRefreshableView().addFooterView(mFootView);

    }

    private void refresh() {
        page = 1;
        canLoadMore = true;
        TransactionsRequest request = new TransactionsRequest.Builder().
                setPage(page).setErrorListener(errorListener).
                setListener(refreshListener).build();
        RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                this).getRequestQueue();
        requestQueue.add(request);
        requestQueue.add(request);
    }

    private void initListener() {
        mListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                if (canLoadMore) {
                    page++;
                    mFootView.setVisibility(View.VISIBLE);
                    TransactionsRequest request = new TransactionsRequest.Builder().
                            setPage(page).setErrorListener(errorListener).
                            setListener(loadMoreListener).build();
                    RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                            ChangeDetailActivity.this).getRequestQueue();
                    requestQueue.add(request);
                    requestQueue.add(request);
                }
            }
        });

        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                refresh();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });
    }

    Response.Listener<List<Transactions>> refreshListener = new Response.Listener<List<Transactions>>() {
        @Override
        public void onResponse(List<Transactions> response) {
            if (response.size() < 15) {
                canLoadMore = false;
            } else {
                canLoadMore = true;
            }
            if (transactionses != null && transactionses.size() > 0) {
                transactionses.clear();
            }
            mEmptyView = ChangeDetailActivity.this.findViewById(R.id.activity_change_detail_list_empty_view);
            mListView.setEmptyView(mEmptyView);

            transactionses.addAll(response);
            mAdatper.notifyDataSetChanged();
            mListView.onRefreshComplete();
        }
    };

    Response.Listener<List<Transactions>> loadMoreListener = new Response.Listener<List<Transactions>>() {
        @Override
        public void onResponse(List<Transactions> response) {
            if (response.size() < 15) {
                canLoadMore = false;
            } else {
                canLoadMore = true;
            }
            transactionses.addAll(response);
            mAdatper.notifyDataSetChanged();
            mFootView.setVisibility(View.INVISIBLE);
        }
    };

    PSGodErrorListener errorListener = new PSGodErrorListener(this) {
        @Override
        public void handleError(VolleyError error) {
            mListView.onRefreshComplete();
            mFootView.setVisibility(View.INVISIBLE);
        }
    };
}
