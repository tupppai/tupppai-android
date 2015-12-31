package com.psgod.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.nineoldandroids.view.ViewHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.psgod.PsGodImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.psgod.BitmapUtils;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.UserPreferences;
import com.psgod.Utils;
import com.psgod.eventbus.InitEvent;
import com.psgod.eventbus.MyInfoRefreshEvent;
import com.psgod.eventbus.PushEvent;
import com.psgod.eventbus.UpdateTabStatusEvent;
import com.psgod.model.LoginUser;
import com.psgod.model.User;
import com.psgod.network.request.GetUserInfoRequest;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.ui.activity.EditProfileActivity;
import com.psgod.ui.activity.FollowerListActivity;
import com.psgod.ui.activity.FollowingListActivity;
import com.psgod.ui.activity.NewMessageActivity;
import com.psgod.ui.activity.SettingActivity;
import com.psgod.ui.adapter.SlidingPageMyAdapter;
import com.psgod.ui.view.PagerSlidingTabStrip;
import com.psgod.ui.widget.AvatarImageView;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;
import com.psgod.ui.widget.dialog.ImageDialog;

import org.json.JSONObject;

import de.greenrobot.event.EventBus;

/**
 * 我的Tab v2.0
 *
 * @author ZouMengyuan
 */
public class MyPageFragment extends Fragment implements
        ScrollTabHolder {
    private static final String TAG = MyPageFragment.class.getSimpleName();
    private Context mContext;
    private ViewHolder mViewHolder;

    private int scrollY;
    private int headerHeight;
    private int headerTranslationDis;

    PsGodImageLoader loader = PsGodImageLoader.getInstance();
    private DisplayImageOptions mAvatarOptions = Constants.DISPLAY_IMAGE_OPTIONS_AVATAR;

    private View mMessageTipView;

    // @Override
    // public void onHiddenChanged(boolean hidden) {
    // super.onHiddenChanged(hidden);
    //
    // if(!hidden){
    // initMyFragmentData();
    // }
    // }

    @Override
    public void onHiddenChanged(boolean hidden) {
        // TODO Auto-generated method stub
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        // 我的页面创建标志
        Constants.IS_USER_FRAGMENT_CREATED = true;
        mContext = getActivity();

        initViews();
        initListeners();

        // 初始化viewpager和tabs
        setupPager();
        setupTabs();

        if ((dialog != null) || (!dialog.isShowing())) {
            dialog.show();
        }
        initMyFragmentData();
    }

    public void onEventMainThread(InitEvent event) {
        initMyFragmentData();
    }

    // EventBus 接收推送状态变化
    public void onEventMainThread(PushEvent event) {
        if (event.pushObjectType == PushEvent.TYPE_ACT_MAIN) {
            showTipView();
        } else if (event.pushObjectType == PushEvent.TYPE_FRAG_PAGE) {
            int type = event.getPushType();
            int count = event.getPushCount();
            switch (type) {
                // 消息页面内的消息提醒更新
                case Constants.PUSH_MESSAGE_COMMENT:
                case Constants.PUSH_MESSAGE_FOLLOW:
                    // case Constants.PUSH_MESSAGE_INVITE:
                case Constants.PUSH_MESSAGE_REPLY:
                case Constants.PUSH_MESSAGE_SYSTEM:
                case Constants.PUSH_MESSAGE_LIKE:
                    if (Constants.IS_MESSAGE_FRAGMENT_CREATED) {
                        setMessageTip(type, count);
                    }
                    break;

                default:
                    break;
            }
        }
    }

    // EventBus 点击我的tab 刷新
    public void onEventMainThread(MyInfoRefreshEvent event) {
        initMyFragmentData();
    }

    public void showTipView() {
        mMessageTipView.setVisibility(View.VISIBLE);
        // 设置是否点击 我的 消息按钮
        Constants.IS_MESSAGE_NEW_PUSH_CLICK = false;
    }

    private void initViews() {
        FrameLayout parentview = new FrameLayout(getActivity());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        parentview.setLayoutParams(params);

        mViewHolder = new ViewHolder();
        mViewHolder.mParentView = parentview;
        mViewHolder.mView = LayoutInflater.from(getActivity()).inflate(
                R.layout.fragment_my_page, parentview, true);

        headerHeight = getResources().getDimensionPixelSize(
                R.dimen.max_header_height);
        headerTranslationDis = -getResources().getDimensionPixelSize(
                R.dimen.header_offset_dis);

        mViewHolder.mActionBar = (RelativeLayout) mViewHolder.mView.
                findViewById(R.id.fragment_my_page_title_layout);
        mViewHolder.mTabsTrips = (PagerSlidingTabStrip) mViewHolder.mView
                .findViewById(R.id.my_profile_tabs);
        mViewHolder.viewPager = (ViewPager) mViewHolder.mView
                .findViewById(R.id.my_view_pager);
        mViewHolder.mLinearHeader = (LinearLayout) mViewHolder.mView
                .findViewById(R.id.my_profile_header);
        mViewHolder.mTitle = (TextView) mViewHolder.mView.findViewById(R.id.title_tv);
        mViewHolder.mSettingButton = (ImageButton) mViewHolder.mView
                .findViewById(R.id.setting_btn);
        mViewHolder.mMessageButton = (ImageButton) mViewHolder.mView
                .findViewById(R.id.message_btn);
        mViewHolder.mFollowingTv = (TextView) mViewHolder.mView
                .findViewById(R.id.my_profile_user_following_count);
        mViewHolder.mFollowerTv = (TextView) mViewHolder.mView
                .findViewById(R.id.my_profile_user_followers_count);
        mViewHolder.mNickNameText = (TextView) mViewHolder.mView
                .findViewById(R.id.nickname_text);
        mViewHolder.mNickNameVip = (ImageView) mViewHolder.mView
                .findViewById(R.id.nickname_vip);
        mViewHolder.mFollowingLayout = (RelativeLayout) mViewHolder.mView
                .findViewById(R.id.layout_following);
        mViewHolder.mFollowerLayout = (RelativeLayout) mViewHolder.mView
                .findViewById(R.id.layout_followers);
        mViewHolder.mLikeTv = (TextView) mViewHolder.mView
                .findViewById(R.id.my_profile_user_like_count);
        mViewHolder.mAvatarIv = (AvatarImageView) mViewHolder.mView
                .findViewById(R.id.my_profile_avatar);
        mMessageTipView = mViewHolder.mView
                .findViewById(R.id.fragment_my_page_message_tip);

        if (dialog == null) {
            dialog = new CustomProgressingDialog(
                    getActivity());
        }
        dialog = new CustomProgressingDialog(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
//		if (Constants.HAS_CHANGE_PHOTO == true) {
//			initMyFragmentData();
//		}
        initMyFragmentData();

        int mMessageCount = UserPreferences.PushMessage
                .getPushMessageCount(UserPreferences.PushMessage.PUSH_COMMENT)
                + UserPreferences.PushMessage
                .getPushMessageCount(UserPreferences.PushMessage.PUSH_REPLY)
                + UserPreferences.PushMessage
                .getPushMessageCount(UserPreferences.PushMessage.PUSH_FOLLOW)
                + UserPreferences.PushMessage
                .getPushMessageCount(UserPreferences.PushMessage.PUSH_SYSTEM)
                + UserPreferences.PushMessage
                .getPushMessageCount(UserPreferences.PushMessage.PUSH_LIKE);

        if ((mMessageCount != 0)
                && (Constants.IS_MESSAGE_NEW_PUSH_CLICK == false)) {
            mMessageTipView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FrameLayout parentview = new FrameLayout(getActivity());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        parentview.setLayoutParams(params);

        mViewHolder.mParentView.removeView(mViewHolder.mView);
        parentview.addView(mViewHolder.mView);

        mViewHolder.mParentView = parentview;
        return parentview;
    }

    private CustomProgressingDialog dialog;

    // 初始化用户个人数据
    public void initMyFragmentData() {
        if (mViewHolder.mAvatarIv == null) {
            initViews();
        }

        // 先加载本地数据
        LoginUser user = LoginUser.getInstance();
        mViewHolder.mAvatarIv.setUser(new User(user));
        if (user != null && user.getAvatarImageUrl() != null
                && user.getAvatarImageUrl() != "") {
            loader.displayImage(user.getAvatarImageUrl(),
                    mViewHolder.mAvatarIv, mAvatarOptions, imageLoadingListener);
        }

        mViewHolder.mFollowingTv.setText(Integer.toString(user
                .getFollowingCount()));
        mViewHolder.mFollowerTv.setText(Integer.toString(user
                .getFollowerCount()));
        mViewHolder.mLikeTv.setText(Integer.toString(user.getLikedCount()));
        mViewHolder.mNickNameText.setText(user.getNickname());
        if (user.isStar()) {
            mViewHolder.mNickNameVip.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.mNickNameVip.setVisibility(View.GONE);
        }
        // 请求后台用户数据进行更新
        GetUserInfoRequest.Builder builder = new GetUserInfoRequest.Builder()
                .setListener(getUserInfoListener);

        GetUserInfoRequest request = builder.build();
        request.setTag(TAG);
        RequestQueue requestQueue = PSGodRequestQueue
                .getInstance(getActivity()).getRequestQueue();
        requestQueue.add(request);
    }

    // 获取用户后台信息之后回调
    private Listener<JSONObject> getUserInfoListener = new Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            if (response != null) {
                // TODO 效率不太高 待改进
                LoginUser.getInstance().initFromJSONObject(response);
                LoginUser user = LoginUser.getInstance();

                if (user.getAvatarImageUrl().equals("")) {
                } else {
                    loader.displayImage(user.getAvatarImageUrl(),
                            mViewHolder.mAvatarIv, mAvatarOptions,
                            imageLoadingListener);
                }

                mViewHolder.mFollowingTv.setText(Integer.toString(user
                        .getFollowingCount()));
                mViewHolder.mFollowerTv.setText(Integer.toString(user
                        .getFollowerCount()));
                mViewHolder.mLikeTv.setText(Integer.toString(user
                        .getLikedCount()));
                mViewHolder.mNickNameText.setText(user.getNickname());
                mViewHolder.mTitle.setText(user.getNickname());
                if (user.isStar()) {
                    mViewHolder.mNickNameVip.setVisibility(View.VISIBLE);
                } else {
                    mViewHolder.mNickNameVip.setVisibility(View.GONE);
                }
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }
    };

    private float pageY0 = 0;
    private float pageY1 = 0;
    private boolean isFirst = true;

    private void setupPager() {
        mViewHolder.adapter = new SlidingPageMyAdapter(getActivity()
                .getSupportFragmentManager(), mContext, mViewHolder.viewPager);
        mViewHolder.adapter.setTabHolderScrollingListener(this);
        mViewHolder.viewPager.setOffscreenPageLimit(mViewHolder.adapter
                .getCacheCount());
        mViewHolder.viewPager.setAdapter(mViewHolder.adapter);
        mViewHolder.viewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mViewHolder.mTabsTrips.onPageScrolled(position, positionOffset,
                        positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                mViewHolder.mTabsTrips.onPageSelected(position);
                reLocation = true;
                SparseArrayCompat<ScrollTabHolder> scrollTabHolders = mViewHolder.adapter
                        .getScrollTabHolders();
                ScrollTabHolder currentHolder = scrollTabHolders.valueAt(position);

                if (position == 0) {
                    pageY1 = mViewHolder.mLinearHeader.getY();
                    mViewHolder.mActionBar.setBackgroundColor(Color.parseColor(pauseColorString(colorLeft, true)));
                    mViewHolder.mNickNameText.setTextColor(Color.parseColor(pauseColorString(255 - colorLeft, false)));
                    mViewHolder.mTitle.setTextColor(Color.parseColor(pauseColorString(255 - (colorLeft - 245) * 13, false)));
                    if (colorLeft < 230) {
                        mViewHolder.mTitle.setVisibility(View.INVISIBLE);
                        mViewHolder.mNickNameText.setVisibility(View.VISIBLE);
                    } else {
                        mViewHolder.mTitle.setVisibility(View.VISIBLE);
                        mViewHolder.mNickNameText.setVisibility(View.INVISIBLE);
                    }
                } else {
                    pageY0 = mViewHolder.mLinearHeader.getY();
                    mViewHolder.mActionBar.setBackgroundColor(Color.parseColor(pauseColorString(colorRight, true)));
                    mViewHolder.mNickNameText.setTextColor(Color.parseColor(pauseColorString(255 - colorRight, false)));
                    mViewHolder.mTitle.setTextColor(Color.parseColor(pauseColorString(255 - (colorRight - 245) * 13, false)));
                    if (colorRight < 230) {
                        mViewHolder.mTitle.setVisibility(View.INVISIBLE);
                        mViewHolder.mNickNameText.setVisibility(View.VISIBLE);
                    } else {
                        mViewHolder.mTitle.setVisibility(View.VISIBLE);
                        mViewHolder.mNickNameText.setVisibility(View.INVISIBLE);
                    }
                }


                if (isFirst) {
                    ViewHelper.setTranslationY(mViewHolder.mLinearHeader, 0);
                    isFirst = false;
                } else if
                        (position == 0) {
                    mViewHolder.mLinearHeader.setY(pageY0);
                } else {
                    mViewHolder.mLinearHeader.setY(pageY1);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                mViewHolder.mTabsTrips.onPageScrollStateChanged(state);
            }
        });
    }

    private void setupTabs() {
        mViewHolder.mTabsTrips.setShouldExpand(true);
        mViewHolder.mTabsTrips.setIndicatorColorResource(R.color.color_aeb9bd);
        mViewHolder.mTabsTrips.setUnderlineColorResource(R.color.color_aeb9bd);
        mViewHolder.mTabsTrips.setUnderlineHeight(5);
        mViewHolder.mTabsTrips.setIndicatorHeight(10);
        mViewHolder.mTabsTrips.setCheckedTextColorResource(R.color.black);
        mViewHolder.mTabsTrips.setTextSize(Utils.dpToPx(mContext, 15));
        mViewHolder.mTabsTrips.setViewPager(mViewHolder.viewPager);
    }

    @Override
    public void adjustScroll(int scrollHeight) {

    }

    private boolean reLocation = false;

    private int headerScrollSize = 0;

    public static final boolean NEED_RELAYOUT = Integer.valueOf(
            Build.VERSION.SDK).intValue() < Build.VERSION_CODES.HONEYCOMB; // 是否超过3.0版本

    private int headerTop = 0;
    private int colorLeft;
    private int colorRight;

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount, int pagePosition) {
        if (mViewHolder.viewPager.getCurrentItem() != pagePosition) {
            return;
        }
        if (headerScrollSize == 0 && reLocation) {
            reLocation = false;
            return;
        }
        reLocation = false;
        scrollY = Math.max(-getScrollY(view), headerTranslationDis);
        int color = (int) ((float) scrollY / (float) headerTranslationDis * 255f) * 2;
        if (pagePosition == 0) {
            colorLeft = color;
        } else {
            colorRight = color;
        }
        mViewHolder.mActionBar.setBackgroundColor(Color.parseColor(pauseColorString(color, true)));
        mViewHolder.mNickNameText.setTextColor(Color.parseColor(pauseColorString(255 - color / 2, false)));
        if (color > 150) {
            mViewHolder.mMessageButton.setImageResource(R.mipmap.ic_message_black);
            mViewHolder.mSettingButton.setImageResource(R.mipmap.ic_my_setting_white);
        } else {
            mViewHolder.mMessageButton.setImageResource(R.mipmap.ic_message);
            mViewHolder.mSettingButton.setImageResource(R.mipmap.ic_my_setting);
        }
        if (color < 230) {
            mViewHolder.mTitle.setVisibility(View.INVISIBLE);
            mViewHolder.mNickNameText.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.mTitle.setVisibility(View.VISIBLE);
            mViewHolder.mNickNameText.setVisibility(View.INVISIBLE);
        }
        mViewHolder.mTitle.setTextColor(Color.parseColor(pauseColorString(255 - (color - 245) * 13, false)));

//        scrollY = -getScrollY(view);
        if (NEED_RELAYOUT) {
            headerTop = scrollY;
            mViewHolder.mLinearHeader.post(new Runnable() {
                @Override
                public void run() {
                    mViewHolder.mLinearHeader.layout(0, headerTop,
                            mViewHolder.mLinearHeader.getWidth(),
                            headerTop + mViewHolder.mLinearHeader.getHeight());
                }
            });
        } else {
            ViewHelper.setTranslationY(mViewHolder.mLinearHeader, scrollY);
//            mLinearHeader.setY(originHeaderY + scrollY);
        }
    }

    private String pauseColorString(int color, boolean hasAlpha) {
        String colorStr;
        String thumb = Integer.toHexString(color < 0 ? 0 : color > 255 ? 255 : color);
        if (thumb.length() < 2) {
            thumb = "0" + thumb;
        }
        if (hasAlpha) {
            colorStr = String.format("#%sFFFFFF", thumb);
        } else {
            colorStr = String.format("#%s%s%s", thumb, thumb, thumb);
        }
        return colorStr;
    }


    boolean once2 = true;
    boolean once3 = true;
    View c1 = null;
    View c2 = null;
    View c3 = null;

    public int getScrollY(AbsListView view) {
        // ListView中

        int top = 0;
        if (view instanceof ListView) {
            c1 = view.getChildAt(0);
            if (c1 == null) {
                return 0;
            }
            top = c1.getTop();
            int firstVisiblePosition = view.getFirstVisiblePosition();

            // 索引从刷新头部开始算起
            if (firstVisiblePosition == 0) {
                return -top + headerScrollSize;
            } else if (firstVisiblePosition == 1) {
                return -top;
            } else {
                return -top + (firstVisiblePosition - 2) * c1.getHeight()
                        + headerHeight;
            }
        } else {
            if (view.getTag().toString().equals("MyPageReply")) {
                if (once3) {
                    c3 = view.getChildAt(0);
                    once3 = false;
                    top = c3.getTop();
                }
                if (c3 == null) {
                    return 0;
                }
                top = c3.getTop();
                int firstVisiblePosition = view.getFirstVisiblePosition();

                // 索引从刷新头部开始算起
                if (firstVisiblePosition == 0) {
                    return -top + headerScrollSize;
                } else if (firstVisiblePosition == 1) {
                    return -top;
                } else {
                    return -top + (firstVisiblePosition - 2) * c3.getHeight()
                            + headerHeight;
                }
            } else if (view.getTag().toString().equals("MyPageAsk")) {
                if (once2) {
                    c2 = view.getChildAt(0);
                    once2 = false;
                    top = c2.getTop();
                }
                if (c2 == null) {
                    return 0;
                }
                top = c2.getTop();
                int firstVisiblePosition = view.getFirstVisiblePosition();

                // 索引从刷新头部开始算起
                if (firstVisiblePosition == 0) {
                    return -top + headerScrollSize;
                } else if (firstVisiblePosition == 1) {
                    return -top;
                } else {
                    return -top + (firstVisiblePosition - 2) * c2.getHeight()
                            + headerHeight;
                }
            } else {
                return 0;
            }

        }

    }

    @Override
    public void onHeaderScroll(boolean isRefreashing, int value,
                               int pagePosition) {
        if (mViewHolder.viewPager.getCurrentItem() != pagePosition) {
            return;
        }
        headerScrollSize = value;
        if (NEED_RELAYOUT) {
            mViewHolder.mLinearHeader.post(new Runnable() {

                @Override
                public void run() {
                    Log.e("Main", "scorry=" + (-headerScrollSize));
                    mViewHolder.mLinearHeader.layout(
                            0,
                            -headerScrollSize,
                            mViewHolder.mLinearHeader.getWidth(),
                            -headerScrollSize
                                    + mViewHolder.mLinearHeader.getHeight());
                }
            });
        } else {
            ViewHelper.setTranslationY(mViewHolder.mLinearHeader, -value);
        }
    }

    private void initListeners() {

        mViewHolder.mSettingButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SettingActivity.class);
                mContext.startActivity(intent);
            }
        });

        mViewHolder.mMessageButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, NewMessageActivity.class);
                mContext.startActivity(intent);
                // 点击之后重置本地消息数据 清零
                resetMessageStatus();
                Constants.IS_MESSAGE_NEW_PUSH_CLICK = true;
            }
        });

        mViewHolder.mFollowingLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                Intent intent = new Intent(activity,
                        FollowingListActivity.class);
                activity.startActivity(intent);
            }
        });

        mViewHolder.mFollowerLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                Intent intent = new Intent(activity, FollowerListActivity.class);
                activity.startActivity(intent);
            }
        });

        mViewHolder.mAvatarIv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
