package com.pires.wesee.eventbus;

/**
 * Created by Administrator on 2016/3/11 0011.
 */
public class BindEvent {

    public enum State{
        FINISH,OK,ERROR
    }

    public State state;

    public BindEvent(State state) {
        this.state = state;
    }
}
