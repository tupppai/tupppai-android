package com.psgod.ui.widget.dialog;

import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.psgod.Constants;
import com.psgod.PSGodApplication;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.eventbus.MyPageRefreshEvent;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.ActionShareRequest;
import com.psgod.network.request.MyInProgressDeleteRequest;
import com.psgod.network.request.PSGodRequestQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;
import de.greenrobot.event.EventBus;

/**
 * 长按进行中时 出现更多分享选择按钮
 * 
 * @author ZouMengyuan
 */
public class InprogressShareMoreDialog extends Dialog {

	private static final String TAG = InprogressShareMoreDialog.class
			.getSimpleName();

	public static final int TYPE_ASK = 1;
	public static final int TYPE_REPLY = 2;
	public static final int STATUS_UNCOLLECTION = 101;
	public static final int STATUS_COLLECTION = 102;

	private Context mContext;
	// 对应的photoItem
	private PhotoItem mPhotoItem;

	private Button mShareWechatFriend;
	private Button mShareWechatMoments;
	private Button mShareWeibo;
	private Button mShareQQ;
	private Button mShareQzone;

	// private Button mInviteBtn;
	private Button mShareLink;
	private Button mDeleteBtn;
	private Button mCancelBtn;
	// 0求P 1已完成 2帮P
	private int type;

	public static int SHARE_TYPE_ASK = 0;
	public static int SHARE_TYPE_REPLY = 1;
	public static int SHARE_TYPE_COMPLETE = 2;

	public InprogressShareMoreDialog(Context context) {
		super(context, R.style.ActionSheetDialog);
		setContentView(R.layout.dialog_inprogress_more_share);

		mContext = context;

		getWindow().getAttributes().width = Constants.WIDTH_OF_SCREEN;
		setCanceledOnTouchOutside(true);

		// 初始化组件
		mContext = context;

		mShareWechatFriend = (Button) findViewById(R.id.dialog_more_share_wechat);
		mShareWechatMoments = (Button) findViewById(R.id.dialog_more_share_moments);
		mShareWeibo = (Button) findViewById(R.id.dialog_more_share_sina);
		mShareQQ = (Button) findViewById(R.id.dialog_more_share_qq_friend);
		mShareQzone = (Button) findViewById(R.id.dialog_more_share_qzone);

		// mInviteBtn = (Button) findViewById(R.id.dialog_more_share_invite);
		mShareLink = (Button) findViewById(R.id.dialog_more_share_link);
		mDeleteBtn = (Button) findViewById(R.id.dialog_more_share_delete);
		mCancelBtn = (Button) findViewById(R.id.dialog_more_share_cancel);

		initListeners();
	}