//                ImageDialog dialog = new ImageDialog(getActivity(),
//                        ((AvatarImageView) view).getImage());
//                dialog.show();
                Intent intent = new Intent(mContext, EditProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 图片加载回调 将图片毛玻璃化处理后作为背景
     */
    private ImageLoadingListener imageLoadingListener = new ImageLoadingListener() {
        @Override
        public void onLoadingComplete(String imageUri, View view,
                                      Bitmap loadedImage) {
            mViewHolder.mLinearHeader.setBackground(new BitmapDrawable(
                    getResources(), BitmapUtils.getBlurBitmap(loadedImage)));
        }

        @Override
        public void onLoadingCancelled(String arg0, View arg1) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onLoadingStarted(String arg0, View arg1) {
            // TODO Auto-generated method stub
        }
    };

    // 更新上方消息icon小红点状态
    public void setMessageTip(int type, int count) {
        mMessageTipView.setVisibility(View.VISIBLE);
    }

    // 点击消息按钮 本地数据清零 (系统消息和赞消息不清零)
    private void resetMessageStatus() {
        UserPreferences.PushMessage.setPushMessageCount(
                UserPreferences.PushMessage.PUSH_COMMENT, 0);
        UserPreferences.PushMessage.setPushMessageCount(
                UserPreferences.PushMessage.PUSH_FOLLOW, 0);
        UserPreferences.PushMessage.setPushMessageCount(
                UserPreferences.PushMessage.PUSH_REPLY, 0);
        // UserPreferences.PushMessage.setPushMessageCount(
        // UserPreferences.PushMessage.PUSH_SYSTEM, 0);

        mMessageTipView.setVisibility(View.INVISIBLE);

        // EventBus通知状态栏 更新底部tab的状态
        EventBus.getDefault().post(new UpdateTabStatusEvent());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private static class ViewHolder {
        ViewGroup mParentView;
        View mView;
        ImageButton mSettingButton;
        ImageButton mMessageButton;
        TextView mNickNameText;
        ImageView mNickNameVip;
        TextView mTitle;
        TextView mFollowingTv;
        TextView mFollowerTv;
        TextView mLikeTv;
        RelativeLayout mFollowingLayout;
        RelativeLayout mFollowerLayout;
        RelativeLayout mActionBar;
        AvatarImageView mAvatarIv;
        // 左右滑动tab
        private PagerSlidingTabStrip mTabsTrips;
        private ViewPager viewPager;
        // 头部区域
        private LinearLayout mLinearHeader;
        // viewpager adapter
        private SlidingPageMyAdapter adapter;
    }

}
