package com.psgod.ui.adapter;

import android.content.Context;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.emoji.FaceConversionUtil;
import com.psgod.model.Comment;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.ui.widget.AvatarImageView;

import java.util.List;

public class CommentExpandableListAdapter extends BaseExpandableListAdapter {
    private static final String TAG = CommentExpandableListAdapter.class
            .getSimpleName();
    private static final int TYPE_INVALID = -1;
    private static final int TYPE_HOT_COMMENT = 0;
    private static final int TYPE_COMMENT = 1;

    private int selectItem = -1;

    public void setSelectItem(int selectItem) {
        this.selectItem = selectItem;
        this.notifyDataSetChanged();
    }

    private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

    private Context mContext;
    private List<Comment> mHotCommentList;
    private List<Comment> mCommentList;

    private RelativeLayout mEmptyView;

    public CommentExpandableListAdapter(Context context,
                                        List<Comment> hotCommentList, List<Comment> commentList) {
        mContext = context;
        mHotCommentList = hotCommentList;
        mCommentList = commentList;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        int type = getGropuType(groupPosition);
        switch (type) {
            case TYPE_HOT_COMMENT:
                if ((childPosition >= 0)
                        && (childPosition < mHotCommentList.size())) {
                    return mHotCommentList.get(childPosition);
                }
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
        Object child = getChild(groupPosition, childPosition);
        if (!(child instanceof Comment)) {
            return null;
        }

        final Comment comment = (Comment) child;
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.item_all_comment_list, null);
            viewHolder.mAvatarIv = (AvatarImageView) convertView
                    .findViewById(R.id.item_comment_list_avatar_imgview);
            viewHolder.mNameTv = (TextView) convertView
                    .findViewById(R.id.item_comment_list_name_tv);
            viewHolder.mTimeTv = (TextView) convertView
                    .findViewById(R.id.comment_time);
            viewHolder.mCommentTv = (TextView) convertView
                    .findViewById(R.id.item_comment_list_comment_tv);
            viewHolder.mReNameTv = (TextView) convertView
                    .findViewById(R.id.item_comment_list_rename_tv);
            viewHolder.mReTv = (TextView) convertView.
                    findViewById(R.id.item_comment_list_re_tv);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (childPosition == selectItem) {
            convertView.setBackgroundColor(mContext.getResources().getColor(
                    R.color.color_f6f6f6));
        } else {
            convertView.setBackgroundColor(mContext.getResources().getColor(
                    R.color.white));
        }

        // 更新图片
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(comment.getAvatarURL(), viewHolder.mAvatarIv,
                mAvatarOptions);
        viewHolder.mNameTv.setText(comment.getNickname());
        // 设置用户头像点击ID
        viewHolder.mAvatarIv.setUserId(comment.getUid());
        viewHolder.mTimeTv.setText(comment.getUpdateTimeStr());

        // 若有嵌套评论情况
//        if (comment.getReplyComments().size() > 0) {
//            SpannableStringBuilder sb = new SpannableStringBuilder();
//
//            String mComment = comment.getContent();
//            SpannableString ss = FaceConversionUtil.getInstace()
//                    .getExpressionString(mContext, mComment);
//            sb.append(ss);

//            List<ReplyComment> mReplyComments = comment.getReplyComments();
//            for (int i = 0; i < mReplyComments.size(); i++) {
//                SpannableString spannableStr = new SpannableString("//");
//                SpannableString spannableString = new SpannableString("@"
//                        + mReplyComments.get(i).mNick + ":");
//
//                SpannableString spannableContent = FaceConversionUtil
//                        .getInstace().getExpressionString(mContext,
//                                String.valueOf(mReplyComments.get(i).mContent));
//
//                sb.append(spannableStr);
//                sb.append(spannableString);
//                sb.append(spannableContent);
//            }

//            viewHolder.mCommentTv.setText(ss);
//        } else {
        // 将emoji表情转化为icon显示 TODO
        SpannableString spannableString = FaceConversionUtil.getInstace()
                .getExpressionString(mContext,
                        String.valueOf(comment.getContent()));
        viewHolder.mCommentTv.setText(spannableString);
//        }
        if (comment.getReplyComments().size() > 0){
            viewHolder.mReTv.setVisibility(View.VISIBLE);
            viewHolder.mReNameTv.setVisibility(View.VISIBLE);
            viewHolder.mReNameTv.setText(comment.getReplyComments().get(0).mNick);
        }else{
            viewHolder.mReTv.setVisibility(View.GONE);
            viewHolder.mReNameTv.setVisibility(View.GONE);
        }


            return convertView;
    }

    private PSGodErrorListener errorListener = new PSGodErrorListener(
            CommentExpandableListAdapter.class.getSimpleName()) {
        @Override
        public void handleError(VolleyError error) {
        }
    };

    @Override
    public int getChildrenCount(int groupPosition) {
        int type = getGropuType(groupPosition);
        switch (type) {
            case TYPE_HOT_COMMENT:
                return mHotCommentList.size();
            case TYPE_COMMENT:
                return mCommentList.size();
            case TYPE_INVALID:
            default:
                return -1;
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        int type = getGropuType(groupPosition);
        switch (type) {
            case TYPE_HOT_COMMENT:
                return mHotCommentList;
            case TYPE_COMMENT:
                return mCommentList;
            case TYPE_INVALID:
            default:
                return null;
        }
    }

    @Override
    public int getGroupCount() {
        int groupCount = 0;
        if (mHotCommentList != null) {
            if (mHotCommentList.size() != 0) {
                ++groupCount;
            }
        }

        if (mCommentList != null) {
            if (mCommentList.size() != 0) {
                ++groupCount;
            }
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
        if (type == TYPE_INVALID) {
            return convertView;
        } else if (type == TYPE_HOT_COMMENT) {
            // 这里没有用ViewHolder优化
            // 主要考虑到目前只有两个分组，没有太大必要优化
            // convertView = LayoutInflater.from(mContext).inflate(
            // R.layout.header_expandablelistview, null);
            // ((TextView) convertView
            // .findViewById(R.id.header_expandablelistview_tv))
            // .setText("热门评论");
            mEmptyView = new RelativeLayout(mContext);
            return mEmptyView;
        } else {
            mEmptyView = new RelativeLayout(mContext);
            // convertView = LayoutInflater.from(mContext).inflate(
            // R.layout.header_expandablelistview, null);
            // ((TextView) convertView
            // .findViewById(R.id.header_expandablelistview_tv))
            // .setText("最新评论");
            return mEmptyView;
        }
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
    private int getGropuType(int groupPosition) {
        int groupCount = getGroupCount();
        if (groupCount == 1) {
            return TYPE_COMMENT;
        } else if (groupCount == 2) {
            if (groupPosition == 0) {
                return TYPE_HOT_COMMENT;
            } else if (groupPosition == 1) {
                return TYPE_COMMENT;
            }
        }
        return TYPE_INVALID;
    }

    private static class ViewHolder {
        AvatarImageView mAvatarIv;
        TextView mNameTv;
        TextView mTimeTv;
        TextView mCommentTv;
        TextView mReNameTv;
        TextView mReTv;
    }
}
