package com.pires.wesee.ui.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.pires.wesee.model.notification.INotification;
import com.pires.wesee.network.request.ActionDeleteMessageRequest;
import com.pires.wesee.network.request.PSGodErrorListener;
import com.pires.wesee.network.request.PSGodRequestQueue;
import com.pires.wesee.ui.adapter.BaseNotificationAdapter;
import com.pires.wesee.ui.widget.dialog.CustomDialog;
import com.pires.wesee.R;

import java.util.List;

public class NotificationListView extends SwipeMenuListView {
	private static final String TAG = NotificationListView.class
			.getSimpleName();
	private Context mContext; 
	private BaseNotificationAdapter mAdapter;
	private List<? extends INotification> mNotificationList;

	private int mMessagePosition;

	public NotificationListView(Context context) {
		super(context);
		init(context);
	}

	public NotificationListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		init(context);
	}

	public NotificationListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mContext = context;

		SwipeMenuCreator creator = new SwipeMenuCreator() {
			@Override
			public void create(SwipeMenu menu) {
				SwipeMenuItem item = new SwipeMenuItem(mContext);
				item.setBackground(R.drawable.selector_swipe_menu_item_btn_color);
				item.setIcon(R.drawable.word_delete);
				item.setWidth(mContext.getResources().getDimensionPixelSize(
						R.dimen.msg_list_size));

				menu.addMenuItem(item);
			}
		};

		setMenuCreator(creator);

		// 左滑删除消息
		setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(int position, SwipeMenu menu,
					int index) {
				switch (index) {
				case 0:
					if ((mNotificationList != null) && (mAdapter != null)) {
						mMessagePosition = position;

						final long messageId = mNotificationList.get(position)
								.getNotificationId();
						Log.v("test", Long.toString(messageId));

						CustomDialog.Builder mBuilder = new CustomDialog.Builder(
								mContext)
								.setMessage("你确定要删除该条记录吗？")
								.setLeftButton("取消", null)
								.setRightButton("确定",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface arg0,
													int arg1) {
												ActionDeleteMessageRequest.Builder builder = new ActionDeleteMessageRequest.Builder()
														.setMessageIds(
																Long.toString(messageId))
														.setListener(
																deleteMessageListener)
														.setErrorListener(
																errorListener);

												ActionDeleteMessageRequest request = builder
														.build();
												request.setTag(TAG);
												RequestQueue requestQueue = PSGodRequestQueue
														.getInstance(mContext)
														.getRequestQueue();
												requestQueue.add(request);
											}
										});
						mBuilder.create().show();
					}
					break;
				default:
					break;
				}
				return false;
			}
		});
		setDivider(new ColorDrawable(Color.parseColor("#33000000")));
		setDividerHeight(mContext.getResources().getDimensionPixelSize(
				R.dimen.divider_height));
	}

	private Listener<Boolean> deleteMessageListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			if (response) {
				mNotificationList.remove(mMessagePosition);
				mAdapter.notifyDataSetChanged();
			}
		}
	};

	private PSGodErrorListener errorListener = new PSGodErrorListener(
			ActionDeleteMessageRequest.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {
		}
	};

	public void setAdapter(BaseNotificationAdapter adapter) {
		if (adapter == null) {
			throw new IllegalArgumentException(TAG
					+ "setAdapter(): The adapter is null");
		}
		super.setAdapter(adapter);
		mAdapter = adapter;
		mNotificationList = mAdapter.getNotificationList();
	}
}
