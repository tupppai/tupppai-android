package com.psgod.network.request;

import com.alibaba.fastjson.JSONArray;
import com.android.volley.Response;
import com.psgod.Constants;
import com.psgod.Logger;
import com.psgod.model.Activities;
import com.psgod.model.Channel;
import com.psgod.model.PhotoItem;
import com.psgod.model.Tupppai;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/7 0007.
 */
public class ChannelRequest extends BaseRequest<List<Tupppai>> {
    private final static String TAG = ChannelRequest.class.getSimpleName();

    public ChannelRequest(int method, String url, Response.Listener<List<Tupppai>> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    @Override
    protected List<Tupppai> doParseNetworkResponse(JSONObject reponse) throws
            UnsupportedEncodingException, JSONException {
        List<Tupppai> tupppais = JSONArray.parseArray(reponse.getJSONObject("data").
                getJSONArray("categories").toString(), Tupppai.class);
        for(Tupppai tupppai : tupppais){
            for(Object jsonObject : tupppai.getThreads()){
//                PhotoItem photoItem = PhotoItem.createPhotoItem(new JSONObject(jsonObject.toString()));
//                tupppai.getThreads().add(photoItem);
            }
        }

        return tupppais;
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
        private Response.Listener<List<Tupppai>> listener;
        private Response.ErrorListener errorListener;

        public Builder setPage(int page) {
            this.page = page;
            return this;
        }

        public Builder setSize(int size) {
            this.size = size;
            return this;
        }

        public Builder setListener(Response.Listener<List<Tupppai>> listener) {
            this.listener = listener;
            return this;
        }

        public Builder setErrorListener(Response.ErrorListener errorListener) {
            this.errorListener = errorListener;
            return this;
        }

        public ChannelRequest build() {
            String url = createUrl();
            ChannelRequest request = new ChannelRequest(METHOD, url,
                    listener, errorListener);
            return request;
        }

        @Override
        public String createUrl() {
            StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);
            sb.append("thread/home");
            sb.append("?page=").append(page);

            String url = sb.toString();
            Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
                    "createUrl: " + url);
            return url;
        }
    }
}
