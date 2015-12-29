package com.psgod.ui.widget;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.psgod.R;
import com.psgod.model.SelectFolder;
import com.psgod.model.SelectImage;
import com.psgod.ui.adapter.MultiImageSelectAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/29 0029.
 */
public class UploadWindow {

    private PopupWindow mPopupWindow;
    private ListView mList;
    private MultiImageSelectAdapter mMultiImageAdapter;
    private Context mContext;
    private ArrayList<SelectImage> images = new ArrayList<SelectImage>();


    private UploadWindow() {

    }

    public UploadWindow(Context context, int width, int height, boolean focusable) {
        super();
        View view = LayoutInflater.from(context).
                inflate(R.layout.popupwindow_select_image_folder, null);
        mContext = context;
        initDefaultView(view);
        mPopupWindow = new PopupWindow(view, width, height, focusable);
        initCommon();
    }

    private void initCommon() {
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
    }
//    public UploadWindow(View view, int width, int height, boolean focusable) {
//        super();
//        mPopupWindow = new PopupWindow(view, width, height, focusable);
//        initCommon();
//    }

    public void showAtLocation(View parent, int gravity, int x, int y) {
        if (mPopupWindow != null) {
            mPopupWindow.showAtLocation(parent, gravity, x, y);
        }
    }

    public void showAsDropDown(View anchor) {
        if (mPopupWindow != null) {
            mPopupWindow.showAsDropDown(anchor);
        }
    }

    public void showAsDropDown(View anchor, int xoff, int yoff) {
        if (mPopupWindow != null) {
            mPopupWindow.showAsDropDown(anchor, xoff, yoff);
        }
    }

    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        if (mPopupWindow != null) {
            mPopupWindow.showAsDropDown(anchor, xoff, yoff, gravity);
        }
    }

    public PopupWindow getmPopupWindow() {
        return mPopupWindow;
    }

    public boolean isShow() {
        return mPopupWindow.isShowing();
    }

    public void dismiss() {
        mPopupWindow.dismiss();
    }

    private void initDefaultView(View view) {
        mList = (ListView) view.findViewById(R.id.image_folder_list);
        mMultiImageAdapter = new MultiImageSelectAdapter(mContext);
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
                    } while (data.moveToNext());

                    mMultiImageAdapter.setData(images);
                    // 设定默认选择
//                    if (resultList != null && resultList.size() > 0) {
//                        mMultiImageAdapter.setDefaultSelected(resultList);
//                    }
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

}
