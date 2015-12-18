package com.psgod.ui.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.ActionLikeRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;

/**
 * Created by Administrator on 2015/12/1 0001.
 */
public class LikeView extends RelativeLayout {
    public LikeView(Context context) {
        super(context);
        init();
    }

    public LikeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LikeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private ImageView likeImg;
    private TextView likeTxt;
    private PhotoItem mPhotoItem;

    public void setmPhotoItem(PhotoItem mPhotoItem) {
        this.mPhotoItem = mPhotoItem;
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_like,null);
        addView(view);
        initView(view);
    }

    private void initView(View view) {
        setClipChildren(false);
        likeImg = (ImageView) view.findViewById(R.id.view_like_img);
        likeTxt = (TextView) view.findViewById(R.id.view_like_txt);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 设置点赞的缩放动画,没点赞过进入动画
                if(mPhotoItem == null){
                    return;
                }
                if (!mPhotoItem.isLiked()) {
                    AnimatorSet animatorZoomSet = new AnimatorSet();
                    animatorZoomSet.setDuration(800);

                    ObjectAnimator zoomX = ObjectAnimator.ofFloat(likeImg,
                            "scaleX", 1f, 1.5f, 1f);
                    ObjectAnimator zoomY = ObjectAnimator.ofFloat(likeImg,
                            "scaleY", 1f, 1.5f, 1f);
                    ObjectAnimator zoomoX = ObjectAnimator.ofFloat(LikeView.this,
                            "scaleX", 1f, 1.5f, 1f);
                    ObjectAnimator zoomoY = ObjectAnimator.ofFloat(LikeView.this,
                            "scaleY", 1f, 1.5f, 1f);
                    animatorZoomSet.playTogether(zoomX, zoomY,zoomoX,zoomoY);
                    animatorZoomSet.start();
                }

                // 点赞网络请求
                setClickable(false);

                int mStatus = mPhotoItem.isLiked() ? 0 : 1;
                ActionLikeRequest.Builder builder = new ActionLikeRequest.Builder()
                        .setPid(mPhotoItem.getPid())
                        .setType(mPhotoItem.getType())
                        .setListener(mActionLikeListener).setStatus(mStatus)
                        .setErrorListener(mActionLikeErrorListener);
                ActionLikeRequest request = builder.build();
                RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                        getContext()).getRequestQueue();
                requestQueue.add(request);
            }
        });
    }

    private Response.Listener<Boolean> mActionLikeListener = new Response.Listener<Boolean>() {
        @Override
        public void onResponse(Boolean response) {
            if (response) {
                mPhotoItem.setLikeCount(mPhotoItem.isLiked() ? mPhotoItem
                        .getLikeCount() - 1 : mPhotoItem.getLikeCount() + 1);
                mPhotoItem.setIsLiked(mPhotoItem.isLiked() ? false : true);
                updateLikeView();
            }
           setClickable(true);
            if(onLikeCheckListener != null){
                onLikeCheckListener.onLikeCheckListener(mPhotoItem);
            }
        }
    };

    // 点赞失败回调函数
    private PSGodErrorListener mActionLikeErrorListener = new PSGodErrorListener(
            ActionLikeRequest.class.getSimpleName()) {
        @Override
        public void handleError(VolleyError error) {
            setClickable(true);
        }
    };

    public void updateLikeView() {
        if (mPhotoItem.isLiked()) {
            likeImg.setImageResource(R.drawable.ic_home_like_selected);
            if(Build.VERSION.SDK_INT >= 16) {
                likeTxt.setBackground(getResources().getDrawable(
                        R.drawable.shape_like_count));
            }
        } else {
            likeImg.setImageResource(R.drawable.ic_home_like_normal);
            if(Build.VERSION.SDK_INT >= 16) {
                likeTxt.setBackground(getResources().getDrawable(
                        R.drawable.shape_unlike_count));
            }
        }
        String textLikeCount = Utils.getCountDisplayText(mPhotoItem
                .getLikeCount());
        likeTxt.setText(textLikeCount);
    }

    private OnLikeCheckListener onLikeCheckListener;

    public void setOnLikeCheckListener(OnLikeCheckListener onLikeCheckListener) {
        this.onLikeCheckListener = onLikeCheckListener;
    }

    public interface  OnLikeCheckListener{
        void onLikeCheckListener(PhotoItem photoItem);
    }

}
