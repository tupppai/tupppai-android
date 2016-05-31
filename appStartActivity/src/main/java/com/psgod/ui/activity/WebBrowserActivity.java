package com.psgod.ui.activity;

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

import com.psgod.R;
import com.psgod.UserPreferences;
import com.psgod.ui.view.TupppaiWebViewChrome;
import com.psgod.ui.widget.ActionBar;
import com.psgod.ui.widget.PsgodWebView;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

public class WebBrowserActivity extends PSGodBaseActivity {
    private static final String TAG = WebBrowserActivity.class.getSimpleName();

    private PsgodWebView mWebView;
    private String mTargetUrl = "http://www.qiupsdashen.com";
    private CustomProgressingDialog dialog;
    public static final String KEY_URL = "target_url";
    public static final String KEY_DESC = "desc";
    private ActionBar mActionBar;
    private View mEmpty;

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
        String url = intent.getStringExtra(KEY_URL);
        if (url != null && !url.trim().equals("")) {
            mEmpty.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);
            if (url.indexOf("?") == -1) {
                url += "?from=android&v=2.0&token="
                        + UserPreferences.TokenVerify.getToken();

            } else {
                url += "&from=android&v=2.0&token="
                        + UserPreferences.TokenVerify.getToken();
            }
        }else{
            mEmpty.setVisibility(View.VISIBLE);
            mWebView.setVisibility(View.GONE);
        }

        mWebView.loadUrl(url);
    }

    @Override
    protected void onPause() {
        //mWebView.reload();
        //mWebView.getClass().getMethod("onPause").invoke(mWebView,(Object[])null);
        super.onPause();

    }
}
