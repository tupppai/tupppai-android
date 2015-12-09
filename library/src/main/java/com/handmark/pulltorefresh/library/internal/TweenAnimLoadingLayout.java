package com.handmark.pulltorefresh.library.internal;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Orientation;
import com.handmark.pulltorefresh.library.R;

public class TweenAnimLoadingLayout extends LoadingLayout {

	private AnimationDrawable animationDrawable;

	public TweenAnimLoadingLayout(Context context, Mode mode,
			Orientation scrollDirection, TypedArray attrs) {
		super(context, mode, scrollDirection, attrs);
		
		// ��ʼ��
		mHeaderImage.setImageResource(R.anim.refresh_anim);
		animationDrawable = (AnimationDrawable) mHeaderImage.getDrawable();
	}
	
	// Ĭ��ͼƬ
	@Override
	protected int getDefaultDrawableResId() {
		return R.drawable.loading_1;
	}
	
	@Override
	protected void onLoadingDrawableSet(Drawable imageDrawable) {
		// NO-OP
	}
	
	@Override
	protected void onPullImpl(float scaleOfLayout) {
		// NO-OP
	}
	// ������ˢ��
	@Override
	protected void pullToRefreshImpl() {
		// NO-OP
	}
	// ����ˢ��ʱ�ص�
	@Override
	protected void refreshingImpl() {
		// ����֡����
		animationDrawable.start();
	}
	// �ͷ���ˢ��
	@Override
	protected void releaseToRefreshImpl() {
		// NO-OP
	}
	// ��������
	@Override
	protected void resetImpl() {
		mHeaderImage.setVisibility(View.VISIBLE);
		mHeaderImage.clearAnimation();
	}

}
