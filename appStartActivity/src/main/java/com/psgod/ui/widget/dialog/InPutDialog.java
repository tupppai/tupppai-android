package com.psgod.ui.widget.dialog;

/**
 * 带输入框的弹出框
 * @author brandwang
 */
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.psgod.R;

public class InPutDialog extends Dialog {
	private TextView titleText;
	private EditText editText;
	private RelativeLayout positiveLayout, negativeLayout;
	private ImageButton positiveButton, negativeButton;

	public InPutDialog(Context context) {
		super(context, R.style.Dialog);
		setCustomDialog();
	}

	private void setCustomDialog() {
		View mView = LayoutInflater.from(getContext()).inflate(
				R.layout.dialog_input_layout, null);

		editText = (EditText) mView.findViewById(R.id.dialog_input_edittext);
		titleText = (TextView) mView.findViewById(R.id.dialog_input_title);
		positiveLayout = (RelativeLayout) mView
				.findViewById(R.id.positiveLayout);
		negativeLayout = (RelativeLayout) mView
				.findViewById(R.id.negativeLayout);
		positiveButton = (ImageButton) mView.findViewById(R.id.positiveButton);
		negativeButton = (ImageButton) mView.findViewById(R.id.negativeButton);
		super.setContentView(mView);
	}

	public View getEditText() {
		return editText;
	}

	public View getTextView() {
		return titleText;
	}

	@Override
	public void setContentView(int layoutResID) {
	}

	@Override
	public void setContentView(View view, LayoutParams params) {
	}

	@Override
	public void setContentView(View view) {
	}

	/**
	 * 确定键监听器
	 * 
	 * @param listener
	 */
	public void setOnPositiveListener(View.OnClickListener listener) {
		positiveLayout.setOnClickListener(listener);
		positiveButton.setOnClickListener(listener);
	}

	/**
	 * 取消键监听器
	 * 
	 * @param listener
	 */
	public void setOnNegativeListener(View.OnClickListener listener) {
		negativeLayout.setOnClickListener(listener);
		negativeButton.setOnClickListener(listener);
	}
}
