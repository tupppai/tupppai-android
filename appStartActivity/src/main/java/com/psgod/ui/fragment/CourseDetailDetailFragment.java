package com.psgod.ui.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.pingplusplus.android.PaymentActivity;
import com.pingplusplus.android.PingppLog;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.model.Comment;
import com.psgod.ui.adapter.CourseDetailCommentAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/1/18 0018.
 */
public class CourseDetailDetailFragment extends BaseFragment {

    public static final int REQUEST_CODE_PAYMENT = 100;

    private static final String TAG = CourseDetailDetailFragment.class.getSimpleName();
    private List<Comment> mComments = new ArrayList<>();
    private Context mContext;
    private CourseDetailCommentAdapter mAdapter;
    private ViewHolder mViewHolder;
    private CourseDetailListener mListListner;
    private View mHeaderView;
    private View mFooterView;

    private TextView mRewardTxt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mContext = getActivity();
        FrameLayout parentView = new FrameLayout(getActivity());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        parentView.setLayoutParams(layoutParams);

        mViewHolder = new ViewHolder();
        mViewHolder.mParentView = parentView;
        mViewHolder.mView = inflater.inflate(R.layout.fragment_course_detail_detail, parentView, true);
        mViewHolder.mListView = (PullToRefreshListView) mViewHolder.mView.findViewById(R.id.course_comment_list_listview);
        mViewHolder.mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mAdapter = new CourseDetailCommentAdapter(mContext, mComments);
        mViewHolder.mListView.setAdapter(mAdapter);

        mHeaderView = getActivity().getLayoutInflater().inflate(R.layout.course_detail_header_layout, null);
        mViewHolder.mListView.getRefreshableView().addHeaderView(mHeaderView);
        mFooterView = LayoutInflater.from(mContext).inflate(R.layout.footer_load_more, null);
        mFooterView.setVisibility(View.INVISIBLE);
        mViewHolder.mListView.getRefreshableView().addFooterView(mFooterView);

        mListListner = new CourseDetailListener(mContext);
        mViewHolder.mListView.setOnRefreshListener(mListListner);
        mViewHolder.mListView.setOnLastItemVisibleListener(mListListner);

        mRewardTxt = (TextView) mViewHolder.mView.findViewById(R.id.reward_tv);

        initListener();
        return parentView;
    }

    private void initListener() {
//        mRewardTxt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent();
//                String packageName = getActivity().getPackageName();
//                ComponentName componentName = new ComponentName(packageName, packageName + ".wxapi.WXPayEntryActivity");
//                intent.setComponent(componentName);
//                intent.putExtra(PaymentActivity.EXTRA_CHARGE, "");
//                getActivity().startActivityForResult(intent, REQUEST_CODE_PAYMENT);
//            }
//        });
    }

    private class CourseDetailListener implements PullToRefreshBase.OnRefreshListener,
            PullToRefreshBase.OnLastItemVisibleListener {
        private Context mContext;

        public CourseDetailListener(Context context) {
            mContext = context;

        }

        @Override
        public void onLastItemVisible() {

        }

        @Override
        public void onRefresh(PullToRefreshBase refreshView) {
            // TODO Auto-generated method stub
        }
    }

    private static class ViewHolder {
        private View mParentView;
        private View mView;
        private PullToRefreshListView mListView;
    }

}
