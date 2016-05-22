package com.psgod.ui.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
 * Created by Administrator on 2016/5/20.
 */
public class MallFragment extends BaseFragment implements OnClickListener{

    private WebView webview;
    private TextView back;
    private TextView exit;
    private TextView webtitle;
    private String MALL = "https://wap.koudaitong.com/v2/showcase/homepage?alias=5q58ne2k";

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mall, container, false);

        initView(view);
        setWeb();

        return view;
    }

    private void setWeb() {
        YouzanUser user = new YouzanUser();
        LoginUser myUser = LoginUser.getInstance();
        user.setUserId(myUser.getUid() + "");
        // 参数初始化
        YouzanBridge.build(getActivity(),webview).create();

        webview.setWebViewClient(new MallWebViewClient());

        YouzanSDK.asyncRegisterUser(user, new OnRegister() {
            @Override
            public void onFailed(QueryError queryError) {
                System.out.println("账号");
            }

            @Override
            public void onSuccess()
            {
                webview.loadUrl(MALL);

            }
        });
    }

    private void initView(View view) {
        webview = (WebView) view.findViewById(R.id.fragment_mall_webview);
        back = (TextView) view.findViewById(R.id.webview_back);
        webtitle = (TextView) view.findViewById(R.id.webview_title);
        back.setOnClickListener(this);

        webview.getSettings().setJavaScriptEnabled(true);
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
            System.out.print("aaa");
            webtitle.setText(view.getTitle());
            if (!url.equals(MALL)) {
                back.setVisibility(View.VISIBLE);


                //getActivity().findViewById(R.id.psgod_linear_tab).setVisibility(View.INVISIBLE);
                //getActivity().findViewById(R.id.psgod_rg_tab_tips).setVisibility(View.GONE);
                //getActivity().findViewById(R.id.middle).setVisibility(View.GONE);
            } else {
                back.setVisibility(View.GONE);
                //getActivity().findViewById(R.id.psgod_linear_tab).setVisibility(View.VISIBLE);
                //getActivity().findViewById(R.id.psgod_rg_tab_tips).setVisibility(View.VISIBLE);
                //getActivity().findViewById(R.id.middle).setVisibility(View.VISIBLE);
            }
        }
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            webtitle.setText(view.getTitle());


        }
    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.webview_back:
                webview.goBack();   //后退

                break;
            case R.id.activity_inprogress_tab_page:
                webview.loadUrl(MALL);
                break;
            default:
                break;
        }
    }

}

