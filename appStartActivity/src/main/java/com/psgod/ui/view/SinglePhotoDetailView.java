package com.psgod.ui.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.psgod.PsGodImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.psgod.BitmapUtils;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.ImageData;
import com.psgod.model.PhotoItem;
import com.psgod.model.SinglePhotoItem;
import com.psgod.model.User;
import com.psgod.ui.activity.CourseDetailActivity;
import com.psgod.ui.activity.PhotoBrowserActivity;
import com.psgod.ui.adapter.SingleImgListAdapter;
import com.psgod.ui.widget.AvatarImageView;

import com.psgod.ui.widget.FollowImage;
import com.psgod.ui.widget.dialog.PSDialog;
import com.psgod.ui.widget.dialog.ShareMoreDialog;
import com.psgod.ui.widget.dialog.SinglePhotoDetailDialog;
import com.psgod.ui.widget.dialog.SinglePhotoDetailDialog2;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 2015/12/2 0002.
 * SinglePhotoDetail（Acticity）使用的单图区域
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

    public SinglePhotoDetailView(Context context, SinglePhotoItem singlePhotoItem) {
        super(context);
        this.mSinglePhotoItem = singlePhotoItem;
        this.mPhotoItem = singlePhotoItem.getPhotoItem();
        this.mAskPhotoItems = singlePhotoItem.getAskPhotoItems();
        this.mReplyPhotoItems = singlePhotoItem.getReplyPhotoItems();
        init();
    }


    public void setPhotoItem(PhotoItem photoItem) {
        this.mPhotoItem = photoItem;
    }

    private SinglePhotoItem mSinglePhotoItem;
    private PhotoItem mPhotoItem;
    PhotoItem mAskPhotoItems;
    List<PhotoItem> mReplyPhotoItems;
    private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS;
    private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_single_photo_detail, null);
        initView(view);
        initListener();
        addView(view);
    }

    private FollowImage follow;
    private AvatarImageView avatar;
    private TextView name;
    private TextView time;
    private LinearLayout imgMutl;
    private ImageView imgLeft;
    private ImageView imgRight;
    private FrameLayout imgSingle;
    private ImageView imgBack;
    private TextView desc;
    private ImageView shareImg;
    private TextView shareTxt;
    private ImageView commentImg;
    private TextView commentTxt;
    private ImageView bang;
    private LikeView like;

    private RelativeLayout headArea;
    private RelativeLayout imgListArea;
    private LinearLayout imgListAsk;
    private RecyclerView imgListReply;
    private SingleImgListAdapter imgListAdapter;

    private FollowImage.OnFollowChangeListener onFollowChangeListener;

    public void setOnFollowChangeListener(FollowImage.OnFollowChangeListener onFollowChangeListener) {
        this.onFollowChangeListener = onFollowChangeListener;
        if (follow != null) {
            follow.setOnFollowChangeListener(onFollowChangeListener);
        }
    }

    private void initView(View view) {
        follow = (FollowImage) view.findViewById(R.id.single_photo_detail_follow);
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

        imgListArea = (RelativeLayout) view.findViewById(R.id.single_photo_detail_imglist_area);
        imgListAsk = (LinearLayout) view.findViewById(R.id.single_photo_detail_imglist_ask);
        imgListReply = (RecyclerView) view.findViewById(R.id.single_photo_detail_imglist_reply);
        imgListReply.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));
        imgListReply.addItemDecoration(new SpaceItemDecoration(
                Utils.dpToPx(getContext(), 5)));
        initImgListArea();

        headArea = (RelativeLayout) view.findViewById(R.id.single_photo_detail_head);
        ViewGroup.LayoutParams backParams = headArea.getLayoutParams();
        backParams.width = Utils.getScreenWidthPx(getContext());
        headArea.setLayoutParams(backParams);
        // 同时设置margin和padding才有效果，不知为何
