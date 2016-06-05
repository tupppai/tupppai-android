package com.pires.wesee;

/**
 * SharedPreferences操作类
 * 
 */
import android.content.Context;
import android.content.SharedPreferences;

public final class UserPreferences {
	public static final String SHARED_PREFERENCES_NAME = "PSGod";

	// 存取token相关 SharedPreferences操作
	public static final class TokenVerify {
		// 更新token
		public static void setToken(String mtoken) {
			SharedPreferences.Editor editor = PSGodApplication
					.getAppContext()
					.getSharedPreferences(SHARED_PREFERENCES_NAME,
							Context.MODE_PRIVATE).edit();
			editor.putString("psgod_token", mtoken);
			editor.commit();
		}

		// 获取token
		public static String getToken() {
			SharedPreferences sp = PSGodApplication.getAppContext()
					.getSharedPreferences(SHARED_PREFERENCES_NAME,
							Context.MODE_PRIVATE);
			String token = sp.getString("psgod_token", "");
			return token;
		}
	}

	// 设置推送各类推送消息及条数sp
	public static final class PushMessage {
		public static final String PUSH_COMMENT = "PushComment";
		public static final String PUSH_REPLY = "PushReply";
		public static final String PUSH_FOLLOW = "PushFollow";
//		public static final String PUSH_INVITE = "PushInvite";
		public static final String PUSH_SYSTEM = "PushSystem";
		public static final String PUSH_LIKE = "PushLike";

		// 设置未读消息的数量
		public static void setPushMessageCount(String key, int count) {
			SharedPreferences.Editor editor = PSGodApplication
					.getAppContext()
					.getSharedPreferences(SHARED_PREFERENCES_NAME,
							Context.MODE_PRIVATE).edit();
			editor.putInt(key, count);
			editor.commit();
		}

		// 获取某类消息的未读数量
		public static int getPushMessageCount(String key) {
			SharedPreferences sp = PSGodApplication.getAppContext()
					.getSharedPreferences(SHARED_PREFERENCES_NAME,
							Context.MODE_PRIVATE);
			int count = sp.getInt(key, 0);
			return count;
		}
	}

	// 设置是否接收某种推送消息sp
	public static final class Notification {
		public static final String COMMENT = "CommentNotification";
		public static final String REPLY = "ReplyNotification";
		public static final String FOLLOW = "FollowNotfication";
//		public static final String INVITE = "InviteNotification";
		public static final String SYSTEM = "SystemNotification";
		public static final String CHAT = "ChatNotification";
		public static final String LIKE = "LikeNotification";

		public static void setIsNotificationReceived(String key,
				boolean isReceived) {
			SharedPreferences.Editor editor = PSGodApplication
					.getAppContext()
					.getSharedPreferences(SHARED_PREFERENCES_NAME,
							Context.MODE_PRIVATE).edit();
			editor.putBoolean(key, isReceived);
			editor.commit();
		}

		public static boolean getIsNotificationReceived(String key) {
			SharedPreferences sp = PSGodApplication.getAppContext()
					.getSharedPreferences(SHARED_PREFERENCES_NAME,
							Context.MODE_PRIVATE);
			boolean isReceived = sp.getBoolean(key, true);
			return isReceived;
		}
	}

}
