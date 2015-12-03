package com.psgod.ui.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.psgod.BitmapUtils;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.PhotoItem;
import com.psgod.ui.activity.CommentListActivity;
import com.psgod.ui.adapter.ViewPagerAdapter;
import com.psgod.ui.widget.AvatarImageView;
import com.psgod.ui.widget.StopViewPager;
import com.psgod.ui.widget.dialog.PSDialog;
import com.psgod.ui.widget.dialog.ShareMoreDialog;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/11/27 0027.
 */
public class CarouselPhotoDetailView extends RelativeLayout {

    private Context mContext;

    public CarouselPhotoDetailView(Context context) {
        super(context);
        init();
    }

    public CarouselPhotoDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CarouselPhotoDetailView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public CarouselPhotoDetailView(Context context, PhotoItem photoItem) {
        super(context);
        mPhotoItem = photoItem;
        init();
    }

    private void init() {
        mContext = getContext();
        parent = LayoutInflater.from(mContext).inflate(R.layout.view_carousel_photo_detail, null);
        addView(parent);
        initView(parent);
        initListener(parent);
//        setBackground(mContext.getResources().getDrawable(R.drawable.shape_dialog_corner));
    }

    private View parent;
    private ViewPager vp;
    private PhotoItem mPhotoItem;

    private RelativeLayout mScroll;
    private RelativeLayout mCover;

    private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS;
    private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

    public void setVp(ViewPager vp) {
        this.vp = vp;
    }

    private void initView(View view) {
        mScroll = (RelativeLayout) view.
                findViewById(R.id.view_carp_photo_detail_insidescroll);
        mCover = (RelativeLayout) view.
                findViewById(R.id.view_carp_photo_detail_coverview);
        initCover(view);
    }

    boolean backInited = false;
    SinglePhotoView singlePhotoView;

    private void initBack() {
        if (!backInited) {
            singlePhotoView = new SinglePhotoView(getContext(), mPhotoItem);
            singlePhotoView.setOnEndListener(new SinglePhotoView.OnEndListener() {
                @Override
                public void onEndListener(SinglePhotoView view) {
                    if (onEndListener != null) {
                        onEndListener.onEnd();
                    }
                }
            });
            mScroll.addView(singlePhotoView);
        }
    }

    private RelativeLayout headLayout;
    private RelativeLayout descLayout;

    private ImageView coverTag;
    private TextView coverName;
    private TextView coverTime;
    private AvatarImageView coverAvatar;
    private ImageView coverCover;
    private ImageView coverBack;
    private ImageView coverCommentImg;
    private ImageView coverShareImg;
    private HtmlTextView coverDesc;
    private TextView coverComment;
    private TextView coverShare;
    private FrameLayout coverImgArea;
    private ImageView coverBang;
    private LikeView coverLike;

