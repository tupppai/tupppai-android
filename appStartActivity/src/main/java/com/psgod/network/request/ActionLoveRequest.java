package com.psgod.network.request;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * 点赞请求
 *
 * @author rayalyuan
 */
public class ActionLoveRequest extends BaseRequest<Boolean> {
    private static final String TAG = ActionLoveRequest.class.getSimpleName();

    public ActionLoveRequest(int method, String url,
                             Listener<Boolean> listener, ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    @Override
    protected Boolean doParseNetworkResponse(JSONObject reponse)
            throws UnsupportedEncodingException, JSONException {
        return true;
    }

    public static class Builder implements IGetRequestBuilder {

        private int num; // 0 取消 1 点赞
        private long pid;
        private Listener<Boolean> listener;
        private ErrorListener errorListener;

        public Builder setPid(long pid) {
            this.pid = pid;
            return this;
        }

        public Builder setNum(int s) {
            this.num = s;
            return this;
        }

        public Builder setListener(Listener<Boolean> listener) {
            this.listener = listener;
            return this;
        }

        public Builder setErrorListener(ErrorListener errorListener) {
            this.errorListener = errorListener;
            return this;
        }

        public ActionLoveRequest build() {
            String url = createUrl();
            ActionLoveRequest request = new ActionLoveRequest(METHOD, url,
                    listener, errorListener);

            Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
                    "createUrl: " + url);
            return request;
        }

        @Override
        public String createUrl() {
            StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);
            sb.append("reply/loveReply/" + pid);
            sb.append("?num=" + num);

            if(num == -1){
                sb.append("&status=").append(0);
            }

            String url = sb.toString();
            Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
                    "createUrl: " + url);
            return url;
        }
    }
}
