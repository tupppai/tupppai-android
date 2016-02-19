package com.psgod.ui.view;

import android.content.Context;
import android.graphics.Color;
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
import com.psgod.network.request.ActionLoveRequest;
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
        if (likeImg != null && likeTxt != null) {
            updateLikeView();
        }
    }

    public static final int LIKE_IMAGE_ID[] = {R.mipmap.ic_like, R.mipmap.like1
            , R.mipmap.like2, R.mipmap.like3};
    public static final int LIKE_TXT_BACK_COLOR[] = {R.drawable.shape_like_count, R.drawable.shape_like_count1
            , R.drawable.shape_like_count2, R.drawable.shape_like_count3};
    public static final String LIKE_TXT_COLOR[] = {"#ffffff", "#ffffff", "#ffffff"
            , "#000000"};

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_like, null);
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
                if (mPhotoItem == null) {
                    return;
                }
//                if (!mPhotoItem.isLiked()) {
//                    AnimatorSet animatorZoomSet = new AnimatorSet();
//                    animatorZoomSet.setDuration(800);
//
//                    ObjectAnimator zoomX = ObjectAnimator.ofFloat(likeImg,
//                            "scaleX", 1f, 1.5f, 1f);
//                    ObjectAnimator zoomY = ObjectAnimator.ofFloat(likeImg,
//                            "scaleY", 1f, 1.5f, 1f);
//                    ObjectAnimator zoomoX = ObjectAnimator.ofFloat(LikeView.this,
//                            "scaleX", 1f, 1.5f, 1f);
//                    ObjectAnimator zoomoY = ObjectAnimator.ofFloat(LikeView.this,
//                            "scaleY", 1f, 1.5f, 1f);
//                    animatorZoomSet.playTogether(zoomX, zoomY,zoomoX,zoomoY);
//                    animatorZoomSet.start();
//                }

                // 点赞网络请求
                setClickable(false);
                ActionLoveRequest.Builder builder = new ActionLoveRequest.Builder()
                        .setPid(mPhotoItem.getPid())
                        .setListener(mActionLikeListener).setNum(mPhotoItem.getLoveCount())
                        .setErrorListener(mActionLikeErrorListener);
                ActionLoveRequest request = builder.build();
                RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                        getContext()).getRequestQueue();
                requestQueue.add(request);
            }
        });

        if (mPhotoItem != null) {
            updateLikeView();
        }

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ActionLoveRequest.Builder builder = new ActionLoveRequest.Builder()
                        .setPid(mPhotoItem.getPid())
                        .setListener(mActionLikeClearListener).setNum(-1)
                        .setErrorListener(mActionLikeErrorListener);
                ActionLoveRequest request = builder.build();
                RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                        getContext()).getRequestQueue();
                requestQueue.add(request);
                return true;
            }
        });
    }

    private Response.Listener<Boolean> mActionLikeClearListener = new Response.Listener<Boolean>() {
        @Override
        public void onResponse(Boolean response) {
            if (response) {
                mPhotoItem.setLikeCount(
                        mPhotoItem.getLikeCount() - mPhotoItem.getLoveCount());
                mPhotoItem.setLoveCount(0);
//                mPhotoItem.setIsLiked(mPhotoItem.isLiked() ? false : true);
                updateLikeView();
            }
            setClickable(true);
            if (onLikeCheckListener != null) {
                onLikeCheckListener.onLikeCheckListener(mPhotoItem);
            }
        }
    };

    private Response.Listener<Boolean> mActionLikeListener = new Response.Listener<Boolean>() {
        @Override
        public void onResponse(Boolean response) {
            if (response) {
                mPhotoItem.setLikeCount(mPhotoItem.getLoveCount() == 3 ?
                        mPhotoItem.getLikeCount() - 3 : mPhotoItem.getLikeCount() + 1);
                mPhotoItem.setLoveCount(mPhotoItem.getLoveCount() == 3 ?
                        0 : mPhotoItem.getLoveCount() + 1);
//                mPhotoItem.setIsLiked(mPhotoItem.isLiked() ? false : true);
                updateLikeView();
            }
            setClickable(true);
            if (onLikeCheckListener != null) {
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
//        if (mPhotoItem.isLiked()) {
//            likeImg.setImageResource(R.drawable.ic_home_like_selected);
//            if(Build.VERSION.SDK_INT >= 16) {
//                likeTxt.setBackground(getResources().getDrawable(
//                        R.drawable.shape_like_count));
//            }
//        } else {
//            likeImg.setImageResource(R.drawable.ic_home_like_normal);
//            if(Build.VERSION.SDK_INT >= 16) {
//                likeTxt.setBackground(getResources().getDrawable(
//                        R.drawable.shape_unlike_count));
//            }
//        }
        likeImg.setImageResource(LIKE_IMAGE_ID[mPhotoItem.getLoveCount()>3?0:mPhotoItem.getLoveCount()]);
        String textLikeCount = Utils.getCountDisplayText(mPhotoItem
                .getLikeCount());
        likeTxt.setText(textLikeCount);
        likeTxt.setBackgroundResource(LIKE_TXT_BACK_COLOR[mPhotoItem.getLoveCount()>3?0:mPhotoItem.getLoveCount()]);
        likeTxt.setTextColor(Color.parseColor(LIKE_TXT_COLOR[mPhotoItem.getLoveCount()>3?0:mPhotoItem.getLoveCount()]));
    }

    private OnLikeCheckListener onLikeCheckListener;

    public void setOnLikeCheckListener(OnLikeCheckListener onLikeCheckListener) {
        this.onLikeCheckListener = onLikeCheckListener;
    }

    public interface OnLikeCheckListener {
        void onLikeCheckListener(PhotoItem photoItem);
    }

}
