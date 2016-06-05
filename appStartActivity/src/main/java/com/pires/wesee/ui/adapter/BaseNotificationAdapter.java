package com.pires.wesee.ui.adapter;

import android.widget.BaseAdapter;

import com.pires.wesee.model.notification.INotification;

import java.util.List;

/**
 * 使用NotificationListView时必须继承BaseNotificationAdapter
 * 
 * @author Rayal
 * 
 */
public abstract class BaseNotificationAdapter extends BaseAdapter {
	public abstract List<? extends INotification> getNotificationList();
}
