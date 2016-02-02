package com.psgod.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.MoneyTransfer;

/**
 * Created by pires on 16/1/21.
 */
public class WithDrawMoneyActivity extends PSGodBaseActivity {
    private static final String TAG = WithDrawMoneyActivity.class.getSimpleName();
    private Button mCompleteBtn;
    private TextView mWeixin;
    private TextView mCount;

    private MoneyTransfer moneyTransfer;

    public static final String RESULT = "result";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.addActivity(WithDrawMoneyActivity.this);
        setContentView(R.layout.activity_withdraw_money);

        Intent intent = getIntent();
        Object obj = intent.getSerializableExtra(RESULT);
        if (obj instanceof MoneyTransfer) {
            moneyTransfer = (MoneyTransfer) obj;
        } else {
            moneyTransfer = new MoneyTransfer();
        }

        initView();
        initListener();


    }

    private void initView() {
//        mCompleteBtn = (Button) this.findViewById(R.id.withdraw_complete_btn);
        mCount = (TextView) findViewById(R.id.withdraw_count_tv);
        mCount.setText(String.format("￥%.2f",(float)moneyTransfer.getAmount()/100f));
//        mWeixin = (TextView) findViewById(R.id.withdraw_weixin_tv);
//        mWeixin.setText(moneyTransfer.getExtra() != null ?
//                moneyTransfer.getExtra().getUser_name() : "");
    }

    private void initListener() {
        mCompleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.finishActivity();
            }
        });
    }
}
