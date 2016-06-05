package com.pires.wesee.network.request;

import android.content.res.Resources;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.pires.wesee.Constants;
import com.pires.wesee.Logger;
import com.pires.wesee.PSGodApplication;
import com.pires.wesee.model.PhotoItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class UserDetailAskRequest extends BaseRequest<List<PhotoItem>> {
	private static final String TAG = UserDetailRequest.class.getSimpleName();

	public UserDetailAskRequest(int method, String url,
			Listener<List<PhotoItem>> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected List<PhotoItem> doParseNetworkResponse(JSONObject response)
			throws UnsupportedEncodingException, JSONException {
		JSONArray data = response.getJSONArray("data");
		int length = data.length();

		List<PhotoItem> items = new ArrayList<PhotoItem>();
		for (int i = 0; i < length; i++) {
			items.add(PhotoItem.createPhotoItem(data.getJSONObject(i),getUrl()));
		}

		return items;
	}

	public static class Builder implements IGetRequestBuilder {
		Resources res = PSGodApplication.getAppContext().getResources();

		private Long mUid;
		private int mWidth = Constants.WIDTH_OF_SCREEN;
		private int mPage;
		private int mSize = 15;

		private Listener<List<PhotoItem>> mListener;
		private ErrorListener mErrorListener;

		public Builder setSize(int size) {
			this.mSize = size;
			return this;
		}

		public Builder setPage(int page) {
			this.mPage = page;
			return this;
		}

		public Builder setWidth(int width) {
			this.mWidth = width;
			return this;
		}

		public Builder setUserId(Long uid) {
			this.mUid = uid;
			return this;
		}

		public Builder setListener(Listener<List<PhotoItem>> listener) {
			this.mListener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.mErrorListener = errorListener;
			return this;
		}

		public UserDetailAskRequest build() {
			String url = createUrl();
			UserDetailAskRequest request = new UserDetailAskRequest(METHOD,
					url, mListener, mErrorListener);
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(PSGOD_BASE_URL)
					.append("profile/askswithreplies");
			sb.append("?uid=" + mUid);
			sb.append("&page=" + mPage);
			sb.append("&width=" + mWidth);
			sb.append("&size=" + mSize);

			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}
	}
}
