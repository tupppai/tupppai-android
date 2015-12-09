package com.psgod.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.LoginUser;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.QQLoginRequest;
import com.psgod.network.request.QQLoginRequest.QQLoginWrapper;
import com.psgod.network.request.WechatUserInfoRequest;
import com.psgod.network.request.WechatUserInfoRequest.WechatUserWrapper;
import com.psgod.ui.activity.LoginActivity;
import com.psgod.ui.activity.MainActivity;
import com.psgod.ui.activity.SetInfoActivity;

import org.json.JSONObject;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;

public class OtherRegisterDialog extends Dialog {

	private Context mContext;

	private TextView mRegisterQQ;
	private TextView mRegisterWeixin;
	private TextView mRegisterPhone;
	private ImageView mCancel;

	// Wechat code
	private String code;
	// wechat avatar
	private String mAvatar;
	// wechat nick
	private String mNickName;
	// wechat gender
	private String mGender;

	private String mThirdAuthId = "";
	private String mThirdAuthName = "";
	private String mThirdAuthGender = "";
	private String mThirdAuthAvatar = "";

	public OtherRegisterDialog(Context context) {
		super(context, R.style.ActionSheetDialog);
		setContentView(R.layout.dialog_other_register);

		mContext = context;

		getWindow().getAttributes().width = Constants.WIDTH_OF_SCREEN;
		setCanceledOnTouchOutside(true);

		mRegisterQQ = (TextView) this.findViewById(R.id.qq_register);
		mRegisterWeixin = (TextView) this.findViewById(R.id.wexin_register);
		mRegisterPhone = (TextView) this.findViewById(R.id.phone_register);
		mCancel = (ImageView) this
				.findViewById(R.id.dialog_other_register_cancel);

		initListeners();
	}

