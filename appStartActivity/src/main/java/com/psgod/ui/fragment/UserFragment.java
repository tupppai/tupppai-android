package com.psgod.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.psgod.Constants;
import com.psgod.Logger;
import com.psgod.R;
import com.psgod.model.LoginUser;
import com.psgod.network.request.GetUserInfoRequest;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.ui.activity.FollowerListActivity;
import com.psgod.ui.activity.FollowingListActivity;
import com.psgod.ui.activity.MyAskActivity;
import com.psgod.ui.activity.MyCollectionsActivity;
import com.psgod.ui.activity.MyInProgressActivity;
import com.psgod.ui.activity.MyWorksActivity;
import com.psgod.ui.activity.SettingActivity;
import com.psgod.ui.view.CircleImageView;
import com.psgod.ui.widget.ActionBar;

import org.json.JSONObject;

public class UserFragment extends BaseFragment {
	private static final String TAG = UserFragment.class.getSimpleName();
	private static int RESULT_OK = 0xffffffff;

	public static final int REQUEST_UPLOAD_IMAGE = 0x330;
	private static final int CONFIRM_CHOOSE_GENDER = 1000;

	private ViewHolder mViewHolder;
	// private DatabaseHelper mDatabaseHelper = null;
	// private Dao<UserInfo, Long> mUserInfoDao;
	// private UserInfo mUserInfo;

	private ActionBar mActionBar;
	private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logger.logMethod(TAG, "onCreate");

		// 用户页面创建标识
		Constants.IS_USER_FRAGMENT_CREATED = true;

		FrameLayout parentView = new FrameLayout(getActivity());
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		parentView.setLayoutParams(params);
		mViewHolder = new ViewHolder();
		mViewHolder.parentView = parentView;
		mViewHolder.view = LayoutInflater.from(getActivity()).inflate(
				R.layout.fragment_user, parentView, true);

		mViewHolder.avatarIv = (CircleImageView) mViewHolder.view
				.findViewById(R.id.fragment_user_avatar_imageview);
		mViewHolder.followerTv = (TextView) mViewHolder.view
				.findViewById(R.id.fragment_user_followers_tag);
		mViewHolder.followerCountTv = (TextView) mViewHolder.view
				.findViewById(R.id.fragment_user_followers_count_tag);
		mViewHolder.likeTv = (TextView) mViewHolder.view
				.findViewById(R.id.fragment_user_like_tag);
		mViewHolder.likeCountTv = (TextView) mViewHolder.view
				.findViewById(R.id.fragment_user_like_count_tag);
		mViewHolder.myAskBtn = (TextView) mViewHolder.view
				.findViewById(R.id.fragment_user_my_askp_btn);
		mViewHolder.myWorksBtn = (TextView) mViewHolder.view
				.findViewById(R.id.fragment_user_my_works_btn);
		mViewHolder.myInProgressBtn = (TextView) mViewHolder.view
				.findViewById(R.id.fragment_user_in_progress_btn);
		mViewHolder.myCollectionsBtn = (TextView) mViewHolder.view
				.findViewById(R.id.fragment_user_collections_btn);
		mViewHolder.followingBtn = (TextView) mViewHolder.view
				.findViewById(R.id.fragment_user_my_follow_btn);
		mViewHolder.settingBtn = (TextView) mViewHolder.view
				.findViewById(R.id.fragment_user_setting_btn);
		initListeners();

