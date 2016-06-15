package com.pires.wesee.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.pires.wesee.eventbus.RefreshEvent;
import com.pires.wesee.ui.activity.PSGodBaseActivity;
import com.pires.wesee.ui.fragment.HomePageDynamicFragment;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2015/12/8 0008.
 * 回退键封装
 */
public class FinishView extends ImageView {
    public FinishView(Context context) {
        super(context);
        init();
    }

    private void init() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getContext() instanceof PSGodBaseActivity) {
                    ((PSGodBaseActivity) getContext()).finish();
                    EventBus.getDefault().post(new RefreshEvent(HomePageDynamicFragment.class.getName()));
                }
            }
        });
    }

    public FinishView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FinishView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

}
