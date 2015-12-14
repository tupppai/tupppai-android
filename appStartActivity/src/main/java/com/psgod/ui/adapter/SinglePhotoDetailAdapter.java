package com.psgod.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.text.SpannableString;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.psgod.PsGodImageLoader;
import com.psgod.Constants;
import com.psgod.PSGodApplication;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.emoji.FaceConversionUtil;
import com.psgod.model.Comment;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.ActionCommentLikeRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.ui.activity.CommentListActivity;
import com.psgod.ui.view.SinglePhotoDetailView;
import com.psgod.ui.widget.AvatarImageView;

import java.util.List;

public class SinglePhotoDetailAdapter extends BaseExpandableListAdapter {
	private static final String TAG = SinglePhotoDetailAdapter.class
			.getSimpleName();
	private static final int TYPE_INVALID = -1;
	private static final int TYPE_PHOTO_ITEM = 0;
	private static final int TYPE_COMMENT = 1;
	private static final int TYPE_ALL_COMMENT = 2;

	private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS;
	private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

	private Context mContext;
	private PhotoItem mPhotoItem;
	private List<Comment> mCommentList;

	private String mCreateTimeStr = null;

//	private PhotoItemView mPhotoItemView;
	private View mHotCommentView;
	private View mCommentView;
	private View mEmptyView;
	private Handler mHandler;

	public SinglePhotoDetailAdapter(Context context, PhotoItem photoItem,
			List<Comment> commentList, Handler handler) {
		mContext = context;
		mPhotoItem = photoItem;
		mCommentList = commentList;
		mHandler = handler;
	}

	public void setPhotoItem(PhotoItem photoItem) {
		this.mPhotoItem = photoItem;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		int type = getGroupViewType(groupPosition);
		switch (type) {
		case TYPE_COMMENT:
			if ((childPosition >= 0) && (childPosition < mCommentList.size())) {
				return mCommentList.get(childPosition);
			}
		}
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		Object obj = getChild(groupPosition, childPosition);
		if (obj instanceof Comment) {
			return ((Comment) obj).getCid();
		} else {
			return -1;
		}
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		int groupType = getGroupViewType(groupPosition);
		if (groupType == TYPE_PHOTO_ITEM) {
			if (mEmptyView == null) {
				mEmptyView = LayoutInflater.from(mContext).inflate(
						R.layout.empty_comment_list_view, null);
			}
			Log.d("rayalyuan", "EmptyView");
			return mEmptyView;
		}
		// else if (groupType == TYPE_HOT_COMMENT) {
		// mCreateTimeStr = mHotCommentList.get(childPosition)
		// .getUpdateTimeStr();
		// }
		else if (groupType == TYPE_COMMENT) {
			mCreateTimeStr = mCommentList.get(childPosition).getUpdateTimeStr();
		}

		final ViewHolder viewHolder;
		if ((convertView == null) || (convertView.getTag() == null)) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_comment_list, null);
			viewHolder.mAvatarIv = (AvatarImageView) convertView
					.findViewById(R.id.item_comment_list_avatar_imgview);
			viewHolder.mNameTv = (TextView) convertView
					.findViewById(R.id.item_comment_list_name_tv);
			viewHolder.mLikeBtn = (ImageButton) convertView
					.findViewById(R.id.item_comment_list_like_btn);
			viewHolder.mLikeCountTv = (TextView) convertView
					.findViewById(R.id.item_comment_list_like_count_tv);
			viewHolder.mLikeArea = (RelativeLayout) convertView
					.findViewById(R.id.item_comment_list_like_area);
			viewHolder.mCommentTv = (TextView) convertView
					.findViewById(R.id.item_comment_list_comment_tv);
			viewHolder.mCommentTimeTv = (TextView) convertView
					.findViewById(R.id.comment_time);
			viewHolder.mReTv = (TextView) convertView.
					findViewById(R.id.item_comment_list_re_tv);
			viewHolder.mRenameTv = (TextView) convertView.
					findViewById(R.id.item_comment_list_rename_tv);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		Object child = getChild(groupPosition, childPosition);
		if (!(child instanceof Comment)) {
			return null;
		}

		final Comment comment = (Comment) child;

		// 更新图片
		PsGodImageLoader imageLoader = PsGodImageLoader.getInstance();
		imageLoader.displayImage(comment.getAvatarURL(), viewHolder.mAvatarIv,
				mAvatarOptions);

