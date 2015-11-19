package com.psgod.ui.widget;

import android.app.ListActivity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;
import android.widget.ListView;

public class ChildListView extends ListView {

	public ChildListView(Context context) {
		super(context);
	}

	public ChildListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ChildListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
}
