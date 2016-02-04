package com.psgod.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.LoginUser;
import com.psgod.network.request.GetUserInfoRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;

import org.json.JSONObject;

/**
 * Created by Administrator on 2016/2/4 0004.
 */
public class WithdrawDepositActivity extends PSGodBaseActivity {
    private static final String TAG = WithdrawDepositActivity.class.getSimpleName();

    private EditText mMoneyEdit;
    private TextView mBalanceTxt;
    private Button mSure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.addActivity(this);
        setContentView(R.layout.activity_withdraw_deposit);

        initView();
        initListener();
        refresh();
    }

    private void initView() {
        mMoneyEdit = (EditText) findViewById(R.id.withdraw_deposit_money_edit);
        mBalanceTxt = (TextView) findViewById(R.id.withdraw_deposit_money_balance);
        mSure = (Button) findViewById(R.id.withdraw_deposit_sure);
    }

    private void initListener() {

        mSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra(WithdrawPhoneVerifyActivity.AMOUNT_DOUBLE,
                        Double.parseDouble(mMoneyEdit.getText().toString()));
                if(LoginUser.getInstance().isBoundWechat()){
                    intent.setClass(WithdrawDepositActivity.this,
                            WithdrawPhoneVerifyActivity.class);
                }else{
                    intent.setClass(WithdrawDepositActivity.this,
                            WithDrawMoneyBindWechatActivity.class);
                }
                startActivity(intent);
            }
        });

        mMoneyEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (s.toString().contains(".")) {
                    if (s.length() - 1 - s.toString().indexOf(".") > 2) {
                        s = s.toString().subSequence(0,
                                s.toString().indexOf(".") + 3);
                        mMoneyEdit.setText(s);
                        mMoneyEdit.setSelection(s.length());
                    }
                }
                if (s.toString().trim().substring(0).equals(".")) {
                    s = "0" + s;
                    mMoneyEdit.setText(s);
                    mMoneyEdit.setSelection(2);
                }

                if (s.toString().startsWith("0")
                        && s.toString().trim().length() > 1) {
                    if (!s.toString().substring(1, 2).equals(".")) {
                        mMoneyEdit.setText(s.subSequence(0, 1));
                        mMoneyEdit.setSelection(1);
                        return;
                    }
                }
                if(s.length()>= 1){
                    double money = Double.parseDouble(s.toString());
                    if(money >= 1 && money <= LoginUser.getInstance().getBalance()){
                        mSure.setBackgroundColor(Color.parseColor("#FFEF04"));
                        mSure.setTextColor(Color.parseColor("#000000"));
                        mSure.setEnabled(true);
                    }else{
                        mSure.setBackgroundColor(Color.parseColor("#70FFEF04"));
                        mSure.setTextColor(Color.parseColor("#70000000"));
                        mSure.setEnabled(false);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }

        });
    }

    private void refresh() {
        mBalanceTxt.setText("正在获取账户余额");
        GetUserInfoRequest.Builder builder = new GetUserInfoRequest.Builder()
                .setListener(new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response != null) {
                            LoginUser.getInstance().initFromJSONObject(response);

                        }
                        mBalanceTxt.setText(
                                String.format("¥ %.2f 元", LoginUser.getInstance().getBalance()));
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

}
