package com.pires.wesee.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

import com.handmark.pulltorefresh.library.PullToRefreshAdapterViewBase;
import com.pires.wesee.Utils;

/**
 * Created by Administrator on 2015/12/8 0008.
 * 滑动时下沉的浮标类
 */
public class FloatScrollHelper {

    private PullToRefreshAdapterViewBase listView;
    private Context context;
    private View floatView;
    private ViewGroup parentView;

    public FloatScrollHelper(PullToRefreshAdapterViewBase listView,
                             RelativeLayout parentView, View floatView, Context context) {
        this.listView = listView;
        this.context = context;
        this.floatView = floatView;
        this.parentView = parentView;
    }

    public void init() {
        RelativeLayout.LayoutParams params;
        if (floatView.getLayoutParams() == null) {
            params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            params = (RelativeLayout.LayoutParams) floatView.getLayoutParams();
        }
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.setMargins(0, 0, 0, Utils.dpToPx(context, viewMargins));
        floatView.setLayoutParams(params);
        parentView.addView(floatView);
        initListener();
    }

    public void setViewParams(int widthDp, int heightDp) {
        RelativeLayout.LayoutParams params;
        if (floatView.getLayoutParams() == null) {
            params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            params = (RelativeLayout.LayoutParams) floatView.getLayoutParams();

        }
        params.width = Utils.dpToPx(context,widthDp);
        params.height = Utils.dpToPx(context,heightDp);
        floatView.setLayoutParams(params);
    }

    private void initListener() {
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                switch (i) {
                    case SCROLL_STATE_TOUCH_SCROLL:
                        downAnim();
                        break;
                    case SCROLL_STATE_IDLE:
                        upAnim();
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });
    }
    //是否正在上升
    private boolean isUpAnim = false;
    //是否正在下降
    private boolean isDownAnim = false;

    private AnimatorSet upAnimSet = new AnimatorSet();
    private AnimatorSet downAnimSet = new AnimatorSet();

    private int viewMargins = 15;
    private int viewHeight = 50;

    private int margin = 15;

    //传dp
    public void setViewMargins(int viewMarginsDp) {
        this.viewMargins = viewMarginsDp;
        margin = viewMargins;
    }

    public void setViewMarginsV19(int viewMarginsDp) {
        this.viewMargins = viewMarginsDp + 33;
        margin = viewMargins;
    }

    public void setViewHeight(int viewMarginsDp) {
        this.viewHeight = viewMarginsDp + 10;
    }

    private void upAnim() {
        if (!isUpAnim) {
            isUpAnim = true;
            downAnimSet.cancel();
            ValueAnimator upAnimator = ValueAnimator.ofInt(margin, viewMargins);
            upAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Integer value = (Integer) valueAnimator.getAnimatedValue();
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) floatView.getLayoutParams();
                    params.setMargins(0, 0, 0, Utils.dpToPx(context, value));
                    margin = value;
                    floatView.setLayoutParams(params);
                }
            });
            upAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    isUpAnim = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    isUpAnim = false;
                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            upAnimSet.setDuration(300);
            upAnimSet.play(upAnimator);
            upAnimSet.start();
        }
    }

    private void downAnim() {
        if (!isDownAnim) {
            isDownAnim = true;
            upAnimSet.cancel();
            ValueAnimator downAnimator = ValueAnimator.ofInt(margin, -1 * viewHeight);
            downAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Integer value = (Integer) valueAnimator.getAnimatedValue();
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) floatView.getLayoutParams();
                    params.setMargins(0, 0, 0, Utils.dpToPx(context, value));
                    margin = value;
                    floatView.setLayoutParams(params);
                }
            });
            downAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    isDownAnim = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    isDownAnim = false;
                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            downAnimSet.setDuration(300);
            downAnimSet.play(downAnimator);
            downAnimSet.start();
        }
    }

    public void setListView(PullToRefreshAdapterViewBase listView) {
        this.listView = listView;
    }
}