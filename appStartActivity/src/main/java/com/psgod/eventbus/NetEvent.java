package com.psgod.eventbus;

/**
 * 网络的事件
 * 
 * @author Administrator
 */

public class NetEvent {
	public boolean isNet;

	public NetEvent(boolean netStatus) {
		this.isNet = netStatus;
	}

	public boolean getIsNet() {
		return isNet;
	}
}
