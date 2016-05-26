package com.psgod.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.psgod.R;
import com.psgod.ui.widget.JsBridgeWebView;

/**
 * Created by Administrator on 2016/5/25.
 */
public class MovieActivity extends Activity implements View.OnClickListener {

    public Context mContext;
    private JsBridgeWebView webview;
    private TextView webtitle;
    private ImageButton back;
    private String mUrl;
    private TextView exit;
    private String MOVIE = "http://wechupin.com/index-app.html#app/playcategory";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        initView();

    }
    private void initView() {
        webview = (JsBridgeWebView) findViewById(R.id.activity_movie_webview);
        webtitle = (TextView) findViewById(R.id.webview_title);
        back = (ImageButton) findViewById(R.id.activity_webview_back);
        back.setOnClickListener(this);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        mUrl = bundle.getString("Url");
        webview.loadUrl(mUrl);
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

