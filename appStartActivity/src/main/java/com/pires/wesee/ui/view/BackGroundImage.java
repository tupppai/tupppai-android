package com.pires.wesee.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.pires.wesee.eventbus.ImgRefreshEvent;

import de.greenrobot.event.EventBus;

public class BackGroundImage extends View {
	private int mPosition;
	private float mDegree;
	private Drawable[] mDrawableLists;
	private int mPrePosition = 0;
	private Drawable mNext;

	public void setmDrawableLists(Drawable[] mDrawableLists) {
		this.mDrawableLists = mDrawableLists;
		if (mDrawableLists.length > 1) {
			mNext = mDrawableLists[1];// 设置下一个背景图片的drawable
		}
	}

	public void setmPosition(int mPosition) {
		this.mPosition = mPosition;
	}

	public void setmDegree(float mDegree) {
		this.mDegree = mDegree;
	}

	public BackGroundImage(Context context) {
		super(context);
	}

	public BackGroundImage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BackGroundImage(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (null == mDrawableLists) {
			return;
		}
		int alpha1 = (int) (255 - (mDegree * 255));
		if (mPosition < mDrawableLists.length) {
			Drawable fore = mDrawableLists[mPosition];
			if (fore != null && fore instanceof Drawable && mNext != null) {
				try {
					fore.setBounds(0, 0, getWidth(), getHeight());
					mNext.setBounds(0, 0, getWidth(), getHeight());
					if (mPrePosition != mPosition) {// 边界判断
						if (mPosition != mDrawableLists.length - 1) {
							mNext = mDrawableLists[mPosition + 1];
						} else {
							mNext = mDrawableLists[mPosition];
						}
					}
					fore.setAlpha(alpha1);// 淡出
					mNext.setAlpha(255);
					mNext.draw(canvas);
					fore.draw(canvas);
					mPrePosition = mPosition;
				} catch (NullPointerException e) {
					EventBus.getDefault().post(new ImgRefreshEvent());
				} catch (Exception e) {
					EventBus.getDefault().post(new ImgRefreshEvent());
				}

			}
		}
		super.onDraw(canvas);
	}
}
