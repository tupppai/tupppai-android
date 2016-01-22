package com.psgod.ui.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.psgod.PsGodImageLoader;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.UserPreferences;
import com.psgod.Utils;
import com.psgod.eventbus.InitEvent;
import com.psgod.network.request.BaseRequest;
import com.psgod.ui.widget.dialog.CustomDialog;
import com.psgod.ui.widget.dialog.RecommendFriendsDialog;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

import java.util.LinkedList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 帐号设置界面
 * 
 * @author Rayal
 * 
 */
public class SettingActivity extends PSGodBaseActivity {
	private View mAccountSafeBtn;
	private View mNotificationBtn;
	// private View mPasswordBtn;
	// private View mAccountBtn;
	private View mClearCacheBtn;
	private View mAboutUsBtn;
	private View mFeedbackBtn;
	private View mRatingBtn;
	private View mExitBtn;
	private View mEditInfoBtn;
	private View mUpdateBtn;
	private View mLikedBtn;
	private View mCommendBtn;
	private View mChangeBtn;
	private ImageView mChangeNotificationIv;

	// activity list
	private List<Activity> mList = new LinkedList<Activity>();
	private RecommendFriendsDialog mRecommendFriendDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		mAccountSafeBtn = this
				.findViewById(R.id.activity_setting_account_safe_btn);
		mNotificationBtn = this
				.findViewById(R.id.activity_setting_notification_btn);
		// mPasswordBtn = this.findViewById(R.id.activity_setting_password_btn);
		// mAccountBtn = this.findViewById(R.id.activity_setting_account_btn);
		mClearCacheBtn = this
				.findViewById(R.id.activity_setting_clear_cache_btn);
		mAboutUsBtn = this.findViewById(R.id.activity_setting_about_us_btn);
		mFeedbackBtn = this.findViewById(R.id.activity_setting_feedback_btn);
		mRatingBtn = this.findViewById(R.id.activity_setting_rating_btn);
		mExitBtn = this.findViewById(R.id.activity_setting_exit_btn);
		mEditInfoBtn = this.findViewById(R.id.activity_setting_editInfo_btn);
		mUpdateBtn = this.findViewById(R.id.activity_setting_check_new_version);
		mLikedBtn = this.findViewById(R.id.activity_setting_liked_btn);
		mCommendBtn = this.findViewById(R.id.activity_setting_commend_btn);
		mChangeBtn = this.findViewById(R.id.activity_setting_change_btn);
		mChangeNotificationIv = (ImageView) this.findViewById(R.id.activity_change_notification_btn);
		initButtonListeners();

		// mRatingBtn.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View view) {
		// Intent intent = new Intent(SettingActivity.this, MainActivity.class);
		// intent.putExtra(MainActivity.IntentParams.KEY_FRAGMENT_ID,
		// MainActivity.IntentParams.VALUE_FRAGMENT_ID_MESSAGE);
		// startActivity(intent);
		// finish();
		// }
		// });

