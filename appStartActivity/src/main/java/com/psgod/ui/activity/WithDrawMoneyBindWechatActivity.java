package com.psgod.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.psgod.R;
import com.psgod.Utils;

/**
 * Created by pires on 16/1/21.
 */
public class WithDrawMoneyBindWechatActivity extends PSGodBaseActivity {

    private static final String TAG = WithDrawMoneyBindWechatActivity.class.getSimpleName();

    public static final String AMOUNT = "amount";

    private double amount;

    private Button mBindBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.addActivity(WithDrawMoneyBindWechatActivity.this);
        setContentView(R.layout.activity_withdraw_money_bind_wechat);

        Intent intent = getIntent();
        amount = intent.getDoubleExtra(AMOUNT, 0);

        initView();
        initListener();

    }

    private void initView() {
        mBindBtn = (Button) this.findViewById(R.id.bind_weixin_btn);

    }

    private void initListener() {
        mBindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WithDrawMoneyBindWechatActivity.this, WithDrawMoneyActivity.class);
                startActivity(intent);
            }
        });
    }
}
