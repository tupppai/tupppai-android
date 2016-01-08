package com.psgod.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.WeakReferenceHandler;
import com.psgod.model.LoginUser;
import com.psgod.network.request.GetVerifyCodeRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.ResetPasswordRequest;
import com.psgod.ui.widget.dialog.CustomDialog;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pires on 16/1/5.
 */
public class NewResetPasswdActivity extends PSGodBaseActivity{

    private static final String TAG = NewResetPasswdActivity.class.getSimpleName();
    private static final String PHONE = "PhoneNum";
    private static final int JUMP_FROM_LOGIN_ACTIVITY = 100;
    private static final int RESEND_TIME_IN_SEC = 60; // 重新发送验证时间（秒）
    private static final int MSG_TIMER = 0x3312;
    private Context mContext;

    private EditText mPhoneText;
    private EditText mVerifyText;
    private Button mResendButton;
    private EditText mPasswdText;
    private Button mLoginBtn;
    private ImageView mBackBtn;

    private int mLeftTime = RESEND_TIME_IN_SEC;
    private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);
    private CustomProgressingDialog mProgressDialog;

    private String mPhoneNum;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_reset_passwd);
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
        mVerifyText = (EditText) findViewById(R.id.verify_code);
        mResendButton = (Button) findViewById(R.id.get_verify_code);
        mPasswdText = (EditText) findViewById(R.id.input_passwd);
        mLoginBtn = (Button) findViewById(R.id.sure_login_btn);
        mBackBtn = (ImageView) findViewById(R.id.back_image);
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
                            NewResetPasswdActivity.this);
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
                                NewResetPasswdActivity.this)
                        .getRequestQueue();
                requestQueue.add(request);
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    // 显示等待对话框
                    if (mProgressDialog == null) {
                        mProgressDialog = new CustomProgressingDialog(
                                NewResetPasswdActivity.this);
                    }
                    if (!mProgressDialog.isShowing()) {
                        mProgressDialog.show();
                    }

                    String mVerifyCode = mVerifyText.getText().toString();
                    String mNewPassword = mPasswdText.getText().toString();
                    ResetPasswordRequest.Builder builder = new ResetPasswordRequest.Builder()
                            .setPhoneNumber(mPhoneNum)
                            .setNewPassword(mNewPassword)
                            .setVerifyCode(mVerifyCode).setListener(listener)
                            .setErrorListener(errorListener);

                    ResetPasswordRequest request = builder.build();
                    request.setTag(TAG);
                    RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                            NewResetPasswdActivity.this).getRequestQueue();
                    requestQueue.add(request);
                }
            }
        });

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewResetPasswdActivity.this.finish();
            }
        });
    }

    private Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            if (response != null) {
                // 取消等待框
                if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                try {
                    if (response.getInt("status") == 1) {
                        // 存储服务端返回的用户信息到sp
                        LoginUser.getInstance().initFromJSONObject(response);

                        Toast.makeText(NewResetPasswdActivity.this, "重置密码成功",
                                Toast.LENGTH_SHORT).show();

                        Bundle extras = new Bundle();
                        extras.putInt(Constants.IntentKey.ACTIVITY_JUMP_FROM,
                                JUMP_FROM_LOGIN_ACTIVITY);
                        MainActivity.startNewActivityAndFinishAllBefore(
                                NewResetPasswdActivity.this,
                                MainActivity.class.getName(), extras);
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    };

    private boolean validate() {
        // 手机号校验
        if (Utils.isNull(mPhoneText)) {
            Toast.makeText(NewResetPasswdActivity.this, "请填写手机号码", Toast.LENGTH_SHORT)
                    .show();
            mPhoneText.requestFocus();
            return false;
        }
        String phoneNum = mPhoneText.getText().toString().trim();
        if (!Utils.matchPhoneNum(phoneNum)) {
            Toast.makeText(NewResetPasswdActivity.this, "电话格式不正确", Toast.LENGTH_SHORT)
                    .show();
            mPhoneText.requestFocus();
            return false;
        }

        if (Utils.isNull(mVerifyText)) {
            Toast.makeText(NewResetPasswdActivity.this, "请填写验证码", Toast.LENGTH_SHORT)
                    .show();
            mVerifyText.requestFocus();
            return false;
        }

        if (Utils.isNull(mPasswdText)) {
            Toast.makeText(NewResetPasswdActivity.this, "请填写密码", Toast.LENGTH_SHORT)
                    .show();
            mPasswdText.requestFocus();
            return false;
        }

        return true;
    }

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

    // 获取到验证码
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

    private void callInputPanel() {
        // 唤起输入键盘 并输入框取得焦点
        mVerifyText.setFocusableInTouchMode(true);
        mVerifyText.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mVerifyText, 0);
    }
}
