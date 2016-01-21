package com.psgod.ui.widget.dialog;

/**
 * 点击三点按钮时 出现更多分享选择按钮
 *
 * @author brandwang
 */

import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
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
import com.psgod.network.request.ActionCollectionRequest;
import com.psgod.network.request.ActionShareRequest;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.ui.widget.ShareButton;

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

public class ShareMoreDialog extends Dialog {
    private static final String TAG = ShareMoreDialog.class.getSimpleName();

    public static final int TYPE_ASK = 1;
    public static final int TYPE_REPLY = 2;
    public static final int STATUS_UNCOLLECTION = 101;
    public static final int STATUS_COLLECTION = 102;

    private Context mContext;
    // 对应的photoItem
    private PhotoItem mPhotoItem;

    private ShareButton mShareWechatFriend;
    private ShareButton mShareWechatMoments;
    private ShareButton mShareWeibo;
    private ShareButton mShareQQ;
    private ShareButton mShareQzone;

    // private Button mInviteBtn;
    private Button mShareLink;
    private Button mReportBtn;
    private Button mCollectionBtn;
    private Button mCancelBtn;

    // 是否被收藏标识
    private Boolean isCollected = false;

    public ShareMoreDialog(Context context) {
        super(context, R.style.ActionSheetDialog);
        setContentView(R.layout.dialog_more_share);

        mContext = context;

        getWindow().getAttributes().width = Constants.WIDTH_OF_SCREEN;
        setCanceledOnTouchOutside(true);

        // 初始化组件
        mContext = context;

        mShareWechatFriend = (ShareButton) findViewById(R.id.dialog_more_share_wechat);
        mShareWechatFriend.
                setShareType(ShareButton.TYPE_WECHAT_FRIEND, ShareButton.TYPE_DRAWABLE_TOP);
        mShareWechatFriend.setPhotoItem(mPhotoItem);
        mShareWechatMoments = (ShareButton) findViewById(R.id.dialog_more_share_moments);
        mShareWechatMoments.
                setShareType(ShareButton.TYPE_WECHAT_MOMENTS, ShareButton.TYPE_DRAWABLE_TOP);
        mShareWechatMoments.setPhotoItem(mPhotoItem);
        mShareWeibo = (ShareButton) findViewById(R.id.dialog_more_share_sina);
        mShareWeibo.
                setShareType(ShareButton.TYPE_WEIBO, ShareButton.TYPE_DRAWABLE_TOP);
        mShareWeibo.setPhotoItem(mPhotoItem);
        mShareQQ = (ShareButton) findViewById(R.id.dialog_more_share_qq_friend);
        mShareQQ.
                setShareType(ShareButton.TYPE_QQ, ShareButton.TYPE_DRAWABLE_TOP);
        mShareQQ.setPhotoItem(mPhotoItem);
        mShareQzone = (ShareButton) findViewById(R.id.dialog_more_share_qzone);
        mShareQzone.
                setShareType(ShareButton.TYPE_QZONE, ShareButton.TYPE_DRAWABLE_TOP);
        mShareQzone.setPhotoItem(mPhotoItem);

        // mInviteBtn = (Button) findViewById(R.id.dialog_more_share_invite);
        mShareLink = (Button) findViewById(R.id.dialog_more_share_link);
        mCollectionBtn = (Button) findViewById(R.id.dialog_more_share_collect);
        mReportBtn = (Button) findViewById(R.id.dialog_more_share_report);
        mCancelBtn = (Button) findViewById(R.id.dialog_more_share_cancel);

        initListeners();
    }

    // 收藏 取消收藏 listener
    private Listener<Boolean> actionCollectionListener = new Listener<Boolean>() {
        @Override
        public void onResponse(Boolean response) {
            if (response == true) {
                Resources res = mContext.getResources();
                if (isCollected) {
                    Drawable img = res.getDrawable(R.drawable.fav_nor);
                    img.setBounds(0, 0, img.getMinimumWidth(),
                            img.getMinimumHeight());
                    mCollectionBtn.setCompoundDrawables(null, img, null, null);
                    mCollectionBtn.setText("收藏");
                    Toast.makeText(mContext, "取消收藏成功", Toast.LENGTH_SHORT)
                            .show();

                    isCollected = false;
                    mPhotoItem.setIsCollected(false);
                } else {
                    Drawable img = res.getDrawable(R.drawable.fav);
                    img.setBounds(0, 0, img.getMinimumWidth(),
                            img.getMinimumHeight());
                    mCollectionBtn.setCompoundDrawables(null, img, null, null);
                    mCollectionBtn.setText("已收藏");
                    Toast.makeText(mContext, "收藏成功", Toast.LENGTH_SHORT).show();

                    isCollected = true;
                    mPhotoItem.setIsCollected(true);
                }
                EventBus.getDefault().post(
                        new MyPageRefreshEvent(MyPageRefreshEvent.COLLECTION));
                mCollectionBtn.setClickable(true);
            }
        }
    };

