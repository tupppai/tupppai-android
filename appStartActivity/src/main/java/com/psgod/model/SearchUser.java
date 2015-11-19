package com.psgod.model;

import java.io.Serializable;
import java.util.List;

public class SearchUser implements Serializable {

	private int ret;
	private int code;
	private String info;
	private String token;
	private int debug;
	private List<SearchUserData> data;

	public void setRet(int ret) {
		this.ret = ret;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setDebug(int debug) {
		this.debug = debug;
	}

	public void setData(List<SearchUserData> data) {
		this.data = data;
	}

	public int getRet() {
		return ret;
	}

	public int getCode() {
		return code;
	}

	public String getInfo() {
		return info;
	}

	public String getToken() {
		return token;
	}

	public int getDebug() {
		return debug;
	}

	public List<SearchUserData> getData() {
		return data;
	}

}
