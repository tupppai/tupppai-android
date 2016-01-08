package com.psgod.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.ResetPasswordRequest;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

/**
 * 重置密码
 * 
 * @author ZouMengyuan
 * 
 */
public class ResetPasswordActivity extends PSGodBaseActivity {

	private static final String TAG = ResetPasswordActivity.class
			.getSimpleName();

	private Context mContext;
	private String mPhoneNumber;
	private String mNewPassword;
	private String mVerifyCode;

	private EditText mPasswordEditText;
	private Button mCompleteButton;
	private TextView mBackTextView;
	private CustomProgressingDialog mProgressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reset_password);
		Utils.addActivity(this);

		mPhoneNumber = getIntent().getStringExtra("mPhoneNumber");
		mVerifyCode = getIntent().getStringExtra("mVerifyCode");
		mContext = ResetPasswordActivity.this;
		initViews();
		initEvents();
	}

	private void initViews() {
		mPasswordEditText = (EditText) findViewById(R.id.input_password);
		mCompleteButton = (Button) findViewById(R.id.rest_complete_btn);
		mBackTextView = (TextView) findViewById(R.id.actionbar);
	}

	private void initEvents() {
		mBackTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		mCompleteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (mProgressDialog == null) {
					mProgressDialog = new CustomProgressingDialog(
							ResetPasswordActivity.this);
				}
				if (!mProgressDialog.isShowing()) {
					mProgressDialog.show();
				}

				mNewPassword = mPasswordEditText.getText().toString().trim();
				ResetPasswordRequest.Builder builder = new ResetPasswordRequest.Builder()
						.setPhoneNumber(mPhoneNumber)
						.setNewPassword(mNewPassword)
						.setVerifyCode(mVerifyCode).setListener(null)
						.setErrorListener(errorListener);

				ResetPasswordRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						ResetPasswordActivity.this).getRequestQueue();
				requestQueue.add(request);
			}
		});
	}

	private Listener<Boolean> listener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			// TODO Toast
			mProgressDialog.dismiss();
			if (response) {
				// 把重置密码的三个Activity finish
				Utils.finishActivity();
				Intent intent = new Intent(ResetPasswordActivity.this,
						LoginActivity.class);
				startActivity(intent);
				Toast.makeText(mContext, "重置密码成功", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mContext, "重置密码失败", Toast.LENGTH_SHORT).show();
			}
		}
	};

	private PSGodErrorListener errorListener = new PSGodErrorListener(
			ResetPasswordRequest.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {
			// TODO
			mProgressDialog.dismiss();
		}
	};

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
