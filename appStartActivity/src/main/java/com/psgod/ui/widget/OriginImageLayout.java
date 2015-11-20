package com.psgod.ui.widget;

import java.util.List;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.ImageData;

public class OriginImageLayout extends RelativeLayout {
	private Context mContext;
	private ImageView mBackground;

	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS_SMALL;

	private ObjectAnimator scaleWidthAnimator = null;
	private ObjectAnimator scaleHeightAnimator = null;
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
			initSingleImage(images.get(0));
		} else if (images.size() == 2) {
			initOverlapImage(images.get(0), images.get(1));
		}

		uploadLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				uploadLayout.setEnabled(false);

				if (mBackground.getVisibility() == View.INVISIBLE) {
					mActionZoomInListener();
				} else {
					mActionZoomOutListener();
				}
			}
		});
	}

	public void initOverlapImage(final ImageData originImage1,
			final ImageData originImage2) {

		final ImageView mImageViewLeft = new ImageView(mContext);
		final ImageView mImageViewRight = new ImageView(mContext);

		RelativeLayout.LayoutParams leftParams = new RelativeLayout.LayoutParams(
				baseThumbHeight / 2, baseThumbHeight);
		leftParams.addRule(ALIGN_PARENT_LEFT, TRUE);
		mImageViewLeft.setLayoutParams(leftParams);
		mImageViewLeft.setScaleType(ScaleType.CENTER_CROP);

		RelativeLayout.LayoutParams rightParams = new RelativeLayout.LayoutParams(
				baseThumbHeight / 2, baseThumbHeight);
		rightParams.addRule(ALIGN_PARENT_RIGHT, TRUE);
		mImageViewRight.setLayoutParams(rightParams);
		mImageViewRight.setScaleType(ScaleType.CENTER_CROP);

		imageLoader.displayImage(originImage1.mImageUrl, mImageViewLeft, mOptions);
		imageLoader.displayImage(originImage2.mImageUrl, mImageViewRight, mOptions);

		uploadLayout.addView(mImageViewLeft);
		uploadLayout.addView(mImageViewRight);

		mImageViewLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// mImageViewRight.setVisibility(INVISIBLE);
				if (mBackground.getVisibility() == View.VISIBLE) {
					uploadLayout.removeAllViews();

					initSingleImage(originImage1);
					mActionZoomOutListener();
				}
			}
		});
		mImageViewRight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mBackground.getVisibility() == View.VISIBLE) {
					uploadLayout.removeAllViews();

					initSingleImage(originImage2);
					mActionZoomOutListener();
				}
			}
		});

		initTipView();
	}

	public void initSingleImage(ImageData originImage) {
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
		imageScaleValue = (float) Constants.WIDTH_OF_SCREEN
				/ Utils.dpToPx(mContext, 100);

		// image view
		thumbImageView = new ImageView(mContext);
		RelativeLayout.LayoutParams uploadImageParams = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		uploadImageParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		uploadImageParams.addRule(RelativeLayout.CENTER_VERTICAL);
		thumbImageView.setLayoutParams(uploadImageParams);
		thumbImageView.setScaleType(ScaleType.CENTER_INSIDE);
		// uploadImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

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

	private void initTipView() {
		// 左上角 原图标示
		thumbTipImage = new ImageView(mContext);
		RelativeLayout.LayoutParams originTipParams = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		originTipParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		originTipParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		thumbTipImage.setLayoutParams(originTipParams);
		thumbTipImage.setBackgroundResource(R.drawable.ic_yuantu);
		uploadLayout.addView(thumbTipImage);
	}

	private void mActionZoomInListener() {

		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.setDuration(300);

		// 缩略图缩小
		alphaIvAnimator = ObjectAnimator.ofFloat(mBackground, "alpha", 0, 1f);
		alphaAnimator = ObjectAnimator.ofFloat(thumbImageView, "alpha", 1f,
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
				LayoutParams params = (LayoutParams) uploadLayout
						.getLayoutParams();
				params.width = width;
				uploadLayout.setLayoutParams(params);
			}
		});
		scaleWidthAnimator = ObjectAnimator.ofFloat(thumbImageView, "scaleX",
				imageScaleValue, 1f);
		scaleHeightAnimator = ObjectAnimator.ofFloat(thumbImageView, "scaleY",
				imageScaleValue, 1f);

		animatorSet.addListener(new AnimatorListener() {
			@Override
			public void onAnimationCancel(Animator arg0) {
				uploadLayout.setEnabled(true);
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				uploadLayout.setEnabled(true);

				// 当size为2 的时候，展示两个
				if (images.size() == 2) {
					uploadLayout.removeAllViews();
					initOverlapImage(images.get(0), images.get(1));
				}

				LayoutParams params = (LayoutParams) thumbImageView.getLayoutParams();
				params.height = LayoutParams.MATCH_PARENT;
				params.width = LayoutParams.MATCH_PARENT;
				thumbImageView.setLayoutParams(params);
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
				alphaAnimator, alphaIvAnimator, scaleWidthAnimator,
				scaleHeightAnimator);
		animatorSet.start();
	};

	private void mActionZoomOutListener() {

		AnimatorSet animatorSet = new AnimatorSet();
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
		scaleWidthAnimator = ObjectAnimator.ofFloat(thumbImageView, "scaleX",
				1f, imageScaleValue);
		scaleHeightAnimator = ObjectAnimator.ofFloat(thumbImageView, "scaleY",
				1f, imageScaleValue);

		// 缩略图放大
		alphaIvAnimator = ObjectAnimator.ofFloat(mBackground, "alpha", 1f, 0);
		alphaAnimator = ObjectAnimator
				.ofFloat(thumbImageView, "alpha", 0.5f, 1);

		animatorSet.addListener(new AnimatorListener() {
			@Override
			public void onAnimationCancel(Animator arg0) {
				uploadLayout.setEnabled(true);
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				uploadLayout.setEnabled(true);
				mBackground.setVisibility(INVISIBLE);

			}

			@Override
			public void onAnimationRepeat(Animator arg0) {

			}

			@Override
			public void onAnimationStart(Animator arg0) {
				LayoutParams params = (LayoutParams) thumbImageView.getLayoutParams();
				params.height = thumbImageHeight;
				params.width = thumbImageWidth;
				thumbImageView.setLayoutParams(params);
			}
		});
		animatorSet.playTogether(zoomWidthAnimator, zoomHeightAnimator,
				alphaAnimator, alphaIvAnimator, scaleWidthAnimator,
				scaleHeightAnimator);
		animatorSet.start();
	};
}