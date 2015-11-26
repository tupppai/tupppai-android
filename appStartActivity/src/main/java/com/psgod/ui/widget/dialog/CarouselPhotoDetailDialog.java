package com.psgod.ui.widget.dialog;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nineoldandroids.view.ViewHelper;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.eventbus.NetEvent;
import com.psgod.ui.adapter.CarouselPhotoAdapter;
import com.psgod.ui.adapter.ViewPagerAdapter;
import com.psgod.ui.fragment.CarouselPhotoDetailFragment;
import com.psgod.ui.fragment.PhotoDetailFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/11/26 0026.
 */
public class CarouselPhotoDetailDialog extends Dialog {

    private Context mContext;
    private int mTheme;
    private int marginTop = 84;
    private ViewPager vp;

    public CarouselPhotoDetailDialog(Context context) {
        super(context, R.style.ActionSheetDialog);
        mContext = context;
        initView();
    }

    public CarouselPhotoDetailDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
        initView();
    }

    private void viewPagerBlow() {
        final AnimatorSet anim = new AnimatorSet();
        anim.setDuration(300);
        ValueAnimator yAnim = ValueAnimator.ofInt(84, 34, 0);
        yAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Integer value = (Integer) valueAnimator.getAnimatedValue();
                RelativeLayout.LayoutParams vParams = (RelativeLayout.LayoutParams) vp.getLayoutParams();
                vParams.setMargins(Utils.dpToPx(mContext, 20),
                        Utils.dpToPx(mContext, value), Utils.dpToPx(mContext, 20), 0);
                vp.setLayoutParams(vParams);
            }
        });
        ValueAnimator xAnim = ValueAnimator.ofInt(20, 0);
        xAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Integer value = (Integer) valueAnimator.getAnimatedValue();
                RelativeLayout.LayoutParams vParams = (RelativeLayout.LayoutParams) vp.getLayoutParams();
                vParams.setMargins(Utils.dpToPx(mContext, value),
                        0, Utils.dpToPx(mContext, value), 0);
                vp.setLayoutParams(vParams);
            }
        });
        anim.playTogether(yAnim, xAnim);
        anim.start();
    }

    private boolean isFlows = false;


    private void initView() {
        setContentView(R.layout.dialog_carousel_photo);
        vp = (ViewPager) findViewById(R.id.dialog_carousel_photo_vp);

        vp.setOffscreenPageLimit(3);
        vp.setPageMargin(Utils.dpToPx(mContext, 5));


        List<View> views = new ArrayList<View>();
        final ImageView view = new ImageView(mContext);
        view.setOnTouchListener(new View.OnTouchListener() {
            float DownX;
            float DownY;
            float Y;
            float oY = view.getY();
            int i = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if (!isFlows) {
                    float moveY = 0;
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            DownX = motionEvent.getX();//float DownX
                            DownY = motionEvent.getRawY();//float DownY
                            Y = view.getY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            i++;
                            if(i == 2) {
                                moveY = motionEvent.getRawY() - DownY;//y轴距离
                                ViewHelper.setTranslationY(view, moveY);
                                Log.e("Y", String.valueOf(moveY));
                                i = 0;
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            view.setY(oY);
                            viewPagerBlow();
                            break;
                    }
                    if (Utils.pxToDp(mContext, view.getY()) < -60) {
                        view.setY(oY);
                        viewPagerBlow();
                    }
//                }
                return true;
            }
        });
        ImageView view2 = new ImageView(mContext);
        ImageView view3 = new ImageView(mContext);
//        RelativeLayout.LayoutParams params = new RelativeLayout.
//                LayoutParams(Utils.dpToPx(mContext,1000), Utils.dpToPx(mContext,1000));
//        params.height = 3000;
//        p[]
//        view.setLayoutParams(params);
        view.setBackground(mContext.getResources().getDrawable(R.mipmap.scroll_first));
//        view2.setLayoutParams(params);
        view2.setBackground(mContext.getResources().getDrawable(R.color.white));
//        view3.setLayoutParams(params);
        view3.setBackground(mContext.getResources().getDrawable(R.color.white));
        views.add(view);
        views.add(view2);
        views.add(view3);
        ViewPagerAdapter adapter = new ViewPagerAdapter(views);
        vp.setAdapter(adapter);

    }

    @Override
    public void show() {
        super.show();
        getWindow().getAttributes().width = Constants.WIDTH_OF_SCREEN;
        getWindow().getAttributes().height = Constants.HEIGHT_OF_SCREEN;
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setWindowAnimations(R.style.popwindow_anim_style);

    }
}
