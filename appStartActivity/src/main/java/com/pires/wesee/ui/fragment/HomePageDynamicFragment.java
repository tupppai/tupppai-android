package com.pires.wesee.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.pires.wesee.ui.activity.MovieActivity;
import com.pires.wesee.Constants;
import com.pires.wesee.R;
import com.pires.wesee.UserPreferences;
import com.pires.wesee.eventbus.RefreshEvent;
import com.pires.wesee.ui.activity.PhotoBrowserActivity;
import com.pires.wesee.ui.activity.UserProfileActivity;
import com.pires.wesee.ui.widget.IntentWebView;

import de.greenrobot.event.EventBus;

/**
 * 新版动态页面
 *  Created by xiaoluo on 2016/5/20.
 */
public class HomePageDynamicFragment extends BaseFragment {

    private IntentWebView mWebview;
    private String cookieDYNAMIC = null;
    private Context mContext;
    private Long i = 1l;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        View view = inflater.inflate(R.layout.fragment_dynamic, container, false);
        EventBus.getDefault().register(this);
        getCookie();
        initView(view);

        return view;
    }

    private void getCookie() {
        String token = UserPreferences.TokenVerify.getToken();
        cookieDYNAMIC = "http://wechupin.com/index-app.html?c=" + token +"&from=android#app/dynamic";
    }

    private void initView(View view) {
        mWebview = new IntentWebView(getActivity());
        mWebview = (IntentWebView) view.findViewById(R.id.fragment_dynamic_webview);
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.loadUrl(cookieDYNAMIC);
        mWebview.setClickable(true);
        mWebview.setEnabled(true);
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