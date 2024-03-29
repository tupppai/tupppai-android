package com.psgod.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.pingplusplus.android.PaymentActivity;
import com.psgod.Constants;
import com.psgod.CustomToast;
import com.psgod.PsGodImageLoader;
import com.psgod.R;
import com.psgod.ThreadManager;
import com.psgod.Utils;
import com.psgod.WeakReferenceHandler;
import com.psgod.eventbus.RefreshEvent;
import com.psgod.model.Comment;
import com.psgod.model.ImageData;
import com.psgod.model.LoginUser;
import com.psgod.model.PhotoItem;
import com.psgod.model.Reward;
import com.psgod.model.User;
import com.psgod.network.request.PhotoRequest;
import com.psgod.network.request.RewardRequest;
import com.psgod.network.request.CommentListRequest;
import com.psgod.network.request.CourseDetailRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.RewardRequest2;
import com.psgod.ui.activity.CommentListActivity;
import com.psgod.ui.adapter.CourseDetailCommentAdapter;
import com.psgod.ui.adapter.CourseDetailImageContentAdapter;
import com.psgod.ui.widget.AvatarImageView;
import com.psgod.ui.widget.ChildListView;
import com.psgod.ui.widget.FollowImage;
import com.psgod.ui.widget.dialog.CustomDialog;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;
import com.psgod.ui.widget.dialog.PayErrorDialog;
import com.psgod.ui.widget.dialog.ShareMoreDialog;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2016/1/18 0018.
 */
public class CourseDetailDetailFragment extends BaseFragment implements Handler.Callback {

    public static final int REQUEST_CODE_PAYMENT = 118;

    private static final String TAG = CourseDetailDetailFragment.class.getSimpleName();
    private List<Comment> mComments = new ArrayList<>();
    private Context mContext;
    private CourseDetailCommentAdapter mAdapter;
    private ViewHolder mViewHolder;
    private CourseDetailListener mListListner;
    private View mHeaderView;
    private View mFooterView;

    private ChildListView mHeadContentList;
    private AvatarImageView mHeadAvatar;
    private TextView mHeadNickname;
    private TextView mHeadCommentCount;
    private FollowImage mHeadFollow;
    private TextView mHeadTime;
    private TextView mHeadLikeCount;
    private TextView mHeadReplyCount;
    private TextView mHeadViewCount;
    private TextView mHeadDesc;
    private TextView mHeadTitle;

    private LinearLayout mRewardArea;
    private TextView mRewardTxt;
    private ImageView mRewardImg;
    private RelativeLayout mCommentArea;
    private RelativeLayout mShareArea;

    private CourseDetailImageContentAdapter mImageAdapter;
    private List<ImageData> mImageDatas = new ArrayList<>();

    private CustomProgressingDialog progressingDialog;
    private ShareMoreDialog shareMoreDialog;

