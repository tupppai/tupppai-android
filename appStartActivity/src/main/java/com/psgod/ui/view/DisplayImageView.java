package com.psgod.ui.view;

/**	
 * DisplayImage类
 * onTouch事件时图片变暗，touchCancel时图片恢复
 * @author brandwang
 */
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

public class DisplayImageView extends ImageView {
	// 触发long click的最短时间
	private static final int mMinLongPressTime = 400;
	// 触发click和long click的最短位移
	private static final int mMinTouchDistance = 10;

	private long mCurrentClickTime;
	private long mTouchUpTime;
	private Context mContext;

	private float mTouchStartLeft;
	private float mTouchStartTop;

	private float mEventX;
	private float mEventY;

	public DisplayImageView(Context context) {
		super(context);
		mContext = context;
	}

	public DisplayImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public DisplayImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mCurrentClickTime = Calendar.getInstance().getTimeInMillis();
			mTouchStartLeft = event.getX();
			mTouchStartTop = event.getY();
			// 在按下事件中设置滤镜
			setFilter();
			break;

		case MotionEvent.ACTION_UP:
			// touch up时判断是否有拖动 若太长则不触发click和long click
			mTouchUpTime = Calendar.getInstance().getTimeInMillis();
			float offsetX = Math.abs(event.getX() - mTouchStartLeft);
			float offsetY = Math.abs(event.getY() - mTouchStartTop);

			if (mTouchUpTime - mCurrentClickTime > mMinLongPressTime
					&& offsetX < mMinTouchDistance
					&& offsetX < mMinTouchDistance) {
				Toast.makeText(mContext, "long click", Toast.LENGTH_SHORT)
						.show();
				performLongClick();
			}
			if (mTouchUpTime - mCurrentClickTime <= mMinLongPressTime
					&& offsetX < mMinTouchDistance
					&& offsetX < mMinTouchDistance) {
				Toast.makeText(mContext, "single click", Toast.LENGTH_SHORT)
						.show();
				// 由于捕获了Touch事件，需要手动触发Click事件
				performLongClick();
			}
			removeFilter();
			break;

		case MotionEvent.ACTION_CANCEL:
			// 在CANCEL和UP事件中清除滤镜
			removeFilter();
			break;

		case MotionEvent.ACTION_MOVE:
			mEventX = event.getX();
			mEventY = event.getY();

			// 当手指touchmove到view之外，则取消滤镜效果
			if (mEventX < this.getX()
					|| mEventX > this.getX() + this.getWidth()
					|| mEventY < this.getY()
					|| mEventY > this.getY() + this.getHeight()) {
				removeFilter();
			}
			break;

		default:
			break;
		}
		return true;
	}

	/**
	 * 设置滤镜
	 */
	private void setFilter() {
		// 先获取设置的src图片
		Drawable drawable = getDrawable();
		// 当src图片为Null，获取背景图片
		if (drawable == null) {
			drawable = getBackground();
		}
		if (drawable != null) {
			// 设置滤镜
			int brightness = -50;
			ColorMatrix cMatrix = new ColorMatrix();
			cMatrix.set(new float[] { 1, 0, 0, 0, brightness, 0, 1, 0, 0,
					brightness,// 改变亮度
					0, 0, 1, 0, brightness, 0, 0, 0, 1, 0 });

			drawable.setColorFilter(new ColorMatrixColorFilter(cMatrix));
		}
	}

	/**
	 * 清除滤镜
	 */
	private void removeFilter() {
		// 先获取设置的src图片
		Drawable drawable = getDrawable();
		// 当src图片为Null，获取背景图片
		if (drawable == null) {
			drawable = getBackground();
		}
		if (drawable != null) {
			// 清除滤镜
			drawable.clearColorFilter();
		}
	}
}
