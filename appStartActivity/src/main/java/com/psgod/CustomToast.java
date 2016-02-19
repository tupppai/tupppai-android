package com.psgod;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

public class CustomToast {

	public static void show(Context context, String text, int time) {
		Toast.makeText(context, text, time).show();
	}

	public static void showError(Context context, String text, int time) {
		Toast.makeText(context, text, time).show();
	}
}
