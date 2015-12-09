package com.psgod.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;

import com.psgod.Constants;
import com.psgod.ui.activity.UserProfileActivity;
import com.psgod.ui.view.CircleImageView;

public class AvatarImageView extends CircleImageView {
	private Context mContext;
	private Long mUserId;

	public AvatarImageView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public AvatarImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		mContext = context;
		init();
	}

	public AvatarImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}

	public void init() {
		this.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mUserId != null) {
					Intent intent = new Intent(mContext,
							UserProfileActivity.class);
					intent.putExtra(Constants.IntentKey.USER_ID, mUserId);
					mContext.startActivity(intent);
				}
			}
		});
	}

	public void setUserId(Long userId) {
		mUserId = userId;
	}
}
