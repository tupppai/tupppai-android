package com.psgod.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.eventbus.DialogEvent;
import com.psgod.ui.adapter.ViewPagerAdapter;
import com.psgod.ui.view.CarouselPhotoDetailView;
import com.psgod.ui.widget.StopViewPager;

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

    public void onEventMainThread(DialogEvent event) {
        ((ViewGroup) parentView).setClipChildren(true);
    }


    View parentView;

    private void initView() {
        parentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_carousel_photo, null);
        setContentView(parentView);
        vp = (ViewPager) findViewById(R.id.dialog_carousel_photo_vp);

        vp.setOffscreenPageLimit(3);
        vp.setPageMargin(Utils.dpToPx(mContext, 10));

        List<View> views = new ArrayList<View>();
        for (int i = 0; i < 20; i++) {
            CarouselPhotoDetailView view = new CarouselPhotoDetailView(mContext);
            view.setVp(vp);
            view.setOnEndListener(new CarouselPhotoDetailView.OnEndListener() {
                @Override
                public void onEnd() {
                    CarouselPhotoDetailDialog.this.dismiss();
                }
            });
            views.add(view);
        }
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
