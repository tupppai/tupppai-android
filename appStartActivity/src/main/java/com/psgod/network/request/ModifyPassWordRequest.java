package com.psgod.network.request;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ModifyPassWordRequest extends BaseRequest<Boolean> {
	private static final String TAG = ModifyPassWordRequest.class
			.getSimpleName();

	public ModifyPassWordRequest(int method, String url,
			Listener<Boolean> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected Boolean doParseNetworkResponse(JSONObject response)
			throws JSONException {
		return true;
	}

	public static class Builder implements IPostRequestBuilder {
		private String oldPwd;
		private String newPwd;
		private Listener<Boolean> listener;
		private ErrorListener errorListener;

		public Builder setOldPwd(String old) {
			this.oldPwd = old;
			return this;
		}

		public Builder setNewPwd(String newpwd) {
			this.newPwd = newpwd;
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

		public ModifyPassWordRequest build() {
			String url = createUrl();
			ModifyPassWordRequest request = new ModifyPassWordRequest(METHOD,
					url, listener, errorListener) {
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
					.append("profile/updatePassword");
			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();
			params.put("old_pwd", oldPwd);
			params.put("new_pwd", newPwd);
			return params;
		}
	}
}
