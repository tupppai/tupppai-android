package com.pires.wesee.network.request;

/**
 * 设置接收消息推送 请求
 * @author brandwang
 */

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.pires.wesee.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class ActionSetNotificationPush extends BaseRequest<Boolean> {
	private static final String TAG = ActionSetNotificationPush.class
			.getSimpleName();

	public ActionSetNotificationPush(int method, String url,
			Listener<Boolean> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected Boolean doParseNetworkResponse(JSONObject reponse)
			throws UnsupportedEncodingException, JSONException {
		Boolean data = reponse.getBoolean("data");
		return data;
	}

	public static class Builder implements IPostRequestBuilder {
		// 推送设置 0 关闭 1 开启
		public static final int TYPE_CLOSE = 0;
		public static final int TYPE_OPEN = 1;

		private int value;
		private String type;
		private Listener<Boolean> listener;
		private ErrorListener errorListener;

		public Builder setValue(int vl) {
			this.value = vl;
			return this;
		}

		public Builder setType(String tp) {
			this.type = tp;
			return this;
		}

		public Builder setListener(Listener<Boolean> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.errorListener = errorListener;
			return this;
		}

		public ActionSetNotificationPush build() {
			String url = createUrl();
			ActionSetNotificationPush request = new ActionSetNotificationPush(
					METHOD, url, listener, errorListener) {
				@Override
				public Map<String, String> getParams() {
					return getPackParams(createParameters());
				}
			};
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url + createParameters());
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(PSGOD_BASE_URL);
			sb.append("profile/set_push_settings");

			String url = sb.toString();
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();
			params.put("type", type);
			params.put("value", Integer.toString(value));

			return params;
		}
	}
}
