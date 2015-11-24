package com.psgod.ui.activity;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.moments.WechatMoments;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.psgod.BitmapUtils;
import com.psgod.Constants;
import com.psgod.CustomToast;
import com.psgod.PSGodApplication;
import com.psgod.PSGodToast;
import com.psgod.R;
import com.psgod.UploadCache;
import com.psgod.UserPreferences;
import com.psgod.Utils;
import com.psgod.WeakReferenceHandler;
import com.psgod.eventbus.MyPageRefreshEvent;
import com.psgod.network.request.ActionShareRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.UploadImageRequest;
import com.psgod.network.request.UploadImageRequest.ImageUploadResult;
import com.psgod.network.request.UploadMultiRequest;
import com.psgod.network.request.UploadMultiRequest.MultiUploadResult;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import de.greenrobot.event.EventBus;

/**
 * 上传多图页面 v2.0
 *
 * @author ZouMengyuan
 */

public class UploadMultiImageActivity extends PSGodBaseActivity {

    private final static String TAG = UploadMultiImageActivity.class
            .getSimpleName();
    public final static String MULTIIMAGESELECTRESULT = "MultiImageSelectResult";

    private Context mContext;
    private ArrayList<String> pathList = new ArrayList<String>();
    private LinearLayout mImageLayout;
    private Button mFinishBtn;
    private EditText mContentEdit;
    private ImageButton mBackBtn;
    private TextView mInputCountTv;
    private ToggleButton[] mShareBtns;
    private int mCheckedShareBtnId;
    private CustomProgressingDialog mProgressDialog;

    public final static String TYPE_ASK_UPLOAD = "TypeAskUpload";
    public final static String TYPE_REPLY_UPLOAD = "TypeReplyUpload";
    public static String IMAGE_UPLOAD_TYPE = TYPE_ASK_UPLOAD;
    private static final int JUMP_FROM_UPLOAD_ASK = 1000;

    private String type;
    private String contentString;
    private Long mAskId = 0l;

    private Bitmap mImageBitmap; // 压缩后的图片
    private ArrayList<Long> mUploadIdList = new ArrayList<Long>(); // 上传多图返回的id
    private ArrayList<Float> mImageRatioList = new ArrayList<Float>(); // 图片高度/图片宽度
    private ArrayList<Float> mImageScaleList = new ArrayList<Float>(); // 屏幕显示宽度/图片显示宽度

    private WeakReferenceHandler handler = new WeakReferenceHandler(this);

