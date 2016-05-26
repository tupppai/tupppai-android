package com.psgod.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.JsPromptResult;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.psgod.ui.activity.MovieActivity;
import com.psgod.ui.view.TupppaiWebViewChrome;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import java.util.Map;

/**
 * Created by brandwang on 2016/1/20 0020.
 * JsBridge
 */
public class JsBridgeWebView extends WebView {

    private CustomProgressingDialog dialog;
//    private String mToken;
    private Context mContext;
    private String mOriginUrl;

    public JsBridgeWebView(Context context) {
        super(context);
        mContext = context;

        init();
    }

    public JsBridgeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        init();
    }

    public JsBridgeWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;

        init();
    }

    private void init() {

        setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                System.out.println("shouldOverrideUrlLoading");
//                mToken = UserPreferences.TokenVerify.getToken();

                // 将token加入header中
//                Map<String,String> extraHeaders = new HashMap<String, String>();
//                extraHeaders.put("Cookie", "token2=" + mToken + ";");
//                extraHeaders.put("from", "android");

                view.loadUrl(url);

                return true;
            }
        });

        getSettings().setJavaScriptEnabled(true);

        setWebChromeClient(new TupppaiWebViewChrome(getContext()) {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                System.out.println("onProgressChanged");
                // WebBrowserActivity.this.setProgress(progress * 100);
                if (progress == 100) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                System.out.println("url" + url);
                System.out.println("default" + defaultValue);
                System.out.println("origin" + mOriginUrl);

                if (!TextUtils.isEmpty(url) && url.startsWith("http://")) {
                    if (defaultValue == mOriginUrl) {
//                        result.confirm();
                        return true;
                    } else {
                        goBack();
                        
                        Intent intent = new Intent();
                        intent.setClass(mContext, MovieActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("Url", defaultValue);
                        intent.putExtras(bundle);

                        mContext.startActivity(intent);

                        result.confirm();


                        // 新alctivity出现后，原页面回退

//                        loadUrl(mOriginUrl);

                        return true;
                    }

                } else {
                    return super.onJsPrompt(view, url, message, defaultValue, result);
                }
            }

        });

        dialog = new CustomProgressingDialog(getContext());

        // 设置内嵌浏览器的属性
        WebSettings websettings = getSettings();
        websettings.setJavaScriptEnabled(true);
        websettings.setBuiltInZoomControls(true);
    }

    @Override
    public void loadUrl(String url) {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }

        mOriginUrl = url;
        super.loadUrl(url);

    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }

        mOriginUrl = url;
        super.loadUrl(url, additionalHttpHeaders);

    }
}
