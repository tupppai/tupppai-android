package com.psgod.ui.activity;

import android.annotation.SuppressLint;
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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.psgod.Constants;
import com.psgod.R;
import com.psgod.UploadCache;
import com.psgod.eventbus.MyPageRefreshEvent;
import com.psgod.eventbus.RefreshEvent;
import com.psgod.model.FileUtils;
import com.psgod.model.SelectFolder;
import com.psgod.model.SelectImage;
import com.psgod.ui.adapter.MultiImageSelectAdapter;
import com.psgod.ui.adapter.SelectFolderAdapter;
import com.psgod.ui.widget.dialog.ImageCategoryDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    public static final String ACTIVITY_ID = "ActivityId";
    public static final String CHANNEL_ID = "channel_id";
    public static final String SELECT_IMAGE = "select_image";
    public static final String SELECT_TYPE = "SelectType";

    public final static String TYPE_ASK_SELECT = "TypeAskSelect";
    public final static String TYPE_REPLY_SELECT = "TypeReplySelect";
    public static String IMAGE_SELECT_TYPE = TYPE_ASK_SELECT;

    // 选择全部图片
    private static final int LOADER_ALL = 0;
    // 选择某个文件夹下的图片
    private static final int LOADER_CATEGORY = 1;

    private static final int REQUEST_CAMERA = 0x225;

    private View mParentView;
    private TextView mNextText;
    private TextView mSelectCountText;
    private TextView mSelectFolderText;
    private ImageButton mBackBtn;
    private GridView mImageGridView;
    private MultiImageSelectAdapter mMultiImageAdapter;
    private List<SelectImage> images = new ArrayList<SelectImage>();

    private Long mAskId = 0l;
    private String mActivityId;
    private String mChannelId;
    private boolean isAsk;
    private File mTmpFile;
    private String jumpPath;

    private View mFolderPopView;
    private PopupWindow mFolderPopupWindow;
    private ListView mImageListView;
    private SelectFolderAdapter mFolderAdapter;
    private boolean hasFolderGened = false;

    // 结果数据
    private ArrayList<String> resultList = new ArrayList<String>();
    // 文件夹数据
    private ArrayList<SelectFolder> mResultFolder = new ArrayList<SelectFolder>();
    private boolean isFinish = false;

    public void onEventMainThread(RefreshEvent event) {
        if (event.className.equals(this.getClass().getName())) {
//			finish();
            isFinish = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFinish) {
            finish();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        setContentView(R.layout.activity_multi_image_select);
        EventBus.getDefault().register(this);

        initViews();
        initListeners();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        IMAGE_SELECT_TYPE = bundle.getString(SELECT_TYPE, "");

        mAskId = bundle.getLong("AskId", 0l);
        mActivityId = bundle.getString(ACTIVITY_ID);
        mChannelId = bundle.getString(CHANNEL_ID);
        isAsk = bundle.getBoolean("isAsk", false);
        List<String> selectImages = bundle.getStringArrayList(SELECT_IMAGE);
        if (selectImages == null) {
            selectImages = new ArrayList<>();
        }
        resultList.addAll(selectImages);
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
//        getSupportLoaderManager().initLoader(LOADER_ALL, null, mLoaderCallback);

    }

    public void initViews() {
        mFolderAdapter = new SelectFolderAdapter(this);

        mImageGridView = (GridView) findViewById(R.id.image_select_grid);
        mMultiImageAdapter = new MultiImageSelectAdapter(mContext);

        mImageGridView.setAdapter(mMultiImageAdapter);

        mParentView = findViewById(R.id.image_select_parent);
        mBackBtn = (ImageButton) findViewById(R.id.btn_back);
        mNextText = (TextView) findViewById(R.id.text_next);
        mSelectCountText = (TextView) findViewById(R.id.select_count);
        mSelectFolderText = (TextView) findViewById(R.id.select_folder);
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

        if (mFolderPopupWindow == null) {
            createPopupFolder();
        }

        Intent intent = getIntent();
        jumpPath = intent.getStringExtra(ImageCategoryDialog.PATH);
        if (jumpPath == null ||
                jumpPath.equals("")) {
            getSupportLoaderManager()
                    .restartLoader(LOADER_ALL, null,
                            mLoaderCallback);
            mSelectFolderText.setText("全部图片");
        } else if (jumpPath != null) {
            Bundle args = new Bundle();
            args.putString("path", jumpPath);
            getSupportLoaderManager()
                    .restartLoader(LOADER_CATEGORY, args,
                            mLoaderCallback);
            mSelectFolderText.setText(intent.getStringExtra(ImageCategoryDialog.NAME));
        }

//        getSupportLoaderManager()
//                .restartLoader(0, null,
//                        mFolderCallback);

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

        mSelectFolderText.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                if (mFolderPopupWindow.isShowing()) {
                    mFolderPopupWindow.dismiss();
                } else {
                    mFolderPopupWindow.showAtLocation(mParentView, Gravity.CENTER, 0, 0);
                    mFolderPopupWindow.setFocusable(true);
                    mFolderPopupWindow.update();

                    mFolderPopupWindow.getContentView().setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            // TODO Auto-generated method stub
                            mFolderPopupWindow.setFocusable(false);
                            mFolderPopupWindow.dismiss();
                            return true;
                        }

                    });

                    int index = mFolderAdapter.getSelectIndex();
                    index = index == 0 ? index : index - 1;
                    mImageListView.setSelection(index);
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
                bundle.putString("ActivityId", mActivityId);
                bundle.putString("channel_id", mChannelId);
                bundle.putBoolean("isAsk", isAsk);
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

    /**
     * 创建弹出的ListView
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @SuppressLint("NewApi")
    private void createPopupFolder() {
        mFolderPopView = LayoutInflater.from(mContext)
                .inflate(R.layout.popupwindow_select_image_folder, null);
        mImageListView = (ListView) mFolderPopView.findViewById(R.id.image_folder_list);
        mFolderPopupWindow = new PopupWindow(mFolderPopView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mFolderPopupWindow.setWidth(-1);
        mFolderPopupWindow.setHeight(-1);
        mFolderPopupWindow.setOutsideTouchable(true);

        mImageListView.setAdapter(mFolderAdapter);

        mImageListView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView,
                                            View view, int i, long l) {
                        if (i == 0) {
                            getSupportLoaderManager()
                                    .restartLoader(LOADER_ALL, null,
                                            mLoaderCallback);
                            mSelectFolderText.setText("全部图片");
                        } else {
                            SelectFolder folder = (SelectFolder) adapterView.getAdapter()
                                    .getItem(i);
                            if (null != folder) {
                                Bundle args = new Bundle();
                                args.putString("path", folder.path);
                                getSupportLoaderManager()
                                        .restartLoader(LOADER_CATEGORY, args,
                                                mLoaderCallback);
                                mSelectFolderText.setText(folder.name);
                            }
                        }
                        mFolderAdapter.setSelectIndex(i);
                        mFolderPopupWindow.dismiss();

                        // 滑动到最初始位置
                        mImageGridView.smoothScrollToPosition(0);
                    }
                });

        // 处理PopupWindow显示后，返回键无法响应
        mImageListView.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                mFolderPopupWindow.dismiss();
                return false;
            }

        });
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
//                getSupportLoaderManager().initLoader(0, null, mFolderCallback);
                mSelectCountText.setText(Integer.toString(resultList.size()));

                Intent intent = new Intent(MultiImageSelectActivity.this,
                        UploadMultiImageActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("SelectType", IMAGE_SELECT_TYPE);
                bundle.putStringArrayList(
                        UploadMultiImageActivity.MULTIIMAGESELECTRESULT,
                        resultList);
                bundle.putLong("AskId", mAskId);
                bundle.putString("channel_id", mChannelId);
                bundle.putString("ActivityId", mActivityId);
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

    private String folderName;

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media._ID};

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (id == LOADER_ALL) {
                CursorLoader cursorLoader = new CursorLoader(MultiImageSelectActivity.this,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        IMAGE_PROJECTION, null, null, IMAGE_PROJECTION[2]
                        + " DESC");
                return cursorLoader;
            } else if (id == LOADER_CATEGORY) {
                folderName = args.getString("path");
                CursorLoader cursorLoader = new CursorLoader(MultiImageSelectActivity.this,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        IMAGE_PROJECTION, IMAGE_PROJECTION[0] + " like '%"
                        + args.getString("path") + "%'", null,
                        IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            }

            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                if (images != null && images.size() > 0) {
                    images.clear();
                }
                int resultLength = resultList.size();
                for(int i = 0 ; i <resultLength; i++ ){
                    images.add(new SelectImage(resultList.get(i),"",0));
                }
                int count = data.getCount();
                if (count > 0) {
                    data.moveToFirst();
                    out:
                    do {
                        String path = data.getString(data
                                .getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                        String name = data.getString(data
                                .getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                        long dateTime = data.getLong(data
                                .getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                        SelectImage image = new SelectImage(path, name,
                                dateTime);
                        //判断是否是已选择的图片，因为已经置顶，如果是则不填充
                        for(int i = 0 ; i <resultLength; i++ ){
                            if(resultList.get(i).equals(path)){
                                images.get(i).name = name;
                                images.get(i).time = dateTime;
                                continue out;
                            }
                        }
                        images.add(image);

                    } while (data.moveToNext());

                    mMultiImageAdapter.setData(images);
                    // 设定默认选择
                    if (resultList != null && resultList.size() > 0) {
                        mMultiImageAdapter.setDefaultSelected(resultList);
                    }

                    //初始化文件目录
                    if (!hasFolderGened) {
                        getSupportLoaderManager().initLoader(0, null, mFolderCallback);
                    }
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    private LoaderManager.LoaderCallbacks<Cursor> mFolderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media._ID};

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            CursorLoader cursorLoader = new CursorLoader(MultiImageSelectActivity.this,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    IMAGE_PROJECTION, null, null, IMAGE_PROJECTION[2]
                    + " DESC");
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
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
                        // 获取文件夹名称
                        File imageFile = new File(path);
                        File folderFile = imageFile.getParentFile();
                        SelectFolder folder = new SelectFolder();
                        folder.name = folderFile.getName();
                        folder.path = folderFile.getAbsolutePath();
                        folder.cover = image;
                        if (!mResultFolder.contains(folder)) {
                            List<SelectImage> imageList = new ArrayList<SelectImage>();
                            imageList.add(image);
                            folder.images = imageList;
                            mResultFolder.add(folder);
                            // 判断进入时是那个相册被选中
                            if (folderFile.getPath().equals(jumpPath)) {
                                mFolderAdapter.setSelectIndex(mResultFolder.size());
                            }
                        } else {
                            // 更新
                            SelectFolder f = mResultFolder.get(mResultFolder
                                    .indexOf(folder));
                            f.images.add(image);
                        }
                    } while (data.moveToNext());
                    mFolderAdapter.setData(mResultFolder);
                    hasFolderGened = true;
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UploadCache.getInstence().clear();
        EventBus.getDefault().post(new MyPageRefreshEvent(MyPageRefreshEvent.ASK));
//        EventBus.getDefault().post(new MyPageRefreshEvent(MyPageRefreshEvent.REPLY));
        EventBus.getDefault().unregister(this);
    }

}
