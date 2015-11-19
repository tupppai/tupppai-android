package com.psgod.network.request;

import com.android.volley.Request.Method;

public interface IGetRequestBuilder {
	int METHOD = Method.GET;

	public String createUrl();
}
