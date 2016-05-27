package com.psgod.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.psgod.R;
import com.psgod.UserPreferences;
import com.psgod.eventbus.RefreshEvent;
import com.psgod.ui.activity.MovieActivity;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2016/5/20.
 */
public class MovieFragment extends BaseFragment implements OnClickListener{

    public Context mContext;
    private WebView mWebview;
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

    private void getCookie() {
        mToken = UserPreferences.TokenVerify.getToken();
        mCookieMOVIE = "http://wechupin.com/index-app.html?c=" + mToken +"&from=android#app/playcategory";
        System.out.println("cookieurl" + mCookieMOVIE + "\n");
    }

    private class MovieWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            super.shouldOverrideUrlLoading(view, url);
            System.out.println("我想知道url是什么" + url +"\n");
            String mStr = url.substring(url.length() - 12, url.length());

            if (!mStr.equals("playcategory")) {
                //重新拼接新URL
                System.out.println("\n" + "mCookieMOVIE =   " + mCookieMOVIE);
                String StrUrl = url.substring(url.indexOf("#") + 1, url.length());
                String mUrl = "http://wechupin.com/index-app.html#" + StrUrl;

                Intent intent = new Intent();
                intent.setClass(mContext, MovieActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Url", mUrl);
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }

            //view.loadUrl(url);
            return true;
        }
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            System.out.println("\n" + "原界面的onPageFinish " + url);
            mWebtitle.setText(view.getTitle());
        }
    }

    private void initView(View view) {
        mWebview = new WebView(mContext);
        mWebview = (WebView) view.findViewById(R.id.fragment_movie_webview);

        mWebtitle = (TextView) view.findViewById(R.id.webview_title);

        mWebview.setWebViewClient(new MovieWebViewClient());
        mWebview.loadUrl(mCookieMOVIE);
    }

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

