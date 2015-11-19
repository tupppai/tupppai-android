package com.psgod.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.psgod.R;
import com.psgod.ui.fragment.WorksGridFragment;

/**
 * 我的作品界面
 * 
 * @author Rayal
 * 
 */
public class MyWorksActivity extends PSGodBaseActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_works);

		FragmentTransaction transaction = this.getSupportFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.activity_my_works_fragment,
				new WorksGridFragment()).commit();
	}
}
