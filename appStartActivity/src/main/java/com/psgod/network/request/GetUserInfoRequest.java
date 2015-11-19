package com.psgod.network.request;

/**
 * 获取用户信息请求
 * @author brandwang
 */
import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;

public class GetUserInfoRequest extends BaseRequest<JSONObject> {
	private static final String TAG = GetUserInfoRequest.class.getSimpleName();

	public GetUserInfoRequest(int method, String url,
			Listener<JSONObject> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected JSONObject doParseNetworkResponse(JSONObject reponse)
			throws UnsupportedEncodingException, JSONException {
		JSONObject data = reponse.getJSONObject("data");
		return data;
	}

	public static class Builder implements IGetRequestBuilder {
		private Listener<JSONObject> listener;
		private ErrorListener errorListener;

		public Builder setListener(Listener<JSONObject> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.errorListener = errorListener;
			return this;
		}

		public GetUserInfoRequest build() {
			String url = createUrl();
			GetUserInfoRequest request = new GetUserInfoRequest(METHOD, url,
					listener, errorListener);
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);
			sb.append("profile/view");

			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}
	}
}
