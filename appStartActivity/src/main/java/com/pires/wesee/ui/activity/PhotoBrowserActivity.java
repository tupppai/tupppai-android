package com.pires.wesee.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.pires.wesee.PsGodImageLoader;
import com.pires.wesee.Constants;
import com.pires.wesee.R;
import com.pires.wesee.ThreadManager;
import com.pires.wesee.WeakReferenceHandler;
import com.pires.wesee.eventbus.RefreshEvent;
import com.pires.wesee.ui.fragment.HomePageDynamicFragment;
import com.pires.wesee.ui.fragment.MovieFragment;
import com.pires.wesee.ui.widget.dialog.PSDialog;

import de.greenrobot.event.EventBus;
import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;

/**
 * 图片查看详情页
 */

public class PhotoBrowserActivity extends PSGodBaseActivity implements Handler.Callback{
	private static final String TAG = PhotoBrowserActivity.class
			.getSimpleName();
	private ImageView mImageView;
	private PhotoViewAttacher mAttacher;
	private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS;
	private Button mDownLoadBtn;

	private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);

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
			System.out.println("mImageUrl  " + mImageUrl + "\n");
			System.out.println("mAskId  " + mAskId + "\n");
			Toast.makeText(this, TAG + ".onCreate(): mReigsterData is null",
					Toast.LENGTH_LONG).show();
			finish();
		}

		mDownLoadBtn = (Button) findViewById(R.id.widget_photo_broswer_upload_btn);
		mImageView = (ImageView) findViewById(R.id.widget_photo_broswer_imageview);

		ThreadManager.executeOnNetWorkThread(new Runnable() {
			@Override
			public void run() {
				PsGodImageLoader imageLoader = PsGodImageLoader.getInstance();
				Bitmap bitmap = imageLoader.loadImageSync(mImageUrl);
				Message message = mHandler.obtainMessage();
				message.obj = bitmap;
				mHandler.sendMessage(message);
			}
		});


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

	@Override
	public boolean handleMessage(Message msg) {
		Bitmap bitmap = (Bitmap) msg.obj;
		mImageView.setImageBitmap(bitmap);
		mAttacher = new PhotoViewAttacher(mImageView);

		// 单次触摸图片 关闭查看页面
		mAttacher.setOnPhotoTapListener(new OnPhotoTapListener() {
			@Override
			public void onPhotoTap(View arg0, float arg1, float arg2) {
				EventBus.getDefault().post(new RefreshEvent(HomePageDynamicFragment.class.getName()));
				finish();
			}
		});
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
				EventBus.getDefault().post(new RefreshEvent(HomePageDynamicFragment.class.getName()));
				finish();
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}
}
