package com.psgod.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class ExtendsGridView extends GridView {
	public ExtendsGridView(Context context) {
		super(context);
	}

	public ExtendsGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	// 重写onMeasure使其不会出现滚动条
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
}
