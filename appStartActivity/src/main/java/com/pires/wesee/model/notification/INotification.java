package com.pires.wesee.model.notification;

/**
 * 消息类接口
 */
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.pires.wesee.model.PhotoItem;

public interface INotification {
	// 获取消息
	public NotificationMessage getNotificationMessage();

	public long getNotificationId();

	// 获取消息人的id
	public long getNotificationUid();

	// 获取消息中附带的photoitem信息
	public PhotoItem getNotificationPhotoItem();

	/**
	 * 各个消息样式定义
	 * 
	 * @param context
	 * @param convertView
	 * @param parent
	 * @return
	 */
	public View bindNotificationView(Context context, View convertView,
			ViewGroup parent);
}
