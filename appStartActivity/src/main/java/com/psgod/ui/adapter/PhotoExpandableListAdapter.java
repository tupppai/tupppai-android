package com.psgod.ui.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.model.Comment;
import com.psgod.model.PhotoItem;
import com.psgod.ui.view.PhotoItemView;
import com.psgod.ui.view.PhotoItemView.PhotoListType;

/**
 * 带评论列表的PhotoListAdapter 主要用于热门的详情页
 * 
 * @author rayalyuan
 * 
 */
public class PhotoExpandableListAdapter extends BaseExpandableListAdapter {
	private static final String TAG = PhotoExpandableListAdapter.class
			.getSimpleName();

	private Context mContext;
	private PhotoListType mPhotoListType;
	private List<PhotoItem> mPhotoItems;
	private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS;

	public PhotoExpandableListAdapter(Context context,
			PhotoListType photoListType, List<PhotoItem> photoItems) {

		// if ((photoListType != PhotoListType.FOLLOW) && (photoListType !=
		// PhotoListType.DETAIL)) {
		// throw new
		// IllegalArgumentException("The PhotoListType of PhotoExpandableListAdapter must be FOLLOW or DETAIL");
		// }

		mContext = context;
		mPhotoListType = photoListType;
		mPhotoItems = photoItems;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		Object group = getGroup(groupPosition);
		if (!(group instanceof PhotoItem)) {
			return null;
		}

		PhotoItem photoItem = (PhotoItem) group;
		List<Comment> comments = photoItem.getHotCommentList();
		if ((childPosition < 0) || (childPosition >= comments.size())) {
			return null;
		} else {
			return comments.get(childPosition);
		}
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
		ViewHolder viewHolder;
		if (convertView != null) {
			viewHolder = (ViewHolder) convertView.getTag();
		} else {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_hotcomment_list, null);
			viewHolder.nameTv = (TextView) convertView
					.findViewById(R.id.item_hotcomment_list_name_tv);
			viewHolder.contentTv = (TextView) convertView
					.findViewById(R.id.item_hotcomment_list_content_tv);
		}

		Comment comment = (Comment) getChild(groupPosition, childPosition);
		viewHolder.nameTv.setText(comment.getNickname());
		viewHolder.contentTv.setText(comment.getContent());
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		Object group = getGroup(groupPosition);
		if (!(group instanceof PhotoItem)) {
			return 0;
		}
		PhotoItem photoItem = (PhotoItem) group;
		return photoItem.getHotCommentList().size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		int groupCount = getGroupCount();
		if ((groupPosition < 0) || (groupPosition >= groupCount)) {
			return null;
		} else {
			return mPhotoItems.get(groupPosition);
		}
	}

	@Override
	public int getGroupCount() {
		if (mPhotoItems != null) {
			return mPhotoItems.size();
		} else {
			return 0;
		}
	}

	@Override
	public long getGroupId(int groupPosition) {
		Object obj = getGroup(groupPosition);
		if (obj instanceof PhotoItem) {
			return ((PhotoItem) obj).getPid();
		} else {
			return -1;
		}
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		PhotoItemView photoItemView;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.widget_photo_item, null);
			photoItemView = (PhotoItemView) convertView;
			photoItemView.initialize(mPhotoListType);
		} else {
			photoItemView = (PhotoItemView) convertView;
			// photoItemView.setViewByPhotoListType(mPhotoListType);
		}

		Object obj = getGroup(groupPosition);
		if (obj instanceof PhotoItem) {
			PhotoItem photoItem = (PhotoItem) obj;
			photoItemView.setPhotoItem(photoItem);
			if (photoItem.getType() == PhotoItem.TYPE_ASK) {
				photoItemView
						.setViewByPhotoListType(PhotoListType.HOT_FOCUS_ASK);
			} else {
				photoItemView
						.setViewByPhotoListType(PhotoListType.HOT_FOCUS_REPLY);
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
		return false;
	}

	// private static class ViewHolder {
	// CircleImageView mAvatarIv;
	// CircleImageView mGenderIv;
	// TextView mNameTv;
	// ToggleButton mLikeBtn;
	// TextView mLikeCountTv;
	// TextView mCommentTv;
	// }

	private static class ViewHolder {
		TextView nameTv;
		TextView contentTv;
	}
}
