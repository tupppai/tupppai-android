package com.psgod.network.request;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;

import org.json.JSONException;
import org.json.JSONObject;

public class ResetPasswordCheckPhoneNumRequest extends BaseRequest<JSONObject> {

	private static final String TAG = ResetPasswordCheckPhoneNumRequest.class
			.getSimpleName();

	public ResetPasswordCheckPhoneNumRequest(int method, String url,
			Listener<JSONObject> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected JSONObject doParseNetworkResponse(JSONObject response)
			throws JSONException {
		JSONObject data = response.getJSONObject("data");
		return data;
	}

	public static class Builder implements IGetRequestBuilder {

		private String phoneNumber;
		private Listener<JSONObject> listener;
		private ErrorListener errorListener;

		public Builder setPhoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
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

		public ResetPasswordCheckPhoneNumRequest build() {
			String url = createUrl();
			ResetPasswordCheckPhoneNumRequest request = new ResetPasswordCheckPhoneNumRequest(
					METHOD, url, listener, errorListener);
			return request;
		}

		@Override
		public String createUrl() {
			// TODO Auto-generated method stub
			final StringBuilder sb = new StringBuilder(
					BaseRequest.PSGOD_BASE_URL)
					.append("account/hasRegistered?");
			sb.append("phone=").append(phoneNumber);
			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}

	}

}
