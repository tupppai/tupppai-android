package com.psgod.ui.activity;

import java.util.HashMap;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.model.LoginUser;
import com.psgod.network.request.BaseRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.WeiboLoginRequest;
import com.psgod.network.request.WeiboLoginRequest.WeiboLoginWrapper;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;
import com.psgod.ui.widget.dialog.OtherRegisterDialog;

/**
 * 欢迎界面
 * 
 * @author brandwang
 * 
 */
public class WelcomeActivity extends PSGodBaseActivity implements
		OnClickListener {
	private final static String TAG = WelcomeActivity.class.getSimpleName();
	public static final int JUMP_FROM_LOGIN = 100;
	private Context mContext;

	private TextView mOthersRegisterBtn;
	private TextView mLoginBtn;
	private RelativeLayout mWeiboRegisterLayout;

	private ImageView welcomeImg;

	private String mThirdAuthId = "";
	private String mThirdAuthName = "";
	private String mThirdAuthGender = "";
	private String mThirdAuthAvatar = "";

	private CustomProgressingDialog mProgressDialog;
	private OtherRegisterDialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		mContext = this;

		ShareSDK.initSDK(this);

		initViews();
		initEvents();
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		boolean isFinishActivity = intent.getBooleanExtra(
				Constants.IntentKey.IS_FINISH_ACTIVITY, false);
		intent.removeExtra(Constants.IntentKey.IS_FINISH_ACTIVITY);
		if (isFinishActivity) {
			String destActivityName = intent
					.getStringExtra(Constants.IntentKey.DEST_ACTIVITY_NAME);

			if (!TextUtils.isEmpty(destActivityName)) {
				Intent newIntent = new Intent();
				newIntent.setClassName(WelcomeActivity.this, destActivityName);
				Bundle extras = intent.getExtras();
				if (extras != null) {
					newIntent.putExtras(extras);
				}
				startActivity(newIntent);
				finish();
			}
		}
	}

	private RelativeLayout mParent;
	protected void initViews() {
		mOthersRegisterBtn = (TextView) findViewById(R.id.others_register_btn);
		mLoginBtn = (TextView) findViewById(R.id.welcome_activity_login_link);
		mWeiboRegisterLayout = (RelativeLayout) findViewById(R.id.weibo_register_layout);
		welcomeImg = (ImageView) findViewById(R.id.welcome_picture);
		mParent = (RelativeLayout) findViewById(R.id.welcome_parent);
		if(BaseRequest.PSGOD_BASE_URL.equals(BaseRequest.PSGOD_BASE_TEST_URL)){
			mParent.setBackground(getResources().getDrawable(R.color.color_9fc25b));
		}else{
			mParent.setBackground(getResources().getDrawable(R.drawable.shape_welcome_gradient));
		}
	}

	int n = 0;

	protected void initEvents() {
		mOthersRegisterBtn.setOnClickListener(this);
		mLoginBtn.setOnClickListener(this);
		mWeiboRegisterLayout.setOnClickListener(this);
		welcomeImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (n == 7) {
					Toast.makeText(WelcomeActivity.this, "切换到测试连接",
							Toast.LENGTH_SHORT).show();
					BaseRequest.PSGOD_BASE_URL = BaseRequest.PSGOD_BASE_TEST_URL;
					mParent.setBackground(getResources().getDrawable(R.color.color_9fc25b));
				} else if (n == 8) {
					Toast.makeText(WelcomeActivity.this, "切换到正式连接",
							Toast.LENGTH_SHORT).show();
					BaseRequest.PSGOD_BASE_URL = BaseRequest.PSGOD_BASE_RELEASE_URL;
					mParent.setBackground(getResources().getDrawable(R.drawable.shape_welcome_gradient));
					n = 0;
				}
				n++;
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 微博注册（若已经注册 则直接登录 否则走注册流程）
		case R.id.weibo_register_layout:
			if (mProgressDialog == null) {
				mProgressDialog = new CustomProgressingDialog(
						WelcomeActivity.this);
			}
			if (!mProgressDialog.isShowing()) {
				mProgressDialog.show();
			}

			Platform weibo = ShareSDK.getPlatform(SinaWeibo.NAME);
			weibo.SSOSetting(false);
			weibo.setPlatformActionListener(weiboLoginListener);
			if (weibo.isValid()) {
				weibo.removeAccount();
				ShareSDK.removeCookieOnAuthorize(true);
			}
			weibo.showUser(null);
			break;

		// 其他方式注册
		case R.id.others_register_btn:
			if (dialog == null) {
				dialog = new OtherRegisterDialog(mContext);
			}
			if (dialog.isShowing()) {
				dialog.dismiss();
			} else {
				dialog.show();
			}
			break;

		// 已有账号登录
		case R.id.welcome_activity_login_link:
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			break;

		default:
			break;
		}
	}

	// 微博登录的监听事件
	private PlatformActionListener weiboLoginListener = new PlatformActionListener() {
		@Override
		public void onError(Platform arg0, int arg1, Throwable arg2) {
			if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		}

		// 用户授权
		@Override
		public void onComplete(Platform arg0, int arg1,
				HashMap<String, Object> res) {
			mThirdAuthId = res.get("id").toString();
			mThirdAuthName = res.get("name").toString();
			mThirdAuthAvatar = res.get("profile_image_url").toString();

			if (!TextUtils.isEmpty(mThirdAuthId)) {
				WeiboLoginRequest.Builder builder = new WeiboLoginRequest.Builder()
						.setCode(mThirdAuthId).setListener(weiboAuthListener)
						.setErrorListener(errorListener);

				WeiboLoginRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						WelcomeActivity.this).getRequestQueue();
				requestQueue.add(request);
			}
		}

		@Override
		public void onCancel(Platform arg0, int arg1) {
			// TODO Auto-generated method stub
			if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		}
	};

	// 新浪微博请求登录回调
	private Listener<WeiboLoginWrapper> weiboAuthListener = new Listener<WeiboLoginRequest.WeiboLoginWrapper>() {
		@Override
		public void onResponse(WeiboLoginWrapper response) {
			if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}

			try {
				int isRegistered = response.isRegistered;
				// 已注册
				if (isRegistered == 1) {
					JSONObject userInfoData = response.UserObject;
					LoginUser.getInstance().initFromJSONObject(userInfoData);

					Bundle extras = new Bundle();
					extras.putInt(Constants.IntentKey.ACTIVITY_JUMP_FROM,
							JUMP_FROM_LOGIN);
					MainActivity.startNewActivityAndFinishAllBefore(
							WelcomeActivity.this, MainActivity.class.getName(),
							extras);
				}
				// 未注册
				if (isRegistered == 0) {
					Intent intent = new Intent(WelcomeActivity.this,
							SetInfoActivity.class);

					intent.putExtra(
							Constants.ThirdAuthInfo.THIRD_AUTH_PLATFORM,
							"weibo");
					intent.putExtra(Constants.ThirdAuthInfo.USER_OPENID,
							mThirdAuthId);
					intent.putExtra(Constants.ThirdAuthInfo.USER_NICKNAME,
							mThirdAuthName);
					intent.putExtra(Constants.ThirdAuthInfo.USER_AVATAR,
							mThirdAuthAvatar);

					startActivity(intent);
					finish();
				}
			} catch (Exception e) {

			}
		}
	};

	private PSGodErrorListener errorListener = new PSGodErrorListener() {
		@Override
		public void handleError(VolleyError error) {
			if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		}
	};

	public static void startNewActivityAndFinishAllBefore(Context context,
			String destActivityName, Bundle extras) {
		Intent intent = new Intent(context, WelcomeActivity.class);
		if (extras != null) {
			intent.putExtras(extras);
		}
		intent.putExtra(Constants.IntentKey.DEST_ACTIVITY_NAME,
				destActivityName);
		intent.putExtra(Constants.IntentKey.IS_FINISH_ACTIVITY, true);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
}
