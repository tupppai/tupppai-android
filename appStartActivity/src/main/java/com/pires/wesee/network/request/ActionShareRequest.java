package com.pires.wesee.network.request;

/**
 * 发起分享的请求
 * @author brandwang
 */

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.pires.wesee.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class ActionShareRequest extends BaseRequest<JSONObject> {
	private final static String TAG = MyFollowersListRequest.class
			.getSimpleName();

	public ActionShareRequest(int method, String url,
			Listener<JSONObject> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected JSONObject doParseNetworkResponse(JSONObject reponse)
			throws UnsupportedEncodingException, JSONException {
		JSONObject data = reponse.getJSONObject("data");
		return data;
	}

	public static class Builder implements IGetRequestBuilder {
		// 分享平台类型
		private String mShareType;
		// 作品类型 1 ask 2 reply
		private int mType;
		// 作品id
		private long mId;
		private int width;
		private Listener<JSONObject> listener;
		private ErrorListener errorListener;

		public Builder setShareType(String shareType) {
			this.mShareType = shareType;
			return this;
		}

		public Builder setType(int type) {
			this.mType = type;
			return this;
		}

		public Builder setId(long id) {
			this.mId = id;
			return this;
		}

		public Builder setListener(Listener<JSONObject> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.errorListener = errorListener;
			return this;
		}

		public ActionShareRequest build() {
			String url = createUrl();
			ActionShareRequest request = new ActionShareRequest(METHOD, url,
					listener, errorListener);
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(PSGOD_BASE_URL)
					.append("app/share");
			sb.append("?share_type=").append(mShareType);
			sb.append("&type=").append(Integer.toString(mType));
			sb.append("&target_id=").append(Long.toString(mId));

			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}
	}
}
