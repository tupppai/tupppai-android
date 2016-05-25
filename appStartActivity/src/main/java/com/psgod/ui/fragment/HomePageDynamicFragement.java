package com.psgod.ui.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.psgod.Logger;
import com.psgod.R;
import com.psgod.UserPreferences;
import com.psgod.eventbus.RefreshEvent;
import com.psgod.model.LoginUser;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;
import com.youzan.sdk.YouzanBridge;
import com.youzan.sdk.YouzanSDK;
import com.youzan.sdk.YouzanUser;
import com.youzan.sdk.http.engine.OnRegister;
import com.youzan.sdk.http.engine.QueryError;
import com.youzan.sdk.web.plugin.YouzanWebClient;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2016/5/24.
 */
public class HomePageDynamicFragement extends BaseFragment implements View.OnClickListener {

    private WebView webview;
    private TextView webtitle;
    private TextView back;
    private TextView exit;
    private String DYNAMIC = "http://wechupin.com/index-app.html#app/dynamic";
    private String cookieDYNAMIC = null;

    private CustomProgressingDialog progressingDialog;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dynamic, container, false);

        EventBus.getDefault().register(this);
        initView(view);

        getCookie();
        // 载入网址
        webview.loadUrl(cookieDYNAMIC);
        return view;
    }
    private void getCookie() {
        String token = UserPreferences.TokenVerify.getToken();
        cookieDYNAMIC = "http://wechupin.com/index-app.html?c=" + token +"#app/dynamic";
    }


    private void initView(View view) {
        progressingDialog = new CustomProgressingDialog(getActivity());
        progressingDialog.show();
        webview = new WebView(getActivity());
        webview = (WebView) view.findViewById(R.id.fragment_dynamic_webview);
        //webtitle = (TextView) view.findViewById(R.id.webview_title);
        //back = (TextView) view.findViewById(R.id.webview_back);
        //back.setOnClickListener(this);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebViewClient(new MovieWebViewClient());
    }

    private class MovieWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            progressingDialog = new CustomProgressingDialog(getActivity());
            progressingDialog.show();
            view.loadUrl(url);
            return true;
        }

        public void onPageFinished(WebView view, String url) {
          //  webtitle.setText(view.getTitle());
            System.out.print(url);
            if (!url.equals(cookieDYNAMIC)) {
                //back.setVisibility(View.VISIBLE);
//                getActivity().findViewById(R.id.psgod_linear_tab).setVisibility(View.GONE);
//                getActivity().findViewById(R.id.psgod_rg_tab_tips).setVisibility(View.GONE);
//                getActivity().findViewById(R.id.middle).setVisibility(View.GONE);
            } else {
              //  back.setVisibility(View.GONE);
//                getActivity().findViewById(R.id.psgod_linear_tab).setVisibility(View.VISIBLE);
//                getActivity().findViewById(R.id.psgod_rg_tab_tips).setVisibility(View.VISIBLE);
//                getActivity().findViewById(R.id.middle).setVisibility(View.VISIBLE);
            }
            System.out.print("OK");
            if(progressingDialog != null && progressingDialog.isShowing()){
                progressingDialog.dismiss();
            }
        }
    }

    public void onEventMainThread(RefreshEvent event) {
        if(event.className.equals(this.getClass().getName())){
            webview.loadUrl(cookieDYNAMIC);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.webview_back:
//                webview.goBack();   //后退
//                break;
            case R.id.activity_tab_tupai_page:
                System.out.print("底部tab");
                webview.reload();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

    }
}