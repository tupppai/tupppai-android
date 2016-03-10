package com.psgod.eventbus;

/**
 * Created by remilia on 2015/11/19
 * 刷新用对象
 */
public class RefreshEvent {

    public String className;

    public RefreshEvent(String className) {
        this.className = className;
    }
}
