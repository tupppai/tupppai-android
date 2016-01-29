package com.psgod.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.psgod.R;
import com.psgod.Utils;
import com.psgod.WeakReferenceHandler;

/**
 * Created by pires on 16/1/7.
 *
 * 绑定手机号，输入手机号
 */
public class BindInputPhoneActivity extends PSGodBaseActivity {

    private static final String TAG = BindInputPhoneActivity.class.getSimpleName();
    private static final String PHONENUM = "PhoneNum";
    private Context mContext;

    private ImageView mBackBtn;
    private EditText mPhoneText;
    private Button mNextBtn;

    private WeakReferenceHandler handler = new WeakReferenceHandler(this);

    @Override
    public void onCreate(Bundle savedInstancestate) {
        super.onCreate(savedInstancestate);
        setContentView(R.layout.activity_bind_input_phone);
        mContext = this;

        if(Utils.isBindInputPhoneShow()){
            finish();
        }
        Utils.setBindInputPhoneShow(true);

        initViews();
        initEvents();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callInputPanel();
            }
        }, 200);

    }

    private void initViews() {
        mBackBtn = (ImageView) findViewById(R.id.ic_back);
        mPhoneText = (EditText) findViewById(R.id.input_phone);
        mNextBtn = (Button) findViewById(R.id.next_btn);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.setBindInputPhoneShow(false);
    }

    private void initEvents() {
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BindInputPhoneActivity.this.finish();
            }
        });

        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    Intent intent = new Intent(BindInputPhoneActivity.this,BindPhoneActivity.class);
                    intent.putExtra(PHONENUM,mPhoneText.getText().toString().trim());
                    startActivity(intent);
                    BindInputPhoneActivity.this.finish();
                }

            }
        });
    }

    private boolean validate() {
        // 手机号校验
        if (Utils.isNull(mPhoneText)) {
            Toast.makeText(BindInputPhoneActivity.this, "请填写手机号码", Toast.LENGTH_SHORT)
                    .show();
            mPhoneText.requestFocus();
            return false;
        }
        String phoneNum = mPhoneText.getText().toString().trim();
        if (!Utils.matchPhoneNum(phoneNum)) {
            Toast.makeText(BindInputPhoneActivity.this, "电话格式不正确", Toast.LENGTH_SHORT)
                    .show();
            mPhoneText.requestFocus();
            return false;
        }
        return true;
    }

    private void callInputPanel() {
        // 唤起输入键盘 并输入框取得焦点
        mPhoneText.setFocusableInTouchMode(true);
        mPhoneText.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mPhoneText, 0);
    }
}
