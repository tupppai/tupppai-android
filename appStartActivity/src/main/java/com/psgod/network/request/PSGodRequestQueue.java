package com.psgod.network.request;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class PSGodRequestQueue {
	private static PSGodRequestQueue instance;
	private RequestQueue mRequestQueue;

	private PSGodRequestQueue(Context context) {
		mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
	}

	public static synchronized PSGodRequestQueue getInstance(Context context) {
		if (instance == null) {
			instance = new PSGodRequestQueue(context);
		}
		return instance;
	}

	public RequestQueue getRequestQueue() {
		return mRequestQueue;
	}
}
