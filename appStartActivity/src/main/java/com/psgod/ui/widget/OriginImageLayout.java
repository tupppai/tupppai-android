package com.psgod.ui.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.psgod.Constants;
import com.psgod.PsGodImageLoader;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.ImageData;

import java.util.List;

/**
 * 原图区域视图
 */

public class OriginImageLayout extends RelativeLayout {
    private Context mContext;
    private ImageView mBackground;

    private PsGodImageLoader imageLoader = PsGodImageLoader.getInstance();
    private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS;

    private ObjectAnimator alphaAnimator = null;
    private ObjectAnimator alphaIvAnimator = null;
    private ValueAnimator zoomWidthAnimator = null;
    private ValueAnimator zoomHeightAnimator = null;

    private ImageView thumbTipImage = null;
    private ImageView thumbImageView = null;

    private int thumbImageWidth = 0;
    private int thumbImageHeight = 0;
    private int originImageWidth = 0;
    private int originImageHeight = 0;
    private float imageScaleValue = 0f;

    private int baseThumbHeight = 0;
    private int clickNum = 0;

    private List<ImageData> images = null;

    OriginImageLayout uploadLayout = this;
    RelativeLayout overlapLayout = null;

    public OriginImageLayout(Context context) {
        super(context);
        mContext = context;
    }

