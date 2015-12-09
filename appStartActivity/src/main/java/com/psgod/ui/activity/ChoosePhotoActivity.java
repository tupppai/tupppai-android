package com.psgod.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;
import android.widget.Toast;

import com.psgod.Constants;
import com.psgod.ImageIOManager;
import com.psgod.Logger;

import java.io.File;

/**
 * 负责控制选择照片的跳转逻辑，不负责界面显示
 * 
 * @author Rayal
 * 
 */
public class ChoosePhotoActivity extends PSGodBaseActivity {
	private static final String TAG = ChoosePhotoActivity.class.getSimpleName();

	// 拍照还是从本地选图
	public static final String REQUEST_CODE = "RequestCode";
	public static final String DEST_ACTIVITY_NAME = "DestActivityName";
	public static final String CHOOSE_PHOTO_FROM = "ChoosePhotoFrom";
	public static final int FROM_CAMERA = 0x770;
	public static final int FROM_ALBUM = 0x771;
	private static final int DEFAULT_REQUEST_CODE = Integer.MIN_VALUE;

	private String mImagePath;
	private String mDestActivityName;
	private int mRequestCode;
	private Bundle mExtras;

	/**
	 * 不需要返回数据的，调用该接口即可
	 * 
	 * @param context
	 * @param chooseFrom
	 * @param destActivityName
	 */
	public static void startActivity(Activity context, int chooseFrom,
			String destActivityName) {
		startActivity(context, chooseFrom, destActivityName,
				DEFAULT_REQUEST_CODE, null);
	}

	/**
	 * 不需要返回数据的，调用该接口即可
	 * 
	 * @param context
	 * @param chooseFrom
	 * @param destActivityName
	 */
	public static void startActivity(Activity context, int chooseFrom,
			String destActivityName, Bundle extras) {
		startActivity(context, chooseFrom, destActivityName,
				DEFAULT_REQUEST_CODE, extras);
	}

	/**
	 * 需要返回数据的，调用该接口启动界面 在onActivityResutl里获取返回的数据
	 * 
	 * @param context
	 * @param chooseFrom
	 *            FROM_CAMERA: 拍照 或 FROM_ALBUM: 从本地相册选择
	 * @param destActivityName
	 * @param requestCode
	 */
	public static void startActivity(Activity context, int chooseFrom,
			String destActivityName, int requestCode, Bundle extras) {
		if ((context == null) || TextUtils.isEmpty(destActivityName)) {
			throw new IllegalArgumentException(
					"ChoosePhotoActivity.startActivity(): illegal arguments");
		}

		Intent intent = new Intent(context, ChoosePhotoActivity.class);
		intent.putExtra(CHOOSE_PHOTO_FROM, chooseFrom);
		intent.putExtra(DEST_ACTIVITY_NAME, destActivityName);

		if (extras != null) {
			intent.putExtras(extras);
		}
		if (requestCode == DEFAULT_REQUEST_CODE) {
			context.startActivity(intent);
		} else {
			intent.putExtra(REQUEST_CODE, requestCode);
			context.startActivityForResult(intent, requestCode);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		mExtras = intent.getExtras();
		mDestActivityName = intent.getStringExtra(DEST_ACTIVITY_NAME);
		mRequestCode = intent.getIntExtra(REQUEST_CODE, DEFAULT_REQUEST_CODE);
		int from = intent.getIntExtra(CHOOSE_PHOTO_FROM, -1);
		if (from == FROM_CAMERA) {
			String state = Environment.getExternalStorageState();
			if (state.equals(Environment.MEDIA_MOUNTED)) {
				Intent newIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				// TODO 指定拍照的路径
				mImagePath = new StringBuilder(ImageIOManager.IMAGE_SAVED_PATH)
						.append(String.valueOf(System.currentTimeMillis()))
						.append(".jpg").toString();
				newIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(new File(mImagePath)));
				newIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
				startActivityForResult(newIntent, FROM_CAMERA);
			} else {
				Toast.makeText(getApplicationContext(), "请确认已经插入SD卡",
						Toast.LENGTH_SHORT).show();
			}
		} else if (from == FROM_ALBUM) {
			Intent newIntent = new Intent(Intent.ACTION_PICK,
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(newIntent, FROM_ALBUM);
		} else {
			// Error: 直接关掉当前界面
			Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_COLOR, TAG,
					"onCreate(): startFrom is null");
			finish();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if ((resultCode == Activity.RESULT_OK)
				&& ((requestCode == FROM_CAMERA) || (requestCode == FROM_ALBUM))) {
			// 把照片的路径传给下一个Activity
			if (requestCode == FROM_CAMERA) {
				// DO NOTHING

			} else {
				if (data == null) {
					Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_COLOR,
							TAG,
							"onActivityResult(): REQUEST_CHOOSE_PHOTO, data is null");
					finish();
				}

				mImagePath = getImagePathFromUri(data);
			}

			Intent intent = new Intent();
			intent.setClassName(ChoosePhotoActivity.this, mDestActivityName);
			intent.putExtra(Constants.IntentKey.PHOTO_PATH, mImagePath);
			if (mExtras != null) {
				intent.putExtras(mExtras);
			}
			if (mRequestCode == DEFAULT_REQUEST_CODE) {
				// 不需要监听结果，直接关闭当前界面
				startActivity(intent);
				finish();
			} else {
				startActivityForResult(intent, mRequestCode);
			}
		} else {
			// 传递requestCode
			setResult(resultCode, data);
			finish();
		}
	}

	/**
	 * 从URI中获取图片的路径
	 * 
	 * @param uri
	 * @return 如果uri 是以content开头，则需利用ContentResolver 找到其文件的绝对路径
	 *         如果是以file开头，则直接把file://去掉即可
	 */
	private String getImagePathFromUri(Intent intent) {
		Uri uri = intent.getData();
		if (uri.getScheme().equals("file")) {
			String path = uri.getEncodedPath();
			return path;
		} else if (uri.getScheme().equals("content")) {
			String[] filePathColumn = { MediaColumns.DATA };
			Cursor cursor = getContentResolver().query(uri, filePathColumn,
					null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String imagePath = cursor.getString(columnIndex);
			cursor.close();
			return imagePath;
		}
		return null;
	}

}