		viewHolder.mNameTv.setText(comment.getNickname());
		viewHolder.mCommentTimeTv.setText(mCreateTimeStr);
		viewHolder.mLikeCountTv.setText(String.valueOf(comment.getLikeCount()));
		viewHolder.mAvatarIv.setUserId(comment.getUid());
		updateLikeView(comment, viewHolder);

		// 若有嵌套评论情况
		if (comment.getReplyComments().size() > 0) {
//			SpannableStringBuilder sb = new SpannableStringBuilder();
//
//			String mComment = comment.getContent();
//			SpannableString ss = FaceConversionUtil.getInstace()
//					.getExpressionString(mContext, mComment);
//			sb.append(ss);
//
//			List<ReplyComment> mReplyComments = comment.getReplyComments();
//			for (int i = 0; i < mReplyComments.size(); i++) {
//				SpannableString spannableStr = new SpannableString("//");
//				SpannableString spannableString = new SpannableString("@"
//						+ mReplyComments.get(i).mNick + ":");
//				// spannableString.setSpan(new
//				// ForegroundColorSpan(Color.parseColor("#74C3FF")),
//				// 0, spannableString.length(),
//				// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//				SpannableString spannableContent = FaceConversionUtil
//						.getInstace().getExpressionString(mContext,
//								String.valueOf(mReplyComments.get(i).mContent));
//
//				sb.append(spannableStr);
//				sb.append(spannableString);
//				sb.append(spannableContent);
//			}
//
//			viewHolder.mCommentTv.setText(sb);
			viewHolder.mReTv.setVisibility(View.VISIBLE);
			viewHolder.mRenameTv.setVisibility(View.VISIBLE);
			viewHolder.mRenameTv.setText(comment.getReplyComments().get(0).mNick);
		} else {
			// 将emoji表情转化为icon显示 TODO
			viewHolder.mReTv.setVisibility(View.GONE);
			viewHolder.mRenameTv.setVisibility(View.GONE);
		}
		SpannableString spannableString = FaceConversionUtil.getInstace()
				.getExpressionString(mContext,
						String.valueOf(comment.getContent()));
		viewHolder.mCommentTv.setText(spannableString);

