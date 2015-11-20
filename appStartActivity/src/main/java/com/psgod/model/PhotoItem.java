package com.psgod.model;

import java.io.Serializable;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.psgod.Logger;
import com.psgod.ThreadManager;
import com.psgod.db.CommentDao;

@DatabaseTable(tableName = "photo_item")
public class PhotoItem implements Serializable, Cloneable {
	// 求P
	public static final byte TYPE_ASK = 1;
	// 回复
	public static final byte TYPE_REPLY = 2;

	// public static final int FROM_HOT = 0;
	// public static final int FROM_RECENT = 1;
	// public static final int FROM_FOCUS = 2;

	public static final int TYPE_HOME_FOCUS = 0;
	public static final int TYPE_HOME_HOT = 1;
	public static final int TYPE_RECENT_ASK = 2;
	public static final int TYPE_RECENT_WORK = 3;
	public static final int TYPE_RECENT_ACT = 4;

	@DatabaseField(id = true, columnName = "pid")
	private long mPid;
	@DatabaseField(columnName = "uid")
	private long mUid;
	@DatabaseField(columnName = "nickname")
	private String mNickname;
	@DatabaseField(columnName = "update_time")
	private long mUpdateTime;
	private String mUpdateTimeStr = null; // 视图上显示的创建时间，不需要存DB
	@DatabaseField(columnName = "avatar_url")
	private String mAvatarURL;
	// 0: 女 1: 男
	@DatabaseField(columnName = "gender")
	private int mGender;
	// 照片是否已经被下载过
	@DatabaseField(columnName = "is_downloaded")
	private boolean mIsDownloaded;
	// 照片是否已经点赞
	@DatabaseField(columnName = "is_liked")
	private boolean mIsLiked;
	// 我是否关注了这个人
	// @DatabaseField(columnName="is_followed")
	private boolean mIsFollowed;
	// 是否被收藏
	@DatabaseField(columnName = "is_collected")
	private boolean mIsCollected;
	@DatabaseField(columnName = "image_url")
	private String mImageURL;
	@DatabaseField(columnName = "image_width")
	private int mImageWidth;
	@DatabaseField(columnName = "image_height")
	private int mImageHeight;
	// 点赞数
	@DatabaseField(columnName = "like_count")
	private int mLikeCount;
	// 评论数
	@DatabaseField(columnName = "comment_count")
	private int mCommentCount;
	// 总分享数
	@DatabaseField(columnName = "share_count")
	private int mShareCount;
	// 微信分享数
	@DatabaseField(columnName = "weixing_share_count")
	private int mWeixingShareCount;
	// 回复作品数
	@DatabaseField(columnName = "reply_count")
	private int mReplyCount;
	// 收藏数量
	@DatabaseField(columnName = "collect_count")
	private int mCollectCount;
	// 图片描述
	@DatabaseField(columnName = "desc")
	private String mDesc;

	// 作品类型 TYPE_ASK | TYPE_REPLY
	@DatabaseField(columnName = "type")
	private int mType;

	@DatabaseField(columnName = "from")
	private int mFrom;

	// 评论内容 用于我评论过的页面
	private String mCommentContent;

	public void setType(int type) {
		this.mType = type;
	}

	// TODO
	// 求p的ask_id
	@DatabaseField(columnName = "ask_id")
	private long mAskId;

	// 关注动态中 求P的id
	@DatabaseField(columnName = "reply_id")
	private long mReplyId;

	// TODO 数据库问题
	private List<Comment> mHotCommentList = new ArrayList<Comment>();
	private List<Comment> mCommentList = new ArrayList<Comment>();
	// 回复作品信息（若有）
	private List<PhotoItem> mReplyPhotoItemsList = new ArrayList<PhotoItem>();

	// 原始求P图片
	private List<ImageData> mUploadImages = new ArrayList<ImageData>();

	public PhotoItem() {
		// needed by ormlite
	}

