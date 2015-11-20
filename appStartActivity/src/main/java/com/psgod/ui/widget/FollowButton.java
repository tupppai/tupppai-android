package com.psgod.ui.widget;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.User;
import com.psgod.network.request.ActionFollowRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;

/**
 * 关注和取消关注按钮
 * 
 * @author Rayal
 */
public class FollowButton extends Button {
	private final static String TAG = FollowButton.class.getSimpleName();

	public static final int TYPE_FOLLOW = 0;
	public static final int TYPE_UNFOLLOW = 1;
	// TYPE_FOLLOW_EACH_STR状态
	public static final int TYPE_FOLLOW_EACH = 2;

	public static final String TYPE_FOLLOW_STR = "已关注";
	public static final String TYPE_UNFOLLOW_STR = "+ 关注";
	public static final String TYPE_FOLLOW_EACH_STR = "互相关注";

	private Context mContext;
	// 状态和对应icon之间关系
	private Map<FollowState, FollowButtonAttribute> mBtnAttrs = new HashMap<FollowState, FollowButtonAttribute>();

	// 关注按钮状态
	private FollowState state = FollowState.UnFollow;
	// 对应User
	private User mUser;

	private OnFollowListener onFollowListener;

	private OnErrorListener onErrorListener;

	public void setOnFollowListener(OnFollowListener onFollowListener) {
		this.onFollowListener = onFollowListener;
	}

	public void setOnErrorListener(OnErrorListener onErrorListener) {
		this.onErrorListener = onErrorListener;
	}

	public static enum FollowState {
		// 未关注 关注状态 TYPE_FOLLOW_EACH_STR
		UnFollow, Following, FollowingEach;
	}

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			// 正在关注 包括TYPE_FOLLOW_EACH_STR
			if (state == FollowState.Following
					|| state == FollowState.FollowingEach) {
				FollowButton.this.setClickable(false);

				ActionFollowRequest.Builder builder = new ActionFollowRequest.Builder()
						.setType(TYPE_UNFOLLOW).setUid(mUser.getUid())
						.setErrorListener(errorListener)
						.setListener(actionFollowListener);

				ActionFollowRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						mContext).getRequestQueue();
				requestQueue.add(request);
			} else if (state == FollowState.UnFollow) {
				FollowButton.this.setClickable(false);

				ActionFollowRequest.Builder builder = new ActionFollowRequest.Builder()
						.setType(TYPE_FOLLOW).setUid(mUser.getUid())
						.setErrorListener(errorListener)
						.setListener(actionFollowListener);

				ActionFollowRequest request = builder.build();
				request.setTag(TAG);
				RequestQueue requestQueue = PSGodRequestQueue.getInstance(
						mContext).getRequestQueue();
				requestQueue.add(request);
			}

		}
	};

	// 关注 取消关注 listener
	private Listener<Boolean> actionFollowListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean response) {
			if (response == true) {
				if (state == FollowState.Following
						|| state == FollowState.FollowingEach) {
					state = FollowState.UnFollow;
					setFollowButtonState(state);
					Toast.makeText(mContext, "取消关注成功", Toast.LENGTH_SHORT)
							.show();
				} else if (state == FollowState.UnFollow) {
					if (mUser.isFollowed() == 1) {
						state = FollowState.FollowingEach;
					} else {
						state = FollowState.Following;
					}
					setFollowButtonState(state);
					Toast.makeText(mContext, "关注成功", Toast.LENGTH_SHORT).show();
				}
				if (onFollowListener != null) {
					onFollowListener.onFollowListener(response, state.name());
				}

				FollowButton.this.setClickable(true);

				// 关注用户有变化 需要自动刷新我的关注页面
				Constants.IS_FOLLOW_NEW_USER = true;
			}
		}
	};

	public interface OnFollowListener {
		void onFollowListener(Boolean response, String state);
	}

	public interface OnErrorListener {
		void onErrorListener(VolleyError error);
	}

	private PSGodErrorListener errorListener = new PSGodErrorListener(
			FollowButton.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {
			// TODO Auto-generated method stub
			if (onErrorListener != null) {
				onErrorListener.onErrorListener(error);
			}
		}
	};

	// 设置对应的user
	public void setUser(User user) {
		this.mUser = user;

		// 关注
		if (mUser.isFollowing() == 1 && mUser.isFollowed() == 0) {
			state = FollowState.Following;
		} else if (mUser.isFollowing() == 0) {
			state = FollowState.UnFollow;
		} else if (mUser.isFollowed() == 1 && mUser.isFollowing() == 1) {
			// TYPE_FOLLOW_EACH_STR状态
			state = FollowState.FollowingEach;
		}
		this.setFollowButtonState(state);
	}

	public FollowButton(Context context) {
		super(context);
		mContext = context;
	}

	public FollowButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		Resources resources = context.getResources();

		int width = resources.getDimensionPixelSize(R.dimen.follow_btn_width);
		int height = resources.getDimensionPixelSize(R.dimen.follow_btn_height);
		this.setWidth(width);
		this.setHeight(height);

		FollowButtonAttribute unfollowAttr = new FollowButtonAttribute();
		unfollowAttr.srcDrawableId = R.drawable.btn_unfollow;
		unfollowAttr.state = TYPE_UNFOLLOW_STR;

		// follow 和 unfollow的命名弄反了╮(╯▽╰)╭
		FollowButtonAttribute followingAttr = new FollowButtonAttribute();
		followingAttr.srcDrawableId = R.drawable.btn_follow;
		followingAttr.state = TYPE_FOLLOW_STR;

		FollowButtonAttribute followeachAttr = new FollowButtonAttribute();
		followeachAttr.srcDrawableId = R.drawable.btn_follow;
		followeachAttr.state = TYPE_FOLLOW_EACH_STR;

		mBtnAttrs.put(FollowState.UnFollow, unfollowAttr);
		mBtnAttrs.put(FollowState.Following, followingAttr);
		mBtnAttrs.put(FollowState.FollowingEach, followeachAttr);

		this.setOnClickListener(mOnClickListener);
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void updateButton() {
		FollowButtonAttribute attr = mBtnAttrs.get(state);
		this.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
		if (android.os.Build.VERSION.SDK_INT >= 16) {
			this.setBackgroundResource(attr.srcDrawableId);
			this.setText(attr.state);
			if (attr.state.equals(TYPE_FOLLOW_EACH_STR)) {
				this.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);
			}
		} else {
			this.setBackgroundResource(attr.srcDrawableId);
			this.setText(attr.state);
			if (attr.state.equals(TYPE_FOLLOW_EACH_STR)) {
				this.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);
			}
		}

	}

	// 根据state设置followButton 的状态
	public void setFollowButtonState(FollowState state) {
		this.state = state;
		updateButton();
	}

	public FollowState getFollowState() {
		return this.state;
	}

	private static class FollowButtonAttribute {
		int srcDrawableId;
		String state;
	}
}