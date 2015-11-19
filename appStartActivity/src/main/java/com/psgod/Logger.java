package com.psgod;

import android.util.Log;

/**
 * 日志输出类 主要功能： 1. 根据用户的染色情况，输出日志 2. 输出日志到本地文件
 * 
 * @author rayalyuan
 * 
 */
public class Logger {
	public static final int LOG_LEVEL_VERBOSE = 0x330;
	public static final int LOG_LEVEL_DEBUG = 0x331;
	public static final int LOG_LEVEL_INFO = 0x332;
	public static final int LOG_LEVEL_WARNING = 0x333;
	public static final int LOG_LEVEL_ERROR = 0x334;

	public static final int USER_LEVEL_COLOR = 0;
	public static final int USER_LEVEL_DEV = 1;

	/**
	 * 输出日志到console（不会输出到本地文件）
	 * 
	 * @param logLevel
	 *            日志级别 LOG_LEVEL_VERBOSE: 对应Log.v LOG_LEVEL_DEBUG: 对应Log.d
	 *            LOG_LEVEL_INFO: 对应Log.i LOG_LEVEL_WARNING: 对应Log.w
	 *            LOG_LEVEL_ERROR: 对应Log.e
	 * @param userLevel
	 *            用户级别 USER_LEVEL_COLOR: 染色用户才会输出日志 USER_LEVEL_DEV: 所有用户都会输出日志
	 * @param tag
	 *            日志TAG
	 * @param msg
	 *            日志信息
	 */
	public static void log(int logLevel, int userLevel, String tag, String msg) {
		log(logLevel, userLevel, tag, msg, false);
	}

	/**
	 * 输出日志到console（不会输出到本地文件）
	 * 
	 * @param logLevel
	 *            日志级别 LOG_LEVEL_VERBOSE: 对应Log.v LOG_LEVEL_DEBUG: 对应Log.d
	 *            LOG_LEVEL_INFO: 对应Log.i LOG_LEVEL_WARNING: 对应Log.w
	 *            LOG_LEVEL_ERROR: 对应Log.e
	 * @param userLevel
	 *            用户级别 USER_LEVEL_COLOR: 染色用户才会输出日志 USER_LEVEL_DEV: 所有用户都会输出日志
	 * @param tag
	 *            日志TAG
	 * @param msg
	 *            日志信息
	 * @param logToFile
	 *            是否输出日志到本地日志文件
	 */
	public static void log(int logLevel, int userLevel, String tag, String msg,
			boolean logToFile) {
		if ((userLevel == USER_LEVEL_DEV) || (Constants.IS_COLOR_USER)) {
			switch (logLevel) {
			case LOG_LEVEL_VERBOSE:
				Log.v(tag, msg);
				break;
			case LOG_LEVEL_INFO:
				Log.i(tag, msg);
				break;
			case LOG_LEVEL_WARNING:
				Log.w(tag, msg);
			case LOG_LEVEL_ERROR:
				Log.e(tag, msg);
			case LOG_LEVEL_DEBUG:
			default:
				Log.d(tag, msg);
				break;
			}

			if (Constants.LOG_TO_FILE && logToFile) {
				// TODO 把日志输出到本地日志文件
			}
		}
	}

	/**
	 * 以LOG_LEVEL_DEBUG的日志级别，和USER_LEVEL_COLOR的用户级别输出日志到console（不会输出到本地文件）
	 * 主要用于输出方法调用时的日志信息，输出格式method(): params[0] params[1] ...
	 * 
	 * @param tag
	 *            日志TAG
	 * @param method
	 *            方法名称
	 * @param params
	 *            变量的值
	 */
	public static void logMethod(String tag, String method, Object... params) {
		logMethod(LOG_LEVEL_DEBUG, USER_LEVEL_COLOR, tag, method, params);
	}

	/**
	 * 输出日志到console（不会输出到本地文件） 主要用于输出方法调用时的日志信息，输出格式method(): params[0]
	 * params[1] ...
	 * 
	 * @param logLevel
	 *            日志级别 LOG_LEVEL_VERBOSE: 对应Log.v LOG_LEVEL_DEBUG: 对应Log.d
	 *            LOG_LEVEL_INFO: 对应Log.i LOG_LEVEL_WARNING: 对应Log.w
	 *            LOG_LEVEL_ERROR: 对应Log.e
	 * @param userLevel
	 *            用户级别 USER_LEVEL_COLOR: 染色用户才会输出日志 USER_LEVEL_DEV: 所有用户都会输出日志
	 * @param tag
	 *            日志TAG
	 * @param method
	 *            方法名称
	 * @param params
	 *            变量的值
	 */
	public static void logMethod(int logLevel, int userLevel, String tag,
			String method, Object... params) {
		StringBuilder sb = new StringBuilder(method);
		sb.append("(): ");
		for (Object obj : params) {
			if (obj == null) {
				sb.append("NULL ");
			} else {
				sb.append(params.toString()).append(" ");
			}
		}
		log(logLevel, userLevel, tag, sb.toString(), false);
	}
}
