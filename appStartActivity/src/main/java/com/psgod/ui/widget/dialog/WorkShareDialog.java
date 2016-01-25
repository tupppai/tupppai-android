package com.psgod.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.psgod.Constants;
import com.psgod.PsGodImageLoader;
import com.psgod.R;
import com.psgod.model.PhotoItem;
import com.psgod.ui.widget.ShareButton;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;

/**
 * Created by Administrator on 2016/1/21 0021.
 */
public class WorkShareDialog extends Dialog {
    public WorkShareDialog(Context context) {
        super(context, R.style.ActionSheetDialog);
        initView();
    }

    public WorkShareDialog(Context context, int theme) {
        super(context, theme);
        initView();
    }

    protected WorkShareDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        initView();
    }

    private View mParent;

    private ImageView mImage;
    private TextView mImageTitle;
    private ShareButton mShareWeibo;
    private ShareButton mShareQQ;
    private ShareButton mShareQZone;
    private ShareButton mShareWeChatM;
    private ShareButton mShareWeChatF;
    private TextView mCancel;

    private PhotoItem mPhotoItem;

    private OnEndListener onEndListener;

    public void setOnEndListener(OnEndListener onEndListener) {
        this.onEndListener = onEndListener;
    }

    private void initView() {
        mParent = LayoutInflater.from(getContext()).inflate(R.layout.dialog_work_share, null);
        setContentView(mParent);

        mImage = (ImageView) findViewById(R.id.work_share_image);
        mImageTitle = (TextView) findViewById(R.id.work_share_image_title);
        mShareQQ = (ShareButton) findViewById(R.id.work_share_qq);
        mShareQQ.setShareType(ShareButton.TYPE_QQ);
        mShareQZone = (ShareButton) findViewById(R.id.work_share_qzone);
        mShareQZone.setShareType(ShareButton.TYPE_QZONE);
        mShareWeibo = (ShareButton) findViewById(R.id.work_share_weibo);
        mShareWeibo.setShareType(ShareButton.TYPE_WEIBO);
        mShareWeChatM = (ShareButton) findViewById(R.id.work_share_wechatm);
        mShareWeChatM.setShareType(ShareButton.TYPE_WECHAT_MOMENTS);
        mShareWeChatF = (ShareButton) findViewById(R.id.work_share_wechatf);
        mShareWeChatF.setShareType(ShareButton.TYPE_WECHAT_FRIEND);
        mCancel = (TextView) findViewById(R.id.work_share_cancel);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        setCanceledOnTouchOutside(true);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (onEndListener != null) {
            onEndListener.onEnd();
        }
    }

    public void setPhotoItem(PhotoItem photoItem) {
        this.mPhotoItem = photoItem;

        mShareQQ.setPhotoItem(mPhotoItem);
        mShareQQ.setOnShareListener(onShareListener);
        mShareQZone.setPhotoItem(mPhotoItem);
        mShareQZone.setOnShareListener(onShareListener);
        mShareWeibo.setPhotoItem(mPhotoItem);
        mShareWeibo.setOnShareListener(onShareListener);
        mShareWeChatM.setPhotoItem(mPhotoItem);
        mShareWeChatM.setOnShareListener(onShareListener);
        mShareWeChatF.setPhotoItem(mPhotoItem);
        mShareWeChatF.setOnShareListener(onShareListener);

        PsGodImageLoader.getInstance().displayImage(mPhotoItem.getImageURL(), mImage,
                Constants.DISPLAY_IMAGE_OPTIONS_SMALL);
        mImageTitle.setText(mPhotoItem.getTitle());
    }

    private ShareButton.OnShareListener onShareListener = new ShareButton.OnShareListener() {
        @Override
        public void onError(Platform platform, int arg1, Throwable arg2) {

        }

        @Override
        public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
            dismiss();
        }

        @Override
        public void onCancel(Platform arg0, int arg1) {

        }
    };

    public interface OnEndListener {
        void onEnd();
    }
}
