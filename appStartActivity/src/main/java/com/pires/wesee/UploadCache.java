package com.pires.wesee;

public class UploadCache {

    private String cache;
    private String uploadType;
    private String token;
    private boolean isFinish = true;

    private static UploadCache uploadCache;

    private UploadCache() {
    }

    public String getCache(String uploadType, String token) {
        isFinish = false;
        if (uploadType.equals(this.uploadType) && token.equals(this.token)) {
            return this.cache == null ? "" : this.cache;
        } else {
            return "";
        }

    }

    public void setCache(String uploadType, String cache) {
        if(!isFinish) {
            this.cache = cache;
            this.uploadType = uploadType;
            this.token = UserPreferences.TokenVerify.getToken();
        }
    }

    public void clear() {
        cache = "";
        uploadType = "";
        token = "";
        isFinish = true;
    }

    public static UploadCache getInstence() {
        if (uploadCache == null) {
            uploadCache = new UploadCache();
        }
        return uploadCache;
    }
}
