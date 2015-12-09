package com.psgod.network.request;

import com.android.volley.Request.Method;

import java.util.Map;

public interface IPostRequestBuilder {
	int METHOD = Method.POST;

	public String createUrl();

	public Map<String, String> createParameters();
}
