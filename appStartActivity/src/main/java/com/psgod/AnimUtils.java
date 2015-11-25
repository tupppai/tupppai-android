package com.psgod;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.psgod.R;

/**
 * Created by Administrator on 2015/11/25 0025.
 */
public class AnimUtils {

    public static void vanishAnimThumb(Context context, final View view, Animation.AnimationListener listener){
        Animation animation = new AnimationUtils().loadAnimation(
                context, R.anim.fav_vanish);
        if(listener == null) {
            animation.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation arg0) {

                }

                @Override
                public void onAnimationRepeat(Animation arg0) {

                }

                @Override
                public void onAnimationEnd(Animation arg0) {
                    view.setVisibility(View.GONE);
                }
            });
        }else{
            animation.setAnimationListener(listener);
        }
        view.setAnimation(animation);
        animation.start();
    }

    public static void vanishAnim(Context context, final View view,ViewGroup parent, Animation.AnimationListener listener){
        Animation animation = new AnimationUtils().loadAnimation(
                context, R.anim.fav_vanish);
        final ImageView thumb = new ImageView(context);
        thumb.setLayoutParams(view.getLayoutParams());
        thumb.setImageDrawable(view.getBackground());
        parent.addView(thumb);
        if(listener == null) {
            animation.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation arg0) {

                }

                @Override
                public void onAnimationRepeat(Animation arg0) {

                }

                @Override
                public void onAnimationEnd(Animation arg0) {
                    thumb.setVisibility(View.GONE);
                }
            });
        }else{
            animation.setAnimationListener(listener);
        }

        thumb.setAnimation(animation);
        animation.start();
    }


}
