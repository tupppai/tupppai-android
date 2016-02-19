package com.psgod.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.psgod.Constants;
import com.psgod.CustomToast;
import com.psgod.PSGodApplication;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.LoginUser;
import com.psgod.network.request.ActionBindAccountRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;

/**
 * Created by pires on 16/1/21.
 */
public class WithDrawMoneyBindWechatActivity extends PSGodBaseActivity {

    private static final String TAG = WithDrawMoneyBindWechatActivity.class.getSimpleName();

    public static final String AMOUNT = "amount";

    private double amount;

    private Button mBindBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.addActivity(WithDrawMoneyBindWechatActivity.this);
        setContentView(R.layout.activity_withdraw_money_bind_wechat);

        Intent intent = getIntent();
        amount = intent.getDoubleExtra(AMOUNT, 0);

        initView();
        initListener();

    }

    private void initView() {
        mBindBtn = (Button) this.findViewById(R.id.bind_weixin_btn);

    }

    private void initListener() {
        mBindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Platform wechat = ShareSDK.getPlatform(Wechat.NAME);
                if (wechat.isValid()) {
                    wechat.removeAccount();
                    ShareSDK.removeCookieOnAuthorize(true);
                }
                wechat.SSOSetting(true);
                wechat.setPlatformActionListener(wechatBindListener);
                wechat.showUser(null);
            }
        });
    }

    // 微信授权监听
    private PlatformActionListener wechatBindListener = new PlatformActionListener() {
        @Override
        public void onError(Platform arg0, int arg1, Throwable arg2) {
            Looper.prepare();
            CustomToast.
                    show(WithDrawMoneyBindWechatActivity.this,
                            "绑定失败，请查看是否安装微信", Toast.LENGTH_SHORT);
            Looper.loop();
        }

        @Override
        public void onComplete(Platform arg0, int arg1,
                               HashMap<String, Object> res) {
            String wechatOpenId = res.get("openid").toString();

            if (!TextUtils.isEmpty(wechatOpenId)) {
                ActionBindAccountRequest.Builder builder = new ActionBindAccountRequest.Builder()
                        .setIsBind(1).setOpenId(wechatOpenId).setType("weixin")
                        .setListener(wechatAuthListener)
                        .setErrorListener(wechatBindErrorListener);

                ActionBindAccountRequest request = builder.build();
                request.setTag(TAG);
                RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                        WithDrawMoneyBindWechatActivity.this).getRequestQueue();
                requestQueue.add(request);
            }
        }

        @Override
        public void onCancel(Platform arg0, int arg1) {
            Looper.prepare();
            CustomToast.
                    show(WithDrawMoneyBindWechatActivity.this,
                            "绑定取消", Toast.LENGTH_SHORT);
            Looper.loop();
        }
    };

    // 绑定微信回调
    private Response.Listener<Boolean> wechatAuthListener = new Response.Listener<Boolean>() {
        @Override
        public void onResponse(Boolean response) {
            if (response) {
                Toast.makeText(WithDrawMoneyBindWechatActivity.this, "绑定微信成功", Toast.LENGTH_SHORT).show();
                LoginUser user = LoginUser.getInstance();
                user.setBoundWechat(true);
                // 修改SP中的状态
                SharedPreferences.Editor editor = PSGodApplication
                        .getAppContext()
                        .getSharedPreferences(
                                Constants.SharedPreferencesKey.NAME,
                                Context.MODE_PRIVATE).edit();
                editor.putBoolean(LoginUser.SPKey.IS_BOUND_WECHAT, true);
                editor.commit();
                Intent intent = new Intent(WithDrawMoneyBindWechatActivity.this,
                        WithdrawPhoneVerifyActivity.class);
                intent.putExtra(WithdrawPhoneVerifyActivity.AMOUNT_DOUBLE,amount);
                startActivity(intent);
            } else {
            }
        }
    };

    // 微信绑定失败listener
    private PSGodErrorListener wechatBindErrorListener = new PSGodErrorListener(this) {
        @Override
        public void handleError(VolleyError error) {
        }
    };

}
