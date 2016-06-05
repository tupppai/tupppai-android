package com.pires.wesee.network.request;

import com.android.volley.Response;
import com.pires.wesee.Logger;
import com.pires.wesee.model.ActivitiesAct;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 2015/12/7 0007.
 */
public class ActivitiesActRequest extends BaseRequest<ActivitiesAct> {
    private final static String TAG = ActivitiesActRequest.class.getSimpleName();

    public ActivitiesActRequest(int method, String url, Response.Listener<ActivitiesAct> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    @Override
    protected ActivitiesAct doParseNetworkResponse(JSONObject reponse) throws
            UnsupportedEncodingException, JSONException {
        ActivitiesAct act = com.alibaba.fastjson.JSONObject
                .parseObject(reponse.getJSONObject("data").
                        getJSONObject("activity").toString(), ActivitiesAct.class);
        return act;
    }

    public static class Builder implements IGetRequestBuilder {
        // public static final int TYPE_HOT = PhotoItem.FROM_HOT;
        // public static final int TYPE_FOCUS = PhotoItem.FROM_FOCUS;

        // public static final int TYPE_HOME_FOCUS = PhotoItem.TYPE_HOME_FOCUS;
        // public static final int TYPE_HOME_HOT = PhotoItem.TYPE_HOME_HOT;
        // public static final int TYPE_RECENT_ASK = PhotoItem.TYPE_RECENT_ASK;
        // public static final int TYPE_RECENT_WORK= PhotoItem.TYPE_RECENT_WORK;

        private String categoryId = "";
        private Response.Listener<ActivitiesAct> listener;
        private Response.ErrorListener errorListener;

        public Builder setCategoryId(String categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder setListener(Response.Listener<ActivitiesAct> listener) {
            this.listener = listener;
            return this;
        }

        public Builder setErrorListener(Response.ErrorListener errorListener) {
            this.errorListener = errorListener;
            return this;
        }

        public ActivitiesActRequest build() {
            String url = createUrl();
            ActivitiesActRequest request = new ActivitiesActRequest(METHOD, url,
                    listener, errorListener);
            return request;
        }

        @Override
        public String createUrl() {
            StringBuilder sb = new StringBuilder(PSGOD_BASE_URL);
            sb.append("category/view");
            sb.append("?category_id=").append(categoryId);

            String url = sb.toString();
            Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
                    "createUrl: " + url);
            return url;
        }
    }
}