	public static PhotoItem createPhotoItem(JSONObject jsonObj)
			throws JSONException {
		PhotoItem item = new PhotoItem();
		if (jsonObj.has("id")) {
			item.mPid = jsonObj.getLong("id");
		}
		item.mUid = jsonObj.getLong("uid");
		item.mNickname = jsonObj.getString("nickname");
		item.mAvatarURL = jsonObj.getString("avatar");
		item.mGender = jsonObj.getInt("sex");
		if (jsonObj.has("is_download")) {
			item.mIsDownloaded = jsonObj.getBoolean("is_download");
		}
		if (jsonObj.has("collected")) {
			item.mIsCollected = jsonObj.getBoolean("collected");
		}
		item.mIsFollowed = jsonObj.getBoolean("is_follow");

		item.mIsLiked = jsonObj.getBoolean("uped");
		item.mImageURL = jsonObj.getString("image_url");
		item.mImageWidth = jsonObj.getInt("image_width");
		item.mImageHeight = jsonObj.getInt("image_height");
		if (jsonObj.has("desc")) {
			item.mDesc = jsonObj.getString("desc");
		}
		if (jsonObj.has("content")) {
			item.mCommentContent = jsonObj.getString("content");
		}

		try {
			item.mLikeCount = jsonObj.getInt("up_count");
		} catch (Exception e) {
			item.mLikeCount = 0;
		}

		item.mCommentCount = jsonObj.getInt("comment_count");
		item.mShareCount = jsonObj.getInt("share_count");
		item.mWeixingShareCount = jsonObj.getInt("weixin_share_count");
		item.mCollectCount = jsonObj.getInt("collect_count");

		if (jsonObj.has("reply_count")) {
			item.mReplyCount = jsonObj.getInt("reply_count");
		} else {
			item.mReplyCount = 0;
		}

		item.mUpdateTime = jsonObj.getLong("create_time");
		// photoItem 是求P还是回复
		item.mType = jsonObj.getInt("type");

		if (jsonObj.has("ask_id")) {
			item.mAskId = jsonObj.getLong("ask_id");
		}

		if (jsonObj.has("reply_id")) {
			item.mReplyId = jsonObj.getLong("reply_id");
		}

		// 获取原图
		if (jsonObj.has("ask_uploads")) {
			JSONArray uploadArray = jsonObj.getJSONArray("ask_uploads");
			int uploadSize = uploadArray.length();

			if (uploadSize > 0) {
				for (int i = 0; i < uploadSize; i++) {
					JSONObject uploadImage = uploadArray.getJSONObject(i);
					item.mUploadImages.add(new ImageData(uploadImage
							.getInt("image_width"), uploadImage
							.getInt("image_height"), uploadImage
							.getString("image_url")));
				}
			}
		}

		if (jsonObj.has("hot_comments")) {
			JSONArray hotComments = jsonObj.getJSONArray("hot_comments");
			int sizeOfHotComments = hotComments.length();

			if (sizeOfHotComments > 0) {
				for (int ix = 0; ix < sizeOfHotComments; ++ix) {
					JSONObject comment = hotComments.getJSONObject(ix);
					item.mHotCommentList.add(Comment.createComment(comment));
				}
			}
		}

		if (jsonObj.has("replies")) {
			JSONArray replyItems = jsonObj.getJSONArray("replies");
			int sizeOfReplyItems = replyItems.length();

			if (sizeOfReplyItems > 0) {
				for (int n = 0; n < sizeOfReplyItems; n++) {
					JSONObject photoItem = replyItems.getJSONObject(n);
					item.mReplyPhotoItemsList.add(PhotoItem
							.createPhotoItem(photoItem));
				}
			}
		}

		return item;
	}

	// 获取实例的克隆
	@Override
	public PhotoItem clone() {
		PhotoItem photoItem = null;
		try {
			photoItem = (PhotoItem) super.clone();
		} catch (Exception e) {
		}
		return photoItem;
	}

	/**
	 * 获取热门评论列表
	 * 
	 * @return
	 */
	public List<Comment> getHotCommentList() {
		return mHotCommentList;
	}

