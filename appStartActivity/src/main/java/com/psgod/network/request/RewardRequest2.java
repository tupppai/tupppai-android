package com.psgod.network.request;

import com.alibaba.fastjson.JSON;
import com.android.volley.Response;
import com.psgod.Logger;
import com.psgod.model.Reward;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 2016/1/20 0020.
 */


/**
 * thread/reward
 */
public class RewardRequest2 extends BaseRequest<JSONObject> {
    private final static String TAG = RewardRequest2.class.getSimpleName();

    public RewardRequest2(int method, String url, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    @Override
    protected JSONObject doParseNetworkResponse(JSONObject reponse) throws UnsupportedEncodingException, JSONException {
        return reponse.getJSONObject("data");
    }

    public static class Builder implements IGetRequestBuilder {

        private String id;
        private double amount;
        private Response.Listener<JSONObject> listener;
        private Response.ErrorListener errorListener;

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setAmount(double amount) {
            this.amount = amount;
            return this;
        }

        public Builder setListener(Response.Listener<JSONObject> listener) {
            this.listener = listener;
            return this;
        }

        public Builder setErrorListener(Response.ErrorListener errorListener) {
            this.errorListener = errorListener;
            return this;
        }

        public RewardRequest2 build() {
            String url = createUrl();
            RewardRequest2 request = new RewardRequest2(METHOD, url,
                    listener, errorListener);
            return request;
        }

        @Override
        public String createUrl() {
            StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);
            sb.append("money/reward");
            sb.append("?ask_id=").append(id);

            String url = sb.toString();
            Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
                    "createUrl: " + url);
            return url;
        }
    }

}
