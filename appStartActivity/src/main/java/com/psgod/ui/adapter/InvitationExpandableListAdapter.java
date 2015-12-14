package com.psgod.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.psgod.PsGodImageLoader;
import com.psgod.Constants;
import com.psgod.PSGodApplication;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.User;
import com.psgod.network.request.ActionFollowRequest;
import com.psgod.network.request.ActionInviteRequest;
import com.psgod.network.request.ActionShareRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.ui.widget.AvatarImageView;
import com.psgod.ui.widget.dialog.CustomDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

public class InvitationExpandableListAdapter extends BaseExpandableListAdapter {
	private static final String TAG = InvitationExpandableListAdapter.class
			.getSimpleName();

	private static final int TYPE_SHARE = 0;
	private static final int TYPE_PSGOD = 1;
	private static final int TYPE_FRIENDS = 2;

	private long mAskId;

	private static Map<Integer, String> GROUP_TITIES;
	static {
		GROUP_TITIES = new HashMap<Integer, String>();
		GROUP_TITIES.put(TYPE_SHARE, "微信邀请");
		GROUP_TITIES.put(TYPE_PSGOD, "邀请大神");
		GROUP_TITIES.put(TYPE_FRIENDS, "邀请好友");
	}

	private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;
	private Context mContext;
	private View mShareView;
	private List<User> mPsGodList;
	private List<User> mFriendList;

