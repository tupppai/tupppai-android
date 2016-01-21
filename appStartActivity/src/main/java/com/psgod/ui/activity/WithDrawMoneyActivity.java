package com.psgod.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.psgod.R;
import com.psgod.Utils;

/**
 * Created by pires on 16/1/21.
 */
public class WithDrawMoneyActivity extends PSGodBaseActivity {
    private static final String TAG = WithDrawMoneyActivity.class.getSimpleName();
    private Context mContext;
    private Button mCompleteBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.addActivity(WithDrawMoneyActivity.this);
        setContentView(R.layout.activity_withdraw_money);
        mContext = this;
        mCompleteBtn = (Button) this.findViewById(R.id.withdraw_complete_btn);

        mCompleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.finishActivity();
            }
        });
    }
}
