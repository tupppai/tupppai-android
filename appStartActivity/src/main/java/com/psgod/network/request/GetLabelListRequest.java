package com.psgod.network.request;

import com.android.volley.Response;
import com.psgod.Logger;
import com.psgod.model.Label;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取上传求P的标签
 *
 * @author ZouMengyuan
 */
public class GetLabelListRequest extends BaseRequest<List<Label>>{

    private static final String TAG = GetLabelListRequest.class.getSimpleName();

    public GetLabelListRequest(int method, String url,
                              Response.Listener<List<Label>> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    @Override
    protected List<Label> doParseNetworkResponse(JSONObject reponse)
            throws UnsupportedEncodingException, JSONException {
        JSONArray data = reponse.getJSONArray("data");
        List<Label> mLabels = new ArrayList<Label>();

        for (int i = 0; i < data.length(); i++) {
            mLabels.add(Label.createLabel(data.getJSONObject(i)));
        }
        return mLabels;
    }

    public static class Builder implements IGetRequestBuilder {
        private Response.Listener<List<Label>> listener;
        private Response.ErrorListener errorListener;

        public Builder setListener(Response.Listener<List<Label>> listener) {
            this.listener = listener;
            return this;
        }

        public Builder setErrorListener(Response.ErrorListener errorListener) {
            this.errorListener = errorListener;
            return this;
        }

        public GetLabelListRequest build() {
            String url = createUrl();
            GetLabelListRequest request = new GetLabelListRequest(METHOD, url,
                    listener, errorListener);
            return request;
        }

        @Override
        public String createUrl() {
            StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);
            sb.append("tag/index");

            String url = sb.toString();
            Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
                    "createUrl: " + url);
            return url;
        }
    }

}
