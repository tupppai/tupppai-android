package com.psgod.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.psgod.R;
import com.psgod.Utils;
import com.psgod.WeakReferenceHandler;
import com.psgod.model.LoginUser;
import com.psgod.ui.widget.ActionBar;
import com.psgod.ui.widget.dialog.RechargeTypeDialog;

/**
 * Created by pires on 16/1/20.
 */
public class SettingChangeActivity extends PSGodBaseActivity {
    private static final String TAG = SettingChangeActivity.class.getSimpleName();
    private Context mContext;
    private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);
    private ActionBar mActionBar;
    private Button mChargeBtn;
    private Button mWithDrawBtn;

    private TextView mMoneyCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.initializeActivity();
        Utils.addActivity(SettingChangeActivity.this);
        setContentView(R.layout.activity_setting_change);

        initView();
        initListener();

    }

    private void initView() {
        mActionBar = (ActionBar) this.findViewById(R.id.actionbar);
        mChargeBtn = (Button) findViewById(R.id.recharge);
        mWithDrawBtn = (Button) findViewById(R.id.withdraw_money);
        mMoneyCount = (TextView) findViewById(R.id.money_count_tv);

        mMoneyCount.setText(
                String.format("%.2f", LoginUser.getInstance().getBalance()));
    }

    private void initListener() {
        mChargeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RechargeTypeDialog dialog = new RechargeTypeDialog(SettingChangeActivity.this, mHandler);
                dialog.show();
            }
        });

        mWithDrawBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingChangeActivity.this, WithDrawMoneyBindWechatActivity.class);
                startActivity(intent);
            }
        });

        mActionBar.setRightBtnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingChangeActivity.this, ChangeDetailActivity.class);
                startActivity(intent);
            }
        });
    }


}
