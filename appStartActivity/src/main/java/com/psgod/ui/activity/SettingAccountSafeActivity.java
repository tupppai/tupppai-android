package com.psgod.ui.activity;

/**
 * 账号安全设置页面
 */
import java.util.HashMap;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.psgod.Constants;
import com.psgod.PSGodApplication;
import com.psgod.R;
import com.psgod.WeakReferenceHandler;
import com.psgod.model.LoginUser;
import com.psgod.model.LoginUser.SPKey;
import com.psgod.network.request.ActionBindAccountRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.ui.widget.dialog.CustomDialog;

public class SettingAccountSafeActivity extends PSGodBaseActivity implements
		Handler.Callback {
	private static final String TAG = SettingAccountSafeActivity.class
			.getSimpleName();

	public static final int BIND_WEIBO_SUCCESS = 100;
	public static final int BIND_WEIBO_ERROR = 101;
	public static final int BIND_WECHAT_SUCCESS = 110;
	public static final int BIND_WECHAT_ERROR = 111;
	public static final int BIND_QQ_SUCCESS = 120;
	public static final int BIND_QQ_ERROR = 121;

	private Context mContext;

	private TextView mBindingPhoneTv;
	private TextView mEditPasswdTv;
	private ToggleButton mBindWeiboBtn;
	private ToggleButton mBindWechatBtn;
	private ToggleButton mBindQQBtn;

	private boolean isBoundWeiBo = false;
	private boolean isBoundWechat = false;
	private boolean isBoundQQ = false;

	private Platform qq = null;

	private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);

	private LoginUser user = LoginUser.getInstance();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_account_safe);
		mContext = this;

		initViews();
		initListeners();
	}

	private void initViews() {
		mBindingPhoneTv = (TextView) findViewById(R.id.account_binding_phone_num);
		mEditPasswdTv = (TextView) findViewById(R.id.activity_edit_passwd_btn);
		mBindQQBtn = (ToggleButton) findViewById(R.id.account_binding_qq_toggle_btn);
		mBindWechatBtn = (ToggleButton) findViewById(R.id.account_binding_wechat_toggle_btn);
		mBindWeiboBtn = (ToggleButton) findViewById(R.id.account_binding_weibo_toggle_btn);

		// 设置两个toggleBtn的状态
		isBoundWeiBo = user.isBoundWeibo();
		isBoundWechat = user.isBoundWechat();
		isBoundQQ = user.isBoundQQ();

		mBindingPhoneTv.setText("手机号" + user.getPhoneNum());
		mBindWeiboBtn.setChecked(isBoundWeiBo);
		mBindWechatBtn.setChecked(isBoundWechat);
		mBindQQBtn.setChecked(isBoundQQ);
	}

	private void initListeners() {
		// 点击修改密码弹出修改密码选项
		mEditPasswdTv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SettingAccountSafeActivity.this,
						SettingPasswordActivity.class);
				startActivity(intent);
			}
		});

		// bind qq click
		mBindQQBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isBoundQQ) {
					// choose to bind qq
					qq = ShareSDK.getPlatform(QZone.NAME);
					if (qq.isValid()) {
						qq.removeAccount();
						ShareSDK.removeCookieOnAuthorize(true);
					}

					qq.SSOSetting(false);
					qq.setPlatformActionListener(qqLoginListener);
					qq.showUser(null);
				} else {
					// choose to unbind qq
					CustomDialog.Builder mBuilder = new CustomDialog.Builder(
							mContext)
							.setMessage("确定取消绑定该QQ账号?")
							.setLeftButton("取消",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											mHandler.sendEmptyMessage(BIND_QQ_SUCCESS);
										}
									})
							.setRightButton("确定",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											ActionBindAccountRequest.Builder builder = new ActionBindAccountRequest.Builder()
													.setType("qq")
													.setIsBind(0)
													.setListener(
															unbindQQListener)
													.setErrorListener(
															qqUnbindErrorListener);
											ActionBindAccountRequest request = builder
													.build();
											request.setTag(TAG);
											RequestQueue reqeustQueue = PSGodRequestQueue
													.getInstance(
															getApplicationContext())
													.getRequestQueue();
											reqeustQueue.add(request);
											reqeustQueue.start();
										}
									});
					mBuilder.create().show();
				}
			}
		});

		// 绑定微博点击事件
		mBindWeiboBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!isBoundWeiBo) {
					// 选择绑定
					Platform weibo = ShareSDK.getPlatform(SinaWeibo.NAME);
					if (weibo.isValid()) {
						weibo.removeAccount();
						ShareSDK.removeCookieOnAuthorize(true);
					}

					weibo.SSOSetting(false);
					weibo.setPlatformActionListener(weiboLoginListener);
					weibo.showUser(null);
				} else {
					// 取消绑定
					CustomDialog.Builder mBuilder = new CustomDialog.Builder(
							mContext)
							.setMessage("确认取消绑定该微博账号？")
							.setLeftButton("取消",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											mHandler.sendEmptyMessage(BIND_WEIBO_SUCCESS);
										}
									})
							.setRightButton("确定",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											ActionBindAccountRequest.Builder builder = new ActionBindAccountRequest.Builder()
													.setType("weibo")
													.setIsBind(0)
													.setListener(
															unbindWeiboListener)
													.setErrorListener(
															wechatUnbindErrorListener);
											ActionBindAccountRequest request = builder
													.build();
											request.setTag(TAG);
											RequestQueue reqeustQueue = PSGodRequestQueue
													.getInstance(
															getApplicationContext())
													.getRequestQueue();
											reqeustQueue.add(request);
											reqeustQueue.start();
										}
									});
					mBuilder.create().show();
				}
			}
		});

		// 绑定微信点击事件
		mBindWechatBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!isBoundWechat) {
					// 选择绑定微信
					Platform wechat = ShareSDK.getPlatform(Wechat.NAME);
					if (wechat.isValid()) {
						wechat.removeAccount();
						ShareSDK.removeCookieOnAuthorize(true);
					}
					wechat.SSOSetting(true);
					wechat.setPlatformActionListener(wechatBindListener);
					wechat.showUser(null);
				} else {
					// 取消绑定微信
					CustomDialog.Builder mBuilder = new CustomDialog.Builder(
							mContext)
							.setMessage("确认取消绑定该微信账号？")
							.setLeftButton("取消",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											mHandler.sendEmptyMessage(BIND_WECHAT_SUCCESS);
										}
									})
							.setRightButton("确定",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											ActionBindAccountRequest.Builder builder = new ActionBindAccountRequest.Builder()
													.setType("weixin")
													.setIsBind(0)
													.setListener(
															unbindWechatListener)
													.setErrorListener(
															wechatUnbindErrorListener);
											ActionBindAccountRequest request = builder
													.build();
											request.setTag(TAG);
											RequestQueue reqeustQueue = PSGodRequestQueue
													.getInstance(
															getApplicationContext())
													.getRequestQueue();
											reqeustQueue.add(request);
											reqeustQueue.start();
										}
									});
					mBuilder.create().show();
				}
			}
		});
	}

	// 微博登录事件监听
	private PlatformActionListener weiboLoginListener = new PlatformActionListener() {
		@Override
		public void onError(Platform arg0, int arg1, Throwable arg2) {
			mHandler.sendEmptyMessage(BIND_WEIBO_ERROR);
		}

		@Override
		public void onComplete(Platform arg0, int arg1,
				HashMap<String, Object> res) {
			String openId = res.get("id").toString();

			if (!TextUtils.isEmpty(openId)) {
				ActionBindAccountRequest.Builder builder = new ActionBindAccountRequest.Builder()
						.setIsBind(1).setOpenId(openId).setType("weibo")
						.setListener(weiboBindListener)
						.setErrorListener(weiboBindErrorListener);

				ActionBindAccountRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						mContext).getRequestQueue();
				requestQueue.add(request);
			}
		}

		@Override
		public void onCancel(Platform arg0, int arg1) {
			mHandler.sendEmptyMessage(BIND_WEIBO_ERROR);
		}
	};

	// 绑定微博回调
	private Listener<Boolean> weiboBindListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			if (response) {
				Toast.makeText(mContext, "绑定微博成功", Toast.LENGTH_SHORT).show();
				LoginUser user = LoginUser.getInstance();
				user.setBoundWeibo(true);

				// 修改SP中的状态
				SharedPreferences.Editor editor = PSGodApplication
						.getAppContext()
						.getSharedPreferences(
								Constants.SharedPreferencesKey.NAME,
								Context.MODE_PRIVATE).edit();
				editor.putBoolean(SPKey.IS_BOUND_WEIBO, true);
				editor.commit();

				isBoundWeiBo = true;
			} else {
				mHandler.sendEmptyMessage(BIND_WEIBO_ERROR);
			}
		}
	};

	// 微博绑定失败listener
	private PSGodErrorListener weiboBindErrorListener = new PSGodErrorListener() {
		@Override
		public void handleError(VolleyError error) {
			mHandler.sendEmptyMessage(BIND_WEIBO_ERROR);
		}
	};

	// 取消绑定微博
	private Listener<Boolean> unbindWeiboListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			if (response) {
				Toast.makeText(mContext, "成功取消绑定微博账号", Toast.LENGTH_SHORT)
						.show();
				LoginUser user = LoginUser.getInstance();
				user.setBoundWeibo(false);

				// 修改SP中的状态
				SharedPreferences.Editor editor = PSGodApplication
						.getAppContext()
						.getSharedPreferences(
								Constants.SharedPreferencesKey.NAME,
								Context.MODE_PRIVATE).edit();
				editor.putBoolean(SPKey.IS_BOUND_WEIBO, false);
				editor.commit();

				isBoundWeiBo = false;
			} else {
				mHandler.sendEmptyMessage(BIND_WEIBO_SUCCESS);
			}
		}
	};

	// 微信授权监听
	private PlatformActionListener wechatBindListener = new PlatformActionListener() {
		@Override
		public void onError(Platform arg0, int arg1, Throwable arg2) {
			mHandler.sendEmptyMessage(BIND_WECHAT_ERROR);
		}

		@Override
		public void onComplete(Platform arg0, int arg1,
				HashMap<String, Object> res) {
			String wechatOpenId = res.get("openid").toString();

			if (!TextUtils.isEmpty(wechatOpenId)) {
				ActionBindAccountRequest.Builder builder = new ActionBindAccountRequest.Builder()
						.setIsBind(1).setOpenId(wechatOpenId).setType("weixin")
						.setListener(wechatAuthListener)
						.setErrorListener(wechatBindErrorListener);

				ActionBindAccountRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						mContext).getRequestQueue();
				requestQueue.add(request);
			}
		}

		@Override
		public void onCancel(Platform arg0, int arg1) {
			mHandler.sendEmptyMessage(BIND_WECHAT_ERROR);
		}
	};

	// 绑定微信回调
	private Listener<Boolean> wechatAuthListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			if (response) {
				Toast.makeText(mContext, "绑定微信成功", Toast.LENGTH_SHORT).show();
				LoginUser user = LoginUser.getInstance();
				user.setBoundWechat(true);
				// 修改SP中的状态
				SharedPreferences.Editor editor = PSGodApplication
						.getAppContext()
						.getSharedPreferences(
								Constants.SharedPreferencesKey.NAME,
								Context.MODE_PRIVATE).edit();
				editor.putBoolean(SPKey.IS_BOUND_WECHAT, true);
				editor.commit();

				isBoundWechat = true;
			} else {
				mHandler.sendEmptyMessage(BIND_WECHAT_ERROR);
			}
		}
	};

	// 微信绑定失败listener
	private PSGodErrorListener wechatBindErrorListener = new PSGodErrorListener() {
		@Override
		public void handleError(VolleyError error) {
			mHandler.sendEmptyMessage(BIND_WECHAT_ERROR);
		}
	};

	// 取消绑定微信
	private Listener<Boolean> unbindWechatListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			if (response) {
				Toast.makeText(mContext, "成功取消绑定微信账号", Toast.LENGTH_SHORT)
						.show();
				LoginUser user = LoginUser.getInstance();
				user.setBoundWechat(false);

				// 修改SP中的状态
				SharedPreferences.Editor editor = PSGodApplication
						.getAppContext()
						.getSharedPreferences(
								Constants.SharedPreferencesKey.NAME,
								Context.MODE_PRIVATE).edit();
				editor.putBoolean(SPKey.IS_BOUND_WECHAT, false);
				editor.commit();

				isBoundWechat = false;
			} else {
				mHandler.sendEmptyMessage(BIND_WECHAT_SUCCESS);
			}
		}
	};

	// 取消绑定微信失败
	private PSGodErrorListener wechatUnbindErrorListener = new PSGodErrorListener() {
		@Override
		public void handleError(VolleyError error) {
			mHandler.sendEmptyMessage(BIND_WECHAT_SUCCESS);
		}
	};

	// qq登陆事件监听
	private PlatformActionListener qqLoginListener = new PlatformActionListener() {
		@Override
		public void onError(Platform arg0, int arg1, Throwable arg2) {
			mHandler.sendEmptyMessage(BIND_QQ_ERROR);
		}

		@Override
		public void onComplete(Platform arg0, int arg1,
				HashMap<String, Object> res) {
			String openId = qq.getDb().getUserId();

			if (!TextUtils.isEmpty(openId)) {
				ActionBindAccountRequest.Builder builder = new ActionBindAccountRequest.Builder()
						.setIsBind(1).setOpenId(openId).setType("qq")
						.setListener(qqBindListener)
						.setErrorListener(qqBindErrorListener);

				ActionBindAccountRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						mContext).getRequestQueue();
				requestQueue.add(request);
			}
		}

		@Override
		public void onCancel(Platform arg0, int arg1) {
			mHandler.sendEmptyMessage(BIND_QQ_ERROR);
		}
	};

	// 绑定qq回调
	private Listener<Boolean> qqBindListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			if (response) {
				Toast.makeText(mContext, "绑定QQ成功", Toast.LENGTH_SHORT).show();
				LoginUser user = LoginUser.getInstance();
				user.setBoundQQ(true);

				// 修改SP中的状态
				SharedPreferences.Editor editor = PSGodApplication
						.getAppContext()
						.getSharedPreferences(
								Constants.SharedPreferencesKey.NAME,
								Context.MODE_PRIVATE).edit();
				editor.putBoolean(SPKey.IS_BOUND_QQ, true);
				editor.commit();

				isBoundQQ = true;
			} else {
				mHandler.sendEmptyMessage(BIND_QQ_ERROR);
			}
		}
	};

	// QQ绑定失败listener
	private PSGodErrorListener qqBindErrorListener = new PSGodErrorListener() {
		@Override
		public void handleError(VolleyError error) {
			mHandler.sendEmptyMessage(BIND_QQ_ERROR);
		}
	};

	// 取消绑定qq
	private Listener<Boolean> unbindQQListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			if (response) {
				Toast.makeText(mContext, "成功取消绑定QQ账号", Toast.LENGTH_SHORT)
						.show();
				LoginUser user = LoginUser.getInstance();
				user.setBoundQQ(false);

				// 修改SP中的状态
				SharedPreferences.Editor editor = PSGodApplication
						.getAppContext()
						.getSharedPreferences(
								Constants.SharedPreferencesKey.NAME,
								Context.MODE_PRIVATE).edit();
				editor.putBoolean(SPKey.IS_BOUND_QQ, false);
				editor.commit();

				isBoundQQ = false;
			} else {
				mHandler.sendEmptyMessage(BIND_QQ_SUCCESS);
			}
		}
	};

	// 取消绑定qq失败
	private PSGodErrorListener qqUnbindErrorListener = new PSGodErrorListener() {
		@Override
		public void handleError(VolleyError error) {
			mHandler.sendEmptyMessage(BIND_QQ_SUCCESS);
		}
	};

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case BIND_WEIBO_ERROR:
			mBindWeiboBtn.setChecked(false);
			break;

		case BIND_WEIBO_SUCCESS:
			mBindWeiboBtn.setChecked(true);
			break;

		case BIND_WECHAT_ERROR:
			mBindWechatBtn.setChecked(false);
			break;

		case BIND_WECHAT_SUCCESS:
			mBindWechatBtn.setChecked(true);
			break;

		case BIND_QQ_ERROR:
			mBindQQBtn.setChecked(false);
			break;

		case BIND_QQ_SUCCESS:
			mBindQQBtn.setChecked(true);
			break;

		default:
			break;
		}
		return false;
	}
}
