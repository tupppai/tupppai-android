package com.psgod.model.notification;

/**
 * 基础消息通知类 （仅消息）
 */

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class NotificationMessage implements Serializable {

	// 消息类型
	private int mType;
	// 消息id
	private long mNid;
	// 发送者id sender
	private long mUid;
	private String mNickName;

	private boolean mIsStar;

	private String mAvatar;
	private String mContent;
	private long mCreatedTime;

	private String mPicUrl; // 系统配图
	private String mJumpUrl;

	private Long mTargetId; // 目标id
	// target type 求助1 作品2 评论3
	private int mTargetType;

	// 回复作品消息
	private long mAskId;
	private long mReplyId;

	private String mDesc;
	private long mForCommentId;

	// 收到的赞需要用到的ask_id
	private long mTargetAskId = -1;

	private long mCommentId = -1;

	public boolean isStar() {
		return mIsStar;
	}

	public void setIsStar(boolean mIsStar) {
		this.mIsStar = mIsStar;
	}

	public static NotificationMessage createNotification(JSONObject jsonObj)
			throws JSONException {
		NotificationMessage notification = new NotificationMessage();
		if (jsonObj.has("id")) {
			notification.setNid(jsonObj.getLong("id"));
		}
		if (jsonObj.has("type")) {
			notification.setType(jsonObj.getInt("type"));
		}
		if (jsonObj.has("uid")) {
			notification.setUid(jsonObj.getLong("uid"));
		}
		if (jsonObj.has("nickname")) {
			notification.setNickName(jsonObj.getString("nickname"));
		}
		if (jsonObj.has("avatar")) {
			notification.setAvatar(jsonObj.getString("avatar"));
		}
		if (jsonObj.has("content")) {
			notification.setContent(jsonObj.getString("content"));
		}
		if (jsonObj.has("create_time")) {
			notification.setCreatedTime(jsonObj.getLong("create_time"));
		}

		if (jsonObj.has("update_time")) {
			notification.setCreatedTime(jsonObj.getLong("update_time"));
		}
		if (jsonObj.has("sender")) {
			notification.setUid(jsonObj.getLong("sender"));
		}
		if (jsonObj.has("jump_url")) {
			notification.setJumpUrl(jsonObj.getString("jump_url"));
		}
		if (jsonObj.has("pic_url")) {
			notification.setPicUrl(jsonObj.getString("pic_url"));
		}
		if (jsonObj.has("target_type")) {
			notification.setTargetType(jsonObj.getInt("target_type"));
		}
		if (jsonObj.has("target_id")) {
			notification.setTargetId(jsonObj.getLong("target_id"));
		}
		if (jsonObj.has("username")) {
			notification.setNickName(jsonObj.getString("username"));
		}

		// 回复作品消息独有
		if (jsonObj.has("ask_id")) {
			notification.setAskId(jsonObj.getLong("ask_id"));
		}

		if (jsonObj.has("reply_id")) {
			notification.setReplyId(jsonObj.getLong("reply_id"));
		}

		if (jsonObj.has("desc")) {
			notification.setmDesc(jsonObj.getString("desc"));
		}

		if (jsonObj.has("for_comment")) {
			notification.setmForCommentId(jsonObj.getLong("for_comment"));
		}

		// 收到的赞需要的ask_id
		if (jsonObj.has("target_ask_id")) {
			notification.setTargetAskId(jsonObj.getLong("target_ask_id"));
		}

		if (jsonObj.has("comment_id")) {
			notification.setCommentId(jsonObj.getLong("comment_id"));
		}

		if (jsonObj.has("is_star")){
			notification.mIsStar = jsonObj.getBoolean("is_star");
		}

		return notification;
	}

	public long getCommentId() {
		return mCommentId;
	}

	public void setCommentId(long mCommentId) {
		this.mCommentId = mCommentId;
	}

	public Long getTargetAskId() {
		return mTargetAskId;
	}

	public void setTargetAskId(Long id) {
		this.mTargetAskId = id;
	}

	public Long getTargetId() {
		return mTargetId;
	}

	public void setTargetId(Long id) {
		this.mTargetId = id;
	}

	public int getTargetType() {
		return mTargetType;
	}

	public void setTargetType(int target_type) {
		this.mTargetType = target_type;
	}

	public String getmDesc() {
		return mDesc;
	}

	public void setmDesc(String mDesc) {
		this.mDesc = mDesc;
	}

	public long getmForCommentId() {
		return mForCommentId;
	}

	public void setmForCommentId(long mForCommentId) {
		this.mForCommentId = mForCommentId;
	}

	public void setPicUrl(String pic_url) {
		this.mPicUrl = pic_url;
	}

	public String getPicUrl() {
		return mPicUrl;
	}

	public void setJumpUrl(String jump_url) {
		this.mJumpUrl = jump_url;
	}

	public String getJumpUrl() {
		return mJumpUrl;
	}

	public void setNid(long nid) {
		this.mNid = nid;
	}

	public long getNid() {
		return mNid;
	}

	public int getType() {
		return mType;
	}

	public void setType(int mType) {
		this.mType = mType;
	}

	public Long getUid() {
		return mUid;
	}

	public void setUid(Long mUid) {
		this.mUid = mUid;
	}

	public String getNickName() {
		return mNickName;
	}

	public Long getAskId() {
		return mAskId;
	}

	public Long getReplyId() {
		return mReplyId;
	}

	public void setNickName(String mNickName) {
		this.mNickName = mNickName;
	}

	public String getAvatar() {
		return mAvatar;
	}

	public void setAvatar(String mAvatar) {
		this.mAvatar = mAvatar;
	}

	public String getContent() {
		return mContent;
	}

	public void setContent(String mContent) {
		this.mContent = mContent;
	}

	public Long getCreatedTime() {
		return mCreatedTime;
	}

	public void setCreatedTime(Long mCreatedTime) {
		this.mCreatedTime = mCreatedTime;
	}

	public void setAskId(Long askId) {
		this.mAskId = askId;
	}

	public void setReplyId(Long replyId) {
		this.mReplyId = replyId;
	}
}
