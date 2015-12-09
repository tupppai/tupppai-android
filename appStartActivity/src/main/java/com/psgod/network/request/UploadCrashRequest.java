package com.psgod.network.request;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UploadCrashRequest extends BaseRequest<Boolean>{
	
	private static final String TAG = UploadCrashRequest.class.getSimpleName();

	public UploadCrashRequest(int method, String url,
			Listener<Boolean> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected Boolean doParseNetworkResponse(JSONObject response)
			throws JSONException {
		return true;
	}
	
	public static class Builder implements IPostRequestBuilder {
		private String mCrashInfo;
		private Listener<Boolean> listener;
		private ErrorListener errorListener;
		
		public Builder setCrashInfo (String crashinfo) {
			this.mCrashInfo = crashinfo;
			this.mCrashInfo.replaceAll(" ", "");
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
		
		public UploadCrashRequest build() {
			String url = createUrl();
			UploadCrashRequest request = new UploadCrashRequest(METHOD, url,
					listener, errorListener) {
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
			StringBuilder sb = new StringBuilder("http://api.loiter.us/")
					.append("app/exceptions");
			String url = sb.toString();
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();
			params.put("message", mCrashInfo);
			return params;
		}
	}

}
