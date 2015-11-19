package com.psgod.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.psgod.R;

public class ImageDialog extends Dialog {

	public ImageDialog(Context context, ImageView view) {
		super(context, R.style.Dialog);
		this.view = view;
		this.context = context;
	}

	private ImageView view;
	private ImageView imgView;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_dialog_img);

		initView();
		initAnimation();
		initListener();
	}

	private void initListener() {
		imgView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ImageDialog.this.dismiss();
			}
		});
	}

	private void initAnimation() {

	}

	@Override
	public void show() {
		super.show();
		getWindow().setWindowAnimations(R.style.imgdialog_anim_style);
	}

	private void initView() {
		imgView = (ImageView) findViewById(R.id.dialog_img_img);
		imgView.setImageDrawable(view.getDrawable());
	}

}
