package com.psgod.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

/**
 * Created by Administrator on 2016/1/18 0018.
 */
public class StopGridView extends GridView {
    public StopGridView(Context context) {
        super(context);
    }

    public StopGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StopGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private boolean canScroll = false;

    public void setCanScroll(boolean isScrolled) {
        this.canScroll = isScrolled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        return canScroll ? super.onTouchEvent(motionEvent) : false;
    }
}
