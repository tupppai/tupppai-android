package com.psgod.network.request;

/**
 * 其他用户的 粉丝、关注请求
 * @author brandwang
 */

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;
import com.psgod.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class OthersFollowListRequest extends BaseRequest<List<User>> {
	private final static String TAG = OthersFollowListRequest.class
			.getSimpleName();

	public static final int REQUEST_OTHER_FANS = 0;
	public static final int REQUEST_OTHER_FOLLOWS = 1;

	private int RequestType;

	public OthersFollowListRequest(int method, String url,
			Listener<List<User>> listener, ErrorListener errorListener,
			int RequestType) {
		super(method, url, listener, errorListener);
		this.RequestType = RequestType;
	}

	@Override
	protected List<User> doParseNetworkResponse(JSONObject reponse)
			throws UnsupportedEncodingException, JSONException {
		JSONArray JsonUsers = null;

		if (RequestType == REQUEST_OTHER_FANS) {
			JsonUsers = reponse.getJSONArray("data");
		} else if (RequestType == REQUEST_OTHER_FOLLOWS) {
			JSONObject data = reponse.getJSONObject("data");
			JsonUsers = data.getJSONArray("fellows");
		}

		int length = JsonUsers.length();

		List<User> users = new ArrayList<User>(length);
		for (int i = 0; i < length; i++) {
			users.add(this.createUserFromFollower(JsonUsers.getJSONObject(i)));
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
			user.setFollowing(jsonObject.getInt("is_follow"));
			user.setFollowerCount(jsonObject.getInt("fans_count"));
			user.setFollowingCount(jsonObject.getInt("fellow_count"));
			user.setAskCount(jsonObject.getInt("ask_count"));
			user.setBackgroundUrl(null);
			user.setLikedCount(jsonObject.getInt("uped_count"));
			user.setReplyCount(jsonObject.getInt("reply_count"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return user;
	}

	public static class Builder implements IGetRequestBuilder {
		private long lastUpdated;
		private int page = 1;
		private int size = 15;
		// 列表类型 0 为粉丝列表 1为关注的列表
		private int list_type = 0;
		private long uid;
		private Listener<List<User>> listener;
		private ErrorListener errorListener;

		public Builder setUid(long id) {
			this.uid = id;
			return this;
		}

		public Builder setListType(int type) {
			this.list_type = type;
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

		public Builder setListener(Listener<List<User>> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.errorListener = errorListener;
			return this;
		}

		public OthersFollowListRequest build() {
			String url = createUrl();
			OthersFollowListRequest request = new OthersFollowListRequest(
					METHOD, url, listener, errorListener, list_type);
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);
			if (list_type == 0) {
				sb.append("profile/fans");
			}
			if (list_type == 1) {
				sb.append("profile/follows");
			}
			sb.append("?uid=").append(uid);
			sb.append("&page=").append(page);
			sb.append("&size=").append(size);
			if (lastUpdated != 0) {
				sb.append("&last_updated=").append(lastUpdated);
			}

			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}
	}
}
