package com.psgod;

import android.graphics.Bitmap;
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

    public static PsGodImageLoader getInstance() {
        if (imgLoader == null) {
            imgLoader = ImageLoader.getInstance();
        }
        if (psGodImageLoader == null) {
            psGodImageLoader = new PsGodImageLoader();
        }
        return psGodImageLoader;
    }

    public void init(ImageLoaderConfiguration configuration) {
        imgLoader.init(configuration);
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

    public void loadImage(String url,ImageLoadingListener listener){
        imgLoader.loadImage(url, listener);
    }

    public void loadImage(String url, DisplayImageOptions options, ImageLoadingListener listener){
        imgLoader.loadImage(getRuleImageUrl(url, options),options,listener);
    }

    public Bitmap loadImageSync(String url){
        return imgLoader.loadImageSync(url);
    }

    public void clearDiskCache(){
        imgLoader.clearDiskCache();
    }

    public static String getRuleImageUrl(String originImageUrl, DisplayImageOptions options) {
        return originImageUrl;
    }

}
