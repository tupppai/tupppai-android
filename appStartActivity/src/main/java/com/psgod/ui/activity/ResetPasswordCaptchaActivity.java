package com.psgod.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.WeakReferenceHandler;
import com.psgod.network.request.CheckVerifyCodeRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.ResetPasswordRequest;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

/**
 * 重置密码输入验证码
 * 
 * @author ZouMengyuan
 * 
 */
public class ResetPasswordCaptchaActivity extends PSGodBaseActivity {

	private static final String TAG = ResetPasswordCaptchaActivity.class
			.getSimpleName();

	private static final int RESEND_TIME_IN_SEC = 60; // 重新发送验证时间（秒）
	private static final int MSG_TIMER = 0x3300;

	private Button mNextButton;
	private Button mResendButton;
	private EditText mCaptchaEditText;
	private TextView mBackTextView;

	private String mPhoneNumber;
	private int mLeftTime = RESEND_TIME_IN_SEC;

	private String mVerifyInput;

	private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);
	private CustomProgressingDialog mProgressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reset_password_captcha);

		Utils.addActivity(this);
		initViews();
		initEvents();

		Intent intent = getIntent();
		mPhoneNumber = intent.getStringExtra("mPhoneNumber");
	}

	private void initViews() {
		mNextButton = (Button) findViewById(R.id.next_step);
		mResendButton = (Button) findViewById(R.id.resend_captcha_btn);
		mCaptchaEditText = (EditText) findViewById(R.id.input_captcha);
		mBackTextView = (TextView) findViewById(R.id.actionbar);
	}

	private void initEvents() {
		mHandler.sendEmptyMessage(MSG_TIMER);

		// 重新发送验证码
		mResendButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mLeftTime = 60;
				if (mLeftTime > 1) {
					mLeftTime--;
					mResendButton.setEnabled(false);
					mResendButton.setText(mLeftTime + "s后可点此重发验证码");
					mResendButton.setTextColor(Color.parseColor("#BDC7CE"));
					mHandler.sendEmptyMessageDelayed(MSG_TIMER, 1000);
				} else {
					mLeftTime = RESEND_TIME_IN_SEC;
					mResendButton.setEnabled(true);
					mResendButton.setText("重新发送验证码");
					mResendButton.setTextColor(Color.parseColor("#7FC7FF"));
				}
			}
		});

		mBackTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		// 点击下一步
		mNextButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mVerifyInput = mCaptchaEditText.getText().toString().trim();

				if (mProgressDialog == null) {
					mProgressDialog = new CustomProgressingDialog(ResetPasswordCaptchaActivity.this);
				}
				if (!mProgressDialog.isShowing()) {
					mProgressDialog.show();
				}
				CheckVerifyCodeRequest.Builder builder = new CheckVerifyCodeRequest.Builder()
						.setVerifyCode(mVerifyInput).setListener(new Response.Listener<Boolean>() {
							@Override
							public void onResponse(Boolean response) {

								if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
									mProgressDialog.dismiss();
								}

								Intent intent = new Intent(
										ResetPasswordCaptchaActivity.this,
										ResetPasswordActivity.class);
								intent.putExtra("mPhoneNumber", mPhoneNumber);
								intent.putExtra("mVerifyCode", mVerifyInput);
								startActivity(intent);
							}
						}).setErrorListener(errorListener);
				CheckVerifyCodeRequest request = builder.build();
				RequestQueue requestQueue = PSGodRequestQueue
						.getInstance(
								ResetPasswordCaptchaActivity.this)
						.getRequestQueue();
				requestQueue.add(request);
			}
		});
	}

	private PSGodErrorListener errorListener = new PSGodErrorListener(
			ResetPasswordRequest.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {
			if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		}
	};

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == MSG_TIMER) {
			// 重发倒计时
			if (mLeftTime > 1) {
				mLeftTime--;
				mResendButton.setEnabled(false);
				mResendButton.setText(mLeftTime + "s后可点此重发验证码");
				mHandler.sendEmptyMessageDelayed(MSG_TIMER, 1000);
			} else {
				mLeftTime = RESEND_TIME_IN_SEC;
				mResendButton.setEnabled(true);
				mResendButton.setText("重新发送验证码");
				mResendButton.setTextColor(Color.parseColor("#7FC7FF"));
			}
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
