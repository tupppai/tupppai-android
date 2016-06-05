package com.pires.wesee.ui.activity;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.pires.wesee.UserSetting;
import com.pires.wesee.PSGodApplication;
import com.pires.wesee.R;
import com.pires.wesee.network.request.ActionSetNotificationPush;
import com.pires.wesee.network.request.PSGodRequestQueue;

/**
 * 通知提醒设置界面
 * 
 * @author Rayal
 */
public class SettingNotificationActivity extends PSGodBaseActivity implements
		OnCheckedChangeListener {
	private static final String TAG = SettingNotificationActivity.class
			.getSimpleName();

	private UserSetting mUserSetting;
	private ToggleButton mCommentSwitch;
	private ToggleButton mReplySwitch;
	private ToggleButton mFollowSwitch;
	// 点赞通知
	private ToggleButton mLikeSwitch;
	private ToggleButton mSystemSwitch;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_notification);
		mCommentSwitch = (ToggleButton) this
				.findViewById(R.id.activity_setting_notification_switch_comment);
		mReplySwitch = (ToggleButton) this
				.findViewById(R.id.activity_setting_notification_switch_reply);
		mFollowSwitch = (ToggleButton) this
				.findViewById(R.id.activity_setting_notification_switch_follow);
		mLikeSwitch = (ToggleButton) this
				.findViewById(R.id.activity_setting_notification_switch_like);
		mSystemSwitch = (ToggleButton) this
				.findViewById(R.id.activity_setting_notification_switch_system);

		// 读取用户设置 初始化推送设置的状态
		mUserSetting = UserSetting.getInstance();
		mCommentSwitch.setChecked(mUserSetting.isReceivedCommentNotification());
		mReplySwitch.setChecked(mUserSetting.isReceivedReplyNotification());
		mFollowSwitch.setChecked(mUserSetting.isReceivedFollowNotification());
		mLikeSwitch.setChecked(mUserSetting.isReceivedLikeNotification());
		mSystemSwitch.setChecked(mUserSetting.isReceivedSystemNotification());

		mCommentSwitch.setOnCheckedChangeListener(this);
		mReplySwitch.setOnCheckedChangeListener(this);
		mFollowSwitch.setOnCheckedChangeListener(this);
		mLikeSwitch.setOnCheckedChangeListener(this);
		mSystemSwitch.setOnCheckedChangeListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton button, boolean isChecked) {
		switch (button.getId()) {
		case R.id.activity_setting_notification_switch_comment:
			setPushNotification("comment", isChecked);
			break;
		case R.id.activity_setting_notification_switch_reply:
			setPushNotification("reply", isChecked);
			break;
		case R.id.activity_setting_notification_switch_follow:
			setPushNotification("follow", isChecked);
			break;
		case R.id.activity_setting_notification_switch_like:
			setPushNotification("like", isChecked);
			break;
		case R.id.activity_setting_notification_switch_system:
			setPushNotification("system", isChecked);
			break;
		default:
			break;
		}
	}

	// 设置用户推送消息
	public void setPushNotification(String type, boolean ischecked) {
		ActionSetNotificationPush.Builder builder = new ActionSetNotificationPush.Builder()
				.setValue(ischecked ? 1 : 0).setType(type)
				.setErrorListener(null);

		if (type == "comment") {
			builder.setListener(setCommentPushListener);
		}
		if (type == "follow") {
			builder.setListener(setFollowPushListener);
		}
		if (type == "like") {
			builder.setListener(setLikePushListener);
		}
		if (type == "reply") {
			builder.setListener(setReplyPushListener);
		}
		if (type == "system") {
			builder.setListener(setSystemListener);
		}

		ActionSetNotificationPush request = builder.build();
		request.setTag(TAG);
		RequestQueue requestQueue = PSGodRequestQueue.getInstance(
				SettingNotificationActivity.this).getRequestQueue();
		requestQueue.add(request);
	}

	// 设置评论 推送消息回调
	private Listener<Boolean> setCommentPushListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			if (response) {
				mUserSetting.setReceivedCommentNotification(mUserSetting
						.isReceivedCommentNotification() ? false : true);
			} else {
				mCommentSwitch.setChecked(mUserSetting
						.isReceivedCommentNotification());
				Toast.makeText(PSGodApplication.getAppContext(), "设置失败，请稍后再试",
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	// 设置关注 推送消息回调
	private Listener<Boolean> setFollowPushListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			if (response) {
				mUserSetting.setReceivedFollowNotification(mUserSetting
						.isReceivedFollowNotification() ? false : true);
			} else {
				mFollowSwitch.setChecked(mUserSetting
						.isReceivedFollowNotification());
				Toast.makeText(PSGodApplication.getAppContext(), "设置失败，请稍后再试",
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	// 设置邀请 推送消息回调
	private Listener<Boolean> setLikePushListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			if (response) {
				mUserSetting.setReceivedLikeNotification(mUserSetting
						.isReceivedLikeNotification() ? false : true);
			} else {
				mLikeSwitch.setChecked(mUserSetting
						.isReceivedLikeNotification());
				Toast.makeText(PSGodApplication.getAppContext(), "设置失败，请稍后再试",
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	// 设置回复 推送消息回调
	private Listener<Boolean> setReplyPushListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			if (response) {
				mUserSetting.setReceivedReplyNotification(mUserSetting
						.isReceivedReplyNotification() ? false : true);
			} else {
				mReplySwitch.setChecked(mUserSetting
						.isReceivedReplyNotification());
				Toast.makeText(PSGodApplication.getAppContext(), "设置失败，请稍后再试",
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	// 设置系统 推送消息回调
	private Listener<Boolean> setSystemListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			if (response) {
				mUserSetting.setReceivedSystemNotification(mUserSetting
						.isReceivedSystemNotification() ? false : true);
			} else {
				mSystemSwitch.setChecked(mUserSetting
						.isReceivedSystemNotification());
				Toast.makeText(PSGodApplication.getAppContext(), "设置失败，请稍后再试",
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	/**
	 * 暂停所有的下载
	 */
	@Override
	public void onStop() {
		super.onStop();
		RequestQueue requestQueue = PSGodRequestQueue.getInstance(this)
				.getRequestQueue();
		requestQueue.cancelAll(TAG);
	}
}
