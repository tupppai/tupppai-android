package com.pires.wesee.ui.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.app.Fragment;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public abstract class BaseFragment extends Fragment {
	// private int layoutResID;
	// private View view;
	//
	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	// view = null;
	// if (layoutResID == 0) {
	// view = inflater.inflate(R.layout.psgod_fragment_base, null);
	// return view;
	// } else {
	// view = inflater.inflate(layoutResID, null);
	// }
	// return view;
	// }
	//
	// public void setContentView(int layoutResID) {
	// this.layoutResID = layoutResID;
	// }
}
