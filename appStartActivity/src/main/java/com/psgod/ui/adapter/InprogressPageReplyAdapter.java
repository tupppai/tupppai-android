package com.psgod.ui.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.psgod.PsGodImageLoader;
import com.psgod.Constants;
import com.psgod.ImageIOManager;
import com.psgod.R;
import com.psgod.ThreadManager;
import com.psgod.WeakReferenceHandler;
import com.psgod.model.PhotoItem;
import com.psgod.model.User;
import com.psgod.network.request.PhotoRequest;
import com.psgod.network.request.PhotoRequest.ImageInfo;
import com.psgod.ui.activity.MultiImageSelectActivity;
import com.psgod.ui.activity.SinglePhotoDetail;
import com.psgod.ui.widget.AvatarImageView;
import com.psgod.ui.widget.dialog.CarouselPhotoDetailDialog;
import com.psgod.ui.widget.dialog.InprogressShareMoreDialog;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InprogressPageReplyAdapter extends BaseAdapter implements
        Handler.Callback {
    private static final String TAG = InprogressPageReplyAdapter.class
            .getSimpleName();
    public static final byte TYPE_ASK = 1;
    public static final byte TYPE_REPLY = 2;
    private Context mContext;
    private List<PhotoItem> mPhotoItems = new ArrayList<PhotoItem>();

    private ProgressDialog mProgressDialog;
    private InprogressShareMoreDialog inprogressShareDialog;
    // 根据type+id进行下载
    private Long mAskId;
    private Long mPhotoId;
    private String mType = "reply";
    private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);

    private static final int MSG_SUCCESSFUL = 0x4440;
    private static final int MSG_FAILED = 0x4441;

    // UIL配置
    private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS_SMALL;
    private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

    public InprogressPageReplyAdapter(Context context,
                                      List<PhotoItem> photoItems) {
        mContext = context;
        mPhotoItems = photoItems;
    }

    @Override
    public int getCount() {
        return mPhotoItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mPhotoItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static ViewHolder mViewHolder;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final PhotoItem photoItem = mPhotoItems.get(position);

        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.item_inprogress_reply, null);
            mViewHolder.avatarIv = (AvatarImageView) convertView
                    .findViewById(R.id.avatar_imgview);
            mViewHolder.mNicknaemTv = (TextView) convertView
                    .findViewById(R.id.nickname_tv);
            mViewHolder.mTimeTv = (TextView) convertView
                    .findViewById(R.id.item_time);
            mViewHolder.mImageView = (ImageView) convertView
                    .findViewById(R.id.reply_imageview);
            mViewHolder.mAskDesc = (HtmlTextView) convertView
                    .findViewById(R.id.ask_desc_tv);
            mViewHolder.mDownloadIv = (ImageView) convertView
                    .findViewById(R.id.download_iv);
            mViewHolder.mUploadIv = (ImageView) convertView
                    .findViewById(R.id.upload_iv);
            mViewHolder.mChannelName = (TextView) convertView.
                    findViewById(R.id.item_inprogress_reply_channel);
            mViewHolder.mChannelTag = (ImageView) convertView.
                    findViewById(R.id.item_inprogress_reply_tag);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        PsGodImageLoader imageLoader = PsGodImageLoader.getInstance();
        imageLoader.displayImage(photoItem.getAvatarURL(),
                mViewHolder.avatarIv, mAvatarOptions);
        mViewHolder.avatarIv.setUser(new User(photoItem)); // 设置点击头像跳转
        imageLoader.displayImage(photoItem.getImageURL(),
                mViewHolder.mImageView, mOptions);

        mViewHolder.mNicknaemTv.setText(photoItem.getNickname());
        mViewHolder.mTimeTv.setText(photoItem.getUpdateTimeStr());
        mViewHolder.mAskDesc.setHtmlFromString(photoItem.getDesc(),
                true);

        mViewHolder.mDownloadIv.setTag(photoItem.getPid());
        mViewHolder.mDownloadIv.setOnClickListener(downloadListener);

        mViewHolder.mUploadIv.setTag(photoItem);
        mViewHolder.mUploadIv.setOnClickListener(uploadClickListener);

        if(photoItem.getCategoryName() != null && !photoItem.getCategoryName().equals("")){
            mViewHolder.mChannelTag.setVisibility(View.VISIBLE);
            mViewHolder.mChannelName.setVisibility(View.VISIBLE);
            mViewHolder.mChannelName.setText(photoItem.getCategoryName());
        }else{
            mViewHolder.mChannelTag.setVisibility(View.GONE);
            mViewHolder.mChannelName.setVisibility(View.GONE);
        }

        convertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 我的帮p主体均为ask
                if (photoItem.getType() == PhotoItem.TYPE_ASK) {
                    if (photoItem.getReplyCount() == 0) {
                        SinglePhotoDetail.startActivity(mContext, photoItem);
                    } else {
                        new CarouselPhotoDetailDialog(mContext, photoItem.getAskId(), photoItem.getPid()).show();
                    }
                }

            }
        });

        convertView.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View arg0) {
                if (inprogressShareDialog == null) {
                    inprogressShareDialog = new InprogressShareMoreDialog(
                            mContext);
                }
                inprogressShareDialog.setPhotoItem(photoItem,
                        InprogressShareMoreDialog.SHARE_TYPE_REPLY);
                if (inprogressShareDialog.isShowing()) {
                    inprogressShareDialog.dismiss();
                } else {
                    inprogressShareDialog.show(InprogressShareMoreDialog.GONETYPE_DELETE);
                }
                return false;
            }
        });



        return convertView;
    }

    private OnClickListener uploadClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            PhotoItem photoItem = (PhotoItem) v.getTag();
            Intent intent = new Intent(mContext,
                    MultiImageSelectActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("SelectType",
                    MultiImageSelectActivity.TYPE_REPLY_SELECT);
            bundle.putLong("AskId", photoItem.getAskId());
            if (photoItem.getCategoryId() != -1) {
                if(photoItem.getCategoryType().equals("activity")) {
                    bundle.putString(MultiImageSelectActivity.ACTIVITY_ID,
                            String.valueOf(photoItem.getCategoryId()));
                }else if(photoItem.getCategoryType().equals("channel")){
                    bundle.putString(MultiImageSelectActivity.CHANNEL_ID,
                            String.valueOf(photoItem.getCategoryId()));
                }
            }
            intent.putExtras(bundle);
            mContext.startActivity(intent);
        }
    };

    private OnClickListener downloadListener = new OnClickListener() {

        @Override
        public void onClick(final View view) {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(mContext);
            }
            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }

            ThreadManager.executeOnNetWorkThread(new Runnable() {
                @Override
                public void run() {
                    ImageInfo info = PhotoRequest.getImageInfo(mType,
                            (Long) view.getTag());

                    if (!info.isSuccessful) {
                        mHandler.sendEmptyMessage(MSG_FAILED);
                    } else {
                        for(String s : info.urls) {
                            Bitmap image = PhotoRequest.downloadImage(s);
                            String path = ImageIOManager.getInstance().saveImage(
                                    String.valueOf(view.getTag()), image);

                            // 更新相册后通知系统扫描更新
                            Intent intent = new Intent(
                                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            Uri uri = Uri.fromFile(new File(path));
                            intent.setData(uri);
                            mContext.sendBroadcast(intent);

                            Message msg = mHandler.obtainMessage(MSG_SUCCESSFUL);
                            msg.obj = path;
                            msg.sendToTarget();
                        }
                    }
                }
            });
        }
    };

    private static class ViewHolder {
        AvatarImageView avatarIv;
        TextView mNicknaemTv;
        TextView mTimeTv;
        ImageView mImageView;
        HtmlTextView mAskDesc;
        ImageView mDownloadIv;
        ImageView mUploadIv;
        ImageView mChannelTag;
        TextView mChannelName;
    }

    @Override
    public boolean handleMessage(Message msg) {
        mProgressDialog.dismiss();
        switch (msg.what) {
            case MSG_SUCCESSFUL:
                String path = (String) msg.obj;
                Toast.makeText(mContext, "素材保存到" + path, Toast.LENGTH_SHORT).show();
                break;
            case MSG_FAILED:
                // TODO 获取图片信息失败
                Toast.makeText(mContext, "下载素材失败", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

}
