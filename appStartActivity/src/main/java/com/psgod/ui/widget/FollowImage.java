package com.psgod.ui.widget;

import java.util.HashMap;
import java.util.Map;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.LoginUser;
import com.psgod.model.PhotoItem;
import com.psgod.model.User;
import com.psgod.network.request.ActionFollowRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.ui.view.PhotoItemView;

/**
 * 关注和取消关注按钮
 * 当前准确可用
 *
 * @author Rayal
 */
public class FollowImage extends ImageView {
    private final static String TAG = FollowImage.class.getSimpleName();

    public static final int TYPE_FOLLOW = 0;
    public static final int TYPE_UNFOLLOW = 1;
    // 互相关注状态
    public static final int TYPE_FOLLOW_EACH = 2;

    // 判断是否需要在关注后隐藏图标
    private boolean isHideFollow = true;

    private Context mContext;
    // 状态和对应icon之间关系
    private Map<FollowState, FollowButtonAttribute> mBtnAttrs = new HashMap<FollowState, FollowButtonAttribute>();

    // 关注按钮状态
    private FollowState state = FollowState.UnFollow;
    // 对应User
    private User mUser;
    // 对应的PhotoItem
    private PhotoItem mPhotoItem;

    // 搜索，关注列表，首页列表 三个地方需要用到这个view 需要下面三个参数信息
    private long mUid = 0;
    private int mIsFan = 0;
    private int mIsFollow = 0;

    public static enum FollowState {
        // 未关注 关注状态 互相关注
        UnFollow, Following, FollowingEach;
    }

    public FollowImage(Context context) {
        super(context);
        mContext = context;
    }

    public FollowImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        Resources resources = context.getResources();

//		int width = resources.getDimensionPixelSize(R.dimen.follow_btn_width);
//		int height = resources.getDimensionPixelSize(R.dimen.follow_btn_height);
//		this.setWidth(width);
//		this.setHeight(height);

        FollowButtonAttribute unfollowAttr = new FollowButtonAttribute();
        unfollowAttr.srcDrawable = resources.getDrawable(R.mipmap.btn_addfollow);

        // follow 和 unfollow的命名弄反了╮(╯▽╰)╭
        FollowButtonAttribute followingAttr = new FollowButtonAttribute();
        followingAttr.srcDrawable = resources
                .getDrawable(R.mipmap.btn_home_followed);

        FollowButtonAttribute followeachAttr = new FollowButtonAttribute();
        followeachAttr.srcDrawable = resources
                .getDrawable(R.mipmap.btn_fri);

        mBtnAttrs.put(FollowState.UnFollow, unfollowAttr);
        mBtnAttrs.put(FollowState.Following, followingAttr);
        mBtnAttrs.put(FollowState.FollowingEach, followeachAttr);

