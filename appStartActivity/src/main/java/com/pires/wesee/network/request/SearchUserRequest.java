package com.pires.wesee.network.request;

import android.content.res.Resources;

import com.alibaba.fastjson.JSON;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.pires.wesee.Logger;
import com.pires.wesee.PSGodApplication;
import com.pires.wesee.model.SearchUser;
import com.pires.wesee.model.SearchUserData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchUserRequest extends BaseRequest<List<SearchUserData>> {
	private static final String TAG = UserDetailRequest.class.getSimpleName();

	public SearchUserRequest(int method, String url,
			Listener<List<SearchUserData>> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected List<SearchUserData> doParseNetworkResponse(JSONObject response)
			throws UnsupportedEncodingException, JSONException {
		List<SearchUserData> items = JSON
				.parseObject(response.toString(), SearchUser.class).getData();
		return items;
	}

	public static class Builder implements IGetRequestBuilder {
		Resources res = PSGodApplication.getAppContext().getResources();

		private String name;
		private int page;

		private Listener<List<SearchUserData>> mListener;
		private ErrorListener mErrorListener;

//		public Builder setName(String name) {
//			this.name = name;
//			return this;
//		}
//
//		public Builder setPage(int page) {
//			this.page = page;
//			return this;
//		}

		public Builder setListener(Listener<List<SearchUserData>> listener) {
			this.mListener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.mErrorListener = errorListener;
			return this;
		}

		public SearchUserRequest build() {
			String url = createUrl();
			SearchUserRequest request = new SearchUserRequest(METHOD, url,
					mListener, mErrorListener);
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL)
					.append("user/search");
//			sb.append("?name=" + name);
//			sb.append("&page=" + page);
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
