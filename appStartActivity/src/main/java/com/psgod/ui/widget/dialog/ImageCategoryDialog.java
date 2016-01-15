package com.psgod.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.psgod.R;
import com.psgod.model.SelectFolder;
import com.psgod.model.SelectImage;
import com.psgod.ui.activity.AlbumSelectImageActivity;
import com.psgod.ui.activity.PSGodBaseActivity;
import com.psgod.ui.adapter.SelectFolderAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/29 0029.
 * 相册选择弹出框
 */
public class ImageCategoryDialog extends Dialog {

    private ListView mList;
    private TextView mCancel;
    private SelectFolderAdapter mFolderAdapter;
    private Context mContext;

    public static final String PATH = "path";
    public static final String NAME = "name";
    public static final int RESULT_CODE = 5049;
    public static final String RESULT = "select_image_result";

    public ImageCategoryDialog(Context context) {
        super(context, R.style.ImageSelectDialog);
        mContext = context;
    }

    private void initView() {
        View view = LayoutInflater.from(mContext).
                inflate(R.layout.popupwindow_select_image_folder, null);
        setContentView(view);
        mList = (ListView) view.findViewById(R.id.image_folder_list);
        mCancel = (TextView) view.findViewById(R.id.image_folder_cancel);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        mFolderAdapter = new SelectFolderAdapter(mContext);
        mFolderAdapter.setSelectIndex(-1);
        mList.setAdapter(mFolderAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(mContext, AlbumSelectImageActivity.class);
                if (i == 0) {
                    intent.putExtra(PATH, "");
                } else {
                    SelectFolder folder = (SelectFolder) adapterView.getAdapter()
                            .getItem(i);
                    intent.putExtra(PATH, folder.path);
                    intent.putExtra(NAME, folder.name);
                }
                ArrayList<String> selectStrs = new ArrayList<String>();
                if (selectImages != null) {
                    for (SelectImage selectImage : selectImages) {
                        selectStrs.add(selectImage.path);
                    }
                    intent.putStringArrayListExtra(AlbumSelectImageActivity.SELECT_IMAGE, selectStrs);
                    intent.putExtra(AlbumSelectImageActivity.SELECT_TYPE,selectType);
                }
                ((PSGodBaseActivity) mContext).startActivityForResult(intent, RESULT_CODE);
                dismiss();
            }
        });
        ((PSGodBaseActivity) mContext).getSupportLoaderManager().initLoader(0, null, mLoaderCallback);
    }

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

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

        private boolean hasFolderGened = false;
        private ArrayList<SelectFolder> mResultFolder = new ArrayList<SelectFolder>();
        private ArrayList<SelectImage> images = new ArrayList<SelectImage>();

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
                        images.add(image);

                        if (!hasFolderGened) {
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
                            } else {
                                // 更新
                                SelectFolder f = mResultFolder.get(mResultFolder
                                        .indexOf(folder));
                                f.images.add(image);
                            }
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
    public void show() {
        initView();
        getWindow().getAttributes().width = -1;
        getWindow().getAttributes().height = -1;
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setWindowAnimations(R.style.popwindow_anim_style);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        super.show();
    }

    List<SelectImage> selectImages;
    String selectType;

    public void show(List<SelectImage> selectImages,String selectType) {
        initView();
        this.selectImages = selectImages;
        this.selectType = selectType;
        getWindow().getAttributes().width = -1;
        getWindow().getAttributes().height = -1;
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setWindowAnimations(R.style.popwindow_anim_style);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        super.show();
    }
}
