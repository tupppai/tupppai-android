package com.psgod;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

/**
 * Created by Administrator on 2015/12/14 0014.
 */
public class PsGodImageLoader {

    private static ImageLoader imgLoader;
    private static PsGodImageLoader psGodImageLoader;
    public static Context context;

    public static PsGodImageLoader getInstance() {
        if (imgLoader == null) {
            imgLoader = ImageLoader.getInstance();
        }
        if (psGodImageLoader == null) {
            psGodImageLoader = new PsGodImageLoader();
        }
        return psGodImageLoader;
    }

    public void init(ImageLoaderConfiguration configuration, Context context) {
        imgLoader.init(configuration);
        this.context = context;
    }

    public void displayImage(String url, ImageAware imageAware) {
        imgLoader.displayImage(url, imageAware);
    }

    public void displayImage(String url, ImageAware imageAware, DisplayImageOptions options) {
        imgLoader.displayImage(getRuleImageUrl(url, options), imageAware, options);
    }

    public void displayImage(String url, ImageAware imageAware, DisplayImageOptions options,
                             ImageLoadingListener listener) {
        imgLoader.displayImage(getRuleImageUrl(url, options), imageAware, options, listener);
    }

    public void displayImage(String url, ImageAware imageAware, ImageLoadingListener listener) {
        imgLoader.displayImage(url, imageAware, listener);
    }

    public void displayImage(String url, ImageAware imageAware, DisplayImageOptions options,
                             ImageLoadingListener listener, ImageLoadingProgressListener progressListener) {
        imgLoader.displayImage(getRuleImageUrl(url, options), imageAware, options, listener, progressListener);
    }

    public void displayImage(String url, ImageView imageView) {
        imgLoader.displayImage(url, imageView);
    }

    public void displayImage(String url, ImageView imageView, DisplayImageOptions options) {
        imgLoader.displayImage(getRuleImageUrl(url, options), imageView, options);
    }

    public void displayImage(String url, ImageView imageView, DisplayImageOptions options,
                             ImageLoadingListener listener) {
        imgLoader.displayImage(getRuleImageUrl(url, options), imageView, options, listener);
    }

    public void displayImage(String url, ImageView imageView, ImageLoadingListener listener) {
        imgLoader.displayImage(url, imageView, listener);
    }

    public void displayImage(String url, ImageView imageView, DisplayImageOptions options,
                             ImageLoadingListener listener, ImageLoadingProgressListener progressListener) {
        imgLoader.displayImage(getRuleImageUrl(url, options), imageView, options, listener, progressListener);
    }

    public void loadImage(String url, ImageLoadingListener listener) {
        imgLoader.loadImage(url, listener);
    }

    public void loadImage(String url, DisplayImageOptions options, ImageLoadingListener listener) {
        imgLoader.loadImage(getRuleImageUrl(url, options), options, listener);
    }

    public void displayImage(String url, ImageArea imageArea) {
        imgLoader.displayImage(url, imageArea.getImage());
    }

    public void displayImage(String url, ImageArea imageArea, DisplayImageOptions options) {
        imgLoader.displayImage(getRuleImageUrl(url, options), imageArea.getImage(), options);
    }

    public void displayImage(String url, ImageArea imageArea, DisplayImageOptions options,
                             ImageLoadingListener listener) {
        imgLoader.displayImage(getRuleImageUrl(url, options), imageArea.getImage(), options, listener);
    }

    public void displayImage(String url, ImageArea imageArea, ImageLoadingListener listener) {
        imgLoader.displayImage(url, imageArea.getImage(), listener);
    }

    public void displayImage(String url, ImageArea imageArea, DisplayImageOptions options,
                             ImageLoadingListener listener, ImageLoadingProgressListener progressListener) {
        imgLoader.displayImage(getRuleImageUrl(url, options), imageArea.getImage(), options, listener, progressListener);
    }

    public void displayLocaleImage() {
//        imgLoader.
    }


    public Bitmap loadImageSync(String url) {
        return imgLoader.loadImageSync(url);
    }

    public void clearDiskCache() {
        imgLoader.clearDiskCache();
    }

    public String getRuleImageUrl(String originImageUrl, DisplayImageOptions options) {
        String ruleImageUrl;
        if (originImageUrl != null && !originImageUrl.equals("")) {
            String[] thumbs = originImageUrl.split("\\?");
            if (options.equals(Constants.DISPLAY_IMAGE_OPTIONS)) {
                ruleImageUrl = String.format("%s?imageView2/0/w/%s", thumbs[0],
                        String.valueOf((int) (1080 * Utils.getWidthScale(context))));
            } else if (options.equals(Constants.DISPLAY_IMAGE_OPTIONS_SMALL)) {
                ruleImageUrl = String.format("%s?imageView2/0/w/%s", thumbs[0],
                        String.valueOf((int) (500 * Utils.getWidthScale(context))));
            } else if (options.equals(Constants.DISPLAY_IMAGE_OPTIONS_AVATAR)) {
                ruleImageUrl = String.format("%s?imageView2/0/w/%s", thumbs[0],
                        String.valueOf((int) (200 * Utils.getWidthScale(context))));
            } else if (options.equals(Constants.DISPLAY_IMAGE_OPTIONS_SMALL_SMALL)) {
                ruleImageUrl = String.format("%s?imageView2/0/w/%s", thumbs[0],
                        String.valueOf((int) (300 * Utils.getWidthScale(context))));
            } else if (options.equals(Constants.DISPLAY_IMAGE_OPTIONS_LOCAL)) {
                ruleImageUrl = "file://" + originImageUrl;
            } else if (options.equals(Constants.DISPLAY_IMAGE_OPTIONS_ORIGIN)) {
                ruleImageUrl = thumbs[0];
            } else {
                ruleImageUrl = originImageUrl;
            }
        } else {
            ruleImageUrl = originImageUrl;
        }
        return ruleImageUrl;
    }

    public interface ImageArea {
        ImageView getImage();
    }

}
