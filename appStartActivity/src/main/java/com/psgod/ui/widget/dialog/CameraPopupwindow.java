package com.psgod.ui.widget.dialog;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.psgod.BitmapUtils;
import com.psgod.CameraBackAnimator;
import com.psgod.R;
import com.psgod.WeakReferenceHandler;
import com.psgod.ui.activity.MultiImageSelectActivity;
import com.psgod.ui.activity.UploadSelectReplyListActivity;

/**
 * 选择上传求P 上传作品 PopupWindow (新版v2.0)
 * 
 * @author ZouMengyuan
 *
 * 暂时废置
 */
public class CameraPopupwindow extends PopupWindow implements OnClickListener {
	private static final String TAG = CameraPopupwindow.class.getSimpleName();

	private Activity mActivity;
	private int statusBarHeight;
	private String mChannelId;

	// 屏幕截图
	private Bitmap mScreenShotBitmap = null;
	// 毛玻璃处理后图
	private Bitmap mOverlayBitmap = null;
	// 弹窗背景
	private RelativeLayout mBgLayout = null;

	private WeakReferenceHandler mHandler;

	private int mode = 0;

	public static final int MODE_ALL = 0;
	public static final int MODE_ASK = 1;
	public static final int MODE_REPLY = 2;

	public void setMode(int mode) {
		this.mode = mode;
	}

	public CameraPopupwindow(Activity activity) {
		mActivity = activity;
		mHandler = new WeakReferenceHandler((Callback) mActivity);

		// 判断当前SDK版本号，如果是4.4以上，则加入沉浸式状态栏
		if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
			mActivity.getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			mActivity.getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}

		setHeight(LayoutParams.MATCH_PARENT);
		setWidth(LayoutParams.MATCH_PARENT);

	}

	public CameraPopupwindow(Activity activity,String channelId) {
		this(activity);
		this.mChannelId = channelId;
	}

	// 截屏并获得毛玻璃化的图片
	private void getBlurPic() {
		mOverlayBitmap = null;

		View view = mActivity.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache(true);
		mScreenShotBitmap = view.getDrawingCache();

		// 将截图亮度调低
		Bitmap mFinalBitmap = Bitmap.createBitmap(mScreenShotBitmap.getWidth(), mScreenShotBitmap.getHeight(), Bitmap.Config.ARGB_8888);
		ColorMatrix cMatrix = new ColorMatrix();
		cMatrix.set(new float[] {1,0,0,0,-150,0,1,0,0,-150,0,0,1,0,-150,0,0,0,1,0});
		Paint paint = new Paint();
		paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));
		Canvas canvas = new Canvas(mFinalBitmap);
		canvas.drawBitmap(mScreenShotBitmap,0,0,paint);

		BitmapUtils.setBlurBitmap(mFinalBitmap,this.getContentView(),mFinalBitmap.toString());
		//		return mOverlayBitmap;
	}

	// 显示弹出框
	public void showCameraPopupwindow(View anchor) {
		final RelativeLayout layout = (RelativeLayout) LayoutInflater.from(
				mActivity).inflate(R.layout.popupwindow_select_ask_reply, null);
		setContentView(layout);

		showAnimation(layout);
		getBlurPic();
		setOutsideTouchable(true);
		setFocusable(true);
		showAtLocation(anchor, Gravity.BOTTOM, 0, statusBarHeight);

		mBgLayout = (RelativeLayout) layout.findViewById(R.id.ask_reply_layout);
		mBgLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isShowing()) {
					closeAnimation(layout);
				}
			}
		});
	}

	// popupwindow显示动画
	private void showAnimation(ViewGroup layout) {
		// 循环弹出效果
		for (int i = 0; i < layout.getChildCount(); i++) {
			final View child = layout.getChildAt(i);

			if (child.getId() == R.id.btn_cancel) {
				continue;
			}

			child.setOnClickListener(this);
			child.setVisibility(View.INVISIBLE);

			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					child.setVisibility(View.VISIBLE);

					ValueAnimator fadeAnim = ObjectAnimator.ofFloat(child,
							"translationY", 600, 0);
					fadeAnim.setDuration(300);
					CameraBackAnimator kickAnimator = new CameraBackAnimator();
					kickAnimator.setDuration(150);
					fadeAnim.setEvaluator(kickAnimator);
					fadeAnim.start();
				}
			}, i * 50);
		}
	}

	// popupwindow收起动画
	private void closeAnimation(ViewGroup layout) {
		for (int i = 0; i < layout.getChildCount(); i++) {
			final View child = layout.getChildAt(i);

			if (child.getId() == R.id.btn_cancel) {
				continue;
			}

			child.setOnClickListener(this);


			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					child.setVisibility(View.VISIBLE);

					ValueAnimator fadeAnim = ObjectAnimator.ofFloat(child,
							"translationY", 0, 600);
					fadeAnim.setDuration(200);
					CameraBackAnimator kickAnimator = new CameraBackAnimator();
					kickAnimator.setDuration(100);
					fadeAnim.setEvaluator(kickAnimator);
					fadeAnim.start();
					fadeAnim.addListener(new AnimatorListener() {
						@Override
						public void onAnimationStart(Animator animation) {
							// TODO Auto-generated method stub
						}

						@Override
						public void onAnimationRepeat(Animator animation) {
							// TODO Auto-generated method stub
						}

						@Override
						public void onAnimationEnd(Animator animation) {
							child.setVisibility(View.INVISIBLE);
						}

						@Override
						public void onAnimationCancel(Animator animation) {
							// TODO Auto-generated method stub
						}
					});
				}
			}, (layout.getChildCount() - i - 1) * 30);

			if (child.getId() == R.id.ask_view) {
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						dismiss();
					}
				}, (layout.getChildCount() - i) * 30 + 100);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ask_view:
			// 选择点击跳转
			Intent intentM = new Intent(mActivity,
					MultiImageSelectActivity.class);
			Bundle bundleM = new Bundle();
			bundleM.putString("SelectType",
					MultiImageSelectActivity.TYPE_ASK_SELECT);
			bundleM.putString("channel_id",mChannelId);
			intentM.putExtras(bundleM);
			mActivity.startActivity(intentM);

			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					dismiss();
				}
			}, 1000);
			break;

		case R.id.reply_view:
			// 选择点击跳转
			Intent intent = new Intent(mActivity,
					UploadSelectReplyListActivity.class);
			intent.putExtra("channel_id" , mChannelId);
			// bundle.putString("SelectType",
			// MultiImageSelectActivity.TYPE_REPLY_SELECT);
			// intent.putExtras(bundle);
			mActivity.startActivity(intent);

			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					dismiss();
				}
			}, 1000);
			break;

		default:
			break;
		}
	}
}
