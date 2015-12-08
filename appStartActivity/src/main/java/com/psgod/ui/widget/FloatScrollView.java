package com.psgod.ui.widget;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nineoldandroids.view.ViewHelper;
import com.psgod.CustomToast;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.PhotoItem;

/**
 * Created by Administrator on 2015/12/8 0008.
 */
public class FloatScrollView {

    private ListView listView;
    private Context context;
    private View floatView;
    private ViewGroup parentView;

    public FloatScrollView(ListView listView,ViewGroup parentView, View floatView, Context context) {
        this.listView = listView;
        this.context = context;
        this.floatView = floatView;
        this.parentView = parentView;
    }

    public void init() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.setMargins(0, 0, 0, Utils.dpToPx(context, 15));
        floatView.setLayoutParams(params);
        parentView.addView(floatView);

        initListener();
    }

    private void initListener() {
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                switch (i){
                    case SCROLL_STATE_TOUCH_SCROLL:
                        break;
                    case SCROLL_STATE_IDLE:
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });
    }

    public void setListView(ListView listView) {
        this.listView = listView;
    }
}
