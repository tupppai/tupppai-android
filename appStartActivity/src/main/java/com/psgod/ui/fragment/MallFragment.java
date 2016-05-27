package com.psgod.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.psgod.R;
import com.psgod.UserPreferences;
import com.psgod.model.LoginUser;
import com.psgod.ui.activity.MallActivity;
import com.psgod.ui.activity.MovieActivity;
import com.psgod.ui.widget.JsBridgeWebView;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;
import com.youzan.sdk.YouzanBridge;
import com.youzan.sdk.YouzanSDK;
import com.youzan.sdk.YouzanUser;
import com.youzan.sdk.http.engine.OnRegister;
import com.youzan.sdk.http.engine.QueryError;
import com.youzan.sdk.web.plugin.YouzanChromeClient;
import com.youzan.sdk.web.plugin.YouzanWebClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/5/20.
 */
public class MallFragment extends BaseFragment implements OnClickListener{

    public Context mContext;
    private String mUrl;
    private WebView mWebview;
    //private  mWebview;
    private TextView mBack;
    private TextView exit;
    private TextView mWebtitle;
    private String mToken;
    private String MALL = "https://wap.koudaitong.com/v2/showcase/homepage?alias=5q58ne2k";
    private CustomProgressingDialog progressingDialog;
    private LoginUser myUser;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mall, container, false);

        mContext = getActivity();
        initView(view);
        setWeb();

        return view;
    }

    private void setWeb() {
        YouzanUser user = new YouzanUser();
        myUser = LoginUser.getInstance();
        user.setUserId(myUser.getUid() + "");
        // 参数初始化
        YouzanBridge.build(getActivity(),mWebview).create();

        mWebview.setWebViewClient(new MallWebViewClient());


        YouzanSDK.asyncRegisterUser(user, new OnRegister() {
            @Override
            public void onFailed(QueryError queryError) {
                System.out.println("账号");
            }

            @Override
            public void onSuccess()
            {
                mWebview.loadUrl(MALL);

            }
        });
    }

    private void initView(View view) {
        mToken = UserPreferences.TokenVerify.getToken();
        progressingDialog = new CustomProgressingDialog(getActivity());
        progressingDialog.show();
        mWebview = (WebView) view.findViewById(R.id.fragment_mall_webview);
        mBack = (TextView) view.findViewById(R.id.webview_back);
        mWebtitle = (TextView) view.findViewById(R.id.webview_title);
        mBack.setOnClickListener(this);

        //mWebview.getSettings().setJavaScriptEnabled(true);
    }

    private class MallChromeClient extends YouzanChromeClient {
        public boolean onJsPrompt(WebView view, String url, String message,
                                  String defaultValue, JsPromptResult result) {
            super.onJsPrompt(view, url, message, defaultValue, result);
//            System.out.println("JsPrompt url  " + url + "\n");
//            System.out.print("message" + message + "\n");
//            System.out.print("defaultValue" + defaultValue + "\n");
//            System.out.print("JsPrompResult" + result + "\n");
            System.out.println("\n" + "url  " + url);
            System.out.println("\n" + "message " + message );
            System.out.println("\n" + "YouzanUrl  " + defaultValue);
            if (!TextUtils.isEmpty(url) && url.startsWith("http://")) {
                Intent intent = new Intent();
                intent.setClass(mContext, MallActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Url", defaultValue);
                bundle.putString("Token", myUser.getUid() + "");
                intent.putExtras(bundle);

                mContext.startActivity(intent);

                result.confirm();
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

    private class MallWebViewClient extends YouzanWebClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            super.shouldOverrideUrlLoading(view, url);
            System.out.println("\n" + "YouzanUrl " + url);
            Intent intent = new Intent();
            intent.setClass(mContext, MallActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("Url", url);
            intent.putExtras(bundle);
            mContext.startActivity(intent);
            //mWebview.goBack();

            return true;
        }
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mWebtitle.setText(view.getTitle());
            if (!url.equals(MALL)) {
                mBack.setVisibility(View.GONE);


                //getActivity().findViewById(R.id.psgod_linear_tab).setVisibility(View.INVISIBLE);
                //getActivity().findViewById(R.id.psgod_rg_tab_tips).setVisibility(View.GONE);
                //getActivity().findViewById(R.id.middle).setVisibility(View.GONE);
            } else {
                mBack.setVisibility(View.GONE);
                //getActivity().findViewById(R.id.psgod_linear_tab).setVisibility(View.VISIBLE);
                //getActivity().findViewById(R.id.psgod_rg_tab_tips).setVisibility(View.VISIBLE);
                //getActivity().findViewById(R.id.middle).setVisibility(View.VISIBLE);
            }
            if(progressingDialog != null && progressingDialog.isShowing()){
                progressingDialog.dismiss();
            }
        }
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mWebtitle.setText(view.getTitle());


        }
    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.webview_back:
                mWebview.goBack();   //后退

                break;
            case R.id.activity_inprogress_tab_page:
                mWebview.loadUrl(MALL);
                break;
            default:
                break;
        }
    }


}

