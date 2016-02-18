package com.psgod.ui.view;

/**
 * 单张图片展示 v2.0
 *
 * @author brandwang
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.psgod.BitmapUtils;
import com.psgod.Constants;
import com.psgod.PsGodImageLoader;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.WeakReferenceHandler;
import com.psgod.model.LoginUser;
import com.psgod.model.PhotoItem;
import com.psgod.model.User;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.ui.activity.CommentListActivity;
import com.psgod.ui.activity.PhotoBrowserActivity;
import com.psgod.ui.activity.SinglePhotoDetail;
import com.psgod.ui.activity.WorksListActivity;
import com.psgod.ui.adapter.HotCommentListAdapter;
import com.psgod.ui.widget.AvatarImageView;
import com.psgod.ui.widget.FollowImage;
import com.psgod.ui.widget.OriginImageLayout;
import com.psgod.ui.widget.dialog.PSDialog;
import com.psgod.ui.widget.dialog.ShareMoreDialog;

import org.json.JSONException;
import org.json.JSONObject;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.moments.WechatMoments;

public class PhotoItemView extends RelativeLayout implements Callback {
    private static final String TAG = PhotoItemView.class.getSimpleName();

    private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS;
    private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

    // 求P
    public static final byte TYPE_ASK = 1;
    // 作品
    public static final byte TYPE_REPLY = 2;

    public static final int STATUS_UNCOLLECTION = 101;
    public static final int STATUS_COLLECTION = 102;

    private PhotoItem mPhotoItem = null;
    // ps类型 求助 作品
    private int mPhotoType;
    // 照片类型
    private PhotoListType mType;
    private Context mContext;

    private AnimateFirstDisplayListener mAnimateFirstListener;

    private AvatarImageView mAvatarImage;
    private TextView mNameTv;
    private TextView mTimeTv;
    private FollowImage mFollowBtn;
    private ImageButton mSingleItemPsBtn;

    private RelativeLayout mImageArea;
    // 一张求助图片
    private ImageView mImageIv;
    // 两张求助图片
    private ImageView mImageViewLeft;
    private ImageView mImageViewRight;
    private RelativeLayout mUploadImages;

    private HtmlTextView mDescTv;

    // 单条求助类型操作栏
    private RelativeLayout mSingleAskBtnsPanel;
    private TextView mSingleAskCommentBtn;
    private TextView mSingleAskShareBtn;

    // 单条作品类型操作栏
    private RelativeLayout mSingleReplyBtnsPanel;
    private ImageView mSingleReplyAllWorksBtn;
    private TextView mSingleAllWorksTxt;
    private TextView mSingleReplyCommentTv;
    private TextView mSingleReplyShareTv;

    // 最新作品 类型操作栏
    private RelativeLayout mHotReplyBtnsPanel;
    private ImageView mHotReplyAllWorksBtn;
    private TextView mHotReplyCommentTv;
    private TextView mHotReplyShareTv;

    // 关注求p 热门求p 类型操作栏
    private RelativeLayout mFocusAskBtnsPanel;
    private TextView mFocusAskCommentTv;
    private TextView mFocusAskShareTv;

    // 关注 热门 作品类型操作栏
    private RelativeLayout mComplexBtnsPanel;
    private ImageView mAllWorksBtn;
    private TextView mAllWorksTxt;
    private TextView mComplexShareBtn;
    //    private TextView mComplexFavBtn;
//    private ImageView mComplexFavImg;
//    private ImageView mComplexFavTempImg;
    private TextView mComplexCommentBtn;

    private RelativeLayout mReplyLikedArea;
    //    private TextView mLikeCountTv;
//    private ImageView mLikeBtn;
    private LikeView mLikeView;

    private ImageView mHelpPSBtn;

    private ListView mCommentsLv;
    private RelativeLayout mCommentsPanel;

    private PSDialog mPsDialog;
    private ShareMoreDialog mShareMoreDialog;

    private boolean isRecentAct = false;

    private ViewEnabledRunnable mViewEnabledRunnable = new ViewEnabledRunnable(
            this);

    private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);

    // 0 未放大 1 放大 2 正在放大或缩小过程中
    private boolean mScaling = false;
    // 图片加载器单例
    private PsGodImageLoader imageLoader = PsGodImageLoader.getInstance();

    /**
     * photoitemview类型： SINGLE_ASK 单页求P SINGLE_REPLY 单页作品 RECENT_REPLY 最新tab 作品
     * HOT_FOCUS_ASK 首页 热门 关注 求P HOT_FOCUS_REPLY 首页 热门 关注 作品
     *
     * @author brandwang
     */
    public static enum PhotoListType {
        SINGLE_ASK, SINGLE_REPLY, RECENT_REPLY, HOT_FOCUS_ASK, HOT_FOCUS_REPLY, COURSE_DETAIL
    }

    public PhotoItemView(Context context) {
        super(context);
        mContext = context;
    }

    public PhotoItemView(Context context, AttributeSet attrs) {
        this(context, null, 0);
    }

    public PhotoItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    public void initialize(PhotoListType type) {
        mType = type;
        initViews();
        initListeners();
        setViewByPhotoListType(type);

        // TODO 这里要跟产品商量具体怎么显示图片
        mAnimateFirstListener = new AnimateFirstDisplayListener();
    }

    // 获取详情页评论按钮
    public TextView getRecentPhotoDetailCommentBtn(int type) {
        if (type == 1) {
            if (mSingleAskCommentBtn == null) {
                initViews();
            }
            return mSingleAskCommentBtn;
        } else {
            if (mSingleReplyCommentTv == null) {
                initViews();
            }
            return mSingleReplyCommentTv;
        }
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        mAvatarImage = (AvatarImageView) this
                .findViewById(R.id.photo_item_avatar_imgview);
        mNameTv = (TextView) this.findViewById(R.id.photo_item_name_tv);
        mTimeTv = (TextView) this.findViewById(R.id.photo_item_time_tv);

        mFollowBtn = (FollowImage) this.findViewById(R.id.photo_item_follow_btn);
        mSingleItemPsBtn = (ImageButton) this
                .findViewById(R.id.single_photo_item_ps_btn);

        mImageArea = (RelativeLayout) this
                .findViewById(R.id.photo_item_image_area);

        mDescTv = (HtmlTextView) this.findViewById(R.id.photo_item_desc_tv);

        // 单条求助类型操作栏
        mSingleAskBtnsPanel = (RelativeLayout) this
                .findViewById(R.id.single_photoitem_ask);
        mSingleAskCommentBtn = (TextView) this
                .findViewById(R.id.single_photoitem_ask_comment_tv);
        mSingleAskShareBtn = (TextView) this
                .findViewById(R.id.single_photoitem_ask_share_tv);

        // 单条作品类型操作栏
        mSingleReplyBtnsPanel = (RelativeLayout) this
                .findViewById(R.id.single_photoitem_reply);
        mSingleReplyAllWorksBtn = (ImageView) this
                .findViewById(R.id.single_photoitem_works_tv);
        mSingleAllWorksTxt = (TextView) this
                .findViewById(R.id.single_photoitem_works_txt);
        mSingleReplyCommentTv = (TextView) this
                .findViewById(R.id.single_photoitem_comment_tv);
        mSingleReplyShareTv = (TextView) this
                .findViewById(R.id.single_photoitem_share_tv);

        // 最新作品 类型操作栏
        mHotReplyBtnsPanel = (RelativeLayout) this
                .findViewById(R.id.hot_reply_item_btns);
        mHotReplyAllWorksBtn = (ImageView) this
                .findViewById(R.id.hot_reply_item_works_tv);
        mHotReplyCommentTv = (TextView) this
                .findViewById(R.id.hot_reply_item_comment_tv);
        mHotReplyShareTv = (TextView) this
                .findViewById(R.id.hot_reply_item_share_tv);

        // 关注求p 热门求p 类型操作栏
        mFocusAskBtnsPanel = (RelativeLayout) this
                .findViewById(R.id.focus_ask_photo_item_btns);
        mFocusAskCommentTv = (TextView) this
                .findViewById(R.id.simple_type_photo_item_comment_tv);
        mFocusAskShareTv = (TextView) this
                .findViewById(R.id.simple_type_photo_item_share_tv);

        // 关注 热门 作品类型操作栏
        mComplexBtnsPanel = (RelativeLayout) this
                .findViewById(R.id.focus_reply_item_btns);
        mAllWorksBtn = (ImageView) this.findViewById(R.id.photo_item_works_tv);
        mAllWorksTxt = (TextView) this.findViewById(R.id.simple_type_photo_item_works_txt);
        mComplexShareBtn = (TextView) this
                .findViewById(R.id.photo_item_share_tv);
//        mComplexFavBtn = (TextView) this.findViewById(R.id.photo_item_fav_tv);
//        mComplexFavImg = (ImageView) this.findViewById(R.id.photo_item_fav_img);
        mComplexCommentBtn = (TextView) this
                .findViewById(R.id.photo_item_comment_tv);
//        mComplexFavTempImg = (ImageView) this
//                .findViewById(R.id.photo_item_fav_temp_img);

        mReplyLikedArea = (RelativeLayout) this
                .findViewById(R.id.photo_item_like_area);

        mLikeView = (LikeView) this.findViewById(R.id.photo_item_like_view);
//        mLikeCountTv = (TextView) this
//                .findViewById(R.id.photo_item_like_count_tv);
        mHelpPSBtn = (ImageView) this.findViewById(R.id.photo_item_help_btn);

        mCommentsLv = (ListView) this
                .findViewById(R.id.photo_item_hot_comments_lv);
        mCommentsPanel = (RelativeLayout) this
                .findViewById(R.id.photo_item_hot_comments_panel);


    }

    /**
     * 初始化组件的动作监听器
     */
    private void initListeners() {
        /**
         * 非详情页时 （有作品）跳转到CarouselPhotoDetailActivity
         * （没有作品）跳转到RecentPhotoDetailActivity
         */
        mImageArea.setOnClickListener(imageOnClickListener);

        // 关注动态 photo点击跳转逻辑
        // if (mType == PhotoListType.FOLLOW) {
        // this.setOnClickListener(new OnClickListener() {
        // @Override
        // public void onClick(View arg0) {
        // mHandler.postDelayed(mViewEnabledRunnable, 1000);
        // if (mPhotoItem.getType() == TYPE_ASK) {
        // // 若回复数为0 则跳求P页面详情
        // if (mPhotoItem.getReplyCount() == 0) {
        // RecentPhotoDetailActivity.startActivity(mContext,
        // mPhotoItem);
        // setEnabled(false);
        // } else {
        // PhotoDetailActivity.startActivity(mContext,
        // mPhotoItem);
        // setEnabled(false);
        // }
        // } else if (mPhotoItem.getType() == TYPE_REPLY) {
        // //获取求P id
        // PhotoDetailActivity.startActivity(mContext, mPhotoItem.getAskId());
        // setEnabled(false);
        // }
        // }
        // });
        // }

        // mDetailShareBtn.setOnClickListener(new OnClickListener() {
        // // 点击直接唤起分享到朋友圈 根据type进行分享
        // @Override
        // public void onClick(View arg0) {
        // Utils.showProgressDialog(mContext);
        //
        // ActionShareRequest.Builder builder = new ActionShareRequest.Builder()
        // .setShareType("moments").setType(mPhotoItem.getType())
        // .setId(mPhotoItem.getPid())
        // .setListener(shareActionListener)
        // .setErrorListener(errorListener);
        //
        // ActionShareRequest request = builder.build();
        // request.setTag(TAG);
        // RequestQueue requestQueue = PSGodRequestQueue.getInstance(
        // PSGodApplication.getAppContext()).getRequestQueue();
        // requestQueue.add(request);
        // }
        // });
        // 其他作品按钮
        mAllWorksBtn.setOnClickListener(allWorkListener);
        mAllWorksTxt.setOnClickListener(allWorkListener);
        mHotReplyAllWorksBtn.setOnClickListener(allWorkListener);
        mSingleReplyAllWorksBtn.setOnClickListener(allWorkListener);
        mSingleAllWorksTxt.setOnClickListener(allWorkListener);
        // bang 按钮点击浮层
        mHelpPSBtn.setOnClickListener(helpPsListener);

        // 点击更多按钮
        mSingleAskShareBtn.setOnClickListener(shareListener);
        mSingleReplyShareTv.setOnClickListener(shareListener);
        mHotReplyShareTv.setOnClickListener(shareListener);
        mFocusAskShareTv.setOnClickListener(shareListener);
        mComplexShareBtn.setOnClickListener(shareListener);

        // 跳转到评论列表界面
        mHotReplyCommentTv.setOnClickListener(commentListener);
        mFocusAskCommentTv.setOnClickListener(commentListener);
        mComplexCommentBtn.setOnClickListener(commentListener);

//        // 作品点赞
//        mLikeBtn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // 设置点赞的缩放动画,没点赞过进入动画
//                if (!mPhotoItem.isLiked()) {
//                    AnimatorSet animatorZoomSet = new AnimatorSet();
//                    animatorZoomSet.setDuration(800);
//
//                    ObjectAnimator zoomX = ObjectAnimator.ofFloat(mLikeBtn,
//                            "scaleX", 1f, 1.5f, 1f);
//                    ObjectAnimator zoomY = ObjectAnimator.ofFloat(mLikeBtn,
//                            "scaleY", 1f, 1.5f, 1f);
//                    animatorZoomSet.playTogether(zoomX, zoomY);
//                    animatorZoomSet.start();
//                }
//
//                // 点赞网络请求
//                mLikeBtn.setClickable(false);
//
//                int mStatus = mPhotoItem.isLiked() ? 0 : 1;
//                ActionLikeRequest.Builder builder = new ActionLikeRequest.Builder()
//                        .setPid(mPhotoItem.getPid())
//                        .setType(mPhotoItem.getType())
//                        .setListener(mActionLikeListener).setStatus(mStatus)
//                        .setErrorListener(mActionLikeErrorListener);
//                ActionLikeRequest request = builder.build();
//                RequestQueue requestQueue = PSGodRequestQueue.getInstance(
//                        mContext).getRequestQueue();
//                requestQueue.add(request);
//            }
//        });

        // 作品收藏
//        mComplexFavBtn.setOnClickListener(favClick);
//        mComplexFavImg.setOnClickListener(favClick);

        // 如果详情页中 点击查看图片详情
        // if (mType == PhotoListType.SINGLE_DETAIL) {
        // mImageIv.setOnClickListener(new OnClickListener() {
        // @Override
        // public void onClick(View arg0) {
        // Intent intent = new Intent(mContext,
        // PhotoBrowserActivity.class);
        // intent.putExtra(Constants.IntentKey.PHOTO_PATH,
        // mPhotoItem.getImageURL());
        // intent.putExtra(Constants.IntentKey.ASK_ID,
        // mPhotoItem.getAskId());
        // intent.putExtra(Constants.IntentKey.PHOTO_ITEM_ID,
        // mPhotoItem.getPid());
        // intent.putExtra(Constants.IntentKey.PHOTO_ITEM_TYPE,
        // (mPhotoItem.getType() == TYPE_ASK) ? "ask" : "reply" );
        // mContext.startActivity(intent);
        // }
        // });
        // }


    }

    private PSGodErrorListener errorListener = new PSGodErrorListener(
            PhotoItemView.class.getSimpleName()) {
        @Override
        public void handleError(VolleyError error) {
        }
    };

    // 微信分享接口请求回调
    private Listener<JSONObject> shareActionListener = new Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            // 隐藏对话框
            Utils.hideProgressDialog();

            ShareSDK.initSDK(mContext);
            Platform wechat = ShareSDK.getPlatform(WechatMoments.NAME);
            wechat.setPlatformActionListener(new PlatformActionListener() {

                @Override
                public void onError(Platform arg0, int arg1, Throwable arg2) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onComplete(Platform arg0, int arg1,
                                       HashMap<String, Object> arg2) {
                    String textShareCount = Utils
                            .getCountDisplayText(mPhotoItem.getShareCount());
                    // mSimpleShareBtn.setText(Integer.parseInt(textShareCount)+1);
                }

                @Override
                public void onCancel(Platform arg0, int arg1) {
                    // TODO Auto-generated method stub

                }
            });

            try {
                if (response.getString("type").equals("image")) {
                    ShareParams sp = new ShareParams();
                    sp.setShareType(Platform.SHARE_IMAGE);
                    sp.setText(response.getString("desc"));
                    sp.setImageUrl(response.getString("image"));
                    wechat.share(sp);
                }
                if (response.getString("type").equals("url")) {
                    // 图文链接分享
                    ShareParams sp = new ShareParams();
                    sp.setShareType(Platform.SHARE_WEBPAGE);
                    sp.setTitle(response.getString("title"));
                    sp.setText(response.getString("desc"));
                    sp.setImageUrl(response.getString("image"));
                    sp.setUrl(response.getString("url"));
                    wechat.share(sp);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    private boolean showOrigin = true;
    private boolean isHomePageHot = false;

    public void setShowOrigin(boolean showOrigin) {
        this.showOrigin = showOrigin;
    }

    public void setIsHomePageHot(boolean isHomePageHot) {
        this.isHomePageHot = isHomePageHot;
    }

    // 配置图片显示细节
    public void setPhotoItem(PhotoItem photoItem) {
        mPhotoItem = photoItem;
        mPhotoType = mPhotoItem.getType();
        mScaling = false;

        mNameTv.setText(mPhotoItem.getNickname());
        mTimeTv.setText(mPhotoItem.getUpdateTimeStr());
        mAllWorksTxt.setText(String.format("已有%s个作品", mPhotoItem.getReplyCount()));
        mSingleAllWorksTxt.setText(String.format("已有%s个作品", mPhotoItem.getReplyCount()));
        if (mPhotoItem.getReplyCount() == 0) {
            mAllWorksTxt.setEnabled(false);
            mSingleAllWorksTxt.setEnabled(false);
        } else {
            mAllWorksTxt.setEnabled(true);
            mSingleAllWorksTxt.setEnabled(true);
        }

        if (isHomePageHot) {
//            mTimeTv.setVisibility(GONE);
//            mNameTv.setTextSize(15);
//            LayoutParams params = (LayoutParams) mNameTv.getLayoutParams();
//            params.addRule(CENTER_VERTICAL);
//            params.addRule(ALIGN_TOP, 0);
//            mNameTv.setLayoutParams(params);

        }

        mDescTv.setHtmlFromString(mPhotoItem.getDesc(), true);


//        mAvatarImage = mAvatarIv.getmAvatarImage();
//
        mAvatarImage.setUser(new User(mPhotoItem));
        imageLoader.displayImage(mPhotoItem.getAvatarURL(), mAvatarImage,
                mAvatarOptions, mAnimateFirstListener);

        mImageArea.removeAllViews();

        mFollowBtn.setPhotoItem(mPhotoItem);
        if ((LoginUser.getInstance().getUid() == mPhotoItem.getUid()) || isHomePageFocus) {
            mFollowBtn.setVisibility(INVISIBLE);
        } else {
            mFollowBtn.setVisibility(VISIBLE);
        }
        mFollowBtn.setOnFollowChangeListener(onFollowChangeListener);

        // 作品情况 展示外面image_url
        if (mPhotoType == PhotoItem.TYPE_REPLY) {
            mImageIv = new ImageView(mContext);

            ViewGroup.LayoutParams workParams = new ViewGroup.LayoutParams(
                    Constants.WIDTH_OF_SCREEN, Constants.WIDTH_OF_SCREEN);
            mImageIv.setLayoutParams(workParams);

            mImageIv.setOnLongClickListener(imageOnLongClickListener);
            mImageIv.setImageResource(R.drawable.ic_zhanwei);

            // 图片回调中将图片毛玻璃化之后作为背景图
            mImageIv.setTag(R.id.image_url, mPhotoItem.getImageURL());
            imageLoader.displayImage(mPhotoItem.getImageURL(), mImageIv,
                    mOptions, imageLoadingListener);
            mImageArea.addView(mImageIv);

            if (mType == PhotoListType.SINGLE_ASK
                    || mType == PhotoListType.SINGLE_REPLY) {
                mImageIv.setOnClickListener(imageBrowserListener);
            } else {
                mImageIv.setOnClickListener(imageOnClickListener);
            }

            if (isHomePageFocus || isHomePageHot) {
                mImageIv.setOnClickListener(imageOnClickListener2);
            }
//            mImageIv.setOnTouchListener(
//                    new OnTouchListener() {
//
//
//            );
        }

        // 求助情况 展示asks_uploads数组中图片
        // 一张求助
        else if (mPhotoType == PhotoItem.TYPE_ASK) {

            if (mPhotoItem.getUploadImagesList().size() == 1
                    ) {
                mImageIv = new ImageView(mContext);

                ViewGroup.LayoutParams singleAskParams = new ViewGroup.LayoutParams(
                        Constants.WIDTH_OF_SCREEN, Constants.WIDTH_OF_SCREEN);
                mImageIv.setLayoutParams(singleAskParams);

                // 图片回调中将图片毛玻璃化之后作为背景图
                mImageIv.setTag(R.id.image_url, mPhotoItem.getUploadImagesList().get(0).mImageUrl);
                imageLoader.displayImage(
                        mPhotoItem.getUploadImagesList().get(0).mImageUrl,
                        mImageIv, mOptions, imageLoadingListener);
                mImageArea.addView(mImageIv);

                if (mType == PhotoListType.SINGLE_ASK
                        || mType == PhotoListType.SINGLE_REPLY) {
                    mImageIv.setOnClickListener(imageBrowserListener);

                } else {
                    mImageIv.setOnClickListener(imageOnClickListener);
                }

                if (isHomePageFocus || isHomePageHot) {
                    mImageIv.setOnClickListener(imageOnClickListener2);
                }
                mImageIv.setOnLongClickListener(imageOnLongClickListener);
            }

            // 两张求助
            else if (mPhotoItem.getUploadImagesList().size() == 2) {
                mImageViewLeft = new ImageView(mContext);
                RelativeLayout.LayoutParams leftParams = new RelativeLayout.LayoutParams(
                        Constants.WIDTH_OF_SCREEN / 2, Constants.WIDTH_OF_SCREEN);
                leftParams.addRule(ALIGN_PARENT_LEFT, TRUE);
                mImageViewLeft.setLayoutParams(leftParams);
                mImageViewLeft.setScaleType(ScaleType.CENTER_CROP);

                mImageViewRight = new ImageView(mContext);
                RelativeLayout.LayoutParams rightParams = new RelativeLayout.LayoutParams(
                        Constants.WIDTH_OF_SCREEN / 2, Constants.WIDTH_OF_SCREEN);
                rightParams.addRule(ALIGN_PARENT_RIGHT, TRUE);
                mImageViewRight.setLayoutParams(rightParams);
                mImageViewRight.setScaleType(ScaleType.CENTER_CROP);

                imageLoader.displayImage(
                        mPhotoItem.getUploadImagesList().get(0).mImageUrl,
                        mImageViewLeft, mOptions, imageLoadingListener);
                imageLoader.displayImage(
                        mPhotoItem.getUploadImagesList().get(1).mImageUrl,
                        mImageViewRight, mOptions);

                mImageViewLeft.setOnClickListener(imageBrowserListener2);

                if (isHomePageFocus || isHomePageHot) {
                    mImageViewLeft.setOnClickListener(imageOnClickListener2);
                }

                mImageViewLeft
                        .setTag(mPhotoItem.getUploadImagesList().get(0).mImageUrl);

                mImageViewLeft.setOnLongClickListener(imageOnLongClickListener);

                mImageViewRight.setOnClickListener(imageBrowserListener2);

                if (isHomePageFocus || isHomePageHot) {
                    mImageViewRight.setOnClickListener(imageOnClickListener2);
                }

                mImageViewRight
                        .setTag(mPhotoItem.getUploadImagesList().get(1).mImageUrl);

                mImageViewRight.setOnLongClickListener(imageOnLongClickListener);


                mImageArea.addView(mImageViewLeft);
                mImageArea.addView(mImageViewRight);
            }
            ImageView view = new ImageView(mContext);
            view.setImageResource(R.mipmap.tag_imgarea_ori);
            RelativeLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(Utils.dpToPx(mContext, 10), Utils.dpToPx(mContext, 10), 0, 0);
            view.setLayoutParams(params);
            mImageArea.addView(view);
        }
        // 求p原图区域 只有作品才显示
        if (mPhotoItem.getUploadImagesList().size() != 0
                && (mPhotoType == TYPE_REPLY)
                && (mType != PhotoListType.SINGLE_ASK)
                && (mType != PhotoListType.SINGLE_REPLY) && showOrigin) {
            // 只有一张原图
            OriginImageLayout originImageLayout = new OriginImageLayout(
                    mContext);
            originImageLayout.setImages(mPhotoItem.getUploadImagesList());
            originImageLayout.setBackground(mImageIv);
            originImageLayout.init();
            mImageArea.addView(originImageLayout);

            // 原图有两张
            // if (mPhotoItem.getUploadImagesList().size() == 2) {
            // ImageView uploadImageRight = new ImageView(mContext);
            //
            // int rightImageId = (new Random()).nextInt(Integer.MAX_VALUE);
            // LayoutParams rightParams = new LayoutParams(
            // Utils.dpToPx(mContext, 50), Utils.dpToPx(mContext, 100));
            // uploadImageRight.setLayoutParams(rightParams);
            // uploadImageRight.setId(rightImageId);
            //
            // ImageView uploadImageLeft = new ImageView(mContext);
            // LayoutParams leftParams = new LayoutParams(
            // Utils.dpToPx(mContext, 50), Utils.dpToPx(mContext, 100));
            // leftParams.addRule(RelativeLayout.LEFT_OF, rightImageId);
            // uploadImageLeft.setLayoutParams(leftParams);
            //
            // //左上角 原图标示
            // ImageView mOriginTipIv = new ImageView(mContext);
            // RelativeLayout.LayoutParams originTipParams = new
            // RelativeLayout.LayoutParams(
            // LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            // originTipParams.addRule(RelativeLayout.ALIGN_LEFT,
            // uploadImageLeft.getId());
            // originTipParams.addRule(RelativeLayout.ALIGN_TOP,
            // uploadImageLeft.getId());
            // mOriginTipIv.setLayoutParams(originTipParams);
            // mOriginTipIv.setBackgroundResource(R.drawable.ic_yuantu);
            //
            // mUploadImages.addView(uploadImageRight);
            // mUploadImages.addView(uploadImageLeft);
            // mUploadImages.addView(mOriginTipIv);
            // }
        } else {

        }

        // 设置操作栏中细节 分simple和complex两种情况
        updateBtnsPanel();
        updateShareView();
        updateCommentView();
        mLikeView.setmPhotoItem(mPhotoItem);

//        updateFavView();
//        updateLikeView();
//        updateFollowView();

        // 若热门评论为空 则隐藏区域 首页热门图片类型中
        if ((mPhotoItem.getHotCommentList().size() != 0 && !isHomePageFocus)
                && ((mType == PhotoListType.HOT_FOCUS_REPLY))) {
            HotCommentListAdapter adapter = new HotCommentListAdapter(mContext,
                    mPhotoItem.getHotCommentList());
            adapter.setPhotoItem(mPhotoItem);
            mCommentsLv.setAdapter(adapter);
            fixListViewHeight(mCommentsLv);
            mCommentsPanel.setVisibility(View.VISIBLE);
        } else {
            mCommentsPanel.setVisibility(View.GONE);
        }

        // 根据是否是活动页面，求P或是作品判断其他作品tab的显示
        if (isRecentAct) {
            mAllWorksBtn.setVisibility(GONE);
            mAllWorksTxt.setVisibility(GONE);
            mHotReplyAllWorksBtn.setVisibility(GONE);
            mSingleReplyAllWorksBtn.setVisibility(GONE);
            mSingleAllWorksTxt.setVisibility(GONE);
//            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mSingleAskShareBtn.getLayoutParams();
//            params.setMargins(0, 0, 0, 0);
//            mSingleAskShareBtn.setLayoutParams(params);
//            params = (RelativeLayout.LayoutParams) mSingleReplyShareTv.getLayoutParams();
//            params.setMargins(0, 0, 0, 0);
//            mSingleReplyShareTv.setLayoutParams(params);
//            params = (RelativeLayout.LayoutParams) mHotReplyShareTv.getLayoutParams();
//            params.setMargins(0, 0, 0, 0);
//            mHotReplyShareTv.setLayoutParams(params);
//            params = (RelativeLayout.LayoutParams) mFocusAskShareTv.getLayoutParams();
//            params.setMargins(0, 0, 0, 0);
//            mFocusAskShareTv.setLayoutParams(params);
//            params = (RelativeLayout.LayoutParams) mComplexShareBtn.getLayoutParams();
//            params.setMargins(0, 0, 0, 0);
//            mComplexShareBtn.setLayoutParams(params);
        } else {
            mAllWorksTxt.setVisibility(VISIBLE);
            mSingleAllWorksTxt.setVisibility(VISIBLE);
            if (mPhotoItem.getReplyCount() >= 2) {
                mAllWorksBtn.setVisibility(VISIBLE);
                mHotReplyAllWorksBtn.setVisibility(VISIBLE);
                mSingleReplyAllWorksBtn.setVisibility(VISIBLE);
            }
        }
    }

    // 在listview 里嵌套listview无法自动计算大小 只能显示第一行 需要重新计算listview的高度
    public void fixListViewHeight(ListView listView) {
        // 如果没有设置数据适配器，则ListView没有子项，返回。
        ListAdapter listAdapter = listView.getAdapter();
        int totalHeight = 0;
        if (listAdapter == null) {
            return;
        }
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            View listViewItem = listAdapter.getView(i, null, listView);
            // 计算子项View 的宽高
            listViewItem.measure(0, 0);
            // 计算所有子项的高度和
            totalHeight += listViewItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        // listView.getDividerHeight()获取子项间分隔符的高度
        // params.height设置ListView完全显示需要的高度
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public void updateBtnsPanel() {
        // 求p类型
        if (mPhotoType == TYPE_ASK) {
            mReplyLikedArea.setVisibility(View.GONE);
            mHelpPSBtn.setVisibility(View.VISIBLE);
        }
        // 作品类型
        if (mPhotoType == TYPE_REPLY) {
            mReplyLikedArea.setVisibility(View.VISIBLE);
            mHelpPSBtn.setVisibility(View.GONE);
        }
    }

    public void updateShareView() {
        String textShareCount = Utils.getCountDisplayText(mPhotoItem
                .getShareCount());
        mSingleAskShareBtn.setText(textShareCount);
        mSingleReplyShareTv.setText(textShareCount);
        mHotReplyShareTv.setText(textShareCount);
        mFocusAskShareTv.setText(textShareCount);
        mComplexShareBtn.setText(textShareCount);
    }

    public void updateCommentView() {
        String textCommentCount = Utils.getCountDisplayText(mPhotoItem
                .getCommentCount());
        mSingleAskCommentBtn.setText(textCommentCount);
        mSingleReplyCommentTv.setText(textCommentCount);
        mHotReplyCommentTv.setText(textCommentCount);
        mFocusAskCommentTv.setText(textCommentCount);
        mComplexCommentBtn.setText(textCommentCount);
    }

//    /**
//     * 根据用户是否点赞，更新点赞按钮
//     */
//    public void updateLikeView() {
//        if (mPhotoItem.isLiked()) {
//            mLikeBtn.setImageResource(R.drawable.ic_home_like_selected);
//            if (Build.VERSION.SDK_INT >= 16) {
//                mLikeCountTv.setBackground(mContext.getResources().getDrawable(
//                        R.drawable.shape_like_count));
//            }
//        } else {
//            mLikeBtn.setImageResource(R.drawable.ic_home_like_normal);
//            if (Build.VERSION.SDK_INT >= 16) {
//                mLikeCountTv.setBackground(mContext.getResources().getDrawable(
//                        R.drawable.shape_unlike_count));
//            }
//        }
//        String textLikeCount = Utils.getCountDisplayText(mPhotoItem
//                .getLikeCount());
//        mLikeCountTv.setText(textLikeCount);
//    }

    /**
     * 根据用户是否收藏
     */
//    public void updateFavView() {
//        Resources res = mContext.getResources();
//        if (mPhotoItem.isCollected()) {
//            mComplexFavImg.setImageDrawable(res
//                    .getDrawable(R.drawable.ic_home_fav_selected));
//        } else {
//            mComplexFavImg.setImageDrawable(res
//                    .getDrawable(R.drawable.ic_home_fav_normal));
//        }
//        String textFavCount = Utils.getCountDisplayText(mPhotoItem
//                .getCollectCount());
//        mComplexFavBtn.setText(textFavCount);
//    }

    private boolean isHomePageFocus = false;

    public void setIsHomePageFocus(boolean isHomePageFocus) {
        this.isHomePageFocus = isHomePageFocus;
    }

    /**
     * 根据不同的复用类型设置View的显示() 这个跟业务的耦合比较大
     */
    public void setViewByPhotoListType(PhotoListType type) {
        switch (type) {
            case SINGLE_ASK:
                mSingleAskBtnsPanel.setVisibility(View.VISIBLE);
                mSingleReplyBtnsPanel.setVisibility(View.GONE);
                mComplexBtnsPanel.setVisibility(View.GONE);
                mHotReplyBtnsPanel.setVisibility(View.GONE);
                mFocusAskBtnsPanel.setVisibility(View.GONE);
                mFollowBtn.setVisibility(View.VISIBLE);
                mCommentsPanel.setVisibility(View.GONE);
                break;

            case SINGLE_REPLY:
                mSingleAskBtnsPanel.setVisibility(View.GONE);
                mSingleReplyBtnsPanel.setVisibility(View.VISIBLE);
                mComplexBtnsPanel.setVisibility(View.GONE);
                mHotReplyBtnsPanel.setVisibility(View.GONE);
                mFocusAskBtnsPanel.setVisibility(View.GONE);
                mFollowBtn.setVisibility(View.VISIBLE);
                mCommentsPanel.setVisibility(View.GONE);
                break;

            case RECENT_REPLY:
                mSingleAskBtnsPanel.setVisibility(View.GONE);
                mSingleReplyBtnsPanel.setVisibility(View.GONE);
                mComplexBtnsPanel.setVisibility(View.GONE);
                mHotReplyBtnsPanel.setVisibility(View.VISIBLE);
                mFocusAskBtnsPanel.setVisibility(View.GONE);
                mFollowBtn.setVisibility(View.VISIBLE);
                mCommentsPanel.setVisibility(View.GONE);
                break;

            case HOT_FOCUS_ASK:
                mSingleAskBtnsPanel.setVisibility(View.GONE);
                mSingleReplyBtnsPanel.setVisibility(View.GONE);
                mComplexBtnsPanel.setVisibility(View.GONE);
                mHotReplyBtnsPanel.setVisibility(View.GONE);
                mFocusAskBtnsPanel.setVisibility(View.VISIBLE);
                mFollowBtn.setVisibility(View.VISIBLE);
                break;

            case HOT_FOCUS_REPLY:
                mSingleAskBtnsPanel.setVisibility(View.GONE);
                mSingleReplyBtnsPanel.setVisibility(View.GONE);
                mComplexBtnsPanel.setVisibility(View.VISIBLE);
                mHotReplyBtnsPanel.setVisibility(View.GONE);
                mFocusAskBtnsPanel.setVisibility(View.GONE);
                mFollowBtn.setVisibility(View.VISIBLE);
                break;

            default:
                break;
        }

    }

//    // 点赞成功回调函数
//    private Listener<Boolean> mActionLikeListener = new Listener<Boolean>() {
//        @Override
//        public void onResponse(Boolean response) {
//            if (response) {
//                mPhotoItem.setLikeCount(mPhotoItem.isLiked() ? mPhotoItem
//                        .getLikeCount() - 1 : mPhotoItem.getLikeCount() + 1);
//                mPhotoItem.setIsLiked(mPhotoItem.isLiked() ? false : true);
//                updateLikeView();
//            }
//            mLikeBtn.setClickable(true);
//        }
//    };
//
//    // 点赞失败回调函数
//    private PSGodErrorListener mActionLikeErrorListener = new PSGodErrorListener(
//            ActionLikeRequest.class.getSimpleName()) {
//        @Override
//        public void handleError(VolleyError error) {
//            mLikeBtn.setClickable(true);
//        }
//    };
//
//    // 收藏回调函数
//    private Listener<Boolean> mActionFavListener = new Listener<Boolean>() {
//        @Override
//        public void onResponse(Boolean response) {
//            if (response) {
//                mPhotoItem
//                        .setCollectCount(mPhotoItem.isCollected() ? mPhotoItem
//                                .getCollectCount() - 1 : mPhotoItem
//                                .getCollectCount() + 1);
//                mPhotoItem.setIsCollected(mPhotoItem.isCollected() ? false
//                        : true);
//                updateFavView();
//            }
//            mComplexFavBtn.setClickable(true);
//            mComplexFavImg.setClickable(true);
//            EventBus.getDefault().post(
//                    new MyPageRefreshEvent(MyPageRefreshEvent.COLLECTION));
//        }
//    };
//
//    // 收藏失败回调函数
//    private PSGodErrorListener mActionFavErrorListener = new PSGodErrorListener(
//            ActionCollectionRequest.class.getSimpleName()) {
//        @Override
//        public void handleError(VolleyError error) {
//            mComplexFavBtn.setClickable(true);
//            CustomToast.show(mContext, "收藏失败，请刷新后重试", Toast.LENGTH_LONG);
//        }
//    };

    /**
     * 图片加载回调 将图片毛玻璃化处理后作为背景
     */
    private ImageLoadingListener imageLoadingListener = new ImageLoadingListener() {
        @Override
        public void onLoadingComplete(String imageUri, View view,
                                      Bitmap loadedImage) {
            String url = (String) view.getTag(R.id.image_url);
            if (imageUri != null && url != null) {
                url = url.split("\\?")[0];
                imageUri = imageUri.split("\\?")[0];
                if (url.equals(imageUri)) {
                    Bitmap bitmap = BitmapUtils.getBlurBitmap(loadedImage, view, imageUri);
                    if (bitmap == null) {
                        mImageArea.setBackgroundColor(Color.parseColor("#00000000"));
                    } else {
                        mImageArea.setBackgroundDrawable(new BitmapDrawable(getResources(),
                                bitmap));
                    }
                }
            }
        }

        @Override
        public void onLoadingCancelled(String arg0, View arg1) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onLoadingStarted(String arg0, View arg1) {
            // TODO Auto-generated method stub
        }
    };

    /**
     * 图片首次出现时的动画
     *
     * @author rayalyuan
     */
    private static class AnimateFirstDisplayListener extends
            SimpleImageLoadingListener {
        static final List<String> displayedImages = Collections
                .synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view,
                                      Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }

    private static class ViewEnabledRunnable implements Runnable {
        private WeakReference<PhotoItemView> ref;

        public ViewEnabledRunnable(PhotoItemView view) {
            ref = new WeakReference<PhotoItemView>(view);
        }

        @Override
        public void run() {
            PhotoItemView view = ref.get();
            if (view != null) {
                view.setEnabled(true);
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        // TODO Auto-generated method stub
        return false;
    }

    // 全部作品
    private OnClickListener allWorkListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, WorksListActivity.class);
            intent.putExtra("ASKID", mPhotoItem.getAskId());
            mContext.startActivity(intent);
        }
    };

    // bang 按钮点击浮层
    private OnClickListener helpPsListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mPsDialog == null) {
                mPsDialog = new PSDialog(mContext);
            }

            mPsDialog.setPhotoItem(mPhotoItem);
            if (mPsDialog.isShowing()) {
                mPsDialog.dismiss();
            } else {
                mPsDialog.show();
            }
        }
    };

    // 评论 跳转到全部评论页
    private OnClickListener commentListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, CommentListActivity.class);
            intent.putExtra(Constants.IntentKey.PHOTO_ITEM, mPhotoItem);
            mContext.startActivity(intent);
            setEnabled(false);
            mHandler.postDelayed(mViewEnabledRunnable, 1000);
        }
    };

    // 分享弹框
    private OnClickListener shareListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mShareMoreDialog == null) {
                mShareMoreDialog = new ShareMoreDialog(mContext);
            }
            mShareMoreDialog.setPhotoItem(mPhotoItem);
            if (mShareMoreDialog.isShowing()) {
                mShareMoreDialog.dismiss();
            } else {
                mShareMoreDialog.show();
            }
        }
    };

    // 点击图片的跳转
    /**
     * 非详情页时 （有作品）跳转到CarouselPhotoDetailActivity
     * （没有作品）跳转到RecentPhotoDetailActivity
     */
    private OnClickListener imageOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if ((mPhotoItem.getType() == 1 && mPhotoItem.getReplyCount() == 0) || isRecentAct) {
                SinglePhotoDetail.startActivity(mContext, mPhotoItem);
                setEnabled(false);
                mHandler.postDelayed(mViewEnabledRunnable, 1000);
            } else {
                Utils.skipByObject(mContext, mPhotoItem);
//                new CarouselPhotoDetailDialog(mContext, mPhotoItem.getAskId(), mPhotoItem.getPid()
//                        , mPhotoItem.getCategoryId()).show();
            }
        }
    };

    //动态页面只跳转到详情
    private OnClickListener imageOnClickListener2 = new OnClickListener() {
        @Override
        public void onClick(View v) {
            SinglePhotoDetail.startActivity(mContext, mPhotoItem);
            setEnabled(false);
            mHandler.postDelayed(mViewEnabledRunnable, 1000);
        }
    };

    // 长按图片Listener
    private OnLongClickListener imageOnLongClickListener = new OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            if (mShareMoreDialog == null) {
                mShareMoreDialog = new ShareMoreDialog(mContext);
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

    // 详情页，点击图片 预览
    private OnClickListener imageBrowserListener = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            Intent intent = new Intent(mContext, PhotoBrowserActivity.class);
            intent.putExtra(Constants.IntentKey.PHOTO_PATH,
                    mPhotoItem.getImageURL());
            intent.putExtra(Constants.IntentKey.ASK_ID, mPhotoItem.getAskId());
            intent.putExtra(Constants.IntentKey.PHOTO_ITEM_ID,
                    mPhotoItem.getPid());
            intent.putExtra(Constants.IntentKey.PHOTO_ITEM_TYPE,
                    (mPhotoItem.getType() == TYPE_ASK) ? "ask" : "reply");
            mContext.startActivity(intent);
        }
    };

    // 详情页，点击图片 预览
    private OnClickListener imageBrowserListener2 = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(mContext, PhotoBrowserActivity.class);
            intent.putExtra(Constants.IntentKey.PHOTO_PATH, view.getTag()
                    .toString());
            intent.putExtra(Constants.IntentKey.ASK_ID, mPhotoItem.getAskId());
            intent.putExtra(Constants.IntentKey.PHOTO_ITEM_ID,
                    mPhotoItem.getPid());
            intent.putExtra(Constants.IntentKey.PHOTO_ITEM_TYPE,
                    (mPhotoItem.getType() == TYPE_ASK) ? "ask" : "reply");
            mContext.startActivity(intent);
        }
    };