    private ErrorListener errorListener = new ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(mContext, "收藏失败", Toast.LENGTH_LONG)
                    .show();
            mCollectionBtn.setClickable(true);
        }
    };

    private ShareButton.OnShareListener onShareListener = new ShareButton.OnShareListener(){

        @Override
        public void onError(Platform platform, int arg1, Throwable arg2) {

        }

        @Override
        public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
            ShareMoreDialog.this.dismiss();
        }

        @Override
        public void onCancel(Platform arg0, int arg1) {

        }
    };

    private void initListeners() {
        // 邀请按钮 TODO
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
        mShareWechatFriend.setOnShareListener(onShareListener);
        mShareWechatMoments.setOnShareListener(onShareListener);
        mShareWeibo.setOnShareListener(onShareListener);
        mShareQQ.setOnShareListener(onShareListener);
        mShareQzone.setOnShareListener(onShareListener);
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

        // 收藏 取消收藏
        mCollectionBtn
                .setOnClickListener(new android.view.View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 请求网络时不可点击
                        mCollectionBtn.setClickable(false);
                        int mCurrentStatus = isCollected ? STATUS_UNCOLLECTION
                                : STATUS_COLLECTION;

                        ActionCollectionRequest.Builder builder = new ActionCollectionRequest.Builder()
                                .setType(mPhotoItem.getType())
                                .setPid(mPhotoItem.getPid())
                                .setStatus(mCurrentStatus)
                                .setErrorListener(errorListener)
                                .setListener(actionCollectionListener);

                        ActionCollectionRequest request = builder.build();
                        RequestQueue requestQueue = PSGodRequestQueue
                                .getInstance(mContext).getRequestQueue();
                        requestQueue.add(request);
                    }
                });

//		// 分享到新浪微博
//		mShareWeibo.setOnClickListener(new android.view.View.OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				Utils.showProgressDialog(mContext);
//
//				ActionShareRequest.Builder builder = new ActionShareRequest.Builder()
//						.setShareType("weibo").setType(mPhotoItem.getType())
//						.setId(mPhotoItem.getPid())
//						.setListener(shareWeiboListener)
//						.setErrorListener(errorListener);
//
//				ActionShareRequest request = builder.build();
//				request.setTag(TAG);
//				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
//						PSGodApplication.getAppContext()).getRequestQueue();
//				requestQueue.add(request);
//			}
//		});
//
//		// 分享到qq
//		mShareQQ.setOnClickListener(new android.view.View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Utils.showProgressDialog(mContext);
//
//				ActionShareRequest.Builder builder = new ActionShareRequest.Builder()
//						.setShareType("qq_friend")
//						.setType(mPhotoItem.getType())
//						.setId(mPhotoItem.getPid())
//						.setListener(shareQQlistener)
//						.setErrorListener(errorListener);
//
//				ActionShareRequest request = builder.build();
//				request.setTag(TAG);
//				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
//						PSGodApplication.getAppContext()).getRequestQueue();
//				requestQueue.add(request);
//			}
//		});
//
//		// 分享到qzone
//		mShareQzone.setOnClickListener(new android.view.View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Utils.showProgressDialog(mContext);
//
//				ActionShareRequest.Builder builder = new ActionShareRequest.Builder()
//						.setShareType("qq_timeline")
//						.setType(mPhotoItem.getType())
//						.setId(mPhotoItem.getPid())
//						.setListener(shareQzoneListener)
//						.setErrorListener(errorListener);
//
//				ActionShareRequest request = builder.build();
//				request.setTag(TAG);
//				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
//						PSGodApplication.getAppContext()).getRequestQueue();
//				requestQueue.add(request);
//			}
//		});
//
//		// 分享到微信朋友圈
//		mShareWechatMoments
//				.setOnClickListener(new android.view.View.OnClickListener() {
//					@Override
//					public void onClick(View arg0) {
//						Utils.showProgressDialog(mContext);
//
//						ActionShareRequest.Builder builder = new ActionShareRequest.Builder()
//								.setShareType("wechat_timeline")
//								.setType(mPhotoItem.getType())
//								.setId(mPhotoItem.getPid())
//								.setListener(shareMomentsListener)
//								.setErrorListener(errorListener);
//
//						ActionShareRequest request = builder.build();
//						request.setTag(TAG);
//						RequestQueue requestQueue = PSGodRequestQueue
//								.getInstance(PSGodApplication.getAppContext())
//								.getRequestQueue();
//						requestQueue.add(request);
//					}
//				});
//
//		// 分享给微信好友
//		mShareWechatFriend
//				.setOnClickListener(new android.view.View.OnClickListener() {
//					@Override
//					public void onClick(View arg0) {
//						Utils.showProgressDialog(mContext);
//
//						ActionShareRequest.Builder builder = new ActionShareRequest.Builder()
//								.setShareType("wechat")
//								.setType(mPhotoItem.getType())
//								.setId(mPhotoItem.getPid())
//								.setListener(shareFriendsListener)
//								.setErrorListener(errorListener);
//
//						ActionShareRequest request = builder.build();
//						request.setTag(TAG);
//						RequestQueue requestQueue = PSGodRequestQueue
//								.getInstance(PSGodApplication.getAppContext())
//								.getRequestQueue();
//						requestQueue.add(request);
//					}
//				});

        mReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                (new ReportDialog(mContext, mPhotoItem)).show();
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

