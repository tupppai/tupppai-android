package com.psgod.ui.adapter;

import android.widget.BaseAdapter;

import com.psgod.model.notification.INotification;

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
