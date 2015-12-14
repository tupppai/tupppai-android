package com.psgod.model.notification;

/**
 * 系统通知
 */

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.psgod.PsGodImageLoader;
import com.psgod.Constants;
import com.psgod.PsGodImageLoader;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.PhotoItem;
import com.psgod.ui.view.ViewUtils;
import com.psgod.ui.widget.AvatarImageView;

import org.json.JSONException;
import org.json.JSONObject;

public class SystemNotification implements INotification {
	private NotificationMessage mNotificationMessage;

	private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS;
	private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

	@Override
	public long getNotificationId() {
		return (mNotificationMessage != null) ? mNotificationMessage.getNid()
				: -1;
	}

	@Override
	public View bindNotificationView(Context context, View convertView,
			ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_notification_reply_list, null);
			viewHolder = new ViewHolder();
			viewHolder.avatarIv = (AvatarImageView) convertView
					.findViewById(R.id.item_notification_reply_list_avatar_imgview);
			viewHolder.nameTv = (TextView) convertView
					.findViewById(R.id.item_notification_reply_list_name_tv);
			viewHolder.contentTv = (TextView) convertView
					.findViewById(R.id.item_notification_reply_list_content_tv);
			viewHolder.imageView = (ImageView) convertView
					.findViewById(R.id.item_notification_reply_list_iv);
			viewHolder.timeTv = (TextView) convertView
					.findViewById(R.id.item_notification_reply_list_time_tv);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		// 设置头像点击跳转
		// viewHolder.avatarIv.setUserId(mNotificationMessage.getUid());

		viewHolder.contentTv.setText(mNotificationMessage.getContent());
		viewHolder.nameTv.setText(TextUtils.isEmpty(mNotificationMessage
				.getNickName()) ? "官方账号" : mNotificationMessage.getNickName());
		viewHolder.timeTv.setText(Utils.getTimeFormatText(mNotificationMessage
				.getCreatedTime()));
		PsGodImageLoader imageLoader = PsGodImageLoader.getInstance();
		imageLoader.displayImage(mNotificationMessage.getAvatar(),
				viewHolder.avatarIv, mAvatarOptions);

		if (mNotificationMessage.getPicUrl() != null
				&& !TextUtils.isEmpty(mNotificationMessage.getPicUrl())) {
			imageLoader.displayImage(mNotificationMessage.getPicUrl(),
					viewHolder.imageView, mOptions);
		}

		// TODO 字体
		ViewUtils.setTextTypeFace(context, viewHolder.nameTv);
		ViewUtils.setTextTypeFace(context, viewHolder.timeTv);

		return convertView;
	}

	private static class ViewHolder {
		AvatarImageView avatarIv;
		ImageView imageView;
		TextView nameTv;
		TextView contentTv;
		TextView timeTv;
	}

	@Override
	public long getNotificationUid() {
		return 0;
	}

	@Override
	public PhotoItem getNotificationPhotoItem() {
		return null;
	}

	// 解析JSON数据
	public static INotification createFromJSON(JSONObject obj)
			throws JSONException {
		SystemNotification notification = new SystemNotification();
		if (obj != null) {
			notification.mNotificationMessage = NotificationMessage
					.createNotification(obj);
		}
		return notification;
	}

	@Override
	public NotificationMessage getNotificationMessage() {
		if (mNotificationMessage != null) {
			return mNotificationMessage;
		}
		return null;
	}
}
