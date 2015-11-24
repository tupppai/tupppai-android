package com.psgod.ui.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.psgod.R;
import com.psgod.UploadCache;
import com.psgod.eventbus.MyPageRefreshEvent;
import com.psgod.model.FileUtils;
import com.psgod.model.SelectImage;
import com.psgod.ui.adapter.MultiImageSelectAdapter;

import de.greenrobot.event.EventBus;

/**
 * 上传多图 选择图片
 * 
 * @author ZouMengyuan
 */
public class MultiImageSelectActivity extends PSGodBaseActivity {
	private final static String TAG = MultiImageSelectActivity.class
			.getSimpleName();
	private Context mContext;

	private final static int MaxImageOne = 1;
	private final static int MaxImageTwo = 2;
	private static int MaxImageSelectCount = MaxImageOne;

	public final static String TYPE_ASK_SELECT = "TypeAskSelect";
	public final static String TYPE_REPLY_SELECT = "TypeReplySelect";
	public static String IMAGE_SELECT_TYPE = TYPE_ASK_SELECT;

	private TextView mNextText;
	private TextView mSelectCountText;
	private ImageButton mBackBtn;
	private GridView mImageGridView;
	private MultiImageSelectAdapter mMultiImageAdapter;
	private List<SelectImage> images;

	private Long mAskId = 0l;

	private File mTmpFile;
	private static final int REQUEST_CAMERA = 0x225;

