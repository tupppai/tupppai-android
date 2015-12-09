package com.psgod;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * 使用匿名内部类Handler有可能会导致内存泄漏， 凡是使用匿名内部类Handler的地方， 都尽量使用WeakReferenceHandler
 * 
 * @author Rayal
 * 
 */
public class WeakReferenceHandler extends Handler {
	private WeakReference<Callback> mWeakReferCallBack;

	public WeakReferenceHandler(Callback cb) {
		super();
		mWeakReferCallBack = new WeakReference<Handler.Callback>(cb);
	}

	public WeakReferenceHandler(Looper looper, Callback cb) {
		super(looper);
		mWeakReferCallBack = new WeakReference<Handler.Callback>(cb);
	}

	@Override
	public void handleMessage(Message msg) {
		Callback cb = mWeakReferCallBack.get();
		if (null != cb) {
			cb.handleMessage(msg);
		}
	}

	@Override
	public String toString() {
		Callback cb = mWeakReferCallBack.get();
		return super.toString() + " " + cb;
	}
}