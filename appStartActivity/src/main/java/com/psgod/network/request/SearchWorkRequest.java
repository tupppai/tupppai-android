package com.psgod.network.request;

import android.content.res.Resources;

import com.alibaba.fastjson.JSON;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;
import com.psgod.PSGodApplication;
import com.psgod.model.SearchWork;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchWorkRequest extends BaseRequest<List<SearchWork.Data>> {
	private static final String TAG = UserDetailRequest.class.getSimpleName();

	public SearchWorkRequest(int method, String url,
			Listener<List<SearchWork.Data>> listener,
			ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected List<SearchWork.Data> doParseNetworkResponse(JSONObject response)
			throws UnsupportedEncodingException, JSONException {
		SearchWork item = JSON.parseObject(
				response.toString(), SearchWork.class);
		return item.getData();
	}

	public static class Builder implements IGetRequestBuilder {
		Resources res = PSGodApplication.getAppContext().getResources();

		private String desc;
		private int page;

		private Listener<List<SearchWork.Data>> mListener;
		private ErrorListener mErrorListener;

		// public Builder setDesc(String desc) {
		// this.desc = desc;
		// return this;
		// }
		//
		// public Builder setPage(int page) {
		// this.page = page;
		// return this;
		// }

		public Builder setListener(Listener<List<SearchWork.Data>> listener) {
			this.mListener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.mErrorListener = errorListener;
			return this;
		}

		public SearchWorkRequest build() {
			String url = createUrl();
			SearchWorkRequest request = new SearchWorkRequest(METHOD, url,
					mListener, mErrorListener);
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL)
					.append("thread/search");
			// sb.append("?desc=" + desc);
			// sb.append("&page=" + page);
			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}
	}

	HashMap<String, String> params = new HashMap<String, String>();

	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		return params;
	}

	public void setParams(HashMap<String, String> params) {
		this.params = params;
	}

	@Override
	public int getMethod() {
		return Request.Method.POST;
	}
}
