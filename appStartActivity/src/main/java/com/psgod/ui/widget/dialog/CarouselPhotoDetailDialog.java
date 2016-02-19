package com.psgod.ui.widget.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.PhotoReplyRequest;
import com.psgod.ui.activity.PSGodBaseActivity;
import com.psgod.ui.adapter.ViewPagerAdapter;
import com.psgod.ui.view.CarouselPhotoDetailView;
import com.psgod.ui.view.PhotoItemView;
import com.psgod.ui.widget.FollowImage;

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
    private long categoryId = -1;
    ViewPagerAdapter adapter;
    private CustomProgressingDialog progressingDialog;

    public CarouselPhotoDetailDialog(Context context, long askId, long replyId) {
        super(context, R.style.CaroPhotoDialog);
        mContext = context;
        this.askId = askId;
        this.replyId = replyId;
    }

    public CarouselPhotoDetailDialog(Context context, long askId, long replyId, long categoryId) {
        super(context, R.style.CaroPhotoDialog);
        mContext = context;
        this.askId = askId;
        this.replyId = replyId;
        this.categoryId = categoryId;
    }

    public CarouselPhotoDetailDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
    }

    View parentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initView() {
        parentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_carousel_photo, null);
        setContentView(parentView);
        parentView.
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismiss();
                    }
                });
        vp = (ViewPager) findViewById(R.id.dialog_carousel_photo_vp);

        vp.setOffscreenPageLimit(3);
        vp.setPageMargin(Utils.dpToPx(mContext, 10));
        views = new ArrayList<View>();
        adapter = new ViewPagerAdapter(views);
        vp.setAdapter(adapter);
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int length = views.size();
                for (int i = 0; i < length; i++) {
                    if (i == position) {
                        views.get(i).setY(0);
                    } else {
                        views.get(i).setY(Utils.dpToPx(mContext, 10));
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    private void initData() {
        PhotoReplyRequest.Builder builder = new PhotoReplyRequest.Builder()
                .setId(askId).setPid(replyId).setPage(1)
                .setListener(initDataListener);
        if(categoryId != -1){
            builder.setCategoryId(categoryId);
        }
        PhotoReplyRequest request = builder.build();
        request.setTag(mContext.getClass().getSimpleName());
//        request.setTag(this.getClass().getName());
        RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                mContext).getRequestQueue();
        requestQueue.add(request);
    }

    @Override
    public void show() {
        initView();
        initData();
        getWindow().getAttributes().width = -1;
        getWindow().getAttributes().height = -1;
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setWindowAnimations(R.style.popwindow_anim_style);
        progressingDialog = new CustomProgressingDialog(mContext);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        progressingDialog.show();
    }

//        AttentionComponentView

    Response.Listener<List<PhotoItem>> initDataListener = new Response.Listener<List<PhotoItem>>() {
        @Override
        public void onResponse(final List<PhotoItem> items) {
            if (views.size() > 0) {
                views.clear();
            }

            //统一相同用户的关注关系
            FollowImage.OnFollowChangeListener onFollowChangeListener =  new FollowImage.OnFollowChangeListener() {
                @Override
                public void onFocusChange(long uid, boolean focusStatus,long pid) {
                    int length = items.size();
                    for (int i = 0; i < length; i++) {
                        if (items.get(i).getUid() == uid && items.get(i).getPid() != pid) {
                            items.get(i).setIsFollowed(focusStatus);
                        }
                    }
                }
            };
            for (PhotoItem item : items) {
                if (item.getUploadImagesList().size() == 2 && item.getType() == PhotoItem.TYPE_ASK) {
                    for (int i = 0; i < 2; i++) {
                        item.setImageURL(item.getUploadImagesList().get(i).mImageUrl);
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
                    continue;
                }
                CarouselPhotoDetailView view = new CarouselPhotoDetailView(getContext(), item);
                view.setVp(vp);
                view.setOnEndListener(new CarouselPhotoDetailView.OnEndListener() {
                    @Override
                    public void onEnd() {
                        CarouselPhotoDetailDialog.this.dismiss();
                    }
                });
                view.setOnFollowChangeListener(onFollowChangeListener);
                views.add(view);
            }
            adapter.notifyDataSetChanged();
            if (items.size() > 1) {
                if (askId == replyId) {
                    vp.setCurrentItem(0);
                } else {
                    if (items.get(0).getUploadImagesList().size() == 1) {
                        vp.setCurrentItem(1);
                    } else {
                        vp.setCurrentItem(2);
                    }
                }
            }
            if (progressingDialog != null && progressingDialog.isShowing()) {
                progressingDialog.dismiss();
            }
            if(!((PSGodBaseActivity)mContext).isFinishing()) {
                CarouselPhotoDetailDialog.super.show();
            }
        }
    };

}
