package com.pires.wesee.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pires.wesee.Constants;
import com.pires.wesee.emoji.FaceConversionUtil;
import com.pires.wesee.eventbus.CommentEvent;
import com.pires.wesee.model.Comment;
import com.pires.wesee.model.PhotoItem;
import com.pires.wesee.ui.activity.UserProfileActivity;
import com.pires.wesee.R;

import java.util.List;

import de.greenrobot.event.EventBus;

public class HotCommentListAdapter extends BaseAdapter {
	private Context mContext;
	private List<Comment> mComments;
	private PhotoItem photoItem;

	public HotCommentListAdapter(Context context, List<Comment> comments) {
		mContext = context;
		mComments = comments;
	}

	@Override
	public int getCount() {
		return mComments.size();
	}

	public void setPhotoItem(PhotoItem photoItem) {
		this.photoItem = photoItem;
	}

	@Override
	public Object getItem(int position) {
		if ((position < 0) || (position >= mComments.size())) {
			return null;
		} else {
			return mComments.get(position);
		}
	}

	@Override
	public long getItemId(int position) {
		Object obj = getItem(position);
		if (obj instanceof PhotoItem) {
			Comment comment = (Comment) obj;
			return comment.getCid();
		}
		return -1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_hotcomment_list, null);
			viewHolder = new ViewHolder();
			viewHolder.mNameTv = (TextView) convertView
					.findViewById(R.id.item_hotcomment_list_name_tv);
			viewHolder.mContentTv = (TextView) convertView
					.findViewById(R.id.item_hotcomment_list_content_tv);
			viewHolder.mAiteTv = (TextView) convertView
					.findViewById(R.id.item_hotcomment_list_aite_name);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		Comment comment = (Comment) this.getItem(position);
		viewHolder.mNameTv.setText(comment.getNickname() + ": ");
		viewHolder.mNameTv.setTag(comment.getUid());
		viewHolder.mNameTv.setOnClickListener(nameClick);
		viewHolder.mContentTv.setText(FaceConversionUtil.getInstace()
				.getExpressionString(mContext,
						String.valueOf(comment.getContent())));
		viewHolder.mContentTv.setTag(comment);
		viewHolder.mContentTv.setOnClickListener(commentClick);
		return convertView;
	}

	private OnClickListener commentClick = new OnClickListener() {
		@Override
		public void onClick(View view) {
			Comment comment = (Comment) view.getTag();
			// Intent intent = new Intent(mContext, CommentListActivity.class);
			// intent.putExtra("cid", comment.getCid());
			// mContext.startActivity(intent);
			CommentEvent event = new CommentEvent();
			event.comment = comment;
			event.photoItem = photoItem;
			EventBus.getDefault().post(event);
		}
	};

	private OnClickListener nameClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Long mUserId = Long.parseLong(v.getTag().toString());
			if (mUserId != null) {
				Intent intent = new Intent(mContext, UserProfileActivity.class);
				intent.putExtra(Constants.IntentKey.USER_ID, mUserId);
				mContext.startActivity(intent);
			}
		}
	};

	private static class ViewHolder {
		TextView mNameTv;
		TextView mContentTv;
		TextView mAiteTv;
	}
}
