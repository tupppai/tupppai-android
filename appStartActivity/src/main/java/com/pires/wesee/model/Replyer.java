package com.pires.wesee.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = "replyer")
public class Replyer implements Serializable {
	@DatabaseField(columnName = "uid")
	protected long mUid;
	@DatabaseField(columnName = "nickname")
	protected String mNickname;
	@DatabaseField(columnName = "avatar_url")
	protected String mAvatorURL;

	// 外键 PhotoItem pid
	@DatabaseField(canBeNull = true, foreign = true, columnName = "pid")
	protected PhotoItem mPhotoItem;

	public Replyer() {

	}

	public Replyer(long uid, String nickname, String avatorURL) {
		mUid = uid;
		mNickname = nickname;
		mAvatorURL = avatorURL;
	}

	public long getUid() {
		return mUid;
	}

	public void setUid(long mUid) {
		this.mUid = mUid;
	}

	public String getNickname() {
		return mNickname;
	}

	public void setNickname(String mNickname) {
		this.mNickname = mNickname;
	}

	public String getAvatorURL() {
		return mAvatorURL;
	}

	public void setAvatorURL(String mAvatorURL) {
		this.mAvatorURL = mAvatorURL;
	}

	public PhotoItem getPhotoItem() {
		return mPhotoItem;
	}

	public void setPhotoItem(PhotoItem mPhotoItem) {
		this.mPhotoItem = mPhotoItem;
	}
}
