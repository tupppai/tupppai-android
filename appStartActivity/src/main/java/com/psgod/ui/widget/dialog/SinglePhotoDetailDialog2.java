package com.psgod.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nineoldandroids.view.ViewHelper;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.psgod.BitmapUtils;
import com.psgod.Constants;
import com.psgod.CustomToast;
import com.psgod.ImageIOManager;
import com.psgod.PsGodImageLoader;
import com.psgod.R;
import com.psgod.ThreadManager;
import com.psgod.Utils;
import com.psgod.WeakReferenceHandler;
import com.psgod.eventbus.RefreshEvent;
import com.psgod.model.PhotoItem;
import com.psgod.model.SinglePhotoItem;
import com.psgod.model.User;
import com.psgod.network.request.PhotoRequest;
import com.psgod.ui.activity.SinglePhotoDetail;
import com.psgod.ui.adapter.ViewPagerAdapter;
import com.psgod.ui.view.LikeView;
import com.psgod.ui.widget.AvatarImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2015/11/26 0026.
 * ViewPager版
 */
public class SinglePhotoDetailDialog2 extends Dialog implements Handler.Callback {

    private Context mContext;
    private SinglePhotoItem mSinglePhotoItem;
    private int num = -1;
    private long pid = -1;
    private int showNum;

    private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);

    public SinglePhotoDetailDialog2(Context context, SinglePhotoItem singlePhotoItem) {
        super(context, R.style.ActionSheetDialog);
        mContext = context;
        mSinglePhotoItem = singlePhotoItem;
    }

    public SinglePhotoDetailDialog2(Context context, SinglePhotoItem singlePhotoItem, int num) {
        super(context, R.style.ActionSheetDialog);
        mContext = context;
        mSinglePhotoItem = singlePhotoItem;
        this.num = num;
    }

    public SinglePhotoDetailDialog2(Context context, SinglePhotoItem singlePhotoItem, long pid) {
        super(context, R.style.ActionSheetDialog);
        mContext = context;
        mSinglePhotoItem = singlePhotoItem;
        this.pid = pid;
    }


    public SinglePhotoDetailDialog2(Context context, int theme) {
        super(context, theme);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView() {
        if (num != -1) {
            showNum = num;
        }
        if (mSinglePhotoItem == null || mSinglePhotoItem.getPhotoItem() == null ||
                mSinglePhotoItem.getReplyPhotoItems() == null) {
            return;
        }
        RelativeLayout layout = new RelativeLayout(mContext);
        ViewGroup.LayoutParams lParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(lParams);
        ViewPager vp = new ViewPager(mContext);
        layout.addView(vp);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        vp.setLayoutParams(params);
//        vp.setPadding(Utils.dpToPx(mContext, 15), 0, Utils.dpToPx(mContext, 15), 0);
        setContentView(layout);
        List<View> views = new ArrayList<>();
        int askLength = mSinglePhotoItem.getAskPhotoItems().getUploadImagesList().size();
        for (int i = 0; i < askLength && !mSinglePhotoItem.getPhotoItem().getIsHomework(); i++) {
            ContentView view = new
                    ContentView(mContext, mSinglePhotoItem.getAskPhotoItems(),
                    mSinglePhotoItem.getAskPhotoItems().getUploadImagesList().get(i).mImageUrl);
            view.setGravity(Gravity.CENTER);
            if (mSinglePhotoItem.getAskPhotoItems().getPid() == mSinglePhotoItem.getPhotoItem().getPid()) {
                view.setIsOwn(true);
            }
            views.add(view);
        }
        int replyLength = mSinglePhotoItem.getReplyPhotoItems().size();
        for (int i = 0; i < replyLength; i++) {
            ContentView view = new
                    ContentView(mContext, mSinglePhotoItem.getReplyPhotoItems().get(i));
            view.setGravity(Gravity.CENTER);
            if (mSinglePhotoItem.getReplyPhotoItems().get(i).getPid()
                    == pid) {
                view.setIsOwn(true);
                showNum = i + askLength;
            }
            views.add(view);
        }
        ViewPagerAdapter adapter = new ViewPagerAdapter(views);
        vp.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        vp.setCurrentItem(showNum);
    }

    private class ContentView extends RelativeLayout {

//        ContentView(PhotoItem photoItem) {
//            super();
//        }
//
//        ContentView(PhotoItem photoItem, String url) {
//            mPhotoItem = photoItem;
//            this.url = url;
//            initView();
//            initListener();
//        }


        public ContentView(Context context, PhotoItem photoItem) {
            super(context);
            mPhotoItem = photoItem;
            initView();
            initListener();
        }

        public ContentView(Context context, PhotoItem photoItem, String url) {
            super(context);
            mPhotoItem = photoItem;
            this.url = url;
            initView();
            initListener();
        }

        public ContentView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public ContentView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        private PhotoItem mPhotoItem;
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
        private View parentView;
        private View parnet;
        private String url;

        private void initView() {
            setPadding(Utils.dpToPx(mContext, 15), 0, Utils.dpToPx(mContext, 15), 0);
            parentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_single_photo_detail, null);
            addView(parentView);
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
            PsGodImageLoader.getInstance().displayImage(this.url == null || this.url.trim().equals("") ? mPhotoItem.getImageURL()
                            : url, coverCover,
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
        }

        private void initListener() {
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

            parentView.setOnTouchListener(new View.OnTouchListener() {
                float downY;
                float leftX;
                float moveY = 0;
                float moveX = 0;

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            downY = motionEvent.getRawY();
                            leftX = motionEvent.getRawX();
                            moveY = 0;
                            moveX = 0;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            moveY = motionEvent.getRawY() - downY;
                            moveX = motionEvent.getRawX() - leftX;
                            if (Math.abs(moveX) > Utils.dpToPx(mContext, 7) &&
                                    Math.abs(moveY) < Utils.dpToPx(mContext, 13)) {
                                ViewHelper.setTranslationY(ContentView.this, 0);

                            } else {
                                ViewHelper.setTranslationY(ContentView.this, moveY);

                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            if (Math.abs(moveY) > Utils.dpToPx(mContext, 50)) {
                                dismiss();
                            } else {
                                ViewHelper.setTranslationY(ContentView.this, 0);
                            }
                            break;
                    }
                    return true;
                }
            });
        }

        private boolean isOwn = false;

        public void setIsOwn(boolean isOwn) {
            this.isOwn = isOwn;
            if (isOwn) {
                detailTxt.setVisibility(View.GONE);
            } else {
                detailTxt.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    public void show() {
        super.show();
//        getWindow().getAttributes().width = Utils.getScreenWidthPx(mContext)
//                - Utils.dpToPx(mContext, 30);
        getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
//        getWindow().getAttributes().height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().getAttributes().height = WindowManager.LayoutParams.WRAP_CONTENT;
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

}
