package com.psgod.network.request;

/**
 * 微博登录请求接口
 * @author brandwang
 */
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;
import com.psgod.network.request.WeiboLoginRequest.WeiboLoginWrapper;

public class WeiboLoginRequest extends BaseRequest<WeiboLoginWrapper> {
	private static final String TAG = UploadReplyRequest.class.getSimpleName();

	public WeiboLoginRequest(int method, String url,
			Listener<WeiboLoginWrapper> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected WeiboLoginWrapper doParseNetworkResponse(JSONObject response)
			throws JSONException {
		JSONObject data = response.getJSONObject("data");

		WeiboLoginWrapper weiboLoginWrapper = new WeiboLoginWrapper();
		weiboLoginWrapper.isRegistered = data.getInt("is_register");
		if (weiboLoginWrapper.isRegistered == 1) {
			weiboLoginWrapper.UserObject = data.getJSONObject("user_obj");
		}
		return weiboLoginWrapper;
	}

	public static class Builder implements IPostRequestBuilder {
		private String openid;
		private Listener<WeiboLoginWrapper> listener;
		private ErrorListener errorListener;

		public Builder setCode(String id) {
			this.openid = id;
			return this;
		}

		public Builder setListener(Listener<WeiboLoginWrapper> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.errorListener = errorListener;
			return this;
		}

		public WeiboLoginRequest build() {
			String url = createUrl();
			WeiboLoginRequest request = new WeiboLoginRequest(METHOD, url,
					listener, errorListener) {
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
					.append("auth/weibo");
			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();
			params.put("openid", openid);

			return params;
		}
	}

	public static class WeiboLoginWrapper {
		public int isRegistered;
		public JSONObject UserObject;
	}
}
