package com.psgod.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.psgod.R;
import com.psgod.ui.adapter.ChannelHeadAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChannelActivity extends PSGodBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        initView();
        initData();
        initListener();
    }

    private TextView mTitleName;
    private ImageView mTitleFinish;
    private PullToRefreshListView mList;

    private TextView mHeadMore;
    private RecyclerView mHeadList;

    private List<Object> objects;
    private ChannelHeadAdapter headAdapter;

    private void initView() {
        mTitleName = (TextView) findViewById(R.id.activity_channal_title_name);
        mTitleFinish = (ImageView) findViewById(R.id.activity_channal_title_finish);
        mList = (PullToRefreshListView) findViewById(R.id.activity_channal_list);
        mList.setAdapter(adapter);
        View head = LayoutInflater.from(this).inflate(R.layout.view_activity_channel_head,null);
        mList.getRefreshableView().addHeaderView(head);
        mHeadList = (RecyclerView) head.findViewById(R.id.activity_channal_head_list);
        mHeadMore = (TextView) head.findViewById(R.id.activity_channal_head_more);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        mHeadList.setLayoutManager(layoutManager);
        objects = new ArrayList<Object>();
        headAdapter = new ChannelHeadAdapter(this,objects);
        mHeadList.setAdapter(headAdapter);

    }

    private void initData() {


    }

    private void initListener() {

    }


    BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            return new View(ChannelActivity.this);
        }
    };

}
