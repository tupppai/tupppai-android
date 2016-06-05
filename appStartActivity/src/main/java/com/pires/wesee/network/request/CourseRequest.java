package com.pires.wesee.network.request;

import com.android.volley.Response;
import com.pires.wesee.Logger;
import com.pires.wesee.model.PhotoItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2015/12/7 0007.
 */
public class CourseRequest extends BaseRequest<List<PhotoItem>> {
    private final static String TAG = CourseRequest.class.getSimpleName();

    public CourseRequest(int method, String url, Response.Listener<List<PhotoItem>> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }


    @Override
    protected List<PhotoItem> doParseNetworkResponse(JSONObject reponse) throws
            UnsupportedEncodingException, JSONException {
        List<PhotoItem> photoItems = new ArrayList<>();
        JSONArray data = reponse.getJSONObject("data").getJSONArray("tutorials");
        int length = data.length();
        for (int i = 0; i < length; i++) {
            PhotoItem item = PhotoItem.createPhotoItem(data.getJSONObject(i),getUrl());
            photoItems.add(item);
        }
        return photoItems;
    }

    public static class Builder implements IGetRequestBuilder {
        // public static final int TYPE_HOT = PhotoItem.FROM_HOT;
        // public static final int TYPE_FOCUS = PhotoItem.FROM_FOCUS;

        // public static final int TYPE_HOME_FOCUS = PhotoItem.TYPE_HOME_FOCUS;
        // public static final int TYPE_HOME_HOT = PhotoItem.TYPE_HOME_HOT;
        // public static final int TYPE_RECENT_ASK = PhotoItem.TYPE_RECENT_ASK;
        // public static final int TYPE_RECENT_WORK= PhotoItem.TYPE_RECENT_WORK;

        private int page = 1;
        private int size = 10;
        private long lastUpdated = -1;
        private String targetType = "";
        private String id;
        private Response.Listener<List<PhotoItem>> listener;
        private Response.ErrorListener errorListener;

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setTargetType(String targetType) {
            this.targetType = targetType;
            return this;
        }

        public Builder setLastUpdated(long lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public Builder setPage(int page) {
            this.page = page;
            return this;
        }

        public Builder setSize(int size) {
            this.size = size;
            return this;
        }

        public Builder setListener(Response.Listener<List<PhotoItem>> listener) {
            this.listener = listener;
            return this;
        }

        public Builder setErrorListener(Response.ErrorListener errorListener) {
            this.errorListener = errorListener;
            return this;
        }

        public CourseRequest build() {
            String url = createUrl();
            CourseRequest request = new CourseRequest(METHOD, url,
                    listener, errorListener);
            return request;
        }

        @Override
        public String createUrl() {
            StringBuilder sb = new StringBuilder(PSGOD_BASE_URL);
            sb.append("thread/tutorials_list");
            sb.append("?page=").append(page);
            sb.append("&last_updated=").append(lastUpdated);
            sb.append("&category_id=").append(id);
            sb.append("&type=").append(targetType);

            String url = sb.toString();
            Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
                    "createUrl: " + url);
            return url;
        }
    }


}
