package com.pires.wesee.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.pires.wesee.Utils;
import com.pires.wesee.network.request.GetVerifyCodeRequest;
import com.pires.wesee.network.request.PSGodErrorListener;
import com.pires.wesee.network.request.PSGodRequestQueue;
import com.pires.wesee.network.request.ResetPasswordCheckPhoneNumRequest;
import com.pires.wesee.ui.widget.dialog.CustomDialog;
import com.pires.wesee.ui.widget.dialog.CustomProgressingDialog;
import com.pires.wesee.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 重置密码输入手机号
 * 
 * @author ZouMengyuan
 * 
 */

public class ResetPasswordInputPhoneActivity extends PSGodBaseActivity {

	private static final String TAG = ResetPasswordInputPhoneActivity.class
			.getSimpleName();

	private Button mNextButton;
	private EditText mEditPhone;
	private TextView mBackTextView;

	private String mPhoneNumber;

	private CustomProgressingDialog mProgressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reset_password_input_phone);

		Utils.addActivity(this);

		initViews();
		initEvents();
	}

	private void initViews() {
		mNextButton = (Button) findViewById(R.id.next_step);
		mEditPhone = (EditText) findViewById(R.id.input_phone);
		mBackTextView = (TextView) findViewById(R.id.actionbar);
	}

	private void initEvents() {
		mBackTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});

		// 点击下一步
		mNextButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (validate()) {
					mPhoneNumber = mEditPhone.getText().toString().trim();
					ResetPasswordCheckPhoneNumRequest.Builder builder = new ResetPasswordCheckPhoneNumRequest.Builder()
							.setPhoneNumber(mPhoneNumber).setListener(listener)
							.setErrorListener(errorListener);
					ResetPasswordCheckPhoneNumRequest request = builder.build();
					request.setTag(TAG);
					RequestQueue requestQueue = PSGodRequestQueue.getInstance(
							ResetPasswordInputPhoneActivity.this)
							.getRequestQueue();
					requestQueue.add(request);
				}

			}
		});
	}

	/**
	 * 校验手机格式
	 * 
	 * @param text
	 * @return
	 */

	private boolean matchPhoneNum(String text) {
		Pattern p = Pattern
				.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
		Matcher m = p.matcher(text);
		return m.matches();
	}

	public boolean validate() {
		if (TextUtils.isEmpty(mEditPhone.getText())) {
			Toast.makeText(ResetPasswordInputPhoneActivity.this, "请填写手机号码",
					Toast.LENGTH_SHORT).show();
			mEditPhone.requestFocus();
			return false;
		}
		String phoneNum = mEditPhone.getText().toString().trim();
		if (!Utils.matchPhoneNum(phoneNum)) {
			Toast.makeText(ResetPasswordInputPhoneActivity.this, "手机号码格式不正确",
					Toast.LENGTH_SHORT).show();
			mEditPhone.requestFocus();
			return false;
		}
		return true;
	}

	private Listener<JSONObject> listener = new Listener<JSONObject>() {
		@Override
		public void onResponse(JSONObject response) {
			if (response != null) {
				try {
					if (response.getBoolean("has_registered") == false) {
						CustomDialog.Builder mBuilder = new CustomDialog.Builder(
								ResetPasswordInputPhoneActivity.this)
								.setMessage("该手机号码未注册，是否返回注册？")
								.setLeftButton("重填手机号码",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface arg0,
													int arg1) {
												mEditPhone.setText("");
												mEditPhone.requestFocus();
											}
										})
								.setRightButton("返回注册",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface arg0,
													int arg1) {
												Intent intent = new Intent(
														ResetPasswordInputPhoneActivity.this,
														SetInfoActivity.class);
												startActivity(intent);
												finish();
											}
										});
						mBuilder.create().show();
					} else {
						CustomDialog.Builder builder = new CustomDialog.Builder(
								ResetPasswordInputPhoneActivity.this)
								.setMessage(
										"确认手机号码，我们将发验证码到此手机号码" + mPhoneNumber)
								.setLeftButton("取消", null)
								.setRightButton("确定",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface arg0,
													int arg1) {
												// 显示等待对话框
												if (mProgressDialog == null) {
													mProgressDialog = new CustomProgressingDialog(
															ResetPasswordInputPhoneActivity.this);
												}
												if (!mProgressDialog
														.isShowing()) {
													mProgressDialog.show();
												}

												GetVerifyCodeRequest.Builder builder = new GetVerifyCodeRequest.Builder()
														.setPhone(mPhoneNumber)
														.setListener(
																getVerifyCodeListener)
														.setErrorListener(
																errorListener);
												GetVerifyCodeRequest request = builder
														.build();
												request.setTag(TAG);
												RequestQueue requestQueue = PSGodRequestQueue
														.getInstance(
																ResetPasswordInputPhoneActivity.this)
														.getRequestQueue();
												requestQueue.add(request);
											}
										});
						builder.create().show();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	// 获取到验证码之后的跳转 跳转到验证码验证页面
	private Listener<Boolean> getVerifyCodeListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}

			Intent intent = new Intent(
					ResetPasswordInputPhoneActivity.this,
					ResetPasswordCaptchaActivity.class);
			intent.putExtra("mPhoneNumber", mPhoneNumber);
			startActivity(intent);
		}
	};

	private PSGodErrorListener errorListener = new PSGodErrorListener(this) {

		@Override
		public void handleError(VolleyError error) {
			// TODO Auto-generated method stub
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
