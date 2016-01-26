package com.psgod.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.WeakReferenceHandler;
import com.psgod.model.Reward;
import com.psgod.network.request.ChannelRequest;
import com.psgod.network.request.ChargeRequest;
import com.psgod.network.request.CommentListRequest;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.RewardRequest;

import org.json.JSONObject;


/**
 * Created by pires on 16/1/20.
 */
public class RechargeDialog extends Dialog implements Handler.Callback {

    public static final String CHANNEL_WECHAT = "wx";
    public static final String CHANNEL_ALIPAY = "alipay";

    private String mChannelType = CHANNEL_WECHAT;

    private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);

    private Context mContext;
    private EditText mRechargeCountEt;
    private TextView mCompleteBtn;

    public RechargeDialog(Context context,String channelType) {
        super(context, R.style.ActionSheetDialog);
        this.mContext = context;
        this.mChannelType = channelType;
        setContentView(R.layout.dialog_recharge_layout);

        mRechargeCountEt = (EditText) findViewById(R.id.recharge_count_edit);
        setPricePoint(mRechargeCountEt);
        mCompleteBtn = (TextView) findViewById(R.id.recharge_sure_tv);

        getWindow().getAttributes().width = Constants.WIDTH_OF_SCREEN;
        setCanceledOnTouchOutside(true);

        initListener();
    }

    private void initListener(){
        mCompleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RechargeDialog.this.dismiss();
                ChargeRequest request = new ChargeRequest.Builder().
                        setListener(new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                            }
                        }).
                        setErrorListener(new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        }).setAmount(mRechargeCountEt.getText().toString())
                        .setType(mChannelType).build();
                RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                       mContext).getRequestQueue();
                requestQueue.add(request);
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

    @Override
    public boolean handleMessage(Message message) {
        return false;
    }
}
