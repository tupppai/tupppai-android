package com.psgod.ui.activity;

/**
 * 手机注册
 * @author brandwang
 */
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.RegisterData;
import com.psgod.network.request.GetVerifyCodeRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.RegisterCheckPhoneNumRequest;
import com.psgod.ui.widget.dialog.CustomDialog;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

public class RegisterPhoneActivity extends PSGodBaseActivity implements
		Handler.Callback {
	private static final String TAG = RegisterPhoneActivity.class
			.getSimpleName();

	private EditText phone_register_number;
	private EditText phone_register_pwd;

	private TextView mBackTextView;
	private ImageView mNextBtn;
	private RegisterData mRegisterData;
	private String mPhoneNumber;

	private CustomProgressingDialog mProgressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_phone);
		mRegisterData = (RegisterData) getIntent().getParcelableExtra(
				Constants.IntentKey.REGISTER_DATA);
		if (mRegisterData == null) {
			Toast.makeText(this, TAG + ".onCreate(): mReigsterData is null",
					Toast.LENGTH_LONG).show();
			finish();
		}
		initViews();
		initEvents();
	}

	private void initViews() {
		phone_register_number = (EditText) findViewById(R.id.phone_et);
		phone_register_pwd = (EditText) findViewById(R.id.password_et);
		phone_register_number.requestFocus();
		mNextBtn = (ImageView) findViewById(R.id.activity_register_phone_next_btn);
		mBackTextView = (TextView) findViewById(R.id.actionbar);
	}

	private void initEvents() {
		// 下一步点击事件
		mNextBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (validate()) {
					// 显示等待对话框
					if (mProgressDialog == null) {
						mProgressDialog = new CustomProgressingDialog(
								RegisterPhoneActivity.this);
					}
					if (!mProgressDialog.isShowing()) {
						mProgressDialog.show();
					}

					// 点击下一步 首先判断该号码是否被注册过
					mPhoneNumber = phone_register_number.getText().toString()
							.trim();
					RegisterCheckPhoneNumRequest.Builder builder = new RegisterCheckPhoneNumRequest.Builder()
							.setPhoneNumber(mPhoneNumber)
							.setListener(checkPhoneListener)
							.setErrorListener(errorListener);
					RegisterCheckPhoneNumRequest request = builder.build();
					request.setTag(TAG);
					RequestQueue requestQueue = PSGodRequestQueue.getInstance(
							RegisterPhoneActivity.this).getRequestQueue();
					requestQueue.add(request);
				}
			}
		});

		mBackTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
	}

	@Override
	public void onStop() {
		super.onStop();
		RequestQueue requestQueue = PSGodRequestQueue.getInstance(this)
				.getRequestQueue();
		requestQueue.cancelAll(TAG);
	}

	// 获取到验证码之后的跳转 跳转到验证码验证页面
	private Listener<Boolean> getVerifyCodeListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			String phonePwd = phone_register_pwd.getText().toString()
					.trim();
			mRegisterData.setPhoneNumber(mPhoneNumber);
			mRegisterData.setPassword(phonePwd);

			Intent intent = new Intent(RegisterPhoneActivity.this,
					RegisterVerifyActivity.class);
			intent.putExtra(Constants.IntentKey.REGISTER_DATA,
					mRegisterData);
			startActivity(intent);
		}
	};

	// 检测手机号码是否注册过接口
	private Listener<Boolean> checkPhoneListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}

			// 手机被注册过
			if (response) {
				if (!mRegisterData.getThirdAuthType().equals("mobile")) {
					// 第三方登录时候
					CustomDialog.Builder mBuilder = new CustomDialog.Builder(
							RegisterPhoneActivity.this)
							.setMessage("该手机号码已注册，确定绑定该号码？")
							.setLeftButton("重填手机号",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											phone_register_number.setText("");
											phone_register_number
													.requestFocus();
										}
									})
							.setRightButton("确定绑定",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
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
															RegisterPhoneActivity.this)
													.getRequestQueue();
											requestQueue.add(request);
										}
									});
					mBuilder.create().show();
				} else {
					// 手机注册时
					CustomDialog.Builder mBuilder = new CustomDialog.Builder(
							RegisterPhoneActivity.this)
							.setMessage("该号码已绑定其他求图派账号")
							.setLeftButton("重填手机号",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											phone_register_number.setText("");
											phone_register_number
													.requestFocus();
										}
									})
							.setRightButton("返回登录",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											Intent intent = new Intent(
													RegisterPhoneActivity.this,
													LoginActivity.class);
											startActivity(intent);
										}
									});
					mBuilder.create().show();
				}
			} else {
				// 没有注册过的情况 直接询问发送短信验证码
				CustomDialog.Builder builder = new CustomDialog.Builder(
						RegisterPhoneActivity.this)
						.setMessage("将要向该手机号码发送短信验证码")
						.setLeftButton("取消", null)
						.setRightButton("确定",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {

										// 显示等待对话框
										if (mProgressDialog == null) {
											mProgressDialog = new CustomProgressingDialog(
													RegisterPhoneActivity.this);
										}
										if (!mProgressDialog.isShowing()) {
											mProgressDialog.show();
										}

										GetVerifyCodeRequest.Builder builder = new GetVerifyCodeRequest.Builder()
												.setPhone(mPhoneNumber)
												.setListener(
														getVerifyCodeListener)
												.setErrorListener(errorListener);
										GetVerifyCodeRequest request = builder
												.build();
										request.setTag(TAG);
										RequestQueue requestQueue = PSGodRequestQueue
												.getInstance(
														RegisterPhoneActivity.this)
												.getRequestQueue();
										requestQueue.add(request);
									}
								});
				builder.create().show();
			}
		}
	};

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

	// 验证手机号码格式和简单密码格式判断
	// TODO 复杂密码格式校验，需定义
	public boolean validate() {
		if (TextUtils.isEmpty(phone_register_number.getText())) {
			Toast.makeText(RegisterPhoneActivity.this, "请填写手机号码",
					Toast.LENGTH_SHORT).show();
			phone_register_number.requestFocus();
			return false;
		}

		String phoneNum = phone_register_number.getText().toString().trim();
		if (!Utils.matchPhoneNum(phoneNum)) {
			Toast.makeText(RegisterPhoneActivity.this, "手机号码格式不正确",
					Toast.LENGTH_SHORT).show();
			phone_register_number.requestFocus();
			return false;
		}

		if (TextUtils.isEmpty(phone_register_pwd.getText())) {
			Toast.makeText(RegisterPhoneActivity.this, "请填写密码",
					Toast.LENGTH_SHORT).show();
			phone_register_pwd.requestFocus();
			return false;
		}

		String phonePwd = phone_register_pwd.getText().toString().trim();
		if (phonePwd.length() < 6) {
			Toast.makeText(RegisterPhoneActivity.this, "密码不能少于六位",
					Toast.LENGTH_SHORT).show();
			phone_register_pwd.requestFocus();
			return false;
		}
		return true;
	}

	private PSGodErrorListener errorListener = new PSGodErrorListener(
			RegisterCheckPhoneNumRequest.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {
			if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		}
	};
}
