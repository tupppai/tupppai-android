package com.psgod.network.request;

/**
 * 【求P回复请求】
 * 
 * @author brandwang
 */

import android.content.res.Resources;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Constants;
import com.psgod.Logger;
import com.psgod.PSGodApplication;
import com.psgod.model.PhotoItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class PhotoReplyRequest extends BaseRequest<List<PhotoItem>> {
	private final static String TAG = PhotoReplyRequest.class.getSimpleName();

	public PhotoReplyRequest(int method, String url,
			Listener<List<PhotoItem>> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected List<PhotoItem> doParseNetworkResponse(JSONObject reponse)
			throws UnsupportedEncodingException, JSONException {
		List<PhotoItem> items = new ArrayList<PhotoItem>();

		JSONObject data = reponse.getJSONObject("data");
		JSONArray replies = data.getJSONArray("replies");

		if (data.has("ask")) {
			JSONObject askItem = data.getJSONObject("ask");
			items.add(PhotoItem.createPhotoItem(askItem,getUrl()));
		}

		int length = replies.length();
		for (int ix = 0; ix < length; ++ix) {
			items.add(PhotoItem.createPhotoItem(replies.getJSONObject(ix),getUrl()));
		}
		return items;
	}

	public static class Builder implements IGetRequestBuilder {
		Resources res = PSGodApplication.getAppContext().getResources();

		private int width = Constants.WIDTH_OF_SCREEN;
		private int page = 1;
		private int needOriginPhoto = 0;
		private Listener<List<PhotoItem>> listener;
		private ErrorListener errorListener;
		private long id;
		private long pid = -1;
		private long lastUpdatedTime = -1;
		private long categoryId = -1;

		public Builder setCategoryId(long categoryId) {
			this.categoryId = categoryId;
			return this;
		}

		public Builder setNeedOriginPhoto(int need) {
			this.needOriginPhoto = need;
			return this;
		}

		public Builder setPid(long pid) {
			this.pid = pid;
			return this;
		}

		public Builder setId(long id) {
			this.id = id;
			return this;
		}

		public Builder setWidth(int width) {
			this.width = width;
			return this;
		}

		public Builder setLastUpdatedTime(long lastUpdatedTime) {
			this.lastUpdatedTime = lastUpdatedTime;
			return this;
		}

		public Builder setPage(int page) {
			this.page = page;
			return this;
		}

		public Builder setListener(Listener<List<PhotoItem>> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.errorListener = errorListener;
			return this;
		}

		public PhotoReplyRequest build() {
			String url = createUrl();
			PhotoReplyRequest request = new PhotoReplyRequest(METHOD, url,
					listener, errorListener);
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL)
					.append("ask/show/").append(id);
			sb.append("?width=").append(width);
			if (pid != -1) {
				sb.append("&reply_id=").append(pid);
			}
			sb.append("&fold=").append(needOriginPhoto);
			sb.append("&page=").append(page);
			if (lastUpdatedTime != -1) {
				sb.append("&last_updated=").append(lastUpdatedTime);
			}

			if(categoryId != -1){
				sb.append("&category_id=").append(categoryId);
			}

			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}
	}

}