		// mExitBtn.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View view) {
		// Intent intent = new Intent(SettingActivity.this, MainActivity.class);
		// intent.putExtra(MainActivity.IntentParams.KEY_FRAGMENT_ID,
		// MainActivity.IntentParams.VALUE_FRAGMENT_ID_HOMEPAGE);
		// intent.putExtra(MainActivity.IntentParams.KEY_HOMEPAGE_ID,
		// MainActivity.IntentParams.VALUE_HOMEPAGE_ID_RECENT);
		// intent.putExtra(MainActivity.IntentParams.KEY_NEED_REFRESH, true);
		// startActivity(intent);
		// finish();
		// }
		// });
	}

	/**
	 * 设置按钮的点击动作监听器
	 */
	private void initButtonListeners() {
		// 账号与安全
		mAccountSafeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SettingActivity.this,
						SettingAccountSafeActivity.class);
				startActivity(intent);
			}
		});

		// 手动检查新版本
		mUpdateBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Utils.showProgressDialog(SettingActivity.this);

				UmengUpdateAgent.setUpdateAutoPopup(false);
				UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
					@Override
					public void onUpdateReturned(int updateStatus,
							UpdateResponse updateInfo) {
						Utils.hideProgressDialog();

						switch (updateStatus) {
						case UpdateStatus.Yes: // has update
							UmengUpdateAgent.showUpdateDialog(
									SettingActivity.this, updateInfo);
							break;
						case UpdateStatus.No: // has no update
							Toast.makeText(SettingActivity.this, "已经是最新了哦",
									Toast.LENGTH_SHORT).show();
							break;
						case UpdateStatus.NoneWifi: // none wifi
							Toast.makeText(SettingActivity.this,
									"建议在Wifi下进行更新哦", Toast.LENGTH_SHORT).show();
							break;
						case UpdateStatus.Timeout: // time out
							Toast.makeText(SettingActivity.this,
									"检查更新失败，请稍后再试", Toast.LENGTH_SHORT).show();
							break;
						}
					}
				});
				UmengUpdateAgent.setUpdateOnlyWifi(false);
				UmengUpdateAgent.update(SettingActivity.this);
			}
		});

		mChangeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(SettingActivity.this,SettingChangeActivity.class);
				startActivity(intent);
			}
		});

		// 我赞过的
		mLikedBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SettingActivity.this,
						SettingLikedActivity.class);
				startActivity(intent);
			}
		});

		// 我评论过的
		mCommendBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SettingActivity.this,
						SettingCommentActivity.class);
				startActivity(intent);
			}
		});

		// 给应用评分 打开三方应用市场
		mRatingBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				try {
					Uri uri = Uri.parse("market://details?id="
							+ getPackageName());
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				} catch (Exception e) {
					Toast.makeText(SettingActivity.this, "打开应用市场失败",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		// 推荐应用给好友
		// mRecommendBtn.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View arg0) {
		// if (mRecommendFriendDialog == null) {
		// mRecommendFriendDialog = new
		// RecommendFriendsDialog(SettingActivity.this);
		// }
		//
		// if (mRecommendFriendDialog.isShowing()) {
		// mRecommendFriendDialog.dismiss();
		// } else {
		// mRecommendFriendDialog.show();
		// }
		//
		// ShareSDK.initSDK(SettingActivity.this);
		// OnekeyShare oks = new OnekeyShare();
		// // 关闭sso授权
		// oks.disableSSOWhenAuthorize();
		//
		// // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
		// oks.setTitle("求PS大神Title！");
		// // text是分享文本，所有平台都需要这个字段
		// oks.setText("欢迎下载使用！ http://www.badu.com");
		// // url仅在微信（包括好友和朋友圈）中使用
		// oks.setUrl("http://www.qiupsdashen.com/mobile/view/recommend.html");
		//
		// oks.setImageUrl("http://7u2spr.com1.z0.glb.clouddn.com/20150326-1451205513ac68292ea.jpg");
		//
		// // 启动分享GUI
		// oks.show(SettingActivity.this);
		// }
		// });

		// 清理缓存
		mClearCacheBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				CustomDialog.Builder builder = new CustomDialog.Builder(
						SettingActivity.this)
						.setMessage("确定要清除掉缓存吗？")
						.setLeftButton("取消",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										// dialog框消失
									}
								})
						.setRightButton("确定",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										PsGodImageLoader.getInstance()
												.clearDiskCache();
										Toast.makeText(SettingActivity.this,
												"缓存清理成功", Toast.LENGTH_SHORT)
												.show();
									}
								});
				builder.create().show();
			}
		});

		// 编辑资料
		mEditInfoBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(SettingActivity.this,
						EditProfileActivity.class);
				startActivity(intent);
			}
		});

		// 用户反馈
		mFeedbackBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(SettingActivity.this,
						FeedBackActivity.class);
				startActivity(intent);
			}
		});

		// 消息提醒
		mNotificationBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(SettingActivity.this,
						SettingNotificationActivity.class);
				startActivity(intent);
			}
		});

		mAboutUsBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(SettingActivity.this,
						WebBrowserActivity.class);
				intent.putExtra(WebBrowserActivity.KEY_URL, BaseRequest.PSGOD_BASE_URL+
						"mobile/contacts.html" + "?version=" + getVersion());
				intent.putExtra(WebBrowserActivity.KEY_DESC,"关于我们");
				startActivity(intent);
			}
		});

		// 退出当前账号
		mExitBtn.setOnClickListener(new OnClickListener() { 
			@Override
			public void onClick(View view) {
				CustomDialog.Builder builder = new CustomDialog.Builder(
						SettingActivity.this)
						.setMessage("确定要退出？")
						.setLeftButton("取消",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										// dialog框消失

									}
								})
						.setRightButton("确定",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										UserPreferences.TokenVerify
												.setToken(""); // 将本地token清空
										Constants.IS_FOCUS_FRAGMENT_CREATED = false;
										Constants.IS_HOME_FRAGMENT_CREATED = false;
										Constants.IS_MESSAGE_FRAGMENT_CREATED = false;
										Constants.IS_USER_FRAGMENT_CREATED = false;
										Constants.IS_INPROGRESS_FRAGMENT_CREATED = false;

										NewLoginInputPhoneActivity
												.startNewActivityAndFinishAllBefore(
														SettingActivity.this,
														NewLoginInputPhoneActivity.class
																.getName(),
														null);
									}
								});

				builder.create().show();
			}
		});
	}

	@Override
	public void finish() {
		EventBus.getDefault().post(new InitEvent());
		super.finish();
	}

	/**
	 * 获取版本号
	 * @return 当前应用的版本号
	 */
	public String getVersion() {
	    try {
	        PackageManager manager = this.getPackageManager();
	        PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
	        String version = info.versionName;
	        return version;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return e.toString();
	    }
	}
}
