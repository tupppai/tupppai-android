package com.psgod.model.notification;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.psgod.PsGodImageLoader;
import com.psgod.Constants;
import com.psgod.PsGodImageLoader;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.PhotoItem;
import com.psgod.model.User;
import com.psgod.ui.view.ViewUtils;
import com.psgod.ui.widget.AvatarImageView;

import org.json.JSONException;
import org.json.JSONObject;

public class FollowNotification implements INotification {
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
					R.layout.item_notification_follow_list, null);
			viewHolder = new ViewHolder();
			viewHolder.avatarIv = (AvatarImageView) convertView
					.findViewById(R.id.item_notification_follow_list_avatar_imgview);
			// viewHolder.genderIv = (CircleImageView) convertView
			// .findViewById(R.id.item_notification_follow_list_gender_imgview);
			viewHolder.nameTv = (TextView) convertView
					.findViewById(R.id.item_notification_follow_list_name_tv);
			viewHolder.timeTv = (TextView) convertView
					.findViewById(R.id.item_notification_follow_list_time_tv);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		// 设置头像点击跳转
		viewHolder.avatarIv.setUser(new User(mNotificationMessage));

		Resources res = context.getResources();
		// int genderDrawableId = (mNotificationMessage.getGender() == 0) ?
		// R.drawable.woman
		// : R.drawable.man;
		PsGodImageLoader imageLoader = PsGodImageLoader.getInstance();
		imageLoader.displayImage(mNotificationMessage.getAvatar(),
				viewHolder.avatarIv, mAvatarOptions);
		// viewHolder.genderIv.setImageDrawable(res.getDrawable(genderDrawableId));
		viewHolder.nameTv.setText(mNotificationMessage.getNickName());
		viewHolder.timeTv.setText(Utils.getTimeFormatText(mNotificationMessage
				.getCreatedTime()));

		// TODO 字体
		ViewUtils.setTextTypeFace(context, viewHolder.nameTv);
		ViewUtils.setTextTypeFace(context, viewHolder.timeTv);

		// convertView.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View arg0) {
		// Intent intent = new Intent(PSGodApplication.getAppContext(),
		// UserProfileActivity.class);
		// intent.putExtra(Constants.IntentKey.USER_ID,
		// mNotificationMessage.getUid());
		// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// PSGodApplication.getAppContext().startActivity(intent);
		// }
		// });

		return convertView;
	}

	private static class ViewHolder {
		AvatarImageView avatarIv;
		// CircleImageView genderIv;
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
	public static FollowNotification createFromJSON(JSONObject jsonObj)
			throws JSONException {
		FollowNotification notification = new FollowNotification();
		notification.mNotificationMessage = NotificationMessage
				.createNotification(jsonObj);
		return notification;
	}

	@Override
	public long getNotificationUid() {
		return (mNotificationMessage != null) ? mNotificationMessage.getUid()
				: -1;
	}

	@Override
	public PhotoItem getNotificationPhotoItem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NotificationMessage getNotificationMessage() {
		if (mNotificationMessage != null) {
			return mNotificationMessage;
		}
		return null;
	}
}
