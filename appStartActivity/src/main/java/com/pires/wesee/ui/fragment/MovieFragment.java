package com.pires.wesee.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pires.wesee.UserPreferences;
import com.pires.wesee.eventbus.RefreshEvent;
import com.pires.wesee.R;
import com.pires.wesee.ui.widget.IntentWebView;

import de.greenrobot.event.EventBus;

/**
 * 影视fragment
 * Created by xiaoluo on 2016/5/20.
 */
public class MovieFragment extends BaseFragment implements OnClickListener{

    public Context mContext;
    private IntentWebView mWebview;
    private TextView mWebtitle;
    private String mCookieMOVIE = null;
    private String mToken;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie, container, false);
        EventBus.getDefault().register(this);
        mContext = getActivity();

        getCookie();
        initView(view);
        return view;
    }

    //取得带cookie的url
    private void getCookie() {
        mToken = UserPreferences.TokenVerify.getToken();
        mCookieMOVIE = "http://wechupin.com/index-app.html?c=" + mToken +"&from=android#app/playcategory";
    }


    private void initView(View view) {
        mWebview = new IntentWebView(mContext);
        mWebview = (IntentWebView) view.findViewById(R.id.fragment_movie_webview);
        mWebtitle = (TextView) view.findViewById(R.id.webview_title);
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.loadUrl(mCookieMOVIE);
        mWebtitle.setText("WEMAKE");
    }

    // evenbus，用于从Activity返回原来的Fragment时，调用重新载入webview
    // 此处是为了解决原webview随着点击而跳转的问题
    public void onEventMainThread(RefreshEvent event) {
        if(event.className.equals(this.getClass().getName())){
            try {
                mWebview.loadUrl(mCookieMOVIE);
            } catch (NullPointerException nu) {
            } catch (Exception e) {
            }
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

