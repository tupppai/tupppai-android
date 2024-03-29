package com.psgod.ui.fragment;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.psgod.R;
import com.psgod.eventbus.RefreshEvent;
import com.psgod.model.Tupppai;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.TupppaiRequest;
import com.psgod.ui.activity.RecentAsksActivity;
import com.psgod.ui.activity.RecentWorkActivity;
import com.psgod.ui.adapter.TupppaiAdapter;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * A simple {@link Fragment} subclass.
 */
public class TupppaiFragment extends BaseFragment {


    public TupppaiFragment() {
    }

    private PullToRefreshListView mListView;
    private TupppaiAdapter mAdapter;
    private List<Tupppai> tupppais;
    private ImageView askImg;
    private ImageView workImg;
    private int page = 1;

    private CustomProgressingDialog progressingDialog;

    private Boolean canLoadMore = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(RefreshEvent event) {
        if(event.className.equals(this.getClass().getName())){
            try {
                mListView.setRefreshing(true);
            }catch (NullPointerException ne){}
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tupppai, container, false);

        initView(view);
        initListener();
        refresh();

        return view;
    }

    private void refresh() {
        page = 1;
        TupppaiRequest request = new TupppaiRequest.Builder().setListener(refreshListener).
                setErrorListener(errorListener).setPage(page).build();

        RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                getActivity()).getRequestQueue();
        requestQueue.add(request);
    }

    private void initView(View view) {
        progressingDialog = new CustomProgressingDialog(getActivity());
        progressingDialog.show();
        View head = LayoutInflater.from(getActivity()).inflate(R.layout.view_tupppai_head, null);
        askImg = (ImageView) head.findViewById(R.id.view_tupppai_head_ask);
        workImg = (ImageView) head.findViewById(R.id.view_tupppai_head_work);
        mListView = (PullToRefreshListView) view.findViewById(R.id.fragment_tupppai_list);
        mListView.getRefreshableView().addHeaderView(head);
        tupppais = new ArrayList<Tupppai>();
        mAdapter = new TupppaiAdapter(getActivity(),tupppais);
        mListView.setAdapter(mAdapter);
        mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
    }

    private void initListener() {
        askImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MobclickAgent.onEvent(getActivity(), "Tupppai_Ask_Click");         //统计求P的点击次数
                Intent intent = new Intent(getActivity(),RecentAsksActivity.class);
                startActivity(intent);
            }
        });
        workImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MobclickAgent.onEvent(getActivity(), "Tupppai_Work_Click");        //统计作品的点击次数
                Intent intent = new Intent(getActivity(), RecentWorkActivity.class);
                startActivity(intent);
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

        mListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                if(canLoadMore){
                    page++;
                    TupppaiRequest request = new TupppaiRequest.Builder().setListener(moreListener).
                            setErrorListener(errorListener).setPage(page).build();

                    RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                            getActivity()).getRequestQueue();
                    requestQueue.add(request);
                }
            }
        });
    }



    PSGodErrorListener errorListener = new PSGodErrorListener(this) {
        @Override
        public void handleError(VolleyError error) {
            mListView.onRefreshComplete();
            if(progressingDialog != null && progressingDialog.isShowing()){
                progressingDialog.dismiss();
            }
        }
    };

    Response.Listener<List<Tupppai>> refreshListener = new Response.Listener<List<Tupppai>>() {
        @Override
        public void onResponse(List<Tupppai> response) {
            mListView.onRefreshComplete();
            if(tupppais.size() > 0){
                tupppais.clear();
            }
            if(response.size() < 10){
                canLoadMore = false;
            }else{
                canLoadMore = true;
            }
            tupppais.addAll(response);
            mAdapter.notifyDataSetChanged();

            if(progressingDialog != null && progressingDialog.isShowing()){
                progressingDialog.dismiss();
            }
        }
    };

    Response.Listener<List<Tupppai>> moreListener = new Response.Listener<List<Tupppai>>() {
        @Override
        public void onResponse(List<Tupppai> response) {
            if(response.size() < 10){
                canLoadMore = false;
            }else{
                canLoadMore = true;
            }
            tupppais.addAll(response);
            mAdapter.notifyDataSetChanged();
        }
    };

}
