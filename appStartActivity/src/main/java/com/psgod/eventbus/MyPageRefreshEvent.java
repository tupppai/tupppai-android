package com.psgod.eventbus;

public class MyPageRefreshEvent {
	private int type;
	public static final int COLLECTION = 2;
	public static final int ASK = 0;
	public static final int WORK = 1;
	public static final int REPLY = 3; 

	public int getType() {
		return type;
	}

	public MyPageRefreshEvent(int type) {
		super();
		this.type = type;
	}

}
