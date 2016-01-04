package com.psgod.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by Administrator on 2016/1/4 0004.
 */
public class ImageSelectInputView extends RelativeLayout {


    public ImageSelectInputView(Context context) {
        super(context);
    }

    public ImageSelectInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageSelectInputView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        onCreate();
    }

    private void onCreate() {
        initView();
    }

    private void initView() {

    }
}
