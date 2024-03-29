package com.psgod.network.request;

/**
 * qq登录请求接口
 * @author brandwang
 */

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;
import com.psgod.network.request.QQLoginRequest.QQLoginWrapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class QQLoginRequest extends BaseRequest<QQLoginWrapper> {
	private static final String TAG = QQLoginRequest.class.getSimpleName();

	public QQLoginRequest(int method, String url,
			Listener<QQLoginWrapper> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected QQLoginWrapper doParseNetworkResponse(JSONObject response)
			throws JSONException {
		JSONObject data = response.getJSONObject("data");

		QQLoginWrapper qqLoginWrapper = new QQLoginWrapper();
		qqLoginWrapper.isRegistered = data.getInt("is_register");
		if (qqLoginWrapper.isRegistered == 1) {
			qqLoginWrapper.UserObject = data.getJSONObject("user_obj");
		}
		return qqLoginWrapper;
	}

	public static class Builder implements IPostRequestBuilder {
		private String openid;
		private String nickname;
		private String avatar;
		private Listener<QQLoginWrapper> listener;
		private ErrorListener errorListener;

		public Builder setNickname(String nickname) {
			this.nickname = nickname;
			return this;
		}

		public Builder setAvatar(String avatar) {
			this.avatar = avatar;
			return this;
		}

		public Builder setCode(String id) {
			this.openid = id;
			return this;
		}

		public Builder setListener(Listener<QQLoginWrapper> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.errorListener = errorListener;
			return this;
		}

		public QQLoginRequest build() {
			String url = createUrl();
			QQLoginRequest request = new QQLoginRequest(METHOD, url, listener,
					errorListener) {
				@Override
				public Map<String, String> getParams() {
					return getPackParams(createParameters());
				}
			};
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL)
					.append("auth/qq");
			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();
			params.put("openid", openid);
			params.put("avatar",avatar);
			params.put("nickname",nickname);
			return params;
		}
	}

	public static class QQLoginWrapper {
		public int isRegistered;
		public JSONObject UserObject;
	}
}
