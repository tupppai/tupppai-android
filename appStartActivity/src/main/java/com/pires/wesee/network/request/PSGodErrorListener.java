package com.pires.wesee.network.request;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.pires.wesee.Logger;
import com.pires.wesee.Utils;
import com.pires.wesee.ui.activity.MainActivity;

public abstract class PSGodErrorListener implements ErrorListener {
    private static final String TAG = PSGodErrorListener.class.getSimpleName();
    protected String tag;
    protected Object parent;

    private PSGodErrorListener() {
        tag = TAG;
    }

    public PSGodErrorListener(Object parent) {
        this.parent = parent;
    }

    public PSGodErrorListener(String tag) {
        this.tag = tag;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Utils.hideProgressDialog();
        String msg = error.getMessage();
        if ((msg != null) && (!msg.equals("")) && (msg.startsWith("java.net.UnknownHostException"))) {
            msg = "网络连接不可用，请稍后再试";
            Utils.showDebugToast(msg);
        } else if ((msg != null) && (!msg.equals("")) && msg.equals("ThirdLogin")) {
            // 第三方登录，未注册手机号时，错误信息
            if (parent instanceof Activity) {
                if (parent instanceof MainActivity) {

                } else {
                    ((Activity) parent).finish();
                }
            } else if (parent instanceof Fragment) {
                Activity activity = ((Fragment) parent).getActivity();
                if( !(activity instanceof MainActivity) ){
                    activity.finish();
                }
            } else if (parent instanceof Dialog) {
                ((Dialog) parent).dismiss();
            }
        } else {
            msg = TextUtils.isEmpty(msg) ? "网络不稳定，请稍后再试" : msg;
            Utils.showDebugToast(msg);
        }
//		msg = TextUtils.isEmpty(msg) ? " Empty message" : "网络连接不可用，请稍后再试";
//		msg = TextUtils.isEmpty(msg) ? " Empty message" : msg;
        Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_COLOR, tag, msg);

        // 错误提示
        // Utils.showDebugToast("请求失败，请稍后再试");
        handleError(error);
    }

    public abstract void handleError(VolleyError error);
}
