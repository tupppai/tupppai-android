package com.psgod.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.psgod.Constants;
import com.psgod.R;
import com.psgod.UserPreferences;
import com.psgod.eventbus.UpdateTabStatusEvent;
import com.psgod.ui.activity.NotificationListActivity;
import com.psgod.ui.widget.BadgeView;

import de.greenrobot.event.EventBus;

public class MessageFragment extends BaseFragment implements OnClickListener {
	private static final String TAG = MessageFragment.class.getSimpleName();

	private ViewHolder mViewHolder;

	private static final int BADGE_COLOR = Color.parseColor("#FE8282");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 消息页面创建标识
		Constants.IS_MESSAGE_FRAGMENT_CREATED = true;

		FrameLayout parentView = new FrameLayout(getActivity());
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		parentView.setLayoutParams(params);

		mViewHolder = new ViewHolder();
		mViewHolder.mParentView = parentView;
		mViewHolder.mView = LayoutInflater.from(getActivity()).inflate(
				R.layout.fragment_message, parentView, true);
		mViewHolder.myCommentBtn = (TextView) mViewHolder.mView
				.findViewById(R.id.fragment_message_comment_btn);
		mViewHolder.myInviteBtn = (TextView) mViewHolder.mView
				.findViewById(R.id.fragment_message_invite_btn);
		mViewHolder.myFollowBtn = (TextView) mViewHolder.mView
				.findViewById(R.id.fragment_message_follow_btn);
		mViewHolder.myReplyBtn = (TextView) mViewHolder.mView
				.findViewById(R.id.fragment_message_reply_btn);
		mViewHolder.mySystemBtn = (TextView) mViewHolder.mView
				.findViewById(R.id.fragment_message_system_btn);

		mViewHolder.bCommentView = new BadgeView(getActivity(),
				mViewHolder.myCommentBtn);
		mViewHolder.bFollowView = new BadgeView(getActivity(),
				mViewHolder.myFollowBtn);
		mViewHolder.bInviteView = new BadgeView(getActivity(),
				mViewHolder.myInviteBtn);
		mViewHolder.bReplyView = new BadgeView(getActivity(),
				mViewHolder.myReplyBtn);
		mViewHolder.bSystemView = new BadgeView(getActivity(),
				mViewHolder.mySystemBtn);

		// 主动更新页面内未读消息的数量
		updatePageMessageCount();

		initListeners();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		FrameLayout parentView = new FrameLayout(getActivity());
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		parentView.setLayoutParams(params);
		mViewHolder.mParentView.removeView(mViewHolder.mView);
		parentView.addView(mViewHolder.mView);
		mViewHolder.mParentView = parentView;

		return parentView;
	}

	/**
	 * TODO 设置各个按钮的动作监听器
	 */
	private void initListeners() {
		mViewHolder.myCommentBtn.setOnClickListener(this);
		mViewHolder.myInviteBtn.setOnClickListener(this);
		mViewHolder.myFollowBtn.setOnClickListener(this);
		mViewHolder.myReplyBtn.setOnClickListener(this);
		mViewHolder.mySystemBtn.setOnClickListener(this);
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
			mViewHolder.bCommentView.setText(Integer.toString(count));
			mViewHolder.bCommentView.setBadgeBackgroundColor(BADGE_COLOR);
			mViewHolder.bCommentView.setTextColor(Color.WHITE);
			mViewHolder.bCommentView.setBadgePosition(5);
			mViewHolder.bCommentView.show();
			break;

		case Constants.PUSH_MESSAGE_FOLLOW:
			mViewHolder.bFollowView.setText(Integer.toString(count));
			mViewHolder.bFollowView.setBadgeBackgroundColor(BADGE_COLOR);
			mViewHolder.bFollowView.setTextColor(Color.WHITE);
			mViewHolder.bFollowView.setBadgePosition(5);
			mViewHolder.bFollowView.show();
			break;

		// case Constants.PUSH_MESSAGE_INVITE:
		// mViewHolder.bInviteView.setText(Integer.toString(count));
		// mViewHolder.bInviteView.setBadgeBackgroundColor(BADGE_COLOR);
		// mViewHolder.bInviteView.setTextColor(Color.WHITE);
		// mViewHolder.bInviteView.setBadgePosition(5);
		// mViewHolder.bInviteView.show();
		// break;

		case Constants.PUSH_MESSAGE_REPLY:
			mViewHolder.bReplyView.setText(Integer.toString(count));
			mViewHolder.bReplyView.setBadgeBackgroundColor(BADGE_COLOR);
			mViewHolder.bReplyView.setTextColor(Color.WHITE);
			mViewHolder.bReplyView.setBadgePosition(5);
			mViewHolder.bReplyView.show();
			break;

		case Constants.PUSH_MESSAGE_SYSTEM:
			mViewHolder.bSystemView.setText(Integer.toString(count));
			mViewHolder.bSystemView.setBadgeBackgroundColor(BADGE_COLOR);
			mViewHolder.bSystemView.setTextColor(Color.WHITE);
			mViewHolder.bSystemView.setBadgePosition(5);
			mViewHolder.bSystemView.show();
			break;
		default:
			break;
		}
	}

	// 点击Tab后清空未读消息数量
	private void resetMessageStatus(int type) {
		switch (type) {
		case Constants.PUSH_MESSAGE_COMMENT:
			if (mViewHolder.bCommentView.isShown()) {
				mViewHolder.bCommentView.hide();
			}
			UserPreferences.PushMessage.setPushMessageCount(
					UserPreferences.PushMessage.PUSH_COMMENT, 0);
			break;

		case Constants.PUSH_MESSAGE_FOLLOW:
			if (mViewHolder.bFollowView.isShown()) {
				mViewHolder.bFollowView.hide();
			}
			UserPreferences.PushMessage.setPushMessageCount(
					UserPreferences.PushMessage.PUSH_FOLLOW, 0);
			break;

		// case Constants.PUSH_MESSAGE_INVITE:
		// if (mViewHolder.bInviteView.isShown()) {
		// mViewHolder.bInviteView.hide();
		// }
		// UserPreferences.PushMessage.setPushMessageCount(
		// UserPreferences.PushMessage.PUSH_INVITE, 0);
		// break;

		case Constants.PUSH_MESSAGE_REPLY:
			if (mViewHolder.bReplyView.isShown()) {
				mViewHolder.bReplyView.hide();
			}
			UserPreferences.PushMessage.setPushMessageCount(
					UserPreferences.PushMessage.PUSH_REPLY, 0);
			break;

		case Constants.PUSH_MESSAGE_SYSTEM:
			if (mViewHolder.bSystemView.isShown()) {
				mViewHolder.bSystemView.hide();
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

	/**
	 * 保存视图组件，避免视图的重复加载
	 * 
	 * @author brandwang
	 * 
	 */
	private static class ViewHolder {
		ViewGroup mParentView;
		View mView;
		TextView myCommentBtn;
		TextView myFollowBtn;
		TextView myInviteBtn;
		TextView myReplyBtn;
		TextView mySystemBtn;

		BadgeView bCommentView;
		BadgeView bFollowView;
		BadgeView bReplyView;
		BadgeView bInviteView;
		BadgeView bSystemView;
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
			Context context = getActivity();
			Intent intent = new Intent(context, NotificationListActivity.class);
			intent.putExtra(Constants.IntentKey.NOTIFICATION_LIST_TYPE, type);
			context.startActivity(intent);
		}
	}
}
