package com.psgod.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.psgod.R;
import com.psgod.model.Comment;
import com.psgod.model.PhotoItem;

import java.util.List;

/**
 * Created by pires on 16/1/19.
 */
public class CourseDetailCommentAdapter extends BaseAdapter {
    private static final String TAG = CourseDetailCommentAdapter.class.getSimpleName();
    private Context mContext;
    private List<Comment> mComments;

    public CourseDetailCommentAdapter(Context context,List<Comment> comments) {
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

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_course_detail_comment,null);
        return view;
    }

    private static class ViewHolder {

    }

}
