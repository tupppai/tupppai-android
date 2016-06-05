package com.pires.wesee.network.request;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.pires.wesee.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterGetVerifyCodeRequest extends BaseRequest<Long> {
	private static final String TAG = RegisterGetVerifyCodeRequest.class
			.getSimpleName();

	public RegisterGetVerifyCodeRequest(int method, String url,
			Listener<Long> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected Long doParseNetworkResponse(JSONObject response)
			throws JSONException {
		JSONObject data = response.getJSONObject("data");
		return data.getLong("uid");
	}

	public static class Builder implements IPostRequestBuilder {
		private String phoneNumber;
		private String password;
		private String avatarId;
		private String nickname;
		private String gender;
		private String cityId;
		private Listener<Long> listener;
		private ErrorListener errorListener;

		public Builder setPhoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
			return this;
		}

		public Builder setPassword(String password) {
			this.password = password;
			return this;
		}

		public Builder setAvatarId(String avatarId) {
			this.avatarId = avatarId;
			return this;
		}

		public Builder setNickname(String nickname) {
			this.nickname = nickname;
			return this;
		}

		public Builder setGender(String gender) {
			this.gender = gender;
			return this;
		}

		public Builder setCityId(String cityId) {
			this.cityId = cityId;
			return this;
		}

		public Builder setListener(Listener<Long> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.errorListener = errorListener;
			return this;
		}

		public RegisterGetVerifyCodeRequest build() {
			String url = createUrl();
			RegisterGetVerifyCodeRequest request = new RegisterGetVerifyCodeRequest(
					METHOD, url, listener, errorListener) {
				@Override
				public Map<String, String> getParams() {
					return getPackParams(createParameters());
				}
			};
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(PSGOD_BASE_URL)
					.append("user/save");
			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();
			params.put("type", "mobile");
			params.put("mobile", phoneNumber);
			params.put("password", password);
			params.put("avatar", avatarId);
			params.put("sex", gender);
			params.put("nickname", nickname);
			params.put("city", cityId);
			return params;
		}
	}
}
