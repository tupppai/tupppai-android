package com.psgod.network.request;

/**
 * 获取短信验证码接口
 * @author brandwang
 */

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class GetVerifyCodeRequest extends BaseRequest<Boolean> {
	private static final String TAG = ActionCollectionRequest.class
			.getSimpleName();

	public GetVerifyCodeRequest(int method, String url,
			Listener<Boolean> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected Boolean doParseNetworkResponse(JSONObject reponse)
			throws UnsupportedEncodingException, JSONException {

		return true;
	}

	public static class Builder implements IGetRequestBuilder {
		private String phoneNum;
		private Listener<Boolean> listener;
		private ErrorListener errorListener;

		public Builder setPhone(String num) {
			this.phoneNum = num;
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

		public GetVerifyCodeRequest build() {
			String url = createUrl();
			GetVerifyCodeRequest request = new GetVerifyCodeRequest(METHOD,
					url, listener, errorListener);

			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);
			sb.append("account/requestAuthCode?");
			sb.append("phone=" + phoneNum);

			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);

			return url;
		}

	}
}
