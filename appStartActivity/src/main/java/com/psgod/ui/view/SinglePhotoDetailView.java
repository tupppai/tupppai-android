package com.psgod.ui.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.psgod.BitmapUtils;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.PhotoItem;
import com.psgod.ui.activity.PhotoBrowserActivity;
import com.psgod.ui.activity.SinglePhotoDetail;
import com.psgod.ui.widget.AvatarImageView;
import com.psgod.ui.widget.dialog.PSDialog;
import com.psgod.ui.widget.dialog.ShareMoreDialog;

import org.w3c.dom.Text;

/**
 * Created by Administrator on 2015/12/2 0002.
 */
public class SinglePhotoDetailView extends RelativeLayout {
    public SinglePhotoDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SinglePhotoDetailView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public SinglePhotoDetailView(Context context, PhotoItem photoItem) {
        super(context);
        this.mPhotoItem = photoItem;
        init();
    }


    public void setPhotoItem(PhotoItem photoItem) {
        this.mPhotoItem = photoItem;
    }

    private PhotoItem mPhotoItem;
    private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS;
    private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_single_photo_detail, null);
        initView(view);
        initListener();
        addView(view);
    }

    FollowView follow;
    AvatarImageView avatar;
    TextView name;
    TextView time;
    LinearLayout imgMutl;
    ImageView imgLeft;
    ImageView imgRight;
    FrameLayout imgSingle;
    ImageView imgBack;
    TextView desc;
    ImageView shareImg;
    TextView shareTxt;
    ImageView commentImg;
    TextView commentTxt;
    ImageView bang;
    LikeView  like;



    private void initView(View view) {
        follow = (FollowView) view.findViewById(R.id.single_photo_detail_follow);
        avatar = (AvatarImageView) view.findViewById(R.id.single_photo_detail_avatar);
        name = (TextView) view.findViewById(R.id.single_photo_detail_name);
        time = (TextView) view.findViewById(R.id.single_photo_detail_time);
        imgMutl = (LinearLayout) view.findViewById(R.id.single_photo_detail_img_mult);
        imgLeft = (ImageView) view.findViewById(R.id.single_photo_detail_img_left);
        imgRight = (ImageView) view.findViewById(R.id.single_photo_detail_img_right);
        imgSingle = (FrameLayout) view.findViewById(R.id.single_photo_detail_img_single);
        imgBack = (ImageView) view.findViewById(R.id.single_photo_detail_img_back);
        desc = (TextView) view.findViewById(R.id.single_photo_detail_desc);
        shareImg = (ImageView) view.findViewById(R.id.single_photo_detail_share_img);
        shareTxt = (TextView) view.findViewById(R.id.single_photo_detail_share_txt);
        commentImg = (ImageView) view.findViewById(R.id.single_photo_detail_comment_img);
        commentTxt = (TextView) view.findViewById(R.id.single_photo_detail_comment_txt);
        bang = (ImageView) view.findViewById(R.id.single_photo_detail_bang);
        like = (LikeView) view.findViewById(R.id.single_photo_detail_like);

        follow.setPhotoItem(mPhotoItem);
        follow.updateFollowView();
        ImageLoader.getInstance().displayImage(mPhotoItem.getAvatarURL(), avatar, mAvatarOptions);
        name.setText(mPhotoItem.getNickname());
        time.setText(mPhotoItem.getUpdateTimeStr());
        initImg();
        desc.setText(mPhotoItem.getDesc());
        shareTxt.setText(String.valueOf(mPhotoItem.getShareCount()));
        commentTxt.setText(String.valueOf(mPhotoItem.getCommentCount()));
        like.setmPhotoItem(mPhotoItem);
        like.updateLikeView();
        if (mPhotoItem.getType() == 1) {
            bang.setVisibility(VISIBLE);
            like.setVisibility(GONE);
        } else {
            bang.setVisibility(GONE);
            like.setVisibility(VISIBLE);
        }
    }

    private OnClickListener shareClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            ShareMoreDialog shareMoreDialog = new ShareMoreDialog(getContext());
            shareMoreDialog.setPhotoItem(mPhotoItem);
            shareMoreDialog.show();
        }
    };

    private void initListener() {
        shareTxt.setOnClickListener(shareClick);
        shareImg.setOnClickListener(shareClick);
        bang.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                PSDialog dialog = new PSDialog(getContext());
                dialog.setPhotoItem(mPhotoItem);
                dialog.show();
            }
        });
    }

    private OnClickListener imgClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getContext(), PhotoBrowserActivity.class);
            intent.putExtra(Constants.IntentKey.PHOTO_PATH, view.getTag()
                    .toString());
            intent.putExtra(Constants.IntentKey.ASK_ID, mPhotoItem.getAskId());
            intent.putExtra(Constants.IntentKey.PHOTO_ITEM_ID,
                    mPhotoItem.getPid());
            intent.putExtra(Constants.IntentKey.PHOTO_ITEM_TYPE,
                    (mPhotoItem.getType() == 1) ? "ask" : "reply");
            getContext().startActivity(intent);
        }
    };

    private void initImg() {
        if (mPhotoItem.getUploadImagesList().size() == 1) {
            imgMutl.setVisibility(INVISIBLE);
            imgSingle.setVisibility(VISIBLE);
            imgBack.setTag(mPhotoItem.getImageURL());
            imgBack.setOnClickListener(imgClick);
            imgBack.setOnLongClickListener(imageOnLongClickListener);
            ImageView imgCover = new ImageView(getContext());
            imgCover.setOnClickListener(imgClick);
            imgCover.setOnLongClickListener(imageOnLongClickListener);
            imgCover.setTag(mPhotoItem.getImageURL());
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            imgCover.setLayoutParams(params);
            imgSingle.addView(imgCover);
            ImageLoader.getInstance().displayImage(mPhotoItem.getImageURL(), imgCover, mOptions, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {

                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    imgBack.setImageBitmap(BitmapUtils.getBlurBitmap(bitmap));
                }

                @Override
                public void onLoadingCancelled(String s, View view) {

                }
            });
        } else if (mPhotoItem.getUploadImagesList().size() == 2) {
            imgMutl.setVisibility(VISIBLE);
            imgSingle.setVisibility(INVISIBLE);
            ImageLoader.getInstance().displayImage(mPhotoItem.getUploadImagesList().get(0).mImageUrl, imgLeft, mOptions);
            ImageLoader.getInstance().displayImage(mPhotoItem.getUploadImagesList().get(1).mImageUrl, imgRight, mOptions);
            imgLeft.setTag(mPhotoItem.getUploadImagesList().get(0).mImageUrl);
            imgRight.setTag(mPhotoItem.getUploadImagesList().get(1).mImageUrl);
            imgRight.setOnClickListener(imgClick);
            imgRight.setOnLongClickListener(imageOnLongClickListener);
            imgLeft.setOnClickListener(imgClick);
            imgLeft.setOnLongClickListener(imageOnLongClickListener);
        }
    }

    public void updateCommentView() {
//        if(mPhotoItem != null && mPhotoItem.getCommentContent() != null) {
//            String textCommentCount = Utils.getCountDisplayText(mPhotoItem
//                    .getCommentCount());

        commentTxt.setText(String.valueOf(mPhotoItem.getCommentCount()));
//        }
    }

    ShareMoreDialog mShareMoreDialog;

    private OnLongClickListener imageOnLongClickListener = new OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            if (mShareMoreDialog == null) {
                mShareMoreDialog = new ShareMoreDialog(getContext());
            }
            mShareMoreDialog.setPhotoItem(mPhotoItem);
            if (mShareMoreDialog.isShowing()) {
                mShareMoreDialog.dismiss();
            } else {
                mShareMoreDialog.show();
            }
            return true;
        }
    };

    public TextView getRecentPhotoDetailCommentBtn() {
        return commentTxt;
    }
}
