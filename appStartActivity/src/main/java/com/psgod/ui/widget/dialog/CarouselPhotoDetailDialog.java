package com.psgod.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.psgod.Constants;
import com.psgod.R;
import com.psgod.ui.adapter.CarouselPhotoAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/11/26 0026.
 */
public class CarouselPhotoDetailDialog extends Dialog {

    private Context mContext;
    private int mTheme;

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

    private void initView() {
        setContentView(R.layout.dialog_carousel_photo);
        ViewPager vp = (ViewPager) findViewById(R.id.dialog_carousel_photo_vp);
        List<View> views = new ArrayList<View>();
        View view = new View(mContext);
        view.setBackground(mContext.getResources().getDrawable(R.drawable.shape_caro_photo_back));
        RelativeLayout.LayoutParams params = new RelativeLayout.
                LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(params);
        views.add(view);
        CarouselPhotoAdapter adapter = new CarouselPhotoAdapter(views);
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
