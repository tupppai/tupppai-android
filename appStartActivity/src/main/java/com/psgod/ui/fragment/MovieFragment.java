package com.psgod.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import com.psgod.R;
import com.psgod.UserPreferences;
import com.psgod.ui.activity.MovieActivity;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/5/20.
 */
public class MovieFragment extends BaseFragment implements OnClickListener{

    public Context mContext;
    private WebView mWebview;
    private TextView mWebtitle;
    private TextView mBack;
    private TextView mExit;
    private String MOVIE = "http://wechupin.com/index-app.html#app/playcategory";
    private String mCookieMOVIE = null;
    private CustomProgressingDialog mProgressingDialog;
    private String mUrl = null;
    private String mToken;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie, container, false);
        mContext = getActivity();

        getCookie();
        initView(view);
        // 载入网址
        mWebview.loadUrl(mCookieMOVIE);
        return view;
    }

    private void getCookie() {
        mToken = UserPreferences.TokenVerify.getToken();
        mCookieMOVIE = "http://wechupin.com/index-app.html?c=" + mToken +"&s=android#app/playcategory";
        System.out.println("cookieurl" + mCookieMOVIE + "\n");
    }

    private void initView(View view) {


        mWebview = new WebView(getActivity());
        mWebview = (WebView) view.findViewById(R.id.fragment_movie_webview);
        mWebtitle = (TextView) view.findViewById(R.id.webview_title);
        mBack = (TextView) view.findViewById(R.id.webview_back);
        mBack.setOnClickListener(this);

        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.setWebViewClient(new MovieWebViewClient());
        mWebview.setWebChromeClient(new MovieChromeClient());


        mWebtitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(mContext, MovieActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Url", "http://wechupin.com/index-app.html?c=f94737dd4f9b10737e0f72cf32d3b1e16174a9f6&s=android#app/playdetail/197");
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }
        });

        mBack.setOnClickListener(this);
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.setWebViewClient(new MovieWebViewClient());
        mWebview.setWebChromeClient(new MovieChromeClient());

    }

    private class MovieChromeClient extends WebChromeClient {
        @Override
        public boolean onJsPrompt(WebView view, String url, String message,
                                  String defaultValue, JsPromptResult result) {

//            System.out.println("JsPrompt url  " + url + "\n");
//            System.out.print("message" + message + "\n");
//            System.out.print("defaultValue" + defaultValue + "\n");
//            System.out.print("JsPrompResult" + result + "\n");

            if (!TextUtils.isEmpty(url) && url.startsWith("http://")) {
                Intent intent = new Intent();
                intent.setClass(mContext, MovieActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Url", defaultValue);
                intent.putExtras(bundle);

                mContext.startActivity(intent);

                result.confirm();
                //mWebview.goBack();
                return true;
            } else {
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }
        }


    }

    private String tokenUrl(String url) {
        String insertStr = "html";
        StringBuffer newUrl = new StringBuffer(url);
        Pattern p = Pattern.compile(insertStr);
        Matcher m = p.matcher(newUrl.toString());
        if (m.find()) {
            newUrl.insert((m.start()+1), "?c=" + mToken);
        }
        return newUrl.toString();
    }


    private class MovieWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            super.shouldOverrideUrlLoading(view, url);
            System.out.println("\n" + "原界面的shouldOverrideUrl  " + url);
//            System.out.println("testtessssssssssssssssssssssssssssssssssssssssssssss");
//            Intent intent = new Intent();
//            intent.setClass(view.getContext(), MovieActivity.class);
//            Bundle bundle = new Bundle();
//            bundle.putString("mUrl", url);
//            intent.putExtras(bundle);
//            view.getContext().startActivity(intent);
            view.loadUrl(url);

            return true;
//                super.shouldOverrideUrlLoading(view, url);
//                progressingDialog.show();
//                //            System.out.println("testtessssssssssssssssssssssssssssssssssssssssssssss");
//                //            Intent intent = new Intent();
//                //            intent.setClass(view.getContext(), MovieActivity.class);
//                //            Bundle bundle = new Bundle();
//                //            bundle.putString("mUrl", url);
//                //            intent.putExtras(bundle);
//                //            view.getContext().startActivity(intent);
//                view.loadUrl(url);
//
//                return true;

        }
        public void onPageFinished(WebView view, String url) {
            mWebview.goBack();
            System.out.println("\n" + "原界面的onPageFinish " + url);
            mWebtitle.setText(view.getTitle());
            if (!url.equals(mCookieMOVIE)) {
               // back.setVisibility(View.VISIBLE);
//                getActivity().findViewById(R.id.psgod_linear_tab).setVisibility(View.GONE);
//                getActivity().findViewById(R.id.psgod_rg_tab_tips).setVisibility(View.GONE);
//                getActivity().findViewById(R.id.middle).setVisibility(View.GONE);
            } else {
                mBack.setVisibility(View.GONE);
//                getActivity().findViewById(R.id.psgod_linear_tab).setVisibility(View.VISIBLE);
//                getActivity().findViewById(R.id.psgod_rg_tab_tips).setVisibility(View.VISIBLE);
//                getActivity().findViewById(R.id.middle).setVisibility(View.VISIBLE);
            }
            if(mProgressingDialog != null && mProgressingDialog.isShowing()){
                mProgressingDialog.dismiss();
            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.webview_back:
                mWebview.goBack();   //后退

//                webview.goBack();   //后退


                break;
            case R.id.activity_tab_tupai_page:
                System.out.print("底部tab");
                mWebview.reload();
                break;
            default:
                break;
        }
    }
}