	// 结果数据
	private ArrayList<String> resultList = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_multi_image_select);

		initViews();
		initListeners();

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		IMAGE_SELECT_TYPE = bundle.getString("SelectType", "");

		mAskId = bundle.getLong("AskId", 0l);

		if (IMAGE_SELECT_TYPE.equals(TYPE_ASK_SELECT)) {
			MaxImageSelectCount = MaxImageTwo;
		} else {
			MaxImageSelectCount = MaxImageOne;
		}

		if (intent.hasExtra("resultList")) {
			resultList = intent.getStringArrayListExtra("resultList");
			mSelectCountText.setText(Integer.toString(resultList.size()));

			if (resultList.size() != 0) {
				mNextText.setEnabled(true);
			} else {
				mNextText.setEnabled(false);
			}
		}

		// 扫描手机内的图片
		getSupportLoaderManager().initLoader(0, null, mLoaderCallback);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		UploadCache.getInstence().clear();
		EventBus.getDefault().post(new MyPageRefreshEvent(MyPageRefreshEvent.ASK));
		EventBus.getDefault().post(new MyPageRefreshEvent(MyPageRefreshEvent.REPLY));

	}

	public void initViews() {
		mImageGridView = (GridView) findViewById(R.id.image_select_grid);
		mMultiImageAdapter = new MultiImageSelectAdapter(mContext);
		mImageGridView.setAdapter(mMultiImageAdapter);

		mBackBtn = (ImageButton) findViewById(R.id.btn_back);
		mNextText = (TextView) findViewById(R.id.text_next);
		mSelectCountText = (TextView) findViewById(R.id.select_count);
		mNextText.setEnabled(false);

		mImageGridView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@SuppressWarnings("deprecation")
					@Override
					@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
					public void onGlobalLayout() {

						final int width = mImageGridView.getWidth();
						final int height = mImageGridView.getHeight();

						final int desireSize = getResources()
								.getDimensionPixelOffset(
										R.dimen.multi_image_slect_size);
						final int numCount = width / desireSize;
						mImageGridView.setNumColumns(numCount);
						final int columnSpace = getResources()
								.getDimensionPixelOffset(
										R.dimen.multi_image_select_space_size);
						int columnWidth = (width - columnSpace * (numCount - 1))
								/ numCount;
						mMultiImageAdapter.setItemSize(columnWidth);

						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
							mImageGridView.getViewTreeObserver()
									.removeOnGlobalLayoutListener(this);
						} else {
							mImageGridView.getViewTreeObserver()
									.removeGlobalOnLayoutListener(this);
						}
					}
				});
	}

	public void initListeners() {
		mImageGridView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapterView,
							View view, int i, long l) {
						// 如果显示照相机，则第一个Grid显示为照相机
						if (i == 0) {
							if (resultList.size() < MaxImageSelectCount) {
								showCameraAction();
							} else {
								Toast.makeText(MultiImageSelectActivity.this,
										"最多选择" + MaxImageSelectCount + "张图片",
										Toast.LENGTH_SHORT).show();
							}

						} else {
							// 正常操作
							SelectImage image = (SelectImage) adapterView
									.getAdapter().getItem(i);
							selectImageFromGrid(image, view);
						}
					}
				});

		mNextText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(MultiImageSelectActivity.this,
						UploadMultiImageActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("SelectType", IMAGE_SELECT_TYPE);
				bundle.putStringArrayList(
						UploadMultiImageActivity.MULTIIMAGESELECTRESULT,
						resultList);
				bundle.putLong("AskId", mAskId);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});

		mBackBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	/**
	 * 选择相机
	 */
	private void showCameraAction() {
		// 设置系统相机拍照后的输出路径为DCIM目录
		mTmpFile = FileUtils.createTmpFile(this);
		// 跳转到系统照相机
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (cameraIntent.resolveActivity(this.getPackageManager()) != null) {
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(mTmpFile));
			startActivityForResult(cameraIntent, REQUEST_CAMERA);
		} else {
			Toast.makeText(this, "没有相机", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 相机拍照完成后，返回图片路径
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_CAMERA) {

				// 更新相册后通知系统扫描更新
				Intent intentSystem = new Intent(
						Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				Uri uri = Uri.fromFile(mTmpFile);
				intentSystem.setData(uri);
				mContext.sendBroadcast(intentSystem);

				resultList.add(mTmpFile.getAbsolutePath());
				// 调用相机拍照后，再次扫描手机内的图片
				getSupportLoaderManager().initLoader(0, null, mLoaderCallback);
				mSelectCountText.setText(Integer.toString(resultList.size()));

				Intent intent = new Intent(MultiImageSelectActivity.this,
						UploadMultiImageActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("SelectType", IMAGE_SELECT_TYPE);
				bundle.putStringArrayList(
						UploadMultiImageActivity.MULTIIMAGESELECTRESULT,
						resultList);
				intent.putExtras(bundle);
				startActivity(intent);

			}
		}
	}

	/**
	 * 选择图片操作
	 * 
	 * @param image
	 */
	private void selectImageFromGrid(SelectImage image, View imgView) {
		if (image != null) {
			// 多选模式
			if (resultList.contains(image.path)) {
				resultList.remove(image.path);
				// 移除

				if (resultList.size() != 0) {
					mSelectCountText
							.setText(Integer.toString(resultList.size()));
					mNextText.setEnabled(true);
				} else {
					mSelectCountText.setText("0");
					mNextText.setEnabled(false);
				}
			} else {
				// 添加
				View view = new View(this);

				// 判断选择数量问题
				if (MaxImageSelectCount == resultList.size()) {
					Toast.makeText(MultiImageSelectActivity.this,
							"最多选择" + MaxImageSelectCount + "张图片",
							Toast.LENGTH_SHORT).show();
					return;
				}

				resultList.add(image.path);
				mSelectCountText.setText(Integer.toString(resultList.size()));
				mNextText.setEnabled(true);
			}
			mMultiImageAdapter.select(image);
		}
	}

	private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

		private final String[] IMAGE_PROJECTION = {
				MediaStore.Images.Media.DATA,
				MediaStore.Images.Media.DISPLAY_NAME,
				MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media._ID };

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			CursorLoader cursorLoader = new CursorLoader(
					MultiImageSelectActivity.this,
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					IMAGE_PROJECTION, null, null, IMAGE_PROJECTION[2] + " DESC");
			return cursorLoader;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			if (data != null) {
				images = new ArrayList<SelectImage>();
				int count = data.getCount();
				if (count > 0) {
					data.moveToFirst();
					do {
						String path = data.getString(data
								.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
						String name = data.getString(data
								.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
						long dateTime = data.getLong(data
								.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
						SelectImage image = new SelectImage(path, name,
								dateTime);
						images.add(image);

					} while (data.moveToNext());

					mMultiImageAdapter.setData(images);
					// 设定默认选择
					if (resultList != null && resultList.size() > 0) {
						mMultiImageAdapter.setDefaultSelected(resultList);
					}

				}
			}
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {

		}
	};

}
