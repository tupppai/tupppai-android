package com.psgod.network.request;

/**
 * 获取短信验证码接口
 * @author brandwang
 */
import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;

public class GetVerifyCodeRequest extends BaseRequest<String> {
	private static final String TAG = ActionCollectionRequest.class
			.getSimpleName();

	public GetVerifyCodeRequest(int method, String url,
			Listener<String> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected String doParseNetworkResponse(JSONObject reponse)
			throws UnsupportedEncodingException, JSONException {
		JSONObject data = reponse.getJSONObject("data");
		String code = data.getString("code");

		return code;
	}

	public static class Builder implements IGetRequestBuilder {
		private String phoneNum;
		private Listener<String> listener;
		private ErrorListener errorListener;

		public Builder setPhone(String num) {
			this.phoneNum = num;
			return this;
		}

		public Builder setListener(Listener<String> listener) {
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
