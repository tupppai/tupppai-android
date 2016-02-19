package com.psgod;

import android.app.Activity;
import android.graphics.Bitmap.Config;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import java.util.List;

public class Constants {
	/**
	 * IS_DEV标志位 正式环境 false 测试开发环境 true
	 */
	public static final Boolean IS_DEV = true;

	public static final String APP_NAME = "PSGod";
	public static final String APP_ID = "wx86ff6f67a2b9b4b8";
	public static final String APP_SECRET = "c2da31fda3acf1c09c40ee25772b6ca5";
	public static final boolean DEBUG = true;

	public static int WIDTH_OF_SCREEN; // 手机屏幕宽度
	public static int HEIGHT_OF_SCREEN; // 手机屏幕高度
	public static final int MAX_LABEL_COUNT = 3;
	// public static boolean IS_LOGIN = false; //判断用户登录状态
	// public static final int PS_AVATAR_IMAGEVIEW_SIZE = 58;

	public static final boolean IS_COLOR_USER = true; // 是否染色用户
	public static final boolean LOG_TO_FILE = true; // 全局开关，是否把日志写到文件

	public static final String SIGN = "tupppaisignmd5";

	// 六种消息类型
	public static final int PUSH_MESSAGE_SYSTEM = 0;
	public static final int PUSH_MESSAGE_COMMENT = 1;
	public static final int PUSH_MESSAGE_REPLY = 2;
	public static final int PUSH_MESSAGE_FOLLOW = 3;
	// public static final int PUSH_MESSAGE_INVITE = 4;
	public static final int PUSH_MESSAGE_LIKE = 5;

	// 四种Fragment是否被创建过
	public static boolean IS_HOME_FRAGMENT_CREATED = false;
	public static boolean IS_FOCUS_FRAGMENT_CREATED = false;
	public static boolean IS_MESSAGE_FRAGMENT_CREATED = false;
	public static boolean IS_USER_FRAGMENT_CREATED = false;
	public static boolean IS_INPROGRESS_FRAGMENT_CREATED = false;

	// 标志位 是否关注过新的用户
	public static boolean IS_FOLLOW_NEW_USER = false;

	// 标志位 我的 右上角有消息推送后 是否点击过
	public static boolean IS_MESSAGE_NEW_PUSH_CLICK = false;

	// TODO
	// 当前其他用户id
	public static long CURRENT_OTHER_USER_ID;

	// 当前首页Tab 0 热门或 1 求P
	public static int CURRENT_HOMEPAGE_TAB = 0;
	// 当前最近页面Tab 活动 0 求p 1 作品 2
	public static int CURRENT_RECENTPAGE_TAB = 0;
	// 进行中Tab 求P 帮P 已完成
	public static int CURRENT_INPROGRESS_TAB = 0;
	// 教程Tab 详情 作业
	public static int CURRENT_COURSE_TAB = 0;
	// 我的Tab 图片 收藏
	public static int CURRENT_MY_TAB = 0;

	// 官方app名称
	public static String OFFICAL_APP_NAME = "图派";
	// 官网地址
	public static String OFFICAL_WEBSITE = "http://www.qiupsdashen.com";

