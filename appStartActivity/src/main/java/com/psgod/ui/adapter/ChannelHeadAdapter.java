package com.psgod.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.model.PhotoItem;
import com.psgod.ui.activity.SinglePhotoDetail;
import com.psgod.ui.widget.dialog.CarouselPhotoDetailDialog;

import java.util.List;

/**
 * Created by Administrator on 2015/12/7 0007.
 */
public class ChannelHeadAdapter extends RecyclerView.Adapter<ChannelHeadAdapter.ViewHolder> {

    private Context context;
    private List<PhotoItem> list;

    private DisplayImageOptions mSmallOptions = Constants.DISPLAY_IMAGE_OPTIONS_SMALL;

    public ChannelHeadAdapter(Context context, List<PhotoItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_channel_head, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ImageLoader.getInstance().displayImage(list.get(position).getImageURL(), holder.img, mSmallOptions);
        holder.img.setTag(R.id.tupppai_view_id,position);
        holder.img.setOnClickListener(onClickListener);
        holder.txt.setText(list.get(position).getDesc());
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Integer position = (Integer) view.getTag(R.id.tupppai_view_id);
            PhotoItem photoItem = list.get(position);
            if (photoItem.getType() == 1 && photoItem.getReplyCount() == 0) {
                SinglePhotoDetail.startActivity(context, photoItem);
            } else {
                new CarouselPhotoDetailDialog(context, photoItem.getAskId(), photoItem.getPid()).show();
            }
        }
    };

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView img;
        TextView txt;

        public ViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.item_channel_head_img);
            txt = (TextView) itemView.findViewById(R.id.item_channel_head_txt);
        }
    }
}