    private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);
    private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);

    private long id;
    private PhotoItem mPhotoItem;

    private boolean isRewardEnd = true;
    private boolean isRewardError = false;
    private double amount = 0;
    private Reward reward;

    public CourseDetailDetailFragment(long id) {
        this.id = id;
    }

    public void onEventMainThread(RefreshEvent event) {
        if (event.className.equals(this.getClass().getName())) {
            refresh();
        }
    }

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
        mViewHolder.mListView.setMode(PullToRefreshBase.Mode.DISABLED);
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

        mRewardArea = (LinearLayout) mViewHolder.mView.findViewById(R.id.reward_area);
        mRewardTxt = (TextView) mViewHolder.mView.findViewById(R.id.reward_tv);
        mRewardImg = (ImageView) mViewHolder.mView.findViewById(R.id.reward_img);
        mCommentArea = (RelativeLayout) mViewHolder.mView.findViewById(R.id.comment_area);
        mShareArea = (RelativeLayout) mViewHolder.mView.findViewById(R.id.share_area);

        mHeadAvatar = (AvatarImageView) mHeaderView.findViewById(R.id.avatar_image);
        mHeadCommentCount = (TextView) mHeaderView.findViewById(R.id.comment_tv_count);
        mHeadContentList = (ChildListView) mHeaderView.findViewById(R.id.course_content_list);
        mImageAdapter = new CourseDetailImageContentAdapter(mContext, mImageDatas);
        mHeadContentList.setAdapter(mImageAdapter);
        mHeadFollow = (FollowImage) mHeaderView.findViewById(R.id.follow_iamge);
        mHeadNickname = (TextView) mHeaderView.findViewById(R.id.nickname_tv);
        mHeadLikeCount = (TextView) mHeaderView.findViewById(R.id.like_count_tv);
        mHeadViewCount = (TextView) mHeaderView.findViewById(R.id.view_count_tv);
        mHeadReplyCount = (TextView) mHeaderView.findViewById(R.id.course_image_count_tv);
        mHeadDesc = (TextView) mHeaderView.findViewById(R.id.course_desc);
        mHeadTitle = (TextView) mHeaderView.findViewById(R.id.course_title);

        mHeadTime = (TextView) mHeaderView.findViewById(R.id.time_tv);
        if (progressingDialog == null) {
            progressingDialog = new CustomProgressingDialog(getActivity());
        }
        progressingDialog.show();

        shareMoreDialog = new ShareMoreDialog(getActivity());

        refresh();
        initListener();
        return parentView;
    }

    private void initView(boolean isRefresh) {
        if (isRefresh) {
            if (mImageDatas.size() > 0) {
                mImageDatas.clear();
            }
            mImageDatas.addAll(mPhotoItem.getUploadImagesList());
            mImageAdapter.setIsLock(mPhotoItem.getHasUnlocked() == 1 ? false : true);
            mImageAdapter.notifyDataSetChanged();
        }
        if (mPhotoItem.getHasBought() == 1 ? true : false) {
            mRewardImg.setImageResource(R.mipmap.like3);
        } else {
            mRewardImg.setImageResource(R.mipmap.ic_like);
        }
        PsGodImageLoader.getInstance().displayImage(mPhotoItem.getAvatarURL(),
                mHeadAvatar, Constants.DISPLAY_IMAGE_OPTIONS_AVATAR);
        mHeadCommentCount.setText(String.valueOf("(" + mPhotoItem.getCommentCount() + ")"));
        mHeadFollow.setPhotoItem(mPhotoItem);
        mHeadAvatar.setUser(new User(mPhotoItem));
        mHeadNickname.setText(mPhotoItem.getNickname());
        mHeadTime.setText(mPhotoItem.getUpdateTimeStr());
        mHeadTitle.setText(mPhotoItem.getTitle());
        mHeadDesc.setText(mPhotoItem.getDescription());
        mHeadLikeCount.setText(String.valueOf(mPhotoItem.getLikeCount()));
        mHeadViewCount.setText(String.valueOf(mPhotoItem.getClickCount()));
        mHeadReplyCount.setText(String.valueOf(mPhotoItem.getReplyCount()));

    }

    private void refresh() {
        CourseDetailRequest request = new CourseDetailRequest.Builder().
                setId(String.valueOf(id)).setListener(refreshListener).
                setErrorListener(errorListener).build();

        RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                getActivity()).getRequestQueue();
        requestQueue.add(request);
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

        mShareArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shareMoreDialog == null) {
                    shareMoreDialog = new ShareMoreDialog(getActivity());
                }
                shareMoreDialog.setPhotoItem(mPhotoItem);
                shareMoreDialog.setShowType(ShareMoreDialog.TYPE_SHARE);
                shareMoreDialog.show();
            }
        });

        mCommentArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CommentListActivity.class);
                intent.putExtra(Constants.IntentKey.PHOTO_ITEM, mPhotoItem);
                startActivity(intent);
            }
        });

        mRewardArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomDialog dialog = new CustomDialog.Builder(getActivity()).
                        setLeftButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).setRightButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        mRewardTxt.setText(String.format("正向对方转入\n打赏随机金额"));
