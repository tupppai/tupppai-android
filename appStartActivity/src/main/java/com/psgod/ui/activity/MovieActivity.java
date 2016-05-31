package com.psgod.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.psgod.R;
import com.psgod.eventbus.RefreshEvent;
import com.psgod.ui.fragment.HomePageDynamicFragment;
import com.psgod.ui.fragment.MovieFragment;

import de.greenrobot.event.EventBus;

/**
 *
 * 新版web容器activity
 */
public class MovieActivity extends Activity implements View.OnClickListener {

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

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        mUrl = bundle.getString("Url");
        mCurrentUrl = mUrl;
        initView();
    }

    private void initView() {
        mWebview = (WebView) findViewById(R.id.activity_movie_webview);
        mWebtitle = (TextView) findViewById(R.id.webview_title);
        mBack = (TextView) findViewById(R.id.activity_webview_back);
        mExit = (TextView) findViewById(R.id.activity_webview_exit);
        mBack.setOnClickListener(this);
        mExit.setOnClickListener(this);

        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.setWebViewClient(new MovieWebViewClient());
        //mWebview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        mUrl = bundle.getString("Url");
        mWebview.loadUrl(mUrl);
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

    private class MovieWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }


        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        public void onPageFinished(WebView view, String url) {
            mCurrentUrl = url;

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

