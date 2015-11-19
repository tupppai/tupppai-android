package com.psgod.network.request;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;
import com.psgod.PSGodApplication;
import com.psgod.model.PhotoItem;

public class SettingLikedRequest extends BaseRequest<List<PhotoItem>> {
	private static final String TAG = SettingLikedRequest.class.getSimpleName();

	public SettingLikedRequest(int method, String url,
			Listener<List<PhotoItem>> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected List<PhotoItem> doParseNetworkResponse(JSONObject response)
			throws UnsupportedEncodingException, JSONException {
		JSONArray data = response.getJSONArray("data");
		int length = data.length();
		List<PhotoItem> items = new ArrayList<PhotoItem>(length);
		for (int ix = 0; ix < length; ++ix) {
			items.add(PhotoItem.createPhotoItem(data.getJSONObject(ix)));
		}
		return items;
	}

	public static class Builder implements IGetRequestBuilder {
		Resources res = PSGodApplication.getAppContext().getResources();

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

		public Builder setListener(Listener<List<PhotoItem>> listener) {
			this.mListener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.mErrorListener = errorListener;
			return this;
		}

		public SettingLikedRequest build() {
			String url = createUrl();
			SettingLikedRequest request = new SettingLikedRequest(METHOD, url,
					mListener, mErrorListener);
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL)
					.append("profile/uped");
			sb.append("?page=" + mPage);
			sb.append("&size=" + mSize);

			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}
	}

}
