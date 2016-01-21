package com.psgod.ui.activity;

import android.content.Context;
import android.os.Bundle;

import com.psgod.R;

/**
 * Created by pires on 16/1/21.
 */
public class WithDrawMoneyActivity extends PSGodBaseActivity {
    private static final String TAG = WithDrawMoneyActivity.class.getSimpleName();
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw_money);
    }
}
