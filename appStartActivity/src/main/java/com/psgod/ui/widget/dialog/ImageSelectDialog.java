package com.psgod.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.psgod.Constants;
import com.psgod.CustomToast;
import com.psgod.LoadUtils;
import com.psgod.PsGodImageLoader;
import com.psgod.R;
import com.psgod.UpLoadUtils;
import com.psgod.Utils;
import com.psgod.WeakReferenceHandler;
import com.psgod.model.FileUtils;
import com.psgod.model.PhotoItem;
import com.psgod.model.SelectImage;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.UserPhotoRequest;
import com.psgod.ui.activity.MultiImageSelectActivity;
import com.psgod.ui.activity.PSGodBaseActivity;
import com.psgod.ui.adapter.MultiImageSelectRecyclerAdapter;
import com.psgod.ui.view.EmptyRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2016/1/4 0004.
 */
public class ImageSelectDialog extends Dialog implements Handler.Callback {

    private String categoryid = "";
    //活动需要手动传askid
    private String askId = "";
    private int showType;
    private PSGodBaseActivity mContext;

    private List<PhotoItem> mPhotoItems = new ArrayList<>();
    private List<PhotoItem> mDonePhotoItems = new ArrayList<>();
    private RelativeLayout mView;
    private RelativeLayout mArea;
    private RelativeLayout mInputArea;
    private EmptyRecyclerView mImageArea;
    private LinearLayout mPreviewArea;
    private TextView mAlbumTxt;
    private TextView mPhotoTxt;
    private TextView mNumTxt;
    private TextView mSureTxt;
    private TextView mDoneTxt;
    private ImageView mBangpImg;
    private ImageView mImageimg;
    private EditText mEdit;
    private TextView mUpTxt;
    private View mEnpty;

    private MultiImageSelectRecyclerAdapter mAdapter;
    private List<SelectImage> selectResultImages = new ArrayList<SelectImage>();
    private List<SelectImage> images = new ArrayList<SelectImage>();
    private List<SelectImage> originImages = new ArrayList<SelectImage>();

