package com.psgod.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.WeakReferenceHandler;

import java.util.jar.Attributes;
import java.util.logging.Handler;

/**
 * Created by pires on 16/1/20.
 */
public class RechargeTypeDialog extends Dialog {
    private static final String TAG = RechargeTypeDialog.class.getSimpleName();
    private Context mContext;
    private TextView mAlipayTv;
    private TextView mWeixinTv;
    private TextView mCancelTv;

    private double amount;
    private int requestCode;

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public RechargeTypeDialog(Context context) {
        super(context, R.style.ActionSheetDialog);
        this.mContext = context;
        setContentView(R.layout.dialog_recharge_type);

        mAlipayTv = (TextView) this.findViewById(R.id.recharge_alipay);
//        mAlipayTv.setEnabled(false);

        mWeixinTv = (TextView) this.findViewById(R.id.recharge_weixin);
        mCancelTv = (TextView) this.findViewById(R.id.cancen_btn);

        getWindow().getAttributes().width = Constants.WIDTH_OF_SCREEN;
        setCanceledOnTouchOutside(true);

        mAlipayTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RechargeTypeDialog.this.dismiss();
                RechargeDialog dialog = new RechargeDialog(mContext, RechargeDialog.CHANNEL_ALIPAY);
                dialog.setRequestCode(requestCode);
                dialog.setAmount(amount);
                dialog.show();
            }
        });

        mWeixinTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RechargeTypeDialog.this.dismiss();
                RechargeDialog dialog = new RechargeDialog(mContext, RechargeDialog.CHANNEL_WECHAT);
                dialog.setRequestCode(requestCode);
                dialog.setAmount(amount);
                dialog.show();
            }
        });

        mCancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RechargeTypeDialog.this.dismiss();
            }
        });

    }

    @Override
    public void show() {
        super.show();
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setWindowAnimations(R.style.popwindow_anim_style);
    }

}
