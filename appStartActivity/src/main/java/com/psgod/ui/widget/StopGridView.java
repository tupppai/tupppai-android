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

    float DownY;
    float moveY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        if (!canScroll) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    moveY = 0;
                    DownY = motionEvent.getRawY();//float DownY
                    break;
                case MotionEvent.ACTION_MOVE:
                    moveY = motionEvent.getRawY() - DownY;//y轴距离
                    if (Math.abs(moveY) > 10) {
                        return false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (Math.abs(moveY) > 10) {
                        return false;
                    }
                    break;
            }
        }
        return super.onTouchEvent(motionEvent);
    }
}
