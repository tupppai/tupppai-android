package com.psgod.eventbus;

/**
 * 推送事件
 * 
 * @author brandwang
 */

public class PushEvent {
	// 推送类型 数量
	public int pushType = -1;
	public int pushCount;
	public int pushObjectType;

	public static final int TYPE_ACT_MAIN = 0;
	public static final int TYPE_FRAG_PAGE = 1;

	public PushEvent(int type, int count, int ObjectType) {
		this.pushCount = count;
		this.pushType = type;
		this.pushObjectType = ObjectType;
	}

	// 获取推送类型
	public int getPushType() {
		return pushType;
	}

	// 获取推送数量
	public int getPushCount() {
		return pushCount;
	}
}
