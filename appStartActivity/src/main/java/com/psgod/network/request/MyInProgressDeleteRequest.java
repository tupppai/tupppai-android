package com.psgod.network.request;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;

public class MyInProgressDeleteRequest extends BaseRequest<Boolean> {
	private static final String TAG = MyInProgressDeleteRequest.class
			.getSimpleName();

	public MyInProgressDeleteRequest(int method, String url,
			Listener<Boolean> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected Boolean doParseNetworkResponse(JSONObject reponse)
			throws UnsupportedEncodingException, JSONException {
		Boolean data = reponse.getBoolean("data");
		return data;
	}

	public static class Builder implements IPostRequestBuilder {

		private long id;
		private Listener<Boolean> mListener;
		private ErrorListener mErrorListener;

		public Builder setId(long id) {
			this.id = id;
			return this;
		}

		public Builder setListener(Listener<Boolean> listener) {
			this.mListener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.mErrorListener = errorListener;
			return this;
		}

		public MyInProgressDeleteRequest build() {
			String url = createUrl();
			MyInProgressDeleteRequest request = new MyInProgressDeleteRequest(
					METHOD, url, mListener, mErrorListener) {
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
					.append("profile/deleteDownloadRecord");
			String url = sb.toString();
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();
			params.put("id", Long.toString(id));
			return params;
		}

	}

}
