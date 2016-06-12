package com.pires.wesee.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.pires.wesee.Constants;
import com.pires.wesee.ui.activity.WebViewActivity;
import com.pires.wesee.ui.activity.PhotoBrowserActivity;
import com.pires.wesee.ui.activity.UserProfileActivity;
import com.pires.wesee.ui.view.TupppaiWebViewChrome;

import java.util.Map;

/**
 * Created by xiaoluo on 2016/6/8.
 *  自定义webview
 */
public class IntentWebView extends WebView {

    private Context mContext;
    private Long i = 1l;
    private String mWebviewTitle;
    public IntentWebView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public String getWebViewTitle() {
        return mWebviewTitle;
    }

    public IntentWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public IntentWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    private void init() {

        setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.stopLoading();
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                System.out.println("点击webview " + url + "\n");
                String mStr = url.substring(url.length() - 7, url.length());  //dynamic
                String nStr = url.substring(url.length() - 12, url.length()); //playcategory
                // 如果不是初始webview界面，才发生跳转
                if (!mStr.equals("dynamic") && !nStr.equals("playcategory")) {
                    if (url.contains("user-profile/")) {  //点击用户头像，转到用户界面
                        intent.setClass(mContext, UserProfileActivity.class);
                        String mUserId = url.substring(url.indexOf("user-profile/") + 13, url.length());
                        Long mLongId = Long.parseLong(mUserId);
                        intent.putExtra(Constants.IntentKey.USER_ID, mLongId);
                    } else if (url.contains("image_popup")) {  //点击图片，转到图片浏览界面
                        String picUrl = url.substring(url.indexOf("image_popup") + 12, url.length());
                        intent.setClass(mContext, PhotoBrowserActivity.class);
                        intent.putExtra(Constants.IntentKey.PHOTO_PATH, picUrl);
                        intent.putExtra(Constants.IntentKey.ASK_ID, i);
                        intent.putExtra(Constants.IntentKey.PHOTO_ITEM_ID, i);
                        intent.putExtra(Constants.IntentKey.PHOTO_ITEM_TYPE, "ask");
                    } else {  //点击评论，电影等，跳到电影界面
                        intent.setClass(mContext, WebViewActivity.class);
                        String StrUrl = url.substring(url.indexOf("#") + 1, url.length());
                        String mUrl = "http://wechupin.com/index-app.html#" + StrUrl;
                        bundle.putString("Url", mUrl);
                        intent.putExtras(bundle);
                    }
                    mContext.startActivity(intent);
                    //Toast.makeText(mContext,url,Toast.LENGTH_SHORT).show();
                } else {
                    view.loadUrl(url);
                }


                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:window.onhashchange = function() { myjsi.doStuff(url); };");
                //Toast.makeText(mContext,url,Toast.LENGTH_SHORT).show();
            }
        });
        getSettings().setJavaScriptEnabled(true);
        setWebChromeClient(new TupppaiWebViewChrome(getContext()) {
            @Override
            public void onProgressChanged(WebView view, int progress) {

            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                mWebviewTitle = title;
            }
        });

        // 设置内嵌浏览器的属性
        addJavascriptInterface(new MyJSI(), "myjsi");
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

    private class MyJSI {
        @JavascriptInterface
        public void doStuff(String url) {
            //Toast.makeText(mContext,"hash change" + "new" + url,Toast.LENGTH_SHORT).show();
//
//            Intent intent = new Intent();
//            Bundle bundle = new Bundle();
//            System.out.println("点击webview " + url + "\n");
//            if (!url.contains("dynamic")) {
//                if (url.contains("user-profile/")) {  //点击用户头像，转到用户界面
//                    intent.setClass(mContext, UserProfileActivity.class);
//                    String mUserId = url.substring(url.indexOf("user-profile/") + 13, url.length());
//                    Long mLongId = Long.parseLong(mUserId);
//                    intent.putExtra(Constants.IntentKey.USER_ID, mLongId);
//
//                } else if (url.contains("image_popup")) {  //点击图片，转到图片浏览界面
//                    String picUrl = url.substring(url.indexOf("image_popup") + 12, url.length());
//                    intent.setClass(mContext, PhotoBrowserActivity.class);
//                    intent.putExtra(Constants.IntentKey.PHOTO_PATH, picUrl);
//                    intent.putExtra(Constants.IntentKey.ASK_ID, i);
//                    intent.putExtra(Constants.IntentKey.PHOTO_ITEM_ID, i);
//                    intent.putExtra(Constants.IntentKey.PHOTO_ITEM_TYPE, "ask");
//
//                } else {  //点击评论，电影等，跳到电影界面
//                    intent.setClass(mContext, WebViewActivity.class);
//                    String StrUrl = url.substring(url.indexOf("#") + 1, url.length());
//                    String mUrl = "http://wechupin.com/index-app.html#" + StrUrl;
//                    bundle.putString("Url", mUrl);
//                    intent.putExtras(bundle);
//                }
//                mContext.startActivity(intent);
//            }

        }
    }
}
