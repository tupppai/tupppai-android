package com.psgod.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.psgod.BitmapUtils;
import com.psgod.Constants;
import com.psgod.CustomToast;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.eventbus.DialogEvent;
import com.psgod.model.ImageData;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.PhotoReplyRequest;
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
    private ViewPager vp;
    private List<View> views;
    private long askId;
    private long replyId;
    ViewPagerAdapter adapter;

    public CarouselPhotoDetailDialog(Context context, long askId, long replyId) {
        super(context, R.style.ActionSheetDialog);
        mContext = context;
        this.askId = askId;
        this.replyId = replyId;
    }

    public CarouselPhotoDetailDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
    }

    View parentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        initView();
    }

    private void initView() {
        parentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_carousel_photo, null);
        setContentView(parentView);
        vp = (ViewPager) findViewById(R.id.dialog_carousel_photo_vp);

        vp.setOffscreenPageLimit(3);
        vp.setPageMargin(Utils.dpToPx(mContext, 10));
        views = new ArrayList<View>();
        adapter = new ViewPagerAdapter(views);
        vp.setAdapter(adapter);

        initData();
    }

    private void initData() {
        PhotoReplyRequest.Builder builder = new PhotoReplyRequest.Builder()
                .setId(askId).setPid(replyId).setPage(1)
                .setListener(initDataListener);

        PhotoReplyRequest request = builder.build();
        request.setTag(this.getClass().getName());
        RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                mContext).getRequestQueue();
        requestQueue.add(request);
    }

    @Override
    public void show() {
        super.show();
        getWindow().getAttributes().width = -1;
        getWindow().getAttributes().height = -1;
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setWindowAnimations(R.style.popwindow_anim_style);

    }

    Response.Listener<List<PhotoItem>> initDataListener = new Response.Listener<List<PhotoItem>>() {
        @Override
        public void onResponse(List<PhotoItem> items) {
            if (views.size() > 0) {
                views.clear();
            }
            for (PhotoItem item : items) {
                CarouselPhotoDetailView view = new CarouselPhotoDetailView(getContext(), item);
                view.setVp(vp);
                view.setOnEndListener(new CarouselPhotoDetailView.OnEndListener() {
                    @Override
                    public void onEnd() {
                        CarouselPhotoDetailDialog.this.dismiss();
                    }
                });
                views.add(view);
            }
            adapter.notifyDataSetChanged();
        }
    };


}
