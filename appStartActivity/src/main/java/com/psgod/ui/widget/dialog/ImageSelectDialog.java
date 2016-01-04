package com.psgod.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.psgod.R;

/**
 * Created by Administrator on 2016/1/4 0004.
 */
public class ImageSelectDialog {

    private long categoryid = -1;
    private Context mContext;
    private RelativeLayout mParent;
    private RelativeLayout mArea;
    private RecyclerView mImageArea;
    private TextView mAlbumTxt;
    private TextView mPhotoTxt;
    private TextView mNumTxt;
    private TextView mSureTxt;
    private ImageView mBangpImg;
    private ImageView mImageimg;
    private EditText mEdit;
    private TextView mUpTxt;

    public ImageSelectDialog(Context context) {
//        super(context, R.style.CaroPhotoDialog);
        mContext = context;
    }

    public ImageSelectDialog(Context context, long categoryid) {
//        super(context, R.style.CaroPhotoDialog);
        this.categoryid = categoryid;
        mContext = context;
    }

//    public void show() {
//        initView();
//        getWindow().getAttributes().width = -1;
//        getWindow().getAttributes().height = -1;
//        getWindow().setGravity(Gravity.BOTTOM);
//        getWindow().setWindowAnimations(R.style.popwindow_anim_style);
//        getWindow().setSoftInputMode(
//                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
//                        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//        super.show();
//    }

    private void initView() {
        mParent = (RelativeLayout) LayoutInflater.from(mContext).
                inflate(R.layout.dialog_image_select, null);
//        setContentView(mParent);
        mArea = (RelativeLayout) mParent.findViewById(R.id.dialog_image_select_area);
        mImageArea = (RecyclerView) mParent.findViewById(R.id.dialog_image_select_imgarea);
        mAlbumTxt = (TextView) mParent.findViewById(R.id.dialog_image_select_album_txt);
        mPhotoTxt = (TextView) mParent.findViewById(R.id.dialog_image_select_photo_txt);
        mNumTxt = (TextView) mParent.findViewById(R.id.dialog_image_select_num_txt);
        mSureTxt = (TextView) mParent.findViewById(R.id.dialog_image_select_sure_txt);
        mBangpImg = (ImageView) mParent.findViewById(R.id.widge_image_select_bangplist_img);
        mImageimg = (ImageView) mParent.findViewById(R.id.widge_image_select_image_img);
        mEdit = (EditText) mParent.findViewById(R.id.widge_image_select_edit);
        mUpTxt = (TextView) mParent.findViewById(R.id.widge_image_select_up);

    }
}
