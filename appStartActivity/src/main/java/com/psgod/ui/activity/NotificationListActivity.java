package com.psgod.ui.activity;

/**
 * 消息列表界面
 * @author Rayal
 */

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.psgod.Constants;
import com.psgod.Logger;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.notification.INotification;
import com.psgod.model.notification.NotificationMessage;
import com.psgod.network.request.ActionDeleteMessageRequest;
import com.psgod.network.request.NotificationListRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.ui.adapter.NotificationListAdapter;
import com.psgod.ui.view.PullToRefreshSwipeMenuListView;
import com.psgod.ui.widget.ActionBar;
import com.psgod.ui.widget.dialog.CarouselPhotoDetailDialog;
import com.psgod.ui.widget.dialog.CustomDialog;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import java.util.ArrayList;
import java.util.List;

public class NotificationListActivity extends PSGodBaseActivity {
	private final static String TAG = NotificationListActivity.class
			.getSimpleName();

	// 系统消息类型
	public static final int SYSTEM_MESSAGE_TYPE_URL = 0; // 打开链接跳转
	public static final int SYSTEM_MESSAGE_TYPE_ASK = 1;
	public static final int SYSTEM_MESSAGE_TYPE_REPLY = 2;
	public static final int SYSTEM_MESSAGE_TYPE_COMMENT = 3;
	public static final int SYSTEM_MESSAGE_TYPE_USER = 4;

	public static final int TYPE_COMMENT_NOTIFICATION = NotificationListRequest.TYPE_COMMENT_NOTIFICATION;
	public static final int TYPE_REPLY_NOTIFICATION = NotificationListRequest.TYPE_REPLY_NOTIFICATION;
	public static final int TYPE_FOLLOW_NOTIFICATION = NotificationListRequest.TYPE_FOLLOW_NOTIFICATION;
	public static final int TYPE_INVITE_NOTIFICATION = NotificationListRequest.TYPE_INVITE_NOTIFICATION;
	public static final int TYPE_SYSTEM_NOTIFICATION = NotificationListRequest.TYPE_SYSTEM_NOTIFICATION;

	private ActionBar mActoinBar;
	private PullToRefreshSwipeMenuListView mNotificationListView;
	private NotificationListAdapter mAdapter;
	private List<INotification> mNotificationList;
	private NotificationListListener mListener; // TODO 还没添加Listener

	private View mEmptyView;
	private CustomProgressingDialog mProgressDialog;
	private int mPage;
	private int mType;

	private long mLastUpdatedTime;
	private static final long DEFAULT_LAST_REFRESH_TIME = -1;