	// UIL 图片加载默认配置
	public static final DisplayImageOptions DISPLAY_IMAGE_OPTIONS = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.ic_lietu)
			.showImageOnFail(R.drawable.ic_lietu)
			.showImageOnLoading(R.drawable.ic_zhanwei).cacheInMemory(true)
			.bitmapConfig(Config.RGB_565)
			.imageScaleType(ImageScaleType.IN_SAMPLE_INT).cacheOnDisk(true)
			.considerExifParams(true).displayer(new FadeInBitmapDisplayer(100))
			.displayer(new SimpleBitmapDisplayer()).build();

	// UIL 图片加载默认配置
	public static final DisplayImageOptions DISPLAY_IMAGE_OPTIONS_ORIGIN = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.ic_lietu)
			.showImageOnFail(R.drawable.ic_lietu)
			.showImageOnLoading(R.drawable.ic_zhanwei).cacheInMemory(true)
			.bitmapConfig(Config.RGB_565)
			.imageScaleType(ImageScaleType.IN_SAMPLE_INT).cacheOnDisk(true)
			.considerExifParams(true).displayer(new FadeInBitmapDisplayer(100))
			.displayer(new SimpleBitmapDisplayer()).build();


	// UIL 图片加载默认配置
	public static final DisplayImageOptions DISPLAY_IMAGE_OPTIONS_LOCAL = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.ic_lietu)
			.showImageOnFail(R.drawable.ic_lietu)
			.showImageOnLoading(R.drawable.ic_zhanwei).cacheInMemory(true)
			.bitmapConfig(Config.RGB_565)
			.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
			.considerExifParams(true).displayer(new FadeInBitmapDisplayer(100))
			.displayer(new SimpleBitmapDisplayer()).build();

	// UIL 图片加载默认配置 （小）
	public static final DisplayImageOptions DISPLAY_IMAGE_OPTIONS_SMALL = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.ic_zhanwei_small)
			.showImageOnFail(R.drawable.ic_zhanwei_small)
			.showImageOnLoading(R.drawable.ic_zhanwei_small)
			.cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
			.bitmapConfig(Config.RGB_565)
			.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
			.displayer(new FadeInBitmapDisplayer(100))
			.displayer(new SimpleBitmapDisplayer()).build();

	// UIL 图片加载默认配置 （超小）
	public static final DisplayImageOptions DISPLAY_IMAGE_OPTIONS_SMALL_SMALL = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.ic_zhanwei_small)
			.showImageOnFail(R.drawable.ic_zhanwei_small)
			.showImageOnLoading(R.drawable.ic_zhanwei_small)
			.cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
			.bitmapConfig(Config.RGB_565)
			.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
			.displayer(new FadeInBitmapDisplayer(100))
			.displayer(new SimpleBitmapDisplayer()).build();

	// UIL头像默认配置
	public static final DisplayImageOptions DISPLAY_IMAGE_OPTIONS_AVATAR = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.head_portrait)
			.showImageOnFail(R.drawable.head_portrait)
			.showImageOnLoading(R.drawable.head_portrait).cacheInMemory(true)
			.bitmapConfig(Config.RGB_565)
			.imageScaleType(ImageScaleType.IN_SAMPLE_INT).cacheOnDisk(true)
			.considerExifParams(true).displayer(new SimpleBitmapDisplayer())
			.build();

	// UIL Banner默认配置
	public static final DisplayImageOptions DISPLAY_BANNER_OPTIONS = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.banner_zhanwei)
			.showImageOnFail(R.drawable.banner_zhanwei)
			.showImageOnLoading(R.drawable.banner_zhanwei).cacheInMemory(true)
			.bitmapConfig(Config.RGB_565)
			.imageScaleType(ImageScaleType.IN_SAMPLE_INT).cacheOnDisk(true)
			.considerExifParams(true).displayer(new SimpleBitmapDisplayer())
			.build();

	/**
	 * 目录名称
	 */
	public static final String DIR_AVATAR = "avatar"; // 头像的保存地址
	public static final String DIR_SAVED_IMAGE = "images"; // 素材保存地址

	public static interface ThirdAuthInfo {
		String THIRD_AUTH_PLATFORM = "ThirdAuthPlatform";
		String USER_OPENID = "UserOpenId";
		String USER_NICKNAME = "UserNickname";
		String USER_GENDER = "UserGender";
		String USER_PROVINCE = "UserProvince";
		String USER_CITY = "UserCity";
		String USER_AVATAR = "UserAvatar";
	}

	public static interface IntentKey {
		String PHOTO_ITEM_TYPE = "PhotoItemType";
		String USER_ID = "UserId";
		String USER_INFO = "UserInfo";
		String USER_NICKNAME = "UserNickname";
		String ASK_ID = "AskId";
		String REPLY_ID = "ReplyId";
		String PHOTO_ITEM = "PhotoItem";
		String PHOTO_ITEM_ID = "PhotoItemId";
		String PHOTO_PATH = "PhotoPath";
		String UPLOAD_IMAGE_PATH = "UploadImagePath";
		String UPLOAD_IMAGE = "UploadImage";
		String START_UPLOAD_IMAGE_ACTIVITY_FROM = "StartUploadImageActivityFrom";
		String START_SET_AVATAR_ACTIVITY_FROM = "StartSetAvatarActivityFrom";
		String UPLOAD_IMAGE_ACTIVITY_TYPE = "UploadImageActivityType";
		String REGISTER_DATA = "RegisterData";
		String NOTIFICATION_LIST_TYPE = "NotificationListType";
		String ACTIVITY_JUMP_FROM = "ActivityJumpFrom";
		String DEST_ACTIVITY_NAME = "DestActivityName";
		String IS_FINISH_ACTIVITY = "IsFinishActivity";
		String IS_SETTING_CHANGE_CLICK = "IsSettingChangeClick";
	}

	public static interface SharedPreferencesKey {
		String NAME = "PSGod";
		String HOT_PHOTO_LIST_LAST_REFRESH_TIME = "HotPhotoListLastRefreshTime";
		String RECENT_PHOTO_LIST_LAST_REFRESH_TIME = "RecentPhotoListLastRefreshTime";
		String CHANNEL_LIST_LAST_REFRESH_TIME = "ChannelListLastRefreshTime";
		String COURSE_LIST_LAST_REFRESH_TIME = "CourseListLastRefreshTime";
		String FOCUS_PHOTO_LIST_LAST_REFRESH_TIME = "FocusPhotoListLastResfreshTime";
		String PHOTO_DETAIL_LIST_LAST_REFRESH_TIME = "PhotoDetailListLastRefreshTime";
		String RECENT_PHOTO_DETAIL_LIST_LAST_REFRESH_TIME = "RecentPhotoDetailListLastRefreshTime";
		String MY_ASK_PHOTO_LIST_LAST_REFRESH_TIME = "MyAskPhotoListLastRefreshTime";
		String MY_WORK_PHOTO_LIST_LAST_REFRESH_TIMEStr = "MyWorkPhotoListLastRefreshTime";
		String MY_INPROGRESS_PHOTO_LIST_LAST_REFRESH_TIME = "MyInprogressListLastRefreshTIme";
		String MY_COLLECTION_PHOTO_LIST_LAST_REFRESH_TIME = "MyCollectionListLastRefreshTime";
		String MY_FOCUS_PHOTO_LIST_LAST_REFRESH_TIME = "MyFocusPhotoListLastRefreshTime";
		String FOLLOWER_LIST_LAST_REFRESH_TIME = "FollowerListLastRefreshTime";
		String RECOMMEND_FOCUS_LIST_LAST_REFRESH_TIME = "RecommendFocusListLastRefreshTime";
		String OTHERS_FOLLOWER_LIST_LAST_REFRESH_TIME = "OthersFollowerListLastRefreshTime";
		String OTHERS_FOLLOWING_LIST_LAST_REFRESH_TIME = "OthersFollowingListLastRefreshTime";
		String INVITATION_LIST_LAST_REFRESH_TIME = "InvitationListLastRefreshTime";
		String INPROGRESS_ASK_LIST_LAST_REFRESH_TIME = "InprogressAskListLastRefreshTime";
		String INPROGRESS_REPLY_LIST_LAST_REFRESH_TIME = "InprogressReplyListLastRefreshTime";
		String INPROGRESS_COMPLETE_LIST_LAST_REFRESH_TIME = "InprogressCompleteListLastRefreshTime";
		String MY_ASK_LIST_LAST_REFRESH_TIME = "MyAskListLastRefreshTime";
		String MY_REPLY_LIST_LAST_REFRESH_TIME = "MyReplyListLastRefreshTime";
		String MY_COLLECTION_LIST_LAST_REFRESH_TIME = "MyCollectionListLastRefreshTime";
		String IS_FIRST_RUN = "IsFirstRun";
		String MY_MESSAGE_LIST_LAST_REFRESH_TIME = "MyMessageListLastRefreshTime";
		String SETTING_LIKED_LIST_LAST_REFRESH_TIME = "SettingLikedListLastRefreshTime";
		String SETTING_COMMEND_LIST_LAST_REFRESH_TIME = "SettingCommendListLastRefreshTime";
		String MESSAGE_LIKE_LIST_LAST_REFRESH_TIME = "MessageLikeListLastRefreshTime";
		String MESSAGE_SYSTEM_LIST_LAST_REFRESH_TIME = "MessageSystemListLastRefreshTime";
		// public static final String HOT_PHOTO_LIST_LAST_REFRESH_TIME =
		// "HotPhotoListLastRefreshTime";
	}

	public static interface Color {
		int HEAD_EXPANDABLE_LIST_VIEW_RED = android.graphics.Color
				.parseColor("#FFEE0149");
		int HEAD_EXPANDABLE_LIST_VIEW_BLUE = android.graphics.Color
				.parseColor("#FF00ADEF");
		int HEAD_EXPANDABLE_LIST_BACKGROUND = android.graphics.Color
				.parseColor("#FFFFFFFF");
	}

	// 登录时的activityList 用于关闭登录所有的activity
	public static List<Activity> activityList = null;
}
