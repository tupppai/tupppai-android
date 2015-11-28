package com.psgod.ui.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.nineoldandroids.view.ViewHelper;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.PhotoItem;
import com.psgod.ui.adapter.ViewPagerAdapter;
import com.psgod.ui.widget.StopScrollView;
import com.psgod.ui.widget.StopViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/11/27 0027.
 */
public class CarouselPhotoDetailView extends RelativeLayout {

    private Context mContext;
    private View mParent;

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

    public CarouselPhotoDetailView(Context context,PhotoItem photoItem) {
        super(context);
        init();
    }

    public void setParent(View mParent) {
        this.mParent = mParent;
    }

    private void init() {
        mContext = getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_carousel_photo_detail, null);
        addView(view);
        initView(view);
        initListener(view);
    }


    private void initView(View view) {
        mScroll = (StopScrollView) view.
                findViewById(R.id.view_carp_photo_detail_insidescroll);
        mCover = (RelativeLayout) view.
                findViewById(R.id.view_carp_photo_detail_coverview);
    }

    private ViewPager vp;
    private PhotoItem mPhotoItem;

    private StopScrollView mScroll;
    private RelativeLayout mCover;

    public void setVp(ViewPager vp) {
        this.vp = vp;
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
                }
                return true;
            }
        });
    }

    private void viewPagerBlow() {
        isDown = false;
        mScroll.setCanScroll(true);
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
            ObjectAnimator coverAnim = ObjectAnimator.ofFloat(mCover,"alpha",1f,0);
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
        anim.playTogether(xAnim,scrollAnim,coverAnim);
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
