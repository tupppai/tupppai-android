package com.psgod.ui.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.psgod.R;

/**
 * Created by pires on 16/1/4.
 */
public class NewLoginInputPhoneActivity extends PSGodBaseActivity{
    private static final String TAG = NewLoginInputPhoneActivity.class.getSimpleName();

    private EditText mPhoneEdit;
    private Button mNextButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_login_input_phone);

        mPhoneEdit = (EditText) findViewById(R.id.phone_edit);
        mNextButton = (Button) findViewById(R.id.next_btn);
    }
}
