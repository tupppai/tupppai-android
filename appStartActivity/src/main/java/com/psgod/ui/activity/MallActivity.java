package com.psgod.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.psgod.R;
import com.psgod.model.LoginUser;
import com.youzan.sdk.YouzanBridge;
import com.youzan.sdk.YouzanSDK;
import com.youzan.sdk.YouzanUser;
import com.youzan.sdk.http.engine.OnRegister;
import com.youzan.sdk.http.engine.QueryError;
import com.youzan.sdk.web.plugin.YouzanWebClient;

/**
 * Created by Administrator on 2016/5/25.
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

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        mUrl = bundle.getString("Url");
        mCurrentUrl = mUrl;
        System.out.println("第二界面mUrl " + mUrl + "\n");
        //webview.loadUrl(mUrl);
        initView();
        setWeb();

    }

    private void setWeb() {
        YouzanUser user = new YouzanUser();
        LoginUser myUser = LoginUser.getInstance();
        user.setUserId(myUser.getUid() + "");
        // 参数初始化
        YouzanBridge.build(this,mWebview).create();

        mWebview.setWebViewClient(new MallWebViewClient());

        YouzanSDK.asyncRegisterUser(user, new OnRegister() {
            @Override
            public void onFailed(QueryError queryError) {
                System.out.println("账号");
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
            System.out.print("aaa");
            //mWebtitle.setText(view.getTitle());
            if (!url.equals(MALL)) {
                mBack.setVisibility(View.VISIBLE);


                //getActivity().findViewById(R.id.psgod_linear_tab).setVisibility(View.INVISIBLE);
                //getActivity().findViewById(R.id.psgod_rg_tab_tips).setVisibility(View.GONE);
                //getActivity().findViewById(R.id.middle).setVisibility(View.GONE);
            } else {
                mBack.setVisibility(View.GONE);
                //getActivity().findViewById(R.id.psgod_linear_tab).setVisibility(View.VISIBLE);
                //getActivity().findViewById(R.id.psgod_rg_tab_tips).setVisibility(View.VISIBLE);
                //getActivity().findViewById(R.id.middle).setVisibility(View.VISIBLE);
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
}
