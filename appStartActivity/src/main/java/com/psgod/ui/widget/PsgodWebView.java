package com.psgod.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.psgod.ui.view.TupppaiWebViewChrome;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import java.util.Map;

/**
 * Created by Administrator on 2016/1/20 0020.
 * 支持平铺的Webview
 */
public class PsgodWebView extends WebView {

    private CustomProgressingDialog dialog;

    public PsgodWebView(Context context) {
        super(context);
        init();
    }

    public PsgodWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PsgodWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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
        setWebChromeClient(new TupppaiWebViewChrome(getContext()) {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                // WebBrowserActivity.this.setProgress(progress * 100);
                if (progress == 100) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            }
        });

        dialog = new CustomProgressingDialog(getContext());

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
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
        super.loadUrl(url);

    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
        super.loadUrl(url, additionalHttpHeaders);

    }
}