	private void initListeners() {
		mCancel.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		// qq注册
		mRegisterQQ.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.showProgressDialog(mContext);

				// QQ登录需要通过qzone授权
				final Platform qq = ShareSDK.getPlatform(QZone.NAME);
				qq.SSOSetting(false);
				if (qq.isValid()) {
					qq.removeAccount();
					ShareSDK.removeCookieOnAuthorize(true);
				}
				qq.setPlatformActionListener(new PlatformActionListener() {
					@Override
					public void onError(Platform arg0, int arg1, Throwable arg2) {
						Utils.hideProgressDialog();
					}

					@Override
					public void onComplete(Platform arg0, int arg1,
							HashMap<String, Object> res) {
						mThirdAuthId = qq.getDb().getUserId();
						mThirdAuthName = res.get("nickname").toString();
						mThirdAuthAvatar = res.get("figureurl_qq_2").toString();

						if (!TextUtils.isEmpty(mThirdAuthId)) {
							QQLoginRequest.Builder builder = new QQLoginRequest.Builder()
									.setCode(mThirdAuthId)
									.setListener(qqAuthListener)
									.setErrorListener(errorListener);

							QQLoginRequest request = builder.build();
							RequestQueue requestQueue = PSGodRequestQueue
									.getInstance(mContext).getRequestQueue();
							requestQueue.add(request);
						}
					}

					@Override
					public void onCancel(Platform arg0, int arg1) {
						Utils.hideProgressDialog();
					}
				});
				qq.showUser(null);
			}
		});

		// 手机号码注册
		mRegisterPhone
				.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(mContext,
								SetInfoActivity.class);
						mContext.startActivity(intent);
						dismiss();
					}
				});

		// 微信注册
		mRegisterWeixin
				.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						com.psgod.Utils.showProgressDialog(mContext);

						Platform weixin = ShareSDK.getPlatform(Wechat.NAME);
						if (weixin.isValid()) {
							weixin.removeAccount();
							ShareSDK.removeCookieOnAuthorize(true);
						}
						weixin.setPlatformActionListener(new PlatformActionListener() {
							@Override
							public void onError(Platform arg0, int arg1,
									Throwable arg2) {
								com.psgod.Utils.hideProgressDialog();
							}

							@Override
							public void onComplete(Platform platform,
									int action, HashMap<String, Object> res) {
								code = res.get("openid").toString();
								mAvatar = res.get("headimgurl").toString();
								mGender = res.get("sex").toString();
								mNickName = res.get("nickname").toString();

								// 验证code是否被注册
								if (!TextUtils.isEmpty(code)) {
									WechatUserInfoRequest.Builder builder = new WechatUserInfoRequest.Builder()
											.setCode(code)
											.setListener(wechatAuthListener)
											.setErrorListener(errorListener);

									WechatUserInfoRequest request = builder
											.build();
									RequestQueue requestQueue = PSGodRequestQueue
											.getInstance(getContext())
											.getRequestQueue();
									requestQueue.add(request);
								}
							}

							@Override
							public void onCancel(Platform arg0, int arg1) {
								com.psgod.Utils.hideProgressDialog();
							}
						});
						weixin.SSOSetting(true);
						weixin.showUser(null);
					}
				});
	}

	private PSGodErrorListener errorListener = new PSGodErrorListener() {
		@Override
		public void handleError(VolleyError error) {
			com.psgod.Utils.hideProgressDialog();
		}
	};

	// qq授权回调
	private Listener<QQLoginWrapper> qqAuthListener = new Listener<QQLoginWrapper>() {
		@Override
		public void onResponse(QQLoginWrapper response) {
			com.psgod.Utils.hideProgressDialog();

			try {
				int isRegistered = response.isRegistered;
				// 已注册
				if (isRegistered == 1) {
					JSONObject userInfoData = response.UserObject;
					LoginUser.getInstance().initFromJSONObject(userInfoData);

					Bundle extras = new Bundle();
					extras.putInt(Constants.IntentKey.ACTIVITY_JUMP_FROM,
							LoginActivity.JUMP_FROM_LOGIN);

					// 关闭之前所有页面
					MainActivity.startNewActivityAndFinishAllBefore(
							mContext, MainActivity.class.getName(), extras);
				}
				// 未注册
				if (isRegistered == 0) {
					Intent intent = new Intent(mContext, SetInfoActivity.class);

					intent.putExtra(
							Constants.ThirdAuthInfo.THIRD_AUTH_PLATFORM, "qq");
					intent.putExtra(Constants.ThirdAuthInfo.USER_OPENID,
							mThirdAuthId);
					intent.putExtra(Constants.ThirdAuthInfo.USER_AVATAR,
							mThirdAuthAvatar);
					intent.putExtra(Constants.ThirdAuthInfo.USER_NICKNAME,
							mThirdAuthName);

					mContext.startActivity(intent);
				}
			} catch (Exception e) {
			}
		}
	};

	// 微信授权回调
	private Listener<WechatUserWrapper> wechatAuthListener = new Listener<WechatUserWrapper>() {
		@Override
		public void onResponse(WechatUserWrapper response) {
			com.psgod.Utils.hideProgressDialog();

			try {
				int isRegistered = response.isRegistered;
				// 已注册
				if (isRegistered == 1) {
					JSONObject userInfoData = response.UserObject;
					LoginUser.getInstance().initFromJSONObject(userInfoData);

					Bundle extras = new Bundle();
					extras.putInt(Constants.IntentKey.ACTIVITY_JUMP_FROM,
							LoginActivity.JUMP_FROM_LOGIN);

					// 关闭之前所有页面
					MainActivity.startNewActivityAndFinishAllBefore(
							mContext, MainActivity.class.getName(), extras);
				}
				// 未注册
				if (isRegistered == 0) {
					Intent intent = new Intent(mContext, SetInfoActivity.class);

					intent.putExtra(
							Constants.ThirdAuthInfo.THIRD_AUTH_PLATFORM,
							"weixin");
					intent.putExtra(Constants.ThirdAuthInfo.USER_OPENID, code);
					intent.putExtra(Constants.ThirdAuthInfo.USER_AVATAR,
							mAvatar);
					intent.putExtra(Constants.ThirdAuthInfo.USER_GENDER,
							mGender);
					intent.putExtra(Constants.ThirdAuthInfo.USER_NICKNAME,
							mNickName);

					mContext.startActivity(intent);
				}
			} catch (Exception e) {
			}
		}
	};

	@Override
	public void show() {
		super.show();
		getWindow().setGravity(Gravity.BOTTOM);
		getWindow().setWindowAnimations(R.style.popwindow_anim_style);
	}
}