    private int sharetype;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_multi_image);
        mContext = this;

        Bundle bundle = getIntent().getExtras();
        pathList = bundle
                .getStringArrayList(UploadMultiImageActivity.MULTIIMAGESELECTRESULT);
        type = bundle.getString("SelectType");
        mAskId = bundle.getLong("AskId", mAskId);
        if (type.equals(MultiImageSelectActivity.TYPE_ASK_SELECT)) {
            IMAGE_UPLOAD_TYPE = TYPE_ASK_UPLOAD;
        } else {
            IMAGE_UPLOAD_TYPE = TYPE_REPLY_UPLOAD;
        }

        sharetype = (IMAGE_UPLOAD_TYPE.equals(TYPE_ASK_UPLOAD) == true) ? 1
                : 2;

        initViews();
        initListener();

        // 延时300毫秒 弹出输入法
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                callInputPanel();
            }
        }, 300);

    }

    public void initViews() {
        mBackBtn = (ImageButton) findViewById(R.id.btn_back);
        mFinishBtn = (Button) findViewById(R.id.btn_complete);
        mContentEdit = (EditText) findViewById(R.id.upload_text);

        if (type.equals(MultiImageSelectActivity.TYPE_ASK_SELECT)) {
            mContentEdit.setHint("写下你的图片需求吧");
        } else {
            mContentEdit.setHint("输入你想对观众说的吧");
        }
        UploadCache uploadCache = UploadCache.getInstence();
        String cache = UploadCache.getInstence().getCache(IMAGE_UPLOAD_TYPE,
                UserPreferences.TokenVerify.getToken());
        if (!cache.equals("")) {
            mContentEdit.setText(cache);
        }
        mContentEdit.setFocusableInTouchMode(true);
        mContentEdit.requestFocus();
        mInputCountTv = (TextView) findViewById(R.id.text_count);
        mImageLayout = (LinearLayout) findViewById(R.id.image_layout);
        mShareBtns = new ToggleButton[3];
        mShareBtns[0] = (ToggleButton) findViewById(R.id.activity_upload_image_share_weibo);
        mShareBtns[1] = (ToggleButton) findViewById(R.id.activity_upload_image_share_moment);
        mShareBtns[2] = (ToggleButton) findViewById(R.id.activity_upload_image_share_qzone);
        mContentEdit.addTextChangedListener(mTextWatcher);

        if (mProgressDialog == null) {
            mProgressDialog = new CustomProgressingDialog(
                    UploadMultiImageActivity.this);
        }

        if ((pathList.size() == 1) && (IMAGE_UPLOAD_TYPE == TYPE_ASK_UPLOAD)) {
            ImageView mImage = new ImageView(this);
            LinearLayout.LayoutParams lpLayoutParams = new LinearLayout.LayoutParams(
                    Utils.dpToPx(mContext, 82), Utils.dpToPx(mContext, 82));

            mImage.setLayoutParams(lpLayoutParams);
            mImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ImageLoader.getInstance().displayImage("file://" + pathList.get(0),
                    mImage);
            mImageLayout.addView(mImage);

            LinearLayout.LayoutParams lpLayoutParamAdd = new LinearLayout.LayoutParams(
                    Utils.dpToPx(mContext, 82), Utils.dpToPx(mContext, 82));
            lpLayoutParamAdd.setMargins(Utils.dpToPx(mContext, 9), 0, 0, 0);
            ImageView mImage2 = new ImageView(this);
            mImage2.setLayoutParams(lpLayoutParamAdd);
            mImage2.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mImage2.setBackgroundResource(R.drawable.add_photo);
            mImageLayout.addView(mImage2);

            mImage2.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(UploadMultiImageActivity.this,
                            MultiImageSelectActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("SelectType", type);
                    intent.putExtras(bundle);
                    intent.putStringArrayListExtra("resultList", pathList);
                    startActivity(intent);
                }
            });
        } else if ((pathList.size() == 1)
                && (IMAGE_UPLOAD_TYPE == TYPE_REPLY_UPLOAD)) {
            ImageView mImage = new ImageView(this);
            LinearLayout.LayoutParams lpLayoutParams = new LinearLayout.LayoutParams(
                    Utils.dpToPx(mContext, 82), Utils.dpToPx(mContext, 82));
            mImage.setLayoutParams(lpLayoutParams);
            mImage.setScaleType(ImageView.ScaleType.CENTER);
            ImageLoader.getInstance().displayImage("file://" + pathList.get(0),
                    mImage);
            mImageLayout.addView(mImage);
        } else {
            for (int i = 0; i < pathList.size(); i++) {

                ImageView mImage = new ImageView(this);
                LinearLayout.LayoutParams lpLayoutParams = new LinearLayout.LayoutParams(
                        Utils.dpToPx(mContext, 82), Utils.dpToPx(mContext, 82));
                if (i == 1) {
                    lpLayoutParams.setMargins(Utils.dpToPx(mContext, 9), 0, 0,
                            0);
                }
                mImage.setLayoutParams(lpLayoutParams);
                mImage.setScaleType(ImageView.ScaleType.CENTER);
                ImageLoader.getInstance().displayImage(
                        "file://" + pathList.get(i), mImage);
                mImageLayout.addView(mImage);
            }
        }

    }

    // 监听标签字数
    private TextWatcher mTextWatcher = new TextWatcher() {
        private CharSequence temp;
        private int editStart;
        private int editEnd;

        @Override
        public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
            temp = s;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int arg1, int arg2,
                                      int arg3) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            editStart = mContentEdit.getSelectionStart();
            editEnd = mContentEdit.getSelectionEnd();

            mInputCountTv.setText(temp.length() + "/18");
            if (temp.length() > 140) {
                showToast(new PSGodToast("！最多输入140个字"));

                s.delete(editStart - 1, editEnd);
                int tempSelection = editStart;
                mContentEdit.setText(s);
                mContentEdit.setSelection(tempSelection);
            }
        }
    };

    private void switchOffShareBtns(int exceptId) {
        for (ToggleButton shareBtn : mShareBtns) {
            if (shareBtn.getId() != exceptId) {
                shareBtn.setChecked(false);
            }
        }
    }

    public void initListener() {
        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    mCheckedShareBtnId = buttonView.getId();
                    switchOffShareBtns(mCheckedShareBtnId);
                } else {
                    mCheckedShareBtnId = -1;
                }
            }
        };

        for (ToggleButton shareBtn : mShareBtns) {
            shareBtn.setOnCheckedChangeListener(listener);
        }

        mFinishBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                contentString = mContentEdit.getText().toString().trim();
                UploadCache.getInstence().clear();

                if (contentString.equals("")) {
                    Toast.makeText(mContext, "请输入描述", Toast.LENGTH_SHORT)
                            .show();
                } else {
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
                        if (imageHeight < 320 || imageWidth < 320) {
                            CustomToast.showError(UploadMultiImageActivity.this, "上传图片尺寸应大于320*320", Toast.LENGTH_LONG);
                            if(mProgressDialog.isShowing()){
                                mProgressDialog.dismiss();
                            }
                            break;
                        }
                        float mRatio = (float) imageHeight / imageWidth;
                        mImageRatioList.add(mRatio);

                        Resources res = getResources();
                        float mScale = (float) (Constants.WIDTH_OF_SCREEN - 2 * res
                                .getDimensionPixelSize(R.dimen.photo_margin))
                                / imageWidth;
                        mImageScaleList.add(mScale);

                        // 上传照片
                        UploadImageRequest.Builder builder = new UploadImageRequest.Builder()
                                .setBitmap(mImageBitmap).setErrorListener(
                                        errorListener);
                        if ((i + 1) == pathList.size()) {
                            builder.setListener(uploadImageListener);
                        } else {
                            builder.setListener(uploadImageListenerId);
                        }
                        UploadImageRequest request = builder.build();
                        request.setTag(TAG);
                        RequestQueue reqeustQueue = PSGodRequestQueue
                                .getInstance(UploadMultiImageActivity.this)
                                .getRequestQueue();
                        reqeustQueue.add(request);
                    }
                }

            }
        });

        mBackBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private Listener<ImageUploadResult> uploadImageListenerId = new Listener<ImageUploadResult>() {

        @Override
        public void onResponse(ImageUploadResult response) {
            mUploadIdList.add(response.id);
        }

    };

    private Listener<ImageUploadResult> uploadImageListener = new Listener<ImageUploadResult>() {

        @Override
        public void onResponse(ImageUploadResult response) {
            mUploadIdList.add(response.id);
            UploadMultiRequest.Builder builder = new UploadMultiRequest.Builder()
                    .setUploadType(IMAGE_UPLOAD_TYPE).setContent(contentString)
                    .setUploadIdList(mUploadIdList)
                    .setRatioList(mImageRatioList).setAskId(mAskId)
                    .setScaleList(mImageScaleList).setListener(uploadListener)
                    .setErrorListener(errorListener);

            UploadMultiRequest request = builder.builder();
            RequestQueue reqeustQueue = PSGodRequestQueue.getInstance(
                    getApplicationContext()).getRequestQueue();
            reqeustQueue.add(request);
        }

    };

    public Listener<MultiUploadResult> uploadListener = new Listener<MultiUploadResult>() {
        @Override
        public void onResponse(MultiUploadResult response) {
            mProgressDialog.dismiss();

            Toast.makeText(mContext, "上传成功", Toast.LENGTH_SHORT).show();

            switch (mCheckedShareBtnId) {
                case R.id.activity_upload_image_share_weibo:
                    Utils.showProgressDialog(mContext);

                    ActionShareRequest.Builder weibobuilder = new ActionShareRequest.Builder()
                            .setShareType("weibo").setType(sharetype)
                            .setId(response.mId).setListener(shareWeiboListener)
                            .setErrorListener(errorListener);

                    ActionShareRequest weiborequest = weibobuilder.build();
                    weiborequest.setTag(TAG);
                    RequestQueue weiborequestQueue = PSGodRequestQueue.getInstance(
                            PSGodApplication.getAppContext()).getRequestQueue();
                    weiborequestQueue.add(weiborequest);
                    break;
                case R.id.activity_upload_image_share_moment:
                    Utils.showProgressDialog(mContext);

                    ActionShareRequest.Builder wechatbuilder = new ActionShareRequest.Builder()
                            .setShareType("wechat_timeline").setType(sharetype)
                            .setId(response.mId).setListener(shareMomentsListener)
                            .setErrorListener(errorListener);

                    ActionShareRequest wechatrequest = wechatbuilder.build();
                    wechatrequest.setTag(TAG);
                    RequestQueue wechatrequestQueue = PSGodRequestQueue
                            .getInstance(PSGodApplication.getAppContext())
                            .getRequestQueue();
                    wechatrequestQueue.add(wechatrequest);
                    break;
                case R.id.activity_upload_image_share_qzone:
                    Utils.showProgressDialog(mContext);

                    ActionShareRequest.Builder qzonebuilder = new ActionShareRequest.Builder()
                            .setShareType("qq_timeline").setType(sharetype)
                            .setId(response.mId).setListener(shareQzoneListener)
                            .setErrorListener(errorListener);

                    ActionShareRequest qzonerequest = qzonebuilder.build();
                    qzonerequest.setTag(TAG);
                    RequestQueue qzonerequestQueue = PSGodRequestQueue.getInstance(
                            PSGodApplication.getAppContext()).getRequestQueue();
                    qzonerequestQueue.add(qzonerequest);
                    break;
                default:
                    if (IMAGE_UPLOAD_TYPE == TYPE_ASK_UPLOAD) {
                        // 新建求P成功后跳转最新求p 页面
                        Intent intent = new Intent(UploadMultiImageActivity.this,
                                MainActivity.class);
                        intent.putExtra(MainActivity.IntentParams.KEY_FRAGMENT_ID,
                                MainActivity.IntentParams.VALUE_FRAGMENT_ID_RECENT);
                        intent.putExtra(
                                MainActivity.IntentParams.KEY_RECENTPAGE_ID,
                                MainActivity.IntentParams.VALUE_RECENTPAGE_ID_ASKS);
                        intent.putExtra(MainActivity.IntentParams.KEY_NEED_REFRESH,
                                true);
                        EventBus.getDefault().post(new MyPageRefreshEvent(0));
                        startActivity(intent);
                    } else {
                        // 新建作品成功后跳转最新作品 页面
                        Intent intent = new Intent(UploadMultiImageActivity.this,
                                MainActivity.class);
                        intent.putExtra(MainActivity.IntentParams.KEY_FRAGMENT_ID,
                                MainActivity.IntentParams.VALUE_FRAGMENT_ID_RECENT);
                        intent.putExtra(
                                MainActivity.IntentParams.KEY_RECENTPAGE_ID,
                                MainActivity.IntentParams.VALUE_RECENTPAGE_ID_WORKS);
                        intent.putExtra(MainActivity.IntentParams.KEY_NEED_REFRESH,
                                true);
                        EventBus.getDefault().post(new MyPageRefreshEvent(1));
                        startActivity(intent);
                    }
                    UploadCache.getInstence().clear();
                    UploadMultiImageActivity.this.finish();
                    break;
            }
        }
    };

    // 微博分享接口请求回调 新浪微博只支持图文／文字
    private Listener<JSONObject> shareWeiboListener = new Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Utils.hideProgressDialog();

            ShareSDK.initSDK(mContext);
            try {
                OnekeyShare oks = new OnekeyShare() {
                    @Override
                    public void onComplete(Platform platform, int action,
                                           HashMap<String, Object> res) {
                        if (IMAGE_UPLOAD_TYPE == TYPE_ASK_UPLOAD) {
                            // 新建求P成功后跳转最新求p 页面
                            Intent intent = new Intent(
                                    UploadMultiImageActivity.this,
                                    MainActivity.class);
                            intent.putExtra(
                                    MainActivity.IntentParams.KEY_FRAGMENT_ID,
                                    MainActivity.IntentParams.VALUE_FRAGMENT_ID_RECENT);
                            intent.putExtra(
                                    MainActivity.IntentParams.KEY_RECENTPAGE_ID,
                                    MainActivity.IntentParams.VALUE_RECENTPAGE_ID_ASKS);
                            intent.putExtra(
                                    MainActivity.IntentParams.KEY_NEED_REFRESH,
                                    true);
                            EventBus.getDefault().post(
                                    new MyPageRefreshEvent(0));
                            startActivity(intent);
                        } else {
                            // 新建作品成功后跳转最新作品 页面
                            Intent intent = new Intent(
                                    UploadMultiImageActivity.this,
                                    MainActivity.class);
                            intent.putExtra(
                                    MainActivity.IntentParams.KEY_FRAGMENT_ID,
                                    MainActivity.IntentParams.VALUE_FRAGMENT_ID_RECENT);
                            intent.putExtra(
                                    MainActivity.IntentParams.KEY_RECENTPAGE_ID,
                                    MainActivity.IntentParams.VALUE_RECENTPAGE_ID_WORKS);
                            intent.putExtra(
                                    MainActivity.IntentParams.KEY_NEED_REFRESH,
                                    true);
                            EventBus.getDefault().post(
                                    new MyPageRefreshEvent(1));
                            startActivity(intent);
                        }
                        UploadCache.getInstence().clear();
                        UploadMultiImageActivity.this.finish();
                    }
                };

                oks.setPlatform(SinaWeibo.NAME);
                oks.disableSSOWhenAuthorize();
                oks.setSilent(false);

                oks.setText(response.getString("desc"));
                oks.setImageUrl(response.getString("image"));
                oks.show(mContext);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    // 微信朋友圈分享接口请求回调
    private Listener<JSONObject> shareMomentsListener = new Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Utils.hideProgressDialog();

            ShareSDK.initSDK(mContext);
            Platform wechat = ShareSDK
                    .getPlatform(mContext, WechatMoments.NAME);
            wechat.setPlatformActionListener(new PlatformActionListener() {
                @Override
                public void onError(Platform arg0, int arg1, Throwable arg2) {
                }

                @Override
                public void onComplete(Platform arg0, int arg1,
                                       HashMap<String, Object> arg2) {
                    if (IMAGE_UPLOAD_TYPE == TYPE_ASK_UPLOAD) {
                        // 新建求P成功后跳转最新求p 页面
                        Intent intent = new Intent(
                                UploadMultiImageActivity.this,
                                MainActivity.class);
                        intent.putExtra(
                                MainActivity.IntentParams.KEY_FRAGMENT_ID,
                                MainActivity.IntentParams.VALUE_FRAGMENT_ID_RECENT);
                        intent.putExtra(
                                MainActivity.IntentParams.KEY_RECENTPAGE_ID,
                                MainActivity.IntentParams.VALUE_RECENTPAGE_ID_ASKS);
                        intent.putExtra(
                                MainActivity.IntentParams.KEY_NEED_REFRESH,
                                true);
                        EventBus.getDefault().post(new MyPageRefreshEvent(0));
                        startActivity(intent);
                    } else {
                        // 新建作品成功后跳转最新作品 页面
                        Intent intent = new Intent(
                                UploadMultiImageActivity.this,
                                MainActivity.class);
                        intent.putExtra(
                                MainActivity.IntentParams.KEY_FRAGMENT_ID,
                                MainActivity.IntentParams.VALUE_FRAGMENT_ID_RECENT);
                        intent.putExtra(
                                MainActivity.IntentParams.KEY_RECENTPAGE_ID,
                                MainActivity.IntentParams.VALUE_RECENTPAGE_ID_WORKS);
                        intent.putExtra(
                                MainActivity.IntentParams.KEY_NEED_REFRESH,
                                true);
                        EventBus.getDefault().post(new MyPageRefreshEvent(1));
                        startActivity(intent);
                    }
                    UploadCache.getInstence().clear();
                    UploadMultiImageActivity.this.finish();
                }

                @Override
                public void onCancel(Platform arg0, int arg1) {
                }
            });

            try {
                if (response.getString("type").equals("image")) {
                    ShareParams sp = new ShareParams();

                    sp.setShareType(Platform.SHARE_IMAGE);
                    sp.setTitle(response.getString("title"));
                    sp.setText(response.getString("desc"));
                    sp.setImageUrl(response.getString("image"));
                    wechat.share(sp);
                }
                if (response.getString("type").equals("url")) {
                    // 图文链接分享
                    ShareParams sp = new ShareParams();

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

    // qzone分享回调 qq空间分享图文
    private Listener<JSONObject> shareQzoneListener = new Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Utils.hideProgressDialog();

            ShareSDK.initSDK(mContext);
            try {
                OnekeyShare oks = new OnekeyShare() {
                    @Override
                    public void onComplete(Platform platform, int action,
                                           HashMap<String, Object> res) {
                        if (IMAGE_UPLOAD_TYPE == TYPE_ASK_UPLOAD) {
                            // 新建求P成功后跳转最新求p 页面
                            Intent intent = new Intent(
                                    UploadMultiImageActivity.this,
                                    MainActivity.class);
                            intent.putExtra(
                                    MainActivity.IntentParams.KEY_FRAGMENT_ID,
                                    MainActivity.IntentParams.VALUE_FRAGMENT_ID_RECENT);
                            intent.putExtra(
                                    MainActivity.IntentParams.KEY_RECENTPAGE_ID,
                                    MainActivity.IntentParams.VALUE_RECENTPAGE_ID_ASKS);
                            intent.putExtra(
                                    MainActivity.IntentParams.KEY_NEED_REFRESH,
                                    true);
                            EventBus.getDefault().post(
                                    new MyPageRefreshEvent(0));
                            startActivity(intent);
                        } else {
                            // 新建作品成功后跳转最新作品 页面
                            Intent intent = new Intent(
                                    UploadMultiImageActivity.this,
                                    MainActivity.class);
                            intent.putExtra(
                                    MainActivity.IntentParams.KEY_FRAGMENT_ID,
                                    MainActivity.IntentParams.VALUE_FRAGMENT_ID_RECENT);
                            intent.putExtra(
                                    MainActivity.IntentParams.KEY_RECENTPAGE_ID,
                                    MainActivity.IntentParams.VALUE_RECENTPAGE_ID_WORKS);
                            intent.putExtra(
                                    MainActivity.IntentParams.KEY_NEED_REFRESH,
                                    true);
                            EventBus.getDefault().post(
                                    new MyPageRefreshEvent(1));
                            startActivity(intent);
                        }
                        UploadCache.getInstence().clear();
                        UploadMultiImageActivity.this.finish();
                    }
                };
                oks.setPlatform(QZone.NAME);

                oks.setTitle(response.getString("title"));
                oks.setTitleUrl(response.getString("url"));
                oks.setText(response.getString("desc"));
                oks.setImageUrl(response.getString("image"));
                // 设置发布分享的网站名称和网址
                oks.setSite(Constants.OFFICAL_APP_NAME);
                oks.setSiteUrl(Constants.OFFICAL_WEBSITE);

                oks.show(mContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private PSGodErrorListener errorListener = new PSGodErrorListener(
            UploadMultiRequest.class.getSimpleName()) {
        @Override
        public void handleError(VolleyError error) {
            Utils.hideProgressDialog();
            mProgressDialog.dismiss();
        }
    };

    private void callInputPanel() {
        // 唤起输入键盘 并输入框取得焦点
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mContentEdit, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        UploadCache.getInstence().setCache(IMAGE_UPLOAD_TYPE,
                mContentEdit.getText().toString());
    }

}
