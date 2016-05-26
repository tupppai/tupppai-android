package com.psgod.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.psgod.R;
import com.psgod.UserPreferences;
import com.psgod.ui.widget.JsBridgeWebView;

/**
 * Created by Administrator on 2016/5/20.
 */
public class MovieFragment extends BaseFragment implements OnClickListener{

    public Context mContext;
    private JsBridgeWebView webview;
    private TextView webtitle;
    private TextView back;
    private TextView exit;

    private String MOVIE = "http://wechupin.com/index-app.html";
    private String HASH = "#app/playcategory";
    private String mUrl = null;
    private String mToken;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie, container, false);
        mContext = getActivity();

        initView(view);

        return view;
    }

    private void initView(View view) {
        webview = new JsBridgeWebView(mContext);
        webview = (JsBridgeWebView) view.findViewById(R.id.fragment_movie_webview);

        webtitle = (TextView) view.findViewById(R.id.webview_title);
        back = (TextView) view.findViewById(R.id.webview_back);
        back.setOnClickListener(this);

        mToken = UserPreferences.TokenVerify.getToken();
        MOVIE = MOVIE + "?C=" + mToken + "&from=android" + HASH;
        webview.loadUrl(MOVIE);
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

