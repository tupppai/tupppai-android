package com.psgod.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.psgod.Constants;
import com.psgod.ImageIOManager;
import com.psgod.R;
import com.psgod.ThreadManager;
import com.psgod.WeakReferenceHandler;
import com.psgod.eventbus.MyPageRefreshEvent;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.PhotoRequest;
import com.psgod.network.request.PhotoRequest.ImageInfo;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * 点击PS按钮弹出的底部对话框 功能： 1. 下载图片帮P 2. 添加至进行中 3. 取消
 *
 * @author Rayal
 *
 */
public class PSDialog extends Dialog implements Handler.Callback {
	private static final String TAG = PSDialog.class.getSimpleName();
	// 求P
	public static final byte TYPE_ASK = 1;
	// 回复
	public static final byte TYPE_REPLY = 2;

	private static final int MSG_SUCCESSFUL = 0x4400;
	private static final int MSG_FAILED = 0x4401;
	public static final int MSG_RECORD_SUCCESS = 0x4402;
	private static final int MSG_RECORD_FAILED = 0x4403;

	private Context mContext;
	private PhotoItem mPhotoItem;
	private Button mDownloadBtn;
	private Button mUploadBtn;
	private Button mCancelBtn;
	private CustomProgressingDialog mProgressDialog;

	// 是否在照片浏览器中调起
	private boolean isFromPhotoBroswer = false;
	// 根据type+id进行下载
	private Long mAskId;
	private Long mPhotoId;
	private String mType;
	private long category_id = -1;

	private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);
	private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS;

	public PSDialog(Context context) {
		super(context, R.style.ActionSheetDialog);
		setContentView(R.layout.dialog_ps_old);
		mContext = context;

		getWindow().getAttributes().width = Constants.WIDTH_OF_SCREEN;
		setCanceledOnTouchOutside(true);

		// 初始化组件
		mDownloadBtn = (Button) findViewById(R.id.dialog_ps_download);
		mUploadBtn = (Button) findViewById(R.id.dialog_ps_upload);
		mCancelBtn = (Button) findViewById(R.id.dialog_ps_cancel);

		initListeners();

	}

	public void setPhotoItem(PhotoItem photoItem) {
		mPhotoItem = photoItem;
		mAskId = mPhotoItem.getAskId();
		mPhotoId = mPhotoItem.getPid();
		mType = (mPhotoItem.getType() == TYPE_ASK) ? "ask" : "reply";
		category_id = mPhotoItem.getCategoryId();
	}

	// TODO PhotoBroswer的暂时这样
	// 设置该psDialog的父类为PhotoBroswer
	public void setIsFromPhotoBroswer() {
		isFromPhotoBroswer = true;
	}

	// 设置photo的type, target id, ask id
	public void setPhotoInfo(String type, Long id, Long askId) {
		mType = type;
		mPhotoId = id;
		mAskId = askId;

	}

	private void initListeners() {
		mDownloadBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mProgressDialog == null) {
					mProgressDialog = new CustomProgressingDialog(mContext);
				}
				if (!mProgressDialog.isShowing()) {
					mProgressDialog.show();
				}

				ThreadManager.executeOnNetWorkThread(new Runnable() {
					@Override
					public void run() {
						ImageInfo info = PhotoRequest.getImageInfo(mType,
								mPhotoId);

						if (!info.isSuccessful) {
							mHandler.sendEmptyMessage(MSG_FAILED);
						} else {
							for(String s:info.urls){
								String[] thumbs = s.split("/");
								String name;
								if(thumbs.length > 0) {
									name = thumbs[thumbs.length - 1];
								}else {
									name = s;
								}
								Bitmap image = PhotoRequest.downloadImage(s);
								String path = ImageIOManager.getInstance()
										.saveImage(name, image);
								// 更新相册后通知系统扫描更新
								Intent intent = new Intent(
										Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
								Uri uri = Uri.fromFile(new File(path));
								intent.setData(uri);
								mContext.sendBroadcast(intent);

								Message msg = mHandler
										.obtainMessage(MSG_SUCCESSFUL);
								msg.obj = path;
								msg.sendToTarget();
							}
						}
					}
				});
			}
		});

		// 点击 塞入进行中
		mUploadBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (mProgressDialog == null) {
					mProgressDialog = new CustomProgressingDialog(mContext);
				}
				if (!mProgressDialog.isShowing()) {
					mProgressDialog.show();
				}

				ThreadManager.executeOnNetWorkThread(new Runnable() {
					@Override
					public void run() {
						ImageInfo info = PhotoRequest.getImageInfo(mType,
								mPhotoId,category_id);
						if (!info.isSuccessful) {
							mHandler.sendEmptyMessage(MSG_RECORD_FAILED);
						} else {
							mHandler.sendEmptyMessage(MSG_RECORD_SUCCESS);
						}
					}
				});

			}
		});

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

	@Override
	public boolean handleMessage(final Message msg) {
		mProgressDialog.dismiss();
		View toastView = LayoutInflater.from(mContext).inflate(
				R.layout.toast_layout, null);
		TextView aboveText = (TextView) toastView
				.findViewById(R.id.toast_text_above);
		TextView belowText = (TextView) toastView
				.findViewById(R.id.toast_text_below);
		dismiss();
		switch (msg.what) {
		case MSG_SUCCESSFUL:
			String path = (String) msg.obj;
			Toast toast = Toast.makeText(mContext, "素材保存到" + path,
					Toast.LENGTH_SHORT);
			aboveText.setText("下载成功,");
			belowText.setText("我猜你会用美图秀秀来P?");
			toast.setView(toastView);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			if (!isFromPhotoBroswer) {
				mPhotoItem.setIsDownloaded(true);
			}
			break;
		case MSG_FAILED:
			// TODO 获取图片信息失败
			Toast.makeText(mContext, "下载素材失败", Toast.LENGTH_SHORT).show();
			break;
		case MSG_RECORD_SUCCESS:
			Toast toast2 = Toast.makeText(mContext, "已塞入进行中",
					Toast.LENGTH_SHORT);
			aboveText.setText("添加成功,");
			belowText.setText("在“进行中”等你下载喽!");
			toast2.setView(toastView);
			toast2.setGravity(Gravity.CENTER, 0, 0);
			toast2.show();
			EventBus.getDefault().post(
					new MyPageRefreshEvent(MyPageRefreshEvent.REPLY));
			break;
		case MSG_RECORD_FAILED:
			Toast.makeText(mContext, "塞入进行中失败", Toast.LENGTH_SHORT).show();
			break;
		}
		return true;
	}
}