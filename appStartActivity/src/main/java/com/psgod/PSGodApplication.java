package com.psgod;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import cn.sharesdk.framework.ShareSDK;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.psgod.emoji.FaceConversionUtil;
import com.psgod.model.NotificationBean;
import com.psgod.model.PhotoItem;
import com.psgod.model.notification.SystemNotification;
import com.psgod.ui.activity.AppStartActivity;
import com.psgod.ui.activity.CarouselPhotoDetailActivity;
import com.psgod.ui.activity.CommentListActivity;
import com.psgod.ui.activity.FollowingListActivity;
import com.psgod.ui.activity.MessageLikeActivity;
import com.psgod.ui.activity.MessageSystemActivity;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;

/**
 * 整个应用的入口 负责初始化必要的数据和类
 *
 * @author rayalyuan
 */
public class PSGodApplication extends Application {
    private static final String TAG = PSGodApplication.class.getSimpleName();
    private static Context mAppContext;

    private static PSGodApplication instance;

    private PushAgent mPushAgent;

    public static NotificationBean mBean;
    public static UMessage umsg;

    public static PSGodApplication getInstance() {
        if (instance == null) {
            instance = new PSGodApplication();
        }
        return instance;
    }

    // @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    // @SuppressWarnings("unused")
    @SuppressLint("NewApi")
    @Override
    public void onCreate() {
        // if (Constants.Config.DEVELOPER_MODE && Build.VERSION.SDK_INT >=
        // Build.VERSION_CODES.GINGERBREAD) {
        // StrictMode.setThreadPolicy(new
        // StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().build());
        // StrictMode.setVmPolicy(new
        // StrictMode.VmPolicy.Builder().detectAll().penaltyDeath().build());
        // }
        mAppContext = this.getApplicationContext();

        // 注册crash捕捉上报系统
        CrashHandler catchHandler = CrashHandler.getInstance();
        catchHandler.init(getApplicationContext());

        mPushAgent = PushAgent.getInstance(this);
        mPushAgent.setDebugMode(true);

        /**
         * 该Handler是在IntentService中被调用，故 1.
         * 如果需启动Activity，需添加Intent.FLAG_ACTIVITY_NEW_TASK 2.
         * IntentService里的onHandleIntent方法是并不处于主线程中，因此，如果需调用到主线程，需如下所示;
         * 或者可以直接启动Service
         * */
        UmengMessageHandler messageHandler = new UmengMessageHandler() {
            // 自定义通知处理 更新消息栏小红点数量
            @Override
            public void dealWithNotificationMessage(final Context context,
                                                    final UMessage msg) {
                super.dealWithNotificationMessage(context, msg);
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(msg.custom
                                    .toString());
                            int type = jsonObject.getInt("type");
                            int count = jsonObject.getInt("count");

                            // 发送广播
                            Intent intent = new Intent(
                                    "android.intent.action.PUSH_MESSAGE_BROADCAST");
                            intent.putExtra("type", type);
                            intent.putExtra("count", count);
                            sendBroadcast(intent);

                            // 更新各类推送本地的数量
                            updatePushData(type, count);
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        mPushAgent.setMessageHandler(messageHandler);


        /**
         * 该Handler是在BroadcastReceiver中被调用，故
         * 如果需启动Activity，需添加Intent.FLAG_ACTIVITY_NEW_TASK
         * */
        UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler() {
            @Override
            public void dealWithCustomAction(Context context, UMessage msg) {

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(msg.custom
                            .toString());
                    int type = jsonObject.getInt("type");
//                    int count = jsonObject.getInt("count");
                    Long targetId = jsonObject.getLong("target_id");
                    Long targetAskId = jsonObject.getLong("target_ask_id");
                    int targetType = jsonObject.getInt("target_type");
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    switch (type) {
                        case 0:
                            intent.setClass(getApplicationContext(), MessageSystemActivity.class);
                            break;
                        case 1:
                            intent.setClass(getApplicationContext(), CommentListActivity.class);
                            PhotoItem item = new PhotoItem();
                            item.setPid(targetId);
                            item.setType(targetType);
                            intent.putExtra("Constants.IntentKey.PHOTO_ITEM",item);
                            break;
                        case 2:
                            CarouselPhotoDetailActivity.startActivity(getApplicationContext(),targetAskId,targetId);
                            return;
                        case 3:
                            intent.setClass(getApplicationContext(), FollowingListActivity.class);
                            break;
//                        case 4:
//                            intent.setClass(getApplicationContext(), MessageSystemActivity.class);
//                            break;
                        case 5:
                            intent.setClass(getApplicationContext(), MessageLikeActivity.class);
                            break;
                        default:
                            intent.setClass(getApplicationContext(), AppStartActivity.class);
                            break;
                    }
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                // TODO 打开MainActivity
                // Intent intent = new Intent(Intent.ACTION_MAIN);
                // intent.addCategory(Intent.CATEGORY_LAUNCHER);
                // ComponentName cn = new ComponentName("com.psgod",
                // "com.psgod.ui.activity.AppStartActivity");
                // intent.setComponent(cn);
                // startActivity(intent);
            }
        };
        mPushAgent.setNotificationClickHandler(notificationClickHandler);

        // 获取屏幕的宽度
        Point outSize = new Point();
        WindowManager wm = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);

        if (android.os.Build.VERSION.SDK_INT < 13) {
            wm.getDefaultDisplay().getWidth();
            wm.getDefaultDisplay().getHeight();
        } else {
            wm.getDefaultDisplay().getSize(outSize);
        }
        Constants.WIDTH_OF_SCREEN = outSize.x;
        Constants.HEIGHT_OF_SCREEN = outSize.y;

        initImageLoader(getApplicationContext());

        // 初始化表情数据
        // TODO 放在其他位置
        new Thread(new Runnable() {
            @Override
            public void run() {
                FaceConversionUtil.getInstace().getFileText(mAppContext);
            }
        }).start();

        // 初始化sharesdk
        ShareSDK.initSDK(mAppContext);
        ShareSDK.initSDK(mAppContext);
    }

    // 更新各类推送本地的数量
    public void updatePushData(int type, int count) {
        if (type != -1) {
            switch (type) {
                case Constants.PUSH_MESSAGE_COMMENT:
                    int commentCount = UserPreferences.PushMessage
                            .getPushMessageCount(UserPreferences.PushMessage.PUSH_COMMENT);
                    commentCount = count + commentCount;

                    UserPreferences.PushMessage.setPushMessageCount(
                            UserPreferences.PushMessage.PUSH_COMMENT, commentCount);
                    break;

                case Constants.PUSH_MESSAGE_REPLY:
                    int replyCount = UserPreferences.PushMessage
                            .getPushMessageCount(UserPreferences.PushMessage.PUSH_REPLY);
                    replyCount = count + replyCount;

                    UserPreferences.PushMessage.setPushMessageCount(
                            UserPreferences.PushMessage.PUSH_REPLY, replyCount);
                    break;

                case Constants.PUSH_MESSAGE_FOLLOW:
                    int followCount = UserPreferences.PushMessage
                            .getPushMessageCount(UserPreferences.PushMessage.PUSH_FOLLOW);
                    followCount = count + followCount;

                    UserPreferences.PushMessage.setPushMessageCount(
                            UserPreferences.PushMessage.PUSH_FOLLOW, followCount);
                    break;

                case Constants.PUSH_MESSAGE_LIKE:
                    int likeCount = UserPreferences.PushMessage
                            .getPushMessageCount(UserPreferences.PushMessage.PUSH_LIKE);
                    likeCount = likeCount + count;

                    UserPreferences.PushMessage.setPushMessageCount(
                            UserPreferences.PushMessage.PUSH_LIKE, likeCount);
                    break;

                case Constants.PUSH_MESSAGE_SYSTEM:
                    int systemCount = UserPreferences.PushMessage
                            .getPushMessageCount(UserPreferences.PushMessage.PUSH_SYSTEM);
                    systemCount = count + systemCount;

                    UserPreferences.PushMessage.setPushMessageCount(
                            UserPreferences.PushMessage.PUSH_SYSTEM, systemCount);
                    break;

                default:
                    break;
            }
        }
    }

    public static Context getAppContext() {
        return mAppContext;
    }

    public static NotificationBean getNotificationBean() {
        return mBean;
    }

    /**
     * 配置Universal Image Loader
     *
     * @param context
     */
    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you
        // may tune some of them,
        // or you can create default configuration by
        // ImageLoaderConfiguration.createDefault(this); method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .memoryCacheSize(1024 * 1024 * 2).threadPoolSize(2)
                .memoryCache(new WeakMemoryCache())
                .diskCacheSize(50 * 1024 * 1024)
                        // 50 MB
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs() // Remove for release app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }
}