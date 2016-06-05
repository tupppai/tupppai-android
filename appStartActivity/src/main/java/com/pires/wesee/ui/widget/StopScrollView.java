package com.pires.wesee.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by Administrator on 2015/11/27 0027.
 */
public class StopScrollView extends ScrollView {
    public StopScrollView(Context context) {
        super(context);
    }

    public StopScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StopScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private boolean canScroll = false;

    public void setCanScroll(boolean isScrolled) {
        this.canScroll = isScrolled;
    }
    float DownY;
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
            float moveY;
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    DownY = motionEvent.getRawY();//float DownY
                    break;
                case MotionEvent.ACTION_MOVE:
                    moveY = motionEvent.getRawY() - DownY;//y轴距离
                    if(getScrollY() == 0 && moveY > 0){
                        canScroll = false;
                        return false;
                    }else{
                        canScroll = true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
        return canScroll ?super.onTouchEvent(motionEvent):false;
    }


}
