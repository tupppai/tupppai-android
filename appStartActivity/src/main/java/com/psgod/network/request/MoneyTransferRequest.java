package com.psgod.network.request;

import com.alibaba.fastjson.JSON;
import com.android.volley.Response;
import com.psgod.Logger;
import com.psgod.model.MoneyTransfer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 2016/1/20 0020.
 */


/**
 * thread/reward
 */
public class MoneyTransferRequest extends BaseRequest<MoneyTransfer> {
    private final static String TAG = MoneyTransferRequest.class.getSimpleName();

    public MoneyTransferRequest(int method, String url, Response.Listener<MoneyTransfer> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    @Override
    protected MoneyTransfer doParseNetworkResponse(JSONObject reponse) throws UnsupportedEncodingException, JSONException {
        MoneyTransfer transfer = JSON.
                parseObject(reponse.getJSONObject("data").toString(),MoneyTransfer.class);
        return transfer;
    }

    public static class Builder implements IGetRequestBuilder {

        private String amount;
        private String type;
        private String code;
        private Response.Listener<MoneyTransfer> listener;
        private Response.ErrorListener errorListener;

        public Builder setCode(String code) {
            this.code = code;
            return this;
        }

        public Builder setAmount(String amount) {
            this.amount = amount;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setListener(Response.Listener<MoneyTransfer> listener) {
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
//            sb.append("&code=").append(code);

            String url = sb.toString();
            Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
                    "createUrl: " + url);
            return url;
        }
    }

}
