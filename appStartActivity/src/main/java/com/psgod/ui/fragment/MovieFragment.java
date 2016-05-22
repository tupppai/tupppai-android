package com.psgod.ui.fragment;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.psgod.R;
import com.psgod.model.LoginUser;
import com.psgod.model.Tupppai;
import com.psgod.ui.adapter.TupppaiAdapter;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;
import com.youzan.sdk.YouzanSDK;
import com.youzan.sdk.YouzanUser;
import com.youzan.sdk.http.engine.OnRegister;
import com.youzan.sdk.http.engine.QueryError;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/5/20.
 */
public class MovieFragment extends BaseFragment implements OnClickListener{

    private WebView webview;
    private TextView webtitle;
    private TextView back;
    private TextView exit;
    private String MOVIE = "http://wechupin.com/index-app.html#app/playcategory";

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie, container, false);

        initView(view);
        // 载入网址
        webview.loadUrl(MOVIE);
        return view;
    }

    private void initView(View view) {
        webview = new WebView(getActivity());
        webview = (WebView) view.findViewById(R.id.fragment_movie_webview);
        webtitle = (TextView) view.findViewById(R.id.webview_title);
        back = (TextView) view.findViewById(R.id.webview_back);
        back.setOnClickListener(this);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebViewClient(new MovieWebViewClient());
    }

    private class MovieWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        public void onPageFinished(WebView view, String url) {
            webtitle.setText(view.getTitle());
            System.out.print(url);
            if (!url.equals(MOVIE)) {
                back.setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.psgod_linear_tab).setVisibility(View.GONE);
                getActivity().findViewById(R.id.psgod_rg_tab_tips).setVisibility(View.GONE);
                getActivity().findViewById(R.id.middle).setVisibility(View.GONE);
            } else {
                back.setVisibility(View.GONE);
                //getActivity().findViewById(R.id.psgod_linear_tab).setVisibility(View.VISIBLE);
            }
            System.out.print("OK");
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.webview_back:
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

