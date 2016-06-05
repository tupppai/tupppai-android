package com.pires.wesee.network;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.pires.wesee.PSGodApplication;

public class NetworkUtil {
	private static final int VIVO_3G = 17;

	public static interface NetworkType {
		int UNKNOWN = -1;
		int NONE = 0;
		int WIFI = 1;
		int G2 = 2;
		int G3 = 3;
		int G4 = 4;
		int CABLE = 5;
	}

	/**
	 * 获取当前的网络状态
	 * 
	 * @return
	 */
	public static int getNetworkType() {
		ConnectivityManager cm = (ConnectivityManager) PSGodApplication
				.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
			return NetworkType.NONE;
		}

		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo == null || !networkInfo.isConnected()) {
			return NetworkType.NONE;
		}

		switch (networkInfo.getType()) {
		case ConnectivityManager.TYPE_WIFI:
			return NetworkType.WIFI;
		case ConnectivityManager.TYPE_ETHERNET:
			return NetworkType.CABLE;
		case ConnectivityManager.TYPE_MOBILE: {
			int subType = networkInfo.getSubtype();
			switch (subType) {
			// 参考android.telephony.TelephonyManager#getNetworkClass
			case TelephonyManager.NETWORK_TYPE_GPRS:
			case TelephonyManager.NETWORK_TYPE_EDGE:
			case TelephonyManager.NETWORK_TYPE_CDMA:
			case TelephonyManager.NETWORK_TYPE_1xRTT:
			case TelephonyManager.NETWORK_TYPE_IDEN:
				return NetworkType.G2;
			case TelephonyManager.NETWORK_TYPE_UMTS:
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
			case TelephonyManager.NETWORK_TYPE_HSDPA:
			case TelephonyManager.NETWORK_TYPE_HSUPA:
			case TelephonyManager.NETWORK_TYPE_HSPA:
			case TelephonyManager.NETWORK_TYPE_EVDO_B:
			case TelephonyManager.NETWORK_TYPE_EHRPD:
			case TelephonyManager.NETWORK_TYPE_HSPAP:
			case VIVO_3G:
				return NetworkType.G3;
			case TelephonyManager.NETWORK_TYPE_LTE:
				return NetworkType.G4;
			default:
				return NetworkType.UNKNOWN;
			}
		}
		default:
			return NetworkType.UNKNOWN;
		}
	}

	// 判断网络连接状态
	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	// 判断wifi状态
	public static boolean isWifiConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (mWiFiNetworkInfo != null) {
				return mWiFiNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	// 判断移动网络
	public static boolean isMobileConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mMobileNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (mMobileNetworkInfo != null) {
				return mMobileNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	// 获取连接类型
	public static int getConnectedType(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
				return mNetworkInfo.getType();
			}
		}
		return -1;
	}

	/**
	 * 唤起网络设置面板
	 * 
	 * @param paramContext
	 */
	public static void startToSettings(Context paramContext) {
		if (paramContext == null)
			return;
		try {
			if (Build.VERSION.SDK_INT > 10) {
				paramContext.startActivity(new Intent(
						"android.settings.SETTINGS"));
				return;
			}
		} catch (Exception localException) {
			localException.printStackTrace();
			return;
		}
		paramContext.startActivity(new Intent(
				"android.settings.WIRELESS_SETTINGS"));
	}
}
