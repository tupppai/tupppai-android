package com.pires.wesee;

import android.os.Build;

/**
 * 安卓版本辅助类
 * 
 * @author rayalyuan
 * 
 */
public final class VersionUtils {
	private VersionUtils() {

	}

	/**
	 * 版本是否在2.1之后（API 7）
	 */
	public static boolean isECLAIR_MR1() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1;
	}

	/**
	 * 版本是否在2.2之后(API 8)
	 */
	public static boolean isrFroyo() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
	}

	/**
	 * 版本是否在2.3之后（API 9）
	 */
	public static boolean isGingerBread() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
	}

	/**
	 * 版本是否在4.0之后（API 14)
	 */
	public static boolean isIceScreamSandwich() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	}

	/**
	 * 版本是否在3.0之后（API 11)
	 */
	public static boolean isHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	/**
	 * 版本是否在3.2之后（API 13)
	 */
	public static boolean isHoneycombMR2() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2;
	}

	// /**
	// * 版本是否再4.1之后(API 16)
	// */
	// public static boolean isJellyBean() {
	// return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
	// }
}
