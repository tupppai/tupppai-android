package com.psgod.network.request;

import android.text.TextUtils;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;
import com.psgod.model.RegisterData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterRequest extends BaseRequest<JSONObject> {
	private static final String TAG = RegisterRequest.class.getSimpleName();

	public RegisterRequest(int method, String url,
			Listener<JSONObject> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected JSONObject doParseNetworkResponse(JSONObject response)
			throws JSONException {
		JSONObject data = response.getJSONObject("data");
		return data;
	}

	public static class Builder implements IPostRequestBuilder {
		private RegisterData registerData;
		private Listener<JSONObject> listener;
		private ErrorListener errorListener;

		public Builder setRegisterData(RegisterData registerData) {
			this.registerData = registerData;
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

		public RegisterRequest build() {
			String url = createUrl();
			RegisterRequest request = new RegisterRequest(METHOD, url,
					listener, errorListener) {
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
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL)
					.append("account/register");
			String url = sb.toString();
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();

			params.put("type", registerData.getThirdAuthType());
			params.put("mobile", registerData.getPhoneNumber());
			params.put("password", registerData.getPassword());
			if (!TextUtils.isEmpty(registerData.getNickname())) {
				params.put("nickname", registerData.getNickname());
			}
			params.put("code", registerData.getVerifyCode());
//			params.put("avatar", registerData.getAvatar());
//			params.put("sex", String.valueOf(registerData.getGender()));
//			params.put("province", String.valueOf(registerData.getProvinceId()));
//			params.put("city", String.valueOf(registerData.getCityId()));

			if (!TextUtils.isEmpty(registerData.getOpenId())) {
				params.put("openid", registerData.getOpenId());
			}

			if (!TextUtils.isEmpty(registerData.getThirdAvatar())) {
				params.put("avatar_url", registerData.getThirdAvatar());
			}

			return params;
		}
	}
}
