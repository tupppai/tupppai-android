package com.psgod.ui.activity;

import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.ui.widget.dialog.PSDialog;

public class PhotoBrowserActivity extends PSGodBaseActivity {
	private static final String TAG = PhotoBrowserActivity.class
			.getSimpleName();
	private ImageView mImageView;
	private PhotoViewAttacher mAttacher;
	private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS;
	private Button mDownLoadBtn;

	private Long mAskId;
	private Long mPhotoId;
	private String mType;

	private String mImageUrl;
	private PSDialog mPsDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.widget_photo_browser);

		Intent intent = getIntent();
		mImageUrl = intent.getStringExtra(Constants.IntentKey.PHOTO_PATH);
		mAskId = intent.getLongExtra(Constants.IntentKey.ASK_ID, -1);
		mPhotoId = intent.getLongExtra(Constants.IntentKey.PHOTO_ITEM_ID, -1);
		mType = intent.getStringExtra(Constants.IntentKey.PHOTO_ITEM_TYPE);

		if (TextUtils.isEmpty(mImageUrl) || mAskId == -1) {
			Toast.makeText(this, TAG + ".onCreate(): mReigsterData is null",
					Toast.LENGTH_LONG).show();
			finish();
		}

		mDownLoadBtn = (Button) findViewById(R.id.widget_photo_broswer_upload_btn);
		mImageView = (ImageView) findViewById(R.id.widget_photo_broswer_imageview);
		mAttacher = new PhotoViewAttacher(mImageView);

		// 单次触摸图片 关闭查看页面
		mAttacher.setOnPhotoTapListener(new OnPhotoTapListener() {
			@Override
			public void onPhotoTap(View arg0, float arg1, float arg2) {
				finish();
			}
		});

		ImageLoader imageLoader = ImageLoader.getInstance();
		Bitmap bitmap = imageLoader.loadImageSync(mImageUrl);
		mImageView.setImageBitmap(bitmap);

		// 点击下载按钮 唤起弹框
		mDownLoadBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mPsDialog == null) {
					mPsDialog = new PSDialog(PhotoBrowserActivity.this);
				}

				mPsDialog.setIsFromPhotoBroswer();
				mPsDialog.setPhotoInfo(mType, mPhotoId, mAskId);

				if (mPsDialog.isShowing()) {
					mPsDialog.dismiss();
				} else {
					mPsDialog.show();
				}
			}
		});
	}
}
