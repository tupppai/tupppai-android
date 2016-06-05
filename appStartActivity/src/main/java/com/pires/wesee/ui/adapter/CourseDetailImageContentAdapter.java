package com.pires.wesee.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.pires.wesee.PsGodImageLoader;
import com.pires.wesee.model.ImageData;
import com.pires.wesee.Constants;
import com.pires.wesee.R;
import com.pires.wesee.Utils;

import java.util.List;

/**
 * Created by pires on 16/1/21.
 */
public class CourseDetailImageContentAdapter extends BaseAdapter {

    private static final String TAG = CourseDetailCommentAdapter.class.getSimpleName();
    private Context mContext;
    private List<ImageData> mImageDatas;
    private ViewHolder mViewHolder;

    private boolean isLock = true;

    public CourseDetailImageContentAdapter(Context context, List<ImageData> imageDatas) {
        this.mContext = context;
        this.mImageDatas = imageDatas;
    }

    @Override
    public int getCount() {
        return mImageDatas.size();
    }

    @Override
    public Object getItem(int i) {
        return mImageDatas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public boolean isLock() {
        return isLock;
    }

    public void setIsLock(boolean isLock) {
        this.isLock = isLock;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_course_detail_image_content, parent, false);
            mViewHolder = new ViewHolder();
            mViewHolder.mImage = (ImageView) convertView.findViewById(R.id.course_image);
            mViewHolder.mLockArea = (RelativeLayout) convertView.findViewById(R.id.course_detail_image_lockarea);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        ImageData imageData = mImageDatas.get(position);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) mViewHolder.mImage.getLayoutParams();
        if (params == null) {
            params = new RelativeLayout.
                    LayoutParams(Utils.getScreenWidthPx(mContext),
                    getImageHeight(imageData.mImageWidth, imageData.mImageHeight));
        } else {
            params.width = Utils.getScreenWidthPx(mContext);
            params.height = getImageHeight(imageData.mImageWidth, imageData.mImageHeight);
        }
        mViewHolder.mImage.setLayoutParams(params);
        mViewHolder.mLockArea.setLayoutParams(params);
        PsGodImageLoader.getInstance().displayImage(imageData.mImageUrl
                , mViewHolder.mImage, Constants.DISPLAY_IMAGE_OPTIONS_ORIGIN);
        if (isLock && position == getCount() - 1) {
            mViewHolder.mLockArea.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.mLockArea.setVisibility(View.GONE);
        }
        return convertView;
    }

    private int getImageHeight(int realWidth, int realHeight) {
        return (int) ((float) realHeight * (float) Utils.getScreenWidthPx(mContext) / (float) realWidth);
    }

    private static class ViewHolder {
        ImageView mImage;
        RelativeLayout mLockArea;
    }
}
