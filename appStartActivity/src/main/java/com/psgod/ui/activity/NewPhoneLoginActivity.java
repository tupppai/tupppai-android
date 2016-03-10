package com.psgod.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.psgod.Constants;
import com.psgod.CustomToast;
import com.psgod.PSGodApplication;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.WeakReferenceHandler;
import com.psgod.model.LoginUser;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.QQLoginRequest;
import com.psgod.network.request.UserLoginRequest;
import com.psgod.network.request.WechatUserInfoRequest;
import com.psgod.network.request.WeiboLoginRequest;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

/**
 * Created by pires on 16/1/4.
 * 登录第二步，输入密码页
 */
public class NewPhoneLoginActivity extends PSGodBaseActivity {
    private static final String TAG = NewPhoneLoginActivity.class.getSimpleName();
    private static final String PHONE = "PhoneNum";
    private static final int JUMP_FROM_LOGIN = 100;
    private static final String QQPLAT = "qq";
    private static final String WEIBOPLAT = "weibo";
    private static final String WEIXINPLAT = "weixin";
    private Context mContext;

    private TextView mPhoneText;
    private EditText mPasswdText;
    private Button mLoginBtn;
    private ImageView mResetBtn;
    private ImageView mWeiboLoginBtn;
    private ImageView mWechatLoginBtn;
    private ImageView mQQLoginBtn;
    private ScrollView mParent;

    private String mThirdAuthId = "";
    private String mThirdToken = "";
    private String mThirdAuthName = "";
    private String mThirdAuthGender = "";
    private String mThirdAuthAvatar = "";

    private CustomProgressingDialog mProgressDialog;
    private WeakReferenceHandler handler = new WeakReferenceHandler(this);
    private String mPhoneNum;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_login_phone);
        mContext = this;
        mPhoneNum = getIntent().getStringExtra(PHONE);

        initViews();
        initEvents();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callInputPanel();
            }
        }, 200);

    }

    private void initViews() {
        mPhoneText = (TextView) findViewById(R.id.input_phone);
        mPhoneText.setText(mPhoneNum);
        mPasswdText = (EditText) findViewById(R.id.input_passwd);
        mLoginBtn = (Button) findViewById(R.id.login_btn);
        mResetBtn = (ImageView) findViewById(R.id.forget_passwd);
        mParent = (ScrollView) findViewById(R.id.parent);

        mWeiboLoginBtn = (ImageView) findViewById(R.id.weibo_login);
        mWechatLoginBtn = (ImageView) findViewById(R.id.weixin_login);
        mQQLoginBtn = (ImageView) findViewById(R.id.qq_login);

    }

    private void initEvents() {
        mParent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Utils.hideInputPanel(NewPhoneLoginActivity.this, view);
                return false;
            }
        });