	private ErrorListener errorListener = new ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {
			Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_LONG)
					.show();

		}
	};

	private void initListeners() {
		// 邀请按钮
		// mInviteBtn.setOnClickListener(new android.view.View.OnClickListener()
		// {
		// @Override
		// public void onClick(View view) {
		// Intent intent = new Intent(getContext(),
		// InvitationListActivity.class);
		// //传递图片的ask_id
		// intent.putExtra(Constants.IntentKey.ASK_ID, mPhotoItem.getAskId());
		// mContext.startActivity(intent);
		// }
		// });

		// 复制链接
		mShareLink.setOnClickListener(new android.view.View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ActionShareRequest.Builder builder = new ActionShareRequest.Builder()
						.setShareType("copy").setType(mPhotoItem.getType())
						.setId(mPhotoItem.getPid()).setListener(copyListener)
						.setErrorListener(errorListener);

				ActionShareRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						PSGodApplication.getAppContext()).getRequestQueue();
				requestQueue.add(request);

				dismiss();
			}
		});

		// 分享到新浪微博
		mShareWeibo.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Utils.showProgressDialog(mContext);

				ActionShareRequest.Builder builder = new ActionShareRequest.Builder()
						.setShareType("weibo").setType(mPhotoItem.getType())
						.setId(mPhotoItem.getPid())
						.setListener(shareWeiboListener)
						.setErrorListener(errorListener);

				ActionShareRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						PSGodApplication.getAppContext()).getRequestQueue();
				requestQueue.add(request);
			}
		});

		// 分享到qq
		mShareQQ.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.showProgressDialog(mContext);

				ActionShareRequest.Builder builder = new ActionShareRequest.Builder()
						.setShareType("qq").setType(mPhotoItem.getType())
						.setId(mPhotoItem.getPid())
						.setListener(shareQQlistener)
						.setErrorListener(errorListener);

				ActionShareRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						PSGodApplication.getAppContext()).getRequestQueue();
				requestQueue.add(request);
			}
		});

		// 分享到qzone
		mShareQzone.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.showProgressDialog(mContext);

				ActionShareRequest.Builder builder = new ActionShareRequest.Builder()
						.setShareType("qzone").setType(mPhotoItem.getType())
						.setId(mPhotoItem.getPid())
						.setListener(shareQzoneListener)
						.setErrorListener(errorListener);

				ActionShareRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						PSGodApplication.getAppContext()).getRequestQueue();
				requestQueue.add(request);
			}
		});

		// 分享到微信朋友圈
		mShareWechatMoments
				.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Utils.showProgressDialog(mContext);

						ActionShareRequest.Builder builder = new ActionShareRequest.Builder()
								.setShareType("moments")
								.setType(mPhotoItem.getType())
								.setId(mPhotoItem.getPid())
								.setListener(shareMomentsListener)
								.setErrorListener(errorListener);

						ActionShareRequest request = builder.build();
						request.setTag(TAG);
						RequestQueue requestQueue = PSGodRequestQueue
								.getInstance(PSGodApplication.getAppContext())
								.getRequestQueue();
						requestQueue.add(request);
					}
				});

		// 分享给微信好友
		mShareWechatFriend
				.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Utils.showProgressDialog(mContext);

						ActionShareRequest.Builder builder = new ActionShareRequest.Builder()
								.setShareType("wechat")
								.setType(mPhotoItem.getType())
								.setId(mPhotoItem.getPid())
								.setListener(shareFriendsListener)
								.setErrorListener(errorListener);

						ActionShareRequest request = builder.build();
						request.setTag(TAG);
						RequestQueue requestQueue = PSGodRequestQueue
								.getInstance(PSGodApplication.getAppContext())
								.getRequestQueue();
						requestQueue.add(request);
					}
				});

		mDeleteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
				if (type == SHARE_TYPE_ASK) {
					MyInProgressDeleteRequest.Builder builder = new MyInProgressDeleteRequest.Builder()
							.setType(SHARE_TYPE_ASK)
							.setId(mPhotoItem.getPid())
							.setListener(deleteListener)
							.setErrorListener(errorListener);

					MyInProgressDeleteRequest request = builder.build();
					request.setTag(TAG);
					RequestQueue requestQueue = PSGodRequestQueue.getInstance(
							mContext.getApplicationContext()).getRequestQueue();
					requestQueue.add(request);
				}else if (type == SHARE_TYPE_REPLY) {
					MyInProgressDeleteRequest.Builder builder = new MyInProgressDeleteRequest.Builder()
							.setType(SHARE_TYPE_REPLY)
							.setId(mPhotoItem.getPid())
							.setListener(deleteListener)
							.setErrorListener(errorListener);

					MyInProgressDeleteRequest request = builder.build();
					request.setTag(TAG);
					RequestQueue requestQueue = PSGodRequestQueue.getInstance(
							mContext.getApplicationContext()).getRequestQueue();
					requestQueue.add(request);
				} else {
					MyInProgressDeleteRequest.Builder builder = new MyInProgressDeleteRequest.Builder()
							.setType(SHARE_TYPE_COMPLETE)
							.setId(mPhotoItem.getPid())
							.setListener(deleteListener)
							.setErrorListener(errorListener);

					MyInProgressDeleteRequest request = builder.build();
					request.setTag(TAG);
					RequestQueue requestQueue = PSGodRequestQueue.getInstance(
							mContext.getApplicationContext()).getRequestQueue();
					requestQueue.add(request);
				}

			}
		});

		mCancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
			}
		});
	}

	private Listener<JSONObject> copyListener = new Listener<JSONObject>() {
		@Override
		public void onResponse(JSONObject response) {
			ClipboardManager clip = (ClipboardManager) mContext
					.getSystemService(Context.CLIPBOARD_SERVICE);
			try {
				clip.setText(response.getString("url"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // 复制
			Toast.makeText(mContext, "复制成功", Toast.LENGTH_SHORT).show();
		}
	};

	// 删除接口回调
	private Listener<Boolean> deleteListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			if (response == true) {
				Toast.makeText(mContext, "删除成功", Toast.LENGTH_SHORT).show();
				EventBus.getDefault().post(new MyPageRefreshEvent(3));
			}
		}
	};

	// qzone分享回调
	private Listener<JSONObject> shareQzoneListener = new Listener<JSONObject>() {
		@Override
		public void onResponse(JSONObject response) {
			Utils.hideProgressDialog();

			ShareSDK.initSDK(mContext);
			try {
				OnekeyShare oks = new OnekeyShare();

				oks.setPlatform(QZone.NAME);
				oks.setTitle(response.getString("title"));
				oks.setText(response.getString("desc"));
				oks.setTitleUrl(response.getString("url"));
				oks.setImageUrl(response.getString("image"));
				oks.setSite("图pai");
				oks.setSiteUrl(Constants.OFFICAL_WEBSITE);

				oks.show(mContext);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	// 微信好友分享请求回调
	private Listener<JSONObject> shareFriendsListener = new Listener<JSONObject>() {
		@Override
		public void onResponse(JSONObject response) {
			Utils.hideProgressDialog();

			ShareSDK.initSDK(mContext);
			Platform wechatFriends = ShareSDK
					.getPlatform(mContext, Wechat.NAME);
			wechatFriends
					.setPlatformActionListener(new PlatformActionListener() {
						@Override
						public void onError(Platform arg0, int arg1,
								Throwable arg2) {
						}

						@Override
						public void onComplete(Platform arg0, int arg1,
								HashMap<String, Object> arg2) {
						}

						@Override
						public void onCancel(Platform arg0, int arg1) {
						}
					});

			try {
				if (response.getString("type").equals("image")) {
					ShareParams sp = new ShareParams();
					sp.setShareType(Platform.SHARE_IMAGE);
					sp.setText(response.getString("desc"));
					sp.setImageUrl(response.getString("image"));
					wechatFriends.share(sp);
				}
				if (response.getString("type").equals("url")) {
					// 图文链接分享
					ShareParams sp = new ShareParams();
					sp.setShareType(Platform.SHARE_WEBPAGE);
					sp.setTitle(response.getString("title"));
					sp.setText(response.getString("desc"));
					sp.setImageUrl(response.getString("image"));
					sp.setUrl(response.getString("url"));
					wechatFriends.share(sp);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	// 微信朋友圈分享接口请求回调
	private Listener<JSONObject> shareMomentsListener = new Listener<JSONObject>() {
		@Override
		public void onResponse(JSONObject response) {
			Utils.hideProgressDialog();

			ShareSDK.initSDK(mContext);
			Platform wechat = ShareSDK
					.getPlatform(mContext, WechatMoments.NAME);
			wechat.setPlatformActionListener(new PlatformActionListener() {
				@Override
				public void onError(Platform arg0, int arg1, Throwable arg2) {
				}

				@Override
				public void onComplete(Platform arg0, int arg1,
						HashMap<String, Object> arg2) {
				}

				@Override
				public void onCancel(Platform arg0, int arg1) {
				}
			});

			try {
				if (response.getString("type").equals("image")) {
					ShareParams sp = new ShareParams();
					sp.setShareType(Platform.SHARE_IMAGE);
					sp.setText(response.getString("desc"));
					sp.setImageUrl(response.getString("image"));
					wechat.share(sp);
				}
				if (response.getString("type").equals("url")) {
					// 图文链接分享
					ShareParams sp = new ShareParams();
					sp.setShareType(Platform.SHARE_WEBPAGE);
					sp.setTitle(response.getString("title"));
					sp.setText(response.getString("desc"));
					sp.setImageUrl(response.getString("image"));
					sp.setUrl(response.getString("url"));
					wechat.share(sp);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	// qq分享接口请求回调
	private Listener<JSONObject> shareQQlistener = new Listener<JSONObject>() {
		@Override
		public void onResponse(JSONObject response) {
			Utils.hideProgressDialog();

			ShareSDK.initSDK(mContext);
			try {
				OnekeyShare oks = new OnekeyShare();

				oks.setPlatform(QQ.NAME);
				oks.setTitle(response.getString("title"));
				oks.setTitleUrl(response.getString("url"));
				oks.setText(response.getString("desc"));
				oks.setImageUrl(response.getString("image"));

				oks.show(mContext);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	};

	// 微博分享接口请求回调
	private Listener<JSONObject> shareWeiboListener = new Listener<JSONObject>() {
		@Override
		public void onResponse(JSONObject response) {
			Utils.hideProgressDialog();

			ShareSDK.initSDK(mContext);
			try {
				OnekeyShare oks = new OnekeyShare();

				oks.setPlatform(SinaWeibo.NAME);
				oks.disableSSOWhenAuthorize();
				oks.setSilent(false);
				oks.setText(response.getString("desc"));
				oks.setImageUrl(response.getString("image"));
				oks.show(mContext);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};

	public void setPhotoItem(PhotoItem photoItem, int type) {
		mPhotoItem = photoItem;
		this.type = type;
	}

	@Override
	public void show() {
		super.show();
		getWindow().setGravity(Gravity.BOTTOM);
		getWindow().setWindowAnimations(R.style.popwindow_anim_style);
	}

	public static final int GONETYPE_DELETE = 0;

	public void show(int goneType) {
		if (goneType == GONETYPE_DELETE
				&& mDeleteBtn.getVisibility() == View.INVISIBLE) {
			mDeleteBtn.setVisibility(View.VISIBLE);
		}
		show();
	}

}
