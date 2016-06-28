package com.psgod.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.Selection;
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
import com.psgod.CustomToast;
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
import com.psgod.network.request.QQLoginRequest;
import com.psgod.network.request.RegisterRequest;
import com.psgod.network.request.WechatUserInfoRequest;
import com.psgod.network.request.WeiboLoginRequest;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

/**
 * Created by pires on 16/1/4.
 * 第二步，注册页
 */
public class NewRegisterPhoneActivity extends PSGodBaseActivity{
    private static final String TAG = NewRegisterPhoneActivity.class.getSimpleName();
    private static final String PHONE = "PhoneNum";
    private static final int JUMP_FROM_LOGIN_ACTIVITY = 100;
    private static final int RESEND_TIME_IN_SEC = 60; // 重新发送验证时间（秒）
    private static final int MSG_TIMER = 0x3311;
    private static final int MSG_CODE = 0x3333;
    private static final String QQPLAT = "qq";
    private static final String WEIBOPLAT = "weibo";
    private static final String WEIXINPLAT = "weixin";
    private Context mContext;
    private RegisterData mRegisterData = new RegisterData();

    private EditText mPhoneText;
    private EditText mPasswdText;
    private EditText mVerifyText;
    private Button mResendButton;
    private Button mRegisterBtn;

    private ImageView mWeiboLoginBtn;
    private ImageView mWechatLoginBtn;
    private ImageView mQQLoginBtn;

    private String mThirdAuthId = "";
    private String mThirdAuthName = "";
    private String mThirdAuthGender = "";
    private String mThirdAuthAvatar = "";
    private String mThirdAuthUnion = "";

