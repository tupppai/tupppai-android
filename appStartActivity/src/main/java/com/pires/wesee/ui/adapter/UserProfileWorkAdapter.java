package com.pires.wesee.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pires.wesee.model.PhotoItem;
import com.pires.wesee.ui.view.PhotoWaterFallItemView;
import com.pires.wesee.ui.widget.dialog.InprogressShareMoreDialog;
import com.pires.wesee.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/15 0015.
 */
public class UserProfileWorkAdapter extends RecyclerView.Adapter<UserProfileWorkAdapter.ViewHolder> {

    private Context mContext;
    private List<PhotoItem> mPhotoItems = new ArrayList<PhotoItem>();
    private PhotoWaterFallItemView.PhotoWaterFallListType mPhotoType;
    private InprogressShareMoreDialog inprogressShareDialog;
    private int type = 0;

    public UserProfileWorkAdapter(Context context, List<PhotoItem> photoItems,
                                  PhotoWaterFallItemView.PhotoWaterFallListType photoType) {
        mContext = context;
        mPhotoItems = photoItems;
        mPhotoType = photoType;
    }


    public void setType(int type) {
        this.type = type;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(
                R.layout.widget_photo_water_fall_item_view, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PhotoWaterFallItemView photoWaterFallItemView;
        try {
            final PhotoItem photoItem = mPhotoItems.get(position);
            photoWaterFallItemView = holder.view;
            photoWaterFallItemView.setType(type);
            photoWaterFallItemView.initPhotoWaterFallListType(mPhotoType);
            photoWaterFallItemView.setPhotoItem(photoItem, mPhotoType);

            if (mPhotoType == PhotoWaterFallItemView.PhotoWaterFallListType.INPROGRESS_COMPLETE) {
                photoWaterFallItemView
                        .setOnLongClickListener(new View.OnLongClickListener() {

                            @Override
                            public boolean onLongClick(View arg0) {
                                if (inprogressShareDialog == null) {
                                    inprogressShareDialog = new InprogressShareMoreDialog(
                                            mContext);
                                }
                                inprogressShareDialog
                                        .setPhotoItem(
                                                photoItem,
                                                InprogressShareMoreDialog.SHARE_TYPE_COMPLETE);
                                if (inprogressShareDialog.isShowing()) {
                                    inprogressShareDialog.dismiss();
                                } else {
                                    inprogressShareDialog.show();
                                }
                                return true;
                            }
                        });
            }
        } catch (Exception e) {

        }

    }

    @Override
    public int getItemCount() {
        return mPhotoItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        PhotoWaterFallItemView view;

        public ViewHolder(View itemView) {
            super(itemView);
            view = (PhotoWaterFallItemView) itemView;
        }
    }

}
