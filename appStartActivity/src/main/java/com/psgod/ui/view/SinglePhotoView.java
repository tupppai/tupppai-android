package com.psgod.ui.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.WeakReferenceHandler;
import com.psgod.model.Comment;
import com.psgod.model.LoginUser;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.CommentListRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.PostCommentRequest;
import com.psgod.ui.adapter.SinglePhotoDetailAdapter;
import com.psgod.ui.widget.ActionBar;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2015/11/28 0028.
 */
public class SinglePhotoView extends RelativeLayout implements
        Handler.Callback {
    public SinglePhotoView(Context context, PhotoItem photoItem) {
        super(context);
        this.mPhotoItem = photoItem;
        init();
    }

    public SinglePhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SinglePhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private static final String TAG = SinglePhotoView.class.getSimpleName();

    private static final int MSG_LOAD_MORE = 330;
    public static final int MSG_HIDE = 0X551;
    public static final int ITEM_SHOW = 495;

    private TextView mCommentBtn;
    private SinglePhotoDatailView mPhotoItemView;

    private PullToRefreshExpandableListView mListView;
    private SinglePhotoDetailAdapter mAdapter;
    private PhotoItem mPhotoItem;
    private CommentListListener mListViewListener;
    private TextView mSendCommentBtn;
    private EditText mCommentEditText;
    private ActionBar actionBar;

    private RelativeLayout mParent;

    private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);

    // 加载更多footer
    private View mCommentListFooter;
    // 照片的id
    private long mId = -1;
    // 是否需要后台返回photoitem
    private int mNeedOriginPhotoItem = 0;

    private List<Comment> mHotCommentList;
    private List<Comment> mCommentList;

//    private CustomProgressingDialog mProgressDialog;
    private int mPage = 1;

    private long replyToCid;

    // 评论内容
    String commentContent = "";
    // @的信息 若有
    StringBuilder atComments = new StringBuilder();

    // 判断是否可以加载下一页
    private boolean canLoadMore = true;

    private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.activity_single_photo_detail, null);

        mId = mPhotoItem.getPid();


        mSendCommentBtn = (TextView) view
                .findViewById(R.id.activity_comment_list_post_btn);
        mCommentEditText = (EditText) view
                .findViewById(R.id.activity_comment_list_input_panel);
        mHotCommentList = mPhotoItem.getHotCommentList();
        if (mHotCommentList == null) {
            mHotCommentList = new ArrayList<Comment>();
        }
        mCommentList = mPhotoItem.getCommentList();
        if (mCommentList == null) {
            mCommentList = new ArrayList<Comment>();
        }
        mAdapter = new SinglePhotoDetailAdapter(getContext(), mPhotoItem, mCommentList,
                mHandler);

        mListView = (PullToRefreshExpandableListView) view.findViewById(R.id.activity_photo_detail_expandable_list);
        mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mListView.getRefreshableView().setAdapter(mAdapter);
        // loadmore footer
        mCommentListFooter = LayoutInflater.from(getContext())
                .inflate(R.layout.footer_load_more, null);
        mListView.getRefreshableView().addFooterView(mCommentListFooter);
        mCommentListFooter.setVisibility(View.GONE);

        mListViewListener = new CommentListListener(getContext(), mId);
        mListView.setOnLastItemVisibleListener(mListViewListener);
        mListView.setOnRefreshListener(mListViewListener);
        mListView.getRefreshableView().setOnChildClickListener(
                mListViewListener);
        mListView.setScrollingWhileRefreshingEnabled(true);
        actionBar = (ActionBar) view.findViewById(R.id.single_photo_detail_parent_actionbar);
        actionBar.setLeftBtnOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onEndListener != null){
                    onEndListener.onEndListener(SinglePhotoView.this);
                }
            }
        });

        mParent = (RelativeLayout) view.findViewById(R.id.single_photo_detail_parent);

        // 展开所有分组
        int groupCount = mAdapter.getGroupCount();
        for (int ix = 0; ix < groupCount; ++ix) {
            mListView.getRefreshableView().expandGroup(ix);
        }

        // 设置点击分组不可展开
        mListView.getRefreshableView().setOnGroupClickListener(
                new ExpandableListView.OnGroupClickListener() {
                    @Override
                    public boolean onGroupClick(ExpandableListView parent,
                                                View v, int groupPosition, long id) {
                        atComments.delete(0, atComments.length());
                        hideInputPanel();
                        mCommentEditText.setHint("添加评论");
                        replyToCid = 0;
                        return true;
                    }
                });

