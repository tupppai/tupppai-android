package com.psgod;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Message;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.psgod.model.PhotoItem;
import com.psgod.model.SelectImage;
import com.psgod.network.request.BaseRequest;
import com.psgod.network.request.PhotoRequest;
import com.psgod.ui.activity.ChannelActivity;
import com.psgod.ui.activity.RecentActActivity;
import com.psgod.ui.activity.SinglePhotoDetail;
import com.psgod.ui.activity.WebBrowserActivity;
import com.psgod.ui.widget.dialog.CarouselPhotoDetailDialog;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /**
     * px转dp
     * @param context
     * @param pxValue
     * @return
     */
    public static float pxToDpFloat(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return pxValue / scale + 0.5f;
    }

    public static int pxToDp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 以xxhdpi为基准，将px转化为dp
     * @param context
     * @param dpValue
     * @return
     */
    public static float pxToDpOrigin(Context context, float dpValue) {
//        final float scale = context.getResources().getDisplayMetrics().density;
        return dpValue / 3 + 0.5f;
    }

    /**
     * 判断两浮点数是否相等
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
     * @param editText
     * @return
     */
    public static boolean isNull(EditText editText) {
        String text = editText.getText().toString().trim();
        return TextUtils.isEmpty(text);
    }

    /**
     * 校验手机格式是否符合要求
     * @param text
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

    // 初始化修改密码流程的activity列表 便于一次性关闭
    public static void initializeActivity() {
        if (Constants.activityList == null) {
            Constants.activityList = new LinkedList<Activity>();
        }
    }

    // 增加一个activity
    public static void addActivity(Activity activity) {
        initializeActivity();
        Constants.activityList.add(activity);
    }

    // finish掉所有activity
    public static void finishActivity() {
        initializeActivity();
        for (Activity activity : Constants.activityList) {
            if (activity != null && !activity.isFinishing()) {
                activity.finish();
            }
        }
        Constants.activityList.clear();
    }

    public static void removeActivity(Activity activity){
        try {
            Constants.activityList.remove(activity);
        }catch (Exception e){

        }
    }

    static CustomProgressingDialog mProgressDialog = null;

    // 显示等待对话框
    public static void showProgressDialog(Context context) {
        // 显示等待对话框
        mProgressDialog = new CustomProgressingDialog(context);
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    // 隐藏对话框
    public static void hideProgressDialog() {
        if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
            try {
                mProgressDialog.dismiss();
            } catch (Exception e) {

            }

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

    /**
     * 任意Object转换PhotoItem对象
     * @param data
     * @return
     */
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

    /**
     * 获得基于xxhdpi的屏幕宽比
     * @param context
     * @return
     */
    public static float getWidthScale(Context context) {
        float result;
        DisplayMetrics outMetrics = new DisplayMetrics();
        ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(outMetrics);
        result = pxToDpFloat(context, outMetrics.widthPixels) / pxToDpOrigin(context, 1080f);
        return result;
    }

    /**
     * 获得基于xxhdpi的屏幕高比
     * @param context
     * @return
     */
    public static float getHeightScale(Context context) {
        float result;
        DisplayMetrics outMetrics = new DisplayMetrics();
        ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(outMetrics);
        result = pxToDpFloat(context, outMetrics.heightPixels) / pxToDpOrigin(context, 1776f);
        return result;
    }

    /**
     * 获得真实屏幕高
     * @param context
     * @return
     */
    public static int getScreenHeightPx(Context context) {
        Point point = new Point();
        ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(point);
        return point.y;
    }

    /**
     * 获得内容部分屏幕高
     * @param context
     * @return
     */
    public static int getUnrealScreenHeightPx(Context context) {
        DisplayMetrics outMetrics = new DisplayMetrics();
        ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels - getStatusBarHeight(context);
    }

    /**
     * 获得屏幕宽度
     * @param context
     * @return
     */
    public static int getScreenWidthPx(Context context) {
        Point point = new Point();
        ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(point);
        return point.x;
    }

    /**
     * 将SelectImage对象容器转化为String
     * @param images
     * @return
     */
    public static List<String> selectImageToString(List<SelectImage> images) {
        List<String> strs = new ArrayList<String>();
        for (SelectImage image : images) {
            strs.add(image.path);
        }
        return strs;
    }

    /**
     * 根据url跳转指定页面，配合jsbrige使用
     * @param context
     * @param url
     * @param title
     */
    public static void skipByUrl(Context context, String url, String title) {
        if (url.indexOf("http") != -1) {
            Intent intent = new Intent(context,
                    WebBrowserActivity.class);
            intent.putExtra(WebBrowserActivity.KEY_URL, url);
            intent.putExtra(WebBrowserActivity.KEY_DESC, title);
            context.startActivity(intent);
        } else if (url.indexOf("tupppai://") == -1) {
            Intent intent = new Intent(context,
                    WebBrowserActivity.class);
            intent.putExtra(WebBrowserActivity.KEY_URL,
                    BaseRequest.PSGOD_BASE_URL + url);
            intent.putExtra(WebBrowserActivity.KEY_DESC, title);
            context.startActivity(intent);
        } else {
            /**
             * tupppai:// + activity + / + id
             */
            String[] s = url.split("tupppai://");
            if (s.length == 2) {
                String[] thumb = s[1].split("/");
                if (thumb.length == 2) {
                    Intent intent = new Intent();
                    if (thumb[0].equals("activity")) {
                        intent.setClass(context, RecentActActivity.class);
                        intent.putExtra(RecentActActivity.INTENT_ID, thumb[1]);
                    } else {
                        intent.setClass(context, ChannelActivity.class);
                        intent.putExtra(ChannelActivity.INTENT_ID, thumb[1]);
                        intent.putExtra(ChannelActivity.INTENT_TITLE, title);
                    }
                    context.startActivity(intent);
                }
            }
        }
    }

    /**
     * 根据对象类型进行跳转，封装跳转
     * @param context
     * @param obj
     */
    public static void skipByObject(Context context, Object obj) {
        if (obj instanceof PhotoItem) {
            PhotoItem photoItem = (PhotoItem) obj;
//            if (photoItem.getCategoryType().equals("tutorial")) {
            SinglePhotoDetail.startActivity(
                    context, photoItem);
//            } else {
//                new CarouselPhotoDetailDialog(context,
//                        photoItem.getAskId(),
//                        photoItem.getPid(),
//                        photoItem.getCategoryId()).show();
//            }
        }
    }

    public static void skipByObject(Context context, String type, long id) {
        SinglePhotoDetail.startActivity(context, id, type);
    }

    private static boolean isBindInputPhoneShow = false;

    public static void setBindInputPhoneShow(boolean isBindInputPhoneShow) {
        Utils.isBindInputPhoneShow = isBindInputPhoneShow;
    }

    public static boolean isBindInputPhoneShow() {
        return isBindInputPhoneShow;
    }

    // 隐藏输入法
    public static void hideInputPanel(Context context, View view) {
        // 隐藏软键盘
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * 生成md5
     * @param str
     * @return
     */
    public static String toMd5(String str) {

        String md5str = "";
        try {
            //1 创建一个提供信息摘要算法的对象，初始化为md5算法对象
            MessageDigest md = MessageDigest.getInstance("MD5");

            //2 将消息变成byte数组
            byte[] input = str.getBytes();

            //3 计算后获得字节数组,这就是那128位了
            byte[] buff = md.digest(input);

            //4 把数组每一字节（一个字节占八位）换成16进制连成md5字符串
            md5str = bytesToHex(buff);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return md5str;
    }

    /**
     * 二进制转十六进制
     * @param bytes
     * @return
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer md5str = new StringBuffer();
        //把数组每一字节换成16进制连成md5字符串
        int digital;
        for (int i = 0; i < bytes.length; i++) {
            digital = bytes[i];

            if (digital < 0) {
                digital += 256;
            }
            if (digital < 16) {
                md5str.append("0");
            }
            md5str.append(Integer.toHexString(digital));
        }
        return md5str.toString().toUpperCase();
    }

}
