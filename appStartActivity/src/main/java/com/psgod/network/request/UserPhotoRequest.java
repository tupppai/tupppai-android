package com.psgod.network.request;

/**
 * 用户相关照片列表(我的作品、我的求P、我的进行中、我的收藏)
 * @author brandwang
 */

import android.content.res.Resources;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Constants;
import com.psgod.Logger;
import com.psgod.PSGodApplication;
import com.psgod.R;
import com.psgod.model.PhotoItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class UserPhotoRequest extends BaseRequest<List<PhotoItem>> {
	private final static String TAG = UserPhotoRequest.class.getSimpleName();
	private int request_type = 0;
	private static final int MY_PAGE_ASK = 5;

	public UserPhotoRequest(int method, String url,
			Listener<List<PhotoItem>> listener, ErrorListener errorListener,
			int type) {
		super(method, url, listener, errorListener);
		this.request_type = type;
	}

	@Override
	protected List<PhotoItem> doParseNetworkResponse(JSONObject response)
			throws JSONException {
		JSONArray data = response.getJSONArray("data");

		int length = data.length();
		List<PhotoItem> items = new ArrayList<PhotoItem>();
		for (int ix = 0; ix < length; ++ix) {
			items.add(PhotoItem.createPhotoItem(data.getJSONObject(ix)));
		}

//		for (int ix = 0; ix < length; ++ix) {
//			if (this.request_type == MY_PAGE_ASK) {
//				JSONObject jsonObject = data.getJSONObject(ix);
//				if (jsonObject.has("ask_uploads")) {
//					JSONArray uploadArray = jsonObject
//							.getJSONArray("ask_uploads");
//					int uploadSize = uploadArray.length();
//					if (uploadSize == 1) {
//						items.add(PhotoItem.createPhotoItem(data
//								.getJSONObject(ix)));
//					} else if (uploadSize == 2) {
//						PhotoItem photoItem = PhotoItem.createPhotoItem(data
//								.getJSONObject(ix));
//						PhotoItem cloneItem = new PhotoItem();
//						cloneItem = photoItem.clone();
//						items.add(photoItem);
//						JSONObject uploadImage = uploadArray.getJSONObject(1);
//						cloneItem.setImageURL(uploadImage
//								.getString("image_url"));
//						items.add(cloneItem);
//					}
//				}
//			} else {
//				items.add(PhotoItem.createPhotoItem(data.getJSONObject(ix)));
//			}
//		}
		return items;
	}

	public static class Builder implements IGetRequestBuilder {
		public static final int MY_ASK = 0;
		public static final int MY_WORKS = 1;
		public static final int MY_INPROGRESS = 2;
		public static final int MY_COLLECTION = 3;
		public static final int MY_PICTURE = 4;
		public static final int MY_PAGE_ASK = 5;

		Resources res = PSGodApplication.getAppContext().getResources();
		private int width = Constants.WIDTH_OF_SCREEN - 2
				* res.getDimensionPixelSize(R.dimen.photo_margin);
		private int type = MY_ASK;
		private long lastUpdated = -1;
		private int page = 1;
		private int size = 15;
		private String channelId;
		private Listener<List<PhotoItem>> listener;
		private ErrorListener errorListener;

		public Builder setChannelId(String channelId) {
			this.channelId = channelId;
			return this;
		}

		public Builder setWidth(int width) {
			this.width = width;
			return this;
		}

		public Builder setType(int type) {
			this.type = type;
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

		public UserPhotoRequest build() {
			String url = createUrl();

			UserPhotoRequest request = new UserPhotoRequest(METHOD, url,
					listener, errorListener, type);
			return request;
		}

		@Override
		public String createUrl() {
			// TODO 接口替换 根据type
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);
			switch (type) {
			case MY_ASK:
				sb.append("profile/asks");
				break;

			case MY_COLLECTION:
				sb.append("thread/subscribed");
				break;

			case MY_INPROGRESS:
				sb.append("profile/downloaded");
				break;

			case MY_WORKS:
				sb.append("profile/replies");
				break;

			case MY_PICTURE:
				sb.append("profile/threads");
				break;

			case MY_PAGE_ASK:
				sb.append("profile/asks");
				break;

			default:
				break;
			}

			sb.append("?width=").append(width);
			sb.append("&page=").append(page);
			sb.append("&size=").append(size);

			if(channelId != null && !channelId.equals("")){
				sb.append("&channel_id=").append(channelId);
			}

			if (lastUpdated != -1) {
				sb.append("&last_updated=").append(lastUpdated);
			}

			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}
	}
}
