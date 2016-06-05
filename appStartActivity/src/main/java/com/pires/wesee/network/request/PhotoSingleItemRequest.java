package com.pires.wesee.network.request;

import com.android.volley.Response;
import com.pires.wesee.model.PhotoItem;
import com.pires.wesee.Constants;
import com.pires.wesee.Logger;
import com.pires.wesee.model.SinglePhotoItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/1/28 0028.
 */
public class PhotoSingleItemRequest extends BaseRequest<SinglePhotoItem> {
    private final static String TAG = CourseRequest.class.getSimpleName();

    public PhotoSingleItemRequest(int method, String url, Response.Listener<SinglePhotoItem> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }


    @Override
    protected SinglePhotoItem doParseNetworkResponse(JSONObject reponse) throws
            UnsupportedEncodingException, JSONException {
        SinglePhotoItem item = new SinglePhotoItem();
        if (getUrl().indexOf("ask/show/") != -1) {
            item.setAskPhotoItems(PhotoItem.createPhotoItem(
                    reponse.getJSONObject("data").getJSONObject("ask"), getUrl()));
            item.setPhotoItem(item.getAskPhotoItems());
            JSONArray jsonArray = reponse.getJSONObject("data").getJSONArray("replies");
            if (jsonArray != null) {
                List<PhotoItem> photoItems = new ArrayList<PhotoItem>();
                int length = jsonArray.length();
                for (int i = 0; i < length; i++) {
                    PhotoItem photoItem = PhotoItem.createPhotoItem(jsonArray.getJSONObject(i)
                            , getUrl());
                    photoItems.add(photoItem);
                }
                item.setReplyPhotoItems(photoItems);
            }
        } else {
            item.setPhotoItem(PhotoItem.createPhotoItem(
                    reponse.getJSONObject("data"), getUrl()));
        }
        return item;
    }

    public static class Builder implements IGetRequestBuilder {

        private String id;
        private String type = Constants.IntentKey.REPLY_ID;
        private Response.Listener<SinglePhotoItem> listener;
        private Response.ErrorListener errorListener;

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setListener(Response.Listener<SinglePhotoItem> listener) {
            this.listener = listener;
            return this;
        }

        public Builder setErrorListener(Response.ErrorListener errorListener) {
            this.errorListener = errorListener;
            return this;
        }

        public PhotoSingleItemRequest build() {
            String url = createUrl();
            PhotoSingleItemRequest request = new PhotoSingleItemRequest(METHOD, url,
                    listener, errorListener);
            return request;
        }

        @Override
        public String createUrl() {
            StringBuilder sb = new StringBuilder(PSGOD_BASE_URL);
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