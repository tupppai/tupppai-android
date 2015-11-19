package com.psgod.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.View;

import com.handmark.pulltorefresh.library.OverscrollHelper;
import com.handmark.pulltorefresh.library.PullToRefreshAdapterViewBase;
import com.handmark.pulltorefresh.library.internal.EmptyViewMethodAccessor;

public class PullToRefreshSwipeMenuListView extends
		PullToRefreshAdapterViewBase<NotificationListView> {

	public PullToRefreshSwipeMenuListView(Context context) {
		super(context);
	}

	public PullToRefreshSwipeMenuListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PullToRefreshSwipeMenuListView(Context context, Mode mode) {
		super(context, mode);
	}

	public PullToRefreshSwipeMenuListView(Context context, Mode mode,
			AnimationStyle style) {
		super(context, mode, style);
	}

	@Override
	public Orientation getPullToRefreshScrollDirection() {
		return Orientation.VERTICAL;
	}

	@Override
	protected NotificationListView createRefreshableView(Context context,
			AttributeSet attrs) {
		final NotificationListView sv;

		if (VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
			sv = new InternalNotificationListView(context, attrs);
		} else {
			sv = new InternalNotificationListViewSDK9(context, attrs);
		}

		// Use Generated ID (from res/values/ids.xml)
		// sv.setId(R.id.swipeview);
		return sv;
	}

	class InternalNotificationListView extends NotificationListView implements
			EmptyViewMethodAccessor {
		public InternalNotificationListView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		public void setEmptyView(View emptyView) {
			PullToRefreshSwipeMenuListView.this.setEmptyView(emptyView);
		}

		@Override
		public void setEmptyViewInternal(View emptyView) {
			super.setEmptyView(emptyView);
		}
	}

	@TargetApi(9)
	final class InternalNotificationListViewSDK9 extends
			InternalNotificationListView {
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
			OverscrollHelper.overScrollBy(PullToRefreshSwipeMenuListView.this,
					deltaX, scrollX, deltaY, scrollY, isTouchEvent);

			return returnValue;
		}
	}
}
