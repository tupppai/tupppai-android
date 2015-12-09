package com.psgod.network.request;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;
import com.psgod.network.request.UploadAskRequest.AskUploadResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UploadAskRequest extends BaseRequest<AskUploadResult> {
	private static final String TAG = UploadAskRequest.class.getSimpleName();

	public UploadAskRequest(int method, String url,
			Listener<AskUploadResult> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected AskUploadResult doParseNetworkResponse(JSONObject response)
			throws JSONException {
		AskUploadResult result = new AskUploadResult();
		JSONObject data = response.getJSONObject("data");
		result.askId = data.getLong("ask_id");
		// public long[] labelId; TODO
		return result;
	}

	public static class Builder implements IPostRequestBuilder {
		private long uploadId;
		private float scale;
		private float ratio;
		private Listener<AskUploadResult> listener;
		private ErrorListener errorListener;

		public Builder setUploadId(long uploadId) {
			this.uploadId = uploadId;
			return this;
		}

		public Builder setScale(float scale) {
			this.scale = scale;
			return this;
		}

		public Builder setRatio(float ratio) {
			this.ratio = ratio;
			return this;
		}

		public Builder setListener(Listener<AskUploadResult> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.errorListener = errorListener;
			return this;
		}

		public UploadAskRequest build() {
			String url = createUrl();
			UploadAskRequest request = new UploadAskRequest(METHOD, url,
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
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL)
					.append("ask/save");
			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();
			params.put("upload_id", Long.toString(uploadId));
			params.put("ratio", Float.toString(ratio));
			params.put("scale", Float.toString(scale));

			return params;
		}
	}

	public static class AskUploadResult {
		public long askId;
	}
}
