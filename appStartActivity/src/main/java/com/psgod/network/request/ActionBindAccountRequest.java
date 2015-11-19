package com.psgod.network.request;

/**
 * 绑定/解绑第三方账号 请求
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

public class ActionBindAccountRequest extends BaseRequest<Boolean> {
	private static final String TAG = ActionBindAccountRequest.class
			.getSimpleName();

	public ActionBindAccountRequest(int method, String url,
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
		// 1 绑定 0 取消绑定
		public static final int TYPE_BIND = 1;
		public static final int TYPE_UNBIND = 0;

		// 平台类型 weixin weibo mobile
		private String type;
		private String openid;
		private int isBind;
		private Listener<Boolean> listener;
		private ErrorListener errorListener;

		public Builder setIsBind(int isbind) {
			this.isBind = isbind;
			return this;
		}

		public Builder setType(String t) {
			this.type = t;
			return this;
		}

		public Builder setOpenId(String id) {
			this.openid = id;
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

		public ActionBindAccountRequest build() {
			String url = createUrl();
			ActionBindAccountRequest request = new ActionBindAccountRequest(
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
			if (isBind == TYPE_BIND) {
				sb.append("auth/bind");
			}
			if (isBind == TYPE_UNBIND) {
				sb.append("auth/unbind");
			}
			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();
			if (isBind == TYPE_BIND) {
				params.put("openid", openid);
			}
			params.put("type", type);
			return params;
		}

	}
}
