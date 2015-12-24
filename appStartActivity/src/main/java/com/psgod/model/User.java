package com.psgod.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

public class User {
	protected long mUid;
	protected String mAvatarImageUrl;
	protected String mNickname;
	protected int mGender;
	protected String mBackgroundUrl;
	protected int mProvinceId;
	protected int mCityId;
	protected int mAskCount;
	protected int mReplyCount;
	protected int mFollowerCount;
	protected int mFollowingCount;
	protected int mLikedCount;
	protected boolean mIsStar ;

	// 是否被邀请过 用于邀请相关页面
	protected Boolean mIsInvited;
	// 0 否 1 是
	// 被关注 粉丝
	protected int mIsFollowed;
	// 主动关注 关注
	protected int mIsFollowing;

	public static User createUser(JSONObject jsonObject) throws JSONException {
		User user = new User();
		user.mUid = jsonObject.getLong("uid");
		user.mAvatarImageUrl = jsonObject.getString("avatar");
		user.mNickname = jsonObject.getString("nickname");
		user.mGender = jsonObject.getInt("sex");
		user.mFollowerCount = jsonObject.getInt("fans_count");
		user.mFollowingCount = jsonObject.getInt("fellow_count");
		// 是否被关注
		user.mIsFollowed = jsonObject.getInt("is_fan");
		// 是否关注
		user.mIsFollowing = jsonObject.getInt("is_follow");
		user.mAskCount = jsonObject.getInt("ask_count");
		user.mReplyCount = jsonObject.getInt("reply_count");
		user.mLikedCount = jsonObject.getInt("uped_count");
		if (jsonObject.has("has_invited")) {
			user.mIsInvited = jsonObject.getBoolean("has_invited");
		} else {
			user.mIsInvited = false;
		}

		if(jsonObject.has("is_star")){
			user.mIsStar = jsonObject.getBoolean("is_star");
		}

		return user;
	}

	public boolean isStar() {
		return mIsStar;
	}

	public void setIsStar(boolean mIsStar) {
		this.mIsStar = mIsStar;
	}

	public Boolean getIsInvited() {
		return mIsInvited;
	}

	public void setIsInvited(Boolean isinvited) {
		this.mIsInvited = isinvited;
	}

	public long getUid() {
		return mUid;
	}

	public void setUid(long uid) {
		this.mUid = uid;
	}

	public String getAvatarImageUrl() {
		return mAvatarImageUrl;
	}

	public void setAvatarImageUrl(String avatarImageUrl) {
		this.mAvatarImageUrl = avatarImageUrl;
	}

	public String getNickname() {
		return mNickname;
	}

	public void setNickname(String nickname) {
		this.mNickname = nickname;
	}

	public int getGender() {
		return mGender;
	}

	public void setGender(int gender) {
		this.mGender = gender;
	}

	public String getBackgroundUrl() {
		return mBackgroundUrl;
	}

	public void setBackgroundUrl(String backgroundUrl) {
		this.mBackgroundUrl = backgroundUrl;
	}

	public int getCityId() {
		return mCityId;
	}

	public int getProvinceId() {
		return mProvinceId;
	}

	public void setCityId(int cityId) {
		this.mCityId = cityId;
	}

	public void setProvinceId(int provinceId) {
		this.mProvinceId = provinceId;
	}

	public int getAskCount() {
		return mAskCount;
	}

	public void setAskCount(int askCount) {
		this.mAskCount = askCount;
	}

	public int getReplyCount() {
		return mReplyCount;
	}

	public void setReplyCount(int replyCount) {
		this.mReplyCount = replyCount;
	}

	public int getFollowerCount() {
		return mFollowerCount;
	}

	public void setFollowerCount(int followerCount) {
		this.mFollowerCount = followerCount;
	}

	public int getFollowingCount() {
		return mFollowingCount;
	}

	public void setFollowingCount(int followingCount) {
		this.mFollowingCount = followingCount;
	}

	public int getLikedCount() {
		return mLikedCount;
	}

	public void setLikedCount(int likedCount) {
		this.mLikedCount = likedCount;
	}

	public int isFollowed() {
		return mIsFollowed;
	}

	public void setFollowed(int isFollowed) {
		this.mIsFollowed = isFollowed;
	}

	public int isFollowing() {
		return mIsFollowing;
	}

	public void setFollowing(int isFollowing) {
		this.mIsFollowing = isFollowing;
	}

	public User(Object object){
		super();

		Field[] fields = object.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			fields[i].setAccessible(true);
			switch (fields[i].getName()){
				case "mUid":
				case "uid":
//					mUid = fields[i].get
			}
//			if (fields[i].getName().equals())
//			fields[i].set(bugMy, new BugMy());
//			System.out.println(fields[i].get(bugMy));
		}

	}

	public User(){
		super();
	}
}
