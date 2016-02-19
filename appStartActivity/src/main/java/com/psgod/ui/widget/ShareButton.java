package com.psgod.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.psgod.Constants;
import com.psgod.CustomToast;
import com.psgod.PSGodApplication;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.ActionShareRequest;
import com.psgod.network.request.PSGodRequestQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

/**
 * Created by Administrator on 2016/1/21 0021.
 */
public class ShareButton extends Button {
    private static final String TAG = ShareButton.class.getSimpleName();

    public ShareButton(Context context) {
        super(context);
    }

    public ShareButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShareButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public static final int TYPE_WECHAT_FRIEND = 1;
    public static final int TYPE_WECHAT_MOMENTS = 2;
    public static final int TYPE_WEIBO = 3;
    public static final int TYPE_QQ = 4;
    public static final int TYPE_QZONE = 5;

    public static final int TYPE_DRAWABLE_TOP = 1;
    public static final int TYPE_DRAWABLE_BACKGROUND = 2;

    private OnShareListener onShareListener;

    private String sharePlatform;
    private Response.Listener<JSONObject> shareListener;
    private PhotoItem mPhotoItem;

    public void setPhotoItem(PhotoItem photoItem) {
        this.mPhotoItem = photoItem;
    }

    public void setOnShareListener(OnShareListener onShareListener) {
        this.onShareListener = onShareListener;
    }

    public void setShareType(int shareType) {
        setShareType(shareType, TYPE_DRAWABLE_BACKGROUND);
    }