    public OriginImageLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
    }

    public OriginImageLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    public void setImages(List<ImageData> images) {
        this.images = images;
    }

    public void setBackground(ImageView mBackground) {
        this.mBackground = mBackground;

    }

    public void init() {
        baseThumbHeight = Utils.dpToPx(mContext, 100);
        // layout
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                baseThumbHeight, baseThumbHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        uploadLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
        uploadLayout.setLayoutParams(layoutParams);

        if (images.size() == 1) {
            this.setVisibility(VISIBLE);
            initSingleImage(images.get(0));
        } else if (images.size() == 2) {
            this.setVisibility(VISIBLE);
            initOverlapImage(images.get(0), images.get(1));
        } else {
            this.setVisibility(GONE);
        }

        uploadLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                uploadLayout.setEnabled(false);

                if (mBackground.getVisibility() == View.INVISIBLE) {
                    mActionZoomIn();
                } else {
                    mActionZoomOut();
                }
            }
        });
        imageScaleValue = (float) Constants.WIDTH_OF_SCREEN
                / Utils.dpToPx(mContext, 100);
    }

    ImageView mImageViewLeft;
    ImageView mImageViewRight;

    public void initOverlapImage(final ImageData originImage1,
                                 final ImageData originImage2) {
        mImageViewLeft = new ImageView(mContext);
        mImageViewRight = new ImageView(mContext);
        RelativeLayout.LayoutParams leftParams = new RelativeLayout.LayoutParams(
                baseThumbHeight, baseThumbHeight);
        leftParams.addRule(ALIGN_PARENT_LEFT, TRUE);
        leftParams.setMargins(-baseThumbHeight / 2, 0, 0, 0);
        mImageViewLeft.setScaleType(ScaleType.CENTER_CROP);
        mImageViewLeft.setLayoutParams(leftParams);

        RelativeLayout.LayoutParams rightParams = new RelativeLayout.LayoutParams(
                baseThumbHeight, baseThumbHeight);
        rightParams.addRule(ALIGN_PARENT_RIGHT, TRUE);
        rightParams.setMargins(0, 0, -baseThumbHeight / 2, 0);
        mImageViewRight.setScaleType(ScaleType.CENTER_CROP);
        mImageViewRight.setLayoutParams(rightParams);

        imageLoader.displayImage(originImage1.mImageUrl, mImageViewLeft, mOptions);
        imageLoader.displayImage(originImage2.mImageUrl, mImageViewRight, mOptions);

        uploadLayout.addView(mImageViewLeft);
        uploadLayout.addView(mImageViewRight);

        mImageViewLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mBackground.getVisibility() == View.VISIBLE) {
                    clickNum = 0;
                    mActionZoomOut();
                } else {
                    mActionZoomIn();
                }
            }
        });
        mImageViewRight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mBackground.getVisibility() == View.VISIBLE) {
                    clickNum = 1;
                    mActionZoomOut();
                } else {
                    mActionZoomIn();
                }
            }
        });

        initTipView();
    }

    public void initOverlapImage(final ImageData originImage1,
                                 final ImageData originImage2, boolean isLeft) {
        initOverlapImage(originImage1, originImage2);
        if (isLeft) {
            LayoutParams params = (LayoutParams) mImageViewLeft.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            mImageViewLeft.setLayoutParams(params);
            LayoutParams rparams = (LayoutParams) mImageViewRight.getLayoutParams();
            rparams.setMargins(0, 0, -baseThumbHeight, 0);
            mImageViewRight.setLayoutParams(rparams);
        } else {
            LayoutParams params = (LayoutParams) mImageViewLeft.getLayoutParams();
            params.setMargins(-baseThumbHeight, 0, 0, 0);
            mImageViewLeft.setLayoutParams(params);
            LayoutParams rparams = (LayoutParams) mImageViewRight.getLayoutParams();
            rparams.setMargins(0, 0, 0, 0);
            mImageViewRight.setLayoutParams(rparams);
        }
    }

    public void initThumb(ImageData originImage) {
        thumbImageWidth = baseThumbHeight;
        thumbImageHeight = baseThumbHeight;
        originImageWidth = Constants.WIDTH_OF_SCREEN;
        originImageHeight = Constants.WIDTH_OF_SCREEN;

        if (originImage.mImageHeight > originImage.mImageWidth) {
            thumbImageWidth = thumbImageHeight * originImage.mImageWidth
                    / originImage.mImageHeight;
            originImageWidth = originImageHeight * originImage.mImageWidth
                    / originImage.mImageHeight;
        } else {
            thumbImageHeight = thumbImageWidth * originImage.mImageHeight
                    / originImage.mImageWidth;
            originImageHeight = originImageWidth * originImage.mImageHeight
                    / originImage.mImageWidth;
        }
    }

    public void initSingleImage(ImageData originImage) {
        initThumb(originImage);
        // image view
        thumbImageView = new ImageView(mContext);
        RelativeLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(CENTER_IN_PARENT);
        thumbImageView.setLayoutParams(params);
        thumbImageView.setScaleType(ScaleType.FIT_CENTER);
        imageLoader.displayImage(originImage.mImageUrl, thumbImageView, mOptions, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                thumbBitmap = bitmap;
            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
        uploadLayout.addView(thumbImageView);

        initTipView();
    }

    Bitmap thumbBitmap;

    private void
    initTipView() {
        // 左上角 原图标示
        thumbTipImage = new ImageView(mContext);
        RelativeLayout.LayoutParams originTipParams = new RelativeLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        originTipParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        originTipParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        originTipParams.setMargins(0, Utils.dpToPx(mContext, 4), 0, 0);
        thumbTipImage.setLayoutParams(originTipParams);
        thumbTipImage.setBackgroundResource(R.mipmap.tag_single_ori);
        uploadLayout.addView(thumbTipImage);
    }

    //动画开始标记
    boolean animStart = false;

    // 是否恢复
    boolean isRestore = true;

    private void mActionZoomIn() {

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(300);
        final ImageView scaleImage = images.size() == 2 ? clickNum == 0 ? mImageViewLeft : mImageViewRight : thumbImageView;


        // 缩略图缩小
        alphaIvAnimator = ObjectAnimator.ofFloat(mBackground, "alpha", 0, 1f);
        alphaAnimator = ObjectAnimator.ofFloat(scaleImage, "alpha", 1f,
                0.9f);

        zoomWidthAnimator = ValueAnimator.ofInt(Constants.WIDTH_OF_SCREEN,
                baseThumbHeight);
        zoomWidthAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int height = (Integer) animation.getAnimatedValue();
                LayoutParams params = (LayoutParams) uploadLayout
                        .getLayoutParams();
                params.height = height;
                uploadLayout.setLayoutParams(params);
            }
        });
        zoomHeightAnimator = ValueAnimator.ofInt(Constants.WIDTH_OF_SCREEN,
                baseThumbHeight);
        zoomHeightAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int width = (Integer) animation.getAnimatedValue();

                if (width < (baseThumbHeight + (Constants.WIDTH_OF_SCREEN - baseThumbHeight) / 11) && isRestore && images.size() == 2) {
                    uploadLayout.removeAllViews();
                    initOverlapImage(images.get(0), images.get(1), clickNum == 0 ? true : false);
                    isRestore = false;
                }

                LayoutParams params = (LayoutParams) uploadLayout
                        .getLayoutParams();
                params.width = width;
                uploadLayout.setLayoutParams(params);
            }
        });

        animatorSet.addListener(new AnimatorListener() {
            @Override
            public void onAnimationCancel(Animator arg0) {
                uploadLayout.setEnabled(true);
            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                uploadLayout.setEnabled(true);
                LayoutParams params = (LayoutParams) scaleImage.getLayoutParams();
                params.height = LayoutParams.MATCH_PARENT;
                params.width = LayoutParams.MATCH_PARENT;
                scaleImage.setLayoutParams(params);
                // 当size为2 的时候，展示两个
                if (images.size() == 2) {
                    mActionZoomInIn();
                }
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
            }

            @Override
            public void onAnimationStart(Animator arg0) {
                mBackground.setVisibility(VISIBLE);

            }
        });
        animatorSet.playTogether(zoomWidthAnimator, zoomHeightAnimator,
                alphaAnimator, alphaIvAnimator);
        animatorSet.start();
    }

    private void mActionZoomInIn() {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(300);
        ValueAnimator animator = ValueAnimator.ofInt(0, baseThumbHeight / 2);

        animator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int width = (Integer) valueAnimator.getAnimatedValue();

                if (clickNum == 0) {
                    LayoutParams rparams = (LayoutParams) mImageViewRight.getLayoutParams();
                    rparams.setMargins(0, 0, width - baseThumbHeight, 0);
                    mImageViewRight.setLayoutParams(rparams);
                    LayoutParams lparams = (LayoutParams) mImageViewLeft.getLayoutParams();
                    lparams.setMargins(-width, 0, 0, 0);
                    mImageViewLeft.setLayoutParams(lparams);
                } else {
                    LayoutParams rparams = (LayoutParams) mImageViewRight.getLayoutParams();
                    rparams.setMargins(0, 0, -width, 0);
                    mImageViewRight.setLayoutParams(rparams);
                    LayoutParams lparams = (LayoutParams) mImageViewLeft.getLayoutParams();
                    lparams.setMargins(width - baseThumbHeight, 0, 0, 0);
                    mImageViewLeft.setLayoutParams(lparams);
                }
            }
        });
        animator.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isRestore = true;
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animatorSet.play(animator);
        animatorSet.start();
    }

    private void mActionZoomOut() {
        if (animStart) {
            return;
        }
        animStart = true;
        if (images.size() == 2) {
            AnimatorSet animatorSet = new AnimatorSet();
            ValueAnimator animator = ValueAnimator.ofInt(baseThumbHeight / 2, 0);
            animator.setDuration(300);
            animator.addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int width = (Integer) valueAnimator.getAnimatedValue();
                    if (clickNum == 0) {
                        LayoutParams rparams = (LayoutParams) mImageViewRight.getLayoutParams();
                        rparams.setMargins(0, 0, width - baseThumbHeight, 0);
                        mImageViewRight.setLayoutParams(rparams);
                        LayoutParams lparams = (LayoutParams) mImageViewLeft.getLayoutParams();
                        lparams.setMargins(-width, 0, 0, 0);
                        mImageViewLeft.setLayoutParams(lparams);
                    } else {
                        LayoutParams params = (LayoutParams) mImageViewRight.getLayoutParams();
                        params.setMargins(0, 0, -width, 0);
                        mImageViewRight.setLayoutParams(params);
                        LayoutParams lparams = (LayoutParams) mImageViewLeft.getLayoutParams();
                        lparams.setMargins(width - baseThumbHeight, 0, 0, 0);
                        mImageViewLeft.setLayoutParams(lparams);
                    }
                }
            });
            animator.addListener(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    mActionZoomOutOut();
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            animatorSet.play(animator);
            animatorSet.start();
        } else {
            mActionZoomOutOut();
        }

    }

    private void mActionZoomOutOut() {
        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(300);

        zoomWidthAnimator = ValueAnimator.ofInt(baseThumbHeight,
                Constants.WIDTH_OF_SCREEN);
        zoomWidthAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int height = (Integer) animation.getAnimatedValue();
                LayoutParams params = (LayoutParams) uploadLayout
                        .getLayoutParams();
                params.height = height;
                uploadLayout.setLayoutParams(params);
            }
        });
        zoomHeightAnimator = ValueAnimator.ofInt(baseThumbHeight,
                Constants.WIDTH_OF_SCREEN);
        zoomHeightAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int width = (Integer) animation.getAnimatedValue();
                LayoutParams params = (LayoutParams) uploadLayout
                        .getLayoutParams();
                params.width = width;
                uploadLayout.setLayoutParams(params);
            }
        });

        ImageView scaleImage = images.size() == 2 ? clickNum == 0 ? mImageViewLeft : mImageViewRight : thumbImageView;
        final RelativeLayout.LayoutParams uploadImageParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        uploadImageParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        scaleImage.setLayoutParams(uploadImageParams);
        scaleImage.setScaleType(ScaleType.FIT_CENTER);

        // 缩略图放大
        alphaIvAnimator = ObjectAnimator.ofFloat(mBackground, "alpha", 1f, 0);
        alphaAnimator = ObjectAnimator
                .ofFloat(scaleImage, "alpha", 0.5f, 1);

        animatorSet.addListener(new AnimatorListener() {
            @Override
            public void onAnimationCancel(Animator arg0) {
                uploadLayout.setEnabled(true);
            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                uploadLayout.setEnabled(true);
                mBackground.setVisibility(INVISIBLE);
                animStart = false;
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {

            }

            @Override
            public void onAnimationStart(Animator arg0) {

            }
        });
        animatorSet.playTogether(zoomWidthAnimator, zoomHeightAnimator,
                alphaAnimator, alphaIvAnimator);
        animatorSet.start();
    }
}
