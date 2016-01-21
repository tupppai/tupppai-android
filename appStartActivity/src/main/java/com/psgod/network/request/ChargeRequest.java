package com.psgod.network.request;

import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 2016/1/20 0020.
 */


/**
 * money/charge
 */
public class ChargeRequest extends BaseRequest {
    public ChargeRequest(int method, String url, Response.Listener listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    @Override
    protected Object doParseNetworkResponse(JSONObject reponse) throws UnsupportedEncodingException, JSONException {
        return null;
    }



    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
