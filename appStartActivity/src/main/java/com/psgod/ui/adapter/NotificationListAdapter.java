package com.psgod.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.psgod.model.notification.INotification;

import java.util.List;

/**
 * 消息类公用Adapter
 * 
 * @author Rayal
 */
public class NotificationListAdapter extends BaseNotificationAdapter {
	private static final String TAG = NotificationListAdapter.class
			.getSimpleName();

	private Context mContext;
	private List<? extends INotification> mNotificationList;

	public NotificationListAdapter(Context context,
			List<? extends INotification> notificationList) {
		mContext = context;
		mNotificationList = notificationList;
	}

	@Override
	public int getCount() {
		return mNotificationList.size();
	}

	@Override
	public Object getItem(int position) {
		if ((position < 0) || (position >= mNotificationList.size())) {
			return null;
		} else {
			return mNotificationList.get(position);
		}
	}

	@Override
	public long getItemId(int position) {
		Object obj = getItem(position);
		if (obj == null) {
			return -1;
		} else {
			INotification notification = (INotification) obj;
			return notification.getNotificationId();
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Object obj = getItem(position);
		if (!(obj instanceof INotification)) {
			return null;
		} else {
			return ((INotification) obj).bindNotificationView(mContext,
					convertView, parent);
		}
	}

	@Override
	public List<? extends INotification> getNotificationList() {
		return mNotificationList;
	}
}
