package com.pires.wesee.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.pires.wesee.Constants;
import com.pires.wesee.R;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

/**
 * 分享弹窗
 */
public class RecommendFriendsDialog extends Dialog {
	private static final String TAG = RecommendFriendsDialog.class.getSimpleName();

	private Context mContext;
	
	private Button mShareWechatFriend;
	private Button mShareWechatMoments;
	private Button mShareWeibo;
	private Button mShareQQ;
	private Button mShareQzone;

	private Button mCancelBtn;
	
	public RecommendFriendsDialog(Context context) {
		super(context, R.style.ActionSheetDialog);
		setContentView(R.layout.dialog_recommend_friends);
		
		mContext = context;
		
		getWindow().getAttributes().width = Constants.WIDTH_OF_SCREEN;
		setCanceledOnTouchOutside(true);
		
		//初始化组件
		mContext = context;
		
		mShareWechatFriend = (Button) findViewById(R.id.dialog_recommend_friends_wechat);
		mShareWechatMoments = (Button) findViewById(R.id.dialog_recommend_friends_moments);
		mShareWeibo = (Button) findViewById(R.id.dialog_recommend_friends_sina);
		mShareQQ = (Button) findViewById(R.id.dialog_recommend_friends_qq_friend);
		mShareQzone = (Button) findViewById(R.id.dialog_recommend_friends_qzone);
		
		mCancelBtn = (Button) findViewById(R.id.dialog_recommend_friends_cancel);
		
		initListeners();
	}
	
	private ErrorListener errorListener = new ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {
			Toast.makeText(mContext, error.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	};
	
	private void initListeners() {
		
		//分享到新浪微博
		mShareWeibo.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ShareSDK.initSDK(mContext);
				
				OnekeyShare oks = new OnekeyShare();
				oks.setPlatform(SinaWeibo.NAME);
				oks.disableSSOWhenAuthorize();
				oks.setSilent(false);
				
				oks.setText("分享title");
				oks.setImageUrl("http://7u2spr.com1.z0.glb.clouddn.com/20150326-1451205513ac68292ea.jpg");
				oks.show(mContext);
			}
		});
		
		//分享到qq
		mShareQQ.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				OnekeyShare oks = new OnekeyShare();
				
				oks.setPlatform(QQ.NAME);
				oks.setTitle("分享title");
				oks.setTitleUrl("http://qiupsdashen.com");
				oks.setText("分享描述");
				oks.setImageUrl("http://7u2spr.com1.z0.glb.clouddn.com/20150326-1451205513ac68292ea.jpg");

				oks.show(mContext);
			}
		});
		
		//分享到qzone
		mShareQzone.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				OnekeyShare oks = new OnekeyShare();
				
				oks.setPlatform(QZone.NAME);
				oks.setTitle("title");
				oks.setText("desc");
				oks.setTitleUrl("http://qiupsdashen.com");
				oks.setImageUrl("http://7u2spr.com1.z0.glb.clouddn.com/20150326-1451205513ac68292ea.jpg");
				
				oks.setSite("图pai");
				oks.setSiteUrl(Constants.OFFICAL_WEBSITE);
				
				oks.show(mContext);
			}
		});
		
		//分享到微信朋友圈
		mShareWechatMoments.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ShareSDK.initSDK(mContext);
				Platform wechat = ShareSDK.getPlatform(mContext,
						WechatMoments.NAME);
				wechat.setPlatformActionListener(null);
				
				ShareParams sp = new ShareParams();
				sp.setShareType(Platform.SHARE_IMAGE);
				sp.setText("desc");
				sp.setImageUrl("http://7u2spr.com1.z0.glb.clouddn.com/20150326-1451205513ac68292ea.jpg");
				
				wechat.share(sp);
			}
		});
		
		//分享给微信好友
		mShareWechatFriend.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ShareSDK.initSDK(mContext);
				Platform wechatFriends = ShareSDK.getPlatform(mContext, Wechat.NAME);
				wechatFriends.setPlatformActionListener(null);

				ShareParams sp = new ShareParams();
				sp.setShareType(Platform.SHARE_IMAGE);
				sp.setText("desc");
				sp.setImageUrl("http://7u2spr.com1.z0.glb.clouddn.com/20150326-1451205513ac68292ea.jpg");
				wechatFriends.share(sp);
			}
		});
		
		mCancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
			}
		});
	}
	
	@Override
	public void show() {
		super.show();
		getWindow().setGravity(Gravity.BOTTOM);
		getWindow().setWindowAnimations(R.style.popwindow_anim_style);
	}
}
