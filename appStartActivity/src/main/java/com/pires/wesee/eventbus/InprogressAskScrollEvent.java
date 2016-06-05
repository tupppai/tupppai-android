package com.pires.wesee.eventbus;

public class InprogressAskScrollEvent {

	private int position;

	public InprogressAskScrollEvent(int position) {
		super();
		this.position = position;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

}
