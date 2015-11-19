package com.psgod.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.psgod.Constants;
import com.psgod.R;
import com.psgod.UserPreferences;
import com.psgod.eventbus.UpdateTabStatusEvent;
import com.psgod.ui.widget.BadgeView;

import de.greenrobot.event.EventBus;

public class MessageActivity extends PSGodBaseActivity implements
		OnClickListener {
	private static final String TAG = MessageActivity.class.getSimpleName();

	private static final int BADGE_COLOR = Color.parseColor("#FE8282");

	private Context mContext;

	private TextView myCommentBtn;
	private TextView myFollowBtn;
	private TextView myInviteBtn;
	private TextView myReplyBtn;
	private TextView mySystemBtn;

	private BadgeView bCommentView;
	private BadgeView bFollowView;
	private BadgeView bReplyView;
	private BadgeView bInviteView;
	private BadgeView bSystemView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 消息页面创建标识
		Constants.IS_MESSAGE_FRAGMENT_CREATED = true;
		setContentView(R.layout.activity_message);
		mContext = this;

		myCommentBtn = (TextView) findViewById(R.id.fragment_message_comment_btn);
		myInviteBtn = (TextView) findViewById(R.id.fragment_message_invite_btn);
		myFollowBtn = (TextView) findViewById(R.id.fragment_message_follow_btn);
		myReplyBtn = (TextView) findViewById(R.id.fragment_message_reply_btn);
		mySystemBtn = (TextView) findViewById(R.id.fragment_message_system_btn);

		bCommentView = new BadgeView(mContext, myCommentBtn);
		bFollowView = new BadgeView(mContext, myFollowBtn);
		bInviteView = new BadgeView(mContext, myInviteBtn);
		bReplyView = new BadgeView(mContext, myReplyBtn);
		bSystemView = new BadgeView(mContext, mySystemBtn);

		// 主动更新页面内未读消息的数量
		updatePageMessageCount();

		initListeners();
	}

	/**
	 * TODO 设置各个按钮的动作监听器
	 */
	private void initListeners() {
		myCommentBtn.setOnClickListener(this);
		myInviteBtn.setOnClickListener(this);
		myFollowBtn.setOnClickListener(this);
		myReplyBtn.setOnClickListener(this);
		mySystemBtn.setOnClickListener(this);
	}

	private void updatePageMessageCount() {
		int commentCount = UserPreferences.PushMessage
				.getPushMessageCount(UserPreferences.PushMessage.PUSH_COMMENT);
		if (commentCount > 0) {
			setPushMessage(Constants.PUSH_MESSAGE_COMMENT, commentCount);
		}

		int replyCount = UserPreferences.PushMessage
				.getPushMessageCount(UserPreferences.PushMessage.PUSH_REPLY);
		if (replyCount > 0) {
			setPushMessage(Constants.PUSH_MESSAGE_REPLY, replyCount);
		}

		int followCount = UserPreferences.PushMessage
				.getPushMessageCount(UserPreferences.PushMessage.PUSH_FOLLOW);
		if (followCount > 0) {
			setPushMessage(Constants.PUSH_MESSAGE_FOLLOW, followCount);
		}

		// int inviteCount = UserPreferences.PushMessage.getPushMessageCount(
		// UserPreferences.PushMessage.PUSH_INVITE);
		// if (inviteCount > 0) {
		// setPushMessage(Constants.PUSH_MESSAGE_INVITE, inviteCount);
		// }

		int systemCount = UserPreferences.PushMessage
				.getPushMessageCount(UserPreferences.PushMessage.PUSH_SYSTEM);

		if (systemCount > 0) {
			setPushMessage(Constants.PUSH_MESSAGE_SYSTEM, systemCount);
		}
	}

	// 设置推送消息的状态
	public void setPushMessage(int type, int count) {
		switch (type) {
		case Constants.PUSH_MESSAGE_COMMENT:
			bCommentView.setText(Integer.toString(count));
			bCommentView.setBadgeBackgroundColor(BADGE_COLOR);
			bCommentView.setTextColor(Color.WHITE);
			bCommentView.setBadgePosition(5);
			bCommentView.show();
			break;

		case Constants.PUSH_MESSAGE_FOLLOW:
			bFollowView.setText(Integer.toString(count));
			bFollowView.setBadgeBackgroundColor(BADGE_COLOR);
			bFollowView.setTextColor(Color.WHITE);
			bFollowView.setBadgePosition(5);
			bFollowView.show();
			break;

		// case Constants.PUSH_MESSAGE_INVITE:
		// bInviteView.setText(Integer.toString(count));
		// bInviteView.setBadgeBackgroundColor(BADGE_COLOR);
		// bInviteView.setTextColor(Color.WHITE);
		// bInviteView.setBadgePosition(5);
		// bInviteView.show();
		// break;

		case Constants.PUSH_MESSAGE_REPLY:
			bReplyView.setText(Integer.toString(count));
			bReplyView.setBadgeBackgroundColor(BADGE_COLOR);
			bReplyView.setTextColor(Color.WHITE);
			bReplyView.setBadgePosition(5);
			bReplyView.show();
			break;

		case Constants.PUSH_MESSAGE_SYSTEM:
			bSystemView.setText(Integer.toString(count));
			bSystemView.setBadgeBackgroundColor(BADGE_COLOR);
			bSystemView.setTextColor(Color.WHITE);
			bSystemView.setBadgePosition(5);
			bSystemView.show();
			break;
		default:
			break;
		}
	}

	// 点击Tab后清空未读消息数量
	private void resetMessageStatus(int type) {
		switch (type) {
		case Constants.PUSH_MESSAGE_COMMENT:
			if (bCommentView.isShown()) {
				bCommentView.hide();
			}
			UserPreferences.PushMessage.setPushMessageCount(
					UserPreferences.PushMessage.PUSH_COMMENT, 0);
			break;

		case Constants.PUSH_MESSAGE_FOLLOW:
			if (bFollowView.isShown()) {
				bFollowView.hide();
			}
			UserPreferences.PushMessage.setPushMessageCount(
					UserPreferences.PushMessage.PUSH_FOLLOW, 0);
			break;

		// case Constants.PUSH_MESSAGE_INVITE:
		// if (bInviteView.isShown()) {
		// bInviteView.hide();
		// }
		// UserPreferences.PushMessage.setPushMessageCount(
		// UserPreferences.PushMessage.PUSH_INVITE, 0);
		// break;

		case Constants.PUSH_MESSAGE_REPLY:
			if (bReplyView.isShown()) {
				bReplyView.hide();
			}
			UserPreferences.PushMessage.setPushMessageCount(
					UserPreferences.PushMessage.PUSH_REPLY, 0);
			break;

		case Constants.PUSH_MESSAGE_SYSTEM:
			if (bSystemView.isShown()) {
				bSystemView.hide();
			}
			UserPreferences.PushMessage.setPushMessageCount(
					UserPreferences.PushMessage.PUSH_SYSTEM, 0);
			break;

		default:
			break;
		}
		// EventBus通知状态栏 更新底部tab的状态
		EventBus.getDefault().post(new UpdateTabStatusEvent());
	}

	@Override
	public void onClick(View v) {
		int type = -1;
		switch (v.getId()) {
		case R.id.fragment_message_comment_btn:
			type = NotificationListActivity.TYPE_COMMENT_NOTIFICATION;
			resetMessageStatus(Constants.PUSH_MESSAGE_COMMENT);
			break;
		// case R.id.fragment_message_invite_btn:
		// type = NotificationListActivity.TYPE_INVITE_NOTIFICATION;
		// resetMessageStatus(Constants.PUSH_MESSAGE_INVITE);
		// break;
		case R.id.fragment_message_follow_btn:
			type = NotificationListActivity.TYPE_FOLLOW_NOTIFICATION;
			resetMessageStatus(Constants.PUSH_MESSAGE_FOLLOW);
			break;
		case R.id.fragment_message_reply_btn:
			type = NotificationListActivity.TYPE_REPLY_NOTIFICATION;
			resetMessageStatus(Constants.PUSH_MESSAGE_REPLY);
			break;
		case R.id.fragment_message_system_btn:
			type = NotificationListActivity.TYPE_SYSTEM_NOTIFICATION;
			resetMessageStatus(Constants.PUSH_MESSAGE_SYSTEM);
			break;
		}

		if (type != -1) {
			Context context = mContext;
			Intent intent = new Intent(context, NotificationListActivity.class);
			intent.putExtra(Constants.IntentKey.NOTIFICATION_LIST_TYPE, type);
			context.startActivity(intent);
		}
	}

}
