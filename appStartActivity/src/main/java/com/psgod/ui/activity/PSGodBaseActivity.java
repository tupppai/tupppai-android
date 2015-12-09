package com.psgod.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.psgod.Logger;
import com.psgod.PSGodToast;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.WeakReferenceHandler;
import com.psgod.network.NetworkUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.Set;
import java.util.TreeSet;

/**
 * 应用的基础Activity，实现一些公共的逻辑 所有的Activity都必须继承PSGodBaseActivity
 *
 * @author rayalyuan
 *
 */
public abstract class PSGodBaseActivity extends FragmentActivity implements
		Handler.Callback {
	private static final String TAG = PSGodBaseActivity.class.getSimpleName();

	protected static final int MSG_SHOW_TOAST = 0x6600;
	protected static final int MSG_REMOVE_TOAST = 0x6601;

	protected Set<PSGodToast> mToasts = new TreeSet<PSGodToast>();
	protected TextView mToastView;
	protected boolean mIsShowingToast = false;
	protected PSGodToast mCurrentToast;

	protected WeakReferenceHandler mBaseHandler = new WeakReferenceHandler(this);

	// protected static int countOfActivity = 0;

	// public static final String CONNECTIVITY_CHANGE_ACTION =
	// "android.net.conn.CONNECTIVITY_CHANGE";
	// private BroadcastReceiver mReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 判断当前SDK版本号，如果是4.4以上，则加入沉浸式状态栏
		if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
			getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}

		// ++countOfActivity;

		// //友盟统计应用启动数据
		// PushAgent.getInstance(this).onAppStart();

		// if (countOfActivity == 1) {
		// IntentFilter filter = new IntentFilter();
		// filter.addAction(CONNECTIVITY_CHANGE_ACTION);
		// mReceiver = new NetworkStateReceiver();
		// this.getApplication().registerReceiver(mReceiver, filter);
		// }
	}

	//开启友盟统计
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	};

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		mToasts.clear();
		mIsShowingToast = false;
		if (mToastView != null) {
			mToastView.setVisibility(View.GONE);
		}
	}

	private boolean isJump = false;
	@Override
	public void finish() {
		if (getIntent().getBooleanExtra("isSingle",false)){
			Intent intent = new Intent(this,MainActivity.class);
			startActivity(intent);
			isJump = true;
		}
		super.finish();
	}

	@Override
	public void onDestroy() {
		if (getIntent().getBooleanExtra("isSingle",false) && !isJump){
			Intent intent = new Intent(this,MainActivity.class);
			startActivity(intent);
		}
		super.onDestroy();
		// --countOfActivity;
		// if (countOfActivity == 0) {
		// this.getApplication().unregisterReceiver(mReceiver);
		// }
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.remove("android:support:fragments");
	}

	protected void showToast(PSGodToast toast) {
		if (mToastView == null) {
			Resources res = getResources();
			int topMargin = 0;
			// 4.4以上版本，加了状态栏，弹出Tosat时要margintop要考虑进去
			if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
				topMargin = res.getDimensionPixelSize(R.dimen.actionbar_height)
						+ Utils.getStatusBarHeight(getApplicationContext());
			} else {
				topMargin = res.getDimensionPixelSize(R.dimen.actionbar_height);
			}
			int height = res.getDimensionPixelSize(R.dimen.toast_height);
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, height);
			params.setMargins(0, topMargin, 0, 0);
			mToastView = new TextView(this);
			mToastView.setGravity(Gravity.CENTER);
			mToastView.setBackgroundColor(android.graphics.Color
					.parseColor("#FE8282"));
			mToastView.setText(toast.getContent());
			mToastView.setVisibility(View.GONE);
			mToastView.setTextColor(android.graphics.Color
					.parseColor("#FFFFFF"));
			// mToastView.setTextSize(); TODO
			addContentView(mToastView, params);
		}

		mToasts.add(toast);
		mBaseHandler.obtainMessage(MSG_SHOW_TOAST).sendToTarget();
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_SHOW_TOAST:
			if (mToasts.isEmpty()) {
				break;
			}

			boolean needUpdate = false;
			PSGodToast toast = mToasts.iterator().next();
			if (mIsShowingToast) {
				if (mCurrentToast.getPriority() < toast.getPriority()) {
					long duration = mCurrentToast.getDuration();
					mCurrentToast = toast;
					needUpdate = true;

					if (duration != PSGodToast.DURATION_FOREVER) {
						mBaseHandler.removeMessages(MSG_REMOVE_TOAST);
					}
				}
			} else {
				mIsShowingToast = true;
				mCurrentToast = toast;
				needUpdate = true;
			}

			if (needUpdate) {
				mToastView.setText(mCurrentToast.getContent());
				mToastView.setVisibility(View.VISIBLE);

				final AlphaAnimation appearAnimation = new AlphaAnimation(0, 1);
				appearAnimation.setDuration(500);
				mToastView.setAnimation(appearAnimation);
				appearAnimation.startNow();

				long duration = mCurrentToast.getDuration();
				if (duration != PSGodToast.DURATION_FOREVER) {
					mBaseHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							mBaseHandler.obtainMessage(MSG_REMOVE_TOAST)
									.sendToTarget();
						}
					}, duration);
				}
			}
			break;
		case MSG_REMOVE_TOAST:
			mToasts.remove(mCurrentToast);
			mIsShowingToast = false;
			if (mToasts.isEmpty()) {
				mToastView.clearAnimation();
				final AlphaAnimation disappearAnimation = new AlphaAnimation(1,
						0);
				disappearAnimation.setDuration(500);
				mToastView.setAnimation(disappearAnimation);
				disappearAnimation.startNow();
				disappearAnimation
						.setAnimationListener(new AnimationListener() {

							@Override
							public void onAnimationStart(Animation animation) {

							}

							@Override
							public void onAnimationRepeat(Animation animation) {

							}

							@Override
							public void onAnimationEnd(Animation animation) {
								mToastView.setVisibility(View.GONE);
							}
						});

			} else {
				mBaseHandler.obtainMessage(MSG_SHOW_TOAST).sendToTarget();
			}
			break;
		default:
			break;
		}
		return true;
	}

	protected void onNetworkStateChanged(int networkType) {
		Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
				"onNetworkStateChanged(): networkType=" + networkType);
	}

	protected class NetworkStateReceiver extends BroadcastReceiver {
		int preNetworkType;

		public NetworkStateReceiver() {
			preNetworkType = NetworkUtil.getNetworkType();
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			int networkType = NetworkUtil.getNetworkType();
			if (preNetworkType != networkType) {
				preNetworkType = networkType;
				onNetworkStateChanged(networkType);
			}
		}
	}
}
