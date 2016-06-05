package com.pires.wesee.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.pires.wesee.Utils;
import com.pires.wesee.network.request.ModifyPassWordRequest;
import com.pires.wesee.network.request.PSGodErrorListener;
import com.pires.wesee.network.request.PSGodRequestQueue;
import com.pires.wesee.ui.widget.ActionBar;
import com.pires.wesee.ui.widget.dialog.CustomProgressingDialog;
import com.pires.wesee.R;

public class SettingPasswordActivity extends PSGodBaseActivity {
	private final static String TAG = SettingPasswordActivity.class
			.getSimpleName();

	private ActionBar mActionBar;
	private EditText mOldPassWordEt;
	private EditText mNewPasswordEt;
	private EditText mNewConfirmEt;

	private String mOldPw;
	private String mNewPw;
	private String mNewPwConfirm;

	private CustomProgressingDialog mProgressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_password);

		mActionBar = (ActionBar) findViewById(R.id.actionbar);
		mOldPassWordEt = (EditText) findViewById(R.id.login_old_password);
		mNewPasswordEt = (EditText) findViewById(R.id.activity_setting_password_new_password_edittext);
		mNewConfirmEt = (EditText) findViewById(R.id.activity_setting_password_confirm_password_edittext);

		// 实际监听
		initEvents();
	}

	private void initEvents() {
		mActionBar.setRightBtnOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				mOldPw = mOldPassWordEt.getText().toString().trim();
				mNewPw = mNewPasswordEt.getText().toString().trim();
				mNewPwConfirm = mNewConfirmEt.getText().toString().trim();

				if (validate()) {
					// 显示等待对话框
					if (mProgressDialog == null) {
						mProgressDialog = new CustomProgressingDialog(
								SettingPasswordActivity.this);
					}

					if (!mProgressDialog.isShowing()) {
						mProgressDialog.show();
					}

					ModifyPassWordRequest.Builder builder = new ModifyPassWordRequest.Builder()
							.setNewPwd(mNewPw).setOldPwd(mOldPw)
							.setListener(modifyPwdListener)
							.setErrorListener(errorListener);
					ModifyPassWordRequest request = builder.build();
					request.setTag(TAG);
					RequestQueue requestQueue = PSGodRequestQueue.getInstance(
							SettingPasswordActivity.this).getRequestQueue();
					requestQueue.add(request);
				}
			}
		});
	}

	// 修改密码返回Listener
	private Listener<Boolean> modifyPwdListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			mProgressDialog.dismiss();
			Toast.makeText(SettingPasswordActivity.this, "密码修改成功",
					Toast.LENGTH_SHORT).show();
			finish();
		}
	};

	private PSGodErrorListener errorListener = new PSGodErrorListener(
			SettingPasswordActivity.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {
			mProgressDialog.dismiss();
		}
	};

	// 本地校验
	private boolean validate() {
		if (Utils.isNull(mOldPassWordEt)) {
			Toast.makeText(SettingPasswordActivity.this, "请输入原密码",
					Toast.LENGTH_SHORT).show();
			mOldPassWordEt.requestFocus();
			return false;
		}

		if (Utils.isNull(mNewPasswordEt)) {
			Toast.makeText(SettingPasswordActivity.this, "请输入新密码",
					Toast.LENGTH_SHORT).show();
			mNewPasswordEt.requestFocus();
			return false;
		}

		if (Utils.isNull(mNewConfirmEt)) {
			Toast.makeText(SettingPasswordActivity.this, "请确认新密码",
					Toast.LENGTH_SHORT).show();
			mNewConfirmEt.requestFocus();
			return false;
		}
		Log.e(TAG, mNewPw);
		Log.e(TAG, mNewPwConfirm);
		if (!mNewPw.equals(mNewPwConfirm)) {
			Toast.makeText(SettingPasswordActivity.this, "两次输入的密码不一致",
					Toast.LENGTH_SHORT).show();
			return false;
		}

		if (mNewPw.length() < 6 || mNewPwConfirm.length() < 6) {
			Toast.makeText(SettingPasswordActivity.this, "密码设置不能少于六位",
					Toast.LENGTH_SHORT).show();
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