//        // 显示等待对话框
//        if (mProgressDialog == null) {
//            mProgressDialog = new CustomProgressingDialog(
//                    getContext());
//        }
//        if (!mProgressDialog.isShowing()) {
//            mProgressDialog.show();
//        }

        // 初始化评论的数据
        refresh();

        initEvents();
        addView(view);
    }

    public void initEvents() {
        // 发送评论
        mSendCommentBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                commentContent = mCommentEditText.getText().toString();
                if (TextUtils.isEmpty(commentContent)) {
                    Toast.makeText(getContext(), "请输入评论内容",
                            Toast.LENGTH_SHORT).show();
                    mCommentEditText.requestFocus();
                } else {
                    Comment comment = new Comment();

                    // 获取本地用户数据
                    LoginUser user = LoginUser.getInstance();

                    comment.setCid(0);
                    comment.setUid(user.getUid());
                    comment.setContent(commentContent + atComments.toString());
                    comment.setPid(mPhotoItem.getPid());
                    comment.setAvatarURL(user.getAvatarImageUrl());
                    comment.setCreatedTime(System.currentTimeMillis());

                    // 该条评论点赞数
                    comment.setLikeCount(0);
                    comment.setGender(user.getGender());
                    comment.setNickName(user.getNickname());

                    mCommentList.add(0, comment);
                    mAdapter.notifyDataSetChanged();
                    mListView.getRefreshableView().setSelection(
                            mHotCommentList.size() + 1);

                    // // 展开所有分组
                    int groupCount = mAdapter.getGroupCount();
                    for (int ix = 0; ix < groupCount; ++ix) {
                        mListView.getRefreshableView().expandGroup(ix);
                    }

                    hideInputPanel();
                    // 清空输入框
                    mCommentEditText.setText("");

                    // 隐藏表情选择窗口
                    ((FaceRelativeLayout) findViewById(R.id.FaceRelativeLayout))
                            .hideFaceView();

                    mPhotoItem.setCommentCount(mPhotoItem.getCommentCount() + 1);
                    mPhotoItemView.updateCommentView();

                    // 后台发送评论
                    sendCommentBackEnd();
                }
            }
        });

    }

    // 后台发布评论
    public void sendCommentBackEnd() {
        PostCommentRequest.Builder builder = new PostCommentRequest.Builder()
                .setContent(commentContent).setCid(replyToCid)
                .setPid(mPhotoItem.getPid()).setType(mPhotoItem.getType())
                .setListener(sendCommentListener)
                .setErrorListener(sendCommentErrorListener);

        PostCommentRequest request = builder.build();
        request.setTag(TAG);
        RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                getContext()).getRequestQueue();
        requestQueue.add(request);
    }

    // 发送成功后 将返回的comment_id 填充进本地数据首条中
    private Response.Listener<Long> sendCommentListener = new Response.Listener<Long>() {
        @Override
        public void onResponse(Long response) {
            if (response != null) {
                mCommentList.get(0).setCid(response);
            }
        }
    };

    private PSGodErrorListener sendCommentErrorListener = new PSGodErrorListener() {
        @Override
        public void handleError(VolleyError error) {
            Toast.makeText(getContext(), "评论失败，请稍后再试",
                    Toast.LENGTH_SHORT).show();
        }
    };

    private void hideInputPanel() {
        // 隐藏软键盘
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSendCommentBtn.getWindowToken(), 0);
    }

    private void callInputPanel() {
        // 唤起输入键盘 并输入框取得焦点
        mCommentEditText.setFocusableInTouchMode(true);
        mCommentEditText.requestFocus();

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mCommentEditText, 0);
    }

    // 回退键关闭表情框
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && ((FaceRelativeLayout) findViewById(R.id.FaceRelativeLayout))
                .hideFaceView()) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void refresh() {
        mPage = 1;
        CommentListRequest.Builder builder = new CommentListRequest.Builder()
                .setPid(mId).setPage(mPage).setType(mPhotoItem.getType())
                .setNeedPhotoItem(mNeedOriginPhotoItem)
                .setListener(refreshListener).setErrorListener(errorListener);

        CommentListRequest request = builder.build();
        request.setTag(TAG);
        RequestQueue requestQueue = PSGodRequestQueue.getInstance(getContext())
                .getRequestQueue();
        requestQueue.add(request);
    }

    private PSGodErrorListener errorListener = new PSGodErrorListener(
            CommentListRequest.class.getSimpleName()) {
        @Override
        public void handleError(VolleyError error) {
            mListView.onRefreshComplete();
        }
    };

    private Response.Listener<CommentListRequest.CommentListWrapper> refreshListener = new Response.Listener<CommentListRequest.CommentListWrapper>() {
        @Override
        public void onResponse(CommentListRequest.CommentListWrapper response) {
            if (mNeedOriginPhotoItem == 1) {
                mPhotoItem = response.photoItem;
                mAdapter.setPhotoItem(response.photoItem);
            }
            mHotCommentList.clear();
            mHotCommentList.addAll(response.hotCommentList);

            mCommentList.clear();
            mCommentList.addAll(response.recentCommentList);
            mAdapter.notifyDataSetChanged();
            mListView.onRefreshComplete();

            mPhotoItemView = mAdapter.getPhotoItemView();
            // 获得头部中评论Tv
            mCommentBtn = mPhotoItemView.getRecentPhotoDetailCommentBtn();

            mCommentBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    callInputPanel();
                }
            });

