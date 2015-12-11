package com.psgod.network.request;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;
import com.psgod.model.User;
import com.psgod.network.request.MyFollowingListRequest.FollowingListWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MyFollowingListRequest extends BaseRequest<FollowingListWrapper> {
	private static final String TAG = MyFollowingListRequest.class
			.getSimpleName();

	public MyFollowingListRequest(int method, String url,
			Listener<FollowingListWrapper> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected FollowingListWrapper doParseNetworkResponse(JSONObject reponse)
			throws UnsupportedEncodingException, JSONException {
		JSONObject data = reponse.getJSONObject("data");
		JSONArray recommendFellows = data.getJSONArray("recommends");
		JSONArray myFellows = data.getJSONArray("fellows");
		int mTotalMasters = data.getInt("totalMasters");

		int recommendFellowsLength = recommendFellows.length();
		int myFellowsLength = myFellows.length();

		List<User> recommendFellowList = new ArrayList<User>(
				recommendFellowsLength);
		for (int i = 0; i < recommendFellowsLength; i++) {
			recommendFellowList.add(User.createUser(recommendFellows
					.getJSONObject(i)));
		}

		List<User> myFellowList = new ArrayList<User>(myFellowsLength);
		for (int n = 0; n < myFellowsLength; n++) {
			myFellowList.add(User.createUser(myFellows.getJSONObject(n)));
		}

		FollowingListWrapper followingListWrapper = new FollowingListWrapper();
		followingListWrapper.recommendUserList = recommendFellowList;
		followingListWrapper.myUserList = myFellowList;
		followingListWrapper.mTotalMasters = mTotalMasters;

		return followingListWrapper;
	}

	public static class Builder implements IGetRequestBuilder {
		public static final int TYPE_FELLOWS = 0;
		public static final int TYPE_INVITATION = 1;

		private long lastUpdated = -1;
		private int page;
		private int size = 15;
		private long askId;
		// 类型区分 0 为我的关注列表 1 为邀请来P的列表
		private int type = TYPE_FELLOWS;
		private Listener<FollowingListWrapper> listener;
		private ErrorListener errorListener;

		public Builder setAskId(long id) {
			this.askId = id;
			return this;
		}

		public Builder setType(int tp) {
			this.type = tp;
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

		public Builder setListener(Listener<FollowingListWrapper> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.errorListener = errorListener;
			return this;
		}

		public MyFollowingListRequest build() {
			String url = createUrl();
			MyFollowingListRequest request = new MyFollowingListRequest(METHOD,
					url, listener, errorListener);
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);
            boolean boo = false;
			if (type == TYPE_FELLOWS) {
				sb.append("profile/follows");
				if (lastUpdated != -1) {
					sb.append("?last_updated=").append(lastUpdated);
				}else{
                    sb.append("?page=").append(page);
                    boo = true;
				}
			}
			if (type == TYPE_INVITATION) {
				sb.append("profile/follows");
				sb.append("?ask_id=").append(askId);
				if (lastUpdated != -1) {
					sb.append("&last_updated=").append(lastUpdated);
				}
			}
            if(!boo) {
                sb.append("&page=").append(page);
            }
			sb.append("&size=").append(size);

			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}
	}

	public static class FollowingListWrapper {
		public List<User> recommendUserList;
		public List<User> myUserList;
		public int mTotalMasters;
	}
}
