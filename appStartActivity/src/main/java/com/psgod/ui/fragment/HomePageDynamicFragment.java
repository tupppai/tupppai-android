package com.psgod.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.psgod.Constants;
import com.psgod.Logger;
import com.psgod.R;
import com.psgod.UserPreferences;
import com.psgod.eventbus.RefreshEvent;
import com.psgod.model.LoginUser;
import com.psgod.model.User;
import com.psgod.ui.activity.MovieActivity;
import com.psgod.ui.activity.UserProfileActivity;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;
import com.youzan.sdk.YouzanBridge;
import com.youzan.sdk.YouzanSDK;
import com.youzan.sdk.YouzanUser;
import com.youzan.sdk.http.engine.OnRegister;
import com.youzan.sdk.http.engine.QueryError;
import com.youzan.sdk.web.plugin.YouzanWebClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2016/5/24.
 */
public class HomePageDynamicFragment extends BaseFragment implements View.OnClickListener {

    private WebView mWebview;
    private String cookieDYNAMIC = null;
    private Context mContext;
    private CustomProgressingDialog progressingDialog;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dynamic, container, false);
        EventBus.getDefault().register(this);
        mContext = getActivity();
        initView(view);

        return view;
    }
    private void getCookie() {
        String token = UserPreferences.TokenVerify.getToken();
        cookieDYNAMIC = "http://wechupin.com/index-app.html?c=" + token +"&from=android#app/dynamic";
    }

    private void initView(View view) {
//        progressingDialog = new CustomProgressingDialog(getActivity());
//        progressingDialog.show();

        mWebview = new WebView(getActivity());
        mWebview = (WebView) view.findViewById(R.id.fragment_dynamic_webview);

        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.setWebViewClient(new DynamicWebViewClient());
        getCookie();
        mWebview.loadUrl(cookieDYNAMIC);
    }


    private class DynamicWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            Intent intent = new Intent();
            intent.setClass(mContext, UserProfileActivity.class);
            Bundle bundle = new Bundle();
            String mStr = url.substring(url.length() - 7, url.length());
            if (!mStr.equals("dynamic")) {
                if (url.indexOf("user-profile/") > 0) {
                    //取用户ID，转到用户界面
                    String mUserId = url.substring(url.indexOf("user-profile/") + 13, url.length());
                    Long mLongId = Long.parseLong(mUserId);
                    intent.putExtra(Constants.IntentKey.USER_ID, mLongId);
                    mContext.startActivity(intent);

                } else if (url != cookieDYNAMIC) {
                    //点的是电影头像，转到电影界面
                    intent.setClass(mContext, MovieActivity.class);
                    String StrUrl = url.substring(url.indexOf("#") + 1, url.length());
                    String mUrl = "http://wechupin.com/index-app.html#" + StrUrl;
                    bundle.putString("Url", mUrl);
                    intent.putExtras(bundle);
                    mContext.startActivity(intent);
                }
            }
            return true;
        }

        public void onPageFinished(WebView view, String url) {
            if(progressingDialog != null && progressingDialog.isShowing()){
                progressingDialog.dismiss();
            }
        }
    }

    public void onEventMainThread(RefreshEvent event) {
        if(event.className.equals(this.getClass().getName())){
            mWebview.loadUrl(cookieDYNAMIC);
        }
    }

    public void onClick(View v) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

    }
}