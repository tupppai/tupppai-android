package com.psgod.model;

import java.io.Serializable;
import java.util.List;


public class SearchUserData implements Serializable {
	private long uid;
	private String username;
	private String nickname;
	private String phone;
	private int sex;
	private String avatar;
	private String uped_count;
	private int current_score;
	private int paid_score;
	private int total_praise;
	private String location;
	private String province;
	private String city;
	private int is_follow;
	private int is_fan;
	private Object bg_image;
	private int status;
	private int is_bound_weixin;
	private int is_bound_qq;
	private int is_bound_weibo;
	private String weixin;
	private String weibo;
	private String qq;
	private int fans_count;
	private int fellow_count;
	private int ask_count;
	private int reply_count;
	private int inprogress_count;
	private int collection_count;
	private List<SearchUserReplies> replies;
	private boolean is_star;

	public boolean is_star() {
		return is_star;
	}

	public void setIs_star(boolean is_star) {
		this.is_star = is_star;
	}

	public int getIs_follow() {
		return is_follow;
	}

	public int getIs_fan() {
		return is_fan;
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public void setIs_fan(int is_fan) {
		this.is_fan = is_fan;
	}

	public void setIs_follow(int is_follow) {
		this.is_follow = is_follow;
	}



	public void setUsername(String username) {
		this.username = username;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public void setUped_count(String uped_count) {
		this.uped_count = uped_count;
	}

	public void setCurrent_score(int current_score) {
		this.current_score = current_score;
	}

	public void setPaid_score(int paid_score) {
		this.paid_score = paid_score;
	}

	public void setTotal_praise(int total_praise) {
		this.total_praise = total_praise;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setBg_image(Object bg_image) {
		this.bg_image = bg_image;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setIs_bound_weixin(int is_bound_weixin) {
		this.is_bound_weixin = is_bound_weixin;
	}

	public void setIs_bound_qq(int is_bound_qq) {
		this.is_bound_qq = is_bound_qq;
	}

	public void setIs_bound_weibo(int is_bound_weibo) {
		this.is_bound_weibo = is_bound_weibo;
	}

	public void setWeixin(String weixin) {
		this.weixin = weixin;
	}

	public void setWeibo(String weibo) {
		this.weibo = weibo;
	}

	public void setQq(String qq) {
		this.qq = qq;
	}

	public void setFans_count(int fans_count) {
		this.fans_count = fans_count;
	}

	public void setFellow_count(int fellow_count) {
		this.fellow_count = fellow_count;
	}

	public void setAsk_count(int ask_count) {
		this.ask_count = ask_count;
	}

	public void setReply_count(int reply_count) {
		this.reply_count = reply_count;
	}

	public void setInprogress_count(int inprogress_count) {
		this.inprogress_count = inprogress_count;
	}

	public void setCollection_count(int collection_count) {
		this.collection_count = collection_count;
	}

	public void setReplies(List<SearchUserReplies> replies) {
		this.replies = replies;
	}

	public String getUsername() {
		return username;
	}

	public String getNickname() {
		return nickname;
	}

	public String getPhone() {
		return phone;
	}

	public int getSex() {
		return sex;
	}

	public String getAvatar() {
		return avatar;
	}

	public String getUped_count() {
		return uped_count;
	}

	public int getCurrent_score() {
		return current_score;
	}

	public int getPaid_score() {
		return paid_score;
	}

	public int getTotal_praise() {
		return total_praise;
	}

	public String getLocation() {
		return location;
	}

	public String getProvince() {
		return province;
	}

	public String getCity() {
		return city;
	}

	public Object getBg_image() {
		return bg_image;
	}

	public int getStatus() {
		return status;
	}

	public int getIs_bound_weixin() {
		return is_bound_weixin;
	}

	public int getIs_bound_qq() {
		return is_bound_qq;
	}

	public int getIs_bound_weibo() {
		return is_bound_weibo;
	}

	public String getWeixin() {
		return weixin;
	}

	public String getWeibo() {
		return weibo;
	}

	public String getQq() {
		return qq;
	}

	public int getFans_count() {
		return fans_count;
	}

	public int getFellow_count() {
		return fellow_count;
	}

	public int getAsk_count() {
		return ask_count;
	}

	public int getReply_count() {
		return reply_count;
	}

	public int getInprogress_count() {
		return inprogress_count;
	}

	public int getCollection_count() {
		return collection_count;
	}

	public List<SearchUserReplies> getReplies() {
		return replies;
	}

	
}