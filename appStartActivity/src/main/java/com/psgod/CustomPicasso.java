package com.psgod;

import android.content.Context;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class CustomPicasso {

	public static void displayImage(Context context, String url, ImageView view) {
		Picasso.with(context)
				.load(url)
				.error(context.getResources().getDrawable(R.drawable.ic_lietu))
				.placeholder(
						context.getResources().getDrawable(
								R.drawable.ic_zhanwei)).into(view);
	}

	public static void displayImageSmall(Context context, String url,
			ImageView view) {
		Picasso.with(context)
				.load(url)
				.error(context.getResources().getDrawable(
						R.drawable.ic_zhanwei_small))
				.placeholder(
						context.getResources().getDrawable(
								R.drawable.ic_zhanwei_small)).into(view);
	}

	public static void displayImageAvatar(Context context, String url,
			ImageView view) {
		Picasso.with(context)
				.load(url)
				.error(context.getResources().getDrawable(
						R.drawable.head_portrait))
				.placeholder(
						context.getResources().getDrawable(
								R.drawable.head_portrait)).into(view);
	}

	public static void displayImageNull(Context context, String url,
			ImageView view) {
		Picasso.with(context).load(url).into(view);
	}

}
