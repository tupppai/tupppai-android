package com.psgod.network.request;

import java.util.Map;

import com.android.volley.Request.Method;

public interface IPostRequestBuilder {
	int METHOD = Method.POST;

	public String createUrl();

	public Map<String, String> createParameters();
}
