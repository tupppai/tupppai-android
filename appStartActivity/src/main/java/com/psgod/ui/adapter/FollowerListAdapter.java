package com.psgod.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.psgod.PsGodImageLoader;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.model.LoginUser;
import com.psgod.model.User;
import com.psgod.ui.activity.UserProfileActivity;
import com.psgod.ui.widget.AvatarImageView;
import com.psgod.ui.widget.FollowImage;

import java.util.List;

public class FollowerListAdapter extends BaseAdapter {
	private Context mContext;
	private List<User> mUsers;

	// UIL配置
	private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS;
	private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

	public FollowerListAdapter(Context context, List<User> users) {
		mContext = context;
		mUsers = users;
	}

	@Override
	public int getCount() {
		return mUsers.size();
	}

	@Override
	public Object getItem(int position) {
		if ((position < 0) || (position >= mUsers.size())) {
			return null;
		} else {
			return mUsers.get(position);
		}
	}

	@Override
	public long getItemId(int position) {
		Object obj = getItem(position);
		if (obj instanceof User) {
			User photoItem = (User) obj;
			return photoItem.getUid();
		}
		return -1;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_user_list, null);
			viewHolder = new ViewHolder();
			viewHolder.avatarIv = (AvatarImageView) convertView
					.findViewById(R.id.item_user_list_avatar_imgview);
			viewHolder.nameTv = (TextView) convertView
					.findViewById(R.id.item_user_list_name_textview);
			viewHolder.followBtn = (FollowImage) convertView
					.findViewById(R.id.item_user_list_follow_btn);
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

		// 头像设置点击跳转
		viewHolder.avatarIv.setUser(mUsers.get(position));

		// 点击昵称跳转
		viewHolder.nameTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, UserProfileActivity.class);
				intent.putExtra(Constants.IntentKey.USER_ID,
						((User) getItem(position)).getUid());
				mContext.startActivity(intent);
			}
		});

		// 设置followBtn对应的User
		viewHolder.followBtn.setUser((User)getItem(position));

		// 如果是自己，不显示关系按钮
		if (LoginUser.getInstance().getUid() == ((User) getItem(position)).getUid()) {
			viewHolder.followBtn.setVisibility(View.GONE);
		} else {
			viewHolder.followBtn.setVisibility(View.VISIBLE);
		}

		PsGodImageLoader imageLoader = PsGodImageLoader.getInstance();
		imageLoader.displayImage(
				((User) getItem(position)).getAvatarImageUrl(),
				viewHolder.avatarIv, mAvatarOptions);

		viewHolder.nameTv.setText(((User) getItem(position)).getNickname());
		viewHolder.followerCountTv
				.setText(Integer.toString(((User) getItem(position))
						.getFollowerCount()) + "粉丝");
		viewHolder.askCountTv.setText(Integer
				.toString(((User) getItem(position)).getAskCount()) + "求P");
		viewHolder.workCountTv.setText(Integer
				.toString(((User) getItem(position)).getReplyCount()) + "作品");

		return convertView;
	}

	private static class ViewHolder {
		AvatarImageView avatarIv;
		FollowImage followBtn;
		TextView nameTv;
		TextView followerCountTv;
		TextView askCountTv;
		TextView workCountTv;
	}
}