	// 控制是否可以加载下一页
	private boolean canLoadMore = true;
	private View mNotificationListFooter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_notification_list);

		mType = getIntent().getIntExtra(
				Constants.IntentKey.NOTIFICATION_LIST_TYPE, -1);
		if (mType == -1) {
			Utils.showDebugToast(TAG + ": No NotificationActivity Type");
			Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_DEV, TAG, TAG
					+ ": No NotificationActivity Type");
			return;
		}

		mNotificationList = new ArrayList<INotification>();

		// 初始化视图
		mActoinBar = (ActionBar) this.findViewById(R.id.actionbar);
		SetActionBarTitle();
		mNotificationListView = (PullToRefreshSwipeMenuListView) this
				.findViewById(R.id.activity_notification_comment_list_listview);
		mNotificationListView.getRefreshableView().setDividerHeight(0);
		mNotificationListView.setMode(Mode.PULL_FROM_START);

		mAdapter = new NotificationListAdapter(this, mNotificationList);
		mNotificationListView.getRefreshableView().setAdapter(mAdapter);

		mNotificationListFooter = LayoutInflater.from(
				NotificationListActivity.this).inflate(
				R.layout.footer_load_more, null);
		mNotificationListView.getRefreshableView().addFooterView(
				mNotificationListFooter);
		mNotificationListFooter.setVisibility(View.GONE);

		if (mProgressDialog == null) {
			mProgressDialog = new CustomProgressingDialog(
					NotificationListActivity.this);
		}
		if (!mProgressDialog.isShowing()) {
			mProgressDialog.show();
		}

		mListener = new NotificationListListener(NotificationListActivity.this);

		mNotificationListView.setOnRefreshListener(mListener);
		mNotificationListView.setOnLastItemVisibleListener(mListener);
		mNotificationListView.setScrollingWhileRefreshingEnabled(true);

		initListener();
		refresh();
	}

	private void SetActionBarTitle() {
		switch (mType) {
		case TYPE_COMMENT_NOTIFICATION:
			mActoinBar.setTitle("评论");
			break;
		case TYPE_REPLY_NOTIFICATION:
			mActoinBar.setTitle("帖子回复");
			break;
		case TYPE_FOLLOW_NOTIFICATION:
			mActoinBar.setTitle("关注通知");
			break;
		case TYPE_INVITE_NOTIFICATION:
			mActoinBar.setTitle("邀请通知");
			break;
		case TYPE_SYSTEM_NOTIFICATION:
			mActoinBar.setTitle("系统通知");
			break;
		}
	}

	/**
	 * 暂停所有的下载
	 */
	@Override
	public void onStop() {
		super.onStop();
		RequestQueue requestQueue = PSGodRequestQueue.getInstance(this)
				.getRequestQueue();
		requestQueue.cancelAll(TAG);
	}

	private class NotificationListListener implements OnRefreshListener,
			OnLastItemVisibleListener {
		// private int mPage;
		private Context mContext;

		public NotificationListListener(Context context) {
			mContext = context;
			SharedPreferences sp = mContext.getSharedPreferences(
					Constants.SharedPreferencesKey.NAME, Context.MODE_PRIVATE);
			mLastUpdatedTime = sp
					.getLong(mType + "", DEFAULT_LAST_REFRESH_TIME);
		}

		@Override
		public void onRefresh(PullToRefreshBase refreshView) {
			// TODO Auto-generated method stub
			refresh();
		}

		@Override
		public void onLastItemVisible() {
			if (canLoadMore) {
				mPage += 1;
				mNotificationListFooter.setVisibility(View.VISIBLE);
				NotificationListRequest.Builder builder = new NotificationListRequest.Builder()
						.setPage(mPage).setType(mType)
						.setErrorListener(errorListener)
						.setListener(loadMoreListener);
				NotificationListRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						mContext).getRequestQueue();
				requestQueue.add(request);
			}
		}

	}

	private void refresh() {
		canLoadMore = false;
		mPage = 1;
		if (mLastUpdatedTime == DEFAULT_LAST_REFRESH_TIME) {
			mLastUpdatedTime = System.currentTimeMillis();
		}
		NotificationListRequest.Builder builder = new NotificationListRequest.Builder()
				.setPage(mPage).setType(mType)
				// TYPE_COMMENT_NOTIFICATION
				.setLastUpdated(mLastUpdatedTime)
				.setErrorListener(errorListener).setListener(refreshListener);
		NotificationListRequest request = builder.build();
		request.setTag(TAG);
		RequestQueue requestQueue = PSGodRequestQueue.getInstance(this)
				.getRequestQueue();
		requestQueue.add(request);
	}

	private Listener<List<? extends INotification>> refreshListener = new Listener<List<? extends INotification>>() {
		@Override
		public void onResponse(List<? extends INotification> response) {
			mNotificationList.clear();
			mNotificationList.addAll(response);
			mAdapter.notifyDataSetChanged();
			mNotificationListView.onRefreshComplete();

			if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}

			if (response.size() < 10) {
				canLoadMore = false;
			} else {
				canLoadMore = true;
			}

			// 刷新之后再添加emptyView
			if (mEmptyView == null) {
				mEmptyView = NotificationListActivity.this
						.findViewById(R.id.activity_notification_comment_list_empty_view);
				mNotificationListView.getRefreshableView().setEmptyView(
						mEmptyView);
			}

			// 保存本次刷新时间到sp
			mLastUpdatedTime = System.currentTimeMillis();
			if (android.os.Build.VERSION.SDK_INT >= 9) {
				getApplicationContext()
						.getSharedPreferences(
								Constants.SharedPreferencesKey.NAME,
								Context.MODE_PRIVATE).edit()
						.putLong(mType + "", mLastUpdatedTime).apply();
			} else {
				getApplicationContext()
						.getSharedPreferences(
								Constants.SharedPreferencesKey.NAME,
								Context.MODE_PRIVATE).edit()
						.putLong(mType + "", mLastUpdatedTime).commit();
			}
		}
	};

	private Listener<List<? extends INotification>> loadMoreListener = new Listener<List<? extends INotification>>() {
		@Override
		public void onResponse(List<? extends INotification> response) {
			if (response.size() > 0) {
				mNotificationList.addAll(response);
				mAdapter.notifyDataSetChanged();
				mNotificationListView.onRefreshComplete();
			}

			mNotificationListFooter.setVisibility(View.INVISIBLE);

			if (response.size() < 10) {
				canLoadMore = false;
			} else {
				canLoadMore = true;
			}
		}
	};

	private PSGodErrorListener errorListener = new PSGodErrorListener(this) {
		@Override
		public void handleError(VolleyError error) {
			// TODO
			mNotificationListView.onRefreshComplete();
		}
	};

	private void initListener() {
		mNotificationListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long arg3) {
				// 根据type 分别进行跳转
				if (mType == TYPE_COMMENT_NOTIFICATION) {
					// 打开照片详情页 带评论
					if (mNotificationList.get(position)
							.getNotificationPhotoItem() != null) {
						SinglePhotoDetail.startActivity(
								NotificationListActivity.this,
								mNotificationList.get(position)
										.getNotificationPhotoItem());
					}
				}
				if (mType == TYPE_FOLLOW_NOTIFICATION) {
					if (mNotificationList.get(position).getNotificationUid() != -1) {
						Intent intent = new Intent(
								NotificationListActivity.this,
								UserProfileActivity.class);
						intent.putExtra(Constants.IntentKey.USER_ID,
								mNotificationList.get(position)
										.getNotificationUid());
						startActivity(intent);
					}
				}
				if (mType == TYPE_INVITE_NOTIFICATION) {
					if (mNotificationList.get(position)
							.getNotificationPhotoItem() != null) {
						// PhotoDetailActivity.startActivity(NotificationListActivity.this,
						// mNotificationList.get(position).getNotificationPhotoItem());
						// 邀请打开照片详情页 带评论
						SinglePhotoDetail.startActivity(
								NotificationListActivity.this,
								mNotificationList.get(position)
										.getNotificationPhotoItem());
					}
				}
				if (mType == TYPE_REPLY_NOTIFICATION) {
					if (mNotificationList.get(position)
							.getNotificationPhotoItem() != null) {
//						CarouselPhotoDetailActivity.startActivity(
//								NotificationListActivity.this,
//								mNotificationList.get(position)
//										.getNotificationPhotoItem());
						Utils.skipByObject(NotificationListActivity.this,mNotificationList.get(position)
								.getNotificationPhotoItem());
//						new CarouselPhotoDetailDialog(NotificationListActivity.this,mNotificationList.get(position)
//								.getNotificationPhotoItem().getAskId(),mNotificationList.get(position)
//								.getNotificationPhotoItem().getPid()).show();
					}
				}
				if (mType == TYPE_SYSTEM_NOTIFICATION) {
					NotificationMessage message = mNotificationList
							.get(position).getNotificationMessage();
					if (message != null) {
						int type = message.getType();

						// 暂时只处理URL ASK USER三种系统消息类型

						// url内嵌浏览器打开
						if (type == SYSTEM_MESSAGE_TYPE_URL) {
							String url = message.getJumpUrl().toString();
							if (!TextUtils.isEmpty(url)) {
								Intent intent = new Intent(
										NotificationListActivity.this,
										WebBrowserActivity.class);
								intent.putExtra(WebBrowserActivity.KEY_URL, url);
								startActivity(intent);
							}
						}
						// 跳转至帖子详情页
						// if (type == SYSTEM_MESSAGE_TYPE_ASK) {
						// if (message.getTargetId() != null) {
						// PhotoDetailActivity.startActivity(
						// NotificationListActivity.this,
						// message.getTargetId());
						// }
						// }
						if (type == SYSTEM_MESSAGE_TYPE_COMMENT) {

						}
						if (type == SYSTEM_MESSAGE_TYPE_REPLY) {

						}
						// 打开个人页面
						if (type == SYSTEM_MESSAGE_TYPE_USER) {
							if (message.getUid() != null) {
								Intent intent = new Intent(
										NotificationListActivity.this,
										UserProfileActivity.class);
								intent.putExtra(Constants.IntentKey.USER_ID,
										message.getUid());
								startActivity(intent);
							}
						}
					}
				}
			}
		});

		// 点击ActionBar的右键，清除所有消息
		mActoinBar.setRightBtnOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO 对话框文字
				CustomDialog.Builder mBuilder = new CustomDialog.Builder(
						NotificationListActivity.this)
						.setMessage("你确定要清空所有记录吗？")
						.setLeftButton("取消", null)
						.setRightButton("确定",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										// 所有消息id间用逗号隔开
										String mMessageIds = "";
										int mMessageLength = mNotificationList
												.size();
										if (mMessageLength > 0) {
											// for (int i = 0; i <
											// mNotificationList.size() - 1;
											// i++) {
											// mMessageIds = mMessageIds +
											// Long.toString(mNotificationList.get(i).getNotificationId())
											// + ",";
											// }
											// mMessageIds = mMessageIds +
											// Long.toString(mNotificationList.get(mMessageLength
											// - 1).getNotificationId());

											ActionDeleteMessageRequest.Builder builder = new ActionDeleteMessageRequest.Builder()
													.setType(mType)
													.setListener(
															deleteMessageListener)
													.setErrorListener(
															errorListener);

											ActionDeleteMessageRequest request = builder
													.build();
											request.setTag(TAG);
											RequestQueue requestQueue = PSGodRequestQueue
													.getInstance(
															NotificationListActivity.this)
													.getRequestQueue();
											requestQueue.add(request);
										} else {
											Toast.makeText(
													NotificationListActivity.this,
													"消息列表为空",
													Toast.LENGTH_SHORT).show();
										}
									}
								});
				mBuilder.create().show();
			}
		});
	}

	private Listener<Boolean> deleteMessageListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			if (response) {
				mNotificationList.clear();
				mAdapter.notifyDataSetChanged();
			}
		}
	};
}
