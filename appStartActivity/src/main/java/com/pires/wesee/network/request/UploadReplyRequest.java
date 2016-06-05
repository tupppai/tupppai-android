package com.pires.wesee.network.request;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.pires.wesee.Logger;
import com.pires.wesee.network.request.UploadReplyRequest.ReplyUploadResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UploadReplyRequest extends BaseRequest<ReplyUploadResult> {
	private static final String TAG = UploadReplyRequest.class.getSimpleName();

	public UploadReplyRequest(int method, String url,
			Listener<ReplyUploadResult> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected ReplyUploadResult doParseNetworkResponse(JSONObject response)
			throws JSONException {
		ReplyUploadResult result = new ReplyUploadResult();
		JSONObject data = response.getJSONObject("data");
		result.replyId = data.getLong("reply_id");
		// public long[] labelId; TODO
		return result;
	}

	public static class Builder implements IPostRequestBuilder {
		private long askId;
		private long uploadId;
		private float scale;
		private float ratio;
		private Listener<ReplyUploadResult> listener;
		private ErrorListener errorListener;

		public Builder setAskId(long askId) {
			this.askId = askId;

			return this;
		}

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

		public Builder setListener(Listener<ReplyUploadResult> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.errorListener = errorListener;
			return this;
		}

		public UploadReplyRequest build() {
			String url = createUrl();
			UploadReplyRequest request = new UploadReplyRequest(METHOD, url,
					listener, errorListener) {
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
			StringBuilder sb = new StringBuilder(PSGOD_BASE_URL)
					.append("reply/save");
			String url = sb.toString();
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();
			params.put("ask_id", String.valueOf(askId));
			params.put("upload_id", String.valueOf(uploadId));
			params.put("ratio", String.valueOf(ratio));
			params.put("scale", String.valueOf(scale));
			return params;
		}
	}

	public static class ReplyUploadResult {
		// 回复id
		public long replyId;
	}
}
