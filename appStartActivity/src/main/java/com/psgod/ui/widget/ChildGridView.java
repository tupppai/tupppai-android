package com.psgod.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class ChildGridView extends GridView {

	public ChildGridView(Context context) {
		super(context);
	}

	public ChildGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ChildGridView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
}
