package com.psgod.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.psgod.Constants;
import com.psgod.PsGodImageLoader;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.ActivitiesAct;
import com.psgod.model.PhotoItem;
import com.psgod.model.Tupppai;
import com.psgod.ui.activity.ChannelActivity;
import com.psgod.ui.activity.CourseActivity;
import com.psgod.ui.activity.RecentActActivity;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

/**
 * Created by Administrator on 2015/12/4 0004.
 */
public class TupppaiAdapter extends MyBaseAdapter<Tupppai> {

    public TupppaiAdapter(Context context, List<Tupppai> list) {
        super(context, list);
    }

    private DisplayImageOptions mSmallSmallOptions = Constants.DISPLAY_IMAGE_OPTIONS_SMALL_SMALL;
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
            ViewGroup.LayoutParams layoutParams = holder.headImg.getLayoutParams();
            if(layoutParams != null){
                layoutParams.height = (int) (layoutParams.height*Utils.getWidthScale(context));
                holder.headImg.setLayoutParams(layoutParams);
            }
            holder.linear = (LinearLayout) convertView.findViewById(R.id.item_tupppai_linear);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        PsGodImageLoader.getInstance().displayImage(list.get(position).getApp_pic(), holder.headImg, mOptions);
        holder.linear.removeAllViews();
        List<PhotoItem> photoItems = list.get(position).getThreads();
        int itemLength = photoItems.size();
        if (itemLength == 0) {
            holder.linear.setVisibility(View.GONE);
        } else {
            holder.linear.setVisibility(View.VISIBLE);
        }
        for (int i = 0; i < 5 && itemLength != 0; i++) {
            ImageView view = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.
                    LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    (Utils.getScreenWidthPx(context) - Utils.dpToPx(context, 9) * 8) / 5);
            params.weight = 1;
            params.setMargins(Utils.dpToPx(context, 9), 0, 0, 0);
            view.setLayoutParams(params);
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            view.setImageDrawable(context.getResources().getDrawable(R.color.transparent));
            if (i < itemLength) {
                PsGodImageLoader.getInstance().
                        displayImage(photoItems.get(i).getImageURL(), view, mSmallSmallOptions);
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
            MobclickAgent.onEvent(context, "Tupppai_Channel_Click" + position);        //统计频道的点击次数

            Intent intent = new Intent();
            intent.putExtra("id", list.get(position).getId());
//            intent.putExtra("title", list.get(position).getDisplay_name());
            if (list.get(position).getCategory_type().equals("activity")) {
//                Tupppai tupppai = list.get(position);
                intent.setClass(context, RecentActActivity.class);
//                ActivitiesAct act = new ActivitiesAct();
//                act.setId(tupppai.getId());
//                act.setAsk_id(tupppai.getAsk_id());
//                act.setBanner_pic(tupppai.getBanner_pic());
//                act.setImage_url(tupppai.getApp_pic());
//                act.setUrl(tupppai.getUrl());
//                act.setPost_btn(tupppai.getPost_btn());
//                act.setDisplay_name(tupppai.getDisplay_name());
//                intent.putExtra(RecentActActivity.OBJ_ACTIVITY, act);
            } else if (list.get(position).getCategory_type().equals("channel")) {
                intent.setClass(context, ChannelActivity.class);
            } else if (list.get(position).getCategory_type().equals("tutorial")) {
                intent.setClass(context, CourseActivity.class);
            }
            context.startActivity(intent);
        }
    };

    private class ViewHolder {
        ImageView headImg;
        LinearLayout linear;
    }
}
