package com.pires.wesee.network.request;

import com.pires.wesee.Logger;
import com.pires.wesee.UserPreferences;
import com.pires.wesee.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 实现HTTP的get和post请求
 * 
 * @author Rayal
 * 
 */
public final class Request {
	private static final String TAG = Request.class.getSimpleName();
	// //public static final String PSGOD_BASE_TEST_URL =
	// "http://android.psgod.net/v1/"; // 测试环境
	// public static final String PSGOD_BASE_RELEASE_URL =
	// "http://android.qiupsdashen.com/v1/"; // 正式环境
	// public static final String PSGOD_BASE_TEST_URL =
	// "http://android.loiter.us/v1/";
	// public static final String PSGOD_BASE_URL = PSGOD_BASE_TEST_URL;
	private static final int REQUEST_TIMEOUT = 10 * 1000; // 连接超时时间
	private static final int SO_TIMEOUT = 15 * 1000; // 等待响应超时时间
	private static final BasicHttpParams httpParams = new BasicHttpParams();
	private static HttpClient client;
	static {
		HttpConnectionParams.setConnectionTimeout(httpParams, REQUEST_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, SO_TIMEOUT);
		client = new DefaultHttpClient(httpParams);
	}

	public static void init() {

	}

	/**
	 * HTTP get请求
	 * 
	 * @param url
	 * @return
	 * @throws JSONException
	 */
	public static JSONObject getRequest(String url) {
		Logger.logMethod(TAG, "getRequest", url);
		if (Utils.hasNullAruguments(url)) {
			return null;
		}

		HttpGet get = null;
		String result = null;
		try {
			get = new HttpGet(url);

			// 获取token设置cookie
			String tokenString = Request.getCookie();
			get.setHeader("cookie", "token=" + tokenString + ";");

			HttpResponse response = client.execute(get);

			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				result = EntityUtils.toString(response.getEntity());
			}
			// TODO 确定将token存入db的位置
		} catch (Exception e) {
			// Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_DEV, TAG,
			// e.getMessage(), true);
		} finally {
			if (get != null) {
				get.abort();
			}
		}
		JSONObject resultJsonObject = getJSONObject(result);

		// 存储cookie
		try {
			Request.saveCookie(resultJsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return resultJsonObject;
	}

	/**
	 * HTTP post请求
	 * 
	 * @param url
	 * @param params
	 * @return
	 * @throws JSONException
	 */
	public synchronized static JSONObject postRequest(final String url,
			final Map<String, Object> params) {
		Logger.logMethod(TAG, "postRequest", params);
		if (Utils.hasNullAruguments(url, params)) {
			return null;
		}

		HttpPost post = null;
		String result = null;
		try {
			post = new HttpPost(url);

			// 获取token设置cookie
			// TODO 可配置是否带header cookie
			String tokenString = Request.getCookie();
			post.setHeader("cookie", "token=" + tokenString + ";");

			UrlEncodedFormEntity entity = transformMapToUrlEncodedFormEntity(params);
			if (entity != null) {
				post.setEntity(entity);
			}

			HttpResponse response = client.execute(post);

			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				result = EntityUtils.toString(response.getEntity());
			}
		} catch (Exception e) {
			// Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_DEV, TAG,
			// e.getMessage(), true);
		} finally {
			if (post != null) {
				post.abort();
			}
		}
		JSONObject resultJsonObject = getJSONObject(result);

		// 存储cookie
		try {
			Request.saveCookie(resultJsonObject);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultJsonObject;
	}

	/**
	 * 把Map转换成UrlEncodedFormEntity
	 * 
	 * @param params
	 * @return
	 */
	private static UrlEncodedFormEntity transformMapToUrlEncodedFormEntity(
			final Map<String, Object> params) {
		List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
		if (params != null) {
			Set<String> keys = params.keySet();
			for (String key : keys) {
				pairs.add(new BasicNameValuePair(key, (String) params.get(key)));
			}
		} else {
			return null;
		}

		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairs,
					"utf-8");
			return entity;
		} catch (UnsupportedEncodingException e) {
			Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_DEV, TAG,
					e.getMessage(), true);
		}
		return null;
	}

	/**
	 * 把字符串转换成JSON对象
	 * 
	 * @param result
	 * @return
	 */
	private static JSONObject getJSONObject(final String result) {
		JSONObject obj = null;
		if (result != null) {
			try {
				obj = new JSONObject(result);
				return obj;
			} catch (JSONException e) {
				Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_DEV, TAG,
						e.getMessage(), true);
			}
		}
		return obj;
	}

	/**
	 * 保存返回的cookie信息
	 * 
	 * @params JSONObject
	 * @author brandwang
	 * @throws JSONException
	 */
	public static void saveCookie(JSONObject response) throws JSONException {
		if (response == null) {
			return;
		}

		int ret = response.getInt("ret");
		if (ret == 1) {
			String token = response.getString("token");
			if (!token.equals("")) {
				// 将token存入sp
				UserPreferences.TokenVerify.setToken(token);
			}
		}
		// TODO token过期失效处理
	}

	/**
	 * // * 获取当前token值
	 * 
	 * @author brandwang
	 */
	public static String getCookie() {
		String token = UserPreferences.TokenVerify.getToken();
		return token;
	}

	/**
	 * 外部请求 不走cookie那一套 不带cookie
	 */
	public static JSONObject originGetRequest(String url) {
		Logger.logMethod(TAG, "getRequest", url);
		if (Utils.hasNullAruguments(url)) {
			return null;
		}

		HttpGet get = null;
		String result = null;
		try {
			get = new HttpGet(url);
			HttpResponse response = client.execute(get);

			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				result = EntityUtils.toString(response.getEntity());
			}
			// TODO 确定将token存入db的位置
		} catch (Exception e) {
			Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_DEV, TAG,
					e.getMessage(), true);
		} finally {
			if (get != null) {
				get.abort();
			}
		}
		JSONObject resultJsonObject = getJSONObject(result);
		return resultJsonObject;
	}
}
