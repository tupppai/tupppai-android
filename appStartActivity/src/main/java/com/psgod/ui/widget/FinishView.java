package com.psgod.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.psgod.ui.activity.PSGodBaseActivity;

/**
 * Created by Administrator on 2015/12/8 0008.
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
