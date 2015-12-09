package com.psgod.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.PhotoItem;
import com.psgod.model.Tupppai;
import com.psgod.ui.activity.ChannelActivity;
import com.psgod.ui.activity.RecentActActivity;

import java.util.List;

/**
 * Created by Administrator on 2015/12/4 0004.
 */
public class TupppaiAdapter extends MyBaseAdapter<Tupppai> {

    public TupppaiAdapter(Context context, List<Tupppai> list) {
        super(context, list);
    }

    private DisplayImageOptions mSmallOptions = Constants.DISPLAY_IMAGE_OPTIONS_SMALL;
    private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS;

    @Override
    public int getCount() {
        return list.size();
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
        ImageLoader.getInstance().displayImage(list.get(position).getApp_pic(), holder.headImg, mOptions);
        holder.linear.removeAllViews();
        List<PhotoItem> photoItems = list.get(position).getThreads();
        for (int i = 0; i < 5; i++) {
            ImageView view = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.
                    LayoutParams(Utils.dpToPx(context, 60), Utils.dpToPx(context, 60));
            params.weight = 1;
            params.setMargins(Utils.dpToPx(context, 9), 0, 0, 0);
            view.setLayoutParams(params);
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (i < photoItems.size()) {
                ImageLoader.getInstance().displayImage(photoItems.get(i).getImageURL(), view, mSmallOptions);
            }
            holder.linear.addView(view);
        }
//        if (list.get(position).getThreads().size() == 0) {
//            for (int i = 0; i < 5; i++) {
//                TextView view = new TextView(context);
//                LinearLayout.LayoutParams params = new LinearLayout.
//                        LayoutParams(Utils.dpToPx(context, 60), Utils.dpToPx(context, 60));
//                params.weight = 1;
//                params.setMargins(Utils.dpToPx(context, 9), 0, 0, 0);
//                view.setLayoutParams(params);
//                view.setText("+");
//                view.setGravity(Gravity.CENTER);
//                view.setBackgroundColor(Color.parseColor("#cacaca"));
//                holder.linear.addView(view);
//            }
//        }
        convertView.setTag(R.id.tupppai_view_id, position);
        convertView.setOnClickListener(viewClick);
        return convertView;
    }

    View.OnClickListener viewClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Integer position = (Integer) view.getTag(R.id.tupppai_view_id);
            Intent intent = new Intent();
            intent.putExtra("id", list.get(position).getId());
            if (list.get(position).getCategory_type().equals("activity")) {
                intent.setClass(context, RecentActActivity.class);
            } else {
                intent.setClass(context, ChannelActivity.class);

            }
            context.startActivity(intent);
        }
    };

    private class ViewHolder {
        ImageView headImg;
        LinearLayout linear;
    }
}