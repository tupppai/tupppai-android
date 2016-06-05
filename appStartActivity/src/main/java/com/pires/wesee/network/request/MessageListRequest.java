package com.pires.wesee.network.request;

import android.content.res.Resources;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.pires.wesee.Constants;
import com.pires.wesee.Logger;
import com.pires.wesee.PSGodApplication;
import com.pires.wesee.model.notification.NotificationMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MessageListRequest extends BaseRequest<List<NotificationMessage>> {

	private static final String TAG = NotificationListRequest.class
			.getSimpleName();

	public MessageListRequest(int method, String url,
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
		
		public static final int MESSAGE_TYPE_LIKE = 0;
		public static final int MESSAGE_TYPE_SYSTEM = 1;
		public static final int MESSAGE_TYPE_COMMENT = 2;

		private int type;
		private int mPage;
		private int mSize = 10;
		private int mWidth = Constants.WIDTH_OF_SCREEN;
		private long mLastUpdated = -1;
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
		
		public Builder setType (int type) {
			this.type = type;
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

		public MessageListRequest build() {
			String url = createUrl();
			MessageListRequest request = new MessageListRequest(METHOD,
					url, listener, errorListener);
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(PSGOD_BASE_URL);
			sb.append("message/index?type=");
			
			switch (type){
			case MESSAGE_TYPE_LIKE:
				sb.append("like");
				break;
			case MESSAGE_TYPE_SYSTEM:
				sb.append("system");
				break;
			case MESSAGE_TYPE_COMMENT:
				sb.append("comment");
				break;

			}

			if (mLastUpdated != -1) {
				sb.append("&last_updated=").append(mLastUpdated);
			}
			
			sb.append("&width=").append(mWidth);
//			sb.append("&last_updated=").append(mLastUpdated);
			sb.append("&page=").append(mPage);
			sb.append("&size=").append(mSize);

			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}

	}
}
