package com.psgod.network.request;

import com.android.volley.Response;
import com.psgod.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ZouMengyuan on 15/11/26.
 *
 * 重置密码，验证验证码
 */


public class CheckVerifyCodeRequest extends BaseRequest<Boolean> {

    private static final String TAG = CheckVerifyCodeRequest.class
            .getSimpleName();

    private CheckVerifyCodeRequest(int method, String url,
                                 Response.Listener<Boolean> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    @Override
    protected Boolean doParseNetworkResponse(JSONObject response)
            throws JSONException {
        return true;
    }

    public static class Builder implements IPostRequestBuilder {

        private String mVerifyCode;
        private Response.Listener<Boolean> listener;
        private Response.ErrorListener errorListener;

        public Builder setVerifyCode(String mVerifyCode) {
            this.mVerifyCode = mVerifyCode;
            return this;
        }

        public Builder setListener(Response.Listener<Boolean> listener) {
            this.listener = listener;
            return this;
        }

        public Builder setErrorListener(Response.ErrorListener errorListener) {
            this.errorListener = errorListener;
            return this;
        }

        public CheckVerifyCodeRequest build() {
            String url = createUrl();
            CheckVerifyCodeRequest request = new CheckVerifyCodeRequest(METHOD,
                    url, listener, errorListener) {
                @Override
                public Map<String, String> getParams() {
                    return getPackParams(createParameters());
                }
            };
            Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
                    "createUrl: " + url + createParameters());
            return request;
        }

        @Override
        public String createUrl() {
            StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL)
                    .append("account/checkAuthCode");
            String url = sb.toString();
            return url;
        }

        @Override
        public Map<String, String> createParameters() {
            Map<String, String> params = new HashMap<String, String>();
            params.put("code", mVerifyCode);
            return params;
        }

    }
}
