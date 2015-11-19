package com.psgod.network.request;

/**
 * 邀请操作
 * @author brandwang
 */
import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;

public class ActionInviteRequest extends BaseRequest<Boolean> {
	private static final String TAG = ActionCollectionRequest.class
			.getSimpleName();

	public ActionInviteRequest(int method, String url,
			Listener<Boolean> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected Boolean doParseNetworkResponse(JSONObject reponse)
			throws UnsupportedEncodingException, JSONException {
		return true;
	}

	public static class Builder implements IGetRequestBuilder {
		private long uid;
		private long askId;
		private Listener<Boolean> listener;
		private ErrorListener errorListener;

		public Builder setUid(long id) {
			this.uid = id;
			return this;
		}

		public Builder setAskId(long askid) {
			this.askId = askid;
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

		public ActionInviteRequest build() {
			String url = createUrl();
			ActionInviteRequest request = new ActionInviteRequest(METHOD, url,
					listener, errorListener);

			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);
			sb.append("invitation/invite?");
			sb.append("ask_id=" + askId);
			sb.append("&invite_uid=" + uid);

			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);

			return url;
		}

	}
}
