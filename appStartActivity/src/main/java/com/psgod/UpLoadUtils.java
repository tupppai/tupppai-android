package com.psgod;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.psgod.eventbus.MyPageRefreshEvent;
import com.psgod.eventbus.RefreshEvent;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.UploadImageRequest;
import com.psgod.network.request.UploadMultiRequest;
import com.psgod.ui.activity.ChannelActivity;
import com.psgod.ui.activity.MultiImageSelectActivity;
import com.psgod.ui.activity.RecentActActivity;
import com.psgod.ui.activity.RecentAsksActivity;
import com.psgod.ui.activity.RecentWorkActivity;
import com.psgod.ui.fragment.TupppaiFragment;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2016/1/7 0007.
 */
public class UpLoadUtils {

    private static UpLoadUtils upLoadUtils;

    private Context mContext;
    private String descTxt;
    private List<String> pathList;
    private CustomProgressingDialog mProgressDialog;
    private String uploadType;

    private String mActivityId;
    private String mChannelId;
    private long mAskId = -1;

    public static final String TYPE_ASK_UPLOAD = "type_ask_upload";
    public static final String TYPE_REPLY_UPLOAD = "type_reply_upload";
    public static final String TYPE_ACTIVITY_UPLOAD = "type_activity_upload";

    private String categoryId;

    private Bitmap mImageBitmap; // 压缩后的图片
    private ArrayList<Long> mUploadIdList = new ArrayList<Long>(); // 上传多图返回的id
    private ArrayList<Float> mImageRatioList = new ArrayList<Float>(); // 图片高度/图片宽度
    private ArrayList<Float> mImageScaleList = new ArrayList<Float>(); // 屏幕显示宽度/图片显示宽度

    protected UpLoadUtils() {

    }

    public static UpLoadUtils getInstance(Context context) {
        if (upLoadUtils == null) {
            upLoadUtils = new UpLoadUtils();
        }
        upLoadUtils.mContext = context;
        upLoadUtils.mProgressDialog = new CustomProgressingDialog(context);
        return upLoadUtils;
    }


    public void upLoad(String dsec, List<String> pathList, long askId, String categoryId, String uploadType) {
        descTxt = dsec;
        this.pathList = pathList;
        this.mAskId = askId;
        switch (uploadType){
            case TYPE_ASK_UPLOAD:
                this.uploadType = UploadMultiRequest.TYPE_ASK_UPLOAD;
                break;
            case TYPE_ACTIVITY_UPLOAD:
                this.uploadType = UploadMultiRequest.TYPE_REPLY_UPLOAD;
                mActivityId = categoryId;
                break;
            case TYPE_REPLY_UPLOAD:
                this.uploadType = UploadMultiRequest.TYPE_REPLY_UPLOAD;
                mChannelId = categoryId;
                break;
        }
        this.uploadType = uploadType;
        // 显示等待对话框
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }

        mUploadIdList.clear();
        mImageRatioList.clear();
        mImageScaleList.clear();