//        RelativeLayout.LayoutParams followLayoutParams = (RelativeLayout.LayoutParams) follow.getLayoutParams();
//        followLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//        followLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
//        followLayoutParams.setMargins(0, 0, Utils.dpToPx(getContext(), 10), 0);
//        follow.setLayoutParams(followLayoutParams);
//        follow.setPadding(0,0,Utils.dpToPx(getContext(),10),0);

        if (mPhotoItem != null) {
            avatar.setUser(new User(mPhotoItem));
            PsGodImageLoader.getInstance().displayImage(mPhotoItem.getAvatarURL(), avatar, mAvatarOptions);
            name.setText(mPhotoItem.getNickname());
            time.setText(mPhotoItem.getUpdateTimeStr());
            initImg();
            desc.setText(mPhotoItem.getDesc());
            initVariable();
            like.updateLikeView();
            if (mPhotoItem.getType() == 1) {
                bang.setVisibility(VISIBLE);
                like.setVisibility(GONE);
            } else {
                bang.setVisibility(GONE);
                like.setVisibility(VISIBLE);
            }
        }
    }

    private void initImgListArea() {
        if (mAskPhotoItems != null && mAskPhotoItems.getUploadImagesList().size() > 0
                && mReplyPhotoItems != null && mReplyPhotoItems.size() > 0) {
            imgListArea.setVisibility(VISIBLE);
            imgListAsk.removeAllViews();
            if (mPhotoItem.getIsHomework()) {
                String image = mAskPhotoItems.getImageURL();
                ImageView imageView = new ImageView(getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        Utils.dpToPx(getContext(), 45), ViewGroup.LayoutParams.MATCH_PARENT
                );
                params.weight = 1;
                params.setMargins(0, 0, Utils.dpToPx(getContext(), 5), 0);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setLayoutParams(params);
                PsGodImageLoader.getInstance().displayImage(image, imageView,
                        Constants.DISPLAY_IMAGE_OPTIONS_SMALL_SMALL);
                imageView.setTag(R.id.image_url, image);
                imageView.setOnClickListener(homeworkImgClick);
                imgListAsk.addView(imageView);
            } else {
                for (int i = 0 ; i < mAskPhotoItems.getUploadImagesList().size() ; i ++ ) {
                    ImageData image = mAskPhotoItems.getUploadImagesList().get(i);
                    ImageView imageView = new ImageView(getContext());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            Utils.dpToPx(getContext(), 45), ViewGroup.LayoutParams.MATCH_PARENT
                    );
                    params.weight = 1;
                    params.setMargins(0, 0, Utils.dpToPx(getContext(), 5), 0);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setLayoutParams(params);
                    PsGodImageLoader.getInstance().displayImage(image.mImageUrl, imageView,
                            Constants.DISPLAY_IMAGE_OPTIONS_SMALL_SMALL);
                    imageView.setTag(R.id.image_url, i);
                    imageView.setOnClickListener(askImgClick);
                    imgListAsk.addView(imageView);
                }
            }
            imgListAdapter = new SingleImgListAdapter(getContext(), mSinglePhotoItem);
            imgListAdapter.setOwnUrl(mPhotoItem.getImageURL());
            imgListReply.setAdapter(imgListAdapter);
        } else {
            imgListArea.setVisibility(GONE);
        }
    }

    OnClickListener askImgClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Integer i = (Integer) view.getTag(R.id.image_url);
//            boolean isOwn = mPhotoItem.getType() == PhotoItem.TYPE_ASK;
//            mAskPhotoItems.setImageURL(url);
//            new SinglePhotoDetailDialog(getContext(), mAskPhotoItems)
//                    .setIsOwn(isOwn).show();
            new SinglePhotoDetailDialog2(getContext(),mSinglePhotoItem,i).show();
        }
    };

    OnClickListener homeworkImgClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getContext(), CourseDetailActivity.class);
            intent.putExtra("id", mPhotoItem.getAskId());
            getContext().startActivity(intent);
        }
    };

    public void refreshPhotoItem(PhotoItem photoItem) {
        mPhotoItem = photoItem;
        initVariable();
    }

    private void initVariable() {
        follow.setPhotoItem(mPhotoItem);

        shareTxt.setText(String.valueOf(mPhotoItem.getShareCount()));
        commentTxt.setText(String.valueOf(mPhotoItem.getCommentCount()));
        like.setmPhotoItem(mPhotoItem);
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
        if (mPhotoItem.getUploadImagesList().size() == 1 || mPhotoItem.getType() == PhotoItem.TYPE_REPLY) {
            imgMutl.setVisibility(GONE);
            imgSingle.setVisibility(VISIBLE);
            imgBack.setTag(mPhotoItem.getImageURL());
            imgBack.setOnClickListener(imgClick);
            imgBack.setOnLongClickListener(imageOnLongClickListener);
            ImageView imgCover = new ImageView(getContext());
            imgCover.setOnClickListener(imgClick);
            imgCover.setOnLongClickListener(imageOnLongClickListener);
            imgCover.setTag(mPhotoItem.getImageURL());
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT);
            imgCover.setLayoutParams(params);
            imgSingle.addView(imgCover);
            PsGodImageLoader.getInstance().displayImage(mPhotoItem.getImageURL()
                    , imgCover, mOptions, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {

                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    BitmapUtils.setBlurBitmap(bitmap, imgBack, s);
                }

                @Override
                public void onLoadingCancelled(String s, View view) {

                }
            });
        } else if (mPhotoItem.getUploadImagesList().size() == 2) {
            imgMutl.setVisibility(VISIBLE);
            imgSingle.setVisibility(GONE);
            PsGodImageLoader.getInstance().
                    displayImage(mPhotoItem.getUploadImagesList().get(0).mImageUrl, imgLeft, mOptions);
            PsGodImageLoader.getInstance().
                    displayImage(mPhotoItem.getUploadImagesList().get(1).mImageUrl, imgRight, mOptions);
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

    public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

        private int space;

        public SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

            if (parent.getChildPosition(view) != 0)
                outRect.left = space;
        }
    }
}
