package com.pires.wesee.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.pires.wesee.R;
import com.pires.wesee.eventbus.RefreshEvent;
import com.pires.wesee.model.LoginUser;
import com.pires.wesee.ui.fragment.HomePageDynamicFragment;
import com.pires.wesee.ui.fragment.MovieFragment;
import com.youzan.sdk.YouzanBridge;
import com.youzan.sdk.YouzanSDK;
import com.youzan.sdk.YouzanUser;
import com.youzan.sdk.http.engine.OnRegister;
import com.youzan.sdk.http.engine.QueryError;
import com.youzan.sdk.web.plugin.YouzanChromeClient;
import com.youzan.sdk.web.plugin.YouzanWebClient;

import de.greenrobot.event.EventBus;

/**
 *
 * 新版web容器activity
 *  Created by xiaoluo on 2016/6/20.
 */
public class WebViewActivity extends Activity implements View.OnClickListener {

    public Context mContext;
    private WebView mWebview;
    private TextView mWebtitle;
    private TextView mBack;
    private String mUrl;
    private TextView mExit;
    private String mCurrentUrl;
    private String MOVIE = "http://wechupin.com/index-app.html#app/playcategory";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        mContext = this;
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        mUrl = bundle.getString("Url");
        mCurrentUrl = mUrl;
        initView();
        setWeb();
        //mWebview.loadUrl(mUrl);
        mWebview.loadUrl(mUrl);
    }

    //同步注册Youzan用户
    private void setWeb() {
        YouzanUser user = new YouzanUser();
        LoginUser myUser = LoginUser.getInstance();
        user.setUserId(myUser.getUid() + "");
        // 参数初始化
        YouzanBridge.build(this,mWebview)
                .setWebClient(new WebClient())
                .setChromeClient(new ChromeClient())
                .create();

        //mWebview.setWebViewClient(new MallWebViewClient());
        YouzanSDK.asyncRegisterUser(user, new OnRegister() {
            @Override
            public void onFailed(QueryError queryError) {

            }

            @Override
            public void onSuccess()
            {
                mWebview.loadUrl(mUrl);
                //Toast.makeText(mContext, mUrl, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onPause() {
        super.onPause();
        mWebview.onPause();
        mWebview.pauseTimers();
    }

    public void onResume() {
        super.onResume();
        mWebview.onResume();
        mWebview.resumeTimers();
    }

    private void initView() {
        mWebview = (WebView) findViewById(R.id.activity_movie_webview);
        mWebtitle = (TextView) findViewById(R.id.webview_title);
        mBack = (TextView) findViewById(R.id.activity_webview_back);
        mExit = (TextView) findViewById(R.id.activity_webview_exit);
        mBack.setOnClickListener(this);
        mExit.setOnClickListener(this);

        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.getSettings().setBuiltInZoomControls(true);
        mWebview.getSettings().setDomStorageEnabled(true);

        //mWebview.setWebViewClient(new MovieWebViewClient());

       // mWebview.setWebViewClient(new MovieWebViewClient());
//        mWebview.getSettings().setSupportZoom(true);
//        mWebview.getSettings().setBuiltInZoomControls(true);
//        mWebview.getSettings().setUseWideViewPort(true);
//        mWebview.getSettings().setDomStorageEnabled(true);
//        // 应用可以有缓存
//        mWebview.getSettings().setAppCacheEnabled(true);
        //mWebview.getSettings().setAppCacheEnabled(false);
        mWebview.setWebChromeClient(new WebChromeClient() {

        });


        //mWebview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        mUrl = bundle.getString("Url");

    }


    private class ChromeClient extends YouzanChromeClient {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
        }
    }

    private class WebClient extends YouzanWebClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            super.shouldOverrideUrlLoading(view, url);
            if(!url.contains("weixin://")) {
                view.loadUrl(url);
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mCurrentUrl = url;
        }
    }

    // 点击返回时，进行判断
    // 如果当前网页是最初传入的网页时,返回会直接关闭activity
    // 如果当前网页不是最初传入的网页时,返回则调用goBack
    // 点击返回或关闭按钮,都发送eventbus让原fragment刷新
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_webview_back:
                EventBus.getDefault().post(new RefreshEvent(MovieFragment.class.getName()));
                EventBus.getDefault().post(new RefreshEvent(HomePageDynamicFragment.class.getName()));
                if (mCurrentUrl.equals(mUrl)) {
                    finish();
                } else {
                    mWebview.goBack();   //后退
                }
                break;
            case R.id.activity_webview_exit:
                EventBus.getDefault().post(new RefreshEvent(MovieFragment.class.getName()));
                EventBus.getDefault().post(new RefreshEvent(HomePageDynamicFragment.class.getName()));
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
                EventBus.getDefault().post(new RefreshEvent(MovieFragment.class.getName()));
                EventBus.getDefault().post(new RefreshEvent(HomePageDynamicFragment.class.getName()));
                finish();
            } else {
                mWebview.goBack();   //后退
            }
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }
}

