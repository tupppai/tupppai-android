package com.psgod.network.request;

/**
 * 用户登录接口
 */

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class UserLoginRequest extends BaseRequest<JSONObject> {
	private static final String TAG = UserLoginRequest.class.getSimpleName();

	public UserLoginRequest(int method, String url,
			Listener<JSONObject> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected JSONObject doParseNetworkResponse(JSONObject reponse)
			throws UnsupportedEncodingException, JSONException {
		JSONObject data = reponse.getJSONObject("data");
		return data;
	}

	public static class Builder implements IPostRequestBuilder {
		private String mPhoneNum;
		private String mPassWord;
		private Listener<JSONObject> listener;
		private ErrorListener errorListener;

		public Builder setPhoneNum(String num) {
			this.mPhoneNum = num;
			return this;
		}

		public Builder setPassWord(String pwd) {
			this.mPassWord = pwd;
			return this;
		}

		public Builder setListener(Listener<JSONObject> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.errorListener = errorListener;
			return this;
		}

		public UserLoginRequest build() {
			String url = createUrl();
			UserLoginRequest request = new UserLoginRequest(METHOD, url,
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
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);
			sb.append("account/login");

			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();
			params.put("phone", mPhoneNum);
			params.put("password", mPassWord);

			return params;
		}

	}
}
