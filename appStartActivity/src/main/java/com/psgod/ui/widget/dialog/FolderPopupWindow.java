package com.psgod.ui.widget.dialog;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.psgod.R;
import com.psgod.model.SelectFolder;
import com.psgod.model.SelectImage;
import com.psgod.ui.activity.PSGodBaseActivity;
import com.psgod.ui.adapter.SelectFolderAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/1/19 0019.
 * 选择文件夹浮窗
 */
public class FolderPopupWindow extends PopupWindow {

    // 选择全部图片
    private static final int LOADER_ALL = 0;
    // 选择某个文件夹下的图片
    private static final int LOADER_CATEGORY = 1;

    private View mFolderPopView;
    private FragmentActivity mContext;

    private ListView mFolderList;
    private SelectFolderAdapter mFolderAdapter;
    private TextView mCancelTxt;

    private int width;
    private int height;

    private List<SelectImage> images = new ArrayList<SelectImage>();
    private ArrayList<SelectFolder> mResultFolder = new ArrayList<SelectFolder>();

    //是否初始化过文件目录
    private boolean hasFolderGened = false;

    public FolderPopupWindow(FragmentActivity context) {
        super(LayoutInflater.from(context).
                inflate(R.layout.popupwindow_select_image_folder, null), -1, -1, true);
        mFolderPopView = getContentView();
        mContext = context;
        setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        initView();
        initListener();
    }

    private void initView() {
        mFolderList = (ListView) mFolderPopView.findViewById(R.id.image_folder_list);
        mFolderAdapter = new SelectFolderAdapter(mContext);
        mFolderList.setAdapter(mFolderAdapter);
        mContext.getSupportLoaderManager()
                .restartLoader(LOADER_ALL, null,
                        mLoaderCallback);
        mCancelTxt = (TextView) mFolderPopView.findViewById(R.id.image_folder_cancel);
    }

    private void initListener() {
        mFolderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView,
                                    View view, int i, long l) {
                if (i == 0) {
                    mContext.getSupportLoaderManager()
                            .restartLoader(LOADER_ALL, null,
                                    mLoaderCallback);
                } else {
                    SelectFolder folder = (SelectFolder) adapterView.getAdapter()
                            .getItem(i);
                    if (null != folder) {
                        Bundle args = new Bundle();
                        args.putString("path", folder.path);
                        mContext.getSupportLoaderManager()
                                .restartLoader(LOADER_CATEGORY, args,
                                        mLoaderCallback);
                        if (onFolderChangeListener != null) {
                            onFolderChangeListener.onClick(folder);
                        }
                    }
                }
                mFolderAdapter.setSelectIndex(i);
                dismiss();

                // 滑动到最初始位置
//                mImageGridView.smoothScrollToPosition(0);
            }
        });

        mCancelTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    private OnFolderChangeListener onFolderChangeListener;

    public void setOnFolderChangeListener(OnFolderChangeListener onFolderChangeListener) {
        this.onFolderChangeListener = onFolderChangeListener;
    }

    public interface OnFolderChangeListener {

        void onClick(SelectFolder folder);

        void onDataChanger(List<SelectImage> data);

    }
//
//    @Override
//    public void setWidth(int width) {
//        this.width = width;
//    }
//
//    @Override
//    public void setHeight(int height) {
//        this.height = height;
//    }

    /**
     * 其他参数 有需要时扩展
     */

    private void commonInit() {
//        setWidth(width);
//        setHeight(height);
        setTouchable(true);
        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                return false;
            }
        });
        setAnimationStyle(R.style.popwindow_anim_style);
    }

    @Override
    public void showAsDropDown(View anchor) {
        super.showAsDropDown(anchor);
        commonInit();
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        super.showAsDropDown(anchor, xoff, yoff, gravity);
        commonInit();
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        super.showAsDropDown(anchor, xoff, yoff);
        commonInit();
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        commonInit();
    }

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media._ID};

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (id == LOADER_ALL) {
                CursorLoader cursorLoader = new CursorLoader(mContext,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        IMAGE_PROJECTION, null, null, IMAGE_PROJECTION[2]
                        + " DESC");
                return cursorLoader;
            } else if (id == LOADER_CATEGORY) {
                CursorLoader cursorLoader = new CursorLoader(mContext,
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
                        images.add(image);

                    } while (data.moveToNext());

                    if (onFolderChangeListener != null) {
                        onFolderChangeListener.onDataChanger(images);
                    }

                    //初始化文件目录
                    if (!hasFolderGened) {
                        mContext.getSupportLoaderManager().initLoader(0, null, mFolderCallback);
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
            CursorLoader cursorLoader = new CursorLoader(mContext,
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


}
