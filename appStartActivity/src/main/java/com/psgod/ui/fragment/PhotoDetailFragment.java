package com.psgod.ui.fragment;

/**
 * 热门图片详情页面 轮播查看fragment
 *
 * @author brandwang
 */

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.PhotoItem;
import com.psgod.ui.activity.SinglePhotoDetail;

public class PhotoDetailFragment extends BaseFragment {
    private DisplayImageOptions mOptions = Constants.DISPLAY_IMAGE_OPTIONS;
    private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

    private PhotoItem photoItem;
    private ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        photoItem = (PhotoItem) bundle
                .getSerializable(Constants.IntentKey.PHOTO_ITEM);

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getActivity()).inflate(
                R.layout.fragment_hot_photo_detail, null);

        ImageLoader imageLoader = ImageLoader.getInstance();
        String imagePath = photoItem.getImageURL();

        imageView = (ImageView) view
                .findViewById(R.id.fragment_photo_detail_image);
        ViewGroup.LayoutParams params = imageView.getLayoutParams();

        int photoWidth = photoItem.getImageWidth();
        int photoHeight = photoItem.getImageHeight();
        // 不同高宽比进行图片缩放
        if (photoHeight > photoWidth) {
            params.height = Utils.dpToPx(getActivity(), 310f * Utils.getHeightScale(getActivity()));
            params.width = params.height * photoWidth / photoHeight;
        }

        if (photoWidth > photoHeight) {
            params.width = Utils.dpToPx(getActivity(), 280f * Utils.getWidthScale(getActivity()));
            params.height = params.width * photoHeight / photoWidth;
        }

        if (photoHeight == photoWidth) {
            params.width = Utils.dpToPx(getActivity(), 280f * Utils.getWidthScale(getActivity()));
            params.height = Utils.dpToPx(getActivity(), 280f * Utils.getWidthScale(getActivity()));
        }

        imageView.setLayoutParams(params);

        imageLoader.displayImage(imagePath, imageView, mOptions,
                imageLoadingListener);

        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SinglePhotoDetail.startActivity(getActivity(), photoItem);
            }
        });

        return view;
    }

    private ImageLoadingListener imageLoadingListener = new ImageLoadingListener() {
        @Override
        public void onLoadingComplete(String imageUri, View view,
                                      Bitmap loadedImage) {
            Bitmap bitmap = getRoundCornerBitmap(loadedImage, 5);
            imageView.setImageBitmap(bitmap);
        }

        @Override
        public void onLoadingCancelled(String arg0, View arg1) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onLoadingStarted(String arg0, View arg1) {
            // TODO Auto-generated method stub
        }
    };

    /**
     * 根据原图添加圆角
     *
     * @param bitmap
     * @param roundPX
     * @return bitmap
     */
    public static Bitmap getRoundCornerBitmap(Bitmap bitmap, float roundPX) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Bitmap bitmap2 = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap2);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, width, height);
        final RectF rectF = new RectF(rect);

        paint.setColor(color);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, roundPX, roundPX, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return bitmap2;
    }
}
