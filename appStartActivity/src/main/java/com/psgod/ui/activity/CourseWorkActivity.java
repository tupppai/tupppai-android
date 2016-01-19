package com.psgod.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.nineoldandroids.view.ViewHelper;
import com.psgod.R;
import com.psgod.Utils;

/**
 * Created by Administrator on 2016/1/18 0018.
 */
public class CourseWorkActivity extends PSGodBaseActivity {

    private ImageView mScrollHandler;
    private LinearLayout mScrollArea;

    private int originMarginY = 345;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_work);

        initView();
        initListener();
    }

    private void initView() {
        originMarginY = Utils.getScreenHeightPx(this) -
                Utils.getScreenWidthPx(this) / 3 * 2 - Utils.dpToPx(this,45 + 30);
        mScrollHandler = (ImageView)
                findViewById(R.id.activity_course_work_scollhandle);
        mScrollArea = (LinearLayout) findViewById(R.id.activity_course_work_scollarea);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) mScrollArea.getLayoutParams();
        params.setMargins(0, originMarginY, 0, 0);
        mScrollArea.setLayoutParams(params);
    }

    private void initListener() {
        mScrollHandler.setOnTouchListener(new View.OnTouchListener() {
            float downY;
            float moveY = 0;
            int oH = originMarginY;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downY = motionEvent.getRawY();
                        moveY = 0;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        moveY = motionEvent.getRawY() - downY;
                        RelativeLayout.LayoutParams params =
                                (RelativeLayout.LayoutParams) mScrollArea.getLayoutParams();
                        params.setMargins(0,
                                (int) (oH + moveY), 0, 0);
                        mScrollArea.setLayoutParams(params);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!isScrollTop) {
                            scrollToTop((int) (oH + moveY));
                            oH = 0;
                        } else {
                            scrollToBottom((int) (oH + moveY));
                            oH = originMarginY;
                        }
                        break;
                }
                return true;
            }
        });
    }

    private boolean isAnimEnd = true;
    private boolean isScrollTop = false;

    private void scrollToTop(int fromY) {
        if (isAnimEnd) {
            isAnimEnd = false;
            final AnimatorSet anim = new AnimatorSet();
            anim.setDuration(250);
            ValueAnimator yAnim = ValueAnimator.ofInt(fromY, 0);
            yAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Integer value = (Integer) valueAnimator.getAnimatedValue();
                    RelativeLayout.LayoutParams params =
                            (RelativeLayout.LayoutParams) mScrollArea.getLayoutParams();
                    params.setMargins(0, value, 0, 0);
                    mScrollArea.setLayoutParams(params);
                }
            });
            yAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    isAnimEnd = true;
                    isScrollTop = true;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            anim.playTogether(yAnim);
            anim.start();
        }
    }

    private void scrollToBottom(int fromY) {
        if (isAnimEnd) {
            isAnimEnd = false;
            final AnimatorSet anim = new AnimatorSet();
            anim.setDuration(250);
            ValueAnimator yAnim = ValueAnimator.ofInt(fromY, originMarginY);
            yAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Integer value = (Integer) valueAnimator.getAnimatedValue();
                    RelativeLayout.LayoutParams params =
                            (RelativeLayout.LayoutParams) mScrollArea.getLayoutParams();
                    params.setMargins(0, value, 0, 0);
                    mScrollArea.setLayoutParams(params);
                }
            });
            yAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    isAnimEnd = true;
                    isScrollTop = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            anim.playTogether(yAnim);
            anim.start();
        }
    }
}
