package com.psgod.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.psgod.R;
import com.psgod.WeakReferenceHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2016/1/4 0004.
 */
public class ImageSelectDialog extends Dialog implements Handler.Callback {

    private long categoryid = -1;
    private int showType;
    private Context mContext;

    private RelativeLayout mView;
    private RelativeLayout mArea;
    private RelativeLayout mInputArea;
    private RecyclerView mImageArea;
    private TextView mAlbumTxt;
    private TextView mPhotoTxt;
    private TextView mNumTxt;
    private TextView mSureTxt;
    private ImageView mBangpImg;
    private ImageView mImageimg;
    private EditText mEdit;
    private TextView mUpTxt;

//    private

    private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);
    private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);

    public static final int HIDE_INPUT = 1;
    public static final int AREA_SHOW = 2;

    public static final int SHOW_TYPE_ASK = 0;
    public static final int SHOW_TYPE_REPLY = 1;
    public static final int SHOW_TYPE_ACTIVITY = 2;

    public ImageSelectDialog(Context context, int showType) {
        super(context, R.style.ImageSelectDialog);
        mContext = context;
        this.showType = showType;
    }

    public ImageSelectDialog(Context context, long categoryid, int showType) {
        super(context, R.style.ImageSelectDialog);
        this.categoryid = categoryid;
        mContext = context;
        this.showType = showType;
    }

    @Override
    public void show() {
        initView();
        initListener();
        getWindow().getAttributes().width = -1;
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setWindowAnimations(R.style.popwindow_anim_style);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        super.show();
    }

    private void initView() {
        setCanceledOnTouchOutside(true);
        mView = (RelativeLayout) LayoutInflater.from(mContext).
                inflate(R.layout.dialog_image_select, null);
        setContentView(mView);
        mArea = (RelativeLayout) mView.findViewById(R.id.dialog_image_select_area);
        mInputArea = (RelativeLayout) mView.findViewById(R.id.dialog_image_select_input);
        mImageArea = (RecyclerView) mView.findViewById(R.id.dialog_image_select_imgarea);
        mAlbumTxt = (TextView) mView.findViewById(R.id.dialog_image_select_album_txt);
        mPhotoTxt = (TextView) mView.findViewById(R.id.dialog_image_select_photo_txt);
        mNumTxt = (TextView) mView.findViewById(R.id.dialog_image_select_num_txt);
        mSureTxt = (TextView) mView.findViewById(R.id.dialog_image_select_sure_txt);
        mBangpImg = (ImageView) mView.findViewById(R.id.widge_image_select_bangplist_img);
        mImageimg = (ImageView) mView.findViewById(R.id.widge_image_select_image_img);
        mEdit = (EditText) mView.findViewById(R.id.widge_image_select_edit);
        mUpTxt = (TextView) mView.findViewById(R.id.widge_image_select_up);

        switch (showType){
            case SHOW_TYPE_ASK:
                mBangpImg.setVisibility(View.GONE);
                mImageimg.setImageResource(R.mipmap.zuopin_ic_image_selected);
                mPhotoTxt.setVisibility(View.VISIBLE);
                break;
            case SHOW_TYPE_REPLY:
                mBangpImg.setVisibility(View.VISIBLE);
                mImageimg.setImageResource(R.mipmap.bangp_ic_image);
                mPhotoTxt.setVisibility(View.GONE);

                break;
            case SHOW_TYPE_ACTIVITY:
                mBangpImg.setVisibility(View.GONE);
                mImageimg.setImageResource(R.mipmap.zuopin_ic_image_selected);
                mPhotoTxt.setVisibility(View.GONE);

                break;
        }
    }

    private void initListener() {
        mEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mArea.setVisibility(View.GONE);
            }
        });

        mBangpImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideInputPanel();
                fixedThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(150);
                            Message msg = mHandler.obtainMessage(AREA_SHOW);
                            mHandler.sendMessage(msg);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    // 隐藏输入法
    private void hideInputPanel() {
        // 隐藏软键盘
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEdit.getWindowToken(), 0);
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case HIDE_INPUT:
                hideInputPanel();
                break;
            case AREA_SHOW:
                mArea.setVisibility(View.VISIBLE);
                break;
        }
        return true;
    }
}
