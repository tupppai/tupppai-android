package com.pires.wesee.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Map;

/**
 * Created by brandwang on 2016/1/20 0020.
 * JsBridge webview
 */
public class JsBridgeWebView extends WebView {

    private Context mContext;

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
                view.loadUrl(url);

                return true;
            }
        });

        getSettings().setJavaScriptEnabled(true);

        // 设置内嵌浏览器的属性
        WebSettings websettings = getSettings();
        websettings.setJavaScriptEnabled(true);
        websettings.setBuiltInZoomControls(true);
    }

    @Override
    public void loadUrl(String url) {
        super.loadUrl(url);
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        super.loadUrl(url, additionalHttpHeaders);
    }
}
