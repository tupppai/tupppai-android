package com.psgod.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.psgod.Constants;
import com.psgod.PSGodApplication;
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
 * Created by pires on 16/1/7.
 */
public class BindPhoneActivity extends PSGodBaseActivity {

    private static final String TAG = BindPhoneActivity.class.getSimpleName();
    private static final String PHONE = "PhoneNum";
    private static final int RESEND_TIME_IN_SEC = 60; // 重新发送验证时间（秒）
    private static final int MSG_TIMER = 0x3315;
    private Context mContext;

    private RegisterData mRegisterData = new RegisterData();

    private EditText mPhoneText;
    private EditText mCodeText;
    private EditText mPasswdText;
    private Button mResendButton;
    private Button mBindBtn;

    private int mLeftTime = RESEND_TIME_IN_SEC;
    private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);
    private CustomProgressingDialog mProgressDialog;

    @Override
    public void onCreate(Bundle savedInstancestate) {
        super.onCreate(savedInstancestate);
        setContentView(R.layout.activity_bind_phone);
        mContext = this;

        initViews();
        initEvents();
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
                            BindPhoneActivity.this);
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
                                BindPhoneActivity.this)
                        .getRequestQueue();
                requestQueue.add(request);
            }
        });

        mBindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    SharedPreferences sp = PSGodApplication.getAppContext()
                            .getSharedPreferences(Constants.SharedPreferencesKey.NAME,
                                    Context.MODE_PRIVATE);
                    // 显示等待对话框
                    if (mProgressDialog == null) {
                        mProgressDialog = new CustomProgressingDialog(
                                BindPhoneActivity.this);
                    }
                    if (!mProgressDialog.isShowing()) {
                        mProgressDialog.show();
                    }

                    String qqOpenId = sp.getString(Constants.ThirdAuthInfoSharedPreference.QQ_OPEN_ID, "");
                    if (!TextUtils.isEmpty(qqOpenId)) {
                        mRegisterData.setOpenId(sp.getString(Constants.ThirdAuthInfoSharedPreference.QQ_OPEN_ID, ""));
                        mRegisterData.setThirdAvatar(sp.getString(Constants.ThirdAuthInfoSharedPreference.QQ_AVATAR_URL, ""));
                        mRegisterData.setNickname(sp.getString(Constants.ThirdAuthInfoSharedPreference.QQ_NICKNAME, ""));

                        mRegisterData.setThirdAuthType("qq");
                        mRegisterData.setPhoneNumber(mPhoneText.getText().toString());
                        mRegisterData.setPassword(mPasswdText.getText().toString());
                        mRegisterData.setVerifyCode(mCodeText.getText().toString());
                        RegisterRequest.Builder builder = new RegisterRequest.Builder()
                                .setRegisterData(mRegisterData)
                                .setErrorListener(errorListener)
                                .setListener(new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject data) {
                                        showToast(new PSGodToast("绑定QQ成功"));

                                        if (data != null) {
                                            // 存储服务端返回的用户信息到sp
                                            LoginUser.getInstance().initFromJSONObject(data);
                                        }
                                    }
                                });
                        RegisterRequest request = builder.build();
                        request.setTag(TAG);
                        RequestQueue requestQueue = PSGodRequestQueue
                                .getInstance(BindPhoneActivity.this)
                                .getRequestQueue();
                        requestQueue.add(request);
                    }

                    String weixinOpenId = sp.getString(Constants.ThirdAuthInfoSharedPreference.WEIXIN_OPEN_ID, "");
                    if (!TextUtils.isEmpty(weixinOpenId)) {
                        mRegisterData.setOpenId(sp.getString(Constants.ThirdAuthInfoSharedPreference.WEIXIN_OPEN_ID, ""));
                        mRegisterData.setThirdAvatar(sp.getString(Constants.ThirdAuthInfoSharedPreference.WEIXIN_AVATAR_URL, ""));
                        mRegisterData.setNickname(sp.getString(Constants.ThirdAuthInfoSharedPreference.WEIXIN_NICKNAME, ""));

                        mRegisterData.setThirdAuthType("weixin");
                        mRegisterData.setPhoneNumber(mPhoneText.getText().toString());
                        mRegisterData.setPassword(mPasswdText.getText().toString());
                        mRegisterData.setVerifyCode(mCodeText.getText().toString());
                        RegisterRequest.Builder builder = new RegisterRequest.Builder()
                                .setRegisterData(mRegisterData)
                                .setErrorListener(errorListener)
                                .setListener(new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject data) {
                                        showToast(new PSGodToast("绑定微信成功"));

                                        if (data != null) {
                                            // 存储服务端返回的用户信息到sp
                                            LoginUser.getInstance().initFromJSONObject(data);
                                        }
                                    }
                                });
                        RegisterRequest request = builder.build();
                        request.setTag(TAG);
                        RequestQueue requestQueue = PSGodRequestQueue
                                .getInstance(BindPhoneActivity.this)
                                .getRequestQueue();
                        requestQueue.add(request);
                    }

                    String weiboOpenId = sp.getString(Constants.ThirdAuthInfoSharedPreference.WEIBO_OPEN_ID, "");
                    if (!TextUtils.isEmpty(weiboOpenId)) {
                        mRegisterData.setOpenId(sp.getString(Constants.ThirdAuthInfoSharedPreference.WEIBO_OPEN_ID, ""));
                        mRegisterData.setThirdAvatar(sp.getString(Constants.ThirdAuthInfoSharedPreference.WEIBO_AVATAR_URL, ""));
                        mRegisterData.setNickname(sp.getString(Constants.ThirdAuthInfoSharedPreference.WEIBO_NICKNAME, ""));

                        mRegisterData.setThirdAuthType("weibo");
                        mRegisterData.setPhoneNumber(mPhoneText.getText().toString());
                        mRegisterData.setPassword(mPasswdText.getText().toString());
                        mRegisterData.setVerifyCode(mCodeText.getText().toString());
                        RegisterRequest.Builder builder = new RegisterRequest.Builder()
                                .setRegisterData(mRegisterData)
                                .setErrorListener(errorListener)
                                .setListener(new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject data) {
                                        showToast(new PSGodToast("绑定微博成功"));

                                        if (data != null) {
                                            // 存储服务端返回的用户信息到sp
                                            LoginUser.getInstance().initFromJSONObject(data);
                                        }
                                    }
                                });
                        RegisterRequest request = builder.build();
                        request.setTag(TAG);
                        RequestQueue requestQueue = PSGodRequestQueue
                                .getInstance(BindPhoneActivity.this)
                                .getRequestQueue();
                        requestQueue.add(request);
                    }

                    if ((mProgressDialog != null) && (mProgressDialog.isShowing())) {
                        mProgressDialog.dismiss();
                    }

                    BindPhoneActivity.this.finish();

                }
            }
        });
    }

    private void initViews() {
        mPhoneText = (EditText) findViewById(R.id.input_phone);
        mCodeText = (EditText) findViewById(R.id.verify_code);
        mPasswdText = (EditText) findViewById(R.id.input_passwd);
        mResendButton = (Button) findViewById(R.id.get_verify_code);
        mBindBtn = (Button) findViewById(R.id.bind_btn);

    }

    private boolean validate() {
        // 手机号校验
        if (Utils.isNull(mPhoneText)) {
            Toast.makeText(BindPhoneActivity.this, "请填写手机号码", Toast.LENGTH_SHORT)
                    .show();
            mPhoneText.requestFocus();
            return false;
        }
        String phoneNum = mPhoneText.getText().toString().trim();
        if (!Utils.matchPhoneNum(phoneNum)) {
            Toast.makeText(BindPhoneActivity.this, "电话格式不正确", Toast.LENGTH_SHORT)
                    .show();
            mPhoneText.requestFocus();
            return false;
        }

        if (Utils.isNull(mPasswdText)) {
            Toast.makeText(BindPhoneActivity.this, "请填写登录密码", Toast.LENGTH_SHORT)
                    .show();
            mPasswdText.requestFocus();
            return false;
        }

        if (Utils.isNull(mCodeText)) {
            Toast.makeText(BindPhoneActivity.this, "请填写验证码", Toast.LENGTH_SHORT)
                    .show();
            mCodeText.requestFocus();
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
