package com.psgod.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.psgod.R;

/**
 * Created by Administrator on 2016/1/21 0021.
 */
public class PayErrorDialog extends Dialog{
    public PayErrorDialog(Context context) {
        super(context, R.style.ActionSheetDialog);
        initView();
    }

    public PayErrorDialog(Context context, int theme) {
        super(context, theme);
        initView();
    }

    protected PayErrorDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        initView();
    }

    private View mParent;

    private TextView mtitle;
    private TextView mContent;
    private TextView mGo;

    private void initView() {
        mParent = LayoutInflater.from(getContext()).inflate(R.layout.dialog_pay_error,null);
        setContentView(mParent);

        mtitle = (TextView) findViewById(R.id.pay_error_title);
        mContent = (TextView) findViewById(R.id.pay_error_content);
        mGo = (TextView) findViewById(R.id.pay_error_go);

    }



}