        for (int i = 0; i < pathList.size(); i++) {
            mImageBitmap = BitmapUtils.decodeBitmap(pathList.get(i));

            int imageHeight = mImageBitmap.getHeight();
            int imageWidth = mImageBitmap.getWidth();

            float mRatio = (float) imageHeight / imageWidth;
            mImageRatioList.add(mRatio);

            Resources res = mContext.getResources();
            float mScale = (float) (Constants.WIDTH_OF_SCREEN - 2 * res
                    .getDimensionPixelSize(R.dimen.photo_margin))
                    / imageWidth;
            mImageScaleList.add(mScale);

            // 上传照片
            UploadImageRequest.Builder builder = new UploadImageRequest.Builder()
                    .setBitmap(mImageBitmap).setErrorListener(
                            errorListener);
            if (i == 1) {
                builder.setListener(uploadImageListener);
            } else {
                builder.setListener(uploadImageListenerId);
            }
            UploadImageRequest request = builder.build();
            request.setTag(mContext.getClass().getSimpleName());
            RequestQueue reqeustQueue = PSGodRequestQueue
                    .getInstance(mContext)
                    .getRequestQueue();
            reqeustQueue.add(request);
        }


    }

    private PSGodErrorListener errorListener = new PSGodErrorListener(
            UploadMultiRequest.class.getSimpleName()) {
        @Override
        public void handleError(VolleyError error) {
            Utils.hideProgressDialog();
            mProgressDialog.dismiss();
        }
    };

    private Response.Listener<UploadImageRequest.ImageUploadResult> uploadImageListenerId = new Response.Listener<UploadImageRequest.ImageUploadResult>() {

        @Override
        public void onResponse(UploadImageRequest.ImageUploadResult response) {
            mUploadIdList.add(response.id);
            if (mUploadIdList.size() == pathList.size()) {
                UploadMultiRequest.Builder builder = new UploadMultiRequest.Builder()
                        .setUploadType(uploadType).setContent(descTxt)
                        .setUploadIdList(mUploadIdList)
                        .setRatioList(mImageRatioList).setAskId(mAskId)
                        .setActivityId(mActivityId).setChannelId(mChannelId)
                        .setLabelIdList(mSelectLabelIds)
                        .setScaleList(mImageScaleList).setListener(uploadListener)
                        .setErrorListener(errorListener);

                UploadMultiRequest request = builder.builder();
                RequestQueue reqeustQueue = PSGodRequestQueue.getInstance(
                        mContext).getRequestQueue();
                reqeustQueue.add(request);
            }
        }

    };

    private Response.Listener<UploadImageRequest.ImageUploadResult> uploadImageListener = new Response.Listener<UploadImageRequest.ImageUploadResult>() {

        @Override
        public void onResponse(UploadImageRequest.ImageUploadResult response) {
            mUploadIdList.add(response.id);
            if (mUploadIdList.size() == pathList.size()) {
                UploadMultiRequest.Builder builder = new UploadMultiRequest.Builder()
                        .setUploadType(uploadType).setContent(descTxt)
                        .setUploadIdList(mUploadIdList)
                        .setRatioList(mImageRatioList).setAskId(mAskId)
                        .setActivityId(mActivityId).setChannelId(mChannelId)
                        .setLabelIdList(mSelectLabelIds)
                        .setScaleList(mImageScaleList).setListener(uploadListener)
                        .setErrorListener(errorListener);

                UploadMultiRequest request = builder.builder();
                RequestQueue reqeustQueue = PSGodRequestQueue.getInstance(
                        mContext).getRequestQueue();
                reqeustQueue.add(request);
            }
        }

    };

    public Response.Listener<UploadMultiRequest.MultiUploadResult> uploadListener = new Response.Listener<UploadMultiRequest.MultiUploadResult>() {
        @Override
        public void onResponse(UploadMultiRequest.MultiUploadResult response) {
            mProgressDialog.dismiss();

            Toast.makeText(mContext, "上传成功", Toast.LENGTH_SHORT).show();

            if (IMAGE_UPLOAD_TYPE == TYPE_ASK_UPLOAD) {
                // 新建求P成功后跳转最新求p 页面
//                Intent intent = new Intent(UploadMultiImageActivity.this,
//                        MainActivity.class);
//                intent.putExtra(MainActivity.IntentParams.KEY_FRAGMENT_ID,
//                        MainActivity.IntentParams.VALUE_FRAGMENT_ID_RECENT);
//                intent.putExtra(
//                        MainActivity.IntentParams.KEY_RECENTPAGE_ID,
//                        MainActivity.IntentParams.VALUE_RECENTPAGE_ID_ASKS);
//                intent.putExtra(MainActivity.IntentParams.KEY_NEED_REFRESH,
//                        true);
                EventBus.getDefault().post(new MyPageRefreshEvent(MyPageRefreshEvent.ASK));
                Intent intent = new Intent();
                intent.putExtra("isRefresh", true);
                if (isAsk) {
                    intent.setClass(UploadMultiImageActivity.this, RecentAsksActivity.class);
                } else {
                    intent.setClass(UploadMultiImageActivity.this, ChannelActivity.class);
                }
                if (mChannelId != null && !mChannelId.equals("")) {
                    intent.putExtra("id", mChannelId);
                }
                startActivity(intent);
            } else {
                // 新建作品成功后跳转最新作品 页面
//                Intent intent = new Intent(UploadMultiImageActivity.this,
//                        MainActivity.class);
//                intent.putExtra(MainActivity.IntentParams.KEY_FRAGMENT_ID,
//                        MainActivity.IntentParams.VALUE_FRAGMENT_ID_RECENT);
//                intent.putExtra(
//                        MainActivity.IntentParams.KEY_RECENTPAGE_ID,
//                        MainActivity.IntentParams.VALUE_RECENTPAGE_ID_WORKS);
//                intent.putExtra(MainActivity.IntentParams.KEY_NEED_REFRESH,
//                        true);
                EventBus.getDefault().post(new MyPageRefreshEvent(MyPageRefreshEvent.WORK));
                Intent intent = new Intent();
                intent.putExtra("isRefresh", true);
                if (mActivityId != null && !mActivityId.equals("")) {
                    intent.setClass(UploadMultiImageActivity.this, RecentActActivity.class);
                    intent.putExtra("id", mActivityId);
                } else if (mChannelId != null && !mChannelId.equals("")) {
                    intent.setClass(UploadMultiImageActivity.this, ChannelActivity.class);
                    intent.putExtra("id", mChannelId);
                } else {
                    intent.setClass(UploadMultiImageActivity.this, RecentWorkActivity.class);
                }
                startActivity(intent);
            }
            EventBus.getDefault().post(new RefreshEvent(TupppaiFragment.class.getName()));
            EventBus.getDefault().post(new RefreshEvent(MultiImageSelectActivity.class.getName()));
            UploadMultiImageActivity.this.finish();
        }
    };


}