//    OnClickListener favClick = new OnClickListener() {
//
//        @Override
//        public void onClick(View view) {
//            // 请求网络时不可点击
//            mComplexFavBtn.setClickable(false);
//            mComplexFavImg.setClickable(false);
//            if (mComplexFavTempImg.getVisibility() == GONE) {
//                mComplexFavTempImg.setVisibility(VISIBLE);
//                mComplexFavTempImg.setImageDrawable(mComplexFavImg
//                        .getDrawable());
//                AnimUtils.vanishAnimThumb(mContext, mComplexFavTempImg, null);
//            }
//            int mStatus = mPhotoItem.isCollected() ? STATUS_UNCOLLECTION
//                    : STATUS_COLLECTION;
//
//            ActionCollectionRequest.Builder builder = new ActionCollectionRequest.Builder()
//                    .setType(mPhotoItem.getType()).setPid(mPhotoItem.getPid())
//                    .setStatus(mStatus)
//                    .setErrorListener(mActionFavErrorListener)
//                    .setListener(mActionFavListener);
//
//            ActionCollectionRequest request = builder.build();
//            RequestQueue requestQueue = PSGodRequestQueue.getInstance(mContext)
//                    .getRequestQueue();
//            requestQueue.add(request);
//        }
//    };

    public PhotoItem getPhotoItem() {
        return mPhotoItem;
    }

    public void setIsRecentAct(boolean isRecentAct) {
        this.isRecentAct = isRecentAct;
    }

    private FollowImage.OnFollowChangeListener onFollowChangeListener;

    public void setOnFollowChangeListener(FollowImage.OnFollowChangeListener onFollowChangeListener) {
        this.onFollowChangeListener = onFollowChangeListener;
    }


}
