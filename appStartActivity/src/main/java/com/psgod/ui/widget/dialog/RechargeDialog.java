package com.psgod.ui.widget.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.pingplusplus.android.PaymentActivity;
import com.psgod.Constants;
import com.psgod.CustomToast;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.WeakReferenceHandler;
import com.psgod.model.LoginUser;
import com.psgod.model.MoneyTransfer;
import com.psgod.network.request.ChargeRequest;
import com.psgod.network.request.MoneyTransferRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.ui.activity.WithdrawSuccessActivity;
import com.psgod.ui.activity.WithDrawMoneyBindWechatActivity;

import org.json.JSONObject;


/**
 * Created by pires on 16/1/20.
 * 充值弹窗
 */
public class RechargeDialog extends Dialog implements Handler.Callback {

    public static final String CHANNEL_WECHAT = "wx";
    public static final String CHANNEL_ALIPAY = "alipay";


    public static final String TRANSFER_WECHAT = "transfer_wx";

    private String mChannelType = CHANNEL_WECHAT;

    private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);

    private Context mContext;
    private EditText mRechargeCountEt;
    private TextView mCompleteBtn;
    private TextView mTitle;


    private double amount;
    private int requestCode = 191;

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public void setAmount(double amount) {
        this.amount = amount;
        if (amount > 0) {
            mRechargeCountEt.setText(String.format("%.2f", amount));
        }
    }


    public RechargeDialog(Context context, String channelType) {
        super(context, R.style.ActionSheetDialog);
        this.mContext = context;
        this.mChannelType = channelType;
        setContentView(R.layout.dialog_recharge_layout);

        mRechargeCountEt = (EditText) findViewById(R.id.recharge_count_edit);
        setPricePoint(mRechargeCountEt);
        mCompleteBtn = (TextView) findViewById(R.id.recharge_sure_tv);
        mTitle = (TextView) findViewById(R.id.recharge_title);
        if (mChannelType.indexOf("transfer") != -1) {
            mTitle.setText("请输入提现金额 (元)");
        } else {
            mTitle.setText("请输入充值金额 (元)");
        }
        getWindow().getAttributes().width = Constants.WIDTH_OF_SCREEN;
        setCanceledOnTouchOutside(true);

        initListener();
    }

    private void initListener() {
        mRechargeCountEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRechargeCountEt.getText().toString().equals("0.00")) {
                    mRechargeCountEt.setText("");
                }
            }
        });

        mCompleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRechargeCountEt.getText().toString() == null ||
                        mRechargeCountEt.getText().toString().trim().equals("") ||
                        Double.parseDouble(mRechargeCountEt.getText().toString()) <= 0) {
                    CustomToast.show(mContext, "金额必须大于0", Toast.LENGTH_SHORT);
                } else {
                    RechargeDialog.this.dismiss();
                    if (mChannelType.indexOf("transfer") != -1) {
                        /**
                         * 提现部分
                         */
                        LoginUser user = LoginUser.getInstance();
                        if (user.isBoundWechat()) {
                            MoneyTransferRequest request = new MoneyTransferRequest.Builder().
                                    setErrorListener(new PSGodErrorListener(this) {
                                        @Override
                                        public void handleError(VolleyError error) {
                                            dismiss();
                                        }
                                    }).
                                    setListener(new Response.Listener<MoneyTransfer>() {
                                        @Override
                                        public void onResponse(MoneyTransfer response) {
                                            dismiss();
                                            Intent intent = new Intent(mContext, WithdrawSuccessActivity.class);
                                            intent.putExtra(WithdrawSuccessActivity.RESULT, response);
                                            mContext.startActivity(intent);
                                        }
                                    }).setAmount(mRechargeCountEt.getText().toString()).build();
                            RequestQueue requestQueue = PSGodRequestQueue
                                    .getInstance(mContext).getRequestQueue();
                            requestQueue.add(request);
                        } else {
                            /**
                             * 未绑定微信跳转绑定页
                             */
                            Intent intent = new Intent(mContext,
                                    WithDrawMoneyBindWechatActivity.class);
                            intent.putExtra(WithDrawMoneyBindWechatActivity.AMOUNT, amount);
                            mContext.startActivity(intent);
                        }
                    } else {
                        /**
                         * 充值部分
                         */
                        Utils.showProgressDialog(mContext);
                        ChargeRequest request = new ChargeRequest.Builder().
                                setListener(new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        if (response != null) {
                                            Intent intent = new Intent(mContext, PaymentActivity.class);
                                            intent.putExtra(PaymentActivity.EXTRA_CHARGE,
                                                    response.toString());
                                            ((Activity) mContext).startActivityForResult(intent, requestCode);
                                        }
                                    }
                                }).
                                setErrorListener(new PSGodErrorListener(this) {
                                    @Override
                                    public void handleError(VolleyError error) {

                                    }
                                }).setAmount(mRechargeCountEt.getText().toString())
                                .setType(mChannelType).build();
                        RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                                mContext).getRequestQueue();
                        requestQueue.add(request);
                    }
                }
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
                if (s.toString().indexOf("0.00") == 0 && s.length() > 4) {
                    editText.setText(s.toString().substring(4, 5));
                    editText.setSelection(1);
                } else {
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
    public void dismiss() {
        super.dismiss();
//        Utils.hideProgressDialog();
    }

    @Override
    public boolean handleMessage(Message message) {
        return false;
    }
}
