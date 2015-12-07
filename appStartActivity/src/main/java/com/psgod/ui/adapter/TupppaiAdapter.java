package com.psgod.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.psgod.R;
import com.psgod.Utils;

import java.util.List;

/**
 * Created by Administrator on 2015/12/4 0004.
 */
public class TupppaiAdapter extends MyBaseAdapter<Object> {

    public TupppaiAdapter(Context context, List<Object> list) {
        super(context, list);
    }

    @Override
    public int getCount() {
        return 5;
    }

    private static ViewHolder holder;

    @Override
    View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_tuppai, parent, false);
            holder = new ViewHolder();
            holder.headImg = (ImageView) convertView.findViewById(R.id.item_tupppai_headimg);
            holder.linear = (LinearLayout) convertView.findViewById(R.id.item_tupppai_linear);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.headImg.setImageDrawable(context.getResources().getDrawable(R.mipmap.tupppai_banner));
        holder.linear.removeAllViews();
        for (int i = 0; i < 5; i++) {
            ImageView view = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.
                    LayoutParams(Utils.dpToPx(context,60), Utils.dpToPx(context,60));
            params.weight = 1;
                params.setMargins(Utils.dpToPx(context, 9), 0, 0, 0);
            view.setLayoutParams(params);
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            view.setImageDrawable(context.getResources().getDrawable(R.mipmap.tupppai_ask));
            holder.linear.addView(view);
        }

        return convertView;
    }

    private class ViewHolder {
        ImageView headImg;
        LinearLayout linear;
    }
}
