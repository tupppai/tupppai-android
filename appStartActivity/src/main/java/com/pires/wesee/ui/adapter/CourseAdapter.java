package com.pires.wesee.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pires.wesee.PsGodImageLoader;
import com.pires.wesee.model.PhotoItem;
import com.pires.wesee.ui.activity.CourseDetailActivity;
import com.pires.wesee.ui.widget.AvatarImageView;
import com.pires.wesee.Constants;
import com.pires.wesee.R;
import com.pires.wesee.model.User;

import java.util.List;

/**
 * Created by Administrator on 2016/1/18 0018.
 */
public class CourseAdapter extends BaseAdapter {

    private Context mContext;
    private List<PhotoItem> mPhotoItems;

    public CourseAdapter(Context context, List<PhotoItem> photoItems) {
        this.mContext = context;
        this.mPhotoItems = photoItems;
    }

    @Override
    public int getCount() {
        return mPhotoItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mPhotoItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static ViewHolder viewHolder;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_course,null);
            viewHolder = new ViewHolder();
            viewHolder.avatar = (AvatarImageView) convertView.findViewById(R.id.item_course_avatar);
            viewHolder.avatarName = (TextView) convertView.findViewById(R.id.item_course_content_avatar_name);
            viewHolder.imageNum = (TextView) convertView.findViewById(R.id.item_course_content_image);
            viewHolder.img = (ImageView) convertView.findViewById(R.id.item_course_img);
            viewHolder.likeNum = (TextView) convertView.findViewById(R.id.item_course_content_like);
            viewHolder.viewNum = (TextView) convertView.findViewById(R.id.item_course_content_view);
            viewHolder.title = (TextView) convertView.findViewById(R.id.item_course_content_title);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        PhotoItem photoItem = mPhotoItems.get(position);
        PsGodImageLoader.getInstance().displayImage(
                photoItem.getImageURL(),viewHolder.img, Constants.DISPLAY_IMAGE_OPTIONS);
        PsGodImageLoader.getInstance().displayImage(
                photoItem.getAvatarURL(),viewHolder.avatar,Constants.DISPLAY_IMAGE_OPTIONS_AVATAR);
        viewHolder.avatar.setUser(new User(photoItem));
        viewHolder.avatarName.setText(photoItem.getNickname());
        viewHolder.viewNum.setText(String.valueOf(photoItem.getClickCount()));
        viewHolder.likeNum.setText(String.valueOf(photoItem.getLikeCount()));
        viewHolder.title.setText(photoItem.getTitle());
        viewHolder.imageNum.setText(String.valueOf(photoItem.getReplyCount()));
        convertView.setTag(R.id.tupppai_view_id, photoItem);
        convertView.setOnClickListener(click);
        return convertView;
    }

    private View.OnClickListener click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            PhotoItem photoItem = (PhotoItem) view.getTag(R.id.tupppai_view_id);
            Intent intent = new Intent(mContext, CourseDetailActivity.class);
            intent.putExtra("id",photoItem.getAskId());
            mContext.startActivity(intent);
        }
    };

    private static class ViewHolder{
        ImageView img;
        TextView title;
        TextView likeNum;
        TextView viewNum;
        TextView imageNum;
        TextView avatarName;
        AvatarImageView avatar;
    }
}
