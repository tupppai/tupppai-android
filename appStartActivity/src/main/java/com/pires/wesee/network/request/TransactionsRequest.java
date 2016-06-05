package com.pires.wesee.network.request;

import com.alibaba.fastjson.JSON;
import com.android.volley.Response;
import com.pires.wesee.Logger;
import com.pires.wesee.model.Transactions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by Administrator on 2016/1/20 0020.
 */


/**
 * thread/reward
 */
public class TransactionsRequest extends BaseRequest<List<Transactions>> {
    private final static String TAG = TransactionsRequest.class.getSimpleName();

    public TransactionsRequest(int method, String url, Response.Listener<List<Transactions>> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    @Override
    protected List<Transactions> doParseNetworkResponse(JSONObject reponse) throws UnsupportedEncodingException, JSONException {
        List<Transactions> transactionses = JSON.
                parseArray(reponse.getJSONArray("data").toString(), Transactions.class);
        return transactionses;
    }

    public static class Builder implements IGetRequestBuilder {

        private int page;
        private Response.Listener<List<Transactions>> listener;
        private Response.ErrorListener errorListener;

        public Builder setPage(int page) {
            this.page = page;
            return this;
        }

        public Builder setListener(Response.Listener<List<Transactions>> listener) {
            this.listener = listener;
            return this;
        }

        public Builder setErrorListener(Response.ErrorListener errorListener) {
            this.errorListener = errorListener;
            return this;
        }

        public TransactionsRequest build() {
            String url = createUrl();
            TransactionsRequest request = new TransactionsRequest(METHOD, url,
                    listener, errorListener);
            return request;
        }

        @Override
        public String createUrl() {
            StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);
            sb.append("profile/transactions");
            sb.append("?page=").append(page);

            String url = sb.toString();
            Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
                    "createUrl: " + url);
            return url;
        }
    }

}
