package com.psgod.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.eventbus.SearchEvent;
import com.psgod.ui.adapter.SearchPageAdapter;
import com.psgod.ui.fragment.SearchUserFragment;
import com.psgod.ui.fragment.SearchWorkFragment;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 搜索页
 */

public class SearchActivity extends PSGodBaseActivity {

	private EditText mSearchEdit;
	private TextView mCancelTxt;
	private RadioGroup mTabRg;
	private ImageView mCursor;
	private ViewPager mViewPager;

	private RelativeLayout mParent;

	private final int[] TAB_RADIO_BUTTONS_ID = {
			R.id.activity_search_tab_user_rb,
			R.id.activity_search_tab_detail_rb };

	private final int COUNT_OF_FRAGMENTS = 2;

	private List<Fragment> fragments = new ArrayList<Fragment>();

	private SearchPageAdapter mPageAdapter;

	private int pageNum;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		initView();
		initListener();

	}

	private void initView() {
		mSearchEdit = (EditText) findViewById(R.id.activity_search_search_edit);
		mCancelTxt = (TextView) findViewById(R.id.activity_search_cancel_txt);
		mTabRg = (RadioGroup) findViewById(R.id.activity_search_tab_rg);
		mCursor = (ImageView) findViewById(R.id.activity_search_cursor);
		mViewPager = (ViewPager) findViewById(R.id.activity_search_view_pager);
		mParent = (RelativeLayout) findViewById(R.id.activity_search_parent);

		fragments.add(new SearchUserFragment());
		fragments.add(new SearchWorkFragment());

		mPageAdapter = new SearchPageAdapter(getSupportFragmentManager(),
				fragments);

		mViewPager.setAdapter(mPageAdapter);

		mSearchEdit.setFocusableInTouchMode(true);

		mCursorWidth = Utils.dpToPx(SearchActivity.this,38);
		mCurSorOffset = (Constants.WIDTH_OF_SCREEN / 2 - mCursorWidth) / 2;
		mCursorone = mCurSorOffset * 2 + mCursorWidth;
		mCursor.setX(mCurSorOffset);


	}

	int mCursorWidth;
	int mCurSorOffset;
	int mCursorone;

	private void initListener() {

		mCancelTxt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (mSearchEdit.getText() == null
						|| mSearchEdit.getText().toString().equals("")) {
					SearchActivity.this.finish();
				} else {
					mSearchEdit.setText("");
				}
			}
		});

		mSearchEdit
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_SEND
								|| (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
							if (event.getAction() == KeyEvent.ACTION_UP) {
								InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
								imm.hideSoftInputFromWindow(v.getWindowToken(),
										0);
								if (v.getText() == null
										|| v.getText().toString().equals("")) {
									return true;
								}
								EventBus.getDefault()
										.post(new SearchEvent(v.getText()
												.toString()));
								return true;
							} else {
								return true;
							}
						}
						return false;
					}
				});

		mSearchEdit.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				if (mSearchEdit.getText().toString().equals("")) {
					mCancelTxt.setText("返回");
				} else {
					mCancelTxt.setText("取消");
					//即时搜索
//					EventBus.getDefault()
//							.post(new SearchEvent(mSearchEdit.getText().toString()));
				}

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

			}

			@Override
			public void afterTextChanged(Editable arg0) {

			}
		});

		mTabRg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				for (int ix = 0; ix < COUNT_OF_FRAGMENTS; ++ix) {
					if (TAB_RADIO_BUTTONS_ID[ix] == checkedId) {
						mViewPager.setCurrentItem(ix);
					}
				}
			}
		});
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
//			int mCursorMoving = Utils.dpToPx(SearchActivity.this, 180);



			@Override
			public void onPageSelected(int index) {
				mTabRg.check(TAB_RADIO_BUTTONS_ID[index]);
				hideInputPanel();
				Animation animation = null;

				switch (index) {
				case 0:
					// if (pageNum == 1) {
					animation = new TranslateAnimation(mCursorone, 0, 0, 0);
					animation.setFillAfter(true);
					animation.setDuration(300);
					mCursor.setAnimation(animation);
					// }
					break;

				case 1:
					// if (pageNum == 0) {
					animation = new TranslateAnimation(0, mCursorone, 0, 0);
					animation.setFillAfter(true);
					animation.setDuration(300);
					mCursor.setAnimation(animation);
					// }
					break;
				default:
					break;
				}
				// 设置首页当前tab
				pageNum = index;
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				hideInputPanel();
			}
		});

		mParent.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				hideInputPanel();
				return false;
			}
		});

	}

	private void hideInputPanel() {
		// 隐藏软键盘
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mSearchEdit.getWindowToken(), 0);
	}

}
