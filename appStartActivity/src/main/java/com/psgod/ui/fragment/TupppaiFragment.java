package com.psgod.ui.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.psgod.R;
import com.psgod.ui.activity.RecentAsksActivity;
import com.psgod.ui.activity.RecentWorkActivity;
import com.psgod.ui.adapter.TupppaiAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TupppaiFragment extends BaseFragment {


    public TupppaiFragment() {
    }

    private PullToRefreshListView mListView;
    private TupppaiAdapter mAdapter;
    private List<Object> objects;
    private ImageView askImg;
    private ImageView workImg;

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

    }

    private void initView(View view) {
        View head = LayoutInflater.from(getActivity()).inflate(R.layout.view_tupppai_head, null);
        askImg = (ImageView) head.findViewById(R.id.view_tupppai_head_ask);
        workImg = (ImageView) head.findViewById(R.id.view_tupppai_head_work);
        mListView = (PullToRefreshListView) view.findViewById(R.id.fragment_tupppai_list);
        mListView.getRefreshableView().addHeaderView(head);
        objects = new ArrayList<Object>();
        mAdapter = new TupppaiAdapter(getActivity(),objects);
        mListView.setAdapter(mAdapter);
    }

    private void initListener() {
        askImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),RecentAsksActivity.class);
                startActivity(intent);
            }
        });
        workImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), RecentWorkActivity.class);
                startActivity(intent);
            }
        });
    }

}
