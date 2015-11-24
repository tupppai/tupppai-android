package com.psgod;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.psgod.model.PhotoItem;
import com.psgod.model.SearchWork;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

/**
 * 辅助类
 *
 * @author Rayal
 */
public final class Utils {
    /**
     * 判断参数是否有null
     *
     * @param params
     * @return
     */
    public static boolean hasNullAruguments(Object... params) {
        for (Object param : params) {
            if (param == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * dp转换成像素
     *
     * @param context
     * @param dpValue
     * @return
     */
    public static int dpToPx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static float pxToDpFloat(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return pxValue / scale + 0.5f;
    }

    public static float pxToDpOrigin(Context context, float dpValue) {
//        final float scale = context.getResources().getDisplayMetrics().density;
        return dpValue / 3 + 0.5f;
    }

    /**
     * 判断两浮点数是否相等
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean isFloatEquals(float a, float b) {
        if (Math.abs(a - b) < 0.000001) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 把毫秒转化成yyyy-MM-dd的格式
     *
     * @param time
     * @return
     */
    public static String getTimeFormatText(long time) {
        String date = new SimpleDateFormat("yyyy.MM.dd HH:mm")
                .format(new java.util.Date(time * 1000));
        return date;
    }

    /**
     * 判断EditText 内是否为空
     *
     * @param editText
     * @return
     */
    public static boolean isNull(EditText editText) {
        String text = editText.getText().toString().trim();
        return TextUtils.isEmpty(text);
    }

    /**
     * 校验手机格式是否符合要求
     *
     * @param editText
     * @return
     */
    public static boolean matchPhoneNum(String text) {
        Pattern p = Pattern.compile("\\d{11}$");
        // .compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Matcher m = p.matcher(text);
        return m.matches();
    }

    public static void showDebugToast(String msg) {
        if (Constants.DEBUG) {
            Toast.makeText(PSGodApplication.getAppContext(), msg,
                    Toast.LENGTH_SHORT).show();
        }
    }

    // TODO
    // 初始化修改密码流程的activity列表 便于一次性关闭
    public static void initializeActivity() {
        if (Constants.activityList == null) {
            Constants.activityList = new LinkedList<Activity>();
        }
    }

    // 增加一个activity
    public static void addActivity(Activity activity) {
        Constants.activityList.add(activity);
    }

    // finish掉所有activity
    public static void finishActivity() {
        for (Activity activity : Constants.activityList) {
            activity.finish();
        }
    }

    static CustomProgressingDialog mProgressDialog = null;

    // 显示等待对话框
    public static void showProgressDialog(Context context) {
        // 显示等待对话框
        if (mProgressDialog == null) {
            mProgressDialog = new CustomProgressingDialog(context);
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    // 隐藏对话框
    public static void hideProgressDialog() {
        if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = null;
    }

    // 根据uri获取绝对路径
    public static String getAbsoluteImagePath(Context context, Uri uri) {
        String[] proj = {MediaColumns.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null,
                null, null);

        int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }

    // 获取手机状态栏高度
    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }

    /**
     * 获取点赞数、评论数和分享数显示的文字 目前的显示逻辑是少于1万显示具体的数字，大于1万显示X万
     *
     * @return
     */
    public static String getCountDisplayText(int count) {
        if (count < 10000) {
            return String.valueOf(count);
        } else {
            int k = count / 10000;
            return (k + "W");
        }
    }

    /**
     * 获取应用当前版本号
     *
     * @return
     */
    public static String getAppVersion(Context context) {
        String version = null;
        try {
            PackageManager manager = context.getPackageManager();
            version = manager.getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    public static PhotoItem dataToPhoto(Object data) {
        PhotoItem item = null;
        try {
            item = PhotoItem.createPhotoItem(new JSONObject(JSON.toJSONString(data)));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return item;
    }

    public static float getWidthScale(Context context) {
        float result;
        DisplayMetrics outMetrics = new DisplayMetrics();
        ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(outMetrics);
        result = pxToDpFloat(context, outMetrics.widthPixels) / pxToDpOrigin(context, 1080f);
        return result;
    }

    public static float getHeightScale(Context context) {
        float result;
        DisplayMetrics outMetrics = new DisplayMetrics();
        ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(outMetrics);
        result = pxToDpFloat(context, outMetrics.heightPixels) / pxToDpOrigin(context, 1920f);
        return result;
    }
}
