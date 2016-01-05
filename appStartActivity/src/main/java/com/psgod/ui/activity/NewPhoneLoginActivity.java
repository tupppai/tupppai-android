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

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.WeakReferenceHandler;
import com.psgod.model.LoginUser;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.UserLoginRequest;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pires on 16/1/4.
 */
public class NewPhoneLoginActivity extends PSGodBaseActivity {
    private static final String TAG = NewPhoneLoginActivity.class.getSimpleName();
    private static final String PHONE = "PhoneNum";
    private static final int JUMP_FROM_LOGIN = 100;
    private Context mContext;

    private EditText mPhoneText;
    private EditText mPasswdText;
    private Button mLoginBtn;
    private ImageView mResetBtn;

    private CustomProgressingDialog mProgressDialog;
    private WeakReferenceHandler handler = new WeakReferenceHandler(this);
    private String mPhoneNum;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_login_phone);
        mContext = this;
        mPhoneNum = getIntent().getStringExtra(PHONE);

        initViews();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callInputPanel();
            }
        }, 200);
        initEvents();
    }

    private void initViews() {
        mPhoneText = (EditText) findViewById(R.id.input_phone);
        mPhoneText.setText(mPhoneNum);
        mPasswdText = (EditText) findViewById(R.id.input_passwd);
        mLoginBtn = (Button) findViewById(R.id.login_btn);
        mResetBtn = (ImageView) findViewById(R.id.forget_passwd);

    }

    private void initEvents() {
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    if (mProgressDialog == null) {
                        mProgressDialog = new CustomProgressingDialog(NewPhoneLoginActivity.this);
                    }
                    if (!mProgressDialog.isShowing()) {
                        mProgressDialog.show();
                    }

                    String password = mPasswdText.getText().toString()
                            .trim();

                    UserLoginRequest.Builder builder = new UserLoginRequest.Builder()
                            .setPhoneNum(mPhoneNum).setPassWord(password)
                            .setListener(loginListener)
                            .setErrorListener(errorListener);

                    UserLoginRequest request = builder.build();
                    request.setTag(TAG);
                    RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                            NewPhoneLoginActivity.this).getRequestQueue();
                    requestQueue.add(request);
                }
            }
        });

        mResetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NewPhoneLoginActivity.this,NewResetPasswdActivity.class);
                intent.putExtra(PHONE,mPhoneNum);
                startActivity(intent);
            }
        });
    }

    private Response.Listener<JSONObject> loginListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            if (response != null) {
                // 取消等待框
                if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                try {
                    if (response.getInt("status") == 1) {
                        // 存储服务端返回的用户信息到sp
                        LoginUser.getInstance().initFromJSONObject(response);

                        Toast.makeText(NewPhoneLoginActivity.this, "登录成功",
                                Toast.LENGTH_SHORT).show();

                        Bundle extras = new Bundle();
                        extras.putInt(Constants.IntentKey.ACTIVITY_JUMP_FROM,
                                JUMP_FROM_LOGIN);
                        MainActivity.startNewActivityAndFinishAllBefore(
                                NewPhoneLoginActivity.this,
                                MainActivity.class.getName(), extras);
                    } else if (response.getInt("status") == 2) {
                        // 密码错误
                        Toast.makeText(NewPhoneLoginActivity.this, "密码错误",
                                Toast.LENGTH_SHORT).show();
                        mPasswdText.setText("");
                        mPasswdText.requestFocus();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    };

    private void callInputPanel() {
        // 唤起输入键盘 并输入框取得焦点
        mPasswdText.setFocusableInTouchMode(true);
        mPasswdText.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mPasswdText, 0);
    }

    private PSGodErrorListener errorListener = new PSGodErrorListener() {
        @Override
        public void handleError(VolleyError error) {
            // TODO Auto-generated method stub
            if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    };

    private boolean validate() {
        // 手机号校验
        if (Utils.isNull(mPhoneText)) {
            Toast.makeText(NewPhoneLoginActivity.this, "请填写手机号码", Toast.LENGTH_SHORT)
                    .show();
            mPhoneText.requestFocus();
            return false;
        }
        String phoneNum = mPhoneText.getText().toString().trim();
        if (!Utils.matchPhoneNum(phoneNum)) {
            Toast.makeText(NewPhoneLoginActivity.this, "电话格式不正确", Toast.LENGTH_SHORT)
                    .show();
            mPhoneText.requestFocus();
            return false;
        }

        if (Utils.isNull(mPasswdText)) {
            Toast.makeText(NewPhoneLoginActivity.this, "请填写登录密码", Toast.LENGTH_SHORT)
                    .show();
            mPasswdText.requestFocus();
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
