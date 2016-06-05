package com.pires.wesee.receiver;

/**
 * broadcast
 * 检测网络状态变化
 * @author brandwang
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.pires.wesee.eventbus.NetEvent;
import com.pires.wesee.network.NetworkUtil;

import de.greenrobot.event.EventBus;

public class NetReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
			boolean isConnected = NetworkUtil.isNetworkConnected(context);

			if (isConnected) {
				EventBus.getDefault().post(new NetEvent(true));
			} else {
				EventBus.getDefault().post(new NetEvent(false));
			}
		}
	}

}