    public void setShareType(int shareType, int drawableType) {
        switch (shareType) {
            case TYPE_WECHAT_FRIEND:
                setDrawable(getResources().getDrawable(R.drawable.ic_weixin_new), drawableType);
                sharePlatform = "wechat";
                shareListener = shareFriendsListener;
                break;
            case TYPE_WECHAT_MOMENTS:
                setDrawable(getResources().getDrawable(R.drawable.ic_wechat_new), drawableType);
                sharePlatform = "wechat_timeline";
                shareListener = shareMomentsListener;
                break;
            case TYPE_WEIBO:
                setDrawable(getResources().getDrawable(R.drawable.ic_weibo_new), drawableType);
                sharePlatform = "weibo";
                shareListener = shareWeiboListener;
                break;
            case TYPE_QQ:
                setDrawable(getResources().getDrawable(R.drawable.ic_qq_new), drawableType);
                sharePlatform = "qq_friend";
                shareListener = shareQQlistener;
                break;
            case TYPE_QZONE:
                setDrawable(getResources().getDrawable(R.drawable.ic_qzone), drawableType);
                sharePlatform = "qq_timeline";
                shareListener = shareQzoneListener;
                break;
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sharePlatform != null && !sharePlatform.equals("")
                        && shareListener != null && mPhotoItem != null) {
                    Utils.showProgressDialog(getContext());
                    ActionShareRequest.Builder builder = new ActionShareRequest.Builder()
                            .setShareType(sharePlatform).setType(mPhotoItem.getType())
                            .setId(mPhotoItem.getPid())
                            .setListener(shareListener)
                            .setErrorListener(errorListener);

                    ActionShareRequest request = builder.build();
                    request.setTag(TAG);
                    RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                            PSGodApplication.getAppContext()).getRequestQueue();
                    requestQueue.add(request);

                }
            }
        });
    }

    private void setDrawable(Drawable drawable, int drawableType) {
        switch (drawableType) {
            case TYPE_DRAWABLE_TOP:
                setBackground(new ColorDrawable(Color.parseColor("#00000000")));
                setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
                break;
            case TYPE_DRAWABLE_BACKGROUND:
                setBackground(drawable);
                break;
        }
    }

    public interface OnShareListener {
        void onError(Platform platform, int arg1,
                     Throwable arg2);

        void onComplete(Platform arg0, int arg1,
                        HashMap<String, Object> arg2);

        void onCancel(Platform arg0, int arg1);
    }

    private class PsgodPlatformActionListener implements PlatformActionListener {

        @Override
        public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
            if (onShareListener != null) {
                onShareListener.onComplete(platform, i, hashMap);
            }
        }

        @Override
        public void onError(Platform platform, int i, Throwable throwable) {
            if (onShareListener != null) {
                onShareListener.onError(platform, i, throwable);
            }
        }

        @Override
        public void onCancel(Platform platform, int i) {
            if (onShareListener != null) {
                onShareListener.onCancel(platform, i);
            }
        }
    }

    private class PsgodOnekeyShare extends OnekeyShare {
        @Override
        public void onError(Platform arg0, int arg1,
                            Throwable arg2) {
            if (onShareListener != null) {
                onShareListener.onError(arg0, arg1, arg2);
            }
        }

        @Override
        public void onComplete(Platform arg0, int arg1,
                               HashMap<String, Object> arg2) {
            if (onShareListener != null) {
                onShareListener.onComplete(arg0, arg1, arg2);
            }
        }

        @Override
        public void onCancel(Platform arg0, int arg1) {
            if (onShareListener != null) {
                onShareListener.onCancel(arg0, arg1);
            }
        }
    }

    // qzone分享回调 qq空间分享图文
    private Response.Listener<JSONObject> shareQzoneListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Utils.hideProgressDialog();

            ShareSDK.initSDK(getContext());
            try {
                OnekeyShare oks = new PsgodOnekeyShare();
                oks.setPlatform(QZone.NAME);

                oks.setTitle(response.getString("title"));
                oks.setTitleUrl(response.getString("url"));
                oks.setText(response.getString("desc"));
                oks.setImageUrl(response.getString("image"));
                // 设置发布分享的网站名称和网址
                oks.setSite(Constants.OFFICAL_APP_NAME);
                oks.setSiteUrl(Constants.OFFICAL_WEBSITE);

                oks.show(getContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    // 微信好友分享请求回调
    private Response.Listener<JSONObject> shareFriendsListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Utils.hideProgressDialog();

            ShareSDK.initSDK(getContext());
            Platform wechatFriends = ShareSDK
                    .getPlatform(getContext(), Wechat.NAME);
            wechatFriends
                    .setPlatformActionListener(new PsgodPlatformActionListener());

            try {
                if (response.getString("type").equals("image")) {
                    Platform.ShareParams sp = new Platform.ShareParams();

                    sp.setShareType(Platform.SHARE_IMAGE);
                    sp.setTitle(response.getString("title"));
                    sp.setText(response.getString("desc"));
                    sp.setImageUrl(response.getString("image"));
                    wechatFriends.share(sp);
                }
                if (response.getString("type").equals("url")) {
                    // 图文链接分享
                    Platform.ShareParams sp = new Platform.ShareParams();
                    sp.setShareType(Platform.SHARE_WEBPAGE);

                    sp.setTitle(response.getString("title"));
                    sp.setText(response.getString("desc"));
                    sp.setImageUrl(response.getString("image"));
                    sp.setUrl(response.getString("url"));
                    wechatFriends.share(sp);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    // 微信朋友圈分享接口请求回调
    private Response.Listener<JSONObject> shareMomentsListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Utils.hideProgressDialog();

            ShareSDK.initSDK(getContext());
            Platform wechat = ShareSDK
                    .getPlatform(getContext(), WechatMoments.NAME);
            wechat.setPlatformActionListener(new PsgodPlatformActionListener());

            try {
                if (response.getString("type").equals("image")) {
                    Platform.ShareParams sp = new Platform.ShareParams();

                    sp.setShareType(Platform.SHARE_IMAGE);
                    sp.setTitle(response.getString("title"));
                    sp.setText(response.getString("desc"));
                    sp.setImageUrl(response.getString("image"));
                    wechat.share(sp);
                }
                if (response.getString("type").equals("url")) {
                    // 图文链接分享
                    Platform.ShareParams sp = new Platform.ShareParams();

                    sp.setShareType(Platform.SHARE_WEBPAGE);
                    sp.setTitle(response.getString("title"));
                    sp.setText(response.getString("desc"));
                    sp.setImageUrl(response.getString("image"));
                    sp.setUrl(response.getString("url"));
                    wechat.share(sp);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    // qq分享接口请求回调 QQ图文分享
    private Response.Listener<JSONObject> shareQQlistener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Utils.hideProgressDialog();

            ShareSDK.initSDK(getContext());
            try {
                OnekeyShare oks = new PsgodOnekeyShare();
                oks.setPlatform(QQ.NAME);

                oks.setTitle(response.getString("title"));
                oks.setTitleUrl(response.getString("url"));
                oks.setText(response.getString("desc"));
                oks.setImageUrl(response.getString("image"));

                oks.show(getContext());
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    };

    // 微博分享接口请求回调 新浪微博只支持图文／文字
    private Response.Listener<JSONObject> shareWeiboListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Utils.hideProgressDialog();

            ShareSDK.initSDK(getContext());
            try {
                OnekeyShare oks = new PsgodOnekeyShare();

                oks.setPlatform(SinaWeibo.NAME);
                oks.disableSSOWhenAuthorize();
                oks.setSilent(false);

                oks.setText(response.getString("desc"));
                oks.setImageUrl(response.getString("image"));
                oks.show(getContext());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Utils.hideProgressDialog();
            CustomToast.show(getContext(), "分享失败", Toast.LENGTH_SHORT);
        }
    };
}
