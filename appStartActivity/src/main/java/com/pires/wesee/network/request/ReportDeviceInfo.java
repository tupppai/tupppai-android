package com.pires.wesee.network.request;

/**
 * 上报客户端信息 包括umeng device_token
 * @author brandwang
 */

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.pires.wesee.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ReportDeviceInfo extends BaseRequest<Boolean> {
	private static final String TAG = ReportDeviceInfo.class.getSimpleName();

	public ReportDeviceInfo(int method, String url, Listener<Boolean> listener,
			ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	@Override
	protected Boolean doParseNetworkResponse(JSONObject response)
			throws JSONException {
		return true;
	}

	public static class Builder implements IPostRequestBuilder {
		// Device Token Umeng
		private String mToken;
		// MAC地址
		private String mMac;
		// 设备型号
		private String mName;
		// 设备系统
		private String mOS;
		// app版本号
		private String mVersion;

		Listener<Boolean> listener;
		private ErrorListener errorListener;

		public Builder setOs(String os) {
			this.mOS = os;
			return this;
		}

		public Builder setName(String name) {
			this.mName = name;
			return this;
		}

		public Builder setMac(String mac) {
			this.mMac = mac;
			return this;
		}

		public Builder setToken(String token) {
			this.mToken = token;
			return this;
		}

		public Builder setVersion(String version) {
			this.mVersion = version;
			return this;
		}

		public Builder setListener(Listener<Boolean> listener) {
			this.listener = listener;
			return this;
		}

		public Builder setErrorListener(ErrorListener errorListener) {
			this.errorListener = errorListener;
			return this;
		}

		public ReportDeviceInfo build() {
			String url = createUrl();
			ReportDeviceInfo request = new ReportDeviceInfo(METHOD, url,
					listener, errorListener) {
				@Override
				public Map<String, String> getParams() {
					return getPackParams(createParameters());
				}
			};
			return request;
		}

		@Override
		public String createUrl() {
			StringBuilder sb = new StringBuilder(PSGOD_BASE_URL)
					.append("account/updateToken");
			String url = sb.toString();
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"createUrl: " + url);
			return url;
		}

		@Override
		public Map<String, String> createParameters() {
			Map<String, String> params = new HashMap<String, String>();
			params.put("device_token", mToken);
			params.put("device_name", mName);
			params.put("device_mac", mMac);
			params.put("device_os", mOS);
			params.put("version", mVersion);
			// 平台种类
			params.put("platform", Integer.toString(0));

			return params;
		}
	}
}
