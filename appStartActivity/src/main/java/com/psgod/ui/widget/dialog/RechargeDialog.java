package com.psgod.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;

import com.psgod.Constants;
import com.psgod.R;

/**
 * Created by pires on 16/1/20.
 */
public class RechargeDialog extends Dialog {

    private Context mContext;

    public RechargeDialog(Context context) {
        super(context, R.style.ActionSheetDialog);
        this.mContext = context;
        setContentView(R.layout.dialog_recharge_layout);

        getWindow().getAttributes().width = Constants.WIDTH_OF_SCREEN;
        setCanceledOnTouchOutside(true);
    }

    @Override
    public void show() {
        super.show();
        getWindow().setGravity(Gravity.CENTER);
        getWindow().setWindowAnimations(R.style.popwindow_anim_style);
    }
}
