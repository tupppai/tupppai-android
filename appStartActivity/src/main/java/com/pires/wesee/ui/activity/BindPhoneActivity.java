package com.pires.wesee.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.telephony.SmsMessage;
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
import com.pires.wesee.Constants;
import com.pires.wesee.PSGodApplication;
import com.pires.wesee.PSGodToast;
import com.pires.wesee.Utils;
import com.pires.wesee.WeakReferenceHandler;
import com.pires.wesee.eventbus.AvatarEvent;
import com.pires.wesee.eventbus.InitEvent;
import com.pires.wesee.eventbus.MyPageRefreshEvent;
import com.pires.wesee.eventbus.RefreshEvent;
import com.pires.wesee.model.LoginUser;
import com.pires.wesee.model.RegisterData;
import com.pires.wesee.network.request.GetVerifyCodeRequest;
import com.pires.wesee.network.request.PSGodErrorListener;
import com.pires.wesee.network.request.PSGodRequestQueue;
import com.pires.wesee.ui.fragment.HomePageFocusFragment;
import com.pires.wesee.ui.widget.dialog.CustomProgressingDialog;
import com.pires.wesee.R;
import com.pires.wesee.eventbus.BindEvent;
import com.pires.wesee.network.request.RegisterRequest;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.greenrobot.event.EventBus;

/**
 * Created by pires on 16/1/7.
 * <p/>
 * 绑定手机号
 */
public class BindPhoneActivity extends PSGodBaseActivity {

    private static final String TAG = BindPhoneActivity.class.getSimpleName();
    private static final String PHONE = "PhoneNum";
    private static final int RESEND_TIME_IN_SEC = 60; // 重新发送验证时间（秒）
    private static final int MSG_TIMER = 0x3315;
    private static final int MSG_CODE = 0x3333;
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

    private BroadcastReceiver smsReceiver;
    private IntentFilter filter;
    private String patternCoder = "(?<!\\d)\\d{4}(?!\\d)";

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

        codeReceiver();
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
                                        if (data != null) {
                                            // 存储服务端返回的用户信息到sp
                                            LoginUser.getInstance().initFromJSONObject(data);
                                        }
                                        // 更新tab头像
                                        EventBus.getDefault().post(new AvatarEvent());
                                        EventBus.getDefault().post(new RefreshEvent(HomePageFocusFragment.class.getName()));
                                        EventBus.getDefault().post(new MyPageRefreshEvent(MyPageRefreshEvent.ASK));
                                        EventBus.getDefault().post(new MyPageRefreshEvent(MyPageRefreshEvent.REPLY));
                                        EventBus.getDefault().post(new MyPageRefreshEvent(MyPageRefreshEvent.WORK));
                                        EventBus.getDefault().post(new MyPageRefreshEvent(MyPageRefreshEvent.COLLECTION));
                                        EventBus.getDefault().post(new InitEvent());
                                        EventBus.getDefault().post(new BindEvent(BindEvent.State.OK));

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
        switch (msg.what) {
            case MSG_TIMER:
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
                break;
            case MSG_CODE:
                String codeMsg = msg.getData().getString("messagecode");
                if (codeMsg != null && codeMsg.length() >= 4 && codeMsg.length() <= 6) {
                    mCodeText.setText(codeMsg);
                }
                mPasswdText.setFocusableInTouchMode(true);
                mPasswdText.requestFocus();
                break;

            default:
                break;

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

    private PSGodErrorListener errorListener = new PSGodErrorListener(this) {

        @Override
        public void handleError(VolleyError error) {
            if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            Toast.makeText(mContext, "绑定失败", Toast.LENGTH_SHORT).show();
        }

    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void finish() {
        EventBus.getDefault().post(new BindEvent(BindEvent.State.FINISH));
        super.finish();
    }

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