//            if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
//                mProgressDialog.dismiss();
//            }

            // 展开所有分组
            int groupCount = mAdapter.getGroupCount();
            for (int ix = 0; ix < groupCount; ++ix) {
                mListView.getRefreshableView().expandGroup(ix);
            }

            if (response.recentCommentList.size() < 10) {
                canLoadMore = false;
            } else {
                canLoadMore = true;
            }

        }
    };

    private Response.Listener<CommentListRequest.CommentListWrapper> loadMoreListener = new Response.Listener<CommentListRequest.CommentListWrapper>() {
        @Override
        public void onResponse(CommentListRequest.CommentListWrapper response) {
            if (response.recentCommentList.size() > 0) {
                mHotCommentList.clear();
                mHotCommentList.addAll(response.hotCommentList);
                mCommentList.addAll(response.recentCommentList);

                mAdapter.notifyDataSetChanged();
                mListView.onRefreshComplete();
            }

            mCommentListFooter.setVisibility(View.INVISIBLE);

            if (response.recentCommentList.size() < 10) {
                canLoadMore = false;
            } else {
                canLoadMore = true;
            }

            // // 展开所有分组
            // int groupCount = mAdapter.getGroupCount();
            // for (int ix = 0; ix < groupCount; ++ix) {
            // mListView.getRefreshableView().expandGroup(ix);
            // }
        }
    };

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_HIDE:
                atComments.delete(0, atComments.length());
                hideInputPanel();
                mCommentEditText.setHint("添加评论");
                replyToCid = 0;
                break;
            case ITEM_SHOW:
                int group = msg.getData().getInt("group");
                int child = msg.getData().getInt("child");
                mListView.getRefreshableView().setSelectedChild(group, child, true);
        }
        return false;
    }

    private class CommentListListener implements PullToRefreshBase.OnRefreshListener,
            PullToRefreshBase.OnLastItemVisibleListener, ExpandableListView.OnChildClickListener {
        private Context mContext;
        private long mId;

        public CommentListListener(Context context, long id) {
            mId = id;
            mContext = context;
        }

        // 点击评论项 若非本人回复，则自动添加@选项
        @Override
        public boolean onChildClick(ExpandableListView lv, View view,
                                    final int groupPosition, final int childPosition, long id) {
            Object obj = mAdapter.getChild(groupPosition, childPosition);
            if (!(obj instanceof Comment)) {
                return false;
            }
            Comment comment = (Comment) obj;
            String authorName = comment.getNickname();
            // 评论人id
            Long authorId = comment.getUid();
            // 评论id
            Long cid = comment.getCid();

            LoginUser user = LoginUser.getInstance();

            atComments.delete(0, atComments.length());
            // 和本地用户id比对
            if (authorId != user.getUid()) {
                mCommentEditText.setHint("回复@" + authorName + ":");
                replyToCid = cid;

                atComments.append("//@" + comment.getNickname() + ":"
                        + comment.getContent());
                List<Comment.ReplyComment> mReplyComments = comment.getReplyComments();
                for (int i = 0; i < mReplyComments.size(); i++) {
                    atComments
                            .append("//@" + mReplyComments.get(i).mNick + ":");
                    atComments.append(mReplyComments.get(i).mContent);
                }
            } else {
                mCommentEditText.setHint("添加评论");
                replyToCid = 0;
            }
            // 唤起软键盘 并获得焦点
            callInputPanel();
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(200);
                        Message msg = new Message();
                        msg.what = ITEM_SHOW;
                        Bundle bundle = new Bundle();
                        bundle.putInt("group", groupPosition);
                        bundle.putInt("child", childPosition);
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });

            return false;
        }

        @Override
        public void onLastItemVisible() {
            if (canLoadMore) {
                mCommentListFooter.setVisibility(View.VISIBLE);
                ++mPage;
                CommentListRequest.Builder builder = new CommentListRequest.Builder()
                        .setPid(mId).setPage(mPage)
                        .setType(mPhotoItem.getType())
                        .setErrorListener(errorListener)
                        .setListener(loadMoreListener);

                CommentListRequest request = builder.build();
                request.setTag(TAG);
                RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                        mContext).getRequestQueue();
                requestQueue.add(request);
            }
        }

        @Override
        public void onRefresh(PullToRefreshBase refreshView) {
            refresh();
        }

    }

    public interface OnEndListener {
        void onEndListener(SinglePhotoView view);
    }

    private OnEndListener onEndListener;

    public void setOnEndListener(OnEndListener onEndListener) {
        this.onEndListener = onEndListener;
    }
}
