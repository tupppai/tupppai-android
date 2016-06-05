package com.pires.wesee.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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
import com.pires.wesee.Constants;
import com.pires.wesee.CustomToast;
import com.pires.wesee.PSGodApplication;
import com.pires.wesee.Utils;
import com.pires.wesee.model.LoginUser;
import com.pires.wesee.network.request.PSGodErrorListener;
import com.pires.wesee.network.request.PSGodRequestQueue;
import com.pires.wesee.network.request.QQLoginRequest;
import com.pires.wesee.network.request.WechatUserInfoRequest;
import com.pires.wesee.network.request.WeiboLoginRequest;
import com.pires.wesee.ui.widget.dialog.CustomDialog;
import com.pires.wesee.ui.widget.dialog.CustomProgressingDialog;
import com.pires.wesee.R;
import com.pires.wesee.network.request.UserLoginRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

public class LoginActivity extends PSGodBaseActivity implements
		Handler.Callback {
	private final static String TAG = LoginActivity.class.getSimpleName();
	public static final int JUMP_FROM_LOGIN = 100;
	private static final String QQPLAT = "qq";
	private static final String WEIBOPLAT = "weibo";
	private static final String WEIXINPLAT = "weixin";

	private Button mLoginButton;
	private EditText mLoginNum;
	private EditText mLoginPassword;
	private Button mResetButton;

	private TextView mBackTextView;

	private ImageView mWeiboLoginBtn;
	private ImageView mWechatLoginBtn;
	private ImageView mQQLoginBtn;

	private String mThirdAuthId = "";
	private String mThirdAuthName = "";
	private String mThirdAuthGender = "";
	private String mThirdAuthAvatar = "";

	private CustomProgressingDialog mProgressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		Utils.initializeActivity();
		Utils.addActivity(this);

		initViews();
		initEvents();
	}

	private void initViews() {
		mLoginNum = (EditText) findViewById(R.id.login_number);
		mLoginPassword = (EditText) findViewById(R.id.login_pwd);
		mLoginButton = (Button) findViewById(R.id.login_confirm_btn);
		mResetButton = (Button) findViewById(R.id.reset_button);

		// 返回键，返回到上一个Activity
		mBackTextView = (TextView) findViewById(R.id.actionbar);

		mWeiboLoginBtn = (ImageView) findViewById(R.id.activity_login_weibo_btn);
		mWechatLoginBtn = (ImageView) findViewById(R.id.activity_login_wechat_btn);
		mQQLoginBtn = (ImageView) findViewById(R.id.activity_login_qq_btn);
	}

	// QQ登录监听事件
	private PlatformActionListener qqLoginListener = new PlatformActionListener() {

		@Override
		public void onError(Platform arg0, int arg1, Throwable arg2) {
			if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		}

		@Override
		public void onComplete(Platform arg0, int arg1,
				HashMap<String, Object> res) {
			mThirdAuthId = ShareSDK.getPlatform(QQ.NAME).getDb().getUserId();
			mThirdAuthName = res.get("nickname").toString();
			mThirdAuthAvatar = res.get("figureurl_qq_2").toString();

			if (!TextUtils.isEmpty(mThirdAuthId)) {
				QQLoginRequest.Builder builder = new QQLoginRequest.Builder()
						.setCode(mThirdAuthId).setListener(qqAuthListener)
						.setErrorListener(errorListener);

				QQLoginRequest request = builder.build();
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						LoginActivity.this).getRequestQueue();
				requestQueue.add(request);
			}
		}

		@Override
		public void onCancel(Platform arg0, int arg1) {
			// TODO Auto-generated method stub
			if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		}
	};

	private Listener<QQLoginRequest.QQLoginWrapper> qqAuthListener = new Listener<QQLoginRequest.QQLoginWrapper>() {

		@Override
		public void onResponse(QQLoginRequest.QQLoginWrapper response) {
			if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}

			try {
				int isRegistered = response.isRegistered;
				// 已注册
				if (isRegistered == 1) {
					JSONObject userInfoData = response.UserObject;
					LoginUser.getInstance().initFromJSONObject(userInfoData);

					Bundle extras = new Bundle();
					extras.putInt(Constants.IntentKey.ACTIVITY_JUMP_FROM,
							LoginActivity.JUMP_FROM_LOGIN);

					// 关闭之前所有页面
					MainActivity.startNewActivityAndFinishAllBefore(
							LoginActivity.this, MainActivity.class.getName(),
							extras);
				}
				// 未注册
				if (isRegistered == 0) {
					SharedPreferences.Editor editor = PSGodApplication
							.getAppContext()
							.getSharedPreferences(Constants.SharedPreferencesKey.NAME,
									Context.MODE_PRIVATE).edit();
					editor.putString(Constants.ThirdAuthInfo.THIRD_AUTH_PLATFORM,QQPLAT);
					editor.putString(Constants.ThirdAuthInfo.USER_OPENID, mThirdAuthId);
					editor.putString(Constants.ThirdAuthInfo.USER_AVATAR, mThirdAuthAvatar);
					editor.putString(Constants.ThirdAuthInfo.USER_NICKNAME, mThirdAuthName);
					if (android.os.Build.VERSION.SDK_INT >= 9) {
						editor.apply();
					} else {
						editor.commit();
					}

					JSONObject userInfoData = new JSONObject();
					userInfoData.put("uid", 0l);
					userInfoData.put("nickname", mThirdAuthName);
					userInfoData.put("sex", 0);
					userInfoData.put("phone", "0");
					userInfoData.put("avatar", mThirdAuthAvatar);
					userInfoData.put("fans_count", 0);
					userInfoData.put("fellow_count", 0);
					userInfoData.put("uped_count", 0);
					userInfoData.put("ask_count", 0);
					userInfoData.put("reply_count", 0);
					userInfoData.put("inprogress_count", 0);
					userInfoData.put("collection_count", 0);
					userInfoData.put("is_bound_weixin", 0);
					userInfoData.put("is_bound_qq", 0);
					userInfoData.put("is_bound_weibo", 0);
					userInfoData.put("city", 1);
					userInfoData.put("province", 11);

					LoginUser.getInstance().initFromJSONObject(userInfoData);

					Bundle extras = new Bundle();
					extras.putInt(Constants.IntentKey.ACTIVITY_JUMP_FROM,
							JUMP_FROM_LOGIN);
					MainActivity.startNewActivityAndFinishAllBefore(
							LoginActivity.this, MainActivity.class.getName(),
							extras);
//					Intent intent = new Intent(LoginActivity.this,
//							SetInfoActivity.class);
//
//					intent.putExtra(
//							Constants.ThirdAuthInfo.THIRD_AUTH_PLATFORM, "qq");
//					intent.putExtra(Constants.ThirdAuthInfo.USER_OPENID,
//							mThirdAuthId);
//					intent.putExtra(Constants.ThirdAuthInfo.USER_AVATAR,
//							mThirdAuthAvatar);
//					intent.putExtra(Constants.ThirdAuthInfo.USER_NICKNAME,
//							mThirdAuthName);
//
//					LoginActivity.this.startActivity(intent);
				}
			} catch (Exception e) {
			}
		}
	};

	// 微博登录的监听事件
	private PlatformActionListener weiboLoginListener = new PlatformActionListener() {
		@Override
		public void onError(Platform arg0, int arg1, Throwable arg2) {
			if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		}

		// 用户授权
		@Override
		public void onComplete(Platform arg0, int arg1,
				HashMap<String, Object> res) {
			mThirdAuthId = res.get("id").toString();
			mThirdAuthName = res.get("name").toString();
			mThirdAuthAvatar = res.get("profile_image_url").toString();

			if (!TextUtils.isEmpty(mThirdAuthId)) {
				WeiboLoginRequest.Builder builder = new WeiboLoginRequest.Builder()
						.setCode(mThirdAuthId).setListener(weiboAuthListener)
						.setErrorListener(errorListener);

				WeiboLoginRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						LoginActivity.this).getRequestQueue();
				requestQueue.add(request);
			}
		}

		@Override
		public void onCancel(Platform arg0, int arg1) {
			// TODO Auto-generated method stub
			if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		}
	};

	private Listener<WeiboLoginRequest.WeiboLoginWrapper> weiboAuthListener = new Listener<WeiboLoginRequest.WeiboLoginWrapper>() {
		@Override
		public void onResponse(WeiboLoginRequest.WeiboLoginWrapper response) {
			if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}

			try {
				int isRegistered = response.isRegistered;
				// 已注册
				if (isRegistered == 1) {
					JSONObject userInfoData = response.UserObject;
					LoginUser.getInstance().initFromJSONObject(userInfoData);

					Bundle extras = new Bundle();
					extras.putInt(Constants.IntentKey.ACTIVITY_JUMP_FROM,
							JUMP_FROM_LOGIN);
					MainActivity.startNewActivityAndFinishAllBefore(
							LoginActivity.this, MainActivity.class.getName(),
							extras);
				}
				// 未注册
				if (isRegistered == 0) {

					SharedPreferences.Editor editor = PSGodApplication
							.getAppContext()
							.getSharedPreferences(Constants.SharedPreferencesKey.NAME,
									Context.MODE_PRIVATE).edit();
					editor.putString(Constants.ThirdAuthInfo.THIRD_AUTH_PLATFORM,WEIBOPLAT);
					editor.putString(Constants.ThirdAuthInfo.USER_OPENID, mThirdAuthId);
					editor.putString(Constants.ThirdAuthInfo.USER_AVATAR, mThirdAuthAvatar);
					editor.putString(Constants.ThirdAuthInfo.USER_NICKNAME, mThirdAuthName);
					if (android.os.Build.VERSION.SDK_INT >= 9) {
						editor.apply();
					} else {
						editor.commit();
					}

					JSONObject userInfoData = new JSONObject();
					userInfoData.put("uid", 0l);
					userInfoData.put("nickname", mThirdAuthName);
					userInfoData.put("sex", 0);
					userInfoData.put("phone", "0");
					userInfoData.put("avatar", mThirdAuthAvatar);
					userInfoData.put("fans_count", 0);
					userInfoData.put("fellow_count", 0);
					userInfoData.put("uped_count", 0);
					userInfoData.put("ask_count", 0);
					userInfoData.put("reply_count", 0);
					userInfoData.put("inprogress_count", 0);
					userInfoData.put("collection_count", 0);
					userInfoData.put("is_bound_weixin", 0);
					userInfoData.put("is_bound_qq", 0);
					userInfoData.put("is_bound_weibo", 0);
					userInfoData.put("city", 1);
					userInfoData.put("province", 11);

					LoginUser.getInstance().initFromJSONObject(userInfoData);

					Bundle extras = new Bundle();
					extras.putInt(Constants.IntentKey.ACTIVITY_JUMP_FROM,
							JUMP_FROM_LOGIN);
					MainActivity.startNewActivityAndFinishAllBefore(
							LoginActivity.this, MainActivity.class.getName(),
							extras);
//					Intent intent = new Intent(LoginActivity.this,
//							SetInfoActivity.class);
//
//					intent.putExtra(
//							Constants.ThirdAuthInfo.THIRD_AUTH_PLATFORM,
//							"weibo");
//					intent.putExtra(Constants.ThirdAuthInfo.USER_OPENID,
//							mThirdAuthId);
//					intent.putExtra(Constants.ThirdAuthInfo.USER_NICKNAME,
//							mThirdAuthName);
//					intent.putExtra(Constants.ThirdAuthInfo.USER_AVATAR,
//							mThirdAuthAvatar);
//
//					startActivity(intent);
//					finish();
				}
			} catch (Exception e) {

			}
		}

	};

	private void initEvents() {
		// QQ登录
		mQQLoginBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// 显示等待对话框
				if (mProgressDialog == null) {
					mProgressDialog = new CustomProgressingDialog(
							LoginActivity.this);
				}
				if (!mProgressDialog.isShowing()) {
					mProgressDialog.show();
				}

				Platform qq = ShareSDK.getPlatform(QQ.NAME);
				qq.SSOSetting(false);
				qq.setPlatformActionListener(qqLoginListener);
				if (qq.isValid()) {
					qq.removeAccount();
					ShareSDK.removeCookieOnAuthorize(true);
				}
				qq.showUser(null);
			}
		});

		// 微博登录
		mWeiboLoginBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// 显示等待对话框
				if (mProgressDialog == null) {
					mProgressDialog = new CustomProgressingDialog(
							LoginActivity.this);
				}
				if (!mProgressDialog.isShowing()) {
					mProgressDialog.show();
				}

				Platform weibo = ShareSDK.getPlatform(SinaWeibo.NAME);

				weibo.SSOSetting(false);
				weibo.setPlatformActionListener(weiboLoginListener);
				if (weibo.isValid()) {
					weibo.removeAccount();
					ShareSDK.removeCookieOnAuthorize(true);
				}
				weibo.showUser(null);
			}
		});

		// 微信授权回调
		final Listener<WechatUserInfoRequest.WechatUserWrapper> wechatAuthListener = new Listener<WechatUserInfoRequest.WechatUserWrapper>() {
			@Override
			public void onResponse(WechatUserInfoRequest.WechatUserWrapper response) {
				if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}

				try {
					int isRegistered = response.isRegistered;
					// 已注册
					if (isRegistered == 1) {
						JSONObject userInfoData = response.UserObject;
						LoginUser.getInstance()
								.initFromJSONObject(userInfoData);

						Bundle extras = new Bundle();
						extras.putInt(Constants.IntentKey.ACTIVITY_JUMP_FROM,
								JUMP_FROM_LOGIN);
						MainActivity.startNewActivityAndFinishAllBefore(
								LoginActivity.this,
								MainActivity.class.getName(), extras);
					}
					// 未注册
					if (isRegistered == 0) {
						SharedPreferences.Editor editor = PSGodApplication
								.getAppContext()
								.getSharedPreferences(Constants.SharedPreferencesKey.NAME,
										Context.MODE_PRIVATE).edit();
						editor.putString(Constants.ThirdAuthInfo.THIRD_AUTH_PLATFORM,WEIXINPLAT);
						editor.putString(Constants.ThirdAuthInfo.USER_OPENID, mThirdAuthId);
						editor.putString(Constants.ThirdAuthInfo.USER_AVATAR, mThirdAuthAvatar);
						editor.putString(Constants.ThirdAuthInfo.USER_NICKNAME, mThirdAuthName);
						if (android.os.Build.VERSION.SDK_INT >= 9) {
							editor.apply();
						} else {
							editor.commit();
						}

						JSONObject userInfoData = new JSONObject();
						userInfoData.put("uid", 0l);
						userInfoData.put("nickname", mThirdAuthName);
						userInfoData.put("sex", 0);
						userInfoData.put("phone", "0");
						userInfoData.put("avatar", mThirdAuthAvatar);
						userInfoData.put("fans_count", 0);
						userInfoData.put("fellow_count", 0);
						userInfoData.put("uped_count", 0);
						userInfoData.put("ask_count", 0);
						userInfoData.put("reply_count", 0);
						userInfoData.put("inprogress_count", 0);
						userInfoData.put("collection_count", 0);
						userInfoData.put("is_bound_weixin", 0);
						userInfoData.put("is_bound_qq", 0);
						userInfoData.put("is_bound_weibo", 0);
						userInfoData.put("city", 1);
						userInfoData.put("province", 11);

						LoginUser.getInstance().initFromJSONObject(userInfoData);

						Bundle extras = new Bundle();
						extras.putInt(Constants.IntentKey.ACTIVITY_JUMP_FROM,
								JUMP_FROM_LOGIN);
						MainActivity.startNewActivityAndFinishAllBefore(
								LoginActivity.this, MainActivity.class.getName(),
								extras);
//						Intent intent = new Intent(LoginActivity.this,
//								SetInfoActivity.class);
//
//						intent.putExtra(
//								Constants.ThirdAuthInfo.THIRD_AUTH_PLATFORM,
//								"weixin");
//						intent.putExtra(Constants.ThirdAuthInfo.USER_OPENID,
//								mThirdAuthId);
//						intent.putExtra(Constants.ThirdAuthInfo.USER_AVATAR,
//								mThirdAuthAvatar);
//						intent.putExtra(Constants.ThirdAuthInfo.USER_GENDER,
//								mThirdAuthGender);
//						intent.putExtra(Constants.ThirdAuthInfo.USER_NICKNAME,
//								mThirdAuthName);
//
//						startActivity(intent);
//						finish();
					}
				} catch (Exception e) {
				}
			}
		};

		// 微信登录
		mWechatLoginBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// 显示等待对话框
				if (mProgressDialog == null) {
					mProgressDialog = new CustomProgressingDialog(
							LoginActivity.this);
				}
				if (!mProgressDialog.isShowing()) {
					mProgressDialog.show();
				}

				// 微信授权
				Platform weixin = ShareSDK.getPlatform(Wechat.NAME);
				if (weixin.isValid()) {
					weixin.removeAccount();
					ShareSDK.removeCookieOnAuthorize(true);
				}
				weixin.setPlatformActionListener(new PlatformActionListener() {
					@Override
					public void onError(Platform arg0, int arg1, Throwable arg2) {
						Looper.prepare();
						if (mProgressDialog != null
								&& mProgressDialog.isShowing()) {
							mProgressDialog.dismiss();
						}
						CustomToast.show(LoginActivity.this, "微信登录异常",
								Toast.LENGTH_LONG);
						Looper.loop();
					}

					@Override
					public void onComplete(Platform platform, int action,
							HashMap<String, Object> res) {
						mThirdAuthId = res.get("openid").toString();
						mThirdAuthAvatar = res.get("headimgurl").toString();
						mThirdAuthGender = res.get("sex").toString();
						mThirdAuthName = res.get("nickname").toString();

						// 验证code是否被注册
						if (!TextUtils.isEmpty(mThirdAuthId)) {
							WechatUserInfoRequest.Builder builder = new WechatUserInfoRequest.Builder()
									.setCode(mThirdAuthId)
									.setListener(wechatAuthListener)
									.setErrorListener(errorListener);

							WechatUserInfoRequest request = builder.build();
							request.setTag(TAG);
							RequestQueue requestQueue = PSGodRequestQueue
									.getInstance(LoginActivity.this)
									.getRequestQueue();
							requestQueue.add(request);
						}
					}

					@Override
					public void onCancel(Platform arg0, int arg1) {
						if (mProgressDialog != null
								&& mProgressDialog.isShowing()) {
							mProgressDialog.dismiss();
						}
						CustomToast.show(LoginActivity.this, "取消微信登录",
								Toast.LENGTH_LONG);
					}
				});
				weixin.SSOSetting(true);
				weixin.showUser(null);
			}
		});

		// 点击登录
		mLoginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (validate()) {
					// 显示等待对话框
					if (mProgressDialog == null) {
						mProgressDialog = new CustomProgressingDialog(
								LoginActivity.this);
					}
					if (!mProgressDialog.isShowing()) {
						mProgressDialog.show();
					}

					String phoneNum = mLoginNum.getText().toString().trim();
					String password = mLoginPassword.getText().toString()
							.trim();

					UserLoginRequest.Builder builder = new UserLoginRequest.Builder()
							.setPhoneNum(phoneNum).setPassWord(password)
							.setListener(loginListener)
							.setErrorListener(errorListener);

					UserLoginRequest request = builder.build();
					request.setTag(TAG);
					RequestQueue requestQueue = PSGodRequestQueue.getInstance(
							LoginActivity.this).getRequestQueue();
					requestQueue.add(request);
				}
			}
		});
		// 返回键
		mBackTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}

		});

		mResetButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(LoginActivity.this,
						ResetPasswordInputPhoneActivity.class);
				startActivity(intent);
			}
		});
	}

	private Listener<JSONObject> loginListener = new Listener<JSONObject>() {
		@Override
		public void onResponse(JSONObject response) {
			if (response != null) {
				// 取消等待框
				if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				try {
					if (response.getInt("status") == 1) {
						// 存储服务端返回的用户信息到sp
						LoginUser.getInstance().initFromJSONObject(response);

						Toast.makeText(LoginActivity.this, "登录成功",
								Toast.LENGTH_SHORT).show();

						Bundle extras = new Bundle();
						extras.putInt(Constants.IntentKey.ACTIVITY_JUMP_FROM,
								JUMP_FROM_LOGIN);
						MainActivity.startNewActivityAndFinishAllBefore(
								LoginActivity.this,
								MainActivity.class.getName(), extras);
					} else if (response.getInt("status") == 2) {
						// 密码错误
						Toast.makeText(LoginActivity.this, "密码错误",
								Toast.LENGTH_SHORT).show();
						mLoginPassword.setText("");
						mLoginPassword.requestFocus();
					} else if (response.getInt("status") == 3) {
						// 手机号码未注册
						CustomDialog.Builder mBuilder = new CustomDialog.Builder(
								LoginActivity.this)
								.setMessage("该手机号码未注册，是否返回注册？")
								.setLeftButton("重填手机号码",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface arg0,
													int arg1) {
												mLoginNum.setText("");
												mLoginPassword.setText("");
												mLoginNum.requestFocus();
											}
										})
								.setRightButton("返回注册",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface arg0,
													int arg1) {
												Intent intent = new Intent(
														LoginActivity.this,
														SetInfoActivity.class);
												startActivity(intent);
												finish();
											}
										});
						mBuilder.create().show();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	private PSGodErrorListener errorListener = new PSGodErrorListener(this) {
		@Override
		public void handleError(VolleyError error) {
			// TODO Auto-generated method stub
			if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		}
	};

	private boolean validate() {
		// 手机号校验
		if (Utils.isNull(mLoginNum)) {
			Toast.makeText(LoginActivity.this, "请填写手机号码", Toast.LENGTH_SHORT)
					.show();
			mLoginNum.requestFocus();
			return false;
		}
		String phoneNum = mLoginNum.getText().toString().trim();
		if (!Utils.matchPhoneNum(phoneNum)) {
			Toast.makeText(LoginActivity.this, "电话格式不正确", Toast.LENGTH_SHORT)
					.show();
			mLoginNum.requestFocus();
			return false;
		}

		if (Utils.isNull(mLoginPassword)) {
			Toast.makeText(LoginActivity.this, "请填写登录密码", Toast.LENGTH_SHORT)
					.show();
			mLoginPassword.requestFocus();
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
