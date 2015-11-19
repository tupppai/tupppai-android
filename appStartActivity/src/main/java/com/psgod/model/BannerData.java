package com.psgod.model;

import org.json.JSONException;
import org.json.JSONObject;
/**
 * 首页banner
 * 
 * @author ZouMengyuan
 *
 */
public class BannerData {
	
	protected long id;
	protected long uid;
	protected String small_pic;
	protected String large_pic;
	protected String url;
	protected String desc;
	protected int status;
	protected int orderBy;
	protected long create_time;
	protected long update_time;
	
	public static BannerData createBanner (JSONObject jsonObject) throws JSONException {
		BannerData bannerData = new BannerData();
		bannerData.id = jsonObject.getLong("id");
		bannerData.uid = jsonObject.getLong("uid");
		bannerData.small_pic = jsonObject.getString("small_pic");
		bannerData.large_pic = jsonObject.getString("large_pic");
		bannerData.url = jsonObject.getString("url");
		bannerData.desc = jsonObject.getString("desc");
		bannerData.status = jsonObject.getInt("status");
		bannerData.orderBy = jsonObject.getInt("orderBy");
		bannerData.create_time = jsonObject.getLong("create_time");
		bannerData.update_time = jsonObject.getLong("update_time");
		
		return bannerData;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public String getSmall_pic() {
		return small_pic;
	}

	public void setSmall_pic(String small_pic) {
		this.small_pic = small_pic;
	}

	public String getLarge_pic() {
		return large_pic;
	}

	public void setLarge_pic(String large_pic) {
		this.large_pic = large_pic;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(int orderBy) {
		this.orderBy = orderBy;
	}

	public long getCreate_time() {
		return create_time;
	}

	public void setCreate_time(long create_time) {
		this.create_time = create_time;
	}

	public long getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(long update_time) {
		this.update_time = update_time;
	}

}
