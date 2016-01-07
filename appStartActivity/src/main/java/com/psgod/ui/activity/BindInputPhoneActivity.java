package com.psgod.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.psgod.R;

/**
 * Created by pires on 16/1/7.
 */
public class BindInputPhoneActivity extends PSGodBaseActivity {

    private static final String TAG = BindInputPhoneActivity.class.getSimpleName();

    private Context mContext;
    private Button mNextBtn;

    @Override
    public void onCreate(Bundle savedInstancestate) {
        super.onCreate(savedInstancestate);

        setContentView(R.layout.activity_bind_input_phone);

        mNextBtn = (Button) findViewById(R.id.next_btn);

        mContext = this;

        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BindInputPhoneActivity.this,BindPhoneActivity.class);
                startActivity(intent);
                BindInputPhoneActivity.this.finish();
            }
        });
    }
}
