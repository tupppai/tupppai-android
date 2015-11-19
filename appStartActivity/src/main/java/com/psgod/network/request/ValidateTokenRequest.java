package com.psgod.network.request;

/**
 * 用户相关照片列表(我的作品、我的求P、我的进行中、我的收藏)
 * @author brandwang
 */

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;

public class ValidateTokenRequest extends BaseRequest<Void> {
	private final static String TAG = ValidateTokenRequest.class
			.getSimpleName();

	public ValidateTokenRequest(int method, String url,
			Listener<Void> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected Void doParseNetworkResponse(JSONObject response)
			throws JSONException {
		return null;
	}

	public static class Builder implements IPostRequestBuilder {
		private String token;
		private Listener<Void> listener;
		private ErrorListener errorListener;

		public Builder setToken(String token) {
			this.token = token;
			return this;
		}

		public Builder setListener(Listener<Void> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.errorListener = errorListener;
			return this;
		}

		public ValidateTokenRequest build() {
			String url = createUrl();
			ValidateTokenRequest request = new ValidateTokenRequest(METHOD,
					url, listener, errorListener) {
				@Override
				public Map<String, String> getParams() {
					return createParameters();
				}
			};
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL)
					.append("account/checkTokenValidity");
			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();
			params.put("token", token);
			return params;
		}
	}
}