		initUserFragmentData();
		// try {
		// mDatabaseHelper = OpenHelperManager.getHelper(getActivity(),
		// DatabaseHelper.class);
		// mUserInfoDao = mDatabaseHelper.getDao(UserInfo.class);
		// mUserInfo = mUserInfoDao.queryForAll().get(0);
		//
		// initUserFragmentData();
		// Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
		// "user nick: " + mUserInfo.getNickName());
		// } catch (Exception e) {
		// Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
		// "no user data");
		// }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Logger.logMethod(TAG, "onCreateView");
		FrameLayout parentView = new FrameLayout(getActivity());
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		parentView.setLayoutParams(params);
		mViewHolder.parentView.removeView(mViewHolder.view);
		parentView.addView(mViewHolder.view);
		mViewHolder.parentView = parentView;
		return parentView;
	}

	// 初始化用户个人数据
	public void initUserFragmentData() {
		// 先加载本地数据
		LoginUser user = LoginUser.getInstance();

		mActionBar = (ActionBar) mViewHolder.view.findViewById(R.id.actionbar);
		mActionBar.setTitle(user.getNickname());

		mViewHolder.myAskBtn.setText("我的求P （"
				+ Integer.toString(user.getAskCount()) + "）");
		mViewHolder.myWorksBtn.setText("我的作品 （"
				+ Integer.toString(user.getReplyCount()) + "）");

		ImageLoader loader = ImageLoader.getInstance();
		loader.displayImage(user.getAvatarImageUrl(), mViewHolder.avatarIv,
				mOptions);

		mViewHolder.followerCountTv.setText(Integer.toString(user
				.getFollowerCount()));
		mViewHolder.likeCountTv.setText(Integer.toString(user.getLikedCount()));

		// 请求后台用户数据进行更新
		GetUserInfoRequest.Builder builder = new GetUserInfoRequest.Builder()
				.setListener(getUserInfoListener);

		GetUserInfoRequest request = builder.build();
		request.setTag(TAG);
		RequestQueue requestQueue = PSGodRequestQueue
				.getInstance(getActivity()).getRequestQueue();
		requestQueue.add(request);
	}

	// 获取用户后台信息之后回调
	private Listener<JSONObject> getUserInfoListener = new Listener<JSONObject>() {
		@Override
		public void onResponse(JSONObject response) {
			if (response != null) {
				// TODO 效率不太高 待改进
				LoginUser.getInstance().initFromJSONObject(response);

				LoginUser user = LoginUser.getInstance();
				mActionBar = (ActionBar) mViewHolder.view
						.findViewById(R.id.actionbar);
				mActionBar.setTitle(user.getNickname());

				mViewHolder.myAskBtn.setText("我的求P （"
						+ Integer.toString(user.getAskCount()) + "）");
				mViewHolder.myWorksBtn.setText("我的作品 （"
						+ Integer.toString(user.getReplyCount()) + "）");

				ImageLoader loader = ImageLoader.getInstance();
				loader.displayImage(user.getAvatarImageUrl(),
						mViewHolder.avatarIv, mOptions);

				mViewHolder.followerCountTv.setText(Integer.toString(user
						.getFollowerCount()));
				mViewHolder.likeCountTv.setText(Integer.toString(user
						.getFollowingCount()));
			}
		}
	};

	/**
	 * 设置各个按钮的动作监听器
	 */
	private void initListeners() {
		// mViewHolder.avatarIv.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// if (mViewHolder.choosePhotoDialog == null) {
		// mViewHolder.choosePhotoDialog = new Dialog(getActivity(),
		// R.style.ActionSheetDialog);
		// mViewHolder.choosePhotoDialog.setContentView(R.layout.dialog_set_avatar);
		// mViewHolder.choosePhotoDialog.getWindow().getAttributes().width =
		// Constants.WIDTH_OF_SCREEN;
		// mViewHolder.choosePhotoDialog.setCanceledOnTouchOutside(true);
		//
		// // 设置点击拍照按钮 动作监听器
		// Button takePhotoButton = (Button) mViewHolder.choosePhotoDialog
		// .findViewById(R.id.dialog_set_avatar_take_photo);
		// takePhotoButton.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View arg0) {
		// mViewHolder.choosePhotoDialog.dismiss();
		// ChoosePhotoActivity.startActivity(
		// getActivity(),
		// ChoosePhotoActivity.FROM_CAMERA,
		// SetAvatarActivity.class.getName(),
		// REQUEST_UPLOAD_IMAGE, null);
		// }
		// });
		//
		// // 设置本地选择图片上传 动作监听器
		// Button choosePhotoBtn = (Button) mViewHolder.choosePhotoDialog
		// .findViewById(R.id.dialog_set_avatar_choose_photo);
		// choosePhotoBtn.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// mViewHolder.choosePhotoDialog.dismiss();
		// ChoosePhotoActivity.startActivity(
		// getActivity(),
		// ChoosePhotoActivity.FROM_ALBUM,
		// SetAvatarActivity.class.getName(),
		// REQUEST_UPLOAD_IMAGE, null);
		// }
		// });
		//
		// // 设置取消按钮动作监听器
		// Button cancelBtn = (Button) mViewHolder.choosePhotoDialog
		// .findViewById(R.id.dialog_set_avatar_cancel);
		// cancelBtn.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View arg0) {
		// mViewHolder.choosePhotoDialog.dismiss();
		// }
		// });
		// }
		//
		// if (mViewHolder.choosePhotoDialog.isShowing()) {
		// mViewHolder.choosePhotoDialog.dismiss();
		// } else {
		// mViewHolder.choosePhotoDialog.show();
		// mViewHolder.choosePhotoDialog.getWindow().setGravity(Gravity.BOTTOM);
		// }
		// }
		// });

		OnClickListener followerListListener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				Activity activity = getActivity();
				Intent intent = new Intent(activity, FollowerListActivity.class);
				activity.startActivity(intent);
			}
		};

		mViewHolder.followerTv.setOnClickListener(followerListListener);
		mViewHolder.followerCountTv.setOnClickListener(followerListListener);

		mViewHolder.likeTv = (TextView) mViewHolder.view
				.findViewById(R.id.fragment_user_like_tag);
		mViewHolder.likeCountTv = (TextView) mViewHolder.view
				.findViewById(R.id.fragment_user_like_count_tag);

		mViewHolder.myAskBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Activity activity = getActivity();
				Intent intent = new Intent(activity, MyAskActivity.class);
				activity.startActivity(intent);
			}
		});

		mViewHolder.myWorksBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Activity activity = getActivity();
				Intent intent = new Intent(activity, MyWorksActivity.class);
				activity.startActivity(intent);
			}
		});

		mViewHolder.myInProgressBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Activity activity = getActivity();
				Intent intent = new Intent(activity, MyInProgressActivity.class);
				// Intent intent = new Intent(activity,WorksListActivity.class);
				activity.startActivity(intent);
			}
		});

		mViewHolder.myCollectionsBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Activity activity = getActivity();
				Intent intent = new Intent(activity,
						MyCollectionsActivity.class);
				activity.startActivity(intent);
			}
		});

		mViewHolder.followingBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Activity activity = getActivity();
				Intent intent = new Intent(activity,
						FollowingListActivity.class);
				activity.startActivity(intent);
			}
		});

		mViewHolder.settingBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Activity activity = getActivity();
				Intent intent = new Intent(activity, SettingActivity.class);
				activity.startActivity(intent);
			}
		});
	}

	// 上传头像的回调
	// @Override
	// public void onActivityResult(int requestCode, int resultCode, Intent
	// data) {
	// super.onActivityResult(requestCode, resultCode, data);
	// Log.v("TEST", Integer.toString(requestCode));
	// if (resultCode == RESULT_OK) {
	// if (requestCode == REQUEST_UPLOAD_IMAGE) {
	// if (data == null) {
	// Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_COLOR,
	// TAG,
	// "onActivityResult(): REQUEST_UPLOAD_IMAGE, data is null");
	// return;
	// }
	//
	// String path = data.getExtras().getString(
	// Constants.IntentKey.PHOTO_PATH);
	// Long imgId = data.getExtras().getLong("imageId");
	//
	// Log.v("TEST", Long.toString(imgId));
	// if (!TextUtils.isEmpty(path)) {
	// ModifyUserData.Builder builder = new ModifyUserData.Builder()
	// .setAvatar(imgId)
	// .setListener(modifyAvatarListener)
	// .setErrorListener(errorListener);
	// ModifyUserData request = builder.build();
	// request.setTag(TAG);
	// RequestQueue reqeustQueue = PSGodRequestQueue.getInstance(
	// PSGodApplication.getAppContext()).getRequestQueue();
	// reqeustQueue.add(request);
	// reqeustQueue.start();
	//
	// //更新头像
	// Bitmap image = BitmapFactory.decodeFile(path);
	// mViewHolder.avatarIv.setImageBitmap(image);
	// }
	// }
	// }
	// }
	//
	// private PSGodErrorListener errorListener = new
	// PSGodErrorListener(EditProfileActivity.class.getSimpleName()) {
	// @Override
	// public void handleError(VolleyError error) {
	// }
	// };
	//
	// private Listener<Integer> modifyAvatarListener = new Listener<Integer>()
	// {
	// @Override
	// public void onResponse(Integer response) {
	// if (response == 1) {
	// Toast.makeText(PSGodApplication.getAppContext(), "修改头像成功",
	// Toast.LENGTH_SHORT).show();
	// }
	// if (response == 0) {
	// Toast.makeText(PSGodApplication.getAppContext(), "修改头像失败，请稍后再试",
	// Toast.LENGTH_SHORT).show();
	// }
	// }
	// };

	/**
	 * 保存视图组件，避免视图的重复加载
	 * 
	 * @author Rayal
	 * 
	 */
	private static class ViewHolder {
		ViewGroup parentView;
		View view;
		Dialog choosePhotoDialog;
		CircleImageView avatarIv;
		CircleImageView genderIv;
		TextView followerTv;
		TextView followerCountTv;
		TextView likeTv;
		TextView likeCountTv;
		TextView myAskBtn;
		TextView myWorksBtn;
		TextView myInProgressBtn;
		TextView myCollectionsBtn;
		TextView followingBtn;
		TextView settingBtn;
	}
}
