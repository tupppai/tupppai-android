package com.psgod.network.request;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;

public class RegisterCheckPhoneNumRequest extends BaseRequest<Boolean> {
	private static final String TAG = RegisterCheckPhoneNumRequest.class
			.getSimpleName();

	public RegisterCheckPhoneNumRequest(int method, String url,
			Listener<Boolean> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected Boolean doParseNetworkResponse(JSONObject response)
			throws JSONException {
		JSONObject data = response.getJSONObject("data");
		Boolean is_register = data.getBoolean("has_registered");
		// is_register=true为已注册
		return is_register;
	}

	public static class Builder implements IGetRequestBuilder {
		private String phoneNumber;
		private Listener<Boolean> listener;
		private ErrorListener errorListener;

		public Builder setPhoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
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

		public RegisterCheckPhoneNumRequest build() {
			String url = createUrl();
			RegisterCheckPhoneNumRequest request = new RegisterCheckPhoneNumRequest(
					METHOD, url, listener, errorListener);
			return request;
		}

		@Override
		public String createUrl() {
			final StringBuilder sb = new StringBuilder(
					BaseRequest.PSGOD_BASE_URL).append(
					"account/hasRegistered?phone=").append(phoneNumber);
			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}
	}
}
