package com.pires.wesee.ui.activity;

/**
 * 应用内嵌浏览器
 *
 * @author brandwang
 */

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.pires.wesee.R;
import com.pires.wesee.UserPreferences;
import com.pires.wesee.ui.widget.PsgodWebView;
import com.pires.wesee.ui.widget.dialog.CustomProgressingDialog;
import com.pires.wesee.ui.widget.ActionBar;
import com.pires.wesee.model.LoginUser;
import com.youzan.sdk.YouzanBridge;
import com.youzan.sdk.YouzanSDK;
import com.youzan.sdk.YouzanUser;
import com.youzan.sdk.http.engine.OnRegister;
import com.youzan.sdk.http.engine.QueryError;
import com.youzan.sdk.web.plugin.YouzanChromeClient;
import com.youzan.sdk.web.plugin.YouzanWebClient;

public class WebBrowserActivity extends PSGodBaseActivity {
    private static final String TAG = WebBrowserActivity.class.getSimpleName();

    private PsgodWebView mWebView;
    private String mTargetUrl = "http://www.qiupsdashen.com";
    private CustomProgressingDialog dialog;
    public static final String KEY_URL = "target_url";
    public static final String KEY_DESC = "desc";
    private ActionBar mActionBar;
    private View mEmpty;
    private String mUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);

        setContentView(R.layout.activity_web_browser);
        setProgressBarVisibility(true);

        mWebView = (PsgodWebView) findViewById(R.id.web_browser_webview);
        mEmpty = findViewById(R.id.web_browser_empty);
        mActionBar = (ActionBar) findViewById(R.id.web_browser_actionbar);
        String desc = getIntent().getStringExtra(KEY_DESC);
        if (desc != null && !desc.equals("")) {
            mActionBar.setTitle(desc);
        }
        Intent intent = getIntent();
        mUrl = intent.getStringExtra(KEY_URL);
        if (mUrl != null && !mUrl.trim().equals("")) {
            mEmpty.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);
            if (mUrl.indexOf("?") == -1) {
                mUrl += "?from=android&v=2.0&token="
                        + UserPreferences.TokenVerify.getToken();

            } else {
                mUrl += "&from=android&v=2.0&token="
                        + UserPreferences.TokenVerify.getToken();
            }
        }else{
            mEmpty.setVisibility(View.VISIBLE);
            mWebView.setVisibility(View.GONE);
        }

        setWeb();





        //mWebView.loadUrl(mUrl);
    }


    //同步注册Youzan用户
    private void setWeb() {
        YouzanUser user = new YouzanUser();
        LoginUser myUser = LoginUser.getInstance();
        user.setUserId(myUser.getUid() + "");
        // 参数初始化
        YouzanBridge.build(this,mWebView)
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

                mWebView.loadUrl(mUrl);
            }
        });
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
        }
    }

    public void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    public void onResume() {
        super.onResume();
        mWebView.onResume();
    }
}
