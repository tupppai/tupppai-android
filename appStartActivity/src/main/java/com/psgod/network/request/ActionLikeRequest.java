package com.psgod.network.request;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;

/**
 * 点赞请求
 * 
 * @author rayalyuan
 */
public class ActionLikeRequest extends BaseRequest<Boolean> {
	private static final String TAG = ActionLikeRequest.class.getSimpleName();

	public ActionLikeRequest(int method, String url,
			Listener<Boolean> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected Boolean doParseNetworkResponse(JSONObject reponse)
			throws UnsupportedEncodingException, JSONException {
		return true;
	}

	public static class Builder implements IGetRequestBuilder {
		// 求P
		public static final byte TYPE_ASK = 1;
		// 回复
		public static final byte TYPE_REPLY = 2;

		private int status; // 0 取消 1 点赞
		private long pid;
		private int type;
		private Listener<Boolean> listener;
		private ErrorListener errorListener;

		public Builder setType(int t) {
			this.type = t;
			return this;
		}

		public Builder setPid(long pid) {
			this.pid = pid;
			return this;
		}

		public Builder setStatus(int s) {
			this.status = s;
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

		public ActionLikeRequest build() {
			String url = createUrl();
			ActionLikeRequest request = new ActionLikeRequest(METHOD, url,
					listener, errorListener);

			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);
			if (type == TYPE_ASK) {
				sb.append("ask/upask/" + pid);
			}
			if (type == TYPE_REPLY) {
				sb.append("reply/upreply/" + pid);
			}
			sb.append("?status=" + status);

			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}
	}
}