    private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);
    private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);

    public static final int HIDE_INPUT = 1;
    public static final int AREA_SHOW_BANG = 2;
    public static final int AREA_SHOW_IMG = 3;
    public static final int HIDE_DIALOG = 4;

    public static final int SHOW_TYPE_ASK = 0;
    public static final int SHOW_TYPE_REPLY = 1;
    public static final int SHOW_TYPE_ACTIVITY = 2;

    public static final int CAMERA = 500;
    private File cameraImage;

    private int bangType = MultiImageSelectRecyclerAdapter.TYPE_BANG_NOW;

    public ImageSelectDialog(PSGodBaseActivity context, int showType) {
        super(context, R.style.ImageSelectDialog);
        mContext = context;
        this.showType = showType;
    }

    public ImageSelectDialog(PSGodBaseActivity context, String categoryid, int showType) {
        super(context, R.style.ImageSelectDialog);
        this.categoryid = categoryid;
        mContext = context;
        this.showType = showType;
    }

    public ImageSelectDialog(PSGodBaseActivity context, String categoryid, String askId, int showType) {
        super(context, R.style.ImageSelectDialog);
        this.categoryid = categoryid;
        this.askId = askId;
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
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        if(mImageimg.hasOnClickListeners()) {
            mImageimg.callOnClick();
        }
        super.show();
    }

    //设置头部默认选择图片,用于相册选择图片后的回调
    public void setselectResultImages(List<String> selectResultImages) {
        if (this.selectResultImages.size() > 0) {
            this.selectResultImages.clear();
        }
        for (String s : selectResultImages) {
            SelectImage selectImage = new SelectImage(s, "", 0);
            this.selectResultImages.add(selectImage);
        }
        if (images != null && images.size() > 0) {
            images.clear();
        }
        images.addAll(this.selectResultImages);
        images.addAll(originImages);
        int selectLength = this.selectResultImages.size();
        int imageLength = images.size();
        for (int i = 0; i < selectLength; i++) {
            for (int y = selectLength; y < imageLength; y++) {
                if (images.get(y).path.equals(images.get(i).path)) {
                    images.remove(y);
                    imageLength--;
                    y--;
                }
            }
        }
        showPreview();
        mAdapter.setDefaultSelected(this.selectResultImages);
        mAdapter.notifyDataSetChanged();
    }

    public void addCamera() {
        SelectImage image = new SelectImage(cameraImage.getPath(), "", 0);
        selectResultImages.add(0, image);
        images.add(0, image);
        mAdapter.setDefaultSelected(selectResultImages);
        mContext.
                getSupportLoaderManager().initLoader(0, null, mLoaderCallback);
        mAdapter.notifyDataSetChanged();
        showPreview();
        notifySelectNum();
    }

    private void notifySelectNum() {
        mNumTxt.setText(String.valueOf(selectResultImages.size()));
    }

    private void initView() {
        setCanceledOnTouchOutside(true);
        mView = (RelativeLayout) LayoutInflater.from(mContext).
                inflate(R.layout.dialog_image_select, null);
        setContentView(mView);
        mArea = (RelativeLayout) mView.findViewById(R.id.dialog_image_select_area);
        mInputArea = (RelativeLayout) mView.findViewById(R.id.dialog_image_select_input);
        mImageArea = (EmptyRecyclerView) mView.findViewById(R.id.dialog_image_select_imgarea);
        mPreviewArea = (LinearLayout) mView.findViewById(R.id.dialog_image_select_previewarea);
        mAlbumTxt = (TextView) mView.findViewById(R.id.dialog_image_select_album_txt);
        mPhotoTxt = (TextView) mView.findViewById(R.id.dialog_image_select_photo_txt);
        mNumTxt = (TextView) mView.findViewById(R.id.dialog_image_select_num_txt);
        mSureTxt = (TextView) mView.findViewById(R.id.dialog_image_select_sure_txt);
        mDoneTxt = (TextView) mView.findViewById(R.id.dialog_image_select_done_txt);
        mBangpImg = (ImageView) mView.findViewById(R.id.widge_image_select_bangplist_img);
        mImageimg = (ImageView) mView.findViewById(R.id.widge_image_select_image_img);
        mEdit = (EditText) mView.findViewById(R.id.widge_image_select_edit);
        mUpTxt = (TextView) mView.findViewById(R.id.widge_image_select_up);
        mEnpty = mView.findViewById(R.id.dialog_image_select_empty);

        mNumTxt.setText(String.valueOf(selectResultImages.size()));
        mImageArea.setLayoutManager(new LinearLayoutManager(mContext,
                LinearLayoutManager.HORIZONTAL, false));
        mAdapter = new MultiImageSelectRecyclerAdapter(mContext);
        mAdapter.setData(images);
        mAdapter.setBangData(mPhotoItems);
        mAdapter.setDoneBangData(mDonePhotoItems);
        mImageArea.setAdapter(mAdapter);

        switch (showType) {
            case SHOW_TYPE_ASK:
                mBangpImg.setVisibility(View.GONE);
                mPhotoTxt.setVisibility(View.VISIBLE);
                mAdapter.setUploadType(MultiImageSelectRecyclerAdapter.TYPE_ASK);
                break;
            case SHOW_TYPE_REPLY:
                mBangpImg.setVisibility(View.VISIBLE);
                mPhotoTxt.setVisibility(View.GONE);
                mAdapter.setUploadType(MultiImageSelectRecyclerAdapter.TYPE_REPLY);
                break;
            case SHOW_TYPE_ACTIVITY:
                mBangpImg.setVisibility(View.GONE);
                mPhotoTxt.setVisibility(View.GONE);
                mAdapter.setUploadType(MultiImageSelectRecyclerAdapter.TYPE_REPLY);
                break;
        }
    }

    //显示预览
    private void showPreview() {
        if (mPreviewArea.getVisibility() != View.VISIBLE) {
            mPreviewArea.setVisibility(View.VISIBLE);
        }
        mPreviewArea.removeAllViews();
        if (selectResultImages.size() == 0 && mAdapter.getCheckedPhotoItem() == null) {
            mPreviewArea.setVisibility(View.INVISIBLE);
            mImageimg.setImageResource(R.mipmap.bangp_ic_image);
        }else {
            if(selectResultImages.size() == 0) {
                mImageimg.setImageResource(R.mipmap.bangp_ic_image);
            }else{
                mImageimg.setImageResource(R.mipmap.zuopin_ic_image_selected);
            }
            if (mAdapter.getCheckedPhotoItem() != null) {
                ImageView view = new ImageView(mContext);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.
                        LayoutParams(Utils.dpToPx(mContext, 25), Utils.dpToPx(mContext, 25));
                view.setScaleType(ImageView.ScaleType.CENTER_CROP);
                layoutParams.setMargins(Utils.dpToPx(mContext, 6), 0, 0, 0);
                view.setLayoutParams(layoutParams);
                PsGodImageLoader.getInstance().
                        displayImage(mAdapter.getCheckedPhotoItem().getImageURL()
                                , view, Constants.DISPLAY_IMAGE_OPTIONS_SMALL_SMALL);
                view.setOnClickListener(previewBangClick);
                mPreviewArea.addView(view);
            }
            for (SelectImage image : selectResultImages) {
                ImageView view = new ImageView(mContext);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.
                        LayoutParams(Utils.dpToPx(mContext, 25), Utils.dpToPx(mContext, 25));
                view.setScaleType(ImageView.ScaleType.CENTER_CROP);
                layoutParams.setMargins(Utils.dpToPx(mContext, 6), 0, 0, 0);
                view.setLayoutParams(layoutParams);
                PsGodImageLoader.getInstance().
                        displayImage(image.path, view, Constants.DISPLAY_IMAGE_OPTIONS_LOCAL);
                view.setTag(image);
                view.setOnClickListener(previewClick);
                mPreviewArea.addView(view);
            }
        }
    }

    private View.OnClickListener previewBangClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mBangpImg.hasOnClickListeners()) {
                bangType = mAdapter.getCheckBangType();
                mBangpImg.callOnClick();
                mImageArea.getLayoutManager().scrollToPosition(mAdapter.getCheckedPhotoItemNum());
            }
        }
    };

    private View.OnClickListener previewClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SelectImage image = (SelectImage) view.getTag();
            int length = images.size();
            for (int i = 0; i < length; i++) {
                if (images.get(i).path.equals(image.path)) {
                    mImageArea.getLayoutManager().scrollToPosition(i);
                    imageImageClick.onClick(view);
                    return;
                }
            }
        }
    };

    private boolean isInitImages = false;

    View.OnClickListener imageImageClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!isInitImages) {
                ((PSGodBaseActivity) mContext).
                        getSupportLoaderManager().initLoader(0, null, mLoaderCallback);
                isInitImages = true;
            }
            mNumTxt.setVisibility(View.VISIBLE);
            mAlbumTxt.setVisibility(View.VISIBLE);
            mPhotoTxt.setVisibility(View.VISIBLE);
            mDoneTxt.setVisibility(View.GONE);
            if (showType == SHOW_TYPE_ACTIVITY) {
                mPhotoTxt.setVisibility(View.INVISIBLE);
            }
            mSureTxt.setVisibility(View.VISIBLE);
            mNumTxt.setText(String.valueOf(selectResultImages.size()));
            mAdapter.setDefaultSelected(selectResultImages);
            mAdapter.setAdapterType(MultiImageSelectRecyclerAdapter.TYPE_IMAGE);
            mAdapter.notifyDataSetChanged();
