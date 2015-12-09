package com.psgod.ui.activity;

/**
 * 编辑用户资料
 * @author brandwang
 */

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.psgod.Constants;
import com.psgod.Logger;
import com.psgod.PSGodApplication;
import com.psgod.R;
import com.psgod.WeakReferenceHandler;
import com.psgod.eventbus.AvatarEvent;
import com.psgod.model.LoginUser;
import com.psgod.model.LoginUser.SPKey;
import com.psgod.network.request.ModifyUserData;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.ui.view.CircleImageView;
import com.psgod.ui.widget.ActionBar;
import com.psgod.ui.widget.GenderSelector;

import java.io.UnsupportedEncodingException;

import de.greenrobot.event.EventBus;

public class EditProfileActivity extends PSGodBaseActivity implements
		Handler.Callback {
	private static final String TAG = EditProfileActivity.class.getSimpleName();

	private static final int CONFIRM_CHOOSE_GENDER = 1000;
	private static final int CONFIRM_CHOOSE_PLACE = 1001;

	public static final int REQUEST_UPLOAD_IMAGE = 0x330;

	private CircleImageView mAvatarView;
	private EditText mSetNickEditText;
	private TextView mSetGenderTv;
	// private TextView mSetPlaceTv;
	private RelativeLayout mGenderLayout;
	// private RelativeLayout mPlaceLayout;
	private RelativeLayout mPopLayout;
	private RelativeLayout mNickLayout;
	private Button mCompleteBtn;

	private Dialog mSetAvatarDialog;
	private ActionBar mActionBar;

	private boolean isNickNameChanged = false;
	private boolean isGenderChanged = false;
	private boolean isCityChanged = false;
	private boolean isAvatarChanged = false;

	// 用户昵称
	private String mNickName;
	// 新头像url
	private String mAvatar;
	// 新头像id
	private long mpid = 0;
	// 性别 1 为男生 0 为女生
	private int mSex;
	// 省份id
	private int provinceId;
	// 城市id
	private int cityId;

	private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);
	private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_profile);

		initViews();
		initData();
		initEvents();
		
		// 设置光标在昵称的后面
		Editable etext = mSetNickEditText.getText();
		Selection.setSelection(etext, etext.length());
	}

	protected void initData() {
		ImageLoader loader = ImageLoader.getInstance();
		// LoginUser实例
		LoginUser user = LoginUser.getInstance();

		mNickName = user.getNickname();
		mAvatar = user.getAvatarImageUrl();
		mSex = user.getGender();

		// 考虑到未填写城市的情况 默认北京 东城
		provinceId = user.getProvinceId();
		cityId = user.getCityId();

		loader.displayImage(user.getAvatarImageUrl(), mAvatarView,
				mAvatarOptions);
		mSetNickEditText.setText(mNickName);

		if (mSex == 1) {
			mSetGenderTv.setText("男");
		} else if (mSex == 0) {
			mSetGenderTv.setText("女");
		}

		// mSetPlaceTv.setText(CityInfo.getProvinceNameById(provinceId) + " "
		// + CityInfo.getCityNameById(provinceId, cityId));
	}

	protected void initViews() {
		mActionBar = (ActionBar) findViewById(R.id.actionbar);
		mSetGenderTv = (TextView) findViewById(R.id.edit_profile_gender_textview);
		// mSetPlaceTv = (TextView)
		// findViewById(R.id.edit_profile_place_textview);
		mAvatarView = (CircleImageView) findViewById(R.id.edit_profile_avatar);
		mPopLayout = (RelativeLayout) findViewById(R.id.edit_profile_relative_layout);
		mGenderLayout = (RelativeLayout) findViewById(R.id.edit_profile_gender_row);
		// mPlaceLayout = (RelativeLayout)
		// findViewById(R.id.edit_profile_place_row);
		mNickLayout = (RelativeLayout) findViewById(R.id.edit_profile_nick_row);
		mSetNickEditText = (EditText) findViewById(R.id.edit_profile_nick_edittext);
		mCompleteBtn = (Button) findViewById(R.id.edit_profile_complete_btn);
		
	}

	// 修改用户资料回调
	private Listener<Boolean> editProfileListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			Toast.makeText(EditProfileActivity.this, "修改资料成功",
					Toast.LENGTH_SHORT).show();
			LoginUser user = LoginUser.getInstance();
			SharedPreferences.Editor editor = PSGodApplication
					.getAppContext()
					.getSharedPreferences(Constants.SharedPreferencesKey.NAME,
							Context.MODE_PRIVATE).edit();
			if (isAvatarChanged) {
				editor.putString(SPKey.AVATAR_URL, mAvatar);
			} 
			if (isNickNameChanged) {
				editor.putString(SPKey.NICKNAME, mNickName);
			} 
			if (isGenderChanged) {
				editor.putInt(SPKey.GENDER, mSex);
			}
			if (android.os.Build.VERSION.SDK_INT >= 9) {
				editor.apply();
			} else {
				editor.commit();
			}
			user.refreshData();
			EventBus.getDefault().post(new AvatarEvent());
			EditProfileActivity.this.finish();
		}
	};

	private PSGodErrorListener errorListener = new PSGodErrorListener(
			EditProfileActivity.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {
		}
	};

	// 判断编辑资料页面是否填写完全
	public boolean validate() {
		// 验证昵称
		if (TextUtils.isEmpty(mSetNickEditText.getText())
				|| mSetNickEditText.getText().equals("点击输入昵称")) {
			Toast.makeText(EditProfileActivity.this, "请输入昵称",
					Toast.LENGTH_SHORT).show();
			return false;
		}

		// 验证是否选择所在地
		// String placeString = mSetPlaceTv.getText().toString();
		// if (placeString == null || placeString.equals("点击选择城市")) {
		// Toast.makeText(EditProfileActivity.this, "请选择所在地",
		// Toast.LENGTH_SHORT)
		// .show();
		// return false;
		// }

		return true;
	}

	protected void initEvents() {
		// 点击按钮 保存并退出
		mCompleteBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				LoginUser user = LoginUser.getInstance();

				if (validate()) {
					
					mNickName = mSetNickEditText.getText().toString().trim();
					ModifyUserData.Builder builder = new ModifyUserData.Builder()
							.setErrorListener(errorListener).setListener(
									editProfileListener);

					// 判断昵称是否修改
					if (!user.getNickname().equals(mNickName)) {
						builder.setNickName(mNickName);
						isNickNameChanged = true;
					}

					if (user.getGender() != mSex) {
						builder.setGender(mSex);
						isGenderChanged = true;
					}

					// if (user.getCityId() != cityId || user.getProvinceId() !=
					// provinceId) {
					// builder.setCity(cityId);
					// builder.setProvince(provinceId);
					// isCityChanged = true;
					// }

					// 判断avatar是否变化
					if (!user.getAvatarImageUrl().equals(mAvatar)) {
						builder.setAvatar(mAvatar);
						isAvatarChanged = true;
					}

					ModifyUserData request = builder.build();
					request.setTag(TAG);
					RequestQueue reqeustQueue = PSGodRequestQueue.getInstance(
							EditProfileActivity.this).getRequestQueue();
					reqeustQueue.add(request);
				}

			}
		});

		// 回车键相当于确认键
		mSetNickEditText.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (KeyEvent.KEYCODE_ENTER == keyCode
						&& event.getAction() == KeyEvent.ACTION_DOWN) {
					return true;
				}
				return false;
			}
		});

		mSetNickEditText.addTextChangedListener(mInputTextWatcher);

		// 点击头像弹出头像跟还选项
		mAvatarView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mSetAvatarDialog == null) {
					mSetAvatarDialog = new Dialog(EditProfileActivity.this,
							R.style.ActionSheetDialog);
					mSetAvatarDialog.setContentView(R.layout.dialog_set_avatar);
					mSetAvatarDialog.getWindow().getAttributes().width = Constants.WIDTH_OF_SCREEN;
					mSetAvatarDialog.setCanceledOnTouchOutside(true);
					mSetAvatarDialog.getWindow().setWindowAnimations(
							R.style.popwindow_anim_style);

					// 设置点击拍照按钮 动作监听器
					Button takePhotoButton = (Button) mSetAvatarDialog
							.findViewById(R.id.dialog_set_avatar_take_photo);
					takePhotoButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							mSetAvatarDialog.dismiss();
							ChoosePhotoActivity.startActivity(
									EditProfileActivity.this,
									ChoosePhotoActivity.FROM_CAMERA,
									SetAvatarActivity.class.getName(),
									REQUEST_UPLOAD_IMAGE, null);
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
									EditProfileActivity.this,
									ChoosePhotoActivity.FROM_ALBUM,
									SetAvatarActivity.class.getName(),
									REQUEST_UPLOAD_IMAGE, null);
						}
					});

					// 设置取消按钮动作监听器
					Button cancelButton = (Button) mSetAvatarDialog
							.findViewById(R.id.dialog_set_avatar_cancel);
					cancelButton.setOnClickListener(new OnClickListener() {
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

		// // 点击编辑所在地地址一栏 弹出地址选择器
		// mPlaceLayout.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// backgroundAlpha(0.6f); //设置背景屏幕透明度0.6
		// // 显示 popupWindow
		// PopupWindow popupWindow = CitySelector.makePopupWindow(
		// EditProfileActivity.this, mHandler);
		// //popupwindow消失时，恢复屏幕透明度 1
		// popupWindow.setOnDismissListener(new poponDismissListener());
		//
		// int[] xy = new int[2];
		// mPopLayout.getLocationOnScreen(xy);
		// popupWindow.setAnimationStyle(R.style.popwindow_anim_style);
		// popupWindow.showAtLocation(mPopLayout, Gravity.BOTTOM, 0, 0);
		// }
		// });

		// 点击编辑性别一栏 弹出性别选择器
		mGenderLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				backgroundAlpha(0.6f); // 设置背景屏幕透明度0.6
				PopupWindow genderWindow = GenderSelector.getGenderPopupWindow(
						EditProfileActivity.this, mHandler);
				// popupwindow消失时，恢复屏幕透明度 1
				genderWindow.setOnDismissListener(new poponDismissListener());

				int[] xy = new int[2];
				mPopLayout.getLocationOnScreen(xy);
				genderWindow.setAnimationStyle(R.style.popwindow_anim_style);
				genderWindow.showAtLocation(mPopLayout, Gravity.BOTTOM, 0, 0);
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
						Toast.makeText(EditProfileActivity.this,
								"用户名最多20个字符 (或10个汉字)", Toast.LENGTH_SHORT)
								.show();
						mSetNickEditText.setText(limitSubstring);
						mSetNickEditText.setSelection(limitSubstring.length());
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

	// 上传头像回调
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

				// 本地url
				String path = data.getExtras().getString(
						Constants.IntentKey.PHOTO_PATH);
				Long imgId = data.getExtras().getLong("imageId");
				String imageUrl = data.getExtras().getString("imagePath");

				if (!TextUtils.isEmpty(path)) {
					// 如果更换头像 更新头像
					mAvatar = imageUrl;
					mpid = imgId;

					// 本地url
					Bitmap image = BitmapFactory.decodeFile(path);
					mAvatarView.setImageBitmap(image);
				}
			}
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case CONFIRM_CHOOSE_GENDER:
			int pos = msg.getData().getInt("gender");
			if (pos == 1) {
				mSetGenderTv.setText("女");
				mSex = 0;
			} else if (pos == 0) {
				mSetGenderTv.setText("男");
				mSex = 1;
			}
			break;

		case CONFIRM_CHOOSE_PLACE:
			provinceId = msg.getData().getInt("provinceId");
			cityId = msg.getData().getInt("cityId");

			// mSetPlaceTv.setText(msg.getData().getString("provinceName") + " "
			// + msg.getData().getString("cityName"));
			break;

		default:
			break;
		}
		return false;
	}

	/**
	 * 暂停所有的下载
	 */
	@Override
	public void onStop() {
		super.onStop();
		RequestQueue requestQueue = PSGodRequestQueue.getInstance(this)
				.getRequestQueue();
		requestQueue.cancelAll(TAG);
	}
}
