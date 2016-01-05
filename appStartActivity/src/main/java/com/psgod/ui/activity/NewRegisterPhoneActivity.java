package com.psgod.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.psgod.Constants;
import com.psgod.PSGodToast;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.WeakReferenceHandler;
import com.psgod.model.LoginUser;
import com.psgod.model.RegisterData;
import com.psgod.network.request.GetVerifyCodeRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.RegisterRequest;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import org.json.JSONObject;

/**
 * Created by pires on 16/1/4.
 */
public class NewRegisterPhoneActivity extends PSGodBaseActivity{
    private static final String TAG = NewRegisterPhoneActivity.class.getSimpleName();
    private static final String PHONE = "PhoneNum";
    private static final int JUMP_FROM_LOGIN_ACTIVITY = 100;
    private static final int RESEND_TIME_IN_SEC = 60; // 重新发送验证时间（秒）
    private static final int MSG_TIMER = 0x3311;
    private Context mContext;
    private RegisterData mRegisterData = new RegisterData();

    private EditText mPhoneText;
    private EditText mPasswdText;
    private EditText mVerifyText;
    private Button mResendButton;
    private Button mRegisterBtn;

    private int mLeftTime = RESEND_TIME_IN_SEC;
    private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);
    private CustomProgressingDialog mProgressDialog;
    private String type = "mobile";
    private String mPhoneNum;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_register_phone);
        mContext = this;
        mPhoneNum = getIntent().getStringExtra(PHONE);

        initViews();
        initEvents();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callInputPanel();
            }
        }, 200);
    }

    private void initViews() {
        mPhoneText = (EditText) findViewById(R.id.input_phone);
        mPhoneText.setText(mPhoneNum);
        mPasswdText = (EditText) findViewById(R.id.input_passwd);
        mVerifyText = (EditText) findViewById(R.id.verify_code);
        mResendButton = (Button) findViewById(R.id.get_verify_code);
        mRegisterBtn = (Button) findViewById(R.id.register_login_btn);
    }

    private void callInputPanel() {
        // 唤起输入键盘 并输入框取得焦点
        mPasswdText.setFocusableInTouchMode(true);
        mPasswdText.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mPasswdText, 0);
    }

    private void initEvents() {
        mResendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLeftTime = 60;
                if (mLeftTime > 1) {
                    mLeftTime--;
                    mResendButton.setEnabled(false);
                    mResendButton.setText(mLeftTime + "s后重发");
                    mResendButton.setTextColor(Color.parseColor("#66090909"));
                    mHandler.sendEmptyMessageDelayed(MSG_TIMER, 1000);
                } else {
                    mLeftTime = RESEND_TIME_IN_SEC;
                    mResendButton.setEnabled(true);
                    mResendButton.setText("获取验证码");
                    mResendButton.setTextColor(Color.parseColor("#090909"));
                }

                if (mProgressDialog == null) {
                    mProgressDialog = new CustomProgressingDialog(
                            NewRegisterPhoneActivity.this);
                }
                if (!mProgressDialog
                        .isShowing()) {
                    mProgressDialog.show();
                }

                GetVerifyCodeRequest.Builder builder = new GetVerifyCodeRequest.Builder()
                        .setPhone(mPhoneText.getText().toString())
                        .setListener(
                                getVerifyCodeListener)
                        .setErrorListener(
                                errorListener);
                GetVerifyCodeRequest request = builder
                        .build();
                request.setTag(TAG);
                RequestQueue requestQueue = PSGodRequestQueue
                        .getInstance(
                                NewRegisterPhoneActivity.this)
                        .getRequestQueue();
                requestQueue.add(request);
            }
        });

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    mRegisterData.setThirdAuthType(type);
                    mRegisterData.setPhoneNumber(mPhoneText.getText().toString());
                    mRegisterData.setPassword(mPasswdText.getText().toString());
                    mRegisterData.setVerifyCode(mVerifyText.getText().toString());

                    // 显示等待对话框
                    if (mProgressDialog == null) {
                        mProgressDialog = new CustomProgressingDialog(
                                NewRegisterPhoneActivity.this);
                    }
                    if (!mProgressDialog.isShowing()) {
                        mProgressDialog.show();
                    }

                    RegisterRequest.Builder builder = new RegisterRequest.Builder()
                            .setRegisterData(mRegisterData)
                            .setErrorListener(errorListener)
                            .setListener(registerListener);
                    RegisterRequest request = builder.build();
                    request.setTag(TAG);
                    RequestQueue requestQueue = PSGodRequestQueue
                            .getInstance(NewRegisterPhoneActivity.this)
                            .getRequestQueue();
                    requestQueue.add(request);
                }
            }
        });

    }

    private Response.Listener<JSONObject> registerListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject data) {
            if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            showToast(new PSGodToast("注册成功"));

            if (data != null) {
                // 存储服务端返回的用户信息到sp
                LoginUser.getInstance().initFromJSONObject(data);

                Bundle extras = new Bundle();
                extras.putInt(Constants.IntentKey.ACTIVITY_JUMP_FROM,
                        JUMP_FROM_LOGIN_ACTIVITY);

                MainActivity.startNewActivityAndFinishAllBefore(
                        NewRegisterPhoneActivity.this,
                        MainActivity.class.getName(), extras);
            }
        }
    };

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == MSG_TIMER) {
            // 重发倒计时
            if (mLeftTime > 1) {
                mLeftTime--;
                mResendButton.setEnabled(false);
                mResendButton.setText(mLeftTime + "s后重发");
                mHandler.sendEmptyMessageDelayed(MSG_TIMER, 1000);
            } else {
                mLeftTime = RESEND_TIME_IN_SEC;
                mResendButton.setEnabled(true);
                mResendButton.setText("获取验证码");
                mResendButton.setTextColor(Color.parseColor("#090909"));
            }
        }
        return true;
    }

    private boolean validate() {
        // 手机号校验
        if (Utils.isNull(mPhoneText)) {
            Toast.makeText(NewRegisterPhoneActivity.this, "请填写手机号码", Toast.LENGTH_SHORT)
                    .show();
            mPhoneText.requestFocus();
            return false;
        }
        String phoneNum = mPhoneText.getText().toString().trim();
        if (!Utils.matchPhoneNum(phoneNum)) {
            Toast.makeText(NewRegisterPhoneActivity.this, "电话格式不正确", Toast.LENGTH_SHORT)
                    .show();
            mPhoneText.requestFocus();
            return false;
        }

        if (Utils.isNull(mPasswdText)) {
            Toast.makeText(NewRegisterPhoneActivity.this, "请填写登录密码", Toast.LENGTH_SHORT)
                    .show();
            mPasswdText.requestFocus();
            return false;
        }

        if (Utils.isNull(mVerifyText)) {
            Toast.makeText(NewRegisterPhoneActivity.this, "请填写验证码", Toast.LENGTH_SHORT)
                    .show();
            mVerifyText.requestFocus();
            return false;
        }
        return true;
    }

    // 获取到验证码之后的跳转 跳转到验证码验证页面
    private Response.Listener<Boolean> getVerifyCodeListener = new Response.Listener<Boolean>() {
        @Override
        public void onResponse(Boolean response) {
            if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    };

    private PSGodErrorListener errorListener = new PSGodErrorListener() {

        @Override
        public void handleError(VolleyError error) {
            if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }

    };

    /**
     * 暂停所有的下载
     */
    @Override
    public void onStop() {
        super.onStop();
        RequestQueue requestQueue = PSGodRequestQueue.getInstance(this)
                .getRequestQueue();
        requestQueue.cancelAll(this);
    }
}