	public String getDesc() {
		return mDesc;
	}

	public List<ImageData> getUploadImagesList() {
		return mUploadImages;
	}

	public List<Comment> getCommentList() {
		return mCommentList;
	}

	public List<PhotoItem> getReplyItems() {
		return mReplyPhotoItemsList;
	}

	public long getPid() {
		return mPid;
	}

	public long getUid() {
		return mUid;
	}

	public int getType() {
		return mType;
	}

	public String getNickname() {
		return mNickname;
	}

	public String getAvatarURL() {
		return mAvatarURL;
	}

	public Long getCreatedTime() {
		return mUpdateTime;
	}

	public String getCommentContent() {
		return mCommentContent;
	}

	public static String toLocalTime(String unix) {
		// Long timestamp = Long.parseLong(unix) * 1000;
		String date = new SimpleDateFormat("MM月dd日 HH:mm")
				.format(new java.util.Date(Long.parseLong(unix + "000")));
		return date;
	}

	public String getUpdateTimeStr() {
		if (TextUtils.isEmpty(mUpdateTimeStr)) {
			StringBuffer sb = new StringBuffer();
			long time = System.currentTimeMillis() - (mUpdateTime * 1000);
			long mill = (long) Math.ceil(time / 1000);// 秒前
			long minute = (long) Math.ceil(time / 60 / 1000.0f);// 分钟前
			long hour = (long) Math.ceil(time / 60 / 60 / 1000.0f);// 小时
			long day = (long) Math.ceil(time / 24 / 60 / 60 / 1000.0f);// 天前

			if (day - 1 > 0) {
				if (day > 7) {
					sb.append(toLocalTime(Long.toString(mUpdateTime)));
				} else {
					sb.append(day + "天");
				}
			} else if (hour - 1 > 0) {
				if (hour >= 24) {
					sb.append("1天");
				} else {
					sb.append(hour + "小时");
				}
			} else if (minute - 1 > 0) {
				if (minute == 60) {
					sb.append("1小时");
				} else {
					sb.append(minute + "分钟");
				}
			} else if (mill - 1 > 0) {
				if (mill == 60) {
					sb.append("1分钟");
				} else {
					sb.append(mill + "秒");
				}
			} else {
				sb.append("刚刚");
			}
			if (!sb.toString().equals("刚刚") && (day <= 7)) {
				sb.append("前");
			}
			mUpdateTimeStr = sb.toString();
		}
		return mUpdateTimeStr;
	}

	public static String getUpdateTimeStr(Long updataTime) {
		StringBuffer sb = new StringBuffer();
		long time = System.currentTimeMillis() - (updataTime * 1000);
		long mill = (long) Math.ceil(time / 1000);// 秒前
		long minute = (long) Math.ceil(time / 60 / 1000.0f);// 分钟前
		long hour = (long) Math.ceil(time / 60 / 60 / 1000.0f);// 小时
		long day = (long) Math.ceil(time / 24 / 60 / 60 / 1000.0f);// 天前

		if (day - 1 > 0) {
			if (day > 7) {
				sb.append(toLocalTime(Long.toString(updataTime)));
			} else {
				sb.append(day + "天");
			}
		} else if (hour - 1 > 0) {
			if (hour >= 24) {
				sb.append("1天");
			} else {
				sb.append(hour + "小时");
			}
		} else if (minute - 1 > 0) {
			if (minute == 60) {
				sb.append("1小时");
			} else {
				sb.append(minute + "分钟");
			}
		} else if (mill - 1 > 0) {
			if (mill == 60) {
				sb.append("1分钟");
			} else {
				sb.append(mill + "秒");
			}
		} else {
			sb.append("刚刚");
		}
		if (!sb.toString().equals("刚刚") && (day <= 7)) {
			sb.append("前");
		}
		return sb.toString();
	}

	/**
	 * 获取用户的性别 0: 女 1: 男
	 * 
	 * @return
	 */
	public int getGender() {
		return mGender;
	}

