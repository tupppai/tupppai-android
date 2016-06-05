package com.pires.wesee.network.request;

/**
 * 获取推荐APP列表请求
 * @author brandwang
 */

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.pires.wesee.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class RecommendAppRequest extends BaseRequest<JSONArray> {
	private static final String TAG = RecommendAppRequest.class.getSimpleName();

	public RecommendAppRequest(int method, String url,
			Listener<JSONArray> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected JSONArray doParseNetworkResponse(JSONObject reponse)
			throws UnsupportedEncodingException, JSONException {
		JSONArray data = reponse.getJSONArray("data");
		return data;
	}

	public static class Builder implements IGetRequestBuilder {
		private Listener<JSONArray> listener;
		private ErrorListener errorListener;

		public Builder setListener(Listener<JSONArray> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.errorListener = errorListener;
			return this;
		}

		public RecommendAppRequest build() {
			String url = createUrl();
			RecommendAppRequest request = new RecommendAppRequest(METHOD, url,
					listener, errorListener);

			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(PSGOD_BASE_URL);
			sb.append("app/get_app_list");

			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);

			return url;
		}

	}
}
