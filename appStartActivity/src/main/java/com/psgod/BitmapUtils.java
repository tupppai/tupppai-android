package com.psgod;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.util.LruCache;

/**
 * Bitmap 图片操作类
 * 
 * @author brandwang
 */

public final class BitmapUtils {
	private static final String TAG = BitmapUtils.class.getSimpleName();
	public static LruCache<String, Bitmap> lruCache;
	static {
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int maxSize = maxMemory / 16;
		lruCache = new LruCache<String, Bitmap>(maxSize) {

			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight();
			}
		};
	}

	/**
	 * @description 计算图片的压缩比率
	 * 
	 * @param options
	 *            参数
	 * @param reqWidth
	 *            目标的宽度
	 * @param reqHeight
	 *            目标的高度
	 * @return
	 */
	private static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		int width = options.outWidth;
		int height = options.outHeight;
		int inSampleSize = 1;

		while ((width > reqWidth) || (height > reqHeight)) {
			width >>= 1;
			height >>= 1;
			inSampleSize <<= 1;
		}
		return inSampleSize;
		// if (height > reqHeight || width > reqWidth) {
		// // 计算出实际宽高和目标宽高的比率
		// //final int halfHeight = height / 2;
		// //final int halfWidth = width / 2;
		// //while ((halfHeight / inSampleSize) > reqHeight && (halfWidth /
		// inSampleSize) > reqWidth) {
		// //inSampleSize *= 2;
		// //}
		//
		// // 计算出实际宽高和目标宽高的比率
		// final int heightRatio = Math.round((float) height / (float)
		// reqHeight);
		// final int widthRatio = Math.round((float) width / (float) reqWidth);
		// // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
		// // 一定都会大于等于目标的宽和高。
		// inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		// }
		// return inSampleSize;
	}

	/**
	 * @description 通过传入的bitmap，进行压缩，得到符合标准的bitmap
	 * 
	 * @param src
	 * @param dstWidth
	 * @param dstHeight
	 * @return
	 */
	private static Bitmap createScaleBitmap(Bitmap src, int dstWidth,
			int dstHeight, int inSampleSize) {
		// 如果inSampleSize是2的倍数，也就说这个src已经是我们想要的缩略图了，直接返回即可。
		if (inSampleSize % 2 == 0) {
			return src;
		}
		// 如果是放大图片，filter决定是否平滑，如果是缩小图片，filter无影响，我们这里是缩小图片，所以直接设置为false
		Bitmap dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
		if (src != dst) { // 如果没有缩放，那么不回收
			src.recycle(); // 释放Bitmap的native像素数组
		}
		return dst;
	}

	public static Bitmap decodeBitmap(String path) {
		return decodeBitmap(path, Constants.WIDTH_OF_SCREEN,
				Constants.HEIGHT_OF_SCREEN, null);
	}

	public static Bitmap decodeBitmap(String path, int reqWidth, int reqHeight) {
		return decodeBitmap(path, reqWidth, reqHeight, null);
	}

	public static Bitmap decodeBitmap(String path, int reqWidth, int reqHeight,
			DecodeErrorListener errorListener) {
		if ((reqWidth <= 0) || (reqHeight <= 0)) {
			String msg = new StringBuilder("Error, reqWidth=")
					.append(String.valueOf(reqWidth)).append(" reqHeight=")
					.append(String.valueOf(reqHeight)).toString();
			Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_DEV, TAG, msg,
					true);
			throw new IllegalArgumentException(msg);
		}

		// 获取图片的宽和高
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		options.inJustDecodeBounds = false;
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeFile(path, options);
		} catch (OutOfMemoryError e1) {
			e1.printStackTrace();
			Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_DEV, TAG,
					e1.getMessage(), true);
			if (errorListener != null) {
				errorListener.onDecodeError();
			}
			// 再缩小一倍大小，解码一次
			options.inSampleSize *= 2;
			try {
				bitmap = BitmapFactory.decodeFile(path, options);
			} catch (OutOfMemoryError e2) {
				e1.printStackTrace();
				Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_DEV, TAG,
						e1.getMessage(), true);
				if (errorListener != null) {
					errorListener.onDecodeError();
				}
				// TODO 用裂图吧
				bitmap = null;
			}
		}
		return bitmap;
	}

	public static interface DecodeErrorListener {
		public void onDecodeError();
	}

	// 对图片进行处理，得到毛玻璃效果
	public static Bitmap getBlurBitmap(Bitmap bitmap) {
		Bitmap bluredBitmap = null;
		if (lruCache.get(bitmap.toString()) == null) {

			// 图片缩放比例 TODO 做成参数可配置
			float scaleFactor = 8;
			// 模糊程度
			float radius = 10;

			int width = bitmap.getWidth();
			int height = bitmap.getHeight();

			bluredBitmap = Bitmap.createBitmap((int) (width / scaleFactor),
					(int) (height / scaleFactor), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bluredBitmap);
			canvas.scale(1 / scaleFactor, 1 / scaleFactor);

			Paint paint = new Paint();
			paint.setFlags(Paint.FILTER_BITMAP_FLAG);
			canvas.drawBitmap(bitmap, 0, 0, paint);

			bluredBitmap = FastBlur.doBlur(bluredBitmap, (int) radius, true);

			lruCache.put(bitmap.toString(), bluredBitmap);
		}
		return lruCache.get(bitmap.toString());
	}
}