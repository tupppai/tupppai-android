/**
 * @file XListViewHeader.java
 * @create Apr 18, 2012 5:22:27 PM
 * @author Maxwin
 * @description XListView's header
 */
package me.maxwin.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.huewu.pla.sample.R;

public class XListViewNewHeader extends LinearLayout {
	private LinearLayout mContainer;
//	private ImageView mArrowImageView;
//	private ProgressBar mProgressBar;
//	private TextView mHintTextView;
	
	private ImageView mHeaderImage;
	private ImageView mHeaderImageBottom;
	private int mState = STATE_NORMAL;

	private Animation mRotateUpAnim;
	private Animation mRotateDownAnim;
	
	private final int ROTATE_ANIM_DURATION = 180;
	
	public final static int STATE_NORMAL = 0;
	public final static int STATE_READY = 1;
	public final static int STATE_REFRESHING = 2;

	public XListViewNewHeader(Context context) {
		super(context);
		initView(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public XListViewNewHeader(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	private void initView(Context context) {
		// 初始情况，设置下拉刷新view高度为0
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, 0);
		mContainer = (LinearLayout) LayoutInflater.from(context).inflate(
				R.layout.xlistview_new_header, null);
		addView(mContainer, lp);
		setGravity(Gravity.BOTTOM);

//		mArrowImageView = (ImageView)findViewById(R.id.xlistview_header_arrow);
//		mHintTextView = (TextView)findViewById(R.id.xlistview_header_hint_textview);
//		mProgressBar = (ProgressBar)findViewById(R.id.xlistview_header_progressbar);
		
		mHeaderImage = (ImageView) findViewById(R.id.pull_to_refresh_image);
		mHeaderImageBottom = (ImageView) findViewById(R.id.pull_to_refresh_image_bottom);
		
		mHeaderImage.setImageResource(R.anim.refresh_anim);
		
		mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
		mRotateUpAnim.setFillAfter(true);
		mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
		mRotateDownAnim.setFillAfter(true);
	}

	public void setState(int state) {
		if (state == mState) return ;
		
		if (state == STATE_REFRESHING) {	// 显示进度
			mHeaderImage.clearAnimation();
			mHeaderImage.setVisibility(View.VISIBLE);
			mHeaderImageBottom.setVisibility(View.VISIBLE);
		} else {	// 显示箭头图片
			mHeaderImage.setVisibility(View.VISIBLE);
			mHeaderImageBottom.setVisibility(View.VISIBLE);
		}
		
		switch(state){
		case STATE_NORMAL:
			if (mState == STATE_READY) {
				((AnimationDrawable) mHeaderImage.getDrawable()).start();
			}
			if (mState == STATE_REFRESHING) {
				mHeaderImage.clearAnimation();
			}
//			mHintTextView.setText(R.string.xlistview_header_hint_normal);
			break;
		case STATE_READY:
			if (mState != STATE_READY) {
				mHeaderImage.clearAnimation();
				((AnimationDrawable) mHeaderImage.getDrawable()).start();
//				mHintTextView.setText(R.string.xlistview_header_hint_ready);
			}
			break;
		case STATE_REFRESHING:
			((AnimationDrawable) mHeaderImage.getDrawable()).start();
//			mHintTextView.setText(R.string.xlistview_header_hint_loading);
			break;
			default:
		}
		
		mState = state;
	}
	
	public void setVisiableHeight(int height) {
		if (height < 0)
			height = 0;
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContainer
				.getLayoutParams();
		lp.height = height;
		mContainer.setLayoutParams(lp);
	}

	public int getVisiableHeight() {
		return mContainer.getHeight();
	}

}
