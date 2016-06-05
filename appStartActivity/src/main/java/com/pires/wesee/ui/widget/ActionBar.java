package com.pires.wesee.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pires.wesee.Utils;
import com.pires.wesee.R;

/**
 * 自定义ActionBar
 * 
 * @author Rayal
 * 
 */
public class ActionBar extends RelativeLayout {
	private static final String TAG = ActionBar.class.getSimpleName();

	private static final String VISIBLE = "0";
	private static final String INVISIBLE = "1";
	private static final String GONE = "2";
	private static final int DEFAULT_INT_VALUE = -1;
	private static final float DEFAULT_DIMENSION_VALUE = 0.0f;
	private Context mContext;
	private TextView mLeftBtn;
	private TextView mRightBtn;
	private TextView mTitleTv;

	public ActionBar(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public ActionBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ActionBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ActionBar);

		// 初始化标题
		String title = a.getString(R.styleable.ActionBar_title);
		float titleTextSize = a.getDimension(
				R.styleable.ActionBar_titleTextSize, DEFAULT_DIMENSION_VALUE);
		int titleColor = a.getColor(R.styleable.ActionBar_titleTextColor,
				DEFAULT_INT_VALUE);
		if (title != null) {
			mTitleTv.setText(title);
		}
		if (!Utils.isFloatEquals(titleTextSize, DEFAULT_DIMENSION_VALUE)) {
			mTitleTv.setTextSize(titleTextSize);
		}
		if (titleColor != DEFAULT_INT_VALUE) {
			mTitleTv.setTextColor(titleColor);
		}

		// 初始化左按钮
		String leftBtnVisibility = a
				.getString(R.styleable.ActionBar_leftBtnVisibility);
		if (leftBtnVisibility != null) {
			if (leftBtnVisibility.equals(VISIBLE)) {
				mLeftBtn.setVisibility(View.VISIBLE);
			} else if (leftBtnVisibility.equals(INVISIBLE)) {
				mLeftBtn.setVisibility(View.INVISIBLE);
			} else if (leftBtnVisibility.equals(GONE)) {
				mLeftBtn.setVisibility(View.GONE);
			}
		}
		// String leftBtnText = a.getString(R.styleable.ActionBar_leftBtnText);
		// float leftBtnTextSize =
		// a.getDimension(R.styleable.ActionBar_leftBtnTextSize,
		// DEFAULT_DIMENSION_VALUE);
		// int leftBtnTextColor =
		// a.getColor(R.styleable.ActionBar_leftBtnTextColor,
		// DEFAULT_INT_VALUE);
		// if (leftBtnText != null) {
		// mLeftBtn.setText(leftBtnText);
		// }
		// if (!Utils.isFloatEquals(leftBtnTextSize, DEFAULT_DIMENSION_VALUE)) {
		// mLeftBtn.setTextSize(leftBtnTextSize);
		// }
		// if (leftBtnTextColor != DEFAULT_INT_VALUE) {
		// mLeftBtn.setTextColor(leftBtnTextColor);
		// }

		// 初始化右按钮
		String rightBtnText = a.getString(R.styleable.ActionBar_rightBtnText);
		float rightBtnTextSize = a
				.getDimension(R.styleable.ActionBar_rightBtnTextSize,
						DEFAULT_DIMENSION_VALUE);
		int rightBtnTextColor = a.getColor(
				R.styleable.ActionBar_rightBtnTextColor, DEFAULT_INT_VALUE);
		String rightBtnVisibility = a
				.getString(R.styleable.ActionBar_rightBtnVisibility);

