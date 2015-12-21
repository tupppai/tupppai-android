package com.psgod;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.psgod.eventbus.MyPageRefreshEvent;
import com.psgod.network.request.PhotoRequest;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import de.greenrobot.event.EventBus;

/**
 * Created by remilia on 2015/11/18.
 */
public class LoadUtils implements Handler.Callback {

    private Context mContext;
    private CustomProgressingDialog mProgressDialog;

    private static final int MSG_SUCCESSFUL = 0x4400;
    private static final int MSG_FAILED = 0x4401;
    public static final int MSG_RECORD_SUCCESS = 0x4402;
    private static final int MSG_RECORD_FAILED = 0x4403;

    private long category_id = -1;

    public LoadUtils(Context mContext) {
        this.mContext = mContext;
    }

    private WeakReferenceHandler mHandler = new WeakReferenceHandler(this);
    private boolean isSimple = false;

    public LoadUtils isSimple(boolean isSimple){
        this.isSimple = isSimple;
        return this;
    }

    public LoadUtils setCategory_id(long category_id) {
        this.category_id = category_id;
        return this;
    }

    public void upLoad(final int type, final long pid) {
        if(!isSimple) {
            if (mProgressDialog == null) {
                mProgressDialog = new CustomProgressingDialog(mContext);
            }
            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        }

        ThreadManager.executeOnNetWorkThread(new Runnable() {
            @Override
            public void run() {
                String mType;
                if (type == 1) {
                    mType = "ask";
                } else {
                    mType = "reply";
                }
                PhotoRequest.ImageInfo info = PhotoRequest.getImageInfo(mType,
                        pid,category_id);
                if (!info.isSuccessful) {
                    mHandler.sendEmptyMessage(MSG_RECORD_FAILED);
                } else {
                    mHandler.sendEmptyMessage(MSG_RECORD_SUCCESS);
                }
            }
        });

    }

    @Override
    public boolean handleMessage(Message msg) {
        if(!isSimple) {
            mProgressDialog.dismiss();
            View toastView = LayoutInflater.from(mContext).inflate(
                    R.layout.toast_layout, null);
            TextView aboveText = (TextView) toastView
                    .findViewById(R.id.toast_text_above);
            TextView belowText = (TextView) toastView
                    .findViewById(R.id.toast_text_below);
            switch (msg.what) {
//            case MSG_SUCCESSFUL:
//                String path = (String) msg.obj;
//                Toast toast = Toast.makeText(mContext, "素材保存到" + path,
//                        Toast.LENGTH_SHORT);
//                aboveText.setText("下载成功,");
//                belowText.setText("我猜你会用美图秀秀来P?");
//                toast.setView(toastView);
//                toast.setGravity(Gravity.CENTER, 0, 0);
//                toast.show();
//                if (!isFromPhotoBroswer) {
//                    mPhotoItem.setIsDownloaded(true);
//                }
//                break;
//            case MSG_FAILED:
//                // TODO 获取图片信息失败
//                Toast.makeText(mContext, "下载素材失败", Toast.LENGTH_SHORT).show();
//                break;
                case MSG_RECORD_SUCCESS:
                    Toast toast2 = Toast.makeText(mContext, "已塞入进行中",
                            Toast.LENGTH_SHORT);
                    aboveText.setText("添加成功,");
                    belowText.setText("在“进行中”等你下载喽!");
                    toast2.setView(toastView);
                    toast2.setGravity(Gravity.CENTER, 0, 0);
                    toast2.show();
                    EventBus.getDefault().post(
                            new MyPageRefreshEvent(MyPageRefreshEvent.REPLY));
                    break;
                case MSG_RECORD_FAILED:
                    Toast.makeText(mContext, "塞入进行中失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }else{
            EventBus.getDefault().post(
                    new MyPageRefreshEvent(MyPageRefreshEvent.REPLY));
        }
        return true;
    }
}
