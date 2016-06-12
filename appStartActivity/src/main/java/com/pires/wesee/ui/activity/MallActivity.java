package com.pires.wesee.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.pires.wesee.R;
import com.pires.wesee.model.LoginUser;
import com.youzan.sdk.YouzanBridge;
import com.youzan.sdk.YouzanSDK;
import com.youzan.sdk.YouzanUser;
import com.youzan.sdk.http.engine.OnRegister;
import com.youzan.sdk.http.engine.QueryError;
import com.youzan.sdk.web.plugin.YouzanWebClient;
import com.youzan.sdk.web.plugin.YouzanChromeClient;

/**
 * 商城activity
 * Created by xiaoluo on 2016/5/25.
 *
 */
public class MallActivity extends Activity implements View.OnClickListener {

    private Context mContext;
    private String mUrl;
    private WebView mWebview;
    private TextView mBack;
    private TextView mExit;
    private TextView mWebtitle;
    private String mCurrentUrl;
    private String MALL = "https://wap.koudaitong.com/v2/showcase/homepage?alias=5q58ne2k";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mall);
        //取得传递过来的url
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        mUrl = bundle.getString("Url");
        // 设置当前界面的url,便于后退操作判断
        mCurrentUrl = mUrl;

        initView();
        setWeb();

    }
    //同步注册Youzan用户
    private void setWeb() {
        YouzanUser user = new YouzanUser();
        LoginUser myUser = LoginUser.getInstance();
        user.setUserId(myUser.getUid() + "");
        // 参数初始化
        YouzanBridge.build(this, mWebview)
                .setWebClient(new WebClient())
                .setChromeClient(new ChromeClient())
                .create();
        //YouzanBridge.build(this,mWebview).create();
        //mWebview.setWebViewClient(new MallWebViewClient());

        YouzanSDK.asyncRegisterUser(user, new OnRegister() {
            @Override
            public void onFailed(QueryError queryError) {
            }

            @Override
            public void onSuccess()
            {
                mWebview.loadUrl(mUrl);
            }
        });
    }

    private void initView() {
        mWebview = (WebView) findViewById(R.id.activity_mall_webview);
        mBack = (TextView) findViewById(R.id.activity_webview_back);
        mExit = (TextView) findViewById(R.id.activity_webview_exit);
        mWebtitle = (TextView) findViewById(R.id.webview_title);
        mBack.setOnClickListener(this);
        mExit.setOnClickListener(this);

        mWebview.getSettings().setJavaScriptEnabled(true);
    }

    //继承Youzan接口
    private class MallWebViewClient extends YouzanWebClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            super.shouldOverrideUrlLoading(view, url);
            view.loadUrl(url);
            return true;
        }
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mCurrentUrl = url;
            if (!url.equals(MALL)) {
                mBack.setVisibility(View.VISIBLE);

            } else {
                mBack.setVisibility(View.GONE);

            }
        }

    }

    private class ChromeClient extends YouzanChromeClient {

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            //这里获取到WebView的标题
        }
    }

    private class WebClient extends YouzanWebClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            super.shouldOverrideUrlLoading(view, url);


            if (!url.contains("weixin://")) {


                view.loadUrl(url);
            }
            return true;
        }
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mCurrentUrl = url;
            if (!url.equals(MALL)) {
                mBack.setVisibility(View.VISIBLE);

            } else {
                mBack.setVisibility(View.GONE);

            }
        }
    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.activity_webview_back:
                if (mCurrentUrl.equals(mUrl)) {
                    finish();
                } else {
                    mWebview.goBack();   //后退
                }
                break;
            case R.id.activity_webview_exit:
                finish();
                break;
            default:
                break;
        }
    }

    //设置物理后退按键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (mCurrentUrl.equals(mUrl)) {
                finish();
            } else {
                mWebview.goBack();   //后退
            }
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }


    public void onPause() {
        super.onPause();
        mWebview.onPause();
    }

    public void onResume() {
        super.onResume();
        mWebview.onResume();
    }
}