package com.psgod.network.request;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.psgod.Logger;
import com.psgod.PSGodApplication;
import com.psgod.R;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

public class PhotoRequest {
    private static final String TAG = PhotoRequest.class.getSimpleName();

    public static ImageInfo getImageInfo(String type, long pid) {
        ImageInfo res = new ImageInfo();
        res.isSuccessful = false;
        try {
            // 通过求P id去拉取图片信息
            StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL)
                    .append("profile/downloadFile?type=")
                    .append(type.toString()).append("&target=").append(pid);

            String url = sb.toString();
            Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
                    "getImageInfo, url=" + url);
            JSONObject obj = Request.getRequest(url);

            if ((obj != null) && (obj.getInt("ret") == 1)) {
                JSONObject data = obj.getJSONObject("data");
                res.url = data.getString("url");
                res.isSuccessful = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static ImageInfo getImageInfo(String type, long pid, long category_id) {
        ImageInfo res = new ImageInfo();
        res.isSuccessful = false;
        try {
            // 通过求P id去拉取图片信息
            StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL)
                    .append("profile/downloadFile?type=")
                    .append(type.toString()).append("&target=").append(pid);
            if (category_id != -1) {
                sb.append("&category_id=").append(category_id);
            }
            String url = sb.toString();
            Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
                    "getImageInfo, url=" + url);
            JSONObject obj = Request.getRequest(url);

            if ((obj != null) && (obj.getInt("ret") == 1)) {
                JSONObject data = obj.getJSONObject("data");
                res.url = data.getString("url");
                res.isSuccessful = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static Bitmap downloadImage(String url) {
        BufferedInputStream bis = null;
        Bitmap bitmap = null;
        try {
            bis = new BufferedInputStream((new URL(url)).openStream());
            return BitmapFactory.decodeStream(bis);
        } catch (IOException e) {
            Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_COLOR, TAG,
                    "downloadImage(): failed, msg=" + e.getMessage());
            // 失败的话返回裂图吧
            bitmap = ((BitmapDrawable) PSGodApplication.getAppContext()
                    .getResources().getDrawable(R.drawable.ic_lietu))
                    .getBitmap();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_COLOR,
                            TAG,
                            "downloadImage(): failed, msg=" + e.getMessage());
                }
            }
        }
        return bitmap;
    }

    public static class ImageInfo {
        public boolean isSuccessful;
        public String url;
        public int width;
        public int height;
    }
}
