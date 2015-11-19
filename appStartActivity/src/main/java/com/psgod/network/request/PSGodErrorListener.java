package com.psgod.network.request;

import android.text.TextUtils;

import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.psgod.Logger;
import com.psgod.Utils;

public abstract class PSGodErrorListener implements ErrorListener {
	private static final String TAG = PSGodErrorListener.class.getSimpleName();
	protected String tag;

	public PSGodErrorListener() {
		tag = TAG;
	}

	public PSGodErrorListener(String tag) {
		this.tag = tag;
	}

	@Override
	public void onErrorResponse(VolleyError error) {
		String msg = error.getMessage();
		if ((msg != null) && (!msg.equals("")) && (msg.startsWith("java.net.UnknownHostException"))) {
			msg = "网络连接不可用，请稍后再试";
		} else {
			msg = TextUtils.isEmpty(msg) ? "系统出错，请稍后再试" : msg ;
		}
//		msg = TextUtils.isEmpty(msg) ? " Empty message" : "网络连接不可用，请稍后再试";
//		msg = TextUtils.isEmpty(msg) ? " Empty message" : msg;
		Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_COLOR, tag, msg);
		Utils.showDebugToast(msg);

		// 错误提示
		// Utils.showDebugToast("请求失败，请稍后再试");
		handleError(error);
	}

	public abstract void handleError(VolleyError error);
}