        this.setOnClickListener(mOnClickListener);
    }

    // 设置对应的user
    public void setUser(User user) {
        this.mUser = user;

        this.mUid = user.getUid();
        this.mIsFollow = user.isFollowing();
        this.mIsFan = user.isFollowed();

        initState();
    }

    // 设置对应的PhotoItem
    public void setPhotoItem(PhotoItem photoItem) {
        this.mPhotoItem = photoItem;

        this.mUid = photoItem.getUid();
        this.mIsFollow = (photoItem.isFollowing() == true) ? 1 : 0;
        this.mIsFan = (photoItem.isFollowed() == true) ? 1 : 0;

        initState();
    }

    // 设置对应的信息
    public void setUser(long uid, int isFollow, int isFan) {

        this.mUid = uid;
        this.mIsFollow = isFollow;
        this.mIsFan = isFan;

        initState();
    }

    private void initState() {
        if (LoginUser.getInstance().getUid() == this.mUid) {
            setVisibility(INVISIBLE);
        } else {
            setVisibility(VISIBLE);
        }

        if (mIsFollow == 1 && mIsFan == 0) {
            state = FollowState.Following;
        } else if (mIsFollow == 0) {
            state = FollowState.UnFollow;
        } else if (mIsFan == 1 && mIsFollow == 1) {
            // 互相关注状态
            state = FollowState.FollowingEach;
        }
        this.setFollowButtonState(state);
    }

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            // 正在关注 包括互相关注
            if (state == FollowState.Following
                    || state == FollowState.FollowingEach) {
                FollowImage.this.setClickable(false);

                ActionFollowRequest.Builder builder = new ActionFollowRequest.Builder()
                        .setType(TYPE_UNFOLLOW).setUid(mUid)
                        .setErrorListener(errorListener)
                        .setListener(actionFollowListener);

                ActionFollowRequest request = builder.build();
                request.setTag(TAG);
                RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                        mContext).getRequestQueue();
                requestQueue.add(request);
            } else if (state == FollowState.UnFollow) {
                FollowImage.this.setClickable(false);

                ActionFollowRequest.Builder builder = new ActionFollowRequest.Builder()
                        .setType(TYPE_FOLLOW).setUid(mUid)
                        .setErrorListener(errorListener)
                        .setListener(actionFollowListener);

                ActionFollowRequest request = builder.build();
                request.setTag(TAG);
                RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                        mContext).getRequestQueue();
                requestQueue.add(request);
            }
        }
    };

    // 关注 取消关注 listener
    private Listener<Boolean> actionFollowListener = new Listener<Boolean>() {
        @Override
        public void onResponse(Boolean response) {
            FollowImage.this.setClickable(true);
            if (response == true) {
                if (state == FollowState.Following
                        || state == FollowState.FollowingEach) {
                    state = FollowState.UnFollow;
                    setFollowButtonState(state);
                    Toast.makeText(mContext, "取消关注成功", Toast.LENGTH_SHORT)
                            .show();
                    if (onFollowChangeListener != null) {
                        onFollowChangeListener.onFocusChange(mUid, ((state == FollowState.Following
                                || state == FollowState.FollowingEach) ? false : true), mPhotoItem.getPid());
                    }

                } else if (state == FollowState.UnFollow) {
                    if (isHideFollow) {
                        if (mIsFan == 1) {
                            state = FollowState.FollowingEach;
                        } else {
                            state = FollowState.Following;
                        }
                        final AnimatorSet anim = new AnimatorSet();
                        anim.setDuration(1300);
                        ValueAnimator xAnim = ValueAnimator.ofInt(100, 0);
                        xAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                Integer value = (Integer) valueAnimator.getAnimatedValue();
                                setAlpha((float) value / 100f);
                            }
                        });
                        setClickable(false);
                        xAnim.addListener(animListener);
                        anim.playTogether(xAnim);
                        anim.start();

                        Toast.makeText(mContext, "关注成功", Toast.LENGTH_SHORT).show();
                    } else {
                        if (mIsFan == 1) {
                            state = FollowState.FollowingEach;
                        } else {
                            state = FollowState.Following;
                        }
                        setFollowButtonState(state);
                        Toast.makeText(mContext, "关注成功", Toast.LENGTH_SHORT).show();
                        if (onFollowChangeListener != null) {
                            onFollowChangeListener.onFocusChange(mUid, ((state == FollowState.Following
                                    || state == FollowState.FollowingEach) ? false : true), mPhotoItem.getPid());
                        }

                    }
                }
                if(mPhotoItem != null) {
                    mPhotoItem.setmIsFollow(!state.equals(FollowState.UnFollow));
                }

                // 关注用户有变化 需要自动刷新我的关注页面
                Constants.IS_FOLLOW_NEW_USER = true;
            }
        }
    };

    private PSGodErrorListener errorListener = new PSGodErrorListener(
            FollowImage.class.getSimpleName()) {
        @Override
        public void handleError(VolleyError error) {
            // TODO Auto-generated method stub
        }
    };

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void updateButton() {
        if (isHideFollow) {
            if (state == FollowState.UnFollow) {
                this.setVisibility(VISIBLE);
                setAlpha(1f);
                FollowButtonAttribute attr = mBtnAttrs.get(state);
                this.setImageDrawable(attr.srcDrawable);
            } else {
                setAlpha(1f);
                setVisibility(GONE);
            }
        } else {
            FollowButtonAttribute attr = mBtnAttrs.get(state);
            if (state == FollowState.UnFollow) {
                this.setImageResource(R.mipmap.btn_home_follow);
            } else {
                this.setImageDrawable(attr.srcDrawable);
            }
        }

    }

    // 根据state设置followButton 的状态
    public void setFollowButtonState(FollowState state) {
        this.state = state;
        updateButton();
    }

    public FollowState getFollowState() {
        return this.state;
    }

    private static class FollowButtonAttribute {
        Drawable srcDrawable;
    }

    private OnFollowChangeListener onFollowChangeListener;

    public void setOnFollowChangeListener(OnFollowChangeListener onFollowChangeListener) {
        this.onFollowChangeListener = onFollowChangeListener;
    }

    // 关注接口回调
    public interface OnFollowChangeListener {
        void onFocusChange(long uid, boolean focusStatus, long pid);
    }

    public void setIsHideFollow(boolean isHideFollow) {
        this.isHideFollow = isHideFollow;
    }

    Animator.AnimatorListener animListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            setClickable(true);
            setFollowButtonState(state);
            if (onFollowChangeListener != null) {
                onFollowChangeListener.onFocusChange(mUid, ((state == FollowState.Following
                        || state == FollowState.FollowingEach) ? false : true), mPhotoItem.getPid());
            }

        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };
}
