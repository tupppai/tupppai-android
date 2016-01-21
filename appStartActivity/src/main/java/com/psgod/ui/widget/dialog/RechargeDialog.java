package com.psgod.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.psgod.Constants;
import com.psgod.R;
import com.psgod.WeakReferenceHandler;



/**
 * Created by pires on 16/1/20.
 */
public class RechargeDialog extends Dialog {

    private Context mContext;
    private EditText mRechargeCountEt;
    private WeakReferenceHandler mHandler ;
    private TextView mCompleteBtn;

    public RechargeDialog(Context context,WeakReferenceHandler handler) {
        super(context, R.style.ActionSheetDialog);
        this.mContext = context;
        this.mHandler = handler;
        setContentView(R.layout.dialog_recharge_layout);

        mRechargeCountEt = (EditText) findViewById(R.id.recharge_count_edit);
        setPricePoint(mRechargeCountEt);
        mCompleteBtn = (TextView) findViewById(R.id.recharge_sure_tv);

        getWindow().getAttributes().width = Constants.WIDTH_OF_SCREEN;
        setCanceledOnTouchOutside(true);

        mCompleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RechargeDialog.this.dismiss();
            }
        });
    }

    @Override
    public void show() {
        super.show();
        getWindow().setGravity(Gravity.CENTER);
        getWindow().setWindowAnimations(R.style.popwindow_anim_style);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callInputPanel();
            }
        }, 100);

    }

    private void callInputPanel() {
        // 唤起输入键盘 并输入框取得焦点
        Editable etext = mRechargeCountEt.getText();
        Selection.setSelection(etext, etext.length());

        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mRechargeCountEt, 0);

    }

    public static void setPricePoint(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (s.toString().contains(".")) {
                    if (s.length() - 1 - s.toString().indexOf(".") > 2) {
                        s = s.toString().subSequence(0,
                                s.toString().indexOf(".") + 3);
                        editText.setText(s);
                        editText.setSelection(s.length());
                    }
                }
                if (s.toString().trim().substring(0).equals(".")) {
                    s = "0" + s;
                    editText.setText(s);
                    editText.setSelection(2);
                }

                if (s.toString().startsWith("0")
                        && s.toString().trim().length() > 1) {
                    if (!s.toString().substring(1, 2).equals(".")) {
                        editText.setText(s.subSequence(0, 1));
                        editText.setSelection(1);
                        return;
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }

        });

    }
}
