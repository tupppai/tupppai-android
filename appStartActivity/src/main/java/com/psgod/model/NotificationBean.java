package com.psgod.model;

import com.psgod.PSGodApplication;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * 观察者模式 维护消息通知页面 数量变化
 * 
 * @author brandwang
 */
public class NotificationBean {
	public static final int NOTIFICATION_TYPE_COMMENT = 1;
	public static final int NOTIFICATION_TYPE_REPLY = 2;
	public static final int NOTIFICATION_TYPE_FOLLOW = 3;
	public static final int NOTIFICATION_TYPE_INVITE = 4;
	public static final int NOTIFICATION_TYPE_SYSTEM = 5;

	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
			PSGodApplication.getAppContext());
	private int mType;
	private int mCount = 0;

	public void setNotification(int type, int count) {
		int oldCount = mCount;
		mType = type;
		mCount = count;
		propertyChangeSupport.firePropertyChange("type", oldCount, mCount);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
}
