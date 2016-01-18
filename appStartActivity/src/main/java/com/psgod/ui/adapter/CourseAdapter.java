package com.psgod.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.psgod.model.PhotoItem;

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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return convertView;
    }

    private static class ViewHolder{

    }
}