    private int mLeftTime = RESEND_TIME_IN_SEC;
    private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);
    private CustomProgressingDialog mProgressDialog;
    private String type = "mobile";
    private String mPhoneNum;

    private BroadcastReceiver smsReceiver;
    private IntentFilter filter;
    private String patternCoder = "(?<!\\d)\\d{4}(?!\\d)";

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

        codeReceiver();
    }

    private void initViews() {
        mPhoneText = (EditText) findViewById(R.id.input_phone);
        mPhoneText.setText(mPhoneNum);
        mPasswdText = (EditText) findViewById(R.id.input_passwd);
        mVerifyText = (EditText) findViewById(R.id.verify_code);
        mResendButton = (Button) findViewById(R.id.get_verify_code);
        mRegisterBtn = (Button) findViewById(R.id.register_login_btn);

        mWeiboLoginBtn = (ImageView) findViewById(R.id.weibo_login);
        mWechatLoginBtn = (ImageView) findViewById(R.id.weixin_login);
        mQQLoginBtn = (ImageView) findViewById(R.id.qq_login);
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

    private void callInputPanel() {
        // 唤起输入键盘 并输入框取得焦点
        mPasswdText.setFocusableInTouchMode(true);
        mPasswdText.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mPasswdText, 0);
    }

    private void initEvents() {
        // QQ登录
        mQQLoginBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // 显示等待对话框
                if (mProgressDialog == null) {
                    mProgressDialog = new CustomProgressingDialog(
                            NewRegisterPhoneActivity.this);
                }
                if (!mProgressDialog.isShowing()) {
                    mProgressDialog.show();
                }

                Platform qq = ShareSDK.getPlatform(QQ.NAME);
                qq.SSOSetting(false);
                qq.setPlatformActionListener(qqLoginListener);
                if (qq.isValid()) {
                    qq.removeAccount();
                    ShareSDK.removeCookieOnAuthorize(true);
                }
                qq.showUser(null);
            }
        });

        // 微博登录
        mWeiboLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 显示等待对话框
                if (mProgressDialog == null) {
                    mProgressDialog = new CustomProgressingDialog(
                            NewRegisterPhoneActivity.this);
                }
                if (!mProgressDialog.isShowing()) {
                    mProgressDialog.show();
                }

                Platform weibo = ShareSDK.getPlatform(SinaWeibo.NAME);

                weibo.SSOSetting(false);
                weibo.setPlatformActionListener(weiboLoginListener);
                if (weibo.isValid()) {
                    weibo.removeAccount();
                    ShareSDK.removeCookieOnAuthorize(true);
                }
                weibo.showUser(null);
            }
        });

        // 微信授权回调
        final Response.Listener<WechatUserInfoRequest.WechatUserWrapper> wechatAuthListener = new Response.Listener<WechatUserInfoRequest.WechatUserWrapper>() {
            @Override
            public void onResponse(WechatUserInfoRequest.WechatUserWrapper response) {
                if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }

                try {
                    int isRegistered = response.isRegistered;
                    // 已注册
                    if (isRegistered == 1) {
                        JSONObject userInfoData = response.UserObject;
                        LoginUser.getInstance()
                                .initFromJSONObject(userInfoData);

                        Bundle extras = new Bundle();
                        extras.putInt(Constants.IntentKey.ACTIVITY_JUMP_FROM,
                                JUMP_FROM_LOGIN_ACTIVITY);
                        MainActivity.startNewActivityAndFinishAllBefore(
                                NewRegisterPhoneActivity.this,
                                MainActivity.class.getName(), extras);
                    }
                    // 未注册
                    if (isRegistered == 0) {
                        SharedPreferences.Editor editor = PSGodApplication
                                .getAppContext()
                                .getSharedPreferences(Constants.SharedPreferencesKey.NAME,
                                        Context.MODE_PRIVATE).edit();
                        editor.putString(Constants.ThirdAuthInfo.THIRD_AUTH_PLATFORM,WEIXINPLAT);
                        editor.putString(Constants.ThirdAuthInfo.USER_OPENID, mThirdAuthId);
                        editor.putString(Constants.ThirdAuthInfo.USER_AVATAR, mThirdAuthAvatar);
                        editor.putString(Constants.ThirdAuthInfo.USER_NICKNAME, mThirdAuthName);
                        if (android.os.Build.VERSION.SDK_INT >= 9) {
                            editor.apply();
                        } else {
                            editor.commit();
                        }

                        JSONObject userInfoData = new JSONObject();
                        userInfoData.put("uid", 0l);
                        userInfoData.put("nickname", mThirdAuthName);
                        userInfoData.put("sex", 0);
                        userInfoData.put("phone", "0");
                        userInfoData.put("avatar", mThirdAuthAvatar);
                        userInfoData.put("fans_count", 0);
                        userInfoData.put("fellow_count", 0);
                        userInfoData.put("uped_count", 0);
                        userInfoData.put("ask_count", 0);
                        userInfoData.put("reply_count", 0);
                        userInfoData.put("inprogress_count", 0);
                        userInfoData.put("collection_count", 0);
                        userInfoData.put("is_bound_weixin", 0);
                        userInfoData.put("is_bound_qq", 0);
                        userInfoData.put("is_bound_weibo", 0);
                        userInfoData.put("city", 1);
                        userInfoData.put("province", 11);

                        LoginUser.getInstance().initFromJSONObject(userInfoData);

                        Bundle extras = new Bundle();
                        extras.putInt(Constants.IntentKey.ACTIVITY_JUMP_FROM,
                                JUMP_FROM_LOGIN_ACTIVITY);
                        MainActivity.startNewActivityAndFinishAllBefore(
                                NewRegisterPhoneActivity.this, MainActivity.class.getName(),
                                extras);
//						Intent intent = new Intent(LoginActivity.this,
//								SetInfoActivity.class);
//
//						intent.putExtra(
//								Constants.ThirdAuthInfo.THIRD_AUTH_PLATFORM,
//								"weixin");
//						intent.putExtra(Constants.ThirdAuthInfo.USER_OPENID,
//								mThirdAuthId);
//						intent.putExtra(Constants.ThirdAuthInfo.USER_AVATAR,
//								mThirdAuthAvatar);
//						intent.putExtra(Constants.ThirdAuthInfo.USER_GENDER,
//								mThirdAuthGender);
//						intent.putExtra(Constants.ThirdAuthInfo.USER_NICKNAME,
//								mThirdAuthName);
//
//						startActivity(intent);
//						finish();
                    }
                } catch (Exception e) {
                }
            }
        };

        // 微信登录
        mWechatLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 显示等待对话框
                if (mProgressDialog == null) {
                    mProgressDialog = new CustomProgressingDialog(
                            NewRegisterPhoneActivity.this);
                }
                if (!mProgressDialog.isShowing()) {
                    mProgressDialog.show();
                }

                // 微信授权
                Platform weixin = ShareSDK.getPlatform(Wechat.NAME);
                if (weixin.isValid()) {
                    weixin.removeAccount();
                    ShareSDK.removeCookieOnAuthorize(true);
                }
                weixin.setPlatformActionListener(new PlatformActionListener() {
                    @Override
                    public void onError(Platform arg0, int arg1, Throwable arg2) {
                        Looper.prepare();
                        if (mProgressDialog != null
                                && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        CustomToast.show(NewRegisterPhoneActivity.this, "微信登录异常",
                                Toast.LENGTH_LONG);
                        Looper.loop();
                    }

                    @Override
                    public void onComplete(Platform platform, int action,
                                           HashMap<String, Object> res) {
                        mThirdAuthId = res.get("openid").toString();
                        mThirdAuthUnion  = res.get("unionid").toString();
                        mThirdAuthAvatar = res.get("headimgurl").toString();
                        mThirdAuthGender = res.get("sex").toString();
                        mThirdAuthName = res.get("nickname").toString();

//                        Toast.makeText(NewRegisterPhoneActivity.this,mThirdAuthUnion,Toast.LENGTH_SHORT).show();

                        // 验证code是否被注册
                        if (!TextUtils.isEmpty(mThirdAuthId)) {
                            WechatUserInfoRequest.Builder builder = new WechatUserInfoRequest.Builder()
                                    .setCode(mThirdAuthId)
                                    .setUnionId(mThirdAuthUnion)
                                    .setListener(wechatAuthListener)
                                    .setErrorListener(errorListener);

                            WechatUserInfoRequest request = builder.build();
                            request.setTag(TAG);
                            RequestQueue requestQueue = PSGodRequestQueue
                                    .getInstance(NewRegisterPhoneActivity.this)
                                    .getRequestQueue();
                            requestQueue.add(request);
                        }
                    }

                    @Override
                    public void onCancel(Platform arg0, int arg1) {
                        if (mProgressDialog != null
                                && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        CustomToast.show(NewRegisterPhoneActivity.this, "取消微信登录",
                                Toast.LENGTH_LONG);
                    }
                });
                weixin.SSOSetting(true);
                weixin.showUser(null);
            }
        });

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

    // QQ登录监听事件
    private PlatformActionListener qqLoginListener = new PlatformActionListener() {

        @Override
        public void onError(Platform arg0, int arg1, Throwable arg2) {
            if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }

        @Override
        public void onComplete(Platform arg0, int arg1,
                               HashMap<String, Object> res) {
            mThirdAuthId = ShareSDK.getPlatform(QQ.NAME).getDb().getUserId();
            mThirdAuthName = res.get("nickname").toString();
            mThirdAuthAvatar = res.get("figureurl_qq_2").toString();

            if (!TextUtils.isEmpty(mThirdAuthId)) {
                QQLoginRequest.Builder builder = new QQLoginRequest.Builder()
                        .setCode(mThirdAuthId).setListener(qqAuthListener)
                        .setErrorListener(errorListener);

                QQLoginRequest request = builder.build();
                RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                        NewRegisterPhoneActivity.this).getRequestQueue();
                requestQueue.add(request);
            }
        }

        @Override
        public void onCancel(Platform arg0, int arg1) {
            // TODO Auto-generated method stub
            if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    };

    private Response.Listener<QQLoginRequest.QQLoginWrapper> qqAuthListener = new Response.Listener<QQLoginRequest.QQLoginWrapper>() {

        @Override
        public void onResponse(QQLoginRequest.QQLoginWrapper response) {
            if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }

            try {
                int isRegistered = response.isRegistered;
                // 已注册
                if (isRegistered == 1) {
                    JSONObject userInfoData = response.UserObject;
                    LoginUser.getInstance().initFromJSONObject(userInfoData);

                    Bundle extras = new Bundle();
                    extras.putInt(Constants.IntentKey.ACTIVITY_JUMP_FROM,
                            LoginActivity.JUMP_FROM_LOGIN);

                    // 关闭之前所有页面
                    MainActivity.startNewActivityAndFinishAllBefore(
                            NewRegisterPhoneActivity.this, MainActivity.class.getName(),
                            extras);
                }
                // 未注册
                if (isRegistered == 0) {
                    SharedPreferences.Editor editor = PSGodApplication
                            .getAppContext()
                            .getSharedPreferences(Constants.SharedPreferencesKey.NAME,
                                    Context.MODE_PRIVATE).edit();
                    editor.putString(Constants.ThirdAuthInfo.THIRD_AUTH_PLATFORM,QQPLAT);
                    editor.putString(Constants.ThirdAuthInfo.USER_OPENID, mThirdAuthId);
                    editor.putString(Constants.ThirdAuthInfo.USER_AVATAR, mThirdAuthAvatar);
                    editor.putString(Constants.ThirdAuthInfo.USER_NICKNAME, mThirdAuthName);
                    if (android.os.Build.VERSION.SDK_INT >= 9) {
                        editor.apply();
                    } else {
                        editor.commit();
                    }

                    JSONObject userInfoData = new JSONObject();
                    userInfoData.put("uid", 0l);
                    userInfoData.put("nickname", mThirdAuthName);
                    userInfoData.put("sex", 0);
                    userInfoData.put("phone", "0");
                    userInfoData.put("avatar", mThirdAuthAvatar);
                    userInfoData.put("fans_count", 0);
                    userInfoData.put("fellow_count", 0);
                    userInfoData.put("uped_count", 0);
                    userInfoData.put("ask_count", 0);
                    userInfoData.put("reply_count", 0);
                    userInfoData.put("inprogress_count", 0);
                    userInfoData.put("collection_count", 0);
                    userInfoData.put("is_bound_weixin", 0);
                    userInfoData.put("is_bound_qq", 0);
                    userInfoData.put("is_bound_weibo", 0);
                    userInfoData.put("city", 1);
                    userInfoData.put("province", 11);

                    LoginUser.getInstance().initFromJSONObject(userInfoData);

                    Bundle extras = new Bundle();
                    extras.putInt(Constants.IntentKey.ACTIVITY_JUMP_FROM,
                            JUMP_FROM_LOGIN_ACTIVITY);
                    MainActivity.startNewActivityAndFinishAllBefore(
                            NewRegisterPhoneActivity.this, MainActivity.class.getName(),
                            extras);
//					Intent intent = new Intent(LoginActivity.this,
//							SetInfoActivity.class);
//
//					intent.putExtra(
//							Constants.ThirdAuthInfo.THIRD_AUTH_PLATFORM, "qq");
//					intent.putExtra(Constants.ThirdAuthInfo.USER_OPENID,
//							mThirdAuthId);
//					intent.putExtra(Constants.ThirdAuthInfo.USER_AVATAR,
//							mThirdAuthAvatar);
//					intent.putExtra(Constants.ThirdAuthInfo.USER_NICKNAME,
//							mThirdAuthName);
//
//					LoginActivity.this.startActivity(intent);
                }
            } catch (Exception e) {
            }
        }
    };

    // 微博登录的监听事件
    private PlatformActionListener weiboLoginListener = new PlatformActionListener() {
        @Override
        public void onError(Platform arg0, int arg1, Throwable arg2) {
            if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }

        // 用户授权
        @Override
        public void onComplete(Platform arg0, int arg1,
                               HashMap<String, Object> res) {
            mThirdAuthId = res.get("id").toString();
            mThirdAuthName = res.get("name").toString();
            mThirdAuthAvatar = res.get("profile_image_url").toString();

            if (!TextUtils.isEmpty(mThirdAuthId)) {
                WeiboLoginRequest.Builder builder = new WeiboLoginRequest.Builder()
                        .setCode(mThirdAuthId).setListener(weiboAuthListener)
                        .setErrorListener(errorListener);

                WeiboLoginRequest request = builder.build();
                request.setTag(TAG);
                RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                        NewRegisterPhoneActivity.this).getRequestQueue();
                requestQueue.add(request);
            }
        }

        @Override
        public void onCancel(Platform arg0, int arg1) {
            // TODO Auto-generated method stub
            if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    };

    private Response.Listener<WeiboLoginRequest.WeiboLoginWrapper> weiboAuthListener = new Response.Listener<WeiboLoginRequest.WeiboLoginWrapper>() {
        @Override
        public void onResponse(WeiboLoginRequest.WeiboLoginWrapper response) {
            if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }

            try {
                int isRegistered = response.isRegistered;
                // 已注册
                if (isRegistered == 1) {
                    JSONObject userInfoData = response.UserObject;
                    LoginUser.getInstance().initFromJSONObject(userInfoData);

                    Bundle extras = new Bundle();
                    extras.putInt(Constants.IntentKey.ACTIVITY_JUMP_FROM,
                            JUMP_FROM_LOGIN_ACTIVITY);
                    MainActivity.startNewActivityAndFinishAllBefore(
                            NewRegisterPhoneActivity.this, MainActivity.class.getName(),
                            extras);
                }
                // 未注册
                if (isRegistered == 0) {

                    SharedPreferences.Editor editor = PSGodApplication
                            .getAppContext()
                            .getSharedPreferences(Constants.SharedPreferencesKey.NAME,
                                    Context.MODE_PRIVATE).edit();
                    editor.putString(Constants.ThirdAuthInfo.THIRD_AUTH_PLATFORM,WEIBOPLAT);
                    editor.putString(Constants.ThirdAuthInfo.USER_OPENID, mThirdAuthId);
                    editor.putString(Constants.ThirdAuthInfo.USER_AVATAR, mThirdAuthAvatar);
                    editor.putString(Constants.ThirdAuthInfo.USER_NICKNAME, mThirdAuthName);
                    if (android.os.Build.VERSION.SDK_INT >= 9) {
                        editor.apply();
                    } else {
                        editor.commit();
                    }

                    JSONObject userInfoData = new JSONObject();
                    userInfoData.put("uid", 0l);
                    userInfoData.put("nickname", mThirdAuthName);
                    userInfoData.put("sex", 0);
                    userInfoData.put("phone", "0");
                    userInfoData.put("avatar", mThirdAuthAvatar);
                    userInfoData.put("fans_count", 0);
                    userInfoData.put("fellow_count", 0);
                    userInfoData.put("uped_count", 0);
                    userInfoData.put("ask_count", 0);
                    userInfoData.put("reply_count", 0);
                    userInfoData.put("inprogress_count", 0);
                    userInfoData.put("collection_count", 0);
                    userInfoData.put("is_bound_weixin", 0);
                    userInfoData.put("is_bound_qq", 0);
                    userInfoData.put("is_bound_weibo", 0);
                    userInfoData.put("city", 1);
                    userInfoData.put("province", 11);

                    LoginUser.getInstance().initFromJSONObject(userInfoData);

                    Bundle extras = new Bundle();
                    extras.putInt(Constants.IntentKey.ACTIVITY_JUMP_FROM,
                            JUMP_FROM_LOGIN_ACTIVITY);
                    MainActivity.startNewActivityAndFinishAllBefore(
                            NewRegisterPhoneActivity.this, MainActivity.class.getName(),
                            extras);
//					Intent intent = new Intent(LoginActivity.this,
//							SetInfoActivity.class);
//
//					intent.putExtra(
//							Constants.ThirdAuthInfo.THIRD_AUTH_PLATFORM,
//							"weibo");
//					intent.putExtra(Constants.ThirdAuthInfo.USER_OPENID,
//							mThirdAuthId);
//					intent.putExtra(Constants.ThirdAuthInfo.USER_NICKNAME,
//							mThirdAuthName);
//					intent.putExtra(Constants.ThirdAuthInfo.USER_AVATAR,
//							mThirdAuthAvatar);
//
//					startActivity(intent);
//					finish();
                }
            } catch (Exception e) {

            }
        }

    };

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
        switch(msg.what) {
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
            case MSG_CODE:
                String codeMsg=msg.getData().getString("messagecode");
                if(codeMsg != null && codeMsg.length() >= 4 && codeMsg.length() <=6) {
                    mVerifyText.setText(codeMsg);
                }
                Editable etext = mPasswdText.getText();
                Selection.setSelection(etext, etext.length());
                break;

            default:
                break;

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