//                        isRewardEnd = false;
//                        fixedThreadPool.execute(new Runnable() {
//                            @Override
//                            public void run() {
//                                for (int i = 1; (i < 6 || !isRewardEnd) && !isRewardError; i++) {
//                                    try {
//                                        Thread.sleep(300);
//                                    } catch (InterruptedException e) {
//                                        e.printStackTrace();
//                                    }
//                                    mHandler.sendEmptyMessage(i % 4);
//                                }
//                                mHandler.sendEmptyMessage(-1);
//                            }
//                        });
//                        mRewardArea.setEnabled(false);
//                        RewardRequest request = new RewardRequest.Builder().
//                                setId(String.valueOf(id)).
//                                setListener(rewardListener).
//                                setErrorListener(new PSGodErrorListener(this) {
//                                    @Override
//                                    public void handleError(VolleyError error) {
////                                        mRewardArea.setEnabled(true);
//                                        isRewardError = true;
////                                      mRewardTxt.setText(String.format("支付出现问题\n请重试"));
//                                    }
//                                }).build();
//                        RequestQueue requestQueue = PSGodRequestQueue.getInstance(
//                                getActivity()).getRequestQueue();
//                        requestQueue.add(request);
                        Utils.showProgressDialog(getActivity());
                        RewardRequest2 request = new RewardRequest2.Builder().
                                setId(String.valueOf(id)).
                                setListener(rewardListener2).
                                setErrorListener(new PSGodErrorListener(this) {
                                    @Override
                                    public void handleError(VolleyError error) {
                                        Utils.hideProgressDialog();
//                                        mRewardArea.setEnabled(true);
//                                        isRewardError = true;
                                        mRewardTxt.setText(String.format("打赏失败"));
                                    }
                                }).build();
                        RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                                getActivity()).getRequestQueue();
                        requestQueue.add(request);
                    }
                }).setMessage("是否要打赏该教程？").create();
                dialog.show();

            }
        });
    }

    Response.Listener<JSONObject> rewardListener2 = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            if (response != null) {
                Intent intent = new Intent(mContext, PaymentActivity.class);
                intent.putExtra(PaymentActivity.EXTRA_CHARGE,
                        response.toString());
                startActivityForResult(intent, REQUEST_CODE_PAYMENT);
            }
        }
    };

    Response.Listener<Reward> rewardListener = new Response.Listener<Reward>() {
        @Override
        public void onResponse(Reward response) {
            if (response != null) {
                reward = response;
                if (response.getType() == 1) {
                    amount = response.getAmount();
                    isRewardEnd = true;
//                mRewardArea.setEnabled(true);
//                refresh();
                } else {
                    isRewardEnd = true;
                    Utils.showProgressDialog(getActivity());
                    PayErrorDialog payErrorDialog = new PayErrorDialog(getActivity());
                    payErrorDialog.setReward(response);
                    payErrorDialog.setRequestCode(REQUEST_CODE_PAYMENT);
                    payErrorDialog.show();
                }
            }
        }
    };

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
            refresh();
        }
    }

    private static class ViewHolder {
        private View mParentView;
        private View mView;
        private PullToRefreshListView mListView;
    }

    Response.Listener<PhotoItem> refreshListener = new Response.Listener<PhotoItem>() {
        @Override
        public void onResponse(PhotoItem response) {
            mViewHolder.mListView.onRefreshComplete();
            if (progressingDialog != null && progressingDialog.isShowing()) {
                progressingDialog.dismiss();
            }
            boolean isRefresh = mPhotoItem == null ?
                    true : mPhotoItem.getHasUnlocked() != response.getHasUnlocked();
            if (response != null) {
                mPhotoItem = response;
            }
            initView(isRefresh);

            CommentListRequest commentRequest = new CommentListRequest.Builder()
                    .setPid(mPhotoItem.getPid()).setType(mPhotoItem.getType())
                    .setCommentId(id).setListener(commentListener)
                    .setErrorListener(errorListener).build();
            RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                    getActivity()).getRequestQueue();
            requestQueue.add(commentRequest);
        }
    };

    Response.Listener<CommentListRequest.CommentListWrapper> commentListener =
            new Response.Listener<CommentListRequest.CommentListWrapper>() {
                @Override
                public void onResponse(CommentListRequest.CommentListWrapper response) {
                    if (mComments == null) {
                        mComments = new ArrayList<>();
                    }
                    mComments.clear();
                    mComments.addAll(response.recentCommentList);
                    mAdapter.notifyDataSetChanged();
                }
            };

    PSGodErrorListener errorListener = new PSGodErrorListener(this) {
        @Override
        public void handleError(VolleyError error) {
            mViewHolder.mListView.onRefreshComplete();
            if (progressingDialog != null && progressingDialog.isShowing()) {
                progressingDialog.dismiss();
            }
            mFooterView.setVisibility(View.GONE);
        }
    };

    //支付成功回调
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        Utils.hideProgressDialog();
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getExtras().getString("pay_result");
                /** 处理返回值
                 * "success" - payment succeed
                 * "fail"    - payment failed
                 * "cancel"  - user canceld
                 * "invalid" - payment plugin not installed
                 */
                if (result.equals("success")) {
//                    RewardRequest request = new RewardRequest.Builder().
//                            setId(String.valueOf(id)).
//                            setListener(new Response.Listener<Reward>() {
//                                @Override
//                                public void onResponse(Reward response) {
//                                    if (response != null) {
//                                        reward = response;
//                                        if (response.getType() == 1) {
//                                            mHandler.sendEmptyMessage(-1);
//                                            CustomToast.show(getActivity(), "支付成功", Toast.LENGTH_SHORT);
//                                        } else {
//                                            PayErrorDialog payErrorDialog = new PayErrorDialog(getActivity());
//                                            payErrorDialog.setReward(response);
//                                            payErrorDialog.setRequestCode(REQUEST_CODE_PAYMENT);
//                                            payErrorDialog.show();
//                                        }
//                                    }
//                                }
//                            }).
//                            setAmount(amount).
//                            setErrorListener(new PSGodErrorListener(this) {
//                                @Override
//                                public void handleError(VolleyError error) {
//                                    mRewardArea.setEnabled(true);
//                                    mRewardTxt.setText(String.format("支付出现问题\n请重试"));
//                                }
//                            }).build();
//                    RequestQueue requestQueue = PSGodRequestQueue.getInstance(
//                            getActivity()).getRequestQueue();
//                    requestQueue.add(request);
                    mRewardTxt.setText(String.format("打赏成功"));
                    refresh();
                    CustomToast.show(getActivity(), "支付成功", Toast.LENGTH_SHORT);

                } else if (result.equals("fail")) {
                    mRewardTxt.setText(String.format("打赏失败"));
                    CustomToast.show(getActivity(), "支付失败", Toast.LENGTH_SHORT);
                } else if (result.equals("cancel")) {
                    mRewardTxt.setText(String.format("取消打赏"));
                    CustomToast.show(getActivity(), "取消支付", Toast.LENGTH_SHORT);
                } else if (result.equals("invalid")) {
                    mRewardTxt.setText(String.format("打赏失败"));
                    CustomToast.show(getActivity(), "未安装微信，无法支付", Toast.LENGTH_SHORT);
                }
