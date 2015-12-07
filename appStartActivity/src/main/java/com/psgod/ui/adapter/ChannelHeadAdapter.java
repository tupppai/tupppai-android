package com.psgod.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.psgod.R;

import java.util.List;

/**
 * Created by Administrator on 2015/12/7 0007.
 */
public class ChannelHeadAdapter extends RecyclerView.Adapter<ChannelHeadAdapter.ViewHolder> {

    private Context context;
    private List<Object> list;

    public ChannelHeadAdapter(Context context, List<Object> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_channel_head,null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.img.setImageDrawable(context.getResources().getDrawable(R.mipmap.tupppai_ask));
        holder.txt.setText("测试");
    }

    @Override
    public int getItemCount() {
        return 10;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        ImageView img;
        TextView txt;

        public ViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.item_channel_head_img);
            txt = (TextView) itemView.findViewById(R.id.item_channel_head_txt);
        }
    }
}
