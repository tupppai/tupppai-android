package com.psgod.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.psgod.R;
import com.psgod.UserPreferences;
import com.psgod.model.LoginUser;
import com.psgod.ui.activity.MallActivity;
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

    private Context mContext;
    private WebView mWebview;
    private TextView mBack;
    private TextView mWebtitle;
    private String mToken;
    private String MALL = "https://wap.koudaitong.com/v2/showcase/homepage?alias=5q58ne2k";
    private LoginUser myUser;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mall, container, false);
        mContext = getActivity();

        initView(view);
        setWeb();

        return view;
    }

    //同步注册Youzan用户
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
        mWebview = (WebView) view.findViewById(R.id.fragment_mall_webview);
        mBack = (TextView) view.findViewById(R.id.webview_back);
        mWebtitle = (TextView) view.findViewById(R.id.webview_title);
        mBack.setOnClickListener(this);
        //设置支持javascript脚本
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.getSettings().setPluginState(WebSettings.PluginState.ON);
    }

    private class MallWebViewClient extends YouzanWebClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            super.shouldOverrideUrlLoading(view, url);

            //传递点击的url到商城Activity
            Intent intent = new Intent();
            intent.setClass(mContext, MallActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("Url", url);
            intent.putExtras(bundle);
            mContext.startActivity(intent);

            return true;
        }
        //页面加载结束调用，取得网页标题
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mWebtitle.setText(view.getTitle());
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.webview_back:
                break;
            default:
                break;
        }
    }


}

