package com.pires.wesee.model;

/**
 * 图片信息相关类
 * @author brandwang
 */
import java.io.Serializable;

public class ImageData implements Serializable {
	public int mImageWidth;
	public int mImageHeight;
	public String mImageUrl;

	public ImageData(int width, int height, String url) {
		mImageWidth = width;
		mImageHeight = height;
		mImageUrl = url;
	}
}
