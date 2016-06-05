package com.pires.wesee.network.request;

/**
 * 举报接口请求
 */

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.pires.wesee.Logger;
import com.pires.wesee.model.PhotoItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ReportRequest extends BaseRequest<Boolean> {
	private static final String TAG = ReportRequest.class.getSimpleName();

	public ReportRequest(int method, String url, Listener<Boolean> listener,
			ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected Boolean doParseNetworkResponse(JSONObject reponse)
			throws JSONException {
		Boolean data = reponse.getBoolean("data");
		return data;
	}

	public static class Builder implements IPostRequestBuilder {
		private PhotoItem mPhotoItem;
		private String mContent;
		private Listener<Boolean> mListener;
		private ErrorListener mErrorListener;

		public Builder setPhotoItem(PhotoItem mPhotoItem) {
			this.mPhotoItem = mPhotoItem;
			return this;
		}

		public Builder setContent(String mContent) {
			this.mContent = mContent;
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

		public ReportRequest build() {
			String url = createUrl();
			ReportRequest request = new ReportRequest(METHOD, url, mListener,
					mErrorListener) {
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
			StringBuilder sb = new StringBuilder(PSGOD_BASE_URL)
					.append("inform/report_abuse");
			String url = sb.toString();
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();
			params.put("target_type", Integer.toString(mPhotoItem.getType()));
			params.put("target_id", Long.toString(mPhotoItem.getPid()));
			params.put("content", mContent);
			return params;
		}
	}

}
