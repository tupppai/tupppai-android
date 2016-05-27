package com.psgod.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.psgod.Constants;
import com.psgod.R;
import com.psgod.UserPreferences;
import com.psgod.eventbus.RefreshEvent;
import com.psgod.ui.activity.MovieActivity;
import com.psgod.ui.activity.UserProfileActivity;

import de.greenrobot.event.EventBus;

/**
 * 新版动态页面
 */
public class HomePageDynamicFragment extends BaseFragment {

    private WebView mWebview;
    private String cookieDYNAMIC = null;
    private Context mContext;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        View view = inflater.inflate(R.layout.fragment_dynamic, container, false);
        EventBus.getDefault().register(this);
        initView(view);

        return view;
    }
    private void getCookie() {
        String token = UserPreferences.TokenVerify.getToken();
        cookieDYNAMIC = "http://wechupin.com/index-app.html?c=" + token +"&from=android#app/dynamic";
    }

    private void initView(View view) {
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
    }

    public void onEventMainThread(RefreshEvent event) {
        if(event.className.equals(this.getClass().getName())){
            mWebview.loadUrl(cookieDYNAMIC);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

    }
}