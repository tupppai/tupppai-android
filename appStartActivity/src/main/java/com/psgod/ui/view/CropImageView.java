package com.psgod.ui.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;

import com.psgod.Utils;

/**
 * 带裁剪功能的ImageView
 * 
 * @author Rayal
 * 
 */
public class CropImageView extends ImageView {
	private static final String TAG = CropImageView.class.getSimpleName();

	// 拖拉，放缩参数
	private float px = 0;
	private float py = 0;
	private float mOldDistance;
	private float mMaxScale;
	private Point mCenter = new Point();

	private int mCropRectWidth;
	private int mCropoRectHeight;
	private int mImageWidth;
	private int mImageHeight;

	// 触摸状态
	private final int STATE_TOUCH_NONE = 0;
	private final int STATE_TOUCH_SINGLE = 1; // 单点
	private final int STATE_TOUCH_ZOOM = 2; // 放缩
	private final int STATE_TOUCH_MUTLI = 3; // 多点触摸（大于等于3点）
	private int mState = STATE_TOUCH_NONE;

	// 默认的裁剪图片宽度与高度
	private final int defaultCropWidth = 300;
	private final int defaultCropHeight = 300;
	private int cropWidth = defaultCropWidth;
	private int cropHeight = defaultCropHeight;

	private static final float DEFAULT_RATIO = 0.0f;
	private float mRatio = DEFAULT_RATIO;

	private boolean mIsViewReady = false;
	private boolean mUseOwnCropZone = false; // 用户自定义裁剪区域的大小

	protected float oriRationWH = 0;// 原始宽高比率
	protected final float maxZoomOut = 5.0f;// 最大扩大到多少倍
	protected final float minZoomIn = 0.333333f;// 最小缩小到多少倍

	protected Drawable mDrawable;// 原图
	protected Rect mDrawableSrcRect = new Rect();
	protected Rect mDrawableDstRect = new Rect();
	protected Rect mRegionRect = new Rect();
	protected Rect mCropRect = new Rect();// 浮层选择框，就是头像选择框
	protected boolean isFrist = true;
	private boolean isTouchInSquare = true;

	private boolean mIsCropable = true; // 是否裁剪状态
	protected Context mContext;

	private Paint mLinePaint = new Paint();
	{
		// mLinePaint.setARGB(200, 50, 50, 50);
		mLinePaint.setStrokeWidth(2F);
		mLinePaint.setStyle(Paint.Style.STROKE);
		mLinePaint.setAntiAlias(true);
		mLinePaint.setColor(Color.parseColor("#00ADEF"));
	}

	public CropImageView(Context context) {
		super(context);
		init(context);
	}

