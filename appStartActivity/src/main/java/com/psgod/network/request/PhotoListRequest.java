package com.psgod.network.request;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Constants;
import com.psgod.Logger;
import com.psgod.model.PhotoItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 【首页网络请求】
 * <p/>
 * width: 屏幕尺寸 type: 取值 hot (最热)、 fellow (关注)，默认 hot last_updated: 最后下拉更新的时间戳（秒）
 * sort: 排序字段，默认为 time order: 排序，取值 desc、asc，默认为 desc page: 页码，默认为 1 size:
 * 单面数目，默认为 15
 *
 * @author rayalyuan
 */
public final class PhotoListRequest extends BaseRequest<List<PhotoItem>> {
    private final static String TAG = PhotoListRequest.class.getSimpleName();


    public PhotoListRequest(int method, String url,
                            Listener<List<PhotoItem>> listener, ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    /**
     * 该方法在子线程里调用
     */
    @Override
    protected List<PhotoItem> doParseNetworkResponse(JSONObject response)
            throws JSONException {
        JSONArray data = response.getJSONArray("data");
        int length = data.length();
        List<PhotoItem> items = new ArrayList<PhotoItem>(length);
        for (int ix = 0; ix < length; ++ix) {
            items.add(PhotoItem.createPhotoItem(data.getJSONObject(ix)));
        }
        return items;
    }

    public static class Builder implements IGetRequestBuilder {
        // public static final int TYPE_HOT = PhotoItem.FROM_HOT;
        // public static final int TYPE_FOCUS = PhotoItem.FROM_FOCUS;

        // public static final int TYPE_HOME_FOCUS = PhotoItem.TYPE_HOME_FOCUS;
        // public static final int TYPE_HOME_HOT = PhotoItem.TYPE_HOME_HOT;
        // public static final int TYPE_RECENT_ASK = PhotoItem.TYPE_RECENT_ASK;
        // public static final int TYPE_RECENT_WORK= PhotoItem.TYPE_RECENT_WORK;

        public static final int ORDER_DESC = 0;
        public static final int ORDER_ASC = 1;

        private int width = Constants.WIDTH_OF_SCREEN;
        private int type = PhotoItem.TYPE_HOME_HOT;
        private long lastUpdated = -1;
        private int sort;
        private int order = ORDER_DESC;
        private int page = 1;
        private int size = 15;
        private long askId = 0l;
        private Listener<List<PhotoItem>> listener;
        private ErrorListener errorListener;
        private String channelId;

        public Builder setChannelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        /**
         * @param type TYPE_HOT: 热门 TYPE_RECENT: 最近
         * @return
         */
        public Builder setType(int type) {
            this.type = type;
            return this;
        }

        public Builder setLastUpdated(long lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public Builder setSort(int sort) {
            this.sort = sort;
            return this;
        }

        public Builder setAskId(long askId) {
            this.askId = askId;
            return this;
        }

        /**
         * 设置排序策略
         *
         * @param sort ORDER_DESC: 降序 ORDER_ASC: 升序
         * @return
         */
        public Builder setOrder(int order) {
            this.order = order;
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

        public Builder setListener(Listener<List<PhotoItem>> listener) {
            this.listener = listener;
            return this;
        }

        public Builder setErrorListener(ErrorListener errorListener) {
            this.errorListener = errorListener;
            return this;
        }

        public PhotoListRequest build() {
            String url = createUrl();
            PhotoListRequest request = new PhotoListRequest(METHOD, url,
                    listener, errorListener);
            return request;
        }

        @Override
        public String createUrl() {
            StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);

            switch (type) {
                case PhotoItem.TYPE_HOME_HOT:
                    sb.append("thread/popular");
                    break;
                case PhotoItem.TYPE_HOME_FOCUS:
                    sb.append("thread/timeline");
                    break;
                case PhotoItem.TYPE_RECENT_ASK:
                    sb.append("ask/index");
                    break;
                case PhotoItem.TYPE_RECENT_WORK:
                    sb.append("reply/index");
                    break;

            }
            sb.append("?width=").append(width);
            sb.append("&page=").append(page);
            if(channelId != null && !channelId.equals("")){
                sb.append("&category_id=").append(channelId);
            }
            // 拉取某个求P的全部作品
            if (askId != 0l) {
                sb.append("&ask_id=").append(askId);
            }
            if (lastUpdated != -1) {
                sb.append("&last_updated=").append(lastUpdated);
            }
            // sb.append("size=").append(size); 用服务器的默认值
            // private int sort;
            // private int order = ORDER_DESC;

            String url = sb.toString();
            Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
                    "createUrl: " + url);
            return url;
        }
    }
}
