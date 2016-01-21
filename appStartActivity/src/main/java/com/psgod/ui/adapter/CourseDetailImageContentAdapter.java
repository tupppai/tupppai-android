package com.psgod.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.psgod.R;
import com.psgod.model.ImageData;

import java.util.List;

/**
 * Created by pires on 16/1/21.
 */
public class CourseDetailImageContentAdapter extends BaseAdapter {

    private static final String TAG = CourseDetailCommentAdapter.class.getSimpleName();
    private Context mContext;
    private List<ImageData> mImageDatas;
    private ViewHolder mViewHolder;

    public CourseDetailImageContentAdapter (Context context , List<ImageData> imageDatas) {
        this.mContext = context;
        this.mImageDatas = imageDatas;
    }

    @Override
    public int getCount() {
        return mImageDatas.size();
    }

    @Override
    public Object getItem(int i) {
        return mImageDatas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_course_detail_image_content,parent,false);
            mViewHolder = new ViewHolder();
            mViewHolder.mImage = (ImageView) convertView.findViewById(R.id.course_image);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        return convertView;
    }

    private static class ViewHolder {
        private ImageView mImage;
    }
}
