package com.psgod.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.psgod.R;
import com.psgod.Utils;
import com.psgod.ui.widget.ActionBar;
import com.psgod.ui.widget.dialog.RechargeTypeDialog;

/**
 * Created by pires on 16/1/20.
 */
public class SettingChangeActivity extends PSGodBaseActivity {
    private static final String TAG = SettingChangeActivity.class.getSimpleName();
    private Context mContext;
    private ActionBar mActionBar;
    private Button mChargeBtn;
    private Button mWithDrawBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.initializeActivity();
        Utils.addActivity(SettingChangeActivity.this);
        setContentView(R.layout.activity_setting_change);

        mActionBar = (ActionBar) this.findViewById(R.id.actionbar);
        mChargeBtn = (Button) findViewById(R.id.recharge);
        mWithDrawBtn = (Button) findViewById(R.id.withdraw_money);

        mChargeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RechargeTypeDialog dialog = new RechargeTypeDialog(SettingChangeActivity.this);
                dialog.show();
            }
        });

        mWithDrawBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingChangeActivity.this,WithDrawMoneyBindWechatActivity.class);
                startActivity(intent);
            }
        });
    }
}
