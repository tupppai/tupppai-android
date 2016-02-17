package com.psgod.ui.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.psgod.Constants;
import com.psgod.PsGodImageLoader;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.PhotoItem;
import com.psgod.ui.activity.SinglePhotoDetail;
import com.psgod.ui.widget.dialog.SinglePhotoDetailDialog;

import java.util.List;

/**
 * Created by Administrator on 2015/12/7 0007.
 */
public class SingleImgListAdapter extends RecyclerView.Adapter<SingleImgListAdapter.ViewHolder> {

    private Context context;
    private List<PhotoItem> list;

    private DisplayImageOptions mSmallSmallOptions = Constants.DISPLAY_IMAGE_OPTIONS_SMALL_SMALL;

    public SingleImgListAdapter(Context context, List<PhotoItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageView view = new ImageView(context);
        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(Utils.dpToPx(context,47),
                        ViewGroup.LayoutParams.MATCH_PARENT);
        view.setScaleType(ImageView.ScaleType.CENTER_CROP);
        view.setLayoutParams(params);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PsGodImageLoader.getInstance().
                displayImage(list.get(position).getImageURL(), holder.img, mSmallSmallOptions);
        holder.img.setTag(R.id.tupppai_view_id,position);
        holder.img.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Integer position = (Integer) view.getTag(R.id.tupppai_view_id);
            PhotoItem photoItem = list.get(position);
            new SinglePhotoDetailDialog(context,photoItem).show();
        }
    };

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView img;

        public ViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView;
        }
    }



}
