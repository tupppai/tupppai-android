package com.psgod.ui.activity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_work);

        initView();
        initListener();
    }

    private void initView() {
        mScrollHandler = (ImageView)
                findViewById(R.id.activity_course_work_scollhandle);
        mScrollArea = (LinearLayout) findViewById(R.id.activity_course_work_scollarea);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) mScrollArea.getLayoutParams();
        params.setMargins(0, Utils.dpToPx(this,345),0,0);
        mScrollArea.setLayoutParams(params);
    }

    private void initListener() {
        mScrollHandler.setOnTouchListener(new View.OnTouchListener() {
            float downY;
            float moveY = 0;
            int oH;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downY = motionEvent.getRawY();
                        moveY = 0;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        moveY = motionEvent.getRawY() - downY;
                        if(moveY < 0){
                            RelativeLayout.LayoutParams params =
                                    (RelativeLayout.LayoutParams) mScrollArea.getLayoutParams();
                            params.setMargins(0, (int) -moveY,0,0);
                            mScrollArea.setLayoutParams(params);
                        }
                        break;
                }
                return true;
            }
        });
    }
}
