package com.psgod;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;

import java.lang.reflect.Method;

/**
 * 权限辅助类
 * 
 * @author rayalyuan
 * 
 */
public final class PermissionUtils {
	private static final String TAG = PermissionUtils.class.getSimpleName();

	private static final String NAME_APP_OPS_SERVICE = "appops";
	private static final String CLASS_NAME_APPOPSMANAGER = "AppOpsManager";
	private static final int OP_CEMARE_IN_ANDROID_SDK_API19 = 26;
	private static final int OP_CEMARE_BEFORE_MEIZU_API19 = 35;
	private static final int MODE_ALLOWED = 0;
	// private static final int OP_RECORD_AUDIO_IN_ANDROID_SDK_API19 = 27;
	// private static final int[] OP_IN_ANDROID_SDK_API19 =
	// {OP_CEMARE_IN_ANDROID_SDK_API19, OP_RECORD_AUDIO_IN_ANDROID_SDK_API19};
	// private static final int OP_RECORD_AUDIO_BEFORE_MEIZU_API19 = 36;
	// private static final int[] OP_IN_ANDROID_SDK_BEFORE_API19 =
	// {OP_CEMARE_BEFORE_MEIZU_API19, OP_RECORD_AUDIO_BEFORE_MEIZU_API19};

	public static final int ALLOW = 0x3300;
	public static final int FORBIDDEN = 0x3301;
	public static final int UNKNOWN = 0x3302;

	/**
	 * 判断应用是否有相机的权限（主要针对魅族和小米手机的坑爹权限设置） 只有SDK>=17才可以判断是否有相机权限
	 * SDK<17返回UNKNOWN，使用其他的方法判断
	 * 
	 * @return ALLOW: 具有相机权限 FORBIDDEN: 不具有相机权限 UNKNOWN: SDK<17，无法用此方法判断
	 */
	public static int isCameraForbidden(Context context) {
		if (!isMeizuPhone() || !isXiaomiPhone()) {
			return ALLOW;
		} else if (Build.VERSION.SDK_INT < 17) {
			return UNKNOWN;
		}

		Object object = context.getSystemService(NAME_APP_OPS_SERVICE);
		if (object != null
				&& object.getClass().getSimpleName()
						.equals(CLASS_NAME_APPOPSMANAGER)) {
			Method method;
			try {
				method = object.getClass().getMethod("checkOpNoThrow",
						int.class, int.class, String.class);
				int op;
				if (Build.VERSION.SDK_INT < 19) {
					op = OP_CEMARE_BEFORE_MEIZU_API19;
					// op = OP_IN_ANDROID_SDK_BEFORE_API19[type];
				} else {
					// int op = OP_IN_ANDROID_SDK_API19[type];
					op = OP_CEMARE_IN_ANDROID_SDK_API19;
				}

				ApplicationInfo info = context.getApplicationInfo();
				int result = (Integer) method.invoke(object, op, info.uid,
						info.packageName);
				return (result == MODE_ALLOWED) ? ALLOW : FORBIDDEN;
			} catch (Exception e) {
				Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_COLOR,
						TAG, e.getMessage());
			}
		}
		return ALLOW;
	}

	public static boolean isMeizuPhone() {
		String manufacture = Build.MANUFACTURER;
		return "Meizu".equalsIgnoreCase(manufacture);
	}

	public static boolean isXiaomiPhone() {
		String manufacture = Build.MANUFACTURER;
		return "Xiaomi".equalsIgnoreCase(manufacture);
	}
}
