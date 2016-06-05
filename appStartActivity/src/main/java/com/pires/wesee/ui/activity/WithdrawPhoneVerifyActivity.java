package com.pires.wesee.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.pires.wesee.CustomToast;
import com.pires.wesee.Utils;
import com.pires.wesee.WeakReferenceHandler;
import com.pires.wesee.model.LoginUser;
import com.pires.wesee.model.MoneyTransfer;
import com.pires.wesee.network.request.GetVerifyCodeRequest;
import com.pires.wesee.network.request.MoneyTransferRequest;
import com.pires.wesee.network.request.PSGodErrorListener;
import com.pires.wesee.network.request.PSGodRequestQueue;
import com.pires.wesee.R;
import com.pires.wesee.eventbus.BindEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.greenrobot.event.EventBus;

/**
 * Created by pires on 16/1/21.
 * 提现手机验证
 */
public class WithdrawPhoneVerifyActivity extends PSGodBaseActivity {
    private static final String TAG = WithdrawPhoneVerifyActivity.class.getSimpleName();

    public static final String AMOUNT_DOUBLE = "amount";
    public static final int RESEND_TIME_IN_SEC = 60;
    private int mLeftTime = RESEND_TIME_IN_SEC;
    private static final int MSG_TIMER = 0x3315;
    private static final int MSG_CODE = 0x3321;

    private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);

    private double amount;

    private TextView mPhoneTxt;
    private TextView mVerifyTxt;
    private EditText mVerifyEdit;
    private Button mSure;

    private BroadcastReceiver smsReceiver;
    private IntentFilter filter;
    private String patternCoder = "(?<!\\d)\\d{4}(?!\\d)";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        Utils.addActivity(this);
        setContentView(R.layout.activity_withdraw_phone_verify);

        Intent intent = getIntent();
        amount = intent.getDoubleExtra(AMOUNT_DOUBLE, 0);

        initView();
        initListener();
//        mVerifyTxt.callOnClick();
        mSure.callOnClick();
//        codeReceiver();
    }

    public void onEventMainThread(BindEvent event) {
        switch (event.state){
            case OK:
                Utils.showProgressDialog(this);
                mSure.callOnClick();
                break;
            case FINISH:
                finish();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    // BroadcastReceiver拦截短信验证码
    private void codeReceiver() {
        filter = new IntentFilter();
        //设置短信拦截参数
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(Integer.MAX_VALUE);
        smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Object[] objs = (Object[]) intent.getExtras().get("pdus");
                for (Object obj : objs) {
                    byte[] pdu = (byte[]) obj;
                    SmsMessage sms = SmsMessage.createFromPdu(pdu);
                    String message = sms.getMessageBody();
                    String from = sms.getOriginatingAddress();
                    if (!TextUtils.isEmpty(from)) {
                        String code = patternCode(message);
                        if (!TextUtils.isEmpty(code)) {
                            Message msg = mHandler.obtainMessage();
                            msg.what = MSG_CODE;
                            Bundle bundle = new Bundle();
                            bundle.putString("messagecode", code);
                            msg.setData(bundle);
                            mHandler.sendMessage(msg);
                        }
                    }
                }
            }
        };
        registerReceiver(smsReceiver, filter);
    }

    private String patternCode(String patternContent) {
        if (TextUtils.isEmpty(patternContent)) {
            return null;
        }
        Pattern p = Pattern.compile(patternCoder);
        Matcher matcher = p.matcher(patternContent);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private void initView() {
        mPhoneTxt = (TextView) findViewById(R.id.withdraw_phone_verify_phone);
        mVerifyTxt = (TextView) findViewById(R.id.withdraw_phone_verify_verify);
        mVerifyEdit = (EditText) findViewById(R.id.withdraw_phone_verify_edit);
        mSure = (Button) findViewById(R.id.withdraw_phone_verify_sure);

        mPhoneTxt.setText(LoginUser.getInstance().getPhoneNum());
    }

    private void initListener() {
        mVerifyTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLeftTime = 60;
                if (mLeftTime > 1) {
                    mLeftTime--;
                    mVerifyTxt.setEnabled(false);
                    mVerifyTxt.setText(mLeftTime + "s后重发");
                    mVerifyTxt.setTextColor(Color.parseColor("#66090909"));
                    mHandler.sendEmptyMessageDelayed(MSG_TIMER, 1000);
                } else {
                    mLeftTime = RESEND_TIME_IN_SEC;
                    mVerifyTxt.setEnabled(true);
                    mVerifyTxt.setText("获取验证码");
                    mVerifyTxt.setTextColor(Color.parseColor("#090909"));
                }

                GetVerifyCodeRequest.Builder builder = new GetVerifyCodeRequest.Builder()
                        .setErrorListener(
                                errorListener);
                GetVerifyCodeRequest request = builder
                        .build();
                request.setTag(TAG);
                RequestQueue requestQueue = PSGodRequestQueue
                        .getInstance(
                                WithdrawPhoneVerifyActivity.this)
                        .getRequestQueue();
                requestQueue.add(request);
            }
        });

        mSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * 暂时去掉验证，需要时解除注释
                 */