//            mEnpty.setVisibility(View.GONE);
            hideInputPanel();
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(150);
                        Message msg = mHandler.obtainMessage(AREA_SHOW_IMG);
                        mHandler.sendMessage(msg);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

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
                mAdapter.setAdapterType(MultiImageSelectRecyclerAdapter.TYPE_BANG);
                mAdapter.setBangType(bangType);
                mAdapter.notifyDataSetChanged();
                mNumTxt.setVisibility(View.INVISIBLE);
                mPhotoTxt.setVisibility(View.INVISIBLE);
                mAlbumTxt.setVisibility(View.INVISIBLE);
                mDoneTxt.setVisibility(View.VISIBLE);
//                if (mPhotoItems.size() == 0) {
//                    mEnpty.setVisibility(View.VISIBLE);
//                } else {
//                    mEnpty.setVisibility(View.GONE);
//                }
                mSureTxt.setVisibility(View.GONE);
                UserPhotoRequest.Builder builder = new UserPhotoRequest.Builder()
                        .setType(UserPhotoRequest.Builder.MY_INPROGRESS).setPage(0).setSize(20)
                        .setListener(refreshListener);
                UserPhotoRequest.Builder doneBuilder = new UserPhotoRequest.Builder()
                        .setType(UserPhotoRequest.Builder.MY_DONE).setPage(0).setSize(20)
                        .setListener(refreshDoneListener);
                if (categoryid != null && !categoryid.equals("")) {
                    builder.setChannelId(categoryid + "");
                    doneBuilder.setChannelId(categoryid + "");
                }
                UserPhotoRequest request = builder.build();
                RequestQueue requestQueue = PSGodRequestQueue.getInstance(mContext)
                        .getRequestQueue();
                requestQueue.add(request);
                requestQueue.add(doneBuilder.build());
                hideInputPanel();
                fixedThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(150);
                            Message msg = mHandler.obtainMessage(AREA_SHOW_BANG);
                            mHandler.sendMessage(msg);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        mImageimg.setOnClickListener(imageImageClick);

        mAlbumTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ImageCategoryDialog(mContext).
                        show(selectResultImages, showType == SHOW_TYPE_ASK ?
                                MultiImageSelectActivity.TYPE_ASK_SELECT :
                                MultiImageSelectActivity.TYPE_REPLY_SELECT);
            }
        });

        mAdapter.setOnImageClickListener(new MultiImageSelectRecyclerAdapter.
                OnImageClickListener() {
            @Override
            public void onImageClick(View view, List<SelectImage> selectImages) {
                mNumTxt.setText(String.valueOf(selectImages.size()));
                selectResultImages.clear();
                selectResultImages.addAll(selectImages);
                showPreview();
            }
        });

        mDoneTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bangType == MultiImageSelectRecyclerAdapter.TYPE_BANG_NOW){
                    bangType = MultiImageSelectRecyclerAdapter.TYPE_BANG_DONE;
                    mDoneTxt.setText("当前任务");
                }else{
                    bangType = MultiImageSelectRecyclerAdapter.TYPE_BANG_NOW;
                    mDoneTxt.setText("历史任务");
                }
                if(mBangpImg.hasOnClickListeners()) {
                    mBangpImg.callOnClick();
                }
            }
        });

        mAdapter.setOnBangClickListener(new MultiImageSelectRecyclerAdapter.
                OnBangClickListener() {
            @Override
            public void onBangClick(View view) {
                showPreview();
            }
        });

        mSureTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击确定时，才填充选择list

            }
        });

        mPhotoTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showType == SHOW_TYPE_ASK) {
                    if (selectResultImages.size() >= 2) {
                        CustomToast.show(mContext, "求p最多只可以发2张~", Toast.LENGTH_LONG);
                        return;
                    }
                } else {
                    if (selectResultImages.size() >= 1) {
                        CustomToast.show(mContext, "作品最多只可以发1张~", Toast.LENGTH_LONG);
                        return;
                    }
                }
                cameraImage = FileUtils.createTmpFile(mContext);
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(mContext.getPackageManager()) != null) {
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(cameraImage));
                    mContext.startActivityForResult(cameraIntent, CAMERA);
                } else {
                    Toast.makeText(mContext, "没有相机", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mUpTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpLoadUtils upLoadUtils = UpLoadUtils.getInstance(mContext);
                if (selectResultImages.size() <= 0) {
                    CustomToast.show(mContext, "最少选择一张图片~", Toast.LENGTH_LONG);
                } else if (mEdit.getText().toString().trim().length() == 0) {
                    CustomToast.show(mContext, "描述不能为空~", Toast.LENGTH_LONG);
                } else {
                    switch (showType) {
                        case SHOW_TYPE_ASK:
                            upLoadUtils.upLoad(mEdit.getText().toString(),
                                    Utils.selectImageToString(selectResultImages),
                                    0, categoryid, UpLoadUtils.TYPE_ASK_UPLOAD);
                            hideInputPanel();
                            fixedThreadPool.execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(200);
                                        mHandler.handleMessage(mHandler.obtainMessage(HIDE_DIALOG));
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            break;
                        case SHOW_TYPE_REPLY:
                            if (mAdapter.getCheckedPhotoItemNum() == -1) {
                                CustomToast.show(mContext, "请选择求p~", Toast.LENGTH_LONG);
                            } else {
                                upLoadUtils.upLoad(mEdit.getText().toString(),
                                        Utils.selectImageToString(selectResultImages),
                                        mAdapter.getCheckedPhotoItem().getAskId(),
                                        categoryid, UpLoadUtils.TYPE_REPLY_UPLOAD);
                                hideInputPanel();
                                fixedThreadPool.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(200);
                                            mHandler.handleMessage(mHandler.obtainMessage(HIDE_DIALOG));
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                            break;
                        case SHOW_TYPE_ACTIVITY:
                            new LoadUtils(mContext).isSimple(true).
                                    setCategory_id(Long.parseLong(categoryid.equals("") ? "0" : categoryid))
                                    .upLoad(1, Long.parseLong(askId.equals("") ? "0" : askId));
                            upLoadUtils.upLoad(mEdit.getText().toString(),
                                    Utils.selectImageToString(selectResultImages),
                                    Long.parseLong(askId), categoryid,
                                    UpLoadUtils.TYPE_ACTIVITY_UPLOAD);
                            hideInputPanel();
                            fixedThreadPool.execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(200);
                                        mHandler.handleMessage(mHandler.obtainMessage(HIDE_DIALOG));
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            break;
                    }
                }
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
            //显示帮p列表
            case AREA_SHOW_BANG:
                mArea.setVisibility(View.VISIBLE);
                break;
            //显示图片列表
            case AREA_SHOW_IMG:
                mArea.setVisibility(View.VISIBLE);
                break;
            case HIDE_DIALOG:
                dismiss();
                break;
        }
        return true;
    }

    private Response.Listener<List<PhotoItem>> refreshListener = new Response.Listener<List<PhotoItem>>() {
        @Override
        public void onResponse(List<PhotoItem> items) {
            mPhotoItems.clear();
            mPhotoItems.addAll(items);
            mImageArea.setEmptyView(mEnpty);
            mAdapter.notifyDataSetChanged();
//            if (mPhotoItems.size() == 0 &&
//                    bangType == MultiImageSelectRecyclerAdapter.TYPE_BANG_NOW) {
//                mEnpty.setVisibility(View.VISIBLE);
//            } else {
//                mEnpty.setVisibility(View.GONE);
//            }
        }
    };

    private Response.Listener<List<PhotoItem>> refreshDoneListener = new Response.Listener<List<PhotoItem>>() {
        @Override
        public void onResponse(List<PhotoItem> items) {
            mDonePhotoItems.clear();
            mDonePhotoItems.addAll(items);
            mImageArea.setEmptyView(mEnpty);
            mAdapter.notifyDataSetChanged();
//            if(bangType == MultiImageSelectRecyclerAdapter.TYPE_BANG_DONE) {
//                if (mDonePhotoItems.size() == 0) {
//                    mEnpty.setVisibility(View.VISIBLE);
//                } else {
//                    mEnpty.setVisibility(View.GONE);
//                }
//            }
        }
    };

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        //判断是否是第一次初始化，如果不是则不需要再finish时填充originImages-+
        private boolean isFirst = true;

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
                    originImages.clear();
                    do {
                        String path = data.getString(data
                                .getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                        String name = data.getString(data
                                .getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                        long dateTime = data.getLong(data
                                .getColumnIndexOrThrow(IMAGE_PROJECTION[2]));

                        SelectImage image = new SelectImage(path, name,
                                dateTime);
                        originImages.add(image);
                    } while (data.moveToNext() && originImages.size() <= 100);
                    if (isFirst) {
                        images.addAll(originImages);
                        mAdapter.notifyDataSetChanged();
                        isFirst = false;
                    }
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