	public CropImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		init(context);
	}

	public CropImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void init(Context context) {
		this.mContext = context;
		try {
			if (android.os.Build.VERSION.SDK_INT >= 11) {
				this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		ViewTreeObserver viewTreeObserver = getViewTreeObserver();
		viewTreeObserver
				.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
					@SuppressLint("NewApi")
					@Override
					public void onGlobalLayout() {
						if (Build.VERSION.SDK_INT < 16) {
							getViewTreeObserver().removeGlobalOnLayoutListener(
									this);
						} else {
							getViewTreeObserver().removeOnGlobalLayoutListener(
									this);
						}

						if (mIsViewReady) {
							return;
						}

						mIsViewReady = true;
						setupCropZone();
					}
				});
	}

	private void setupCropZone() {
		if (mUseOwnCropZone) {
			// TODO

		} else if (!mIsViewReady
				|| (Utils.isFloatEquals(mRatio, DEFAULT_RATIO))) {
			// 还没初始化好
			return;
		} else {
			mDrawable = CropImageView.super.getDrawable();
			Matrix matrix = getImageMatrix();
			float[] values = new float[10];
			matrix.getValues(values);
			Rect rect = mDrawable.getBounds();
			mImageWidth = (int) (values[0] * rect.width());
			mImageHeight = (int) (values[4] * rect.height());

			float ratioOfDrawable = (float) mImageWidth / (float) mImageHeight;
			if (ratioOfDrawable > mRatio) {
				this.cropWidth = (int) (mImageHeight * mRatio);
				this.cropHeight = mImageHeight;
			} else {
				this.cropWidth = mImageWidth;
				this.cropHeight = (int) (mImageWidth / mRatio);
			}

			mDrawableSrcRect.set(mDrawable.getBounds());
			mDrawableDstRect.set(mDrawableSrcRect);

			// 设置裁剪区域
			int cropRectLeft = (getWidth() - cropWidth) / 2;
			int cropRectTop = (getHeight() - cropHeight) / 2;
			mCropRect.set(cropRectLeft, cropRectTop, cropRectLeft + cropWidth,
					cropRectTop + cropHeight);

			// 设置图片区域
			int regionLeft = (getWidth() - mImageWidth) / 2;
			int regionTop = (getHeight() - mImageHeight) / 2;
			mRegionRect.set(regionLeft, regionTop, regionLeft + mImageWidth,
					regionTop + mImageHeight);
		}

		invalidate();
	}

	public void setIsCropable(boolean isCropable) {
		this.mIsCropable = isCropable;
		invalidate();
	}

	public void setRatio(float ratio) {
		this.mDrawable = super.getDrawable();

		if (mDrawable == null) {
			throw new IllegalArgumentException(
					"setRatio(): the drawable is null");
		}

		// 如果ratio为0，则表示原图
		mRatio = ratio;
		setupCropZone();
	}

	public void setCropSize(int width, int height) {
		// TODO 检查一下width和height吧
		cropWidth = width;
		cropHeight = height;
		mUseOwnCropZone = true;
		setupCropZone();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!mIsCropable) {
			return super.onTouchEvent(event);
		}

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mState = STATE_TOUCH_SINGLE;
			px = event.getX();
			py = event.getY();
			isTouchInSquare = mCropRect.contains((int) px, (int) py);
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			if (event.getPointerCount() > 2) {
				mState = STATE_TOUCH_MUTLI;
			} else {
				mState = STATE_TOUCH_ZOOM;
				mOldDistance = getDistance(event);
				mCenter.set((mCropRect.left + mCropRect.right) / 2,
						(mCropRect.top + mCropRect.bottom) / 2);
				mCropRectWidth = mCropRect.right - mCropRect.left;
				mCropoRectHeight = mCropRect.bottom - mCropRect.top;
				mMaxScale = Float.MAX_VALUE;
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			if (event.getPointerCount() == 2) {
				mState = STATE_TOUCH_NONE;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mState == STATE_TOUCH_ZOOM) {
				// 放缩
				float newDistance = getDistance(event);
				float scale = newDistance / mOldDistance;
				if (scale > mMaxScale) {
					scale = mMaxScale;
				}
				int newWidthOfCropRect = (int) (mCropRectWidth * scale);
				int newHeightOfCropRect = (int) (mCropoRectHeight * scale);
				// 判断是否撑满屏幕
				if (newWidthOfCropRect >= mImageWidth) {
					newWidthOfCropRect = mImageWidth;
					scale = (float) newWidthOfCropRect / (float) mCropRectWidth;
					newHeightOfCropRect = (int) (mCropoRectHeight * scale);
					mMaxScale = scale;
				} else if (newHeightOfCropRect >= mImageHeight) {
					newHeightOfCropRect = mImageHeight;
					scale = (float) newHeightOfCropRect
							/ (float) mCropoRectHeight;
					newWidthOfCropRect = (int) (mCropRectWidth * scale);
					mMaxScale = scale;
				}

				int left = mCenter.x - (int) (newWidthOfCropRect / 2.0f);
				int right = mCenter.x + (int) (newWidthOfCropRect / 2.0f);
				int top = mCenter.y - (int) (newHeightOfCropRect / 2.0f);
				int bottom = mCenter.y + (int) (newHeightOfCropRect / 2.0f);
				if ((left >= mRegionRect.left) && (right <= mRegionRect.right)
						&& (top >= mRegionRect.top)
						&& (bottom <= mRegionRect.bottom)) {
					mCropRect.set(left, top, right, bottom);
					mCropRect.sort();
					invalidate();
				}
			} else if (mState == STATE_TOUCH_SINGLE) {
				int dx = (int) (event.getX() - px);
				int dy = (int) (event.getY() - py);
				px = event.getX();
				py = event.getY();
				if ((dx != 0) || (dy != 0)) {
					// 拖动裁剪区域时，移动裁剪区域，并且保证不会移出屏幕
					if (isTouchInSquare) {
						dx = isInsideScreenWidth(dx) ? dx : 0;
						dy = isInsideScreenHeight(dy) ? dy : 0;
						if ((dx != 0) || (dy != 0)) {
							mCropRect.offset(dx, dy);
						}
					}
					mCropRect.sort();
					invalidate();
				}
			}
			break;
		}
		return true;
	}

	/**
	 * TODO: 安卓3.0以下，显示区域会有问题
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		// 不可裁剪状态，跟ImageView一样
		super.onDraw(canvas);

		if (!mIsCropable) {
			return;
		}

		// 判断图片是否为空
		boolean isDrawableEmpty = (mDrawable == null)
				|| (mDrawable.getIntrinsicWidth() == 0)
				|| (mDrawable.getIntrinsicHeight() == 0);
		if (isDrawableEmpty) {
			return;
		}

		// 绘制图片和裁剪区域
		canvas.save();
		canvas.clipRect(mCropRect, Region.Op.DIFFERENCE);
		canvas.drawColor(Color.parseColor("#B3000000")); // TODO
		canvas.restore();
		canvas.drawRect(mCropRect, mLinePaint);
	}

	/**
	 * 判断裁剪区域是否在屏幕范围内
	 * 
	 * @return
	 */
	protected boolean isInsideScreenWidth(int dx) {
		if ((mCropRect.left + dx < mRegionRect.left)
				|| (mCropRect.right + dx > mRegionRect.right)) {
			return false;
		}
		return true;
	}

	/**
	 * 判断裁剪区域是否在屏幕范围内
	 * 
	 * @return
	 */
	protected boolean isInsideScreenHeight(int dy) {
		if ((mCropRect.top + dy < mRegionRect.top)
				|| (mCropRect.bottom + dy > mRegionRect.bottom)) {
			return false;
		}
		return true;
	}

	public Bitmap getCropImage() {
		if (mDrawable == null) {
			this.mDrawable = super.getDrawable();
		}

		// TODO 有可能OOM
		Bitmap tmpBitmap = Bitmap.createBitmap(mDrawable.getIntrinsicWidth(),
				mDrawable.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(tmpBitmap);
		mDrawable.draw(canvas);
		if (!mIsCropable) {
			// 不可裁剪状态下，直接返回原图片
			return tmpBitmap;
		}
		Matrix matrix = getImageMatrix();
		float[] values = new float[10];
		matrix.getValues(values);
		int x = (int) ((mCropRect.left - mRegionRect.left) / values[0]);
		int y = (int) ((mCropRect.top - mRegionRect.top) / values[4]);
		int width = (int) (mCropRect.width() / values[0]);
		int height = (int) (mCropRect.height() / values[4]);
		Bitmap ret;
		if(x + width <= tmpBitmap.getWidth() && y + height <= tmpBitmap.getHeight()) {
			ret = Bitmap.createBitmap(tmpBitmap, x, y, width, height);
		}else{
			ret = tmpBitmap;
		}
		tmpBitmap.recycle();
		tmpBitmap = null;
		return ret;
	}

	/**
	 * 计算两个手指之间的距离
	 * 
	 * @param event
	 * @return
	 */
	private float getDistance(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}
}