//        mParent.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//            }
//        });
        // QQ登录
        mQQLoginBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // 显示等待对话框
                if (mProgressDialog == null) {
                    mProgressDialog = new CustomProgressingDialog(
                            NewPhoneLoginActivity.this);
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
                            NewPhoneLoginActivity.this);
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
                                JUMP_FROM_LOGIN);
                        MainActivity.startNewActivityAndFinishAllBefore(
                                NewPhoneLoginActivity.this,
                                MainActivity.class.getName(), extras);
                    }
                    // 未注册
                    if (isRegistered == 0) {
                        SharedPreferences.Editor editor = PSGodApplication
                                .getAppContext()
                                .getSharedPreferences(Constants.SharedPreferencesKey.NAME,
                                        Context.MODE_PRIVATE).edit();
                        editor.putString(Constants.ThirdAuthInfo.THIRD_AUTH_PLATFORM, WEIXINPLAT);
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
                                JUMP_FROM_LOGIN);
                        MainActivity.startNewActivityAndFinishAllBefore(
                                NewPhoneLoginActivity.this, MainActivity.class.getName(),
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
                            NewPhoneLoginActivity.this);
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
                        CustomToast.show(NewPhoneLoginActivity.this, "微信登录异常",
                                Toast.LENGTH_LONG);
                        Looper.loop();
                    }

                    @Override
                    public void onComplete(Platform platform, int action,
                                           HashMap<String, Object> res) {
                        mThirdAuthId = res.get("openid").toString();
//                        mThirdToken = platform.getDb().getToken();
//                        Toast.makeText(NewPhoneLoginActivity.this, mThirdAuthId, Toast.LENGTH_LONG);
                        mThirdAuthAvatar = res.get("headimgurl").toString();
                        mThirdAuthGender = res.get("sex").toString();
                        mThirdAuthName = res.get("nickname").toString();

                        // 验证code是否被注册
                        if (!TextUtils.isEmpty(mThirdAuthId)) {
                            WechatUserInfoRequest.Builder builder = new WechatUserInfoRequest.Builder()
                                    .setCode(mThirdAuthId)
                                    .setListener(wechatAuthListener)
//                                    .setToken(mThirdToken)
                                    .setErrorListener(errorListener);

                            WechatUserInfoRequest request = builder.build();
                            request.setTag(TAG);
                            RequestQueue requestQueue = PSGodRequestQueue
                                    .getInstance(NewPhoneLoginActivity.this)
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
                        CustomToast.show(NewPhoneLoginActivity.this, "取消微信登录",
                                Toast.LENGTH_LONG);
                    }
                });
                weixin.SSOSetting(true);
                weixin.showUser(null);
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    if (mProgressDialog == null) {
                        mProgressDialog = new CustomProgressingDialog(NewPhoneLoginActivity.this);
                    }
                    if (!mProgressDialog.isShowing()) {
                        mProgressDialog.show();
                    }

                    String password = mPasswdText.getText().toString()
                            .trim();

                    UserLoginRequest.Builder builder = new UserLoginRequest.Builder()
                            .setPhoneNum(mPhoneNum).setPassWord(password)
                            .setListener(loginListener)
                            .setErrorListener(errorListener);

                    UserLoginRequest request = builder.build();
                    request.setTag(TAG);
                    RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                            NewPhoneLoginActivity.this).getRequestQueue();
                    requestQueue.add(request);
                }
            }
        });

        mResetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NewPhoneLoginActivity.this, NewResetPasswdActivity.class);
                intent.putExtra(PHONE, mPhoneNum);
                startActivity(intent);
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
                        NewPhoneLoginActivity.this).getRequestQueue();
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
                            NewPhoneLoginActivity.this, MainActivity.class.getName(),
                            extras);
                }
                // 未注册
                if (isRegistered == 0) {
                    SharedPreferences.Editor editor = PSGodApplication
                            .getAppContext()
                            .getSharedPreferences(Constants.SharedPreferencesKey.NAME,
                                    Context.MODE_PRIVATE).edit();
                    editor.putString(Constants.ThirdAuthInfo.THIRD_AUTH_PLATFORM, QQPLAT);
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
                            JUMP_FROM_LOGIN);
                    MainActivity.startNewActivityAndFinishAllBefore(
                            NewPhoneLoginActivity.this, MainActivity.class.getName(),
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
                        NewPhoneLoginActivity.this).getRequestQueue();
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
                            JUMP_FROM_LOGIN);
                    MainActivity.startNewActivityAndFinishAllBefore(
                            NewPhoneLoginActivity.this, MainActivity.class.getName(),
                            extras);
                }
                // 未注册
                if (isRegistered == 0) {

                    SharedPreferences.Editor editor = PSGodApplication
                            .getAppContext()
                            .getSharedPreferences(Constants.SharedPreferencesKey.NAME,
                                    Context.MODE_PRIVATE).edit();
                    editor.putString(Constants.ThirdAuthInfo.THIRD_AUTH_PLATFORM, WEIBOPLAT);
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
                            JUMP_FROM_LOGIN);
                    MainActivity.startNewActivityAndFinishAllBefore(
                            NewPhoneLoginActivity.this, MainActivity.class.getName(),
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

    private Response.Listener<JSONObject> loginListener = new Response.Listener<JSONObject>() {
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

                        Toast.makeText(NewPhoneLoginActivity.this, "登录成功",
                                Toast.LENGTH_SHORT).show();

                        Bundle extras = new Bundle();
                        extras.putInt(Constants.IntentKey.ACTIVITY_JUMP_FROM,
                                JUMP_FROM_LOGIN);
                        MainActivity.startNewActivityAndFinishAllBefore(
                                NewPhoneLoginActivity.this,
                                MainActivity.class.getName(), extras);
                    } else if (response.getInt("status") == 2) {
                        // 密码错误
                        Toast.makeText(NewPhoneLoginActivity.this, "密码错误",
                                Toast.LENGTH_SHORT).show();
                        mPasswdText.setText("");
                        mPasswdText.requestFocus();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    };

    private void callInputPanel() {
        // 唤起输入键盘 并输入框取得焦点
        mPasswdText.setFocusableInTouchMode(true);
        mPasswdText.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mPasswdText, 0);
    }

    private PSGodErrorListener errorListener = new PSGodErrorListener(this) {
        @Override
        public void handleError(VolleyError error) {
            // TODO Auto-generated method stub
            if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    };

    private boolean validate() {
        // 手机号校验
//        if (Utils.isNull(mPhoneText)) {
//            Toast.makeText(NewPhoneLoginActivity.this, "请填写手机号码", Toast.LENGTH_SHORT)
//                    .show();
//            mPhoneText.requestFocus();
//            return false;
//        }
        String phoneNum = mPhoneText.getText().toString().trim();
        if (!Utils.matchPhoneNum(phoneNum)) {
            Toast.makeText(NewPhoneLoginActivity.this, "电话格式不正确", Toast.LENGTH_SHORT)
                    .show();
            mPhoneText.requestFocus();
            return false;
        }

        if (Utils.isNull(mPasswdText)) {
            Toast.makeText(NewPhoneLoginActivity.this, "请填写登录密码", Toast.LENGTH_SHORT)
                    .show();
            mPasswdText.requestFocus();
            return false;
        }
        return true;
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
