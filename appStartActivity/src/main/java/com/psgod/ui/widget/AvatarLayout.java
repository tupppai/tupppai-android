package com.psgod.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.psgod.Constants;
import com.psgod.PsGodImageLoader;
import com.psgod.R;
import com.psgod.Utils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ZouMengyuan on 15/12/23.
 */
public class AvatarLayout extends FrameLayout{

    private Context mContext;

    private AvatarImageView mAvatarImage;
    private ImageView mVipicon;

    private TypedArray a;

    // 图片加载器单例
    private PsGodImageLoader imageLoader = PsGodImageLoader.getInstance();

    private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

    private AnimateFirstDisplayListener mAnimateFirstListener = new AnimateFirstDisplayListener();

    public AvatarLayout(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public AvatarLayout(Context context,AttributeSet attrs) {
        super(context,attrs);
        this.mContext = context;
        a = mContext.obtainStyledAttributes(attrs, R.styleable.ViewGroup);
        init();
    }

    private void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.widget_avatar_layout,null);
        mAvatarImage = (AvatarImageView) view.findViewById(R.id.avatar_imgview);
        mVipicon = (ImageView) view.findViewById(R.id.avatar_vip_icon);
        initSize(view);
        addView(view);
    }

    private void initSize(View view) {
        ViewGroup.LayoutParams groupLayoutParams = this.getLayoutParams();
        FrameLayout.LayoutParams mlayoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        mAvatarImage.setLayoutParams(mlayoutParams);
        mAvatarImage.setPadding(0, 0, Utils.dpToPx(mContext, 5), 0);


        int mMinWidth = a.getDimensionPixelSize(R.styleable.View_minWidth, 0);
        Toast.makeText(mContext,""+mMinWidth ,Toast.LENGTH_SHORT).show();

        FrameLayout.LayoutParams mIconLayoutParams = new FrameLayout.LayoutParams(Utils.dpToPx(mContext,5),
                Utils.dpToPx(mContext,5));
        mIconLayoutParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        mVipicon.setLayoutParams(mIconLayoutParams);
    }

    public void setAvatarUrl(String avatarUrl) {
        imageLoader.displayImage(avatarUrl, mAvatarImage,
                mAvatarOptions, mAnimateFirstListener);
    }

    /**
     * 图片首次出现时的动画
     *
     * @author rayalyuan
     */
    private static class AnimateFirstDisplayListener extends
            SimpleImageLoadingListener {
        static final List<String> displayedImages = Collections
                .synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view,
                                      Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }

}
