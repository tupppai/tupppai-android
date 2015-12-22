package com.psgod.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.psgod.PsGodImageLoader;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.model.User;
import com.psgod.ui.activity.RecommendFocusActivity;
import com.psgod.ui.activity.UserProfileActivity;
import com.psgod.ui.widget.AvatarImageView;
import com.psgod.ui.widget.FollowImage;

import java.util.List;

public class FollowingExpandableListAdapter extends BaseExpandableListAdapter {
	private static final String TAG = FollowingExpandableListAdapter.class
			.getSimpleName();

	private static final int TYPE_RECOMMEND_USER = 0;
	private static final int TYPE_RECOMMEND_MORE = 1;
	private static final int TYPE_COMMON_USER = 2;

	private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS;
	private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

	private Context mContext;
	private List<User> mRecommendUserList;
	private List<User> mMyUserList;
	private int mTotalMasters;

	private View mRecommendView;
	private View mRecommendLayout;

	public FollowingExpandableListAdapter(Context context,
			List<User> recommendUsers, List<User> commonUsers, int mTotalMasters) {
		mContext = context;
		mRecommendUserList = recommendUsers;
		mMyUserList = commonUsers;
		this.mTotalMasters = mTotalMasters;
		mRecommendView = LayoutInflater.from(mContext).inflate(
				R.layout.footer_load_recommend, null);
		mRecommendLayout = mRecommendView
				.findViewById(R.id.recommend_layout);
		mRecommendView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext,
						RecommendFocusActivity.class);
				mContext.startActivity(intent);
			}
		});
	}

	public void notifyDataSetChanged(int mTotalMasters) {
		this.mTotalMasters = mTotalMasters;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		int type = GetGroupType(groupPosition);
		switch (type) {
		case TYPE_RECOMMEND_USER:
			if ((childPosition >= 0)
					&& (childPosition < mRecommendUserList.size())) {
				return mRecommendUserList.get(childPosition);
			}
		case TYPE_COMMON_USER:
			if ((childPosition >= 0) && (childPosition < mMyUserList.size())) {
				return mMyUserList.get(childPosition);
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

	@Override
	public View getChildView(final int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		Object child = getChild(groupPosition, childPosition);
		if (!(child instanceof User)) {
			return null;
		}

		User user = (User) child;

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
		viewHolder.avatarIv.setUserId(((User) getChild(groupPosition,
				childPosition)).getUid());

		// 点击昵称跳转
		viewHolder.nameTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, UserProfileActivity.class);
				intent.putExtra(Constants.IntentKey.USER_ID,
						((User) getChild(groupPosition, childPosition))
								.getUid());
				mContext.startActivity(intent);
				;
			}
		});

		// 设置关注btn对应的user
		viewHolder.followBtn.setUser(user.getUid(), user.isFollowing(), user.isFollowed());

		// 更新图片
		PsGodImageLoader imageLoader = PsGodImageLoader.getInstance();
		imageLoader.displayImage(user.getAvatarImageUrl(), viewHolder.avatarIv,
				mAvatarOptions);

		viewHolder.nameTv.setText(user.getNickname());
		viewHolder.followerCountTv.setText(Integer.toString(user
				.getFollowerCount()) + "粉丝");
		viewHolder.askCountTv.setText(Integer.toString(user.getAskCount())
				+ "求P");
		viewHolder.workCountTv.setText(Integer.toString(user.getReplyCount())
				+ "作品");

		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		int type = GetGroupType(groupPosition);
		switch (type) {
		case TYPE_RECOMMEND_USER:
			return mRecommendUserList.size();
		case TYPE_COMMON_USER:
			return mMyUserList.size();
		case TYPE_RECOMMEND_MORE:
			return 0;
		default:
			return -1;
		}
	}

	@Override
	public Object getGroup(int groupPosition) {
		int type = GetGroupType(groupPosition);
		switch (type) {
		case TYPE_RECOMMEND_USER:
			return mRecommendUserList;
		case TYPE_COMMON_USER:
			return mMyUserList;
		default:
			return null;
		}
	}

	@Override
	public int getGroupCount() {
		int groupCount = 1;
		if (mRecommendUserList != null) {
			if (mRecommendUserList.size() != 0) {
				++groupCount;
			}
		}

		if (mMyUserList != null) {
			if (mMyUserList.size() != 0) {
				++groupCount;
			}
		}

		return groupCount;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		int type = GetGroupType(groupPosition);
		if (type == TYPE_RECOMMEND_MORE) {
			if (this.mTotalMasters <= mRecommendUserList.size()) {
				mRecommendLayout.setVisibility(View.GONE);
			} else {
				mRecommendLayout.setVisibility(View.VISIBLE);
			}
			return mRecommendView;
		} else {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.header_recommend_following_list, null);
			TextView mGroupHearderTextView = (TextView) convertView
					.findViewById(R.id.header_expandablelistview_red_tv);
			if (type == TYPE_RECOMMEND_MORE) {
				mGroupHearderTextView.setText("推荐关注");
			} else if (type == TYPE_COMMON_USER) {
				mGroupHearderTextView.setText("我的关注");
			}
		}

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

	private int GetGroupType(int groupPosition) {
		int groupCount = getGroupCount();
		if (groupCount == 1) {
			return TYPE_RECOMMEND_MORE;
		} else if ((groupCount == 2) && (mRecommendUserList.size() != 0)) {
			if (groupPosition == 0) {
				return TYPE_RECOMMEND_USER;
			} else {
				return TYPE_RECOMMEND_MORE;
			}
		} else if ((groupCount == 2) && (mMyUserList.size() != 0)) {
			if (groupPosition == 0) {
				return TYPE_RECOMMEND_MORE;
			} else {
				return TYPE_COMMON_USER;
			}
		} else if (groupCount == 3) {
			if (groupPosition == 0) {
				return TYPE_RECOMMEND_USER;
			} else if (groupPosition == 1) {
				return TYPE_RECOMMEND_MORE;
			} else {
				return TYPE_COMMON_USER;
			}
		}
		return TYPE_COMMON_USER;
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
