package com.pires.wesee.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pires.wesee.Constants;
import com.pires.wesee.CustomToast;
import com.pires.wesee.PsGodImageLoader;
import com.pires.wesee.Utils;
import com.pires.wesee.model.PhotoItem;
import com.pires.wesee.ui.activity.MainActivity;
import com.pires.wesee.R;
import com.pires.wesee.model.SelectImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MultiImageSelectRecyclerAdapter extends RecyclerView.Adapter<MultiImageSelectRecyclerAdapter.ViewHolde> {

    //adapterType adapter的类型，任务或者本地图片
    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_BANG = 1;

    //uploadType 上传类型，用来判断可选择的图片数量
    public static final int TYPE_ASK = 0;
    public static final int TYPE_REPLY = 1;

    //bangType 任务类型，判断是当前任务还是过去任务
    public static final int TYPE_BANG_NOW = 0;
    public static final int TYPE_BANG_DONE = 1;

    private int uploadType = TYPE_ASK;
    private int bangType = TYPE_BANG_NOW;

    private Context mContext;

    //图片集合
    private List<SelectImage> mImages = new ArrayList<SelectImage>();

    //被选中的图片集合
    private List<SelectImage> mSelectedImages = new ArrayList<SelectImage>();

    //任务集合
    private List<PhotoItem> mPhotoItems = new ArrayList<PhotoItem>();

    //过去任务的集合
    private List<PhotoItem> mDonePhotoItems = new ArrayList<PhotoItem>();

    // 选择任务的序号
    private int checkedPhotoItem = 0;

    private int adapterType = 0;

    public MultiImageSelectRecyclerAdapter(Context context) {
        mContext = context;
    }

    /**
     * 选择某个图片，改变选择状态
     * @param image
     */

    long timeOne;
    long timeSecond;

    public boolean select(SelectImage image) {

        if (mSelectedImages.contains(image)) {
            mSelectedImages.remove(image);
            notifyDataSetChanged();
            return true;
        } else {
            if (mSelectedImages.size() >= 2 && uploadType == TYPE_ASK) {
                if (timeOne == 0) {
                    timeOne = System.currentTimeMillis();
                } else {
                    timeSecond = System.currentTimeMillis();
                }
                if (timeSecond - timeOne > 1000 || timeSecond == 0) {
                    CustomToast.show(mContext, "求P最多只能选择2张图片哦~", Toast.LENGTH_SHORT);
                    timeOne = 0;
                }
            } else if (mSelectedImages.size() >= 1 && uploadType == TYPE_REPLY) {
                if (timeOne == 0) {
                    timeOne = System.currentTimeMillis();
                } else {
                    timeSecond = System.currentTimeMillis();
                }
                if (timeSecond - timeOne > 1000 || timeSecond == 0) {
                    CustomToast.show(mContext, "作品最多只能选择1张图片哦~", Toast.LENGTH_SHORT);
                    timeOne = 0;
                }
            } else {
                mSelectedImages.add(image);
                notifyDataSetChanged();
                return true;
            }
        }
        return false;
    }

    public void setUploadType(int uploadType) {
        this.uploadType = uploadType;
    }

    public void setBangType(int bangType) {
        this.bangType = bangType;
    }

    /**
     * 通过图片路径设置默认选择
     * @param resultList
     */
    public void setDefaultSelected(List<SelectImage> resultList) {
        mSelectedImages.clear();
        for (SelectImage selectImage : resultList) {
            SelectImage image = getImageByPath(selectImage.path);
            if (image != null) {
                mSelectedImages.add(image);
            }
        }
        notifyDataSetChanged();
    }

    public SelectImage getImageByPath(String path) {
        if (mImages != null && mImages.size() > 0) {
            for (SelectImage image : mImages) {
                if (image.path.equalsIgnoreCase(path)) {
                    return image;
                }
            }
        }
        return null;
    }

    /**
     * 设置数据集
     *
     * @param images
     */
    public void setData(List<SelectImage> images) {
//        mSelectedImages.clear();
//
//        if (images != null && images.size() > 0) {
//            mImages = images;
//        } else {
//            mImages.clear();
//        }
//        notifyDataSetChanged();
        this.mImages = images;
    }

    public List<SelectImage> getSelectedImages() {
        return mSelectedImages;
    }

    public void setBangData(List<PhotoItem> photoItems) {
        mPhotoItems = photoItems;
    }

    public void setDoneBangData(List<PhotoItem> donePhotoItems) {
        mDonePhotoItems = donePhotoItems;
    }

    public void setAdapterType(int adapterType) {
        this.adapterType = adapterType;
    }


    public int getCheckedPhotoItemNum() {
        return checkedPhotoItem > 0 ? checkedPhotoItem - 1 : -checkedPhotoItem - 1;
    }

    public int getCheckBangType() {
        return checkedPhotoItem > 0 ? TYPE_BANG_NOW : TYPE_BANG_DONE;
    }

    /**
     *
     * 0为无选择
     * 正数为现在的任务，序号从1开始
     * 负数为过去任务，序号从-1开始
     *
     * @return
     */
    public PhotoItem getCheckedPhotoItem() {
        return checkedPhotoItem >= 1 ? mPhotoItems.get(checkedPhotoItem - 1) :
                checkedPhotoItem <= -1 ? mDonePhotoItems.get(-checkedPhotoItem - 1) : null;
    }

    @Override
    public ViewHolde onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolde(LayoutInflater.from(mContext).
                inflate(R.layout.item_select_image_recycler, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return adapterType == TYPE_IMAGE ? 0 : position == 20 ? 1 : 0;
    }

    @Override
    public void onBindViewHolder(ViewHolde holder, int position) {
        if (adapterType == TYPE_BANG) {
            if (getItemViewType(position) == 1) {
                ((RelativeLayout) holder.itemView).removeAllViews();
                TextView textView = new TextView(mContext);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        Utils.dpToPx(mContext,135), Utils.dpToPx(mContext,202)
                );
                textView.setGravity(Gravity.CENTER);
                textView.setText("查看全部");
                textView.setLayoutParams(layoutParams);
                textView.setTextSize(15);
                ((RelativeLayout) holder.itemView).addView(textView);
                holder.itemView.setOnClickListener(bangMoreClick);
            } else {
                List<PhotoItem> photoItems;
                if (bangType == TYPE_BANG_NOW) {
                    photoItems = mPhotoItems;
                } else {
                    photoItems = mDonePhotoItems;
                }
                holder.imageArea.setVisibility(View.INVISIBLE);
                holder.bangArea.setVisibility(View.VISIBLE);
                if (photoItems != null) {
                    PsGodImageLoader.getInstance().displayImage(photoItems.get(position).getImageURL()
                            , holder.bangImage, Constants.DISPLAY_IMAGE_OPTIONS_SMALL_SMALL);
                    PsGodImageLoader.getInstance().displayImage(photoItems.get(position).getAvatarURL()
                            , holder.bangAvatar, Constants.DISPLAY_IMAGE_OPTIONS_AVATAR);
                    holder.bangName.setText(photoItems.get(position).getNickname());
                    holder.bangDesc.setText(photoItems.get(position).getDesc());
                    if (position == (bangType == TYPE_BANG_NOW ? checkedPhotoItem - 1
                            : -checkedPhotoItem - 1)) {
                        holder.bangCheck.setImageResource(R.mipmap.zuopin_ic_sel_sel);
                    } else {
                        holder.bangCheck.setImageResource(R.mipmap.zuopin_ic_sel_nor);
                    }
                    holder.itemView.setTag(R.id.tupppai_view_id, position);
                    holder.itemView.setOnClickListener(bangClick);
                }
            }
        } else {
            holder.imageArea.setVisibility(View.VISIBLE);
            holder.bangArea.setVisibility(View.INVISIBLE);
            holder.bindData(mImages.get(position));
            holder.itemView.setTag(R.id.tupppai_view_id, mImages.get(position));
            holder.itemView.setOnClickListener(imageClick);
        }
    }

    private OnImageClickListener onImageClickListener;
    private OnBangClickListener onBangClickListener;

    public void setOnBangClickListener(OnBangClickListener onBangClickListener) {
        this.onBangClickListener = onBangClickListener;
    }

    public void setOnImageClickListener(OnImageClickListener onImageClickListener) {
        this.onImageClickListener = onImageClickListener;
    }

    private View.OnClickListener bangMoreClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Bundle bundle = new Bundle();
            bundle.putInt(MainActivity.IntentParams.KEY_FRAGMENT_ID,
                    R.id.activity_inprogress_tab_page);
            MainActivity.startNewActivityAndFinishAllBefore(mContext,
                    mContext.getClass().getSimpleName(),bundle);
        }
    };

    private View.OnClickListener imageClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SelectImage image = (SelectImage) view.getTag(R.id.tupppai_view_id);
            if (onImageClickListener != null && select(image)) {
                onImageClickListener.onImageClick(view, mSelectedImages);
            }
        }
    };

    //图片点击时回调
    public interface OnImageClickListener {
        void onImageClick(View view, List<SelectImage> selectImages);
    }

    //任务点击时回调
    public interface OnBangClickListener {
        void onBangClick(View view);
    }

    private View.OnClickListener bangClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Integer position = (Integer) view.getTag(R.id.tupppai_view_id);
            checkedPhotoItem = bangType == TYPE_BANG_NOW ? position + 1 : -position - 1;
            notifyDataSetChanged();
            if (onBangClickListener != null) {
                onBangClickListener.onBangClick(view);
            }
        }
    };

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return adapterType == TYPE_BANG ? bangType == TYPE_BANG_NOW ? mPhotoItems.size() == 20 ? 21 :
                mPhotoItems.size() : mDonePhotoItems.size() == 20 ? 21 : mDonePhotoItems.size()
                : mImages.size();
    }

    class ViewHolde extends RecyclerView.ViewHolder {
        RelativeLayout imageArea;
        ImageView imageImage;
        ImageView imageCheck;

        RelativeLayout bangArea;
        ImageView bangAvatar;
        ImageView bangImage;
        TextView bangName;
        ImageView bangCheck;
        TextView bangDesc;

        ViewHolde(View view) {
            super(view);
            imageArea = (RelativeLayout) view.findViewById(R.id.item_select_image_imagearea);
            imageImage = (ImageView) view.findViewById(R.id.item_select_image_imagearea_image);
            imageCheck = (ImageView) view.findViewById(R.id.item_select_image_imagearea_checkmark);

            bangArea = (RelativeLayout) view.findViewById(R.id.item_select_image_bangarea);
            bangAvatar = (ImageView) view.findViewById(R.id.item_select_image_bangarea_avatar);
            bangImage = (ImageView) view.findViewById(R.id.item_select_image_bangarea_image);
            bangName = (TextView) view.findViewById(R.id.item_select_image_bangarea_name);
            bangCheck = (ImageView) view.findViewById(R.id.item_select_image_bangarea_checkmark);
            bangDesc = (TextView) view.findViewById(R.id.item_select_image_bangarea_desc);
        }

        void bindData(final SelectImage data) {
            if (data == null)
                return;
            // 处理单选和多选状态
            if (mSelectedImages.contains(data)) {
                // 设置选中状态
                bangCheck.setImageResource(R.mipmap.zuopin_ic_sel_sel);
                imageCheck.setImageResource(R.mipmap.zuopin_ic_sel_sel);
            } else {
                // 未选择
                bangCheck.setImageResource(R.mipmap.zuopin_ic_sel_nor);
                imageCheck.setImageResource(R.mipmap.zuopin_ic_sel_nor);
            }
            File imageFile = new File(data.path);
//			Bitmap bitmap = BitmapFactory.decodeFile()
            // 显示图片
//            Picasso.with(mContext).load(imageFile)
//                    .placeholder(R.drawable.default_error)
//                            // .error(R.drawable.default_error)
//                    .centerCrop().into(imageImage);
            PsGodImageLoader.getInstance().
                    displayImage(imageFile.getPath()
                            , imageImage, Constants.DISPLAY_IMAGE_OPTIONS_LOCAL);
        }
    }

}
