package com.pires.wesee.network.request;

/**
 * 删除消息请求
 * @author brandwang
 */

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.pires.wesee.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class ActionDeleteMessageRequest extends BaseRequest<Boolean> {
	private static final String TAG = ActionDeleteMessageRequest.class
			.getSimpleName();

	public ActionDeleteMessageRequest(int method, String url,
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
		private String mMessageIds;
		private Listener<Boolean> listener;
		private ErrorListener errorListener;
		private int mType = -1;

		public Builder setType(int type) {
			this.mType = type;
			return this;
		}

		public Builder setMessageIds(String ids) {
			this.mMessageIds = ids;
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

		public ActionDeleteMessageRequest build() {
			String url = createUrl();
			ActionDeleteMessageRequest request = new ActionDeleteMessageRequest(
					METHOD, url, listener, errorListener) {
				@Override
				public Map<String, String> getParams() {
					return getPackParams(createParameters());
				}
			};
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(PSGOD_BASE_URL);
			sb.append("message/delete");

			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();
			switch (mType) {
			case -1:
				params.put("mids", mMessageIds);
				break;
			case 1:
				params.put("type", "comment");
				break;
			case 2:
				params.put("type", "reply");
				break;
			case 3:
				params.put("type", "invite");
				break;
			case 4:
				params.put("type", "follow");
				break;
			case 5:
				params.put("type", "system");
				break;
			}
			return params;
		}

	}
}
