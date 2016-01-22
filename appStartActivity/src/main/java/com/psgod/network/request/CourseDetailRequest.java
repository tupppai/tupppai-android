package com.psgod.network.request;

import com.android.volley.Response;
import com.psgod.Logger;
import com.psgod.model.PhotoItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2015/12/7 0007.
 */
public class CourseDetailRequest extends BaseRequest<PhotoItem> {
    private final static String TAG = CourseDetailRequest.class.getSimpleName();

    public CourseDetailRequest(int method, String url, Response.Listener<PhotoItem> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }


    @Override
    protected PhotoItem doParseNetworkResponse(JSONObject reponse) throws
            UnsupportedEncodingException, JSONException {
        PhotoItem photoItem = PhotoItem.createPhotoItem(reponse.getJSONObject("data"), getUrl());
        return photoItem;
    }

    public static class Builder implements IGetRequestBuilder {
        // public static final int TYPE_HOT = PhotoItem.FROM_HOT;
        // public static final int TYPE_FOCUS = PhotoItem.FROM_FOCUS;

        // public static final int TYPE_HOME_FOCUS = PhotoItem.TYPE_HOME_FOCUS;
        // public static final int TYPE_HOME_HOT = PhotoItem.TYPE_HOME_HOT;
        // public static final int TYPE_RECENT_ASK = PhotoItem.TYPE_RECENT_ASK;
        // public static final int TYPE_RECENT_WORK= PhotoItem.TYPE_RECENT_WORK;

        private String id;
        private Response.Listener<PhotoItem> listener;
        private Response.ErrorListener errorListener;

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setListener(Response.Listener<PhotoItem> listener) {
            this.listener = listener;
            return this;
        }

        public Builder setErrorListener(Response.ErrorListener errorListener) {
            this.errorListener = errorListener;
            return this;
        }

        public CourseDetailRequest build() {
            String url = createUrl();
            CourseDetailRequest request = new CourseDetailRequest(METHOD, url,
                    listener, errorListener);
            return request;
        }

        @Override
        public String createUrl() {
            StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);
            sb.append("thread/tutorial_details");
            sb.append("?tutorial_id=").append(id);

            String url = sb.toString();
            Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
                    "createUrl: " + url);
            return url;
        }
    }


}
