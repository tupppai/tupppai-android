package com.psgod.ui.activity;

import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.psgod.BitmapUtils;
import com.psgod.Constants;
import com.psgod.ImageIOManager;
import com.psgod.R;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.UploadImageRequest;
import com.psgod.network.request.UploadImageRequest.ImageUploadResult;
import com.psgod.ui.view.CropImageView;
import com.psgod.ui.widget.ActionBar;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

/*
 * 用户头像选择页面
 * @author brandwang 
 */
public class SetAvatarActivity extends PSGodBaseActivity {
	private static final String TAG = SetAvatarActivity.class.getSimpleName();

	private CropImageView mImageView;
	private Bitmap mImage; // 原图
	private Bitmap mCropImage; // 裁剪过的图
	private String mImagePath;
	private ActionBar mActionBar;
	private CustomProgressingDialog mProgressDialog;

	private Listener<ImageUploadResult> listener = new Listener<ImageUploadResult>() {
		@Override
		public void onResponse(ImageUploadResult response) {
			// 把图片保存到本地
			String path = ImageIOManager.getInstance().saveAvatar("temp",
					mCropImage);

			Intent intent = getIntent();
			intent.putExtra(Constants.IntentKey.PHOTO_PATH, path);
			intent.putExtra("imageId", response.id);
			intent.putExtra("imagePath", response.url);

			mProgressDialog.dismiss();
			setResult(RESULT_OK, intent);
			finish();
		}
	};

	private PSGodErrorListener errorListener = new PSGodErrorListener(
			UploadImageRequest.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {
			mProgressDialog.dismiss();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload_avatar);

		// 初始化视图
		mActionBar = (ActionBar) this.findViewById(R.id.actionbar);
		mImageView = (CropImageView) this
				.findViewById(R.id.activity_upload_avatar_imageview);
		mActionBar = (ActionBar) this.findViewById(R.id.actionbar);
		initListeners();

		mImagePath = getIntent().getStringExtra(Constants.IntentKey.PHOTO_PATH);
		if (TextUtils.isEmpty(mImagePath)) {
			Toast.makeText(this, TAG + ".onCreate(): image path is null",
					Toast.LENGTH_LONG).show();
			finish();
		}

		setOrientation();

		mImageView.setImageBitmap(mImage);
		mImageView.setRatio(1.0f);
		mImageView.setIsCropable(true);
	}

	private void setOrientation() { // 拍照旋转
		int degree = 0;
		try {
			// 从指定路径下读取图片，并获取其EXIF信息
			ExifInterface exifInterface = new ExifInterface(mImagePath);
			// 获取图片的旋转信息
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		Matrix matrix = new Matrix();
		matrix.postRotate(degree);
		try {
			mImage = BitmapUtils.decodeBitmap(mImagePath);
			// 将原始图片按照旋转矩阵进行旋转，并得到新的图片
			mImage = Bitmap.createBitmap(mImage, 0, 0, mImage.getWidth(),
					mImage.getHeight(), matrix, true);
		} catch (OutOfMemoryError e) {
		} catch (Exception e){}
	}

	@Override
	public void onStop() {
		super.onStop();
		RequestQueue reqeustQueue = PSGodRequestQueue.getInstance(
				getApplicationContext()).getRequestQueue();
		reqeustQueue.cancelAll(TAG);
	}

	private void initListeners() {
		// 点击actionbar下一步上传头像照片
		mActionBar.setRightBtnOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				mCropImage = mImageView.getCropImage();

				// 显示等待对话框
				if (mProgressDialog == null) {
					mProgressDialog = new CustomProgressingDialog(
							SetAvatarActivity.this);
					mProgressDialog.setMessage("上传头像中"); // TODO
				}

				if (!mProgressDialog.isShowing()) {
					mProgressDialog.show();
				}

				UploadImageRequest.Builder builder = new UploadImageRequest.Builder()
						.setBitmap(mCropImage).setListener(listener)
						.setErrorListener(errorListener);
				UploadImageRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue reqeustQueue = PSGodRequestQueue.getInstance(
						getApplicationContext()).getRequestQueue();
				reqeustQueue.add(request);
			}
		});
	}
}
