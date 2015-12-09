package com.psgod.ui.widget.dialog;

/*
 * 自定义转菊花对话框
 * @author brandwang
 */
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;

import com.psgod.R;

public class CustomProgressingDialog extends ProgressDialog {
	private AnimationDrawable mAnimation;
	private Context mContext;
	private ImageView mImageView;
	// 动画资源id
	private int mResid;

	public CustomProgressingDialog(Context context) {
		super(context);

		this.mContext = context;
		this.mResid = R.anim.widget_custom_progress;
		setCanceledOnTouchOutside(false);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initView();
		initData();
	}

	private void initData() {
		mImageView.setBackgroundResource(mResid);

		// 通过ImageView对象拿到显示的AnimationDrawable
		mAnimation = (AnimationDrawable) mImageView.getBackground();
		// 为了防止在onCreate方法中只显示第一帧的解决方案之一
		mImageView.post(new Runnable() {
			@Override
			public void run() {
				mAnimation.start();
			}
		});
	}

	private void initView() {
		setContentView(R.layout.widget_custom_progress);
		mImageView = (ImageView) findViewById(R.id.loadingIv);
	}

//	@Override
//	public void dismiss() {
////		if(this.isShowing()) {
//			super.dismiss();
////		}
//	}
}
