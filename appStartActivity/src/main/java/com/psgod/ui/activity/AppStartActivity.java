package com.psgod.ui.activity;

/**
 * 启动页面 
 * @author brandwang
 */
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.psgod.Constants;
import com.psgod.Logger;
import com.psgod.PSGodApplication;
import com.psgod.PSGodToast;
import com.psgod.R;
import com.psgod.ThreadManager;
import com.psgod.model.LoginUser;
import com.psgod.network.NetworkUtil;
import com.psgod.network.request.BaseRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.PhotoListRequest;
import com.psgod.network.request.Request;

public class AppStartActivity extends PSGodBaseActivity implements
		Handler.Callback {
	private static final String TAG = AppStartActivity.class.getSimpleName();

	private static final int SHOW_TIME_MIN = 2000; // 最短展示时间，thread少于该时间应延时跳转

	private static final int TOKEN_RESULT_OK = 0;
	private static final int TOKEN_RESULT_EMPTY = 1;
	private static final int TOKEN_RESULT_EXPIRED = 2;
	private static final int TOKEN_RESULT_NETWORK_ERROR = 3;
	private static final int TOKEN_RESULT_EXCEPTION = 4;

	private boolean mNetworkError = false;
	private boolean isFirstRun = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_start);

		SharedPreferences sp = PSGodApplication.getAppContext()
				.getSharedPreferences(Constants.SharedPreferencesKey.NAME,
						Context.MODE_PRIVATE);
		isFirstRun = sp.getBoolean(Constants.SharedPreferencesKey.IS_FIRST_RUN,
				true);
		SharedPreferences.Editor editor = PSGodApplication
				.getAppContext()
				.getSharedPreferences(Constants.SharedPreferencesKey.NAME,
						Context.MODE_PRIVATE).edit();

		// 判断是否为第一次启动
		if (isFirstRun) {
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"isFirstRun:" + true);
			editor.putBoolean(Constants.SharedPreferencesKey.IS_FIRST_RUN,
					false);
			if (android.os.Build.VERSION.SDK_INT >= 9) {
				editor.apply();
			} else {
				editor.commit();
			}
			Intent intent = getIntent();
			intent.setClass(AppStartActivity.this, WelcomeScrollActivity.class);
			startActivity(intent);
			finish();
		} else {
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"isFirstRun:" + false);
			ThreadManager.executeOnNetWorkThread(new Thread() {
				@Override
				public void run() {
					long beginTime = System.currentTimeMillis();
					int tokenResult = validateToken();

					// 保证欢迎页展示时间为SHOW_TIME_MIN
					long sleepTime = SHOW_TIME_MIN - System.currentTimeMillis()
							+ beginTime;
					if (sleepTime > 0) {
						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					handleTokenResult(tokenResult);
				}
			});
		}

	}

	private Listener<Void> listener = new Listener<Void>() {
		@Override
		public void onResponse(final Void params) {

		}
	};

	private PSGodErrorListener errorListener = new PSGodErrorListener(
			PhotoListRequest.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {

		}
	};

	// 检测token状况
	private int validateToken() {
		String localToken = Request.getCookie();
		if (TextUtils.isEmpty(localToken)) {
			// 检测本地token是否存在或者过期
			return TOKEN_RESULT_EMPTY;
		} else {
			// 向服务器判断Token是否过期
			try {
				// TODO 改成Request
				final String checkTokenUrl = BaseRequest.PSGOD_BASE_URL
						+ "account/checkTokenValidity";
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("token", localToken);
				JSONObject result = Request.postRequest(checkTokenUrl, params);
				int ret;
				if (result == null || (result.getInt("ret") == 0)) {
					return TOKEN_RESULT_NETWORK_ERROR;
				} else if ((ret = result.getInt("ret")) == 2) {
					return TOKEN_RESULT_EXPIRED;
				} else {
					return TOKEN_RESULT_OK;
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return TOKEN_RESULT_EXCEPTION;
			}
		}
	}

	// 根据检测token的返回结果做出响应
	private void handleTokenResult(final int result) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				switch (result) {
				case TOKEN_RESULT_OK:
					startActivity(MainActivity.class);
					break;
				case TOKEN_RESULT_EMPTY:
					startActivity(WelcomeActivity.class);
					break;
				case TOKEN_RESULT_EXPIRED:
					Toast.makeText(AppStartActivity.this, "身份过期或失效，请重新登录",
							Toast.LENGTH_SHORT).show();
					startActivity(WelcomeActivity.class);
					break;
				case TOKEN_RESULT_NETWORK_ERROR:
					if (!mNetworkError) {
						mNetworkError = true;
						// showToast(new PSGodToast("当前网络不可用，请检查你的网络设置",
						// PSGodToast.DURATION_FOREVER,
						// PSGodToast.PRIORITY_HIGHEST));
						// showToast(new PSGodToast("当前网络不可用，请检查你的网络设置"));
						// 读取本地数据，直接跳到首页
						if (LoginUser.getInstance().canReadData()) {
							startActivity(MainActivity.class);
						} else {
							// TODO 没有本地数据
							startActivity(MainActivity.class);
						}
					}
					break;
				case TOKEN_RESULT_EXCEPTION:
				default:
					Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_DEV,
							TAG, "handleTokenResult(): TOKEN_RESULT_EXCEPTION");
					break;
				}
			}
		});
	}

	@Override
	public void onNetworkStateChanged(int networkType) {
		super.onNetworkStateChanged(networkType);
		if (networkType == NetworkUtil.NetworkType.NONE) {
			mNetworkError = true;
			// showToast(new PSGodToast("当前网络不可用，请检查你的网络设置",
			// PSGodToast.DURATION_FOREVER, PSGodToast.PRIORITY_HIGHEST));
			showToast(new PSGodToast("当前网络不可用，请检查你的网络设置"));
		} else {
			// TODO 这种情况有可能没有满足SHOW_TIME_MIN，先不管了
			mNetworkError = false;
			int tokenResult = validateToken();
			handleTokenResult(tokenResult);
		}
	}

	private void startActivity(Class activity) {
		Intent intent = new Intent(this, activity);
		startActivity(intent);
		finish();
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
