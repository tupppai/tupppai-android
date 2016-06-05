package com.pires.wesee.network.request;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.pires.wesee.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class ActionCollectionRequest extends BaseRequest<Boolean> {
	private static final String TAG = ActionCollectionRequest.class
			.getSimpleName();

	public ActionCollectionRequest(int method, String url,
			Listener<Boolean> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected Boolean doParseNetworkResponse(JSONObject reponse)
			throws UnsupportedEncodingException, JSONException {
		return true;
	}

	public static class Builder implements IGetRequestBuilder {
		public static final int TYPE_ASK = 1;
		public static final int TYPE_REPLY = 2;
		public static final int STATUS_UNCOLLECTION = 101;
		public static final int STATUS_COLLECTION = 102;

		private int type;
		private long pid;
		private int status;
		private Listener<Boolean> listener;
		private ErrorListener errorListener;

		public Builder setStatus(int s) {
			this.status = s;
			return this;
		}

		public Builder setPid(long id) {
			this.pid = id;
			return this;
		}

		public Builder setType(int t) {
			this.type = t;
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

		public ActionCollectionRequest build() {
			String url = createUrl();
			ActionCollectionRequest request = new ActionCollectionRequest(
					METHOD, url, listener, errorListener);

			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(PSGOD_BASE_URL);
			if (type == TYPE_ASK) {
				sb.append("ask/focusask/");
			}
			if (type == TYPE_REPLY) {
				sb.append("reply/collectreply/");
			}
			sb.append(pid);

			if (status == STATUS_COLLECTION) {
				sb.append("?status=1");
			}
			if (status == STATUS_UNCOLLECTION) {
				sb.append("?status=0");
			}

			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl :" + url);
			return url;
		}

	}
}