//
//                String errorMsg = data.getExtras().getString("error_msg"); // 错误信息
//                String extraMsg = data.getExtras().getString("extra_msg"); // 错误信息
//                showMsg(result, errorMsg, extraMsg);
            }
        }
    }

    private boolean startRefresh = false;
//    private int refreshType = 0;

    private void muitRefresh(){
        if(startRefresh){
            refresh();
        }else {
            startRefresh = true;
            for (int i = 0; i < 3; i++) {
                switch (i){
                    case 0:
                        refresh();
                        break;
                    case 1:
                        fixedThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                mHandler.sendEmptyMessage(0);
                            }
                        });
                        break;
                    case 2:
                        fixedThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                startRefresh = false;
                                mHandler.sendEmptyMessage(0);
                            }
                        });
                        break;
                }
            }
        }
    }


    @Override
    public boolean handleMessage(Message message) {
        if(message.what >= 0 && message.what < 10){
            switch (message.what){
                case 0:
                    refresh();
                    break;
            }
        }else if (message.what >= 10) {
            StringBuilder sb = new StringBuilder();
            sb.append("正向对方转入\n打赏随机金额");
            for (int i = 0; i < message.what; i++) {
                sb.append(".");
            }
            mRewardTxt.setText(sb.toString());
        } else {
            if (amount == 0) {
                mRewardTxt.setText(String.format("打赏失败"));
                mRewardArea.setEnabled(true);
            } else {
                mRewardTxt.setText(String.format("已向对方转入\n打赏随机金额%s元",
                        String.format("%.2f", amount)));
                amount = 0;
                mRewardArea.setEnabled(true);
                refresh();
            }
        }
        return true;
    }
}
