package com.psgod.ui.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
        mScroll = (StopScrollView) view.findViewById(R.id.view_carp_photo_detail_scroll);
    }

    private ViewPager vp;
    private PhotoItem mPhotoItem;

    private StopScrollView mScroll;

    public void setVp(ViewPager vp) {
        this.vp = vp;
    }

    private void initListener(final View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            float DownX;
            float DownY;
            float Y;
            float oY = view.getY();

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                float moveY = 0;
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        DownX = motionEvent.getX();//float DownX
                        DownY = motionEvent.getRawY();//float DownY
                        Y = view.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        moveY = motionEvent.getRawY() - DownY;//y轴距离
                        if (isBlow || isStore && moveY < 0) {

                        } else {
                            ViewHelper.setTranslationY(view, moveY);
                        }
//                        Log.e("Y",String.valueOf(Utils.pxToDp(mContext, motionEvent.getRawY())));

                        break;
                    case MotionEvent.ACTION_UP:
                        view.setY(oY);
                        if (!isDown) {
                            viewPagerBlow();
                        } else {
                            isDown = false;
                        }
//                        if (Utils.pxToDp(mContext, motionEvent.getY()) > 300) {
//                            if (onEndListener != null) {
//                                onEndListener.onEnd();
//                            }
//                        }
                        break;
                }
//                if (Utils.pxToDp(mContext, view.getY()) < -70 && moveY < 0) {
//                    view.setY(oY);
//                    viewPagerBlow();
//                } else
                if (Utils.pxToDp(mContext, view.getY()) > -75 && moveY > 0 && isBlow) {
                    viewPagerRestore();
                }
//                else if (Utils.pxToDp(mContext, view.getY()) > 200 && !isBlow) {
//                    if (onEndListener != null) {
//                        onEndListener.onEnd();
//                    }
//                }
                return true;
            }
        });
    }

    private void viewPagerBlow() {
        isDown = false;
        mScroll.setCanScroll(true);
        if (isBlow && vp != null) {
            isFlows = false;
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
            xAnim.addListener(blowAnimListener);
            anim.play(xAnim);
            anim.start();
        }
    }

    private void viewPagerRestore() {
        if (vp == null) {
            return;
        }
        isDown = true;
        isFlows = true;
        isStore = true;
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
        xAnim.addListener(restoreAnimListener);
        anim.play(xAnim);
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

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            isBlow = false;
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
            isStore = false;
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

    private boolean isFlows = true;
    private boolean isBlow = false;
    private boolean isStore = false;
    private boolean isDown = false;

}