//	// qzone分享回调 qq空间分享图文
//	private Listener<JSONObject> shareQzoneListener = new Listener<JSONObject>() {
//		@Override
//		public void onResponse(JSONObject response) {
//			Utils.hideProgressDialog();
//
//			ShareSDK.initSDK(mContext);
//			try {
//				OnekeyShare oks = new OnekeyShare(){
//					@Override
//					public void onError(Platform arg0, int arg1,
//										Throwable arg2) {
//					}
//
//					@Override
//					public void onComplete(Platform arg0, int arg1,
//										   HashMap<String, Object> arg2) {
//						ShareMoreDialog.this.dismiss();
//					}
//
//					@Override
//					public void onCancel(Platform arg0, int arg1) {
//					}
//				};
//				oks.setPlatform(QZone.NAME);
//
//				oks.setTitle(response.getString("title"));
//				oks.setTitleUrl(response.getString("url"));
//				oks.setText(response.getString("desc"));
//				oks.setImageUrl(response.getString("image"));
//				// 设置发布分享的网站名称和网址
//				oks.setSite(Constants.OFFICAL_APP_NAME);
//				oks.setSiteUrl(Constants.OFFICAL_WEBSITE);
//
//				oks.show(mContext);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	};
//
//	// 微信好友分享请求回调
//	private Listener<JSONObject> shareFriendsListener = new Listener<JSONObject>() {
//		@Override
//		public void onResponse(JSONObject response) {
//			Utils.hideProgressDialog();
//
//			ShareSDK.initSDK(mContext);
//			Platform wechatFriends = ShareSDK
//					.getPlatform(mContext, Wechat.NAME);
//			wechatFriends
//					.setPlatformActionListener(new PlatformActionListener() {
//						@Override
//						public void onError(Platform arg0, int arg1,
//								Throwable arg2) {
//						}
//
//						@Override
//						public void onComplete(Platform arg0, int arg1,
//								HashMap<String, Object> arg2) {
//							ShareMoreDialog.this.dismiss();
//						}
//
//						@Override
//						public void onCancel(Platform arg0, int arg1) {
//						}
//					});
//
//			try {
//				if (response.getString("type").equals("image")) {
//					ShareParams sp = new ShareParams();
//
//					sp.setShareType(Platform.SHARE_IMAGE);
//					sp.setTitle(response.getString("title"));
//					sp.setText(response.getString("desc"));
//					sp.setImageUrl(response.getString("image"));
//					wechatFriends.share(sp);
//				}
//				if (response.getString("type").equals("url")) {
//					// 图文链接分享
//					ShareParams sp = new ShareParams();
//					sp.setShareType(Platform.SHARE_WEBPAGE);
//
//					sp.setTitle(response.getString("title"));
//					sp.setText(response.getString("desc"));
//					sp.setImageUrl(response.getString("image"));
//					sp.setUrl(response.getString("url"));
//					wechatFriends.share(sp);
//				}
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	};
//
//	// 微信朋友圈分享接口请求回调
//	private Listener<JSONObject> shareMomentsListener = new Listener<JSONObject>() {
//		@Override
//		public void onResponse(JSONObject response) {
//			Utils.hideProgressDialog();
//
//			ShareSDK.initSDK(mContext);
//			Platform wechat = ShareSDK
//					.getPlatform(mContext, WechatMoments.NAME);
//			wechat.setPlatformActionListener(new PlatformActionListener() {
//				@Override
//				public void onError(Platform arg0, int arg1, Throwable arg2) {
//				}
//
//				@Override
//				public void onComplete(Platform arg0, int arg1,
//						HashMap<String, Object> arg2) {
//					ShareMoreDialog.this.dismiss();
//				}
//
//				@Override
//				public void onCancel(Platform arg0, int arg1) {
//				}
//			});
//
//			try {
//				if (response.getString("type").equals("image")) {
//					ShareParams sp = new ShareParams();
//
//					sp.setShareType(Platform.SHARE_IMAGE);
//					sp.setTitle(response.getString("title"));
//					sp.setText(response.getString("desc"));
//					sp.setImageUrl(response.getString("image"));
//					wechat.share(sp);
//				}
//				if (response.getString("type").equals("url")) {
//					// 图文链接分享
//					ShareParams sp = new ShareParams();
//
//					sp.setShareType(Platform.SHARE_WEBPAGE);
//					sp.setTitle(response.getString("title"));
//					sp.setText(response.getString("desc"));
//					sp.setImageUrl(response.getString("image"));
//					sp.setUrl(response.getString("url"));
//					wechat.share(sp);
//				}
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	};
//
//	// qq分享接口请求回调 QQ图文分享
//	private Listener<JSONObject> shareQQlistener = new Listener<JSONObject>() {
//		@Override
//		public void onResponse(JSONObject response) {
//			Utils.hideProgressDialog();
//
//			ShareSDK.initSDK(mContext);
//			try {
//				OnekeyShare oks = new OnekeyShare(){
//					@Override
//					public void onError(Platform arg0, int arg1,
//										Throwable arg2) {
//					}
//
//					@Override
//					public void onComplete(Platform arg0, int arg1,
//										   HashMap<String, Object> arg2) {
//						ShareMoreDialog.this.dismiss();
//					}
//
//					@Override
//					public void onCancel(Platform arg0, int arg1) {
//					}
//				};
//				oks.setPlatform(QQ.NAME);
//
//				oks.setTitle(response.getString("title"));
//				oks.setTitleUrl(response.getString("url"));
//				oks.setText(response.getString("desc"));
//				oks.setImageUrl(response.getString("image"));
//
//				oks.show(mContext);
//			} catch (Exception e) {
//				// TODO: handle exception
//			}
//		}
//	};
//
//	// 微博分享接口请求回调 新浪微博只支持图文／文字
//	private Listener<JSONObject> shareWeiboListener = new Listener<JSONObject>() {
//		@Override
//		public void onResponse(JSONObject response) {
//			Utils.hideProgressDialog();
//
//			ShareSDK.initSDK(mContext);
//			try {
//				OnekeyShare oks = new OnekeyShare(){
//					@Override
//					public void onError(Platform arg0, int arg1,
//										Throwable arg2) {
//					}
//
//					@Override
//					public void onComplete(Platform arg0, int arg1,
//										   HashMap<String, Object> arg2) {
//						ShareMoreDialog.this.dismiss();
//					}
//
//					@Override
//					public void onCancel(Platform arg0, int arg1) {
//					}
//				};
//
//				oks.setPlatform(SinaWeibo.NAME);
//				oks.disableSSOWhenAuthorize();
//				oks.setSilent(false);
//
//				oks.setText(response.getString("desc"));
//				oks.setImageUrl(response.getString("image"));
//				oks.show(mContext);
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//		}
//	};

    public void setPhotoItem(PhotoItem photoItem) {
        mPhotoItem = photoItem;

        // 初始化与该photoitem相关数据
        updateView();
    }

    // 更新收藏按钮
    public void updateView() {
        isCollected = mPhotoItem.isCollected();
        Resources res = mContext.getResources();
        if (isCollected) {
            Drawable img = res.getDrawable(R.drawable.fav);
            img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
            mCollectionBtn.setCompoundDrawables(null, img, null, null);
            mCollectionBtn.setText("已收藏");
        } else {
            Drawable img = res.getDrawable(R.drawable.fav_nor);
            img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
            mCollectionBtn.setCompoundDrawables(null, img, null, null);
            mCollectionBtn.setText("收藏");
        }
    }

    @Override
    public void show() {
        super.show();
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setWindowAnimations(R.style.popwindow_anim_style);
    }
}
