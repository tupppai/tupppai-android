package com.psgod.network.request;

/**
 * 关注 取消关注 请求
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

public class ActionFollowRequest extends BaseRequest<Boolean> {
	private static final String TAG = ActionFollowRequest.class.getSimpleName();

	public ActionFollowRequest(int method, String url,
			Listener<Boolean> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected Boolean doParseNetworkResponse(JSONObject reponse)
			throws UnsupportedEncodingException, JSONException {
		return true;
	}

	public static class Builder implements IPostRequestBuilder {
		public static final int TYPE_FOLLOW = 1;
		public static final int TYPE_UNFOLLOW = 0;

		private int type;
		private long uid;
		private Listener<Boolean> listener;
		private ErrorListener errorListener;

		public Builder setType(int t) {
			this.type = t;
			return this;
		}

		public Builder setUid(long id) {
			this.uid = id;
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

		public ActionFollowRequest build() {
			String url = createUrl();
			ActionFollowRequest request = new ActionFollowRequest(METHOD, url,
					listener, errorListener) {
				@Override
				public Map<String, String> getParams() {
					return createParameters();
				}
			};
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL)
					.append("profile/follow");
			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();
			params.put("uid", Long.toString(uid));

			if (type == TYPE_FOLLOW) {
				params.put("status", Long.toString(0));
			}

			if (type == TYPE_UNFOLLOW) {
				params.put("status", Long.toString(1));
			}

			return params;
		}

	}
}
