package com.psgod.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.RelativeLayout;

import com.psgod.R;

/**
 * Created by Administrator on 2016/1/4 0004.
 */
public class ImageSelectDialog extends Dialog {

    private long categoryid = -1;
    private RelativeLayout mParent;
    private RelativeLayout mArea;


    public ImageSelectDialog(Context context) {
        super(context, R.style.CaroPhotoDialog);
    }

    public ImageSelectDialog(Context context, long categoryid){
        super(context, R.style.CaroPhotoDialog);
        this.categoryid = categoryid;
    }

    @Override
    public void show() {
        initView();
        super.show();
    }

    private void initView() {

    }
}
