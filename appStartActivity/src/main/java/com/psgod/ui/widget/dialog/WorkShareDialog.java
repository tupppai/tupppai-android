package com.psgod.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;

/**
 * Created by Administrator on 2016/1/21 0021.
 */
public class WorkShareDialog extends Dialog {
    public WorkShareDialog(Context context) {
        super(context);
    }

    public WorkShareDialog(Context context, int theme) {
        super(context, theme);
    }

    protected WorkShareDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
}
