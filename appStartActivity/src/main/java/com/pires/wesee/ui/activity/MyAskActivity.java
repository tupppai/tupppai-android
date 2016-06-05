package com.pires.wesee.ui.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.pires.wesee.ui.fragment.AskGridFragment;
import com.pires.wesee.ui.widget.dialog.CameraDialog;
import com.pires.wesee.R;

/**
 * 我的求P界面
 * 
 * @author Rayal
 * 
 */
public class MyAskActivity extends PSGodBaseActivity {
	private static final String TAG = MyAskActivity.class.getSimpleName();

	private ImageButton mImageButton;
	Dialog mAskDialog;




	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_ask);
		mImageButton = (ImageButton) findViewById(R.id.activity_ask_camera_btn);

		FragmentTransaction transaction = this.getSupportFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.activity_my_ask_fragment,
				new AskGridFragment()).commit();

		mImageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (mAskDialog == null) {
					mAskDialog = new CameraDialog(MyAskActivity.this);
				}
				if (mAskDialog.isShowing()) {
					mAskDialog.dismiss();
				} else {
					mAskDialog.show();
				}
			}
		});
	}

}
