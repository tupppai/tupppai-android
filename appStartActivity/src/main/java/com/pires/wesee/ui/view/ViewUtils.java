package com.pires.wesee.ui.view;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

public final class ViewUtils {
	private static Typeface TYPE_FACE;

	/**
	 * 设置TextView的字体
	 * 
	 * @param context
	 * @param textView
	 */
	public static void setTextTypeFace(Context context, TextView textView) {
		if (TYPE_FACE == null) {
			TYPE_FACE = Typeface.createFromAsset(context.getAssets(),
					"fonts/yahei.ttf");
		}
		textView.setTypeface(TYPE_FACE);
	}
}
