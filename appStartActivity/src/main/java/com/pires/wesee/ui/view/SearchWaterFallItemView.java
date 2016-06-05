package com.pires.wesee.ui.view;

/**
 * 瀑布流图片展示 用于搜索
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dodowaterfall.widget.ScaleImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.pires.wesee.PsGodImageLoader;
import com.pires.wesee.model.SearchWork;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.pires.wesee.Constants;
import com.pires.wesee.R;
import com.pires.wesee.Utils;
import com.pires.wesee.model.User;
import com.pires.wesee.ui.activity.SinglePhotoDetail;
import com.pires.wesee.ui.widget.AvatarImageView;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SearchWaterFallItemView extends RelativeLayout {
    private static final String TAG = SearchWaterFallItemView.class
            .getSimpleName();

    private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS_SMALL;
    private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

    private AnimateFirstDisplayListener mAnimateFirstListener;

    private Context mContext;
    private SearchWork.Data mData;
    private PhotoWaterFallListType mType;

    private AvatarImageView mAvatarIv;
    private TextView mNameTv;
    private ScaleImageView imageView;
    private TextView askDescTv;
    private ImageView typeImg;

    private RelativeLayout mUserInfoLayout;
    private RelativeLayout mDescLayout;

    /**
     * photowaterfallitemview类型：
     * <p/>
     * INPROGRESS_ASK 进行中—求P INPROGRESS_COMPLETE 进行中-已完成
     */
    // TODO 根据功能区分
    public static enum PhotoWaterFallListType {
        INPROGRESS_COMPLETE, RECENT_ASK, USER_PROFILE_WORKS, ALL_WORK
    }

    public SearchWaterFallItemView(Context context) {
        super(context);
        mContext = context;
    }

    public SearchWaterFallItemView(Context context, AttributeSet attribute) {
        this(context, attribute, 0);
        mContext = context;
    }

    public SearchWaterFallItemView(Context context, AttributeSet attrs,
                                   int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    public void initSearchWaterFallList() {
        initViews();

        initListener();
    }

    private void initListener() {
        imageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mData.getType() == 1) {
                    SinglePhotoDetail.startActivity(mContext,
                            Utils.dataToPhoto(mData));
                } else {
//                    new CarouselPhotoDetailDialog(mContext, Long.parseLong(mData.getAsk_id()),
//                            Long.parseLong(mData.getId())).show();
                    Utils.skipByObject(mContext, Long.parseLong(mData.getAsk_id()) ==
                                    Long.parseLong(mData.getId()) ?
                                    Constants.IntentKey.ASK_ID : Constants.IntentKey.REPLY_ID,
                            Long.parseLong(mData.getId()));
                }

            }
        });
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        mAvatarIv = (AvatarImageView) this
                .findViewById(R.id.item_avatar_imageview);
        mNameTv = (TextView) this.findViewById(R.id.nickname_tv);
        mUserInfoLayout = (RelativeLayout) this
                .findViewById(R.id.user_info_layout);
        imageView = (ScaleImageView) this
                .findViewById(R.id.inprogress_ask_item_image);
        askDescTv = (TextView) this.findViewById(R.id.ask_desc_tv);
        mDescLayout = (RelativeLayout) this.findViewById(R.id.desc_layout);
        typeImg = (ImageView) this.findViewById(R.id.type_img);
    }

    // 配置图片显示细节,更新数据
    public void setData(SearchWork.Data data) {
        mData = data;
        mAvatarIv.setUser(new User(mData));

        // 更新图片
        final PsGodImageLoader imageLoader = PsGodImageLoader.getInstance();
        PsGodImageLoader.getInstance().displayImage(mData.getAvatar(), mAvatarIv,
                Constants.DISPLAY_IMAGE_OPTIONS_AVATAR);
        imageView.setImageWidth(mData.getImage_width());
        imageView.setImageHeight(mData.getImage_height());
        imageLoader.displayImage(mData.getImage_url(), imageView, mOptions,
                mAnimateFirstListener);
        mNameTv.setText(mData.getNickname());
        askDescTv.setText(mData.getDesc());
        setTypeImg(mData.getType());
    }

    private void setTypeImg(int type) {
        if (type == 1) {
            typeImg.setImageResource(R.drawable.ic_search_yuantu);
        } else {
            typeImg.setImageResource(R.drawable.ic_search_zuopin);
        }
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
