package com.psgod.ui.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Administrator on 2015/11/27 0027.
 */
public class StopViewPager extends ViewPager {
    public StopViewPager(Context context) {
        super(context);
    }

    public StopViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private boolean canScroll = true;

    public void setCanScroll(boolean canScroll) {
        this.canScroll = canScroll;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return canScroll?super.onTouchEvent(ev):false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return canScroll?super.onInterceptTouchEvent(ev):false;
    }
}
