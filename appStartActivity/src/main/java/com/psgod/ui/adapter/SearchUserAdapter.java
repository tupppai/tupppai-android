package com.psgod.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.model.SearchUserData;
import com.psgod.model.SearchUserReplies;
import com.psgod.network.request.ActionCollectionRequest;
import com.psgod.network.request.ActionFollowRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.ui.activity.UserProfileActivity;
import com.psgod.ui.widget.AvatarImageView;
import com.psgod.ui.widget.dialog.CarouselPhotoDetailDialog;

import java.util.List;

public class SearchUserAdapter extends MyBaseAdapter<SearchUserData> {

	public static final int FOLLOWED = 1;
	public static final int UNFOLLOWED = 0;

	public SearchUserAdapter(Context context, List<SearchUserData> searchUsers) {
		super(context, searchUsers);
	}

	private static ViewHolder viewHolder;

	@Override
	View initView(int position, View view, ViewGroup parent) {
		if (view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(context).inflate(
					R.layout.item_search_user, parent, false);
			viewHolder.mAvatar = (AvatarImageView) view
					.findViewById(R.id.item_search_user_avatar_img);
			viewHolder.mName = (TextView) view
					.findViewById(R.id.item_search_user_name_txt);
			viewHolder.mWork = (TextView) view
					.findViewById(R.id.item_search_user_work_txt);
			viewHolder.mFollow = (TextView) view
					.findViewById(R.id.item_search_user_follow_txt);
			viewHolder.mFollowing = (TextView) view
					.findViewById(R.id.item_search_user_following_txt);
			viewHolder.mFollowed = (Button) view
					.findViewById(R.id.item_search_user_follow_img);
			viewHolder.mGrid = (GridView) view
					.findViewById(R.id.item_search_user_grid);
			viewHolder.mClick = (RelativeLayout) view
					.findViewById(R.id.item_search_user_click_layout);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		SearchUserData data = list.get(position);
		ImageLoader.getInstance().displayImage(data.getAvatar(),
				viewHolder.mAvatar, Constants.DISPLAY_IMAGE_OPTIONS_AVATAR);
		viewHolder.mAvatar.setTag(data.getUid());
		viewHolder.mAvatar.setUserId(Long.parseLong(data.getUid()));
		updateFollowView(viewHolder.mFollowed, data.getIs_follow(),
				data.getStatus());
		viewHolder.mWork.setText(data.getReply_count() + "作品");
		viewHolder.mFollow.setText(data.getFans_count() + "粉丝");
		viewHolder.mFollowing.setText(data.getFellow_count() + "关注");
		viewHolder.mFollowed.setTag(data);
		viewHolder.mFollowed.setOnClickListener(followedClick);
		viewHolder.mName.setText(data.getNickname());
		viewHolder.mClick.setTag(data.getUid());
		viewHolder.mClick.setOnClickListener(avatarClick);
		SearchUserGridAdapter adapter = new SearchUserGridAdapter(context,
				data.getReplies());
		viewHolder.mGrid.setAdapter(adapter);
		return view;
	}

	private OnClickListener avatarClick = new OnClickListener() {

		@Override
		public void onClick(View view) {
			Intent intent = new Intent(context, UserProfileActivity.class);
			intent.putExtra(Constants.IntentKey.USER_ID,
					Long.parseLong(view.getTag().toString()));
			context.startActivity(intent);
		}
	};

	private OnClickListener followedClick = new OnClickListener() {

		@Override
		public void onClick(final View view) {
			final SearchUserData data = (SearchUserData) view.getTag();
			final int uid = Integer.parseInt(data.getUid());
			final int follow = data.getIs_follow();
			final int status = data.getStatus();
			view.setClickable(false);
			final int mType = follow == FOLLOWED ? FOLLOWED : UNFOLLOWED;
			ActionFollowRequest.Builder builder = new ActionFollowRequest.Builder()
					.setType(mType)
					.setUid(uid)
					.setErrorListener(
							new PSGodErrorListener(
									ActionCollectionRequest.class
											.getSimpleName()) {
								@Override
								public void handleError(VolleyError error) {
									view.setClickable(true);
								}
							}).setListener(new Listener<Boolean>() {
						@Override
						public void onResponse(Boolean response) {

							if (response) {
								if (mType == 1) {
									updateFollowView(view, UNFOLLOWED, status);
									view.setTag(data);
								} else {
									updateFollowView(view, FOLLOWED, status);
									view.setTag(data);
								}
							} else {

							}
							view.setClickable(true);
						}
					});

			ActionFollowRequest request = builder.build();
			RequestQueue requestQueue = PSGodRequestQueue.getInstance(context)
					.getRequestQueue();
			requestQueue.add(request);

		}
	};

	public void updateFollowView(View view, int follow, int status) {
		if (follow == 1) {
			((Button) view).setBackgroundResource(R.drawable.btn_follow);
			// if (status != 1) {
			// ((Button) view).setText("互相关注");
			// } else {
			((Button) view).setText("已关注");
			// }
		} else {
			((Button) view).setBackgroundResource(R.drawable.btn_unfollow);
			((Button) view).setText("+ 关注");
		}
	}

	private class ViewHolder {
		AvatarImageView mAvatar;
		TextView mName;
		TextView mWork;
		TextView mFollow;
		TextView mFollowing;
		Button mFollowed;
		RelativeLayout mClick;
		GridView mGrid;
	}

	private class SearchUserGridAdapter extends
			MyBaseAdapter<SearchUserReplies> {

		public SearchUserGridAdapter(Context context,
				List<SearchUserReplies> list) {
			super(context, list);
		}

		@Override
		public int getCount() {
			return list.size() > 4 ? 4 : list.size();
		}

		@Override
		View initView(int position, View view, ViewGroup parent) {
			if (view == null) {
				holder = new ViewHolder();
				view = LayoutInflater.from(context).inflate(
						R.layout.item_search_user_grid, parent, false);
				holder.mImage = (ImageView) view
						.findViewById(R.id.item_search_user_grid_img);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}
			SearchUserReplies replies = list.get(position);
			holder.mImage.setTag(replies.getImage_url());
			ImageLoader.getInstance().displayImage(replies.getImage_url(),
					holder.mImage, Constants.DISPLAY_IMAGE_OPTIONS);
			holder.mImage.setTag(replies.getAsk_id() + "tupai"
					+ replies.getId());
			holder.mImage.setOnClickListener(gridImgClick);

			return view;
		}

		ViewHolder holder;

		class ViewHolder {
			ImageView mImage;
		}

		OnClickListener gridImgClick = new OnClickListener() {

			@Override
			public void onClick(View view) {
				String[] tags = view.getTag().toString().split("tupai");
				Long aid = Long.parseLong(tags[0].toString());
				Long id = Long.parseLong(tags[1].toString());
//				CarouselPhotoDetailActivity.startActivity(context, aid, id);
				new CarouselPhotoDetailDialog(context,aid,id).show();
			}
		};

	}

}
