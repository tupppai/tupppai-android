package com.pires.wesee.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;
import com.pires.wesee.WeakReferenceHandler;
import com.pires.wesee.model.Comment;
import com.pires.wesee.model.LoginUser;
import com.pires.wesee.model.PhotoItem;
import com.pires.wesee.network.request.CommentListRequest;
import com.pires.wesee.network.request.PSGodErrorListener;
import com.pires.wesee.network.request.PSGodRequestQueue;
import com.pires.wesee.network.request.PostCommentRequest;
import com.pires.wesee.ui.adapter.CommentExpandableListAdapter;
import com.pires.wesee.ui.widget.dialog.CustomProgressingDialog;
import com.pires.wesee.Constants;
import com.pires.wesee.R;
import com.pires.wesee.ui.view.FaceRelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 评论列表界面
 *
 * @author Rayal
 */
public class CommentListActivity extends PSGodBaseActivity implements
        Handler.Callback {
    private final static String TAG = CommentListActivity.class.getSimpleName();

    private PullToRefreshExpandableListView mCommentLv;
    private CommentExpandableListAdapter mAdapter;
    private PhotoItem mPhotoItem;
    private View mEmptyView;
    private View mCommentListFooter;
    private View faceView; // 表情页

    private List<Comment> mHotComments;
    private List<Comment> mRecentComments;
    private TextView mSendCommentBtn;
    private EditText mCommentEditText;

    private CustomProgressingDialog mProgressDialog;

    private RelativeLayout mParent;

    // 控制是否可以加载下一页
    private boolean canLoadMore = true;
    private long mLastUpdateTime;
    private int mPage;

    private long replyToCid = 0;

    public static final String COMMENT_ID = "comment_id";

    private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);
    private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);

    CommentListListener mCommentListListener;

    // 评论内容
    String commentContent = "";
    // @的信息 若有
    String atComments;
    // @的用户名
    String atNickName = "";

    private Listener<CommentListRequest.CommentListWrapper> refreshListener = new Listener<CommentListRequest.CommentListWrapper>() {
        @Override
        public void onResponse(CommentListRequest.CommentListWrapper response) {
            mHotComments.clear();
            mHotComments.addAll(response.hotCommentList);
            mRecentComments.clear();
            mRecentComments.addAll(response.recentCommentList);
            mAdapter.notifyDataSetChanged();
            mCommentLv.onRefreshComplete();

            if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }

            // 展开所有分组
            int groupCount = mAdapter.getGroupCount();
            for (int ix = 0; ix < groupCount; ++ix) {
                mCommentLv.getRefreshableView().expandGroup(ix);
            }

            if (response.recentCommentList.size() < 10) {
                canLoadMore = false;
            } else {
                canLoadMore = true;
            }

            mEmptyView = LayoutInflater.from(CommentListActivity.this).inflate(
                    R.layout.empty_comment_image_list_view, null);
            mCommentLv.getRefreshableView().setEmptyView(mEmptyView);

            if (isJump) {
                mCommentListListener.onChildClick(
                        mCommentLv.getRefreshableView(), null, 0, 0, 0L);
                isJump = false;
            }

        }
    };

    boolean isJump = false;

    private Listener<CommentListRequest.CommentListWrapper> loadMoreListener = new Listener<CommentListRequest.CommentListWrapper>() {
        @Override
        public void onResponse(CommentListRequest.CommentListWrapper response) {
            if (response.recentCommentList.size() > 0) {
                mHotComments.clear();
                mHotComments.addAll(response.hotCommentList);
                mRecentComments.addAll(response.recentCommentList);
                mAdapter.notifyDataSetChanged();
            }

            mCommentListFooter.setVisibility(View.INVISIBLE);

            if (response.recentCommentList.size() < 10) {
                canLoadMore = false;
            } else {
                canLoadMore = true;
            }

            // 展开所有分组
            int groupCount = mAdapter.getGroupCount();
            for (int ix = 0; ix < groupCount; ++ix) {
                mCommentLv.getRefreshableView().expandGroup(ix);
            }
        }
    };

    private PSGodErrorListener errorListener = new PSGodErrorListener(
            CommentListRequest.class.getSimpleName()) {
        @Override
        public void handleError(VolleyError error) {
            mCommentLv.onRefreshComplete();
        }
    };

    private PSGodErrorListener sendCommentErrorListener = new PSGodErrorListener(this) {
        @Override
        public void handleError(VolleyError error) {
            Toast.makeText(CommentListActivity.this, "评论失败，请稍后再试",
                    Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_comment_list);

        Object obj = getIntent().getSerializableExtra(
                Constants.IntentKey.PHOTO_ITEM);
        if (obj == null || !(obj instanceof PhotoItem)) {
            // TODO error
            finish();
        }

        faceView = this.findViewById(R.id.activity_comment_face_choose_panel);
        mParent = (RelativeLayout) findViewById(R.id.activity_comment_list_parent);
        mPhotoItem = (PhotoItem) obj;
        mCommentLv = (PullToRefreshExpandableListView) this
                .findViewById(R.id.activity_comment_list_lv);
        mCommentLv.setMode(Mode.PULL_FROM_START);
        mCommentLv.getRefreshableView().setDivider(null); // 去除listview的边框

        mCommentListFooter = LayoutInflater.from(CommentListActivity.this)
                .inflate(R.layout.footer_load_more, null);

        mCommentLv.getRefreshableView().addFooterView(mCommentListFooter);
        mCommentListFooter.setVisibility(View.GONE);

        mHotComments = mPhotoItem.getHotCommentList();
        if (mHotComments == null) {
            mHotComments = new ArrayList<Comment>();
        }
        mRecentComments = mPhotoItem.getCommentList();
        if (mRecentComments == null) {
            mRecentComments = new ArrayList<Comment>();
        }
        mAdapter = new CommentExpandableListAdapter(this, mHotComments,
                mRecentComments);
        mCommentLv.getRefreshableView().setAdapter(mAdapter);

        mCommentListListener = new CommentListListener(this, mPhotoItem);
        mCommentLv.setOnLastItemVisibleListener(mCommentListListener);
        mCommentLv.setOnRefreshListener(mCommentListListener);
        mCommentLv.setScrollingWhileRefreshingEnabled(true);
        // 点击评论添加@内容
        mCommentLv.getRefreshableView().setOnChildClickListener(
                mCommentListListener);

        mSendCommentBtn = (TextView) this
                .findViewById(R.id.activity_comment_list_post_btn);
        mCommentEditText = (EditText) this
                .findViewById(R.id.activity_comment_list_input_panel);

        // 显示等待对话框
        if (mProgressDialog == null) {
            mProgressDialog = new CustomProgressingDialog(
                    CommentListActivity.this);
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }

        Intent intent = getIntent();
        long commentId = intent.getLongExtra(COMMENT_ID, -1);
        if (commentId != -1) {
            refresh(commentId);
        } else {
            refresh();
        }

        // 初始化事件监听
        initEvents();
    }

    // 回退键关闭表情框
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && ((FaceRelativeLayout) findViewById(R.id.FaceRelativeLayout))
                .hideFaceView()) {
            return true;
        }
        if ((keyCode == KeyEvent.KEYCODE_BACK) && (replyToCid != 0)) {
            mCommentEditText.setText("");
            mCommentEditText.setHint("添加评论");
            replyToCid = 0;
            mAdapter.setSelectItem(-1);
            mAdapter.notifyDataSetChanged();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void hideInputPanel() {
        // 隐藏软键盘
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSendCommentBtn.getWindowToken(), 0);
    }

    private void callInputPanel() {
        // 唤起输入键盘 并输入框取得焦点
        mCommentEditText.setFocusableInTouchMode(true);
        mCommentEditText.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mCommentEditText, 0);
    }

    public void initEvents() {
        // 发送评论
        // 发送逻辑 首先本地数据填充展示 后台发送
        mSendCommentBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                commentContent = mCommentEditText.getText().toString();

                if (TextUtils.isEmpty(commentContent)) {
                    Toast.makeText(CommentListActivity.this, "请输入评论内容",
                            Toast.LENGTH_SHORT).show();
                    mCommentEditText.requestFocus();
                } else {
                    // 构造新的评论数据 本地展示
                    Comment comment = new Comment();

                    // 本地用户数据
                    LoginUser user = LoginUser.getInstance();
                    // 用户无法回复自己 comment id暂为0
                    comment.setCid(0);
                    comment.setUid(user.getUid());
                    comment.setContent(commentContent);
                    if (!TextUtils.isEmpty(atNickName)) {
                        comment.mReplyComments.add(new Comment.ReplyComment(0, 0, atComments, atNickName));
                    }
                    comment.setPid(mPhotoItem.getPid());
                    comment.setAvatarURL(user.getAvatarImageUrl());
                    comment.setCreatedTime(System.currentTimeMillis());

                    // 该条评论点赞数
                    comment.setLikeCount(0);
                    comment.setGender(user.getGender());
                    comment.setNickName(user.getNickname());

                    mRecentComments.add(0, comment);
                    mAdapter.notifyDataSetChanged();
                    mCommentLv.getRefreshableView().setSelection(
                            mHotComments.size() + 1);

                    mPhotoItem.setCommentCount(mPhotoItem.getCommentCount() + 1);
                    // TODO即时更新评论的数量

                    // 展开所有分组
                    int groupCount = mAdapter.getGroupCount();
                    for (int ix = 0; ix < groupCount; ++ix) {
                        mCommentLv.getRefreshableView().expandGroup(ix);
                    }

                    hideInputPanel();

                    // 隐藏表情选择窗口
                    ((FaceRelativeLayout) findViewById(R.id.FaceRelativeLayout))
                            .hideFaceView();

                    // 后台发送评论
                    sendCommentBackEnd();
                    mCommentLv.getRefreshableView().setSelection(0);
                }
            }
        });

        mParent.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                hideInputPanel();
            }
        });
    }

    // 后台发送评论
    public void sendCommentBackEnd() {
        PostCommentRequest.Builder builder = new PostCommentRequest.Builder()
                .setContent(commentContent).setCid(replyToCid)
                .setPid(mPhotoItem.getPid()).setType(mPhotoItem.getType())
                .setListener(sendCommentListener)
                .setErrorListener(sendCommentErrorListener);

        PostCommentRequest request = builder.build();
        request.setTag(TAG);
        RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                CommentListActivity.this).getRequestQueue();
        requestQueue.add(request);

        mAdapter.setSelectItem(-1);
    }

    // 发送成功后 将返回的comment_id 填充进本地数据首条中
    private Listener<Long> sendCommentListener = new Listener<Long>() {
        @Override
        public void onResponse(Long response) {
            if (response != null) {
//				mRecentComments.get(0).setCid(response);
                refresh();
                atNickName = "";
                // 清空输入框
                mCommentEditText.setText("");
                mCommentEditText.setHint("添加评论");
                replyToCid = 0;
                mAdapter.setSelectItem(-1);
                mAdapter.notifyDataSetChanged();
            }
        }

    };

    // @Override
    // public void onStop() {
    // super.onStop();
    // RequestQueue requestQueue = PSGodRequestQueue.getInstance(this)
    // .getRequestQueue();
    // requestQueue.cancelAll(TAG);
    // }

    private void refresh(long commentId) {
        canLoadMore = false;

        mLastUpdateTime = System.currentTimeMillis();
        mPage = 1;
        isJump = true;
        CommentListRequest.Builder builder = new CommentListRequest.Builder()
                .setPid(mPhotoItem.getPid()).setPage(mPage)
                .setLastUpdated(mLastUpdateTime).setType(mPhotoItem.getType())
                .setCommentId(commentId).setListener(refreshListener)
                .setErrorListener(errorListener);
        CommentListRequest request = builder.build();
        request.setTag(TAG);
        RequestQueue requestQueue = PSGodRequestQueue.getInstance(this)
                .getRequestQueue();
        requestQueue.add(request);
    }

    private void refresh() {
        canLoadMore = false;

        mLastUpdateTime = System.currentTimeMillis();
        mPage = 1;
        CommentListRequest.Builder builder = new CommentListRequest.Builder()
                .setPid(mPhotoItem.getPid()).setPage(mPage)
                .setLastUpdated(mLastUpdateTime).setType(mPhotoItem.getType())
                .setListener(refreshListener).setErrorListener(errorListener);
        CommentListRequest request = builder.build();
        request.setTag(TAG);
        RequestQueue requestQueue = PSGodRequestQueue.getInstance(this)
                .getRequestQueue();
        requestQueue.add(request);
    }

    private class CommentListListener implements OnRefreshListener,
            OnLastItemVisibleListener, OnChildClickListener {
        private Context mContext;
        private PhotoItem mPhotoItem;

        public CommentListListener(Context context, PhotoItem photoItem) {
            mContext = context;
            mPhotoItem = photoItem;
        }

        @Override
        public void onLastItemVisible() {
            if (canLoadMore) {
                mCommentListFooter.setVisibility(View.VISIBLE);
                ++mPage;
                CommentListRequest.Builder builder = new CommentListRequest.Builder()
                        .setPid(mPhotoItem.getPid()).setPage(mPage)
                        .setLastUpdated(mLastUpdateTime)
                        .setType(mPhotoItem.getType())
                        .setListener(loadMoreListener)
                        .setErrorListener(errorListener);
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

        // 点击评论项 若非本人回复，则自动添加@选项
        @Override
        public boolean onChildClick(ExpandableListView lv, View view,
                                    final int groupPosition, final int childPosition, long id) {
            Comment comment = (Comment) mAdapter.getChild(groupPosition,
                    childPosition);

            String authorName = comment.getNickname();
            // 评论人id
            Long authorId = comment.getUid();
            // 评论id
            Long cid = comment.getCid();

            LoginUser user = LoginUser.getInstance();
            // 和自己的id做比较
            if (authorId != user.getUid()) {
                mCommentEditText.setHint("回复@" + authorName + ":");
                replyToCid = cid;

                atComments = comment.getContent();
                atNickName = comment.getNickname();
            } else {
                atNickName = "";
                mCommentEditText.setHint("添加评论");
                replyToCid = 0;
            }

            // 隐藏表情页
            faceView.setVisibility(View.GONE);

            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(200);
                        Message msg = new Message();
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

            // 唤起软键盘 并获得焦点
            callInputPanel();

            return false;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        int group = msg.getData().getInt("group");
        int child = msg.getData().getInt("child");
        mCommentLv.getRefreshableView().smoothScrollToPosition(child + 3);
        mAdapter.setSelectItem(child);
        return false;
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        try {
            hideInputPanel();
        } catch (NullPointerException ne) {

        } catch (Exception e) {

        }
    }
}
