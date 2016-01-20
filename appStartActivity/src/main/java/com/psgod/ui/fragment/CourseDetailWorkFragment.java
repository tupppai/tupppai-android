package com.psgod.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.psgod.R;
import com.psgod.model.PhotoItem;
import com.psgod.ui.adapter.PhotoListAdapter;
import com.psgod.ui.view.PhotoItemView;
import com.psgod.ui.view.PullToRefreshSwipeMenuListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/1/18 0018.
 */
public class CourseDetailWorkFragment extends BaseFragment {
    private static final String TAG = CourseDetailWorkFragment.class.getSimpleName();
    private Context mContext;
    private ViewHolder mViewHolder;
    private View mFooterView;

    private List<PhotoItem> mPhotoItems = new ArrayList<>();
    private PhotoListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mContext = getActivity();
        mViewHolder = new ViewHolder();
        mViewHolder.mView = inflater.inflate(R.layout.fragment_course_detail_work, null);
        mViewHolder.mListView = (PullToRefreshListView) mViewHolder.mView.findViewById(R.id.course_detail_work_list_listview);
        mViewHolder.mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mAdapter = new PhotoListAdapter(getActivity(), PhotoItemView.PhotoListType.RECENT_REPLY, mPhotoItems);
        mViewHolder.mListView.setAdapter(mAdapter);
        mFooterView = LayoutInflater.from(mContext).inflate(R.layout.footer_load_more, null);
        mViewHolder.mListView.getRefreshableView().addFooterView(mFooterView);
        mFooterView.setVisibility(View.INVISIBLE);

        return mViewHolder.mView;
    }

    private static class ViewHolder {
        private View mView;
        private PullToRefreshListView mListView;
    }

}
