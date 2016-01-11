package com.psgod.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
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
import com.psgod.PSGodApplication;
import com.psgod.PSGodToast;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.WeakReferenceHandler;
import com.psgod.eventbus.AvatarEvent;
import com.psgod.model.LoginUser;
import com.psgod.model.RegisterData;
import com.psgod.network.request.GetVerifyCodeRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.RegisterRequest;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import org.json.JSONObject;

import de.greenrobot.event.EventBus;

/**
 * Created by pires on 16/1/7.
 *
 * 绑定手机号
 */
public class BindPhoneActivity extends PSGodBaseActivity {

    private static final String TAG = BindPhoneActivity.class.getSimpleName();
    private static final String PHONE = "PhoneNum";
    private static final int RESEND_TIME_IN_SEC = 60; // 重新发送验证时间（秒）
    private static final int MSG_TIMER = 0x3315;
    private Context mContext;

    private RegisterData mRegisterData = new RegisterData();

    private ImageView mBackBtn;
    private EditText mPhoneText;
    private EditText mCodeText;
    private EditText mPasswdText;
    private Button mResendButton;
    private Button mBindBtn;

    private int mLeftTime = RESEND_TIME_IN_SEC;
    private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);
    private CustomProgressingDialog mProgressDialog;
    private String mPhoneNum;

    @Override
    public void onCreate(Bundle savedInstancestate) {
        super.onCreate(savedInstancestate);
        setContentView(R.layout.activity_bind_phone);
        mContext = this;
        mPhoneNum = getIntent().getStringExtra(PHONE);

        initViews();
        initEvents();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callInputPanel();
            }
        }, 100);
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

                    // 显示等待对话框
                    if (mProgressDialog == null) {
                        mProgressDialog = new CustomProgressingDialog(
                                BindPhoneActivity.this);
                    }
                    if (!mProgressDialog.isShowing()) {
                        mProgressDialog.show();
                    }

                    SharedPreferences sp = PSGodApplication.getAppContext()
                            .getSharedPreferences(Constants.SharedPreferencesKey.NAME,
                                    Context.MODE_PRIVATE);
                    String thirdAuthType = sp.getString(Constants.ThirdAuthInfo.THIRD_AUTH_PLATFORM, "qq");
                    String openId = sp.getString(Constants.ThirdAuthInfo.USER_OPENID, "");
                    if (!TextUtils.isEmpty(openId)) {
                        mRegisterData.setOpenId(openId);
                        mRegisterData.setThirdAvatar(sp.getString(Constants.ThirdAuthInfo.USER_AVATAR, ""));
                        mRegisterData.setNickname(sp.getString(Constants.ThirdAuthInfo.USER_NICKNAME, ""));

                        mRegisterData.setThirdAuthType(thirdAuthType);
                        mRegisterData.setPhoneNumber(mPhoneText.getText().toString());
                        mRegisterData.setPassword(mPasswdText.getText().toString());
                        mRegisterData.setVerifyCode(mCodeText.getText().toString());
                        RegisterRequest.Builder builder = new RegisterRequest.Builder()
                                .setRegisterData(mRegisterData)
                                .setErrorListener(errorListener)
                                .setListener(new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject data) {
                                        showToast(new PSGodToast("绑定成功"));
                                        // 更新tab头像
                                        EventBus.getDefault().post(new AvatarEvent());

                                        if (data != null) {
                                            // 存储服务端返回的用户信息到sp
                                            LoginUser.getInstance().initFromJSONObject(data);
                                        }

                                        if ((mProgressDialog != null) && (mProgressDialog.isShowing())) {
                                            mProgressDialog.dismiss();
                                        }

                                        BindPhoneActivity.this.finish();
                                    }
                                });
                        RegisterRequest request = builder.build();
                        request.setTag(TAG);
                        RequestQueue requestQueue = PSGodRequestQueue
                                .getInstance(BindPhoneActivity.this)
                                .getRequestQueue();
                        requestQueue.add(request);
                    }

                }
            }
        });

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BindPhoneActivity.this.finish();
            }
        });
    }

    private void initViews() {
        mBackBtn = (ImageView) findViewById(R.id.ic_back);
        mPhoneText = (EditText) findViewById(R.id.input_phone);
        mPhoneText.setText(mPhoneNum);
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

    private void callInputPanel() {
        // 唤起输入键盘 并输入框取得焦点
        mCodeText.setFocusableInTouchMode(true);
        mCodeText.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mCodeText, 0);
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
            Toast.makeText(mContext,"绑定失败", Toast.LENGTH_SHORT).show();
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
