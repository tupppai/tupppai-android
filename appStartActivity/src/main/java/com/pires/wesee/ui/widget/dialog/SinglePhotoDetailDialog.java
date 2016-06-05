package com.pires.wesee.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nineoldandroids.view.ViewHelper;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.pires.wesee.BitmapUtils;
import com.pires.wesee.PsGodImageLoader;
import com.pires.wesee.ThreadManager;
import com.pires.wesee.model.PhotoItem;
import com.pires.wesee.ui.widget.AvatarImageView;
import com.pires.wesee.Constants;
import com.pires.wesee.CustomToast;
import com.pires.wesee.ImageIOManager;
import com.pires.wesee.R;
import com.pires.wesee.Utils;
import com.pires.wesee.WeakReferenceHandler;
import com.pires.wesee.eventbus.RefreshEvent;
import com.pires.wesee.model.User;
import com.pires.wesee.network.request.PhotoRequest;
import com.pires.wesee.ui.activity.SinglePhotoDetail;
import com.pires.wesee.ui.view.LikeView;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator
 * 单图详情弹窗
 */
public class SinglePhotoDetailDialog extends Dialog implements Handler.Callback {

    private Context mContext;
    private PhotoItem mPhotoItem;

    private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);

    public SinglePhotoDetailDialog(Context context, PhotoItem photoItem) {
        super(context, R.style.ActionSheetDialog);
        mContext = context;
        mPhotoItem = photoItem;
    }

    public SinglePhotoDetailDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
    }

    View parentView;
    View parnet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCanceledOnTouchOutside(true);
        initView();
        initListener();
    }

    private ImageView coverTag;
    private TextView coverName;
    private TextView coverTime;
    private AvatarImageView coverAvatar;
    private ImageView coverCover;
    private ImageView coverBack;
    private FrameLayout coverImgArea;
    private ImageView coverBang;
    private LikeView coverLike;
    private TextView downloadTxt;
    private TextView detailTxt;

    private void initView() {
        parentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_single_photo_detail, null);
        setContentView(parentView);

        parnet = parentView.findViewById(R.id.view_single_photo_detail_coverview);
        coverTag = (ImageView) parentView.findViewById(R.id.view_single_photo_detail_cover_tag);
        coverName = (TextView) parentView.findViewById(R.id.view_single_photo_detail_cover_name);
        coverTime = (TextView) parentView.findViewById(R.id.view_single_photo_detail_cover_time);
        coverAvatar = (AvatarImageView) parentView.findViewById(R.id.view_single_photo_detail_cover_avatar);
        coverBack = (ImageView) parentView.findViewById(R.id.view_single_photo_detail_cover_backimg);
        coverImgArea = (FrameLayout) parentView.findViewById(R.id.view_single_photo_detail_cover_imgarea);
        coverBang = (ImageView) parentView.findViewById(R.id.view_single_photo_detail_cover_bang);
        coverLike = (LikeView) parentView.findViewById(R.id.view_single_photo_detail_cover_like);
        downloadTxt = (TextView) parentView.findViewById(R.id.view_single_photo_detail_cover_download);
        detailTxt = (TextView) parentView.findViewById(R.id.view_single_photo_detail_cover_detail);

        if (mPhotoItem.getType() == 1) {
            coverTag.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.tag));
        } else {
            coverTag.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.tag_zuopin));
        }

        coverName.setText(mPhotoItem.getNickname());
        coverTime.setText(mPhotoItem.getUpdateTimeStr());
        PsGodImageLoader.getInstance().displayImage(mPhotoItem.getAvatarURL(), coverAvatar,
                Constants.DISPLAY_IMAGE_OPTIONS_AVATAR);
        coverAvatar.setUser(new User(mPhotoItem));

        coverCover = new ImageView(mContext);
        coverCover.setLayoutParams
                (new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        PsGodImageLoader.getInstance().displayImage(mPhotoItem.getImageURL(), coverCover,
                Constants.DISPLAY_IMAGE_OPTIONS, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {

                    }

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
//                        coverBack.setImageBitmap();
                        BitmapUtils.setBlurBitmap(bitmap, coverBack, s);
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {

                    }
                });
        coverImgArea.addView(coverCover);
        coverLike.setmPhotoItem(mPhotoItem);
        coverLike.updateLikeView();
        if (mPhotoItem.getType() == 1) {
            coverLike.setVisibility(View.GONE);
            coverBang.setVisibility(View.VISIBLE);
        } else {
            coverLike.setVisibility(View.VISIBLE);
            coverBang.setVisibility(View.GONE);
        }
        if (isOwn) {
            detailTxt.setVisibility(View.GONE);
        } else {
            detailTxt.setVisibility(View.VISIBLE);
        }
    }

    private void initListener() {
        parentView.
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismiss();
                    }
                });

        coverBang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PSDialog psDialog = new PSDialog(mContext);
                psDialog.setPhotoItem(mPhotoItem);
                psDialog.show();
            }
        });

        detailTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SinglePhotoDetail.startActivity(mContext, mPhotoItem);
                dismiss();
            }
        });

        downloadTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ThreadManager.executeOnNetWorkThread(new Runnable() {
                    @Override
                    public void run() {
                        String s = mPhotoItem.getImageURL();
                        String[] thumbs = s.indexOf("?") != -1 ?
                                s.split("\\?")[0].split("/") : s.split("/");
                        String name;
                        name = thumbs[thumbs.length - 1];
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
                                .obtainMessage(0);
                        msg.obj = path;
                        msg.sendToTarget();
                    }
                });
            }
        });

        parnet.setOnTouchListener(new View.OnTouchListener() {
            float downY;
            float moveY = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downY = motionEvent.getRawY();
                        moveY = 0;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        moveY = motionEvent.getRawY() - downY;
                        ViewHelper.setTranslationY(view, moveY);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (Math.abs(moveY) > Utils.dpToPx(mContext, 50)) {
                            dismiss();
                        } else {
                            ViewHelper.setTranslationY(view, 0);
                        }
                        break;
                }
                return true;
            }
        });
    }


    @Override
    public void show() {
        super.show();
        getWindow().getAttributes().width = Utils.getScreenWidthPx(mContext)
                - Utils.dpToPx(mContext, 30);
        getWindow().getAttributes().height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setGravity(Gravity.CENTER);
        getWindow().setWindowAnimations(R.style.popwindow_anim_style);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        EventBus.getDefault().post(new RefreshEvent(SinglePhotoDetail.class.getName()));
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case 0:
                CustomToast.
                        show(mContext, "下载图片成功", Toast.LENGTH_SHORT);
                break;
        }
        return true;
    }

    private boolean isOwn = false;

    public SinglePhotoDetailDialog setIsOwn(boolean isOwn) {
        this.isOwn = isOwn;
        return this;
    }
}
