package com.pires.wesee.ui.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.pires.wesee.PSGodToast;
import com.pires.wesee.WeakReferenceHandler;
import com.pires.wesee.model.LoginUser;
import com.pires.wesee.model.RegisterData;
import com.pires.wesee.network.request.PSGodErrorListener;
import com.pires.wesee.network.request.PSGodRequestQueue;
import com.pires.wesee.ui.widget.dialog.CustomProgressingDialog;
import com.pires.wesee.Constants;
import com.pires.wesee.R;
import com.pires.wesee.network.request.RegisterRequest;

import org.json.JSONObject;

/**
 * 手机验证
 * 
 * @author brandwang
 */
public class RegisterVerifyActivity extends PSGodBaseActivity implements
		Handler.Callback {
	private static final String TAG = RegisterVerifyActivity.class
			.getSimpleName();
	public static final int JUMP_FROM_LOGIN_ACTIVITY = 100;
	private static final int RESEND_TIME_IN_SEC = 60; // 重新发送验证时间（秒）
	private static final int MSG_TIMER = 0x3300;

	private Button mResendBtn;
	private EditText mPhoneVerifyCode;
	private RegisterData mRegisterData;
	private int mLeftTime = RESEND_TIME_IN_SEC;
	private TextView mBackTextView;

	private ImageView mSendCodeBtn;
	private CustomProgressingDialog mProgressDialog;
	private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_verify);

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
		mResendBtn = (Button) findViewById(R.id.resend_verify_btn);
		mPhoneVerifyCode = (EditText) findViewById(R.id.phone_vertify_code);
		mResendBtn.setEnabled(false);
		mResendBtn.setText("点击重新发送(60)");
		mBackTextView = (TextView) findViewById(R.id.actionbar);
		mSendCodeBtn = (ImageView) findViewById(R.id.activity_register_verify_next_btn);
	}

	private void initEvents() {
		mHandler.sendEmptyMessage(MSG_TIMER);

		mResendBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mLeftTime = 60;
				if (mLeftTime > 1) {
					mLeftTime--;
					mResendBtn.setEnabled(false);
					mResendBtn.setText(mLeftTime + "s后可点此重发验证码");
					mResendBtn.setTextColor(Color.parseColor("#BDC7CE"));
					mHandler.sendEmptyMessageDelayed(MSG_TIMER, 1000);
				} else {
					mLeftTime = RESEND_TIME_IN_SEC;
					mResendBtn.setEnabled(true);
					mResendBtn.setText("重新发送验证码");
					mResendBtn.setTextColor(Color.parseColor("#FF6D3F"));
				}
			}
		});

		mBackTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});

		// 点击注册完成
		mSendCodeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {

				// 显示等待对话框
				if (mProgressDialog == null) {
					mProgressDialog = new CustomProgressingDialog(
							RegisterVerifyActivity.this);
				}
				if (!mProgressDialog.isShowing()) {
					mProgressDialog.show();
				}
				String inputCode = mPhoneVerifyCode.getText().toString();
				mRegisterData.setVerifyCode(inputCode);
				RegisterRequest.Builder builder = new RegisterRequest.Builder()
						.setRegisterData(mRegisterData)
						.setErrorListener(errorListener)
						.setListener(registerListener);
				RegisterRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue
						.getInstance(RegisterVerifyActivity.this)
						.getRequestQueue();
				requestQueue.add(request);
			}
		});
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

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == MSG_TIMER) {
			// 重发倒计时
			if (mLeftTime > 1) {
				mLeftTime--;
				mResendBtn.setEnabled(false);
				mResendBtn.setText(mLeftTime + "s后可点此重发验证码");
				mHandler.sendEmptyMessageDelayed(MSG_TIMER, 1000);
			} else {
				mLeftTime = RESEND_TIME_IN_SEC;
				mResendBtn.setEnabled(true);
				mResendBtn.setText("重新发送验证码");
				mResendBtn.setTextColor(Color.parseColor("#FF6D3F"));
			}
		}
		return true;
	}

	private Listener<JSONObject> registerListener = new Listener<JSONObject>() {
		@Override
		public void onResponse(JSONObject data) {
			if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			showToast(new PSGodToast("注册成功"));

			if (data != null) {
				// 存储服务端返回的用户信息到sp
				LoginUser.getInstance().initFromJSONObject(data);

				Bundle extras = new Bundle();
				extras.putInt(Constants.IntentKey.ACTIVITY_JUMP_FROM,
						JUMP_FROM_LOGIN_ACTIVITY);

				MainActivity.startNewActivityAndFinishAllBefore(
						RegisterVerifyActivity.this,
						MainActivity.class.getName(), extras);
			}
		}
	};

	private PSGodErrorListener errorListener = new PSGodErrorListener(
			RegisterRequest.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {
			if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			// TODO
			showToast(new PSGodToast("注册失败"));
		}
	};
}