	public InvitationExpandableListAdapter(Context context,
			List<User> psGodList, List<User> friendList, long id) {
		mContext = context;
		mPsGodList = psGodList;
		mFriendList = friendList;
		mAskId = id;

	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		int type = getGropuType(groupPosition);
		switch (type) {
		case TYPE_PSGOD:
			if ((childPosition >= 0) && (childPosition < mPsGodList.size())) {
				return mPsGodList.get(childPosition);
			}
		case TYPE_FRIENDS:
			if ((childPosition >= 0) && (childPosition < mFriendList.size())) {
				return mFriendList.get(childPosition);
			}
		}
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		Object obj = getChild(groupPosition, childPosition);
		if (obj instanceof User) {
			return ((User) obj).getUid();
		} else {
			return -1;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		int type = getGropuType(groupPosition);
		if ((type != TYPE_PSGOD) && (type != TYPE_FRIENDS)) {
			return null;
		}

		final User user = (User) getChild(groupPosition, childPosition);

		final ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.activity_invitation_list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.avatarIv = (AvatarImageView) convertView
					.findViewById(R.id.item_user_list_avatar_imgview);
			viewHolder.nameTv = (TextView) convertView
					.findViewById(R.id.item_user_list_name_textview);
			viewHolder.inviteBtn = (Button) convertView
					.findViewById(R.id.item_user_list_invite_btn);
			viewHolder.followerCountTv = (TextView) convertView
					.findViewById(R.id.item_user_list_follower_num_tag);
			viewHolder.askCountTv = (TextView) convertView
					.findViewById(R.id.item_user_list_ask_num_tag);
			viewHolder.workCountTv = (TextView) convertView
					.findViewById(R.id.item_user_list_work_num_tag);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		PsGodImageLoader imageLoader = PsGodImageLoader.getInstance();
		imageLoader.displayImage(user.getAvatarImageUrl(), viewHolder.avatarIv,
				mAvatarOptions);

		viewHolder.nameTv.setText(user.getNickname());
		viewHolder.followerCountTv.setText(user.getFollowerCount() + "粉丝");
		viewHolder.askCountTv.setText(user.getFollowingCount() + "求P");
		viewHolder.workCountTv.setText(user.getReplyCount() + "作品");

		viewHolder.avatarIv.setUserId(user.getUid());

		// 是否被邀请
		if (user.getIsInvited()) {
			if (android.os.Build.VERSION.SDK_INT >= 16) {
				viewHolder.inviteBtn
						.setBackground(mContext.getResources().getDrawable(
								R.drawable.shape_invitation_list_item_selected));
			} else {
				viewHolder.inviteBtn
						.setBackgroundDrawable(mContext
								.getResources()
								.getDrawable(
										R.drawable.shape_invitation_list_item_selected));
			}
			viewHolder.inviteBtn.setText("已邀请");
			viewHolder.inviteBtn.setEnabled(false);
		} else {
			if (android.os.Build.VERSION.SDK_INT >= 16) {
				viewHolder.inviteBtn.setBackground(mContext.getResources()
						.getDrawable(R.drawable.shape_invitation_list_btn));
			} else {
				viewHolder.inviteBtn.setBackgroundDrawable(mContext
						.getResources().getDrawable(
								R.drawable.shape_invitation_list_btn));
			}
			viewHolder.inviteBtn.setText("邀请");
			viewHolder.inviteBtn.setEnabled(true);
		}

		viewHolder.inviteBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (user.isFollowing() == 0) {

					(new CustomDialog.Builder(mContext))
							.setMessage("你没有关注这位大神不能发送邀请Ta.")
							.setLeftButton("取消", null)
							.setRightButton("关注并邀请",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											viewHolder.inviteBtn
													.setClickable(false);
											ActionFollowRequest.Builder builder = new ActionFollowRequest.Builder()
													.setType(0)
													.setUid(user.getUid())
													.setErrorListener(
															errorListener)
													.setListener(
															new Listener<Boolean>() {
																@Override
																public void onResponse(
																		Boolean response) {
																	if (response == true) {
																		// 关注用户有变化
																		// 需要刷新关注页面
																		Constants.IS_FOLLOW_NEW_USER = true;

																		ActionInviteRequest.Builder builder = new ActionInviteRequest.Builder()
																				.setAskId(
																						mAskId)
																				.setUid(user
																						.getUid())
																				.setErrorListener(
																						errorListener)
																				.setListener(
																						new Listener<Boolean>() {
																							@Override
																							public void onResponse(
																									Boolean response) {
																								if (response) {
																									if (android.os.Build.VERSION.SDK_INT >= 16) {
																										viewHolder.inviteBtn
																												.setBackground(mContext
																														.getResources()
																														.getDrawable(
																																R.drawable.shape_invitation_list_item_selected));
																									} else {
																										viewHolder.inviteBtn
																												.setBackgroundDrawable(mContext
																														.getResources()
																														.getDrawable(
																																R.drawable.shape_invitation_list_item_selected));
																									}

																									user.setIsInvited(true);
																									viewHolder.inviteBtn
																											.setText("已邀请");
																									viewHolder.inviteBtn
																											.setEnabled(false);

																									Toast.makeText(
																											mContext,
																											"邀请成功",
																											Toast.LENGTH_SHORT)
																											.show();
																								} else {
																									Toast.makeText(
																											mContext,
																											"邀请失败，请重试",
																											Toast.LENGTH_SHORT)
																											.show();
																								}
																							}
																						});
																		ActionInviteRequest request = builder
																				.build();
																		request.setTag(TAG);
																		RequestQueue requestQueue = PSGodRequestQueue
																				.getInstance(
																						mContext)
																				.getRequestQueue();
																		requestQueue
																				.add(request);
																	}
																}
															});

											ActionFollowRequest request = builder
													.build();
											request.setTag(TAG);
											RequestQueue requestQueue = PSGodRequestQueue
													.getInstance(mContext)
													.getRequestQueue();
											requestQueue.add(request);
										}

									}).create().show();
				} else {
					viewHolder.inviteBtn.setClickable(false);
					ActionInviteRequest.Builder builder = new ActionInviteRequest.Builder()
							.setAskId(mAskId).setUid(user.getUid())
							.setErrorListener(errorListener)
							.setListener(new Listener<Boolean>() {
								@Override
								public void onResponse(Boolean response) {
									if (response) {
										if (android.os.Build.VERSION.SDK_INT >= 16) {
											viewHolder.inviteBtn
													.setBackground(mContext
															.getResources()
															.getDrawable(
																	R.drawable.shape_invitation_list_item_selected));
										} else {
											viewHolder.inviteBtn
													.setBackgroundDrawable(mContext
															.getResources()
															.getDrawable(
																	R.drawable.shape_invitation_list_item_selected));
										}

										user.setIsInvited(true);
										viewHolder.inviteBtn.setText("已邀请");
										viewHolder.inviteBtn.setEnabled(false);

										Toast.makeText(mContext, "邀请成功",
												Toast.LENGTH_SHORT).show();
									} else {
										Toast.makeText(mContext, "邀请失败，请重试",
												Toast.LENGTH_SHORT).show();
									}
								}
							});
					ActionInviteRequest request = builder.build();
					request.setTag(TAG);
					RequestQueue requestQueue = PSGodRequestQueue.getInstance(
							mContext).getRequestQueue();
					requestQueue.add(request);
				}

			}
		});
		return convertView;
	}

	private PSGodErrorListener errorListener = new PSGodErrorListener(
			Button.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {
			// TODO Auto-generated method stub
			Utils.hideProgressDialog();
		}
	};

	@Override
	public int getChildrenCount(int groupPosition) {
		int type = getGropuType(groupPosition);
		switch (type) {
		case TYPE_SHARE:
			return 0;
		case TYPE_PSGOD:
			return mPsGodList.size();
		case TYPE_FRIENDS:
			return mFriendList.size();
		default:
			return 0;
		}
	}

	@Override
	public Object getGroup(int groupPosition) {
		int type = getGropuType(groupPosition);
		switch (type) {
		case TYPE_PSGOD:
			return mPsGodList;
		case TYPE_FRIENDS:
			return mFriendList;
		default:
			return null;
		}
	}

	@Override
	public int getGroupCount() {
		int groupCount = 1;
		if ((mPsGodList != null) && (mPsGodList.size() != 0)) {
			++groupCount;
		}

		if ((mFriendList != null) && (mFriendList.size() != 0)) {
			++groupCount;
		}

		return groupCount;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return -1;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		int type = getGropuType(groupPosition);
		if (type == TYPE_SHARE) {
			if (mShareView == null) {
				mShareView = createShareView();
			}
			return mShareView;
		}

		String title = GROUP_TITIES.get(type);
		convertView = LayoutInflater.from(mContext).inflate(
				R.layout.header_expandablelistview, null);
		((TextView) convertView.findViewById(R.id.header_expandablelistview_tv))
				.setText(title);
		return convertView;
	}

	private View createShareView() {
		View view = LayoutInflater.from(mContext).inflate(
				R.layout.header_invivation_expandablelistview, null);
		String title = GROUP_TITIES.get(TYPE_SHARE);
		((TextView) view.findViewById(R.id.header_expandablelistview_tv))
				.setText(title);

		// 设置分享按钮操作
		ImageButton shareToWeixingBtn = (ImageButton) view
				.findViewById(R.id.share_to_weixing_btn);
		ImageButton shareToMomentsBtn = (ImageButton) view
				.findViewById(R.id.share_to_moments_btn);

		// 分享到微信好友
		shareToWeixingBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.showProgressDialog(mContext);

				ActionShareRequest.Builder builder = new ActionShareRequest.Builder()
						.setShareType("wechat").setType(1).setId(mAskId)
						.setListener(shareFriendsListener)
						.setErrorListener(errorListener);

				ActionShareRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						mContext).getRequestQueue();
				requestQueue.add(request);
			}
		});

		// 分享到朋友圈
		shareToMomentsBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.showProgressDialog(mContext);

				ActionShareRequest.Builder builder = new ActionShareRequest.Builder()
						.setShareType("moments").setType(1).setId(mAskId)
						.setListener(shareMomentsListener)
						.setErrorListener(errorListener);

				ActionShareRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						PSGodApplication.getAppContext()).getRequestQueue();
				requestQueue.add(request);
			}
		});
		return view;
	}

	// 微信朋友圈邀请接口请求回调
	private Listener<JSONObject> shareMomentsListener = new Listener<JSONObject>() {
		@Override
		public void onResponse(JSONObject response) {
			Utils.hideProgressDialog();

			ShareSDK.initSDK(mContext);
			Platform wechat = ShareSDK
					.getPlatform(mContext, WechatMoments.NAME);
			wechat.setPlatformActionListener(new PlatformActionListener() {
				@Override
				public void onError(Platform arg0, int arg1, Throwable arg2) {
				}

				@Override
				public void onComplete(Platform arg0, int arg1,
						HashMap<String, Object> arg2) {
				}

				@Override
				public void onCancel(Platform arg0, int arg1) {
				}
			});

			try {
				if (response.getString("type").equals("image")) {
					ShareParams sp = new ShareParams();
					sp.setShareType(Platform.SHARE_IMAGE);
					sp.setText(response.getString("desc"));
					sp.setImageUrl(response.getString("image"));
					wechat.share(sp);
				}
				if (response.getString("type").equals("url")) {
					// 图文链接分享
					ShareParams sp = new ShareParams();
					sp.setShareType(Platform.SHARE_WEBPAGE);
					sp.setTitle(response.getString("title"));
					sp.setText(response.getString("desc"));
					sp.setImageUrl(response.getString("image"));
					sp.setUrl(response.getString("url"));
					wechat.share(sp);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	// 微信好友邀请请求回调
	private Listener<JSONObject> shareFriendsListener = new Listener<JSONObject>() {
		@Override
		public void onResponse(JSONObject response) {
			Utils.hideProgressDialog();

			ShareSDK.initSDK(mContext);
			Platform wechatFriends = ShareSDK
					.getPlatform(mContext, Wechat.NAME);
			wechatFriends
					.setPlatformActionListener(new PlatformActionListener() {
						@Override
						public void onError(Platform arg0, int arg1,
								Throwable arg2) {
						}

						@Override
						public void onComplete(Platform arg0, int arg1,
								HashMap<String, Object> arg2) {
						}

						@Override
						public void onCancel(Platform arg0, int arg1) {
						}
					});

			try {
				if (response.getString("type").equals("image")) {
					ShareParams sp = new ShareParams();
					sp.setShareType(Platform.SHARE_IMAGE);
					sp.setText(response.getString("desc"));
					sp.setImageUrl(response.getString("image"));
					wechatFriends.share(sp);
				}
				if (response.getString("type").equals("url")) {
					// 图文链接分享
					ShareParams sp = new ShareParams();
					sp.setShareType(Platform.SHARE_WEBPAGE);
					sp.setTitle(response.getString("title"));
					sp.setText(response.getString("desc"));
					sp.setImageUrl(response.getString("image"));
					sp.setUrl(response.getString("url"));
					wechatFriends.share(sp);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	/**
	 * TYPE_SHARE: 微信邀请 TYPE_PSGOD: 邀请大神 TYPE_FRIENDS: 邀请好友
	 * 
	 * @param groupPosition
	 * @return
	 */
	private int getGropuType(int groupPosition) {
		if (groupPosition == 0) {
			return TYPE_SHARE;
		} else {
			int groupCount = getGroupCount();
			if (groupCount == 2) {
				if ((mPsGodList != null) && (mPsGodList.size() != 0)) {
					return TYPE_PSGOD;
				} else {
					return TYPE_FRIENDS;
				}
			} else {
				return (groupPosition == 2) ? TYPE_FRIENDS : TYPE_PSGOD;
			}
		}
	}

	private static class ViewHolder {
		AvatarImageView avatarIv;
		Button inviteBtn;
		TextView nameTv;
		TextView followerCountTv;
		TextView askCountTv;
		TextView workCountTv;
	}
}
