package com.pires.wesee.model;

import android.text.TextUtils;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Rayal
 * 
 */

@DatabaseTable(tableName = "photo_comment")
public class Comment implements Serializable {

	@DatabaseField(id = true, columnName = "cid")
	protected long mCid; // 评论ID

	@DatabaseField(columnName = "uid")
	protected long mUid; // 评论者ID

	@DatabaseField(columnName = "pid")
	protected long mPid;

	@DatabaseField(columnName = "nickname")
	protected String mNickname; // 用户 昵称

	@DatabaseField(columnName = "avatar_url")
	protected String mAvatarURL; // 头像地址

	@DatabaseField(columnName = "gender")
	protected int mGender; // 性别

	@DatabaseField(columnName = "content")
	protected String mContent; // 评论内容

	@DatabaseField(columnName = "like_count")
	protected int mLikeCount; // 点赞数

	@DatabaseField(columnName = "create_time")
	protected long mCreatedTime; // 创建时间

	private String mUpdateTimeStr = null; // 视图上显示的创建时间，不需要存DB

	@DatabaseField(columnName = "is_like")
	protected boolean mIsLiked; // 是否点过赞

	protected boolean mIsStar;

	public boolean isStar() {
		return mIsStar;
	}

	public void setIsStar(boolean mIsStar) {
		this.mIsStar = mIsStar;
	}

	// 外键 PhotoItem pid
	@DatabaseField(canBeNull = true, foreign = true, columnName = "pid")
	protected PhotoItem mPhotoItem;

	public List<ReplyComment> mReplyComments = new ArrayList<ReplyComment>();

	// TODO 增加Photoitem 参数 构造函数
	public static Comment createComment(JSONObject jsonObj)
			throws JSONException {
		Comment comment = new Comment();
		comment.mCid = jsonObj.getLong("comment_id");
		comment.mUid = jsonObj.getLong("uid");
		comment.mNickname = jsonObj.getString("nickname");
		comment.mAvatarURL = jsonObj.getString("avatar");
		comment.mGender = jsonObj.getInt("sex");
		comment.mContent = jsonObj.getString("content");
		if (jsonObj.has("uped")) {
			comment.mIsLiked = jsonObj.getBoolean("uped");
		}
		try {
			comment.mLikeCount = jsonObj.getInt("up_count");
		} catch (Exception e) {
			comment.mLikeCount = 0;
		}
		comment.mCreatedTime = jsonObj.getLong("create_time");

		if (jsonObj.has("at_comment")) {
			JSONArray atCommentsArray = jsonObj.getJSONArray("at_comment");
			int atCommentsLength = atCommentsArray.length();
			for (int i = 0; i < atCommentsLength; i++) {
				JSONObject replyer = atCommentsArray.getJSONObject(i);
				comment.mReplyComments.add(new ReplyComment(replyer
						.getLong("comment_id"), replyer.getLong("uid"), replyer
						.getString("content"), replyer.getString("nickname")));
			}
		}

		if(jsonObj.has("is_star")){
			comment.mIsStar = jsonObj.getBoolean("is_star");
		}
		// TODO
		comment.mPhotoItem = null;
		return comment;
	}

	// unix时间戳转化为北京时间
	public static String toLocalTime(String unix) {
		Long timestamp = Long.parseLong(unix) * 1000;
		String date = new SimpleDateFormat("MM月dd日 HH:mm")
				.format(new java.util.Date(timestamp));
		return date;
	}

	public String getUpdateTimeStr() {
		SimpleDateFormat formatter = new SimpleDateFormat("MM月dd日 HH:mm");
		if (TextUtils.isEmpty(mUpdateTimeStr)) {
			StringBuffer sb = new StringBuffer();
			long time = System.currentTimeMillis() - (mCreatedTime * 1000);
			long mill = (long) Math.ceil(time / 1000);// 秒前
			long minute = (long) Math.ceil(time / 60 / 1000.0f);// 分钟前
			long hour = (long) Math.ceil(time / 60 / 60 / 1000.0f);// 小时
			long day = (long) Math.ceil(time / 24 / 60 / 60 / 1000.0f);// 天前

			if (day - 1 > 0) {
				if (day > 7) {
					sb.append(toLocalTime(Long.toString(mCreatedTime)));
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
			if (!sb.toString().equals("刚刚") && (day < 7)) {
				sb.append("前");
			}
			mUpdateTimeStr = sb.toString();
		}
		return mUpdateTimeStr;
	}

	public List<ReplyComment> getReplyComments() {
		return mReplyComments;
	}

	public void setReplyComments(List<ReplyComment> replyComments) {
		this.mReplyComments = replyComments;
	}

	public long getCid() {
		return mCid;
	}

	public void setCid(long cid) {
		this.mCid = cid;
	}

	public boolean getIsLiked() {
		return mIsLiked;
	}

	public void setIsLiked(boolean isliked) {
		this.mIsLiked = isliked;
	}

	public long getUid() {
		return mUid;
	}

	public void setUid(long uid) {
		this.mUid = uid;
	}

	public long getPid() {
		return mPid;
	}

	public void setPid(long pid) {
		this.mPid = pid;
	}

	public String getNickname() {
		return mNickname;
	}

	public void setNickName(String nickname) {
		this.mNickname = nickname;
	}

	public String getAvatarURL() {
		return mAvatarURL;
	}

	public void setAvatarURL(String avatarUrl) {
		this.mAvatarURL = avatarUrl;
	}

	public int getGender() {
		return mGender;
	}

	public void setGender(int gender) {
		this.mGender = gender;
	}

	public String getContent() {
		return mContent;
	}

	public void setContent(String content) {
		this.mContent = content;
	}

	public int getLikeCount() {
		return mLikeCount;
	}

	public void setLikeCount(int likeCount) {
		this.mLikeCount = likeCount;
	}

	public long getCreatedTime() {
		return mCreatedTime;
	}

	public void setCreatedTime(long createdTime) {
		this.mCreatedTime = createdTime;
	}

	public void setPhotoItem(PhotoItem photoItem) {
		this.mPhotoItem = photoItem;
	}

	public PhotoItem getPhotoItem() {
		return mPhotoItem;
	}

	public static class ReplyComment implements Serializable {
		public long mPid;
		public long mUid;
		public String mContent;
		public String mNick;

		public ReplyComment(long pid, long uid, String content, String nick) {
			mPid = pid;
			mUid = uid;
			mContent = content;
			mNick = nick;
		}
	}
}
