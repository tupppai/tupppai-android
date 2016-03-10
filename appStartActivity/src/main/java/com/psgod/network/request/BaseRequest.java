package com.psgod.network.request;

import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.psgod.Constants;
import com.psgod.CustomToast;
import com.psgod.PSGodApplication;
import com.psgod.UserPreferences;
import com.psgod.Utils;
import com.psgod.model.LoginUser;
import com.psgod.ui.activity.BindInputPhoneActivity;
import com.psgod.ui.activity.NewLoginInputPhoneActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseRequest<T> extends Request<T> {
    public static final String PSGOD_BASE_TEST_URL = "http://tapi.tupppai.com/"; // 测试环境

    public static final String PSGOD_BASE_RELEASE_URL = "http://api.qiupsdashen.com/"; // 正式环境

    public static String PSGOD_BASE_URL = PSGOD_BASE_TEST_URL;

    protected static final String PROTOCOL_CHARSET = "utf-8";

    private Map<String, String> mHeader = new HashMap<String, String>(1);

    protected Listener<T> mListener;

    public BaseRequest(int method, String url, Listener<T> listener,
                       ErrorListener errorListener) {
        super(method, dealStr(url), errorListener);
        String[] thumb = url.split("\\?");
        if (thumb.length == 2) {

        }
        mListener = listener;
    }

    // 重写getHeaders 请求头添加token
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        String token = UserPreferences.TokenVerify.getToken();
        mHeader.put("Cookie", "token=" + token + ";");
        return mHeader;
    }

    // 重写getRetryPolicy 设置服务器超时时间
    // @Override
    // public RetryPolicy getRetryPolicy() {
    // RetryPolicy retryPolicy = new DefaultRetryPolicy(
    // 10000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    // return retryPolicy;
    // }

    @Override
    protected void deliverResponse(T response) {
        if (mListener != null) {
            mListener.onResponse(response);
        }
    }

    /**
     * 该方法在子线程里调用
     */
    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers,
                            PROTOCOL_CHARSET));
            JSONObject jsonObj = new JSONObject(jsonString);
            int ret = jsonObj.getInt("ret");
            String info = jsonObj.getString("info");
            if (ret == 2) {
                // 判断用户是否绑定手机，没绑定则弹出绑定框
                if (LoginUser.getInstance().getPhoneNum().equals("0")) {
                    Intent intent = new Intent(PSGodApplication.getAppContext(),
                            BindInputPhoneActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PSGodApplication.getAppContext().startActivity(intent);
                    return Response.error(new VolleyError("ThirdLogin"));
                } else {
                    // ret=2表示token失效 需重新验证
                    UserPreferences.TokenVerify.setToken(""); // 将本地token清空
                    Constants.IS_FOCUS_FRAGMENT_CREATED = false;
                    Constants.IS_HOME_FRAGMENT_CREATED = false;
                    Constants.IS_MESSAGE_FRAGMENT_CREATED = false;
                    Constants.IS_USER_FRAGMENT_CREATED = false;
                    Constants.IS_INPROGRESS_FRAGMENT_CREATED = false;

                    // WelcomeActivity.startNewActivityAndFinishAllBefore(
                    // PSGodApplication.getAppContext(),
                    // WelcomeActivity.class.getName(), null);

                    Intent intent = new Intent(PSGodApplication.getAppContext(),
                            NewLoginInputPhoneActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PSGodApplication.getAppContext().startActivity(intent);
                    return Response.error(new VolleyError(info));
                }

            } else if (ret == 0) {
                return Response.error(new VolleyError(info));
            } else {
                T result = doParseNetworkResponse(jsonObj);
                if (result == null) {
                    return Response
                            .error(new VolleyError("The result is null"));
                }

                // 获取返回中token并存储cookie
                try {
                    saveCookie(jsonObj);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                }

                return Response.success(result,
                        HttpHeaderParser.parseCacheHeaders(response));
            }
        } catch (UnsupportedEncodingException e) {
//            CustomToast.showError(PSGodApplication.getAppContext(),
//                    "当前版本过低，请更新版本", Toast.LENGTH_LONG);
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
//            Looper.prepare();
//            CustomToast.showError(PSGodApplication.getAppContext(),
//                    "当前版本过低，请更新版本", Toast.LENGTH_LONG);
//            Looper.loop();
            return Response.error(new ParseError(je));
        }
    }

    //根据url参数配置验证信息
    public static String dealStr(String url) {
        StringBuilder sb = new StringBuilder();
        String result = "";
        if (url.indexOf("?") != -1) {
            String[] thumb = url.split("\\?");
            String[] keys = thumb[1].split("&");
            Arrays.sort(keys);
            for (String s : keys) {
                if (s.indexOf("=") != -1) {
                    sb.append(s.split("=").length >= 2?s.split("=")[1]:"");
                }
            }
            Calendar cal = Calendar.getInstance();
            sb.append(Utils.toMd5(Constants.SIGN));
            sb.append(cal.get(Calendar.DATE));
            result = String.format("&v=2&verify=%s",
                    Utils.toMd5(Utils.toMd5(
                            sb.toString().toLowerCase()).toLowerCase()).toLowerCase());
        }
        return url + result;
    }

    /**
     * 保存返回的cookie信息
     *
     * @throws JSONException
     * @params JSONObject
     * @author brandwang
     */
    public static void saveCookie(JSONObject response) throws JSONException {
        String token = response.getString("token");
        if (!token.equals("")) {
            // 将token存入sp
            UserPreferences.TokenVerify.setToken(token);
        }
        // TODO token过期失效处理
    }

    abstract protected T doParseNetworkResponse(JSONObject reponse)
            throws UnsupportedEncodingException, JSONException;

    //post请求添加验证信息参数
    public Map<String, String> getPackParams(Map<String, String> map) {
        if (map != null && map.size() > 0) {
            StringBuilder sb = new StringBuilder();
            String[] thumbs = new String[map.size()];
            String result = "";
            int i = 0;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                thumbs[i] = String.format("%s=%s", entry.getKey(), entry.getValue());
                i++;
            }
            Arrays.sort(thumbs);
            for (String str : thumbs) {
                sb.append(str.split("=").length < 2 ? "" : str.split("=")[1]);
            }
            Calendar cal = Calendar.getInstance();
            sb.append(Utils.toMd5(Constants.SIGN));
            sb.append(cal.get(Calendar.DATE));

            result = Utils.toMd5(Utils.toMd5(
                            sb.toString().toLowerCase()).toLowerCase()).toLowerCase();
            map.put("v","2");
            map.put("verify",result);
        }
        return map;
    }
}
