package com.pires.wesee.model;

import java.io.Serializable;

public class SearchUserReplies implements Serializable {
	private String id;
	private String ask_id;
	private String desc;
	private String upload_id;
	private String create_time;
	private String update_time;
	private String image_url;
	private int image_width;
	private int image_height;

	public void setId(String id) {
		this.id = id;
	}

	public void setAsk_id(String ask_id) {
		this.ask_id = ask_id;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void setUpload_id(String upload_id) {
		this.upload_id = upload_id;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

	public void setUpdate_time(String update_time) {
		this.update_time = update_time;
	}

	public void setImage_url(String image_url) {
		this.image_url = image_url;
	}

	public void setImage_width(int image_width) {
		this.image_width = image_width;
	}

	public void setImage_height(int image_height) {
		this.image_height = image_height;
	}

	public String getId() {
		return id;
	}

	public String getAsk_id() {
		return ask_id;
	}

	public String getDesc() {
		return desc;
	}

	public String getUpload_id() {
		return upload_id;
	}

	public String getCreate_time() {
		return create_time;
	}

	public String getUpdate_time() {
		return update_time;
	}

	public String getImage_url() {
		return image_url;
	}

	public int getImage_width() {
		return image_width;
	}

	public int getImage_height() {
		return image_height;
	}
}