		// 评论点赞
		viewHolder.mLikeArea.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// 评论只能点赞 无法取消点赞
				if (!comment.getIsLiked()) {
					Resources res = PSGodApplication.getAppContext()
							.getResources();
					viewHolder.mLikeBtn.setImageDrawable(res
							.getDrawable(R.drawable.ic_like_red));
					viewHolder.mLikeCountTv.setTextColor(Color
							.parseColor("#FE8282"));
					comment.setLikeCount(comment.getLikeCount() + 1);
					viewHolder.mLikeCountTv.setText(String.valueOf(comment
							.getLikeCount()));
					viewHolder.mLikeCountTv.setCompoundDrawablesWithIntrinsicBounds(
							res.getDrawable(R.drawable.shape_comment_item_count_point_red),
							null, null, null);
					ActionCommentLikeRequest.Builder builder = new ActionCommentLikeRequest.Builder()
							.setCid(comment.getCid()).setStatus(1)
							.setListener(new Listener<Boolean>() {
								@Override
								public void onResponse(Boolean response) {
									if (response) {
										comment.setIsLiked(true);
									}
								}
							}).setErrorListener(errorListener);

					ActionCommentLikeRequest request = builder.build();
					request.setTag(TAG);
					RequestQueue requestQueue = PSGodRequestQueue.getInstance(
							PSGodApplication.getAppContext()).getRequestQueue();
					requestQueue.add(request);
				}
			}
		});

		return convertView;
	}

	private PSGodErrorListener errorListener = new PSGodErrorListener(
			SinglePhotoDetailAdapter.class.getSimpleName()) {
		@Override
		public void handleError(VolleyError error) {
			// TODO
		}
	};

	/**
	 * 根据点赞状态，更新视图
	 * 
	 * @param isLiked
	 * @param viewHolder
	 */
	private void updateLikeView(Comment comment, ViewHolder viewHolder) {
		Resources res = PSGodApplication.getAppContext().getResources();
		viewHolder.mLikeCountTv.setText(String.valueOf(comment.getLikeCount()));
		if (comment.getIsLiked()) {
			viewHolder.mLikeBtn.setImageDrawable(res
					.getDrawable(R.drawable.ic_like_red));
			viewHolder.mLikeCountTv.setTextColor(Color.parseColor("#FE8282"));
			viewHolder.mLikeCountTv
					.setCompoundDrawablesWithIntrinsicBounds(
							res.getDrawable(R.drawable.shape_comment_item_count_point_red),
							null, null, null);
		} else {
			viewHolder.mLikeBtn.setImageDrawable(res
					.getDrawable(R.drawable.ic_like));
			viewHolder.mLikeCountTv.setTextColor(Color.parseColor("#B2B2B2"));
			viewHolder.mLikeCountTv.setCompoundDrawablesWithIntrinsicBounds(
					res.getDrawable(R.drawable.shape_comment_item_count_point),
					null, null, null);
		}
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		int type = getGroupViewType(groupPosition);
		switch (type) {
		case TYPE_PHOTO_ITEM:
			if (mCommentList.size() == 0) {
				// 用于显示EmptyView
				return 1;
			} else {
				return 0;
			}
		case TYPE_COMMENT:
			return mCommentList.size() > 3 ? 3 : mCommentList.size();
		case TYPE_ALL_COMMENT:
			return 0;
		case TYPE_INVALID:
		default:
			return -1;
		}
	}

	@Override
	public Object getGroup(int groupPosition) {
		int type = getGroupViewType(groupPosition);
		switch (type) {
		case TYPE_COMMENT:
			return mCommentList;
		case TYPE_INVALID:
		default:
			return null;
		}
	}

	@Override
	public int getGroupCount() {
		int groupCount = 1;
		if ((mCommentList != null) && (mCommentList.size() != 0)) {
			++groupCount;
			++groupCount;
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
		int type = getGroupViewType(groupPosition);
		if (type == TYPE_INVALID) {
			return convertView;
		} else if (type == TYPE_COMMENT) {
			return new View(mContext);
		} else if (type == TYPE_PHOTO_ITEM) {
			return getPhotoItemView();
		} else if (type == TYPE_ALL_COMMENT) {
			return getAllCommentView();
		} else {
			return new View(mContext);
		}
	}

	private View getAllCommentView() {
		RelativeLayout layout = new RelativeLayout(mContext);
		layout.setBackgroundColor(Color.WHITE);
		TextView textView = new TextView(mContext);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.setMargins(Utils.dpToPx(mContext, 14), 0, 0, 0);
		textView.setLayoutParams(params);
		textView.setPadding(0, 0, 0, Utils.dpToPx(mContext, 30));
		textView.setGravity(Gravity.CENTER_VERTICAL);
		textView.setText("查看全部评论");
		textView.setTextColor(Color.BLACK);
		textView.setOnClickListener(allCommentClick);
		layout.addView(textView);
		return layout;
	}

	private OnClickListener allCommentClick = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent(mContext, CommentListActivity.class);
			intent.putExtra(Constants.IntentKey.PHOTO_ITEM, mPhotoItem);
			mContext.startActivity(intent);
		}
	};

	private SinglePhotoDetailView singlePhoto;

	public SinglePhotoDetailView getPhotoItemView() {
		if(singlePhoto == null){
			singlePhoto = new SinglePhotoDetailView(mContext,mPhotoItem);
		}
		return singlePhoto;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	/**
	 * TYPE_HOT_COMMENT: 热门评论 TYPE_COMMENT: 评论 TYPE_INVALID: 错误值
	 * 
	 * @param groupPosition
	 * @return
	 */
	private int getGroupViewType(int groupPosition) {
		int groupCount = getGroupCount();
		if (groupCount == 1) {
			return TYPE_PHOTO_ITEM;
		} else if (groupCount == 2) {
			if (groupPosition == 0) {
				return TYPE_PHOTO_ITEM;
			} else if (groupPosition == 1) {
				return TYPE_COMMENT;
			}
		} else if (groupCount == 3) {
			if (groupPosition == 0) {
				return TYPE_PHOTO_ITEM;
			} else if (groupPosition == 1) {
				return TYPE_COMMENT;
			} else if (groupPosition == 2) {
				return TYPE_ALL_COMMENT;
			}
		}
		return TYPE_INVALID;
	}

	private static class ViewHolder {
		AvatarImageView mAvatarIv;
		TextView mNameTv;
		TextView mReTv;
		TextView mRenameTv;
		ImageButton mLikeBtn;
		TextView mLikeCountTv;
		// 点赞区域
		RelativeLayout mLikeArea;
		TextView mCommentTv;
		TextView mCommentTimeTv;
	}
}
