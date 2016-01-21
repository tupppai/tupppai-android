package com.psgod.ui.activity;

import android.content.Context;
import android.os.Bundle;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.psgod.R;

/**
 * Created by pires on 16/1/21.
 */
public class ChangeDetailActivity extends PSGodBaseActivity {
    private static final String TAG = ChangeDetailActivity.class.getSimpleName();
    private Context mContext;
    private PullToRefreshListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        setContentView(R.layout.activity_change_detail);
        mListView = (PullToRefreshListView) findViewById(R.id.change_detail_listview);
    }
}
