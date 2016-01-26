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
public class RewardRequest extends BaseRequest<Reward> {
    private final static String TAG = RewardRequest.class.getSimpleName();

    public RewardRequest(int method, String url, Response.Listener<Reward> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    @Override
    protected Reward doParseNetworkResponse(JSONObject reponse) throws UnsupportedEncodingException, JSONException {
        Reward reward = JSON.parseObject(reponse.getJSONObject("data").toString(),Reward.class);
        return reward;
    }

    public static class Builder implements IGetRequestBuilder {

        private String id;
        private Response.Listener<Reward> listener;
        private Response.ErrorListener errorListener;

        public Builder setId(String id) {
            this.id = id;
            return this;
        }


        public Builder setListener(Response.Listener<Reward> listener) {
            this.listener = listener;
            return this;
        }

        public Builder setErrorListener(Response.ErrorListener errorListener) {
            this.errorListener = errorListener;
            return this;
        }

        public RewardRequest build() {
            String url = createUrl();
            RewardRequest request = new RewardRequest(METHOD, url,
                    listener, errorListener);
            return request;
        }

        @Override
        public String createUrl() {
            StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);
            sb.append("thread/reward");
            sb.append("?ask_id=").append(id);

            String url = sb.toString();
            Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
                    "createUrl: " + url);
            return url;
        }
    }

}
