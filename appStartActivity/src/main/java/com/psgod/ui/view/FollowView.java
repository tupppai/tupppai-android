package com.psgod.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.psgod.R;
import com.psgod.model.LoginUser;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.ActionCollectionRequest;
import com.psgod.network.request.ActionFollowRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;

/**
 * Created by Administrator on 2015/12/2 0002.
 */
public class FollowView extends TextView {
    public FollowView(Context context) {
        super(context);
        init();
    }

    public FollowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FollowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private PhotoItem mPhotoItem;
    public static final int TYPE_FOLLOW = 1;
    public static final int TYPE_UNFOLLOW = 0;

    public void setPhotoItem(PhotoItem photoItem) {
        this.mPhotoItem = photoItem;
        if (LoginUser.getInstance().getUid() == mPhotoItem.getUid()){
            setVisibility(INVISIBLE);
        }else{
            setVisibility(VISIBLE);
        }
    }

    private void init() {
        initView();
        initListener();
    }

    private void initListener() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPhotoItem == null) {
                    return;
                }
                setClickable(false);

                int mType = mPhotoItem.isFollowed() ? TYPE_FOLLOW
                        : TYPE_UNFOLLOW;

                ActionFollowRequest.Builder builder = new ActionFollowRequest.Builder()
                        .setType(mType).setUid(mPhotoItem.getUid())
                        .setErrorListener(mActionFollowErrorListener)
                        .setListener(mActionFollowListener);

                ActionFollowRequest request = builder.build();
                RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                        getContext()).getRequestQueue();
                requestQueue.add(request);
            }
        });
    }


    private int followResId = -1;
    private int unfollowResId = -1;

    public void setFollowResId(int followResId) {
        this.followResId = followResId;
    }

    public void setUnfollowResId(int unfollowResId) {
        this.unfollowResId = unfollowResId;
    }

    public void setActionFollowErrorListener(PSGodErrorListener mActionFollowErrorListener) {
        this.mActionFollowErrorListener = mActionFollowErrorListener;
    }

    public void setActionFollowListener(Response.Listener<Boolean> mActionFollowListener) {
        this.mActionFollowListener = mActionFollowListener;
    }

    private void initView() {
        setBackground(getResources().getDrawable(R.drawable.btn_unfollow));
        setText("+ 关注");
    }

    public void updateFollowView() {
        if (mPhotoItem != null && mPhotoItem.isFollowed()) {
            int id = 0;
            if (followResId == -1) {
                id = R.drawable.btn_follow;
            } else {
                id = followResId;
            }
            setBackgroundResource(id);
            setText("已关注");
        } else {
            int id = 0;
            if (unfollowResId == -1) {
                id = R.drawable.btn_unfollow;
            } else {
                id = unfollowResId;
            }
            setBackgroundResource(id);
            setText("+ 关注");
        }
    }

    private PSGodErrorListener mActionFollowErrorListener = new PSGodErrorListener(
            ActionCollectionRequest.class.getSimpleName()) {
        @Override
        public void handleError(VolleyError error) {
            setClickable(true);
        }
    };

    private Response.Listener<Boolean> mActionFollowListener = new Response.Listener<Boolean>() {
        @Override
        public void onResponse(Boolean response) {
            if (response) {
                mPhotoItem
                        .setIsFollowed(mPhotoItem.isFollowed() ? false : true);
                updateFollowView();
                if(followChangeListener != null){
                    followChangeListener.onFocusChange(mPhotoItem.getUid(),
                            mPhotoItem.isFollowed());
                }
            }
            setClickable(true);

        }
    };


    public PhotoItemView.OnFollowChangeListener followChangeListener;

    public void setOnFollowChangeListener(PhotoItemView.OnFollowChangeListener followChangeListener) {
        this.followChangeListener = followChangeListener;
    }
}
