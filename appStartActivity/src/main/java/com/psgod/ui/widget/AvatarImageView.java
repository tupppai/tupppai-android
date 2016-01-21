package com.psgod.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.psgod.Constants;
import com.psgod.PsGodImageLoader;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.User;
import com.psgod.ui.activity.UserProfileActivity;
import com.psgod.ui.view.CircleImageView;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class AvatarImageView extends RelativeLayout implements PsGodImageLoader.ImageArea {
    private boolean isVip = false;
    private CircleImageView mAvatarImage;
    private ImageView mVipicon;
    private Long mUserId;


    public AvatarImageView(Context context) {
        super(context);
        init();
    }

    public AvatarImageView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    public AvatarImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.widget_avatar_layout, null);
        addView(view);
        mAvatarImage = (CircleImageView) view.findViewById(R.id.avatar_imgview);
        mVipicon = (ImageView) view.findViewById(R.id.avatar_vip_icon);

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUserId != null) {
                    Intent intent = new Intent(getContext(),
                            UserProfileActivity.class);
                    intent.putExtra(Constants.IntentKey.USER_ID, mUserId);
                    getContext().startActivity(intent);
                }
            }
        });

        if (isVip) {
            mVipicon.setVisibility(VISIBLE);
        } else {
            mVipicon.setVisibility(GONE);
        }
    }

    private boolean isRealWidth = false;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!isRealWidth && isVip) {
            RelativeLayout.LayoutParams imageParams = (LayoutParams) mAvatarImage.getLayoutParams();
            if (imageParams != null) {
                imageParams.width = getMeasuredWidth();
                imageParams.height = getMeasuredWidth();
                mAvatarImage.setLayoutParams(imageParams);
            }
            int vWidth = getMeasuredWidth() / 3;
            int vHeight = getMeasuredWidth() / 3;
            RelativeLayout.LayoutParams vParams = (LayoutParams) mVipicon.getLayoutParams();
            if (vParams != null) {
                vParams.width = vWidth;
                vParams.height = vHeight;
                mVipicon.setLayoutParams(vParams);
            }
//            ViewGroup.LayoutParams params = getLayoutParams();
//            if (params != null) {
//                params.width = getMeasuredWidth() + vWidth;
//                setLayoutParams(params);
//            }
            isRealWidth = true;
        }
    }

//    private void initSize(View view) {
//        ViewGroup.LayoutParams groupLayoutParams = this.getLayoutParams();
//        FrameLayout.LayoutParams mlayoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
//                LayoutParams.MATCH_PARENT);
//        mAvatarImage.setLayoutParams(mlayoutParams);
//        mAvatarImage.setPadding(0, 0, Utils.dpToPx(mContext, 5), 0);
//
//        FrameLayout.LayoutParams mIconLayoutParams = new FrameLayout.LayoutParams(Utils.dpToPx(mContext, 5),
//                Utils.dpToPx(mContext, 5));
//        mIconLayoutParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
//        mVipicon.setLayoutParams(mIconLayoutParams);
//    }

//    public void setAvatarUrl(String avatarUrl) {
//        imageLoader.displayImage(avatarUrl, mAvatarImage,
//                mAvatarOptions, mAnimateFirstListener);
//    }

    public void setUser(User user) {
        this.mUserId = user.getUid();
        this.isVip = user.isStar();

        if (mVipicon != null) {
            if (isVip) {
                mVipicon.setVisibility(VISIBLE);
            } else {
                mVipicon.setVisibility(GONE);
            }
            postInvalidate();
        }
    }

    public void setIsVip(boolean isVip) {
        this.isVip = isVip;
    }

    public Long getmUserId() {
        return mUserId;
    }

    public boolean isVip() {
        return isVip;
    }

    @Override
    public ImageView getImage() {
        return mAvatarImage;
    }
}
