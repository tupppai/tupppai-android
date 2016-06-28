package com.psgod.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.psgod.network.request.BaseRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.QQLoginRequest;
import com.psgod.network.request.RegisterCheckPhoneNumRequest;
import com.psgod.network.request.WechatUserInfoRequest;
import com.psgod.network.request.WeiboLoginRequest;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

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
 * 新的登录页
 */
public class NewLoginInputPhoneActivity extends PSGodBaseActivity {
    private static final String TAG = NewLoginInputPhoneActivity.class.getSimpleName();
    private static final int JUMP_FROM_LOGIN = 100;
    private static final String PHONE = "PhoneNum";
    private static final String QQPLAT = "qq";
    private static final String WEIBOPLAT = "weibo";
    private static final String WEIXINPLAT = "weixin";

    private EditText mPhoneEdit;
    private Button mNextButton;
    private LinearLayout mParentLayout;

    private ImageView mChangeRequestUrlImage;
    private ImageView mWeiboLoginBtn;
    private ImageView mWechatLoginBtn;
    private ImageView mQQLoginBtn;

    private String mThirdAuthId = "";
    private String mThirdAuthName = "";
    private String mThirdAuthGender = "";
    private String mThirdAuthAvatar = "";
    private String mThirdAuthUnion = "";

    private CustomProgressingDialog mProgressDialog;
    private WeakReferenceHandler handler = new WeakReferenceHandler(this);

    private int mClickItems = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_login_input_phone);

        initViews();
        initEvents();

