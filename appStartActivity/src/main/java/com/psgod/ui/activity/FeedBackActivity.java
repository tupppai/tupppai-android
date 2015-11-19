package com.psgod.ui.activity;

/**	
 * 用户反馈activity
 * @author brandwang
 */

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.network.request.FeedBackRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.ui.widget.ActionBar;

public class FeedBackActivity extends PSGodBaseActivity {
	private final static String TAG = FeedBackActivity.class.getSimpleName();

	private EditText mFeedBackContent;

	private String mContent;

	private ActionBar mActionBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feedback);

		mFeedBackContent = (EditText) findViewById(R.id.activity_feed_back_content);
		mActionBar = (ActionBar) findViewById(R.id.actionbar);

		initEvents();
	}

	private void initEvents() {
		mActionBar.setRightBtnOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				mContent = mFeedBackContent.getText().toString().trim();

				if (validate()) {
					FeedBackRequest.Builder builder = new FeedBackRequest.Builder()
							.setContent(mContent).setListener(feedBackListener)
							.setErrorListener(errorListener);
					FeedBackRequest request = builder.build();
					request.setTag(TAG);
					RequestQueue requestQueue = PSGodRequestQueue.getInstance(
							FeedBackActivity.this).getRequestQueue();
					requestQueue.add(request);
				}
			}
		});
	}

	// 用户反馈listener
	private Listener<Boolean> feedBackListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			if (response) {
				Toast.makeText(FeedBackActivity.this, "反馈成功",
						Toast.LENGTH_SHORT).show();
				finish();
			} else {
				Toast.makeText(FeedBackActivity.this, "反馈提交失败，请稍后再试",
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	private PSGodErrorListener errorListener = new PSGodErrorListener(
			SettingPasswordActivity.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {
			// TODO
		}
	};

	// 提交前校验函数
	private boolean validate() {
		if (Utils.isNull(mFeedBackContent)) {
			Toast.makeText(FeedBackActivity.this, "反馈内容不能为空",
					Toast.LENGTH_SHORT).show();
			mFeedBackContent.requestFocus();
			return false;
		}

		return true;
	}

	/**
	 * 暂停所有的下载
	 */
	@Override
	public void onStop() {
		super.onStop();
		RequestQueue requestQueue = PSGodRequestQueue.getInstance(this)
				.getRequestQueue();
		requestQueue.cancelAll(TAG);
	}
}
