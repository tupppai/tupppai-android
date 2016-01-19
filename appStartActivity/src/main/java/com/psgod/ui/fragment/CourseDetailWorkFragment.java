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
import com.psgod.ui.view.PullToRefreshSwipeMenuListView;

/**
 * Created by Administrator on 2016/1/18 0018.
 */
public class CourseDetailWorkFragment extends BaseFragment {
    private static final String TAG = CourseDetailWorkFragment.class.getSimpleName();
    private Context mContext;
    private ViewHolder mViewHolder;
    private View mFooterView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        mContext = getActivity();
        FrameLayout parentView = new FrameLayout(getActivity());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        parentView.setLayoutParams(layoutParams);

        mViewHolder = new ViewHolder();
        mViewHolder.mParentView = parentView;
        mViewHolder.mView = inflater.inflate(R.layout.fragment_course_detail_work,parentView,true);
        mViewHolder.mListView = (PullToRefreshListView) mViewHolder.mView.findViewById(R.id.course_detail_work_list_listview);
        mViewHolder.mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mFooterView = LayoutInflater.from(mContext).inflate(R.layout.footer_load_more,null);
        mViewHolder.mListView.getRefreshableView().addFooterView(mFooterView);
        mFooterView.setVisibility(View.INVISIBLE);

        return parentView;
    }

    private static class ViewHolder{
        private View mParentView;
        private View mView;
        private PullToRefreshListView mListView;
    }

}
