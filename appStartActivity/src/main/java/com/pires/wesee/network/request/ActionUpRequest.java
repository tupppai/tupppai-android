package com.pires.wesee.network.request;

/**
 * 求P 回复点赞 取消点赞
 * @author brandwang
 */

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class ActionUpRequest extends BaseRequest<Boolean> {
	private static final String TAG = ActionUpRequest.class.getSimpleName();

	public ActionUpRequest(int method, String url, Listener<Boolean> listener,
			ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected Boolean doParseNetworkResponse(JSONObject reponse)
			throws UnsupportedEncodingException, JSONException {
		// TODO Auto-generated method stub
		return null;
	}

}
