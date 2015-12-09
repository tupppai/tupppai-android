package com.psgod.network.request;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ResetPasswordRequest extends BaseRequest<Boolean> {

	private static final String TAG = ResetPasswordRequest.class
			.getSimpleName();

	private ResetPasswordRequest(int method, String url,
			Listener<Boolean> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected Boolean doParseNetworkResponse(JSONObject response)
			throws JSONException {
		JSONObject data = response.getJSONObject("data");
		Boolean status = data.getBoolean("status");
		return status;
	}

	public static class Builder implements IPostRequestBuilder {

		private String mPhoneNumber;
		private String mNewPassword;
		private String mVerifyCode;
		private Listener<Boolean> listener;
		private ErrorListener errorListener;

		public Builder setPhoneNumber(String mPhoneNumber) {
			this.mPhoneNumber = mPhoneNumber;
			return this;
		}

		public Builder setNewPassword(String mNewPassword) {
			this.mNewPassword = mNewPassword;
			return this;
		}

		public Builder setVerifyCode(String mVerifyCode) {
			this.mVerifyCode = mVerifyCode;
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

		public ResetPasswordRequest build() {
			String url = createUrl();
			ResetPasswordRequest request = new ResetPasswordRequest(METHOD,
					url, listener, errorListener) {
				@Override
				public Map<String, String> getParams() {
					return createParameters();
				}
			};
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url + createParameters());
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL)
					.append("account/resetPassword");
			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();
			params.put("phone", mPhoneNumber);
			params.put("code", mVerifyCode);
			params.put("new_pwd", mNewPassword);
			return params;
		}

	}

}
