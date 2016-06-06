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

import de.greenrobot.event.EventBus;

/**
 * 新版动态页面
 *  Created by xiaoluo on 2016/5/20.
 */
public class HomePageDynamicFragment extends BaseFragment {

    private WebView mWebview;
    private String cookieDYNAMIC = null;
    private Context mContext;
    private Long i = 1l;

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
            Bundle bundle = new Bundle();

            //以url最后7位字符dynamic做为判断,如果不一样时,才进行跳转
            String mStr = url.substring(url.length() - 7, url.length());
            System.out.println("点击webview " + url + "\n");
            if (!mStr.equals("dynamic")) {
                //当url中包含user-profile时,判断为点击到用户头像
                if (url.indexOf("user-profile/") > 0) {
                    //取用户ID，转到用户界面
                    intent.setClass(mContext, UserProfileActivity.class);
                    String mUserId = url.substring(url.indexOf("user-profile/") + 13, url.length());
                    Long mLongId = Long.parseLong(mUserId);
                    intent.putExtra(Constants.IntentKey.USER_ID, mLongId);
                    mContext.startActivity(intent);

                } else if (url != cookieDYNAMIC) {
                    // 点击的是图片
                    // http://wechupin.com/index-app.html#image_popup/http://7u2spr.com1.z0.glb.clouddn.com/20160606-1516535755236545994.jpg
                    if (url.contains("image_popup")) {
                        String picUrl = url.substring(url.indexOf("image_popup") + 12, url.length());
                        intent.setClass(mContext, PhotoBrowserActivity.class);
                        intent.putExtra(Constants.IntentKey.PHOTO_PATH, picUrl);
                        intent.putExtra(Constants.IntentKey.ASK_ID, i);
                        intent.putExtra(Constants.IntentKey.PHOTO_ITEM_ID, i);
                        intent.putExtra(Constants.IntentKey.PHOTO_ITEM_TYPE, "ask");
                        mContext.startActivity(intent);
                        System.out.println("点击图片链接 " + picUrl + "\n");
                    } else if (url.contains("producerindex")) {
                        intent.setClass(mContext, MovieActivity.class);
                        bundle.putString("Url", url);
                        intent.putExtras(bundle);
                        mContext.startActivity(intent);
                    } else {
                        // 点击到其它
                        intent.setClass(mContext, MovieActivity.class);
                        String StrUrl = url.substring(url.indexOf("#") + 1, url.length());
                        String mUrl = "http://wechupin.com/index-app.html#" + StrUrl;
                        bundle.putString("Url", mUrl);
                        intent.putExtras(bundle);
                        mContext.startActivity(intent);
                    }

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