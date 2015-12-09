package com.psgod.network.request;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;
import com.psgod.network.request.WechatUserInfoRequest.WechatUserWrapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class WechatUserInfoRequest extends BaseRequest<WechatUserWrapper> {
	private static final String TAG = WechatUserInfoRequest.class
			.getSimpleName();

	public WechatUserInfoRequest(int method, String url,
			Listener<WechatUserWrapper> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected WechatUserWrapper doParseNetworkResponse(JSONObject response)
			throws JSONException {
		JSONObject data = response.getJSONObject("data");

		WechatUserWrapper wechatUserWrapper = new WechatUserWrapper();
		wechatUserWrapper.isRegistered = data.getInt("is_register");
		if (wechatUserWrapper.isRegistered == 1) {
			wechatUserWrapper.UserObject = data.getJSONObject("user_obj");
		}
		return wechatUserWrapper;
	}

	public static class Builder implements IPostRequestBuilder {
		private String mCode;
		private Listener<WechatUserWrapper> listener;
		private ErrorListener errorListener;

		public Builder setCode(String code) {
			this.mCode = code;
			return this;
		}

		public Builder setListener(Listener<WechatUserWrapper> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.errorListener = errorListener;
			return this;
		}

		public WechatUserInfoRequest build() {
			String url = createUrl();
			WechatUserInfoRequest request = new WechatUserInfoRequest(METHOD,
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
					.append("auth/weixin");
			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();
			params.put("openid", mCode);

			return params;
		}
	}

	public static class WechatUserWrapper {
		public int isRegistered;
		public JSONObject UserObject;
	}
}
