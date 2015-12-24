package com.psgod.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
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
import com.psgod.ui.activity.UserProfileActivity;
import com.psgod.ui.view.CircleImageView;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ZouMengyuan on 15/12/23.
 */
public class AvatarImage extends CircleImageView {

    private Context mContext;



    public AvatarImage(Context context) {
        super(context);
        this.mContext = context;
    }

    public AvatarImage(Context context, AttributeSet attrs) {
        super(context,attrs);
        this.mContext = context;
    }

}
