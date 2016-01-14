package com.psgod.ui.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.psgod.PsGodImageLoader;
import com.psgod.Constants;
import com.psgod.Logger;
import com.psgod.R;
import com.psgod.WeakReferenceHandler;
import com.psgod.model.RegisterData;
import com.psgod.network.request.BaseRequest;
import com.psgod.ui.adapter.CityInfo;
import com.psgod.ui.view.CircleImageView;
import com.psgod.ui.widget.CitySelector;
import com.psgod.ui.widget.dialog.InPutDialog;

import java.io.UnsupportedEncodingException;

public class SetInfoActivity extends PSGodBaseActivity implements
		OnCheckedChangeListener, Handler.Callback {
	private static final String TAG = SetInfoActivity.class.getSimpleName();

	public static final int REQUEST_UPLOAD_IMAGE = 0x330;
	private static final int CONFIRM_CHOOSE_GENDER = 1000;
	private static final int CONFIRM_CHOOSE_PLACE = 1001;

	private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

	private CircleImageView mSetInfoImageBtn;

	private Dialog mSetAvatarDialog;
	private InPutDialog mInPutNickDialog;
	// private ActionBar mActionBar;

	private Button mNextBtn;
	private RelativeLayout mPopLayout;

	private RelativeLayout mSetNickPanel;
	private RelativeLayout mSetPlacePanel;

	private EditText mSetNickTv;
	private TextView mSetPlaceTv;
	private TextView mUserAgreementTip;
	private TextView mBackTextView;
	private EditText mInputEditText;
	private TextView mTitleTextView;
	private RadioGroup mGenderGroup;
	private RadioButton mFemaleButton;
	private RadioButton mMaleButton;

	// 微信返回的头像
	private String thirdAuthAvatar;
	// 图片上传成功后返回的unique url
	private String imageUrl = "";
	// 性别 1 为男生 0 为女生
	private int mSex = 0;
	// 屏幕高度和宽度
	int width, height;
	// wechat openId
	private String openId = "";

	// 省份id
	private int provinceId;
	// 城市id
	private int cityId;
	// 注册平台类型 包括第三方 weibo weixin mobile qq
	private String type = "mobile";

	private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_info);
		initViews();

		Intent intent = getIntent();
		// 第三方登录 跳转注册页面
		if (intent.hasExtra(Constants.ThirdAuthInfo.THIRD_AUTH_PLATFORM)) {
			initThirdAuthData(intent);
		}
		initEvents();
	}

	private void initThirdAuthData(Intent intent) {
		type = intent
				.getStringExtra(Constants.ThirdAuthInfo.THIRD_AUTH_PLATFORM);

		String mNickName = intent
				.getStringExtra(Constants.ThirdAuthInfo.USER_NICKNAME);
		if (intent.hasExtra(Constants.ThirdAuthInfo.USER_GENDER)) {
			String mGender = intent
					.getStringExtra(Constants.ThirdAuthInfo.USER_GENDER);
			if (mGender.equals("1")) {
				mGenderGroup.check(mMaleButton.getId());
				mSex = 1;
			} else if (mGender.equals("0")) {
				mGenderGroup.check(mFemaleButton.getId());
				mSex = 0;
			}
		}
		String mAvatarUrl = intent
				.getStringExtra(Constants.ThirdAuthInfo.USER_AVATAR);
		thirdAuthAvatar = mAvatarUrl;

		if (intent.hasExtra(Constants.ThirdAuthInfo.USER_PROVINCE)) {
			String mProvince = intent
					.getStringExtra(Constants.ThirdAuthInfo.USER_PROVINCE);
			String mCity = intent
					.getStringExtra(Constants.ThirdAuthInfo.USER_CITY);

			mSetPlaceTv.setText(mProvince + "   " + mCity);
			// 根据省份和城市名称 获取当前省份和城市的id
			provinceId = CityInfo.getProvinceIdByName(mProvince);
			cityId = CityInfo.getCityIdByName(mProvince, mCity);
		}
		openId = intent.getStringExtra(Constants.ThirdAuthInfo.USER_OPENID);

		mSetNickTv.setText(mNickName);
		PsGodImageLoader imageLoader = PsGodImageLoader.getInstance();
		imageLoader.displayImage(mAvatarUrl, mSetInfoImageBtn, mAvatarOptions);
	}

	protected void initViews() {
		mGenderGroup = (RadioGroup) findViewById(R.id.select_gender);
		mGenderGroup.setOnCheckedChangeListener(mChangeRadio);
		mFemaleButton = (RadioButton) findViewById(R.id.gender_female);
		mMaleButton = (RadioButton) findViewById(R.id.gender_male);
		mSetNickPanel = (RelativeLayout) findViewById(R.id.set_info_nick_row);
		mSetNickTv = (EditText) findViewById(R.id.set_info_nick_tv);
		mSetPlacePanel = (RelativeLayout) findViewById(R.id.set_info_place_row);
		mSetPlaceTv = (TextView) findViewById(R.id.set_info_place_tv);
		mBackTextView = (TextView) findViewById(R.id.actionbar);

		mSetInfoImageBtn = (CircleImageView) findViewById(R.id.imageButton1);

		// 获取弹出的layout
		mPopLayout = (RelativeLayout) findViewById(R.id.layout_activity_set_info);

		mUserAgreementTip = (TextView) findViewById(R.id.activity_set_info_user_agreement);
		// mActionBar = (com.psgod.ui.widget.ActionBar)
		// findViewById(R.id.actionbar);
		mNextBtn = (Button) findViewById(R.id.activity_set_info_next_btn);
		mInPutNickDialog = new InPutDialog(SetInfoActivity.this);
		mInputEditText = (EditText) mInPutNickDialog.getEditText();
		mInputEditText.addTextChangedListener(mInputTextWatcher);
		mTitleTextView = (TextView) mInPutNickDialog.getTextView();
		mTitleTextView.setText("请输入昵称");
	}

	private RadioGroup.OnCheckedChangeListener mChangeRadio = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {

			if (checkedId == mFemaleButton.getId()) {
				mFemaleButton.setTextColor(Color.parseColor("#FFFFFF"));
				mMaleButton.setTextColor(Color.parseColor("#9B9B9B"));
				mSex = 0;
			} else if (checkedId == mMaleButton.getId()) {
				mFemaleButton.setTextColor(Color.parseColor("#9B9B9B"));
				mMaleButton.setTextColor(Color.parseColor("#FFFFFF"));
				mSex = 1;
			}
		}
	};

	protected void initEvents() {
		mUserAgreementTip.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(SetInfoActivity.this,
						WebBrowserActivity.class);
				intent.putExtra(WebBrowserActivity.KEY_URL, BaseRequest.PSGOD_BASE_URL +
						"mobile/agreement.html");
				intent.putExtra(WebBrowserActivity.KEY_DESC,"用户协议");
				startActivity(intent);
			}
		});

		// 回车键相当于确定键
		mInputEditText.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (KeyEvent.KEYCODE_ENTER == keyCode
						&& event.getAction() == KeyEvent.ACTION_DOWN) {
					mSetNickTv.setText(mInputEditText.getText().toString()
							.trim());
					mSetNickTv.setTextColor(Color.parseColor("#50484B"));
					mInPutNickDialog.dismiss();
					return true;
				}
				return false;
			}
		});

		// 点击提交下一步
		mNextBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (validate()) {
					// 传递注册信息的parcel
					RegisterData registerData = new RegisterData();

					registerData.setThirdAuthType(type);
					registerData.setAvatar(imageUrl);
					registerData.setNickname(mSetNickTv.getText().toString());
					registerData.setProvinceId(provinceId);
					registerData.setCityId(cityId);
					registerData.setGender(mSex);
					registerData.setOpenId(openId);
					registerData.setThirdAvatar(thirdAuthAvatar);

					Intent intent = new Intent(SetInfoActivity.this,
							RegisterPhoneActivity.class);
					intent.putExtra(Constants.IntentKey.REGISTER_DATA,
							registerData);
					intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(intent);
				}
			}
		});

		// 点击输入昵称 弹出输入昵称对话框
		// mSetNickPanel.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		// //初始化 昵称输入框 的内容
		// if(mSetNickTv.getText().toString().equals("点击输入昵称")){
		// mInputEditText.setText("");
		// }else{
		// mInputEditText.setText(mSetNickTv.getText().toString());
		// }
		//
		// //设置光标在昵称的后面
		// Editable etext = mInputEditText.getText();
		// Selection.setSelection(etext, etext.length());
		//
		// if (mInPutNickDialog.isShowing()) {
		// mInPutNickDialog.dismiss();
		// } else {
		// mInPutNickDialog.getWindow().setGravity(Gravity.CENTER);
		// mInPutNickDialog.show();
		// }
		//
		// mHandler.postDelayed(new Runnable(){
		// public void run() {
		// //execute the task
		// callInputPanel();
		// }
		// }, 200);
		// }
		// });

		mInPutNickDialog.setOnPositiveListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (mInputEditText.getText().toString().trim().equals("")) {
					Toast.makeText(SetInfoActivity.this, "昵称不能为空",
							Toast.LENGTH_SHORT).show();
				} else {
					mSetNickTv.setText(mInputEditText.getText().toString()
							.trim());
					mSetNickTv.setTextColor(Color.parseColor("#50484B"));
					mInPutNickDialog.dismiss();
				}
			}
		});

		mInPutNickDialog.setOnNegativeListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mInPutNickDialog.dismiss();
			}
		});

		// 点击编辑所在地地址一栏 弹出地址选择器
		mSetPlacePanel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hideInputPanel();
				backgroundAlpha(0.6f); // 设置背景屏幕透明度0.6
				// 显示 popupWindow
				PopupWindow popupWindow = CitySelector.makePopupWindow(
						SetInfoActivity.this, mHandler);
				// popupwindow消失时，恢复屏幕透明度 1
				popupWindow.setOnDismissListener(new poponDismissListener());

				int[] xy = new int[2];
				mPopLayout.getLocationOnScreen(xy);
				popupWindow.setAnimationStyle(R.style.popwindow_anim_style);
				popupWindow.showAtLocation(mPopLayout, Gravity.BOTTOM, 0, 0);
			}
		});

		// TODO 重构，对话框可以提取为一个组件
		// 点击用户头像编辑头像上传
		mSetInfoImageBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (mSetAvatarDialog == null) {
					mSetAvatarDialog = new Dialog(SetInfoActivity.this,
							R.style.ActionSheetDialog);
					mSetAvatarDialog.setContentView(R.layout.dialog_set_avatar);
					mSetAvatarDialog.getWindow().getAttributes().width = Constants.WIDTH_OF_SCREEN;
					mSetAvatarDialog.setCanceledOnTouchOutside(true);

					// 设置点击拍照按钮 动作监听器
					Button takePhotoButton = (Button) mSetAvatarDialog
							.findViewById(R.id.dialog_set_avatar_take_photo);
					takePhotoButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							mSetAvatarDialog.dismiss();
							ChoosePhotoActivity.startActivity(
									SetInfoActivity.this,
									ChoosePhotoActivity.FROM_CAMERA,
									SetAvatarActivity.class.getName(),
									REQUEST_UPLOAD_IMAGE, null);
							// Intent intent = new Intent(SetInfoActivity.this,
							// SetAvatarActivity.class);
							// intent.putExtra(
							// Constants.IntentKey.START_SET_AVATAR_ACTIVITY_FROM,
							// SetAvatarActivity.START_FROM_CAMERA);
							// startActivity(intent);

						}
					});

					// 设置本地选择图片上传 动作监听器
					Button choosePhotoBtn = (Button) mSetAvatarDialog
							.findViewById(R.id.dialog_set_avatar_choose_photo);
					choosePhotoBtn.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							mSetAvatarDialog.dismiss();
							ChoosePhotoActivity.startActivity(
									SetInfoActivity.this,
									ChoosePhotoActivity.FROM_ALBUM,
									SetAvatarActivity.class.getName(),
									REQUEST_UPLOAD_IMAGE, null);
							// Intent intent = new Intent(SetInfoActivity.this,
							// SetAvatarActivity.class);
							// intent.putExtra(
							// Constants.IntentKey.START_SET_AVATAR_ACTIVITY_FROM,
							// SetAvatarActivity.START_FROM_ALBUM);
							// startActivityForResult(intent,
							// REQUEST_UPLOAD_IMAGE);
						}
					});

					// 设置取消按钮动作监听器
					Button cancelBtn = (Button) mSetAvatarDialog
							.findViewById(R.id.dialog_set_avatar_cancel);
					cancelBtn.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							mSetAvatarDialog.dismiss();
						}
					});
				}

				if (mSetAvatarDialog.isShowing()) {
					mSetAvatarDialog.dismiss();
				} else {
					mSetAvatarDialog.show();
					mSetAvatarDialog.getWindow().setGravity(Gravity.BOTTOM);
				}
			}
		});
		mBackTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}

		});
	}

	private TextWatcher mInputTextWatcher = new TextWatcher() {
		private String temp;

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			temp = s.toString();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (!TextUtils.isEmpty(temp)) {
				String limitSubstring = getLimitSubstring(temp);

				if (!TextUtils.isEmpty(limitSubstring)) {

					if (!limitSubstring.equals(temp)) {
						Toast.makeText(SetInfoActivity.this,
								"用户名最多20个字符 (或10个汉字)", Toast.LENGTH_SHORT)
								.show();
						mInputEditText.setText(limitSubstring);
						mInputEditText.setSelection(limitSubstring.length());
					}
				}
			}
		}
	};

	private String getLimitSubstring(String inputStr) {
		int orignLen = inputStr.length();
		int resultLen = 0;
		String temp = null;
		for (int i = 0; i < orignLen; i++) {
			temp = inputStr.substring(i, i + 1);
			try {// 3 bytes to indicate chinese word,1 byte to indicate english
					// word ,in utf-8 encode
				if (temp.getBytes("utf-8").length == 3) {
					resultLen += 2;
				} else {
					resultLen++;
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			if (resultLen > 20) {
				return inputStr.substring(0, i);
			}
		}
		return inputStr;
	}

	// 设置Popupwindow 背景屏幕的透明度
	public void backgroundAlpha(float bgAlpha) {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.alpha = bgAlpha; // 0.0-1.0
		getWindow().setAttributes(lp);
	}

	class poponDismissListener implements PopupWindow.OnDismissListener {

		@Override
		public void onDismiss() {
			backgroundAlpha(1f);
		}
	}

	private void callInputPanel() {
		// 唤起输入键盘 并输入框取得焦点
		mInputEditText.setFocusableInTouchMode(true);
		mInputEditText.requestFocus();

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(mInputEditText, 0);
	}

	private void hideInputPanel() {
		// 隐藏软键盘
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mSetPlacePanel.getWindowToken(), 0);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_UPLOAD_IMAGE) {
				if (data == null) {
					Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_COLOR,
							TAG,
							"onActivityResult(): REQUEST_UPLOAD_IMAGE, data is null");
					return;
				}

				String path = data.getExtras().getString(
						Constants.IntentKey.PHOTO_PATH);
				String imgUrl = data.getExtras().getString("imagePath");

				if (!TextUtils.isEmpty(path)) {
					imageUrl = imgUrl;
					Bitmap image = BitmapFactory.decodeFile(path);
					mSetInfoImageBtn.setImageBitmap(image);
				}
			}
		}
	}

	// 判断填写信息页面是否填写完全
	public boolean validate() {
		// 验证是否上传头像
		if (imageUrl.equals("") && (type == "mobile")) {
			Toast.makeText(SetInfoActivity.this, "请上传您的头像", Toast.LENGTH_SHORT)
					.show();
			return false;
		}
		// 验证昵称
		if (mSetNickTv.getText().toString().trim().equals("")
				|| mSetNickTv.getText().toString().trim().equals("点击输入昵称")) {
			Toast.makeText(SetInfoActivity.this, "请输入昵称", Toast.LENGTH_SHORT)
					.show();
			return false;
		}

		// 验证是否选择所在地
		String placeString = mSetPlaceTv.getText().toString();
		if (placeString == null || placeString.equals("点击选择城市")) {
			Toast.makeText(SetInfoActivity.this, "请选择所在地", Toast.LENGTH_SHORT)
					.show();
			return false;
		}

		return true;
	}

	@Override
	public void onCheckedChanged(RadioGroup arg0, int arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case CONFIRM_CHOOSE_PLACE:
			provinceId = msg.getData().getInt("provinceId");
			cityId = msg.getData().getInt("cityId");

			mSetPlaceTv.setText(msg.getData().getString("provinceName") + "   "
					+ msg.getData().getString("cityName"));
			mSetPlaceTv.setTextColor(Color.parseColor("#50484B"));
			break;

		default:
			break;
		}
		return false;
	}
}
