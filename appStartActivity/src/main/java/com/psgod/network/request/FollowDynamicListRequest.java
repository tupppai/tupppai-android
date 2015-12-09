package com.psgod.network.request;

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

import java.util.ArrayList;
import java.util.List;

/**
 * 【关注动态请求】
 * 
 * width: 屏幕尺寸 last_updated: 最后下拉更新的时间戳（秒） page: 页码，默认为 1 size: 单面数目，默认为 15
 * 
 * @author brandwang
 * 
 */
public final class FollowDynamicListRequest extends
		BaseRequest<List<PhotoItem>> {
	private final static String TAG = FollowDynamicListRequest.class
			.getSimpleName();

	public FollowDynamicListRequest(int method, String url,
			Listener<List<PhotoItem>> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	/**
	 * 该方法在子线程里调用
	 */
	@Override
	protected List<PhotoItem> doParseNetworkResponse(JSONObject response)
			throws JSONException {
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

		private int width = Constants.WIDTH_OF_SCREEN;
		private long lastUpdated;
		private int page = 1;
		private int size = 15;
		private Listener<List<PhotoItem>> listener;
		private ErrorListener errorListener;

		public Builder setWidth(int width) {
			this.width = width;
			return this;
		}

		public Builder setLastUpdated(long lastUpdated) {
			this.lastUpdated = lastUpdated;
			return this;
		}

		public Builder setPage(int page) {
			this.page = page;
			return this;
		}

		public Builder setSize(int size) {
			this.size = size;
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

		public FollowDynamicListRequest build() {
			String url = createUrl();
			FollowDynamicListRequest request = new FollowDynamicListRequest(
					METHOD, url, listener, errorListener);
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL)
					.append("thread/timeline");
			sb.append("?width=").append(width);
			sb.append("&last_updated=").append(lastUpdated);
			sb.append("&page=").append(page);
			sb.append("&size=").append(size);

			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}
	}
}
