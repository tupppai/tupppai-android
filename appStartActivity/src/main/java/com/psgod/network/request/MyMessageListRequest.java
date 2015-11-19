package com.psgod.network.request;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.psgod.Constants;
import com.psgod.Logger;
import com.psgod.PSGodApplication;
import com.psgod.model.notification.NotificationMessage;

/**
 * 消息列表网络请求 v2.0
 * 
 * @author brandwang
 */
public class MyMessageListRequest extends
		BaseRequest<List<NotificationMessage>> {
	private static final String TAG = NotificationListRequest.class
			.getSimpleName();

	public MyMessageListRequest(int method, String url,
			Listener<List<NotificationMessage>> listener,
			ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected List<NotificationMessage> doParseNetworkResponse(
			JSONObject reponse) throws UnsupportedEncodingException,
			JSONException {
		JSONArray data = reponse.getJSONArray("data");
		List<NotificationMessage> messageList = new ArrayList<NotificationMessage>();

		int length = data.length();
		for (int i = 0; i < length; i++) {
			JSONObject obj = data.getJSONObject(i);

			NotificationMessage message = NotificationMessage
					.createNotification(obj);
			messageList.add(message);
		}
		return messageList;
	}

	public static class Builder implements IGetRequestBuilder {
		Resources res = PSGodApplication.getAppContext().getResources();

		private int mPage;
		private int mSize = 10;
		private int mWidth = Constants.WIDTH_OF_SCREEN;
		private long mLastUpdated;
		private Listener<List<NotificationMessage>> listener;
		private ErrorListener errorListener;

		public Builder setPage(int page) {
			this.mPage = page;
			return this;
		}

		public Builder setSize(int size) {
			this.mSize = size;
			return this;
		}

		public Builder setWidth(int width) {
			this.mWidth = width;
			return this;
		}

		public Builder setLastUpdated(long lastUpdated) {
			this.mLastUpdated = lastUpdated;
			return this;
		}

		public Builder setListener(Listener<List<NotificationMessage>> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.errorListener = errorListener;
			return this;
		}

		public MyMessageListRequest build() {
			String url = createUrl();
			MyMessageListRequest request = new MyMessageListRequest(METHOD,
					url, listener, errorListener);
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);
			sb.append("message/index");
			sb.append("?width=").append(mWidth);
			sb.append("&page=").append(mPage);
			sb.append("&size=").append(mSize);

			if (mLastUpdated != 0) {
				sb.append("&last_updated=").append(mLastUpdated);
			}

			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}

	}
}
