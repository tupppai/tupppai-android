package com.psgod.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.WeakReferenceHandler;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.RegisterCheckPhoneNumRequest;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

/**
 * Created by pires on 16/1/4.
 */
public class NewLoginInputPhoneActivity extends PSGodBaseActivity{
    private static final String TAG = NewLoginInputPhoneActivity.class.getSimpleName();
    private static final String PHONE = "PhoneNum";

    private EditText mPhoneEdit;
    private Button mNextButton;

    private CustomProgressingDialog mProgressDialog;
    private WeakReferenceHandler handler = new WeakReferenceHandler(this);
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_login_input_phone);

        mPhoneEdit = (EditText) findViewById(R.id.phone_edit);
        mNextButton = (Button) findViewById(R.id.next_btn);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callInputPanel();
            }
        },200);

        initEvents();

    }

    private void initEvents() {
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    // 显示等待对话框
                    if (mProgressDialog == null) {
                        mProgressDialog = new CustomProgressingDialog(
                                NewLoginInputPhoneActivity.this);
                    }
                    if (!mProgressDialog.isShowing()) {
                        mProgressDialog.show();
                    }

                    String phoneNum = mPhoneEdit.getText().toString().trim();

                    RegisterCheckPhoneNumRequest.Builder builder = new RegisterCheckPhoneNumRequest.Builder()
                            .setPhoneNumber(phoneNum)
                            .setListener(checkPhoneListener)
                            .setErrorListener(errorListener);
                    RegisterCheckPhoneNumRequest request = builder.build();
                    request.setTag(TAG);
                    RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                            NewLoginInputPhoneActivity.this).getRequestQueue();
                    requestQueue.add(request);
                }
            }
        });
    }

    // 检测手机号码是否注册过接口
    private Response.Listener<Boolean> checkPhoneListener = new Response.Listener<Boolean>() {
        @Override
        public void onResponse(Boolean response) {
            if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            if (response) {
                Intent intent = new Intent(NewLoginInputPhoneActivity.this,NewPhoneLoginActivity.class);
                intent.putExtra(PHONE,mPhoneEdit.getText().toString().trim());
                startActivity(intent);
            } else {
                Intent intent = new Intent(NewLoginInputPhoneActivity.this,NewRegisterPhoneActivity.class);
                intent.putExtra(PHONE,mPhoneEdit.getText().toString().trim());
                startActivity(intent);
            }
        }
    };

    private PSGodErrorListener errorListener = new PSGodErrorListener(
            RegisterCheckPhoneNumRequest.class.getSimpleName()) {
        @Override
        public void handleError(VolleyError error) {
            if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    };

    private void callInputPanel() {
        // 唤起输入键盘 并输入框取得焦点
        mPhoneEdit.setFocusableInTouchMode(true);
        mPhoneEdit.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mPhoneEdit, 0);
    }

    private boolean validate() {
        // 手机号校验
        if (Utils.isNull(mPhoneEdit)) {
            Toast.makeText(NewLoginInputPhoneActivity.this, "请填写手机号码", Toast.LENGTH_SHORT)
                    .show();
            mPhoneEdit.requestFocus();
            return false;
        }
        String phoneNum = mPhoneEdit.getText().toString().trim();
        if (!Utils.matchPhoneNum(phoneNum)) {
            Toast.makeText(NewLoginInputPhoneActivity.this, "电话格式不正确", Toast.LENGTH_SHORT)
                    .show();
            mPhoneEdit.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * 暂停所有的下载
     */
    @Override
    public void onStop() {
        super.onStop();
        RequestQueue requestQueue = PSGodRequestQueue.getInstance(this)
                .getRequestQueue();
        requestQueue.cancelAll(this);
    }
}
