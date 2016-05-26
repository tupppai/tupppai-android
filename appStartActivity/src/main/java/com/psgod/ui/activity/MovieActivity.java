package com.psgod.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import com.psgod.R;
import com.psgod.UserPreferences;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

/**
 * Created by Administrator on 2016/5/25.
 */
public class MovieActivity extends Activity implements View.OnClickListener {

    public Context mContext;
    private WebView webview;
    private TextView webtitle;
    private ImageButton back;
    private String mUrl;
    private String mtoken;
    private TextView exit;
    private String MOVIE = "http://wechupin.com/index-app.html#app/playcategory";
    private String cookieMOVIE = null;
    private CustomProgressingDialog progressingDialog;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        mUrl = bundle.getString("Url");

        initView();

        System.out.println("\n" + "第三界面"+"\n" + mUrl);

        //webview.loadUrl(mUrl);
        //webview.loadUrl("wwww.baidu.com");

    }
    private void initView() {
        progressingDialog = new CustomProgressingDialog(this);
        progressingDialog.show();

        back = (ImageButton) findViewById(R.id.activity_webview_back);
        back.setOnClickListener(this);

        //webview = new WebView(this);
        webview = (WebView) this.findViewById(R.id.activity_movie_webview);
        webtitle = (TextView) findViewById(R.id.webview_title);

        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebViewClient(new MovieWebViewClient());
        webview.loadUrl("www.baidu.com");
    }

    private class MovieWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            progressingDialog.show();
            System.out.println("\n" + "第三界面的shouldOverrideUrl  " + url);
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
             //view.loadUrl("www.baidu.com");
        }

        public void onPageFinished(WebView view, String url) {
            webtitle.setText(view.getTitle());

            if (!url.equals(cookieMOVIE)) {
                back.setVisibility(View.VISIBLE);
//                getActivity().findViewById(R.id.psgod_linear_tab).setVisibility(View.GONE);
//                getActivity().findViewById(R.id.psgod_rg_tab_tips).setVisibility(View.GONE);
//                getActivity().findViewById(R.id.middle).setVisibility(View.GONE);
            } else {
                back.setVisibility(View.GONE);
//                getActivity().findViewById(R.id.psgod_linear_tab).setVisibility(View.VISIBLE);
//                getActivity().findViewById(R.id.psgod_rg_tab_tips).setVisibility(View.VISIBLE);
//                getActivity().findViewById(R.id.middle).setVisibility(View.VISIBLE);
            }

            if(progressingDialog != null && progressingDialog.isShowing()){
                progressingDialog.dismiss();
            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_webview_back:
                webview.goBack();   //后退
                break;
            case R.id.activity_tab_tupai_page:
                System.out.print("底部tab");
                webview.reload();
                break;
            default:
                break;
        }
    }
}

