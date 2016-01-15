package com.psgod.network.request;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class MyInProgressDeleteRequest extends BaseRequest<Boolean> {
	private static final String TAG = MyInProgressDeleteRequest.class
			.getSimpleName();

	public MyInProgressDeleteRequest(int method, String url,
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

		private int type = 0;
		private final int TYPE_ASK = 0;
		private final int TYPE_REPLY = 1;
		private final int TYPE_COMPLETE = 2;


		private long id;
		private long categoryId = -1;
		private Listener<Boolean> mListener;
		private ErrorListener mErrorListener;

		public Builder setCategoryId(long categoryId) {
			this.categoryId = categoryId;
			return this;
		}

		public Builder setId(long id) {
			this.id = id;
			return this;
		}

		public Builder setType (int type) {
			this.type = type;
			return this;
		}

		public Builder setListener(Listener<Boolean> listener) {
			this.mListener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.mErrorListener = errorListener;
			return this;
		}

		public MyInProgressDeleteRequest build() {
			String url = createUrl();
			MyInProgressDeleteRequest request = new MyInProgressDeleteRequest(
					METHOD, url, mListener, mErrorListener) {
				@Override
				public Map<String, String> getParams() {
					return createParameters();
				}
			};
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url + createParameters());
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);
			if (type == TYPE_ASK) {
				sb.append("profile/askdelete");
			} else if (type == TYPE_REPLY) {
				sb.append("profile/deleteDownloadRecord");
			} else {
				sb.append("profile/replydelete");
			}

			String url = sb.toString();
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();
			params.put("id", Long.toString(id));
//			if(categoryId != -1) {
////				params.put("category_id", String.valueOf(categoryId));
//			}
			return params;
		}

	}

}
