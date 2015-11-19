package com.psgod;

/**
 * 用户设置 (推送信息设置)
 * 
 * @author Rayal
 * 
 */
public final class UserSetting {
	private static final String TAG = UserSetting.class.getSimpleName();
	private static UserSetting mInstance = null;

	private boolean mIsReceivedCommentNotification;
	private boolean mIsReceivedReplyNotification;
	private boolean mIsReceivedFollowNotification;
	// private boolean mIsReceivedInviteNotification;
	private boolean mIsReceivedSystemNotification;
	private boolean mIsReceivedLikeNotification;

	private UserSetting() {
		// 读取SP，初始化
		mIsReceivedCommentNotification = UserPreferences.Notification
				.getIsNotificationReceived(UserPreferences.Notification.COMMENT);
		mIsReceivedReplyNotification = UserPreferences.Notification
				.getIsNotificationReceived(UserPreferences.Notification.REPLY);
		mIsReceivedFollowNotification = UserPreferences.Notification
				.getIsNotificationReceived(UserPreferences.Notification.FOLLOW);
		// mIsReceivedInviteNotification =
		// UserPreferences.Notification.getIsNotificationReceived(UserPreferences.Notification.INVITE);
		mIsReceivedSystemNotification = UserPreferences.Notification
				.getIsNotificationReceived(UserPreferences.Notification.SYSTEM);
		mIsReceivedLikeNotification = UserPreferences.Notification
				.getIsNotificationReceived(UserPreferences.Notification.LIKE);
	}

	public static synchronized UserSetting getInstance() {
		if (mInstance == null) {
			mInstance = new UserSetting();
		}
		return mInstance;
	}

	public boolean isReceivedCommentNotification() {
		return mIsReceivedCommentNotification;
	}

	public void setReceivedCommentNotification(
			boolean isReceivedCommentNotification) {
		mIsReceivedCommentNotification = isReceivedCommentNotification;
		UserPreferences.Notification.setIsNotificationReceived(
				UserPreferences.Notification.COMMENT,
				mIsReceivedCommentNotification);
	}

	public boolean isReceivedReplyNotification() {
		return mIsReceivedReplyNotification;
	}

	public void setReceivedReplyNotification(boolean isReceivedReplyNotification) {
		mIsReceivedReplyNotification = isReceivedReplyNotification;
		UserPreferences.Notification.setIsNotificationReceived(
				UserPreferences.Notification.REPLY,
				mIsReceivedReplyNotification);
	}

	public boolean isReceivedFollowNotification() {
		return mIsReceivedFollowNotification;
	}

	public void setReceivedFollowNotification(
			boolean isReceivedFollowNotification) {
		mIsReceivedFollowNotification = isReceivedFollowNotification;
		UserPreferences.Notification.setIsNotificationReceived(
				UserPreferences.Notification.FOLLOW,
				mIsReceivedFollowNotification);
	}

	public boolean isReceivedLikeNotification() {
		return mIsReceivedLikeNotification;
	}

	public void setReceivedLikeNotification(boolean isReceivedLikeNotification) {
		mIsReceivedLikeNotification = isReceivedLikeNotification;
		UserPreferences.Notification.setIsNotificationReceived(
				UserPreferences.Notification.LIKE, mIsReceivedLikeNotification);
	}

	// public boolean isReceivedInviteNotification() {
	// return mIsReceivedInviteNotification;
	// }

	// public void setReceivedInviteNotification(boolean
	// isReceivedInviteNotification) {
	// mIsReceivedInviteNotification = isReceivedInviteNotification;
	// UserPreferences.Notification.setIsNotificationReceived(UserPreferences.Notification.INVITE,
	// mIsReceivedInviteNotification);
	// }

	public boolean isReceivedSystemNotification() {
		return mIsReceivedSystemNotification;
	}

	public void setReceivedSystemNotification(
			boolean isReceivedSystemNotification) {
		mIsReceivedSystemNotification = isReceivedSystemNotification;
		UserPreferences.Notification.setIsNotificationReceived(
				UserPreferences.Notification.SYSTEM,
				mIsReceivedSystemNotification);
	}
}
