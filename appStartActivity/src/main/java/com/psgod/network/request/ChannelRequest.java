package com.psgod.network.request;

import com.android.volley.Response;
import com.psgod.Logger;
import com.psgod.model.Channel;
import com.psgod.model.PhotoItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 2015/12/7 0007.
 */
public class ChannelRequest extends BaseRequest<Channel> {
    private final static String TAG = ChannelRequest.class.getSimpleName();

    public ChannelRequest(int method, String url, Response.Listener<Channel> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    private boolean isRefresh = false;

    public void setIsRefresh(boolean isRefresh) {
        this.isRefresh = isRefresh;
    }

    @Override
    protected Channel doParseNetworkResponse(JSONObject reponse) throws
            UnsupportedEncodingException, JSONException {
        Channel channel = new Channel();
        JSONObject data = reponse.getJSONObject("data");
        JSONArray replies = data.getJSONArray("replies");
        if(isRefresh) {
            JSONArray ask = data.getJSONArray("ask");
            int length = ask.length();
            for (int i = 0; i < length; i++) {
                PhotoItem item = PhotoItem.createPhotoItem(ask.getJSONObject(i));
                channel.getAsk().add(item);
            }
        }
        int length = replies.length();
        for (int i = 0; i < length; i++) {
            PhotoItem item = PhotoItem.createPhotoItem(replies.getJSONObject(i));
            channel.getReplies().add(item);
        }
        return channel;
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
        private String id;
        private Response.Listener<Channel> listener;
        private Response.ErrorListener errorListener;
        private boolean isRefresh = false;

        public Builder setId(String id) {
            this.id = id;
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

        public Builder setListener(Response.Listener<Channel> listener) {
            this.listener = listener;
            return this;
        }

        public Builder setIsRefresh(boolean isRefresh) {
            this.isRefresh = isRefresh;
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
            request.setIsRefresh(isRefresh);
            return request;
        }

        @Override
        public String createUrl() {
            StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);
            sb.append("/thread/get_threads_by_channel");
            sb.append("?page=").append(page);
            sb.append("&last_updated=").append(lastUpdated);
            sb.append("&channel_id=").append(id);

            String url = sb.toString();
            Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
                    "createUrl: " + url);
            return url;
        }
    }


}
