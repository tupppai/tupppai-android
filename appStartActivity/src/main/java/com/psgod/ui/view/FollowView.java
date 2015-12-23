package com.psgod.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
public class FollowView extends Button {

    private Context mContext;
    private PhotoItem mPhotoItem;
    public static final int TYPE_UNFOLLOW = 0;
    public static final int TYPE_FOLLOW = 1;
    public static final int TYPE_FOLLOW_EACH = 2;

    public int state = TYPE_UNFOLLOW;

    public FollowView(Context context) {
        super(context);
        mContext = context;
    }

    public FollowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        this.setOnClickListener(clickListener);
    }

    public void setPhotoItem(PhotoItem photoItem) {
        this.mPhotoItem = photoItem;
        if (LoginUser.getInstance().getUid() == mPhotoItem.getUid()) {
            setVisibility(INVISIBLE);
        } else {
            setVisibility(VISIBLE);
        }

        if (mPhotoItem.isFollowing() == true && mPhotoItem.isFollowed() == false) {
            state = TYPE_FOLLOW;
        } else if (mPhotoItem.isFollowing() == true && mPhotoItem.isFollowed() == true) {
            state = TYPE_FOLLOW_EACH;
        } else if (mPhotoItem.isFollowing() == false){
            state = TYPE_UNFOLLOW;
        }

        setFollowState(state);
    }

    public void setFollowState(int state) {
        this.state = state;
        updateFollowView();
    }

    public void updateFollowView() {
        FollowView.this.setTextSize(10);
        switch(state) {
            case TYPE_UNFOLLOW:
                setBackgroundResource(R.drawable.btn_unfollow);
                setText("+ 关注");
                break;
            case TYPE_FOLLOW:
                setBackgroundResource(R.drawable.btn_follow);
                setText("已关注");
                break;
            case TYPE_FOLLOW_EACH:
                setBackgroundResource(R.drawable.btn_follow);
                setText("互相关注");
                break;
        }
    }

    OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mPhotoItem == null) {
                return;
            }
            setClickable(false);

            int mType = (state == TYPE_UNFOLLOW) ? 0 : 1;
            ActionFollowRequest.Builder builder = new ActionFollowRequest.Builder()
                    .setType(mType).setUid(mPhotoItem.getUid())
                    .setErrorListener(mActionFollowErrorListener)
                    .setListener(mActionFollowListener);

            ActionFollowRequest request = builder.build();
            RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                    getContext()).getRequestQueue();
            requestQueue.add(request);
        }
    };

    private Response.Listener<Boolean> mActionFollowListener = new Response.Listener<Boolean>() {
        @Override
        public void onResponse(Boolean response) {
            if (response) {
                if (state == TYPE_FOLLOW
                        || state == TYPE_FOLLOW_EACH) {
                    state = TYPE_UNFOLLOW;
                    setFollowState(state);
                    Toast.makeText(mContext, "取消关注成功", Toast.LENGTH_SHORT)
                            .show();
                } else if (state == TYPE_UNFOLLOW) {
                    if (mPhotoItem.isFollowed() == true) {
                        state = TYPE_FOLLOW_EACH;
                    } else {
                        state = TYPE_FOLLOW;
                    }

                    setFollowState(state);
                    Toast.makeText(mContext, "关注成功", Toast.LENGTH_SHORT).show();
                }

                FollowView.this.setClickable(true);
            }

        }
    };

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

    public void setActionFollowErrorListener(PSGodErrorListener mActionFollowErrorListener) {
        this.mActionFollowErrorListener = mActionFollowErrorListener;
    }

    public void setActionFollowListener(Response.Listener<Boolean> mActionFollowListener) {
        this.mActionFollowListener = mActionFollowListener;
    }

    private PSGodErrorListener mActionFollowErrorListener = new PSGodErrorListener(
            ActionCollectionRequest.class.getSimpleName()) {
        @Override
        public void handleError(VolleyError error) {
            setClickable(true);
        }
    };

    public PhotoItemView.OnFollowChangeListener followChangeListener;

    public void setOnFollowChangeListener(PhotoItemView.OnFollowChangeListener followChangeListener) {
        this.followChangeListener = followChangeListener;
    }
}
