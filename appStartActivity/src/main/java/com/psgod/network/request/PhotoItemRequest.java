package com.psgod.network.request;

import com.android.volley.Response;
import com.psgod.Constants;
import com.psgod.Logger;
import com.psgod.model.PhotoItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/1/28 0028.
 */
public class PhotoItemRequest extends BaseRequest<PhotoItem> {
    private final static String TAG = CourseRequest.class.getSimpleName();

    public PhotoItemRequest(int method, String url, Response.Listener<PhotoItem> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }


    @Override
    protected PhotoItem doParseNetworkResponse(JSONObject reponse) throws
            UnsupportedEncodingException, JSONException {
        PhotoItem item = PhotoItem.createPhotoItem(reponse.getJSONObject("data"), getUrl());
        return item;
    }

    public static class Builder implements IGetRequestBuilder {

        private String id;
        private String type = Constants.IntentKey.REPLY_ID;
        private Response.Listener<PhotoItem> listener;
        private Response.ErrorListener errorListener;

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
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

        public PhotoItemRequest build() {
            String url = createUrl();
            PhotoItemRequest request = new PhotoItemRequest(METHOD, url,
                    listener, errorListener);
            return request;
        }

        @Override
        public String createUrl() {
            StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);
            if (type.equals(Constants.IntentKey.REPLY_ID)) {
                sb.append("reply/show/");
            } else {
                sb.append("ask/show/");
            }
            sb.append(id);

            String url = sb.toString();
            Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
                    "createUrl: " + url);
            return url;
        }
    }


}