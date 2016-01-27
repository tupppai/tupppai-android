package com.psgod.network.request;

import com.android.volley.Response;
import com.psgod.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 2016/1/20 0020.
 */


/**
 * thread/reward
 */
public class MoneyTransferRequest extends BaseRequest<JSONObject> {
    private final static String TAG = MoneyTransferRequest.class.getSimpleName();

    public MoneyTransferRequest(int method, String url, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    @Override
    protected JSONObject doParseNetworkResponse(JSONObject reponse) throws UnsupportedEncodingException, JSONException {
        return reponse;
    }

    public static class Builder implements IGetRequestBuilder {

        private String amount;
        private String type;
        private Response.Listener<JSONObject> listener;
        private Response.ErrorListener errorListener;

        public Builder setAmount(String amount) {
            this.amount = amount;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
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

        public MoneyTransferRequest build() {
            String url = createUrl();
            MoneyTransferRequest request = new MoneyTransferRequest(METHOD, url,
                    listener, errorListener);
            return request;
        }

        @Override
        public String createUrl() {
            StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);
            sb.append("money/transfer");
            sb.append("?amount=").append(amount);

            String url = sb.toString();
            Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
                    "createUrl: " + url);
            return url;
        }
    }

}
