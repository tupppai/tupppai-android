package com.psgod.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;

import com.psgod.Constants;
import com.psgod.R;
import com.psgod.ui.activity.MyInProgressActivity;

/**
 * 相机按钮对应的底部对话框 功能： // 1. 拍照 // 2. 从手机相册选择 // 3. 上传作品 // 4. 取消 1.求助上传 2.作品上传
 * 
 * @author Rayal
 * 
 */
public class CameraDialog extends Dialog {
	public static final int REQUEST_TAKE_PHOTO = 0x770;
	public static final int REQUEST_GET_IMAGE = 0x771;

	private Context mContext;
	// private Button mTakePhotoBtn;
	private Button mChoosePhotoBtn;
	private Button mUploadPhotoBtn;
	private Button mCancelBtn;

	public CameraDialog(Context context) {
		super(context, R.style.ActionSheetDialog);
		setContentView(R.layout.dialog_camera);
		getWindow().getAttributes().width = Constants.WIDTH_OF_SCREEN;
		setCanceledOnTouchOutside(true);

		// 初始化组件
		mContext = context;
		mChoosePhotoBtn = (Button) findViewById(R.id.dialog_camera_choose_photo);
		mUploadPhotoBtn = (Button) findViewById(R.id.dialog_camera_upload_photo);
		mCancelBtn = (Button) findViewById(R.id.dialog_camera_cancel);
		initListeners();
	}

	private void initListeners() {
		// 设置上传作品按钮的动作监听器
		mUploadPhotoBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
				Intent intent = new Intent(mContext, MyInProgressActivity.class);
				mContext.startActivity(intent);
			}
		});

		// 设置取消按钮的动作监听器
		mCancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
			}
		});
	}

	@Override
	public void show() {
		super.show();
		getWindow().setGravity(Gravity.BOTTOM);
		getWindow().setWindowAnimations(R.style.popwindow_anim_style);
	}
}