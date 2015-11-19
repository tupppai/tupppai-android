package com.psgod.network.request;

/**
 * 推荐关注大神 请求
 */
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;
import com.psgod.model.User;

public class RecommendFollowRequest extends BaseRequest<List<User>> {
	private static final String TAG = RecommendFollowRequest.class
			.getSimpleName();

	public RecommendFollowRequest(int method, String url,
			Listener<List<User>> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected List<User> doParseNetworkResponse(JSONObject reponse)
			throws UnsupportedEncodingException, JSONException {
		JSONArray data = reponse.getJSONArray("data");
		int length = data.length();

		List<User> users = new ArrayList<User>(length);
		for (int i = 0; i < length; i++) {
			users.add(this.createUserFromFollower(data.getJSONObject(i)));
		}

		return users;
	}

	private User createUserFromFollower(JSONObject jsonObject) {
		User user = new User();

		try {
			user.setUid(jsonObject.getLong("uid"));
			user.setAvatarImageUrl(jsonObject.getString("avatar"));
			user.setNickname(jsonObject.getString("nickname"));
			user.setGender(jsonObject.getInt("sex"));
			user.setFollowed(jsonObject.getInt("is_fan"));
			user.setFollowing(jsonObject.getInt("is_follow"));
			user.setFollowerCount(jsonObject.getInt("fans_count"));
			user.setFollowingCount(jsonObject.getInt("fellow_count"));
			user.setAskCount(jsonObject.getInt("ask_count"));
			user.setLikedCount(jsonObject.getInt("uped_count"));
			user.setReplyCount(jsonObject.getInt("reply_count"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return user;
	}

	public static class Builder implements IGetRequestBuilder {
		private long lastUpdated = -1;
		private int page = 1;
		private int size = 15;
		private Listener<List<User>> listener;
		private ErrorListener errorListener;

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

		public Builder setListener(Listener<List<User>> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.errorListener = errorListener;
			return this;
		}

		public RecommendFollowRequest build() {
			String url = createUrl();
			RecommendFollowRequest request = new RecommendFollowRequest(METHOD,
					url, listener, errorListener);
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL)
					.append("profile/get_masters");
			sb.append("?page=").append(page);
			sb.append("&size=").append(size);
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