//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                callInputPanel();
//            }
//        },200);

    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        boolean isFinishActivity = intent.getBooleanExtra(
                Constants.IntentKey.IS_FINISH_ACTIVITY, false);
        intent.removeExtra(Constants.IntentKey.IS_FINISH_ACTIVITY);
        if (isFinishActivity) {
            String destActivityName = intent
                    .getStringExtra(Constants.IntentKey.DEST_ACTIVITY_NAME);

            if (!TextUtils.isEmpty(destActivityName)) {
                Intent newIntent = new Intent();
                newIntent.setClassName(NewLoginInputPhoneActivity.this, destActivityName);
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    newIntent.putExtras(extras);
                }
                startActivity(newIntent);
                finish();
            }
        }
    }

    private void initViews() {
        mParentLayout = (LinearLayout) findViewById(R.id.parent_layout);
        mParentLayout.setBackgroundColor(BaseRequest.PSGOD_BASE_URL.equals
                (BaseRequest.PSGOD_BASE_TEST_URL) ?
                Color.parseColor("#9fc25b") : Color.parseColor("#FFFFFF"));
        mChangeRequestUrlImage = (ImageView) findViewById(R.id.logo_click);
        mPhoneEdit = (EditText) findViewById(R.id.phone_edit);
        mNextButton = (Button) findViewById(R.id.next_btn);
        mWeiboLoginBtn = (ImageView) findViewById(R.id.weibo_login);
        mWechatLoginBtn = (ImageView) findViewById(R.id.weixin_login);
        mQQLoginBtn = (ImageView) findViewById(R.id.qq_login);
    }

    private void initEvents() {
        mChangeRequestUrlImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickItems++;
                if (mClickItems == 7) {
                    mClickItems = 0;
                    mChangeRequestUrlImage.setEnabled(false);
                    if (BaseRequest.PSGOD_BASE_URL.equals(BaseRequest.PSGOD_BASE_RELEASE_URL)) {
                        BaseRequest.PSGOD_BASE_URL = BaseRequest.PSGOD_BASE_TEST_URL;
                        Utils.showDebugToast("切换到测试服");
                        mParentLayout.setBackgroundColor(Color.parseColor("#9fc25b"));
                    } else {
                        BaseRequest.PSGOD_BASE_URL = BaseRequest.PSGOD_BASE_RELEASE_URL;
                        Utils.showDebugToast("切换到正式服");
                        mParentLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    }
                    mChangeRequestUrlImage.setEnabled(true);
                }
            }
        });

        // QQ登录
        mQQLoginBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // 显示等待对话框
                if (mProgressDialog == null) {
                    mProgressDialog = new CustomProgressingDialog(
                            NewLoginInputPhoneActivity.this);
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
                            NewLoginInputPhoneActivity.this);
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
                    // 已注册
                    if (isRegistered == 1) {
                        JSONObject userInfoData = response.UserObject;
                        LoginUser.getInstance()
                                .initFromJSONObject(userInfoData);

                        Bundle extras = new Bundle();
                        extras.putInt(Constants.IntentKey.ACTIVITY_JUMP_FROM,
                                JUMP_FROM_LOGIN);
                        MainActivity.startNewActivityAndFinishAllBefore(
                                NewLoginInputPhoneActivity.this,
                                MainActivity.class.getName(), extras);
                    }
                    // 未注册
                    if (isRegistered == 0) {


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
                                NewLoginInputPhoneActivity.this, MainActivity.class.getName(),
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
                            NewLoginInputPhoneActivity.this);
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
                        CustomToast.show(NewLoginInputPhoneActivity.this, "微信登录异常",
                                Toast.LENGTH_LONG);
                        Looper.loop();
                    }

                    @Override
                    public void onComplete(Platform platform, int action,
                                           HashMap<String, Object> res) {
                        mThirdAuthId = res.get("openid").toString();
                        mThirdAuthAvatar = res.get("headimgurl").toString();
                        mThirdAuthGender = res.get("sex").toString();
                        mThirdAuthName = res.get("nickname").toString();
                        mThirdAuthUnion  = res.get("unionid").toString();

                        /**
                         * 输出res数据
                         */
//                        Iterator iter = res.entrySet().iterator();
//                        StringBuilder sb = new StringBuilder();
//                        while (iter.hasNext()) {
//                            Map.Entry entry = (Map.Entry) iter.next();
//                            Object key = entry.getKey();
//                            Object val = entry.getValue();
//                            sb.append(key.toString() + "," + val.toString());
//                            sb.append("\n");
//                        }
//                        try {
//                            File txt = new File(Environment.getExternalStorageDirectory().getPath() + "/"
//                                    + sb.hashCode() + ".txt");
//                            if (!txt.exists()) {
//                                txt.createNewFile();
//                            }
//                            byte bytes[] = new byte[512];
//                            bytes = sb.toString().getBytes(); //新加的
//                            int b = sb.toString().length(); //改
//                            FileOutputStream fos = new FileOutputStream(txt);
//                            fos.write(bytes, 0, b);
//                            fos.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                        // 验证code是否被注册
                        if (!TextUtils.isEmpty(mThirdAuthId)) {
                            WechatUserInfoRequest.Builder builder = new WechatUserInfoRequest.Builder()
                                    .setCode(mThirdAuthId)
                                    .setAvatar(mThirdAuthAvatar)
                                    .setNickname(mThirdAuthName)
                                    .setUnionId(mThirdAuthUnion)
                                    .setSex(mThirdAuthGender)
                                    .setListener(wechatAuthListener)
                                    .setErrorListener(errorListener);

                            WechatUserInfoRequest request = builder.build();
                            request.setTag(TAG);
                            RequestQueue requestQueue = PSGodRequestQueue
                                    .getInstance(NewLoginInputPhoneActivity.this)
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
                        CustomToast.show(NewLoginInputPhoneActivity.this, "取消微信登录",
                                Toast.LENGTH_LONG);
                    }
                });
                weixin.SSOSetting(true);
                weixin.showUser(null);
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    // 显示等待对话框
                    if (mProgressDialog == null) {
                        mProgressDialog = new CustomProgressingDialog(
                                NewLoginInputPhoneActivity.this);
                    }
                    if (!mProgressDialog.isShowing()) {
                        mProgressDialog.show();
                    }

                    String phoneNum = mPhoneEdit.getText().toString().trim();

                    RegisterCheckPhoneNumRequest.Builder builder = new RegisterCheckPhoneNumRequest.Builder()
                            .setPhoneNumber(phoneNum)
                            .setListener(checkPhoneListener)
                            .setErrorListener(errorListener);
                    RegisterCheckPhoneNumRequest request = builder.build();
                    request.setTag(TAG);
                    RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                            NewLoginInputPhoneActivity.this).getRequestQueue();
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
                        .setCode(mThirdAuthId)
                        .setNickname(mThirdAuthName)
                        .setAvatar(mThirdAuthAvatar)
                        .setListener(qqAuthListener)
                        .setErrorListener(errorListener);

                QQLoginRequest request = builder.build();
                RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                        NewLoginInputPhoneActivity.this).getRequestQueue();
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
                // 已注册
                if (isRegistered == 1) {
                    JSONObject userInfoData = response.UserObject;
                    LoginUser.getInstance().initFromJSONObject(userInfoData);

                    Bundle extras = new Bundle();
                    extras.putInt(Constants.IntentKey.ACTIVITY_JUMP_FROM,
                            LoginActivity.JUMP_FROM_LOGIN);

                    // 关闭之前所有页面
                    MainActivity.startNewActivityAndFinishAllBefore(
                            NewLoginInputPhoneActivity.this, MainActivity.class.getName(),
                            extras);
                }
                // 未注册
                if (isRegistered == 0) {

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
                            NewLoginInputPhoneActivity.this, MainActivity.class.getName(),
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
                        .setCode(mThirdAuthId)
                        .setNickname(mThirdAuthName)
                        .setAvatar(mThirdAuthAvatar)
                        .setListener(weiboAuthListener)
                        .setErrorListener(errorListener);

                WeiboLoginRequest request = builder.build();
                request.setTag(TAG);
                RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                        NewLoginInputPhoneActivity.this).getRequestQueue();
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
                // 已注册
                if (isRegistered == 1) {
                    JSONObject userInfoData = response.UserObject;
                    LoginUser.getInstance().initFromJSONObject(userInfoData);

                    Bundle extras = new Bundle();
                    extras.putInt(Constants.IntentKey.ACTIVITY_JUMP_FROM,
                            JUMP_FROM_LOGIN);
                    MainActivity.startNewActivityAndFinishAllBefore(
                            NewLoginInputPhoneActivity.this, MainActivity.class.getName(),
                            extras);
                }
                // 未注册
                if (isRegistered == 0) {

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
                            NewLoginInputPhoneActivity.this, MainActivity.class.getName(),
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

    // 检测手机号码是否注册过接口
    private Response.Listener<Boolean> checkPhoneListener = new Response.Listener<Boolean>() {
        @Override
        public void onResponse(Boolean response) {
            if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            if (response) {
                Intent intent = new Intent(NewLoginInputPhoneActivity.this, NewPhoneLoginActivity.class);
                intent.putExtra(PHONE, mPhoneEdit.getText().toString().trim());
                startActivity(intent);
            } else {
                Intent intent = new Intent(NewLoginInputPhoneActivity.this, NewRegisterPhoneActivity.class);
                intent.putExtra(PHONE, mPhoneEdit.getText().toString().trim());
                startActivity(intent);
            }
        }
    };

    private PSGodErrorListener errorListener = new PSGodErrorListener(
            RegisterCheckPhoneNumRequest.class.getSimpleName()) {
        @Override
        public void handleError(VolleyError error) {
            if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    };

    private void callInputPanel() {
        // 唤起输入键盘 并输入框取得焦点
        mPhoneEdit.setFocusableInTouchMode(true);
        mPhoneEdit.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mPhoneEdit, 0);
    }

    private boolean validate() {
        // 手机号校验
        if (Utils.isNull(mPhoneEdit)) {
            Toast.makeText(NewLoginInputPhoneActivity.this, "请填写手机号码", Toast.LENGTH_SHORT)
                    .show();
            mPhoneEdit.requestFocus();
            return false;
        }
        String phoneNum = mPhoneEdit.getText().toString().trim();
        if (!Utils.matchPhoneNum(phoneNum)) {
            Toast.makeText(NewLoginInputPhoneActivity.this, "电话格式不正确", Toast.LENGTH_SHORT)
                    .show();
            mPhoneEdit.requestFocus();
            return false;
        }
        return true;
    }

    public static void startNewActivityAndFinishAllBefore(Context context,
                                                          String destActivityName, Bundle extras) {
        Intent intent = new Intent(context, NewLoginInputPhoneActivity.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
        intent.putExtra(Constants.IntentKey.DEST_ACTIVITY_NAME,
                destActivityName);
        intent.putExtra(Constants.IntentKey.IS_FINISH_ACTIVITY, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
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