//                if(mVerifyEdit.getText().toString().length() > 0) {
                Utils.showProgressDialog(WithdrawPhoneVerifyActivity.this);
                MoneyTransferRequest request = new MoneyTransferRequest.Builder().
                        setErrorListener(new PSGodErrorListener(this) {
                            @Override
                            public void handleError(VolleyError error) {

                            }
                        }).
                        setListener(new Response.Listener<MoneyTransfer>() {
                            @Override
                            public void onResponse(MoneyTransfer response) {
                                Utils.hideProgressDialog();
                                if (response != null) {
                                    Intent intent = new Intent(WithdrawPhoneVerifyActivity.this,
                                            WithdrawSuccessActivity.class);
                                    intent.putExtra(WithdrawSuccessActivity.RESULT, response);
                                    WithdrawPhoneVerifyActivity.this.startActivity(intent);
                                }
                            }
                        }).
                        setAmount(String.valueOf(amount)).
                        setCode(mVerifyEdit.getText().toString()).
                        build();
                RequestQueue requestQueue = PSGodRequestQueue
                        .getInstance(WithdrawPhoneVerifyActivity.this).getRequestQueue();
                requestQueue.add(request);
//                }else{
//                    CustomToast.show(WithdrawPhoneVerifyActivity.this,"验证不能为空",Toast.LENGTH_LONG);
//                }
            }
        });
    }

    private PSGodErrorListener errorListener = new PSGodErrorListener(this) {

        @Override
        public void handleError(VolleyError error) {
            CustomToast.show(WithdrawPhoneVerifyActivity.this
                    , "验证码获取失败", Toast.LENGTH_SHORT);
            mLeftTime = RESEND_TIME_IN_SEC;
            mVerifyTxt.setEnabled(true);
            mVerifyTxt.setText("获取验证码");
            mVerifyTxt.setTextColor(Color.parseColor("#090909"));
        }

    };

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_TIMER:
                // 重发倒计时
                if (mLeftTime > 1) {
                    mLeftTime--;
                    mVerifyTxt.setEnabled(false);
                    mVerifyTxt.setText(mLeftTime + "s后重发");
                    mHandler.sendEmptyMessageDelayed(MSG_TIMER, 1000);
                } else {
                    mLeftTime = RESEND_TIME_IN_SEC;
                    mVerifyTxt.setEnabled(true);
                    mVerifyTxt.setText("获取验证码");
                    mVerifyTxt.setTextColor(Color.parseColor("#090909"));
                }
                break;
            case MSG_CODE:
                String codeMsg = msg.getData().getString("messagecode");
                if (codeMsg != null && codeMsg.length() >= 4 && codeMsg.length() <= 6) {
                    mVerifyEdit.setText(codeMsg);
                }

            default:
                break;

        }
        return true;
    }

}
