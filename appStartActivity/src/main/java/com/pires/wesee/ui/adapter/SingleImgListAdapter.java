package com.pires.wesee.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.pires.wesee.Constants;
import com.pires.wesee.PsGodImageLoader;
import com.pires.wesee.Utils;
import com.pires.wesee.model.PhotoItem;
import com.pires.wesee.model.SinglePhotoItem;
import com.pires.wesee.ui.widget.dialog.SinglePhotoDetailDialog2;
import com.pires.wesee.R;

import java.util.List;

/**
 * Created by Administrator on 2015/12/7 0007.
 */
public class SingleImgListAdapter extends RecyclerView.Adapter<SingleImgListAdapter.ViewHolder> {

    private Context context;
    private List<PhotoItem> list;
    private String ownUrl;
    private SinglePhotoItem singlePhotoItem;

    private DisplayImageOptions mSmallSmallOptions = Constants.DISPLAY_IMAGE_OPTIONS_SMALL_SMALL;

    public SingleImgListAdapter(Context context, SinglePhotoItem singlePhotoItem) {
        this.context = context;
        this.singlePhotoItem = singlePhotoItem;
        this.list = singlePhotoItem.getReplyPhotoItems();
    }

    public void setOwnUrl(String ownUrl) {
        this.ownUrl = ownUrl;
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
//            new SinglePhotoDetailDialog(context,photoItem).
//                    setIsOwn(photoItem.getImageURL().equals(ownUrl)).show();
            new SinglePhotoDetailDialog2(context,singlePhotoItem,photoItem.getPid()).show();
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
