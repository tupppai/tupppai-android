package com.pires.wesee.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.View;

import com.etsy.android.grid.StaggeredGridView;
import com.handmark.pulltorefresh.library.OverscrollHelper;
import com.handmark.pulltorefresh.library.PullToRefreshAdapterViewBase;
import com.handmark.pulltorefresh.library.internal.EmptyViewMethodAccessor;

public class PullToRefreshStaggeredGridView extends
		PullToRefreshAdapterViewBase<StaggeredGridView> {

	public PullToRefreshStaggeredGridView(Context context) {
		super(context);
	}

	public PullToRefreshStaggeredGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PullToRefreshStaggeredGridView(Context context, Mode mode) {
		super(context, mode);
	}

	public PullToRefreshStaggeredGridView(Context context, Mode mode,
			AnimationStyle style) {
		super(context, mode, style);
	}

	@Override
	public Orientation getPullToRefreshScrollDirection() {
		return Orientation.VERTICAL;
	}

	@Override
	protected StaggeredGridView createRefreshableView(Context context,
			AttributeSet attrs) {
		final StaggeredGridView gridView;

		if (VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
			gridView = new InternalStaggeredGridView(context, attrs);
		} else {
			gridView = new InternalStaggeredGridView(context, attrs);
		}

		// Use Generated ID (from res/values/ids.xml)
		// sv.setId(R.id.swipeview);
		return gridView;
	}

	class InternalStaggeredGridView extends StaggeredGridView implements
			EmptyViewMethodAccessor {
		public InternalStaggeredGridView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		public void setEmptyView(View emptyView) {
			PullToRefreshStaggeredGridView.this.setEmptyView(emptyView);
		}

		@Override
		public void setEmptyViewInternal(View emptyView) {
			super.setEmptyView(emptyView);
		}
	}

	@TargetApi(9)
	final class InternalNotificationListViewSDK9 extends
			InternalStaggeredGridView {
		public InternalNotificationListViewSDK9(Context context,
				AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
				int scrollY, int scrollRangeX, int scrollRangeY,
				int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {

			final boolean returnValue = super.overScrollBy(deltaX, deltaY,
					scrollX, scrollY, scrollRangeX, scrollRangeY,
					maxOverScrollX, maxOverScrollY, isTouchEvent);

			// Does all of the hard work...
			OverscrollHelper.overScrollBy(PullToRefreshStaggeredGridView.this,
					deltaX, scrollX, deltaY, scrollY, isTouchEvent);

			return returnValue;
		}
	}
}
