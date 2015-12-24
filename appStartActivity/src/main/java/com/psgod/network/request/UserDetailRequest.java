package com.psgod.network.request;

import android.content.res.Resources;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Constants;
import com.psgod.Logger;
import com.psgod.PSGodApplication;
import com.psgod.model.PhotoItem;
import com.psgod.model.User;
import com.psgod.network.request.UserDetailRequest.UserDetailResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class UserDetailRequest extends BaseRequest<UserDetailResult> {
	private static final String TAG = UserDetailRequest.class.getSimpleName();

	public UserDetailRequest(int method, String url,
			Listener<UserDetailResult> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected UserDetailResult doParseNetworkResponse(JSONObject response)
			throws UnsupportedEncodingException, JSONException {
		UserDetailResult result = new UserDetailResult();

		JSONObject data = response.getJSONObject("data");
//		retUser.setUid(data.getLong("uid"));
//		retUser.setNickname(data.getString("nickname"));
//		retUser.setGender(data.getInt("sex"));
//		retUser.setAvatarImageUrl(data.getString("avatar"));
//		retUser.setFollowerCount(data.getInt("fans_count"));
//		retUser.setFollowingCount(data.getInt("fellow_count"));
//		retUser.setAskCount(data.getInt("ask_count"));
//		retUser.setReplyCount(data.getInt("reply_count"));
//		retUser.setBackgroundUrl(data.getString("bg_image"));
//		retUser.setFollowed(data.getInt("is_fan"));
//		retUser.setFollowing(data.getInt("is_follow"));
//		retUser.setLikedCount(data.getInt("uped_count"));
		// retUser.setLikedCount(data.getInt("like_count"));

		result.user = User.createUser(data);

		if (data.has("asks")) {
			JSONArray asks = data.getJSONArray("asks");
			int asks_length = asks.length();
			List<PhotoItem> asksPhotoItems = new ArrayList<PhotoItem>(
					asks_length);
			for (int i = 0; i < asks_length; i++) {
				asksPhotoItems.add(PhotoItem.createPhotoItem(asks
						.getJSONObject(i)));
			}
			result.askPhotoItems = asksPhotoItems;
		}

		if (data.has("replies")) {
			JSONArray replys = data.getJSONArray("replies");
			int replys_length = replys.length();
			List<PhotoItem> replysPhotoItems = new ArrayList<PhotoItem>(
					replys_length);
			for (int i = 0; i < replys_length; i++) {
				replysPhotoItems.add(PhotoItem.createPhotoItem(replys
						.getJSONObject(i)));
			}
			result.replyPhotoItems = replysPhotoItems;
		}

		return result;
	}

	public static class Builder implements IGetRequestBuilder {
		Resources res = PSGodApplication.getAppContext().getResources();

		private Long mUid;
		private int mWidth = Constants.WIDTH_OF_SCREEN;
		private int mPage;
		private int mType; // 1 ask 2 reply
		private int mSize = 15;

		private Listener<UserDetailResult> mListener;
		private ErrorListener mErrorListener;

		public Builder setSize(int size) {
			this.mSize = size;
			return this;
		}

		public Builder setType(int type) {
			this.mType = type;
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

		public Builder setListener(Listener<UserDetailResult> listener) {
			this.mListener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.mErrorListener = errorListener;
			return this;
		}

		public UserDetailRequest build() {
			String url = createUrl();
			UserDetailRequest request = new UserDetailRequest(METHOD, url,
					mListener, mErrorListener);
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL)
					.append("profile/view");
			sb.append("?uid=" + mUid);
			sb.append("&page=" + mPage);
			sb.append("&width=" + mWidth);
			sb.append("&size=" + mSize);
			sb.append("&type=" + mType);

			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}
	}

	public static class UserDetailResult {
		public User user;
		public List<PhotoItem> askPhotoItems;
		public List<PhotoItem> replyPhotoItems;

		public List<PhotoItem> getAskItems() {
			return askPhotoItems;
		}

		public List<PhotoItem> getWorkItems() {
			return replyPhotoItems;
		}

		public User getUserInfo() {
			return user;
		}
	}

}