		if (rightBtnText != null) {
			mRightBtn.setText(rightBtnText);
		}
		if (!Utils.isFloatEquals(rightBtnTextSize, DEFAULT_DIMENSION_VALUE)) {
			mRightBtn.setTextSize(rightBtnTextSize);
		}
		if (rightBtnTextColor != DEFAULT_INT_VALUE) {
			mRightBtn.setTextColor(rightBtnTextColor);
		}
		if (rightBtnVisibility != null) {
			if (rightBtnVisibility.equals(VISIBLE)) {
				mRightBtn.setVisibility(View.VISIBLE);
			} else if (rightBtnVisibility.equals(INVISIBLE)) {
				mRightBtn.setVisibility(View.INVISIBLE);
			} else if (rightBtnVisibility.equals(GONE)) {
				mRightBtn.setVisibility(View.GONE);
			}
		}
		a.recycle();
	}

	private void init() {
		View view = LayoutInflater.from(mContext).inflate(R.layout.actionbar,
				this);
		mLeftBtn = (Button) view.findViewById(R.id.actionbar_left_btn);
		mTitleTv = (TextView) view.findViewById(R.id.actionbar_title);
		mRightBtn = (Button) view.findViewById(R.id.actionbar_right_btn);
		// // 设置ActionBar的大小
		// Resources res = mContext.getResources();
		// this.setBackgroundColor(res.getColor(R.color.actionbar_bg));
		//
		// // 初始化左按钮
		// mLeftBtn = new TextView(mContext);
		// RelativeLayout.LayoutParams leftParams = new
		// RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
		// RelativeLayout.LayoutParams.WRAP_CONTENT);
		// leftParams.addRule(RelativeLayout.CENTER_VERTICAL,
		// RelativeLayout.TRUE);
		// leftParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
		// RelativeLayout.TRUE);
		// leftParams.setMargins(res.getDimensionPixelSize(R.dimen.normal_margin),
		// 0, 0, 0);
		// mLeftBtn.setLayoutParams(leftParams);
		// // mLeftBtn.setMinimumHeight(0);
		// // mLeftBtn.setMinimumWidth(0);
		// mLeftBtn.setCompoundDrawables(res.getDrawable(R.drawable.selector_backward_btn),
		// null, null, null);
		// mLeftBtn.setText("Test");
		// // mLeftBtn.setBackgroundDrawable(null);
		// mLeftBtn.setBackgroundColor(Color.BLUE);
		mLeftBtn.setMinWidth(0);
		mLeftBtn.setMinHeight(0);
		mLeftBtn.setMinimumWidth(0);
		mLeftBtn.setMinimumHeight(0);

		// 点击左按钮时，关闭当前界面
		mLeftBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mContext instanceof Activity) {
					((Activity) mContext).finish();
				}
			}
		});
		//
		// // 初始化右按钮
		// mRightBtn = new TextView(mContext);
		// RelativeLayout.LayoutParams rightParams = new
		// RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
		// RelativeLayout.LayoutParams.WRAP_CONTENT);
		// rightParams.addRule(RelativeLayout.CENTER_VERTICAL,
		// RelativeLayout.TRUE);
		// rightParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
		// RelativeLayout.TRUE);
		// rightParams.setMargins(0, 0,
		// res.getDimensionPixelSize(R.dimen.normal_margin), 0);
		// mRightBtn.setLayoutParams(rightParams);
		// mRightBtn.setText("下一步");
		// mRightBtn.setTextColor(res.getColor(R.color.selector_actionbar_right_btn));
		// // mRightBtn.setBackgroundDrawable(null);
		// mRightBtn.setClickable(true);
		//
		// // 初始化标题
		// mTitleTv = new TextView(mContext);
		// RelativeLayout.LayoutParams titleParams = new
		// RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
		// RelativeLayout.LayoutParams.WRAP_CONTENT);
		// titleParams.addRule(RelativeLayout.CENTER_IN_PARENT,
		// RelativeLayout.TRUE);
		// mTitleTv.setLayoutParams(titleParams);
		// mTitleTv.setTextColor(res.getColor(R.color.actionbar_title));
		// mTitleTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22.0f);
		//
		// // 分割线
		// View line = new View(mContext);
		// line.setBackgroundColor(Color.parseColor("#33000000"));
		// int px = Utils.dpToPx(mContext, 1);
		// RelativeLayout.LayoutParams lineParams = new
		// RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
		// px);
		// lineParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
		// RelativeLayout.TRUE);
		// line.setLayoutParams(lineParams);
		//
		// this.addView(mLeftBtn);
		// this.addView(mTitleTv);
		// this.addView(mRightBtn);
		// this.addView(line);

		// TODO
		// 1. title 加粗
		// 2. title的大小不要硬编码
		// 3. 右按钮的颜色点击没有变化
		// 4. 自定义的属性
	}

	public void setTitle(String title) {
		mTitleTv.setText(title);
	}

	public void setLeftBtnOnClickListener(OnClickListener listener) {
		mLeftBtn.setOnClickListener(listener);
	}

	public void setRightBtnOnClickListener(OnClickListener listener) {
		mRightBtn.setOnClickListener(listener);
	}

	public void setLeftBtnVisibility(int visibility) {
		mLeftBtn.setVisibility(visibility);
	}

	public void setRightBtnVisibility(int visibility) {
		mRightBtn.setVisibility(visibility);
	}

	// 触发actionbar的右键点击
	public void trigerRightBtnClick() {
		mRightBtn.performClick();
	}
}
