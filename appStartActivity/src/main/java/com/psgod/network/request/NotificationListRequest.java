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
import com.psgod.R;
import com.psgod.model.notification.CommentNotification;
import com.psgod.model.notification.FollowNotification;
import com.psgod.model.notification.INotification;
import com.psgod.model.notification.InviteNotification;
import com.psgod.model.notification.ReplyNotification;
import com.psgod.model.notification.SystemNotification;

/**
 * 消息列表网络请求
 * 
 * @author brandwang
 */
public class NotificationListRequest extends
		BaseRequest<List<? extends INotification>> {
	private static final String TAG = NotificationListRequest.class
			.getSimpleName();

	public static final int TYPE_COMMENT_NOTIFICATION = 1;
	public static final int TYPE_REPLY_NOTIFICATION = 2;
	public static final int TYPE_INVITE_NOTIFICATION = 3;
	public static final int TYPE_FOLLOW_NOTIFICATION = 4;
	public static final int TYPE_SYSTEM_NOTIFICATION = 5;

	private int mNotificationType;

	public NotificationListRequest(int method, String url,
			Listener<List<? extends INotification>> listener,
			ErrorListener errorListener, int notificationType) {
		super(method, url, listener, errorListener);
		mNotificationType = notificationType;
	}

	@Override
	protected List<? extends INotification> doParseNetworkResponse(
			JSONObject reponse) throws UnsupportedEncodingException,
			JSONException {
		JSONArray data = reponse.getJSONArray("data");
		List<INotification> notificationList = new ArrayList<INotification>();

		int length = data.length();
		for (int i = 0; i < length; i++) {
			JSONObject obj = data.getJSONObject(i);
			INotification notification = null;
			switch (mNotificationType) {
			case TYPE_COMMENT_NOTIFICATION:
				notification = CommentNotification.createFromJSON(obj);
				break;
			case TYPE_REPLY_NOTIFICATION:
				notification = ReplyNotification.createFromJSON(obj);
				break;
			case TYPE_INVITE_NOTIFICATION:
				notification = InviteNotification.createFromJSON(obj);
				break;
			case TYPE_FOLLOW_NOTIFICATION:
				notification = FollowNotification.createFromJSON(obj);
				break;
			case TYPE_SYSTEM_NOTIFICATION:
				notification = SystemNotification.createFromJSON(obj);
				break;
			default:
				Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_DEV, TAG,
						"doParseNetworkResponse(): unknown notification type",
						true);
				break;
			}
			notificationList.add(notification);
		}
		return notificationList;
	}

	public static class Builder implements IGetRequestBuilder {
		Resources res = PSGodApplication.getAppContext().getResources();

		private int mPage;
		private int mSize = 10;
		private int mWidth = Constants.WIDTH_OF_SCREEN - 2
				* res.getDimensionPixelSize(R.dimen.photo_margin);
		private int mType;
		private long mLastUpdated;
		private Listener<List<? extends INotification>> listener;
		private ErrorListener errorListener;

		public Builder setType(int type) {
			this.mType = type;
			return this;
		}

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

		public Builder setListener(
				Listener<List<? extends INotification>> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.errorListener = errorListener;
			return this;
		}

		public NotificationListRequest build() {
			String url = createUrl();
			NotificationListRequest request = new NotificationListRequest(
					METHOD, url, listener, errorListener, mType);
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(BaseRequest.PSGOD_BASE_URL);
			sb.append("message/list?type=");

			if (mType == TYPE_COMMENT_NOTIFICATION) {
				sb.append("comment");
			} else if (mType == TYPE_REPLY_NOTIFICATION) {
				sb.append("reply");
			} else if (mType == TYPE_INVITE_NOTIFICATION) {
				sb.append("invite");
			} else if (mType == TYPE_FOLLOW_NOTIFICATION) {
				sb.append("follow");
			} else if (mType == TYPE_SYSTEM_NOTIFICATION) {
				sb.append("system");
			}

			sb.append("&width=").append(mWidth);
			sb.append("&last_updated=").append(mLastUpdated);
			sb.append("&page=").append(mPage);
			sb.append("&size=").append(mSize);

			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}

	}
}
