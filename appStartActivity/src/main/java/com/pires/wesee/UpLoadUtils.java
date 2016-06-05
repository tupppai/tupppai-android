package com.pires.wesee;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.pires.wesee.eventbus.MyPageRefreshEvent;
import com.pires.wesee.network.request.UploadImageRequest;
import com.pires.wesee.ui.activity.RecentActActivity;
import com.pires.wesee.ui.activity.RecentWorkActivity;
import com.pires.wesee.R;
import com.pires.wesee.eventbus.RefreshEvent;
import com.pires.wesee.network.request.PSGodErrorListener;
import com.pires.wesee.network.request.PSGodRequestQueue;
import com.pires.wesee.network.request.UploadMultiRequest;
import com.pires.wesee.ui.activity.ChannelActivity;
import com.pires.wesee.ui.activity.MultiImageSelectActivity;
import com.pires.wesee.ui.activity.PSGodBaseActivity;
import com.pires.wesee.ui.activity.RecentAsksActivity;
import com.pires.wesee.ui.fragment.TupppaiFragment;
import com.pires.wesee.ui.widget.dialog.CustomProgressingDialog;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2016/1/7 0007.
 */
public class UpLoadUtils {


    private PSGodBaseActivity mContext;
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

    /**
     *
     */
    public static UpLoadUtils getInstance(PSGodBaseActivity context) {
//        if (upLoadUtils == null) {
//            upLoadUtils = new UpLoadUtils();
//        }
        UpLoadUtils upLoadUtils = new UpLoadUtils();
        upLoadUtils.mContext = context;
        upLoadUtils.mProgressDialog = new CustomProgressingDialog(context);
        return upLoadUtils;
    }


    public void upLoad(String dsec, List<String> pathList, long askId, String uploadType) {
        descTxt = dsec;
        this.pathList = pathList;
        this.mAskId = askId;
        switch (uploadType) {
            case TYPE_ASK_UPLOAD:
                this.uploadType = UploadMultiRequest.TYPE_ASK_UPLOAD;
                break;
            case TYPE_REPLY_UPLOAD:
                this.uploadType = UploadMultiRequest.TYPE_REPLY_UPLOAD;
                break;
        }
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
                builder.setListener(uploadImageListener2);
            } else {
                builder.setListener(uploadImageListener1);
            }
            UploadImageRequest request = builder.build();
            request.setTag(mContext.getClass().getSimpleName());
            RequestQueue reqeustQueue = PSGodRequestQueue
                    .getInstance(mContext)
                    .getRequestQueue();
            reqeustQueue.add(request);
        }


    }

    public interface OnUploadListener{
        void onUpload(UploadImageRequest.ImageUploadResult response);
    }

    private OnUploadListener onUploadListener;

    public void setOnUploadListener(OnUploadListener onUploadListener) {
        this.onUploadListener = onUploadListener;
    }

    public void upLoad(String dsec, List<String> pathList, long askId, String categoryId, String uploadType) {
        descTxt = dsec;
        this.pathList = pathList;
        this.mAskId = askId;
        switch (uploadType) {
            case TYPE_ASK_UPLOAD:
                this.uploadType = UploadMultiRequest.TYPE_ASK_UPLOAD;
                mChannelId = categoryId;
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
                builder.setListener(uploadImageListener2);
            } else {
                builder.setListener(uploadImageListener1);
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

    private Response.Listener<UploadImageRequest.ImageUploadResult> uploadImageListener1 = new Response.Listener<UploadImageRequest.ImageUploadResult>() {

        @Override
        public void onResponse(UploadImageRequest.ImageUploadResult response) {
            mUploadIdList.add(response.id);
            if (mUploadIdList.size() == pathList.size()) {
                if(onUploadListener!= null){
                    onUploadListener.onUpload(response);
                }
                UploadMultiRequest.Builder builder = new UploadMultiRequest.Builder()
                        .setUploadType(uploadType).setContent(descTxt)
                        .setUploadIdList(mUploadIdList)
                        .setRatioList(mImageRatioList).setAskId(mAskId)
                        .setActivityId(mActivityId).setChannelId(mChannelId)
                        .setScaleList(mImageScaleList).setListener(uploadListener)
                        .setErrorListener(errorListener);

                UploadMultiRequest request = builder.builder();
                RequestQueue reqeustQueue = PSGodRequestQueue.getInstance(
                        mContext).getRequestQueue();
                reqeustQueue.add(request);
            }
        }

    };

    private Response.Listener<UploadImageRequest.ImageUploadResult> uploadImageListener2 = new Response.Listener<UploadImageRequest.ImageUploadResult>() {

        @Override
        public void onResponse(UploadImageRequest.ImageUploadResult response) {
            mUploadIdList.add(response.id);
            if (mUploadIdList.size() == pathList.size()) {
                if(onUploadListener!= null){
                    onUploadListener.onUpload(response);
                }
                UploadMultiRequest.Builder builder = new UploadMultiRequest.Builder()
                        .setUploadType(uploadType).setContent(descTxt)
                        .setUploadIdList(mUploadIdList)
                        .setRatioList(mImageRatioList).setAskId(mAskId)
                        .setActivityId(mActivityId).setChannelId(mChannelId)
                        .setScaleList(mImageScaleList).setListener(uploadListener)
                        .setErrorListener(errorListener);

                UploadMultiRequest request = builder.builder();
                RequestQueue reqeustQueue = PSGodRequestQueue.getInstance(
                        mContext).getRequestQueue();
                reqeustQueue.add(request);
            }
        }

    };

    public void setUploadListener(Response.Listener<UploadMultiRequest.MultiUploadResult> uploadListener) {
        this.uploadListener = uploadListener;
    }

    public Response.Listener<UploadMultiRequest.MultiUploadResult> uploadListener = new Response.Listener<UploadMultiRequest.MultiUploadResult>() {
        @Override
        public void onResponse(UploadMultiRequest.MultiUploadResult response) {
            mProgressDialog.dismiss();

            Toast.makeText(mContext, "上传成功", Toast.LENGTH_SHORT).show();

            if (uploadType == UploadMultiRequest.TYPE_ASK_UPLOAD) {
                // 新建求P成功后跳转最新求p 页面
                EventBus.getDefault().post(new MyPageRefreshEvent(MyPageRefreshEvent.ASK));
                Intent intent = new Intent();
                intent.putExtra("isRefresh", true);
                if (mChannelId != null && !mChannelId.equals("")) {
                    intent.putExtra("id", mChannelId);
                    intent.setClass(mContext, ChannelActivity.class);
                } else {
                    intent.setClass(mContext, RecentAsksActivity.class);
                }
                mContext.startActivity(intent);
            } else {
                // 新建作品成功后跳转最新作品 页面
                EventBus.getDefault().post(new MyPageRefreshEvent(MyPageRefreshEvent.WORK));
                Intent intent = new Intent();
                intent.putExtra("isRefresh", true);
                if (mActivityId != null && !mActivityId.equals("")) {
                    intent.setClass(mContext, RecentActActivity.class);
                    intent.putExtra("id", mActivityId);
                } else if (mChannelId != null && !mChannelId.equals("")) {
                    intent.setClass(mContext, ChannelActivity.class);
                    intent.putExtra("id", mChannelId);
                } else {
                    intent.setClass(mContext, RecentWorkActivity.class);
                }
                mContext.startActivity(intent);
            }
            EventBus.getDefault().post(new RefreshEvent(TupppaiFragment.class.getName()));
            EventBus.getDefault().post(new RefreshEvent(MultiImageSelectActivity.class.getName()));
        }
    };

    public void hideProgressDialog(){
        if(mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

}
