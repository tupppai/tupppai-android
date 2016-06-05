package com.pires.wesee.receiver;

/**
 * 接受来自UMENG消息推送的广播
 * @author brandwang
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pires.wesee.eventbus.PushEvent;
import com.pires.wesee.eventbus.RefreshEvent;
import com.pires.wesee.ui.activity.MessageLikeActivity;
import com.pires.wesee.ui.activity.MessageSystemActivity;
import com.pires.wesee.ui.activity.NewMessageActivity;

import de.greenrobot.event.EventBus;

public class PushMessageReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// 设置推送消息的类型和数量
		if (intent.hasExtra("type")) {
			int mType = intent.getIntExtra("type", 0);
			int mCount = intent.getIntExtra("count", 0);

			EventBus.getDefault().post(
					new PushEvent(mType, mCount, PushEvent.TYPE_ACT_MAIN));
			switch (mType){
				case 0:
					EventBus.getDefault().post(new RefreshEvent(MessageSystemActivity.class.getName()));
					break;
				case 5:
					EventBus.getDefault().post(new RefreshEvent(MessageLikeActivity.class.getName()));
					break;
			}
			EventBus.getDefault().post(new RefreshEvent(NewMessageActivity.class.getName()));
		}
	}
}
