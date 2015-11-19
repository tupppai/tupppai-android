package com.psgod.ui.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.model.PhotoItem;
import com.psgod.model.notification.NotificationMessage;
import com.psgod.ui.activity.CarouselPhotoDetailActivity;
import com.psgod.ui.activity.CommentListActivity;
import com.psgod.ui.activity.MessageSystemActivity;
import com.psgod.ui.activity.SinglePhotoDetail;
import com.psgod.ui.activity.UserProfileActivity;
import com.psgod.ui.activity.WebBrowserActivity;
import com.psgod.ui.widget.AvatarImageView;
import com.psgod.ui.widget.EditPopupWindow;

public class MessageListAdapter extends BaseAdapter {
	private Context mContext;
	private List<NotificationMessage> mMessages;

	// UIL配置
	private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS;
	private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

	public MessageListAdapter(Context context,
			List<NotificationMessage> messages) {
		mContext = context;
		mMessages = messages;
	}

	@Override
	public int getCount() {
		return mMessages.size();
	}

	@Override
	public Object getItem(int position) {
		if ((position < 0) || (position >= mMessages.size())) {
			return null;
		} else {
			return mMessages.get(position);
		}
	}

	@Override
	public long getItemId(int position) {
		Object obj = getItem(position);
		if (obj instanceof NotificationMessage) {
			NotificationMessage message = (NotificationMessage) obj;
			return message.getNid();
		}
		return -1;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolderSystem viewHolderSystem = null;
		ViewHolderComment viewHolderComment = null;
		ViewHolderFollow viewHolderFollow = null;
		ViewHolderLike viewHolderLike = null;
		ViewHolderReply viewHolderReply = null;

		ImageLoader imageLoader = ImageLoader.getInstance();

		final NotificationMessage message = (NotificationMessage) getItem(position);
		int type = message.getType();

		// if (convertView == null) {
		switch (type) {
		case Constants.PUSH_MESSAGE_SYSTEM:
			viewHolderSystem = new ViewHolderSystem();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_message_list_system, null);
			viewHolderSystem.avatarIv = (AvatarImageView) convertView
					.findViewById(R.id.item_message_list_avatar);
			viewHolderSystem.nameTv = (TextView) convertView
					.findViewById(R.id.item_message_list_nametv);
			viewHolderSystem.systemContentTv = (TextView) convertView
					.findViewById(R.id.item_message_list_content_tv);
			viewHolderSystem.timeTv = (TextView) convertView
					.findViewById(R.id.item_message_list_time_tv);

			convertView.setTag(viewHolderSystem);
			viewHolderSystem.avatarIv.setUserId(message.getUid());
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO 系统消息页面跳转
					NotificationMessage message = mMessages.get(position);
					if (message != null && message.getJumpUrl() != null
							&& !message.getJumpUrl().equals("")) {
						Intent intent = new Intent(mContext,
								WebBrowserActivity.class);
						intent.putExtra(WebBrowserActivity.KEY_URL, message.getJumpUrl());
						mContext.startActivity(intent);
					}

				}
			});

			break;

		case Constants.PUSH_MESSAGE_FOLLOW:
			viewHolderFollow = new ViewHolderFollow();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_message_list_follow, null);

			viewHolderFollow.avatarIv = (AvatarImageView) convertView
					.findViewById(R.id.item_follow_list_avatar);
			viewHolderFollow.nameTv = (TextView) convertView
					.findViewById(R.id.item_follow_list_nametv);
			viewHolderFollow.timeTv = (TextView) convertView
					.findViewById(R.id.item_follow_list_time_tv);

			convertView.setTag(viewHolderFollow);
			viewHolderFollow.avatarIv.setUserId(message.getUid());
			// 关注消息点击打开其他人主页
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext,
							UserProfileActivity.class);
					intent.putExtra(Constants.IntentKey.USER_ID,
							message.getUid());
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(intent);
				}
			});
			break;

		case Constants.PUSH_MESSAGE_LIKE:
			viewHolderLike = new ViewHolderLike();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_message_list_like, null);
			viewHolderLike.avatarIv = (AvatarImageView) convertView
					.findViewById(R.id.item_like_list_avatar);
			viewHolderLike.nameTv = (TextView) convertView
					.findViewById(R.id.item_like_list_nametv);
			viewHolderLike.timeTv = (TextView) convertView
					.findViewById(R.id.item_like_list_time_tv);

			convertView.setTag(viewHolderLike);
			viewHolderLike.avatarIv.setUserId(message.getUid());
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					CarouselPhotoDetailActivity.startActivity(mContext,
							message.getTargetAskId(), message.getTargetId());
				}
			});
			break;

		case Constants.PUSH_MESSAGE_REPLY:
			viewHolderReply = new ViewHolderReply();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_message_list_reply, null);
			viewHolderReply.avatarIv = (AvatarImageView) convertView
					.findViewById(R.id.item_reply_list_avatar);
			viewHolderReply.imageView = (ImageView) convertView
					.findViewById(R.id.item_reply_list_iv);
			viewHolderReply.nameTv = (TextView) convertView
					.findViewById(R.id.item_reply_list_name_tv);
			viewHolderReply.timeTv = (TextView) convertView
					.findViewById(R.id.item_reply_list_time_tv);

			convertView.setTag(viewHolderReply);
			viewHolderReply.avatarIv.setUserId(message.getUid());
			// 回复作品消息 点击跳转详情页
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					CarouselPhotoDetailActivity.startActivity(mContext,
							message.getAskId(), message.getReplyId());
				}
			});
			break;

		case Constants.PUSH_MESSAGE_COMMENT:
			viewHolderComment = new ViewHolderComment();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_message_list_comment, null);
			viewHolderComment.avatarIv = (AvatarImageView) convertView
					.findViewById(R.id.item_comment_list_avatar);
			viewHolderComment.commentTv = (TextView) convertView
					.findViewById(R.id.item_comment_list_content_tv);
			viewHolderComment.imageView = (ImageView) convertView
					.findViewById(R.id.item_comment_list_iv);
			viewHolderComment.nameTv = (TextView) convertView
					.findViewById(R.id.item_comment_list_name_tv);
			viewHolderComment.timeTv = (TextView) convertView
					.findViewById(R.id.item_comment_list_time_tv);

			convertView.setTag(mMessages.get(position));
			viewHolderComment.avatarIv.setUserId(message.getUid());
			// 评论消息点击跳转照片单页
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					PhotoItem photoItem = new PhotoItem();
					NotificationMessage message = (NotificationMessage) v
							.getTag();
					photoItem.setType(message.getTargetType());
					photoItem.setPid(message.getTargetId());
					Intent intent = new Intent(mContext,
							CommentListActivity.class);
					intent.putExtra(Constants.IntentKey.PHOTO_ITEM, photoItem);
					intent.putExtra("comment_id", message.getCommentId());
					mContext.startActivity(intent);
				}
			});

			//
			break;

		default:
			break;
		}
		// } else {
		// switch (type) {
		// case Constants.PUSH_MESSAGE_SYSTEM:
		// viewHolderSystem = (ViewHolderSystem) convertView.getTag();
		// break;
		//
		// case Constants.PUSH_MESSAGE_LIKE:
		// viewHolderLike = (ViewHolderLike) convertView.getTag();
		// break;
		//
		// case Constants.PUSH_MESSAGE_COMMENT:
		// viewHolderComment = (ViewHolderComment) convertView.getTag();
		// break;
		//
		// case Constants.PUSH_MESSAGE_FOLLOW:
		// viewHolderFollow = (ViewHolderFollow) convertView.getTag();
		// break;
		//
		// case Constants.PUSH_MESSAGE_REPLY:
		// viewHolderReply = (ViewHolderReply) convertView.getTag();
		// break;
		//
		// default:
		// break;
		// }
		// }

		switch (type) {
		case Constants.PUSH_MESSAGE_SYSTEM:
			imageLoader.displayImage(message.getAvatar(),
					viewHolderSystem.avatarIv, mAvatarOptions);
			viewHolderSystem.nameTv.setText(message.getNickName());
			viewHolderSystem.systemContentTv.setText(message.getContent());
			viewHolderSystem.timeTv
					.setText(PhotoItem.getUpdateTimeStr(mMessages.get(position)
							.getCreatedTime()));
			break;

		case Constants.PUSH_MESSAGE_LIKE:
			imageLoader.displayImage(message.getAvatar(),
					viewHolderLike.avatarIv, mAvatarOptions);
			viewHolderLike.nameTv.setText(message.getNickName());
			viewHolderLike.timeTv.setText(PhotoItem.getUpdateTimeStr(mMessages
					.get(position).getCreatedTime()));
			break;

		case Constants.PUSH_MESSAGE_COMMENT:
			imageLoader.displayImage(message.getAvatar(),
					viewHolderComment.avatarIv, mAvatarOptions);
			imageLoader.displayImage(message.getPicUrl(),
					viewHolderComment.imageView, mOptions);
			viewHolderComment.nameTv.setText(message.getNickName());
			viewHolderComment.commentTv.setText(message.getContent());
			viewHolderComment.timeTv
					.setText(PhotoItem.getUpdateTimeStr(mMessages.get(position)
							.getCreatedTime()));
			break;

		case Constants.PUSH_MESSAGE_FOLLOW:
			imageLoader.displayImage(message.getAvatar(),
					viewHolderFollow.avatarIv, mAvatarOptions);
			viewHolderFollow.nameTv.setText(message.getNickName());
			viewHolderFollow.timeTv
					.setText(PhotoItem.getUpdateTimeStr(mMessages.get(position)
							.getCreatedTime()));
			break;

		case Constants.PUSH_MESSAGE_REPLY:
			imageLoader.displayImage(message.getAvatar(),
					viewHolderReply.avatarIv, mAvatarOptions);
			imageLoader.displayImage(message.getPicUrl(),
					viewHolderReply.imageView, mOptions);
			viewHolderReply.nameTv.setText(message.getNickName());
			viewHolderReply.timeTv.setText(PhotoItem.getUpdateTimeStr(mMessages
					.get(position).getCreatedTime()));
			break;

		default:
			break;
		}

		return convertView;
	}

	private static class ViewHolderSystem {
		AvatarImageView avatarIv;
		TextView nameTv;
		TextView timeTv;
		TextView systemContentTv;
	}

	private static class ViewHolderComment {
		AvatarImageView avatarIv;
		TextView nameTv;
		TextView timeTv;
		TextView commentTv;
		ImageView imageView;

	}

	private static class ViewHolderReply {
		AvatarImageView avatarIv;
		TextView nameTv;
		TextView timeTv;
		ImageView imageView;
	}

	private static class ViewHolderFollow {
		AvatarImageView avatarIv;
		TextView nameTv;
		TextView timeTv;
	}

	private static class ViewHolderLike {
		AvatarImageView avatarIv;
		TextView nameTv;
		TextView timeTv;
	}
}
