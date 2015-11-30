package com.psgod.ui.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.psgod.BitmapUtils;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.PhotoItem;
import com.psgod.ui.adapter.ViewPagerAdapter;
import com.psgod.ui.widget.AvatarImageView;
import com.psgod.ui.widget.StopViewPager;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/11/27 0027.
 */
public class CarouselPhotoDetailView extends RelativeLayout {

    private Context mContext;

    public CarouselPhotoDetailView(Context context) {
        super(context);
        init();
    }

    public CarouselPhotoDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CarouselPhotoDetailView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public CarouselPhotoDetailView(Context context, PhotoItem photoItem) {
        super(context);
        mPhotoItem = photoItem;
        init();
    }

    private void init() {
        mContext = getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_carousel_photo_detail, null);
        addView(view);
        initView(view);
        initListener(view);
    }

    private ViewPager vp;
    private PhotoItem mPhotoItem;

    private RelativeLayout mScroll;
    private RelativeLayout mCover;

    private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS;
    private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

    public void setVp(ViewPager vp) {
        this.vp = vp;
    }

    private void initView(View view) {
        mScroll = (RelativeLayout) view.
                findViewById(R.id.view_carp_photo_detail_insidescroll);
        mCover = (RelativeLayout) view.
                findViewById(R.id.view_carp_photo_detail_coverview);
        initCover(view);
        SinglePhotoView singlePhotoView = new SinglePhotoView(getContext(), mPhotoItem);
        singlePhotoView.setOnEndListener(new SinglePhotoView.OnEndListener() {
            @Override
            public void onEndListener(SinglePhotoView view) {
                if (onEndListener != null) {
                    onEndListener.onEnd();
                }
            }
        });
        mScroll.addView(singlePhotoView);
    }

    private ImageView coverTag;
    private TextView coverName;
    private TextView coverTime;
    private AvatarImageView coverAvatar;
    private ImageView coverCover;
    private ImageView coverBack;
    private HtmlTextView coverDesc;
    private TextView coverComment;
    private TextView coverShare;
    private FrameLayout coverImgArea;

    private void initCover(View view) {
        coverTag = (ImageView) view.findViewById(R.id.view_carp_photo_detail_cover_tag);
        coverName = (TextView) view.findViewById(R.id.view_carp_photo_detail_cover_name);
        coverTime = (TextView) view.findViewById(R.id.view_carp_photo_detail_cover_time);
        coverAvatar = (AvatarImageView) view.findViewById(R.id.view_carp_photo_detail_cover_avatar);
        coverBack = (ImageView) view.findViewById(R.id.view_carp_photo_detail_cover_backimg);
        coverDesc = (HtmlTextView) view.findViewById(R.id.view_carp_photo_detail_cover_desc);
        coverComment = (TextView) view.findViewById(R.id.view_carp_photo_detail_cover_comment);
        coverShare = (TextView) view.findViewById(R.id.view_carp_photo_detail_cover_share);
        coverImgArea = (FrameLayout) view.findViewById(R.id.view_carp_photo_detail_cover_imgarea);

        if (mPhotoItem.getType() == 1) {
            coverTag.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.tag));
        } else {
            coverTag.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.tag_zuopin));
        }

        coverName.setText(mPhotoItem.getNickname());
        coverTime.setText(mPhotoItem.getUpdateTimeStr());
        ImageLoader.getInstance().displayImage(mPhotoItem.getAvatarURL(), coverAvatar, mAvatarOptions);

        coverCover = new ImageView(mContext);
        coverCover.setLayoutParams
                (new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        ImageLoader.getInstance().displayImage(mPhotoItem.getImageURL(), coverCover, mOptions, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                coverBack.setImageBitmap(BitmapUtils.getBlurBitmap(bitmap));
//                if(bitmap.getHeight()>bitmap.getWidth()){
//                }else{
//                    ViewGroup.LayoutParams params = view.getLayoutParams();
//                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
//                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
//                    view.setLayoutParams(params);
//                }
            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
        coverImgArea.addView(coverCover);

        coverDesc.setHtmlFromString(mPhotoItem.getDesc(), true);
        coverComment.setText(String.valueOf(mPhotoItem.getCommentCount()));
        coverShare.setText(String.valueOf(mPhotoItem.getShareCount()));


    }


    private void initListener(final View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            float DownY;
            float Y;
            float oY = view.getY();
            float moveY = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        DownY = motionEvent.getRawY();
                        Y = view.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        moveY = motionEvent.getRawY() - DownY;
                        if (isBlow && moveY < 0) {

                        } else {
                            ViewHelper.setTranslationY(view, moveY);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        view.setY(oY);
                        if (!isDown && moveY <= 0) {
                            viewPagerBlow();
                        } else {
                            isDown = false;
                        }
                        break;
                }
                if (Utils.pxToDp(mContext, view.getY()) < -70 && moveY < 0) {
                    view.setY(oY);
                    viewPagerBlow();
                } else if (Utils.pxToDp(mContext, view.getY()) > -75 && moveY > 0 && isBlow) {
                    viewPagerRestore();
                } else if (Utils.pxToDp(mContext, view.getY()) > 150 && isAnimEnd) {
                    if (onEndListener != null) {
                        onEndListener.onEnd();
                    }
                } else if(Utils.pxToDp(mContext,view.getY()) > 250){
                    if (onEndListener != null) {
                        onEndListener.onEnd();
                    }
                }
                return true;
            }
        });
    }

    private void viewPagerBlow() {
        isDown = false;
//        mScroll.setCanScroll(true);
        if (!isBlow && vp != null) {
            isBlow = true;
            final AnimatorSet anim = new AnimatorSet();
            anim.setDuration(300);
            ValueAnimator xAnim = ValueAnimator.ofInt(20, -1);
            xAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Integer value = (Integer) valueAnimator.getAnimatedValue();
                    RelativeLayout.LayoutParams vParams = (RelativeLayout.LayoutParams) vp.getLayoutParams();
                    vParams.setMargins(Utils.dpToPx(mContext, value),
                            Utils.dpToPx(mContext, 84 / 20f * value), Utils.dpToPx(mContext, value), 0);
                    vp.setLayoutParams(vParams);
                }
            });
            mScroll.setVisibility(VISIBLE);
            ObjectAnimator scrollAnim = ObjectAnimator.ofFloat(mScroll, "alpha", 0, 1f);
            ObjectAnimator coverAnim = ObjectAnimator.ofFloat(mCover, "alpha", 1f, 0);
            xAnim.addListener(blowAnimListener);
            anim.playTogether(xAnim, scrollAnim, coverAnim);
            anim.start();
        }
    }

    private void viewPagerRestore() {
        if (vp == null) {
            return;
        }
        isDown = true;
        isBlow = false;
        final AnimatorSet anim = new AnimatorSet();
        anim.setDuration(300);
        ValueAnimator xAnim = ValueAnimator.ofInt(-1, 20);
        xAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Integer value = (Integer) valueAnimator.getAnimatedValue();
                RelativeLayout.LayoutParams vParams = (RelativeLayout.LayoutParams) vp.getLayoutParams();
                vParams.setMargins(Utils.dpToPx(mContext, value),
                        Utils.dpToPx(mContext, 84f / 20f * (float) value), Utils.dpToPx(mContext, (float) value), 0);
                vp.setLayoutParams(vParams);
            }
        });
        mCover.setVisibility(VISIBLE);
        ObjectAnimator scrollAnim = ObjectAnimator.ofFloat(mScroll, "alpha", 1f, 0);
        ObjectAnimator coverAnim = ObjectAnimator.ofFloat(mCover, "alpha", 0, 1f);
        xAnim.addListener(restoreAnimListener);
        anim.playTogether(xAnim, scrollAnim, coverAnim);
        anim.start();
    }

    ViewPagerAdapter adapter;
    ViewPagerAdapter thumbAdatper;

    Animator.AnimatorListener blowAnimListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {
            vp.setOffscreenPageLimit(0);
            vp.setClipChildren(true);
            if (vp instanceof StopViewPager) {
                ((StopViewPager) vp).setCanScroll(false);
            }
            thumbAdatper = (ViewPagerAdapter) vp.getAdapter();
            List<View> list = new ArrayList<View>();
            list.add(CarouselPhotoDetailView.this);
            adapter = new ViewPagerAdapter(list);
            vp.setAdapter(adapter);
            isAnimEnd = false;
            isCover = false;
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            isAnimEnd = true;
            mCover.setVisibility(GONE);
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };

    Animator.AnimatorListener restoreAnimListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {
            vp.setOffscreenPageLimit(0);
            isAnimEnd = false;
            isCover = false;
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            vp.setOffscreenPageLimit(3);
            vp.setClipChildren(false);
            if (vp instanceof StopViewPager) {
                ((StopViewPager) vp).setCanScroll(true);
            }
            vp.setAdapter(thumbAdatper);
            int position = thumbAdatper.getItemPosition(CarouselPhotoDetailView.this);
            vp.setCurrentItem(position == -1 ? 0 : position);
            isAnimEnd = true;
            isCover = true;
            mScroll.setVisibility(GONE);
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };

    public interface OnEndListener {
        void onEnd();
    }

    private OnEndListener onEndListener;

    public void setOnEndListener(OnEndListener onEndListener) {
        this.onEndListener = onEndListener;
    }

    //是否是放大的状态
    private boolean isBlow = false;

    //防止放大和缩小动画冲突
    private boolean isDown = false;

    //当前是否有动画
    private boolean isAnimEnd = true;

    //判断是否为表层观看页
    private boolean isCover = true;


}