    private void initCover(View view) {
        headLayout = (RelativeLayout) view.findViewById(R.id.detail_cover_head);
        descLayout = (RelativeLayout) view.findViewById(R.id.cover_desc_layout);
        coverTag = (ImageView) view.findViewById(R.id.view_carp_photo_detail_cover_tag);
        coverName = (TextView) view.findViewById(R.id.view_carp_photo_detail_cover_name);
        coverTime = (TextView) view.findViewById(R.id.view_carp_photo_detail_cover_time);
        coverAvatar = (AvatarImageView) view.findViewById(R.id.view_carp_photo_detail_cover_avatar);
        coverBack = (ImageView) view.findViewById(R.id.view_carp_photo_detail_cover_backimg);
        coverDesc = (HtmlTextView) view.findViewById(R.id.view_carp_photo_detail_cover_desc);
        coverComment = (TextView) view.findViewById(R.id.view_carp_photo_detail_cover_comment);
        coverCommentImg = (ImageView) view.findViewById(R.id.cover_comment_img);
        coverShare = (TextView) view.findViewById(R.id.view_carp_photo_detail_cover_share);
        coverShareImg = (ImageView) view.findViewById(R.id.cover_share_img);
        coverImgArea = (FrameLayout) view.findViewById(R.id.view_carp_photo_detail_cover_imgarea);
        coverBang = (ImageView) view.findViewById(R.id.view_carp_photo_detail_cover_bang);
        coverLike = (LikeView) view.findViewById(R.id.view_carp_photo_detail_cover_like);

        LayoutParams headParams = (LayoutParams) headLayout.getLayoutParams();
        headParams.height = (int) (headParams.height*Utils.getHeightScale(getContext()));
        headLayout.setLayoutParams(headParams);

        LayoutParams backParams = (LayoutParams) coverImgArea.getLayoutParams();
        backParams.height = (int) (backParams.height*Utils.getHeightScale(getContext()));
        coverImgArea.setLayoutParams(backParams);

        LayoutParams descParams = (LayoutParams) descLayout.getLayoutParams();
        descParams.height = (int) (descParams.height*Utils.getHeightScale(getContext()));
        descLayout.setLayoutParams(descParams);

//        LayoutParams likeParams = (LayoutParams) coverLike.getLayoutParams();
//        likeParams.height = (int) (likeParams.height*Utils.getHeightScale(getContext()));
//        coverLike.setLayoutParams(likeParams);

        if (mPhotoItem.getType() == 1) {
            coverTag.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.tag));
        } else {
            coverTag.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.tag_zuopin));
        }

        coverName.setText(mPhotoItem.getNickname());
        coverTime.setText(mPhotoItem.getUpdateTimeStr());
        ImageLoader.getInstance().displayImage(mPhotoItem.getAvatarURL(), coverAvatar, mAvatarOptions);
        coverAvatar.setUserId(mPhotoItem.getUid());

        coverCover = new ImageView(mContext);
        coverCover.setLayoutParams
                (new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        ImageLoader.getInstance().displayImage(mPhotoItem.getImageURL(), coverCover, mOptions, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                coverBack.setImageBitmap(BitmapUtils.getBlurBitmap(bitmap));
//                if(bitmap.getHeight()>bitmap.getWidth()){
//                }else{
//                    ViewGroup.LayoutParams params = view.getLayoutParams();
//                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
//                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
//                    view.setLayoutParams(params);
//                }
            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
//        LayoutParams areaParams = (LayoutParams) coverImgArea.getLayoutParams();
//        areaParams.height = (int)((float)areaParams.height * Utils.getHeightScale(mContext));
//        coverImgArea.setLayoutParams(areaParams);
        coverImgArea.addView(coverCover);

        coverDesc.setHtmlFromString(mPhotoItem.getDesc(), true);
        coverComment.setText(String.valueOf(mPhotoItem.getCommentCount()));
        coverShare.setText(String.valueOf(mPhotoItem.getShareCount()));
        coverLike.setmPhotoItem(mPhotoItem);
        coverLike.updateLikeView();
        if (mPhotoItem.getType() == 1) {
            coverLike.setVisibility(GONE);
            coverBang.setVisibility(VISIBLE);
        } else {
            coverLike.setVisibility(VISIBLE);
            coverBang.setVisibility(GONE);
        }

    }

    private OnClickListener commentClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(mContext, CommentListActivity.class);
            intent.putExtra(Constants.IntentKey.PHOTO_ITEM, mPhotoItem);
            mContext.startActivity(intent);
        }
    };

    private OnClickListener shareClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            ShareMoreDialog shareMoreDialog = new ShareMoreDialog(mContext);
            shareMoreDialog.setPhotoItem(mPhotoItem);
            shareMoreDialog.show();
        }
    };

    private void initListener(final View view) {
        coverShare.setOnClickListener(shareClick);
        coverShareImg.setOnClickListener(shareClick);
        coverComment.setOnClickListener(commentClick);
        coverCommentImg.setOnClickListener(commentClick);

        coverBang.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                PSDialog psDialog = new PSDialog(mContext);
                psDialog.setPhotoItem(mPhotoItem);
                psDialog.show();
            }
        });

        view.setOnTouchListener(new View.OnTouchListener() {
            float downY;
            float leftX;
            float Y;
            float oY = view.getY();
            float moveY = 0;
            float moveX = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

//                switch (motionEvent.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        downY = motionEvent.getRawY();
//                        leftX = motionEvent.getRawX();
//                        Y = view.getY();
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        moveY = motionEvent.getRawY() - downY;
//                        moveX = motionEvent.getRawX() - leftX;
//                        if ((isBlow && moveY < 0) || (Math.abs(moveY) < 30 && Math.abs(moveX) > 5)
//                                || (!isAnimEnd && moveY > 100) || (!isDown && !isAnimEnd)) {
//
//                        } else {
//                            ViewHelper.setTranslationY(view, moveY);
//                        }
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        view.setY(0);
//                        if (!isDown && moveY <= 0) {
//                            viewPagerBlow(84);
//                        } else {
//                            isDown = false;
//                        }
//                        break;
//                }
//                if (Utils.pxToDp(mContext, view.getY()) < -70 && moveY < 0) {
//                    view.setY(oY);
//                    viewPagerBlow(40);
//                } else if (Utils.pxToDp(mContext, view.getY()) > -75 && moveY > 0 && isBlow) {
//                    viewPagerRestore();
//                } else if (Utils.pxToDp(mContext, view.getY()) > 150 && isAnimEnd) {
//                    if (onEndListener != null) {
//                        onEndListener.onEnd();
//                    }
//                }
                if (isOrigin) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            downY = motionEvent.getRawY();
                            leftX = motionEvent.getRawX();
                            Y = view.getY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            moveY = motionEvent.getRawY() - downY;
                            moveX = motionEvent.getRawX() - leftX;
                            if ((isBlow && moveY < 0) || (Math.abs(moveY) < 35) || (Math.abs(moveX) > 20 && Math.abs(moveY) < 40)
                                    || (!isAnimEnd && moveY > 100) || (!isDown && !isAnimEnd)) {

                            } else {
                                ViewHelper.setTranslationY(view, moveY);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
//
//                        if (!isDown && moveY <= 0) {
//                            viewPagerBlow(84);
//                        } else {
//                            isDown = false;
//                        }
                            if (Utils.pxToDp(mContext, view.getY()) < -55 && moveY < 0) {
                                view.setY(oY);
                                viewPagerBlow(40);
                            } else if (Utils.pxToDp(mContext, view.getY()) > -50 && moveY > 0 && isBlow) {
                                viewPagerRestore(84);
                            }
                            if (Utils.pxToDp(mContext, view.getY()) > 150 && isAnimEnd && !isBlow) {
                                if (onEndListener != null) {
                                    onEndListener.onEnd();
                                }
                            }
                            if (isAnimEnd) {
                                goOrigin(view.getY());
                            }
                            break;
                    }
                }
                return true;
            }
        });
    }

    private void goOrigin(final float top) {
        isAnimEnd = false;
        isOrigin = false;
        final AnimatorSet anim = new AnimatorSet();
        anim.setDuration(250);
        ValueAnimator yAnim = ValueAnimator.ofFloat(top, 0);
        yAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Float value = (Float) valueAnimator.getAnimatedValue();
                parent.setY(value);
            }
        });
        yAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isAnimEnd = true;
                isOrigin = true;
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        anim.playTogether(yAnim);
        anim.start();
    }

    private void viewPagerBlow(final float top) {
        isDown = false;
//        mScroll.setCanScroll(true);
        if (!isBlow && vp != null) {
            setY(0);
            initBack();
            isBlow = true;
            final AnimatorSet anim = new AnimatorSet();
            anim.setDuration(200);
            ValueAnimator xAnim = ValueAnimator.ofInt(20, 0);
            xAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Integer value = (Integer) valueAnimator.getAnimatedValue();
                    RelativeLayout.LayoutParams vParams = (RelativeLayout.LayoutParams) vp.getLayoutParams();
                    vParams.setMargins(Utils.dpToPx(mContext, value),
                            Utils.dpToPx(mContext, top / 20f * value + 48), Utils.dpToPx(mContext, value), 0);
                    vp.setLayoutParams(vParams);
                }
            });
            ObjectAnimator scrollAnim = ObjectAnimator.ofFloat(mScroll, "alpha", 0.1f, 1f);
            scrollAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator)
                {
                    mScroll.setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    isAnimEnd = true;
                    RelativeLayout.LayoutParams cParams = (RelativeLayout.LayoutParams) mCover.getLayoutParams();
                    cParams.setMargins(0,
                            0, 0, 0);
                    mCover.setLayoutParams(cParams);
                    mCover.setVisibility(GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            ObjectAnimator coverAnim = ObjectAnimator.ofFloat(mCover, "alpha", 1f, 0f);
            xAnim.addListener(blowAnimListener);
            anim.playTogether(xAnim,coverAnim);
            anim.start();
            AnimatorSet anim2 = new AnimatorSet();
            anim2.setStartDelay(250);
            anim2.setDuration(250);
            anim2.play(scrollAnim);
            anim2.start();
        }
    }

    private void viewPagerRestore(final float top) {
        if (vp == null) {
            return;
        }
        isDown = true;
        isBlow = false;
        final AnimatorSet anim = new AnimatorSet();
        anim.setDuration(200);
        ValueAnimator xAnim = ValueAnimator.ofInt(0, 20);
        xAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Integer value = (Integer) valueAnimator.getAnimatedValue();
                RelativeLayout.LayoutParams vParams = (RelativeLayout.LayoutParams) vp.getLayoutParams();
                vParams.setMargins(Utils.dpToPx(mContext, value),
                        Utils.dpToPx(mContext, top / 20f * (float) value - 48), Utils.dpToPx(mContext, (float) value), 0);
                vp.setLayoutParams(vParams);
            }
        });
        ObjectAnimator scrollAnim = ObjectAnimator.ofFloat(mScroll, "alpha", 1f, 0.0f);
        xAnim.addListener(restoreAnimListener);
        anim.playTogether(xAnim, scrollAnim);
        anim.start();

        ObjectAnimator coverAnim = ObjectAnimator.ofFloat(mCover, "alpha", 0.1f, 1f);
        coverAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                RelativeLayout.LayoutParams vParams = (RelativeLayout.LayoutParams) vp.getLayoutParams();
                vParams.setMargins(Utils.dpToPx(mContext,20),
                        Utils.dpToPx(mContext,84), Utils.dpToPx(mContext,20), 0);
                vp.setLayoutParams(vParams);
                mCover.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                vp.setOffscreenPageLimit(3);
                vp.setClipChildren(false);
                if (vp instanceof StopViewPager) {
                    ((StopViewPager) vp).setCanScroll(true);
                }
                vp.setAdapter(thumbAdatper);
                int position = thumbAdatper.getItemPosition(CarouselPhotoDetailView.this);
                vp.setCurrentItem(position == -1 ? 0 : position);
                isAnimEnd = true;
                isCover = true;
                mScroll.setVisibility(GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        AnimatorSet anim2 = new AnimatorSet();
        anim2.setStartDelay(250);
        anim2.setDuration(250);
        anim2.play(coverAnim);
        anim2.start();
    }

    ViewPagerAdapter adapter;
    ViewPagerAdapter thumbAdatper;

    Animator.AnimatorListener blowAnimListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {
            vp.setOffscreenPageLimit(0);
            vp.setClipChildren(true);
            if (vp instanceof StopViewPager) {
                ((StopViewPager) vp).setCanScroll(false);
            }
            thumbAdatper = (ViewPagerAdapter) vp.getAdapter();
            List<View> list = new ArrayList<View>();
            list.add(CarouselPhotoDetailView.this);
            adapter = new ViewPagerAdapter(list);
            vp.setAdapter(adapter);
            isAnimEnd = false;
            isCover = false;
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            RelativeLayout.LayoutParams vParams = (RelativeLayout.LayoutParams) vp.getLayoutParams();
            vParams.setMargins(0,
                    0, 0, 0);
            vp.setLayoutParams(vParams);
            RelativeLayout.LayoutParams cParams = (RelativeLayout.LayoutParams) mCover.getLayoutParams();
            cParams.setMargins(0,
                    Utils.dpToPx(mContext,48), 0, 0);
            mCover.setLayoutParams(cParams);
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };

    Animator.AnimatorListener restoreAnimListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {
            vp.setOffscreenPageLimit(0);
            isAnimEnd = false;
            isCover = false;
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            parent.setY(0);
            setY(Utils.dpToPx(mContext,-10));
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };

    public interface OnEndListener {
        void onEnd();
    }

    private OnEndListener onEndListener;

    public void setOnEndListener(OnEndListener onEndListener) {
        this.onEndListener = onEndListener;
    }

    //是否是放大的状态
    private boolean isBlow = false;

    //防止放大和缩小动画冲突
    private boolean isDown = false;

    //当前是否有动画
    private boolean isAnimEnd = true;

    //判断是否为表层观看页
    private boolean isCover = true;

    //判断回弹动画是否结束
    private boolean isOrigin = true;

}
