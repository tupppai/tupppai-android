package com.psgod.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.WeakReferenceHandler;
import com.psgod.model.LoginUser;
import com.psgod.network.request.GetUserInfoRequest;
import com.psgod.network.request.MoneyTransferRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.ui.widget.ActionBar;
import com.psgod.ui.widget.ShareButton;
import com.psgod.ui.widget.dialog.RechargeDialog;
import com.psgod.ui.widget.dialog.RechargeTypeDialog;

import org.json.JSONObject;

/**
 * Created by pires on 16/1/20.
 */
public class SettingChangeActivity extends PSGodBaseActivity {
    private static final String TAG = SettingChangeActivity.class.getSimpleName();
    private ActionBar mActionBar;
    private Button mChargeBtn;
    private Button mWithDrawBtn;

    private TextView mMoneyCount;

    private double amount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_change);

        initView();
        initListener();

    }

    private void initView() {
        mActionBar = (ActionBar) this.findViewById(R.id.actionbar);
        mChargeBtn = (Button) findViewById(R.id.recharge);
        mWithDrawBtn = (Button) findViewById(R.id.withdraw_money);
        mMoneyCount = (TextView) findViewById(R.id.money_count_tv);

        GetUserInfoRequest.Builder builder = new GetUserInfoRequest.Builder()
                .setListener(new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response != null) {
                            LoginUser.getInstance().initFromJSONObject(response);

                        }
                        mMoneyCount.setText(
                                String.format("%.2f", LoginUser.getInstance().getBalance()));
                    }
                })
                .setErrorListener(new PSGodErrorListener(this) {
                    @Override
                    public void handleError(VolleyError error) {

                    }
                });
        GetUserInfoRequest request = builder.build();
        request.setTag(TAG);
        RequestQueue requestQueue = PSGodRequestQueue
                .getInstance(this).getRequestQueue();
        requestQueue.add(request);

    }

    private void initListener() {
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
                LoginUser user = LoginUser.getInstance();
                RechargeDialog rechargeDialog = new
                        RechargeDialog(SettingChangeActivity.this, RechargeDialog.TRANSFER_WECHAT);
                rechargeDialog.show();
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
