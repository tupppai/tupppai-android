package com.pires.wesee.network.request;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.pires.wesee.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class EditAskDescRequest extends BaseRequest<Boolean> {
	private static final String TAG = EditAskDescRequest.class.getSimpleName();

	public EditAskDescRequest(int method, String url,
			Listener<Boolean> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected Boolean doParseNetworkResponse(JSONObject reponse)
			throws UnsupportedEncodingException, JSONException {
		return true;
	}

	public static class Builder implements IPostRequestBuilder {

		private long askId;
		private String desc;
		private Listener<Boolean> listener;
		private ErrorListener errorListener;

		public Builder setDesc(String desc) {
			this.desc = desc;
			return this;
		}

		public Builder setAskId(long askId) {
			this.askId = askId;
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

		public EditAskDescRequest build() {
			String url = createUrl();
			EditAskDescRequest request = new EditAskDescRequest(METHOD, url,
					listener, errorListener) {
				@Override
				public Map<String, String> getParams() {
					return getPackParams(createParameters());
				}
			};
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url + createParameters());
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL)
					.append("ask/edit");
			String url = sb.toString();
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();
			params.put("ask_id", Long.toString(askId));
			params.put("desc", desc);
			return params;
		}

	}
}
