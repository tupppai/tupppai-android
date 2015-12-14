package com.psgod.model.notification;

import android.content.Context;
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

public class ReplyNotification implements INotification {
	private NotificationMessage mNotificationMessage;
	private PhotoItem mPhotoItem;
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
			viewHolder.imageView = (ImageView) convertView
					.findViewById(R.id.item_notification_reply_list_iv);
			viewHolder.timeTv = (TextView) convertView
					.findViewById(R.id.item_notification_reply_list_time_tv);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		// 设置头像点击跳转
		viewHolder.avatarIv.setUserId(mNotificationMessage.getUid());

		viewHolder.nameTv.setText(mNotificationMessage.getNickName());
		viewHolder.timeTv.setText(Utils.getTimeFormatText(mNotificationMessage
				.getCreatedTime()));
		PsGodImageLoader imageLoader = PsGodImageLoader.getInstance();
		imageLoader.displayImage(mNotificationMessage.getAvatar(),
				viewHolder.avatarIv, mAvatarOptions);
		// viewHolder.imageView.getLayoutParams().width = 100;
		// viewHolder.imageView.getLayoutParams().height = 100;
		imageLoader.displayImage(mPhotoItem.getImageURL(),
				viewHolder.imageView, mOptions);

		// TODO 字体
		ViewUtils.setTextTypeFace(context, viewHolder.nameTv);
		ViewUtils.setTextTypeFace(context, viewHolder.timeTv);

		// convertView.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View arg0) {
		// PhotoDetailActivity.startActivity(PSGodApplication.getAppContext(),
		// mPhotoItem);
		// }
		// });

		return convertView;
	}

	private static class ViewHolder {
		AvatarImageView avatarIv;
		ImageView imageView;
		TextView nameTv;
		TextView timeTv;
	}

	/**
	 * 解析JSON数据
	 * 
	 * @param jsonObj
	 * @return
	 * @throws JSONException
	 */
	public static ReplyNotification createFromJSON(JSONObject jsonObj)
			throws JSONException {
		ReplyNotification notification = new ReplyNotification();
		if (jsonObj.has("reply")) {
			notification.mNotificationMessage = NotificationMessage
					.createNotification(jsonObj.getJSONObject("reply"));
		}
		if (jsonObj.has("ask")) {
			notification.mPhotoItem = PhotoItem.createPhotoItem(jsonObj
					.getJSONObject("ask"));
		}
		return notification;
	}

	@Override
	public long getNotificationUid() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public PhotoItem getNotificationPhotoItem() {
		return (mPhotoItem != null) ? mPhotoItem : null;
	}

	@Override
	public NotificationMessage getNotificationMessage() {
		if (mNotificationMessage != null) {
			return mNotificationMessage;
		}
		return null;
	}
}
