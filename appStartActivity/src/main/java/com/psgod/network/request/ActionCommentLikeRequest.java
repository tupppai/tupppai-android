package com.psgod.network.request;

/**
 * 评论点赞请求
 * @author brandwang
 */
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;

public class ActionCommentLikeRequest extends BaseRequest<Boolean> {
	private static final String TAG = ActionCommentLikeRequest.class
			.getSimpleName();

	public ActionCommentLikeRequest(int method, String url,
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
		// 评论id
		private long mCId;
		// 0 取消 1 点赞
		private int status;
		private Listener<Boolean> listener;
		private ErrorListener errorListener;

		public Builder setStatus(int stat) {
			this.status = stat;
			return this;
		}

		public Builder setCid(long id) {
			this.mCId = id;
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

		public ActionCommentLikeRequest build() {
			String url = createUrl();
			ActionCommentLikeRequest request = new ActionCommentLikeRequest(
					METHOD, url, listener, errorListener) {
				@Override
				public Map<String, String> getParams() {
					return createParameters();
				}
			};
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);
			sb.append("comment/upComment/" + Long.toString(mCId));

			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);

			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();
			params.put("status", Integer.toString(status));
			return params;
		}

	}
}
