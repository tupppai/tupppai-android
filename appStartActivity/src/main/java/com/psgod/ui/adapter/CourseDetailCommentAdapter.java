package com.psgod.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.psgod.Constants;
import com.psgod.PsGodImageLoader;
import com.psgod.R;
import com.psgod.model.Comment;
import com.psgod.model.PhotoItem;
import com.psgod.model.User;
import com.psgod.ui.widget.AvatarImageView;

import java.util.List;

/**
 * Created by pires on 16/1/19.
 */
public class CourseDetailCommentAdapter extends BaseAdapter {
    private static final String TAG = CourseDetailCommentAdapter.class.getSimpleName();
    private Context mContext;
    private List<Comment> mComments;

    public CourseDetailCommentAdapter(Context context, List<Comment> comments) {
        mContext = context;
        mComments = comments;
    }

    @Override
    public int getCount() {
        return mComments.size();
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
        return position;
    }

    private static ViewHolder viewHolder ;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.
                    from(mContext).inflate(R.layout.item_course_detail_comment, null);
            viewHolder = new ViewHolder();
            viewHolder.avatar = (AvatarImageView) convertView.findViewById(R.id.avatar_iamge);
            viewHolder.nickname = (TextView) convertView.findViewById(R.id.nickname_text);
            viewHolder.time = (TextView) convertView.findViewById(R.id.time_text);
            viewHolder.desc = (TextView) convertView.findViewById(R.id.comment_content_tv);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Comment comment = mComments.get(position);
        PsGodImageLoader.getInstance().displayImage(comment.getAvatarURL(),viewHolder.avatar,
                Constants.DISPLAY_IMAGE_OPTIONS_AVATAR);
        viewHolder.avatar.setUser(new User(comment));
        viewHolder.nickname.setText(comment.getNickname());
        viewHolder.desc.setText(comment.getContent());
        viewHolder.time.setText(comment.getUpdateTimeStr());

        return convertView;
    }

    private static class ViewHolder {
        AvatarImageView avatar;
        TextView nickname;
        TextView time;
        TextView desc;
    }

}
