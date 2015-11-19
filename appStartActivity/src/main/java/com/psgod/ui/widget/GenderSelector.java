package com.psgod.ui.widget;

/**
 * 性别选择组件
 * @author brandwang
 */

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.psgod.Constants;
import com.psgod.R;
import com.psgod.ui.adapter.GenderAdapter;

public class GenderSelector {
	private static Context context;

	// Scrolling 标志位
	private static boolean scrolling = false;
	private static Handler mHandler;
	private static TextView mGenderShowTextView;
	private static GenderAdapter mGenderAdapter;
	private static Button button_ok;
	private static Button button_cancel;

	private static int CONFIRM_CHOOSE_GENDER = 1000;

	// 创建性别选择PopUpwindow
	public static PopupWindow getGenderPopupWindow(Context cx, Handler handler) {
		context = cx;
		mHandler = handler;

		// 0 女 1 男
		final String[] genderDatas = { "男", "女" };

		final PopupWindow window;
		window = new PopupWindow(context);

		// 实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0xb0000000);
		window.setBackgroundDrawable(dw);

		View contentView = LayoutInflater.from(context).inflate(
				R.layout.widget_gender_selector, null);
		window.setContentView(contentView);

		mGenderShowTextView = (TextView) contentView
				.findViewById(R.id.tv_gender);

		// 性别选择器
		final WheelView genderWheel = (WheelView) contentView
				.findViewById(R.id.gender_wheel);
		genderWheel.setVisibleItems(5);
		mGenderAdapter = new GenderAdapter(context);
		genderWheel.setViewAdapter(mGenderAdapter);

		// genderWheel监听器
		genderWheel.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {

			}
		});

		genderWheel.addScrollingListener(new OnWheelScrollListener() {
			@Override
			public void onScrollingStarted(WheelView wheel) {
				scrolling = true;
			}

			@Override
			public void onScrollingFinished(WheelView wheel) {
				scrolling = false;

				mGenderShowTextView.setText(genderDatas[genderWheel
						.getCurrentItem()]);
			}
		});

		button_ok = (Button) contentView.findViewById(R.id.button_ok);
		button_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Message msg = mHandler.obtainMessage();
				msg.what = CONFIRM_CHOOSE_GENDER;
				// bundle 传递参数
				Bundle bundle = new Bundle();
				bundle.putInt("gender", genderWheel.getCurrentItem());

				msg.setData(bundle);
				msg.sendToTarget();

				window.dismiss();
			}
		});

		button_cancel = (Button) contentView.findViewById(R.id.button_cancel);
		button_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				window.dismiss();
			}

		});

		window.setWidth(Constants.WIDTH_OF_SCREEN);
		window.setHeight(Constants.HEIGHT_OF_SCREEN / 3);

		// 设置PopupWindow外部区域是否可触摸
		window.setFocusable(true); // 设置PopupWindow可获得焦点
		window.setTouchable(true); // 设置PopupWindow可触摸
		window.setOutsideTouchable(true); // 设置非PopupWindow区域可触摸

		return window;
	}

}