	public boolean isDownloaded() {
		return mIsDownloaded;
	}

	public void setIsDownloaded(boolean isDownloaded) {
		this.mIsDownloaded = isDownloaded;
	}

	public boolean isLiked() {
		return mIsLiked;
	}

	public void setIsLiked(boolean isLiked) {
		mIsLiked = isLiked;
	}

	public boolean isCollected() {
		return mIsCollected;
	}

	public void setIsCollected(boolean collected) {
		mIsCollected = collected;
	}

	public boolean isFollowed() {
		return mIsFollowed;
	}

	public void setIsFollowed(boolean followed) {
		mIsFollowed = followed;
	}

	public void setDesc(String desc) {
		mDesc = desc;
	}

	public void setUploadImages(List<ImageData> images) {
		this.mUploadImages = images;
	}

	public String getImageURL() {
		return mImageURL;
	}

	public void setImageURL(String imageURL) {
		this.mImageURL = imageURL;
	}

	public int getImageWidth() {
		return mImageWidth;
	}

	public void setImageWidth(int width) {
		this.mImageWidth = width;
	}

	public int getImageHeight() {
		return mImageHeight;
	}

	public void setImageHeight(int height) {
		this.mImageHeight = height;
	}

	public int getLikeCount() {
		return mLikeCount;
	}

	public void setLikeCount(int likeCount) {
		this.mLikeCount = likeCount;
	}

	public int getCommentCount() {
		return mCommentCount;
	}

	public void setCommentCount(int commentCount) {
		this.mCommentCount = commentCount;
	}

	public int getCollectCount() {
		return mCollectCount;
	}

	public void setCollectCount(int collectCount) {
		this.mCollectCount = collectCount;
	}

	public int getShareCount() {
		return mShareCount;
	}

	public void setShareCount(int shareCount) {
		this.mShareCount = shareCount;
	}

	public int getReplyCount() {
		return this.mReplyCount;
	}

	public void setFrom(int from) {
		this.mFrom = from;
	}

	public int getFrom() {
		return this.mFrom;
	}

	public long getAskId() {
		return this.mAskId;
	}

	public void setAskId(long id) {
		this.mAskId = id;
	}

	public long getReplyId() {
		return this.mReplyId;
	}

	public void setReplyId(long id) {
		this.mReplyId = id;
	}

	public void setHotCommentList(List<Comment> hotCommentList) {
		this.mHotCommentList = hotCommentList;
	}

	/**
	 * TODO 临时放在这里
	 * 
	 * @param context
	 * @param photoItemDao
	 * @param items
	 * @param from
	 */
	public static void savePhotoList(final Context context,
			final Dao<PhotoItem, Long> photoItemDao,
			final List<PhotoItem> items, final int from) {
		ThreadManager.executeOnFileThread(new Runnable() {
			@Override
			public void run() {
				// DatabaseConnection connection = null;
				try {
					// mPhotoItemDao.delete(mDbPhotoItems);
					// connection = photoItemDao.startThreadConnection();
					// photoItemDao.setAutoCommit(connection, false);
					for (PhotoItem item : items) {
						item.setFrom(from);
						// photoItemDao.create(item);
						photoItemDao.createOrUpdate(item);
					}
					// photoItemDao.commit(connection);

					CommentDao commentDao = new CommentDao(context);
					for (PhotoItem item : items) {
						List<Comment> hotComments = item.getHotCommentList();
						for (Comment hotComment : hotComments) {
							hotComment.setPhotoItem(item);
							commentDao.add(hotComment);
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
					Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_DEV,
							"PSGodDatabase", e.getMessage());
				}
				// finally {
				// if (connection != null) {
				// try {
				// photoItemDao
				// .endThreadConnection(connection);
				// } catch (SQLException e) {
				// e.printStackTrace();
				// }
				// }
				// }
			}
		});
	}

	public void setPid(long mPid) {
		this.mPid = mPid;
	}
	
}