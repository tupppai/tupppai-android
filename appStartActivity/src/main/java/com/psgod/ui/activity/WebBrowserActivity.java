package com.psgod.ui.activity;

/**
 * 应用内嵌浏览器
 * @author brandwang
 */

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.psgod.R;
import com.psgod.UserPreferences;
import com.psgod.ui.view.TupppaiWebViewChrome;
import com.psgod.ui.widget.ActionBar;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

public class WebBrowserActivity extends PSGodBaseActivity {
	private static final String TAG = WebBrowserActivity.class.getSimpleName();

	private WebView mWebView;
	private String mTargetUrl = "http://www.qiupsdashen.com";
	private CustomProgressingDialog dialog;
	public static final String KEY_URL = "target_url";
	public static final String KEY_DESC = "desc";
	private ActionBar mActionBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);

		setContentView(R.layout.activity_web_browser);
		setProgressBarVisibility(true);

		mWebView = (WebView) findViewById(R.id.web_browser_webview);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});

		mActionBar = (ActionBar) findViewById(R.id.web_browser_actionbar);
		String desc = getIntent().getStringExtra(KEY_DESC);
		if(desc!=null && !desc.equals("")){
			mActionBar.setTitle(desc);
		}
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setWebChromeClient(new TupppaiWebViewChrome(this){
			@Override
			public void onProgressChanged(WebView view, int progress) {
				// WebBrowserActivity.this.setProgress(progress * 100);
				if (progress == 100) {
					if (dialog != null && dialog.isShowing()) {
						dialog.dismiss();
					}
				}
			}
		});
		//
		// mTargetUrl = TextUtils
		// .isEmpty(getIntent().getStringExtra("target_url")) ?
		// "http://www.qiupsdashen.com"
		// : getIntent().getStringExtra("target_url").toString();
		Intent intent = getIntent();
		String url = intent.getStringExtra(KEY_URL);
		if (url != null && url.indexOf("?") == -1) {
			url += "?from=android&v=2.0&token="
					+ UserPreferences.TokenVerify.getToken();
		} else {
			url += "&from=android&v=2.0&token="
					+ UserPreferences.TokenVerify.getToken();
		}

		mWebView.loadUrl(url);
		dialog = new CustomProgressingDialog(this);
		dialog.show(); 
		// 设置内嵌浏览器的属性
		WebSettings websettings = mWebView.getSettings();
		websettings.setJavaScriptEnabled(true);
		websettings.setBuiltInZoomControls(true);
	}
}
