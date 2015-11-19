package com.psgod;

public class UploadCache {

	private String cache;
	private String uploadType;
	private String token;

	private static UploadCache uploadCache;

	private UploadCache() {
	}

	public String getCache(String uploadType, String token) {
		if (uploadType.equals(this.uploadType) && token.equals(this.token)) {
			return this.cache == null ? "" : this.cache;
		} else {
			return "";
		}
	}

	public void setCache(String uploadType, String cache) {
		this.cache = cache;
		this.uploadType = uploadType;
		this.token = UserPreferences.TokenVerify.getToken();
	}

	public void clear() {
		cache = "";
		uploadType = "";
		token = "";
	}

	public static UploadCache getInstence() {
		if (uploadCache == null) {
			return new UploadCache();
		} else {
			return uploadCache;
		}
	}
}
