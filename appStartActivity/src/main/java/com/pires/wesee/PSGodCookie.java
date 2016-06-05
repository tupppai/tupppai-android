package com.pires.wesee;

/**
 * Cookie相关类
 * 
 * @author brandwang
 */

public class PSGodCookie {
	private static String mToken = null;
	private static String mUid = null;

	// 设置token
	public void setToken(String token) {
		PSGodCookie.mToken = token;
		// TODO token写入sp和db
	}

	// 获得token
	public static String getToken() {
		return mToken;
	}

	// 设置uid
	public void setUid(String uid) {
		PSGodCookie.mUid = uid;
	}

	// 获得uid
	public static String getUid() {
		return mUid;
	}
}
