package com.pires.wesee.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.pires.wesee.R;
import com.pires.wesee.model.Reward;

/**
 * Created by Administrator on 2016/1/21 0021.
 * 支付失败时弹窗
 */
public class PayErrorDialog extends Dialog{

    public PayErrorDialog(Context context) {
        super(context, R.style.ActionSheetDialog);
        this.context =context;
        initView();
        initListener();

    }

    public PayErrorDialog(Context context, int theme) {
        super(context, theme);
        initView();
        initListener();

    }

    protected PayErrorDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        initView();
        initListener();
    }

    private void initListener() {
        mGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                RechargeTypeDialog rechargeTypeDialog = new RechargeTypeDialog(context);
                if(reward != null){
                    rechargeTypeDialog.setAmount(1);
                }
                rechargeTypeDialog.setRequestCode(requestCode);
                rechargeTypeDialog.show();
            }
        });
    }

    private View mParent;

    private TextView mtitle;
    private TextView mContent;
    private TextView mGo;

    private Context context;
    // 金额对象
    private Reward reward;
    private int requestCode;

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public void setReward(Reward reward) {
        this.reward = reward;
    }

    private void initView() {
        mParent = LayoutInflater.from(getContext()).inflate(R.layout.dialog_pay_error,null);
        setContentView(mParent);

        mtitle = (TextView) findViewById(R.id.pay_error_title);
        mContent = (TextView) findViewById(R.id.pay_error_content);
        mGo = (TextView) findViewById(R.id.pay_error_go);

    }



}
