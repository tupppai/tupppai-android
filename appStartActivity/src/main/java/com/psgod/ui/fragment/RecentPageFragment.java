package com.psgod.ui.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.psgod.Constants;
import com.psgod.Logger;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.eventbus.RefreshEvent;
import com.psgod.ui.activity.MainActivity;
import com.psgod.ui.adapter.RecentPageAdapter;
import com.psgod.ui.widget.dialog.CameraPopupwindow;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 最新tab v2.0
 *
 * @author brandwang
 */
public class RecentPageFragment extends Fragment {
    private static final String TAG = RecentPageFragment.class.getSimpleName();

    public static final int REQUEST_TAKE_PHOTO = 0x770;
    public static final int REQUEST_CHOOSE_PHOTO = 0x771;

    private final int COUNT_OF_FRAGMENTS = 3;
    private final int[] TAB_RADIO_BUTTONS_ID = {
            R.id.fragment_recentpage_asks_radio_btn,
            R.id.fragment_recentpage_works_radio_btn,
            R.id.fragment_recentpage_act_radio_btn
    };

    private ViewHolder mViewHolder;
    private RecentPageAdapter mPhotoListPagerAdapter;

    // 游标偏移距离
    private int mCurSorOffset;
    // 游标宽度
    private int mCursorWidth;

    private OnRefreshListener onRefreshListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout parentView = new FrameLayout(getActivity());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        parentView.setLayoutParams(params);

        mViewHolder = new ViewHolder();
        mViewHolder.mParentView = parentView;
        mViewHolder.mView = LayoutInflater.from(getActivity()).inflate(
                R.layout.fragment_recent_page, parentView, true);
        // mViewHolder.mCameraBtn = (ImageButton) mViewHolder.mView
        // .findViewById(R.id.fragment_homepage_camera_btn);
        mViewHolder.viewPager = (ViewPager) mViewHolder.mView
                .findViewById(R.id.fragment_recentpage_view_pager);
        mViewHolder.tabRadioGroup = (RadioGroup) mViewHolder.mView
                .findViewById(R.id.fragment_recentpage_tab_radio_group);

        mViewHolder.tabRadioButtonAsk = (RadioButton) mViewHolder.mView
                .findViewById(R.id.fragment_recentpage_asks_radio_btn);
        mViewHolder.tabRadioButtonWorks = (RadioButton) mViewHolder.mView
                .findViewById(R.id.fragment_recentpage_works_radio_btn);
        mViewHolder.tabRadioButtonAct = (RadioButton) mViewHolder.
                mView.findViewById(R.id.fragment_recentpage_act_radio_btn);

        mViewHolder.mCursor = (ImageView) mViewHolder.mView
                .findViewById(R.id.fragment_recentpage_cursor);

        initCursor();

        List<Fragment> fragments = new ArrayList<Fragment>();
        if (mViewHolder.mRecentPageAsksFragment == null) {
            mViewHolder.mRecentPageAsksFragment = new RecentPageAsksFragment();
        }
        if (mViewHolder.mRecentPageWorksFragment == null) {
            mViewHolder.mRecentPageWorksFragment = new RecentPageWorksFragment();
        }
        if (mViewHolder.mRecentPageActFragment == null) {
            mViewHolder.mRecentPageActFragment = new RecentPageActFragment();
        }

        fragments.add(mViewHolder.mRecentPageAsksFragment);
        fragments.add(mViewHolder.mRecentPageWorksFragment);
        fragments.add(mViewHolder.mRecentPageActFragment);

        mPhotoListPagerAdapter = new RecentPageAdapter(getActivity()
                .getSupportFragmentManager(), fragments);

        mViewHolder.viewPager.setAdapter(mPhotoListPagerAdapter);
        initListeners();

    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int id = msg.what;
            if (id == MainActivity.IntentParams.VALUE_RECENTPAGE_ID_ASKS) {
                mViewHolder.viewPager.setCurrentItem(0);
            } else if (id == MainActivity.IntentParams.VALUE_RECENTPAGE_ID_WORKS) {
                mViewHolder.viewPager.setCurrentItem(1);
            }

            // 触发自动下拉刷新
            switch (id) {
                case MainActivity.IntentParams.VALUE_RECENTPAGE_ID_ASKS:
                    EventBus.getDefault().post(new RefreshEvent(RecentPageAsksFragment.class.getName()));
                    break;

                case MainActivity.IntentParams.VALUE_HOMEPAGE_ID_FOCUS:
                    EventBus.getDefault().post(new RefreshEvent(RecentPageWorksFragment.class.getName()));
                    break;

                default:
                    break;
            }
        }
    };

    public void onNewIntent(Intent intent) {

        // TODO 未初始化的情况
        if (intent == null) {
            // TODO 输出日志
            return;
        }

        final int id = intent.getIntExtra(
                MainActivity.IntentParams.KEY_RECENTPAGE_ID, -1);

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(id);
            }
        }).start();
    }

    public interface OnRefreshListener {
        public void onRefresh(RecentPageFragment fragment);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Logger.logMethod(TAG, "onCreateView");
        FrameLayout parentView = new FrameLayout(getActivity());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        parentView.setLayoutParams(params);
        mViewHolder.mParentView.removeView(mViewHolder.mView);
        parentView.addView(mViewHolder.mView);
        mViewHolder.mParentView = parentView;
        mPhotoListPagerAdapter.notifyDataSetChanged();

        return parentView;
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

    }

    // 初始化顶部RadioGroup游标
    private void initCursor() {
        mCursorWidth = Utils.dpToPx(getActivity(), 38);
        // 游标左侧偏移量
        mCurSorOffset = Constants.WIDTH_OF_SCREEN / 3 - mCursorWidth
                - Utils.dpToPx(getActivity(), 18);
        Matrix matrix = new Matrix();
        matrix.setTranslate(mCurSorOffset, 0);
        mViewHolder.mCursor.setImageMatrix(matrix);
    }

    private void initListeners() {
        // 双击顶部radio button自动回顶部刷新 最近->求p
        mViewHolder.tabRadioButtonAct.setOnTouchListener(new OnTouchListener() {
            int count = 0;
            int firClick = 0;
            int secClick = 0;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    count++;

                    if (count == 1) {
                        firClick = (int) System.currentTimeMillis();
                    } else if (count == 2) {
                        secClick = (int) System.currentTimeMillis();
                        if (secClick - firClick < 1000) {
                            // 双击事件 下拉刷新首页热门列表
                            EventBus.getDefault().post(new RefreshEvent(RecentPageActFragment.class.getName()));
                        }
                        count = 0;
                        firClick = 0;
                        secClick = 0;
                    }
                }
                return false;
            }

        });
        mViewHolder.tabRadioButtonAsk
                .setOnTouchListener(new OnTouchListener() {
                    int count = 0;
                    int firClick = 0;
                    int secClick = 0;

                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        if (MotionEvent.ACTION_DOWN == event.getAction()) {
                            count++;

                            if (count == 1) {
                                firClick = (int) System.currentTimeMillis();
                            } else if (count == 2) {
                                secClick = (int) System.currentTimeMillis();
                                if (secClick - firClick < 1000) {
                                    // 双击事件 下拉刷新首页热门列表
                                    EventBus.getDefault().post(new RefreshEvent(RecentPageAsksFragment.class.getName()));
                                }
                                count = 0;
                                firClick = 0;
                                secClick = 0;
                            }
                        }
                        return false;
                    }
                });

        // 双击顶部radio button自动回顶部刷新 求P
        mViewHolder.tabRadioButtonWorks
                .setOnTouchListener(new OnTouchListener() {
                    int count = 0;
                    int firClick = 0;
                    int secClick = 0;

                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        if (MotionEvent.ACTION_DOWN == event.getAction()) {
                            count++;

                            if (count == 1) {
                                firClick = (int) System.currentTimeMillis();
                            } else if (count == 2) {
                                secClick = (int) System.currentTimeMillis();
                                if (secClick - firClick < 1000) {
                                    // 双击事件 下拉刷新首页热门列表
                                    EventBus.getDefault().post(new RefreshEvent(RecentPageWorksFragment.class.getName()));
                                }
                                count = 0;
                                firClick = 0;
                                secClick = 0;
                            }
                        }
                        return false;
                    }
                });

        // mViewHolder.mCameraBtn.setOnClickListener(new OnClickListener() {
        // @Override
        // public void onClick(View v) {
        //
        // showCameraPopupwindow(v); //弹出选择上传求P还是作品的PopupWindow
        //
        // // if (mViewHolder.mCameraDialog == null) {
        // // mViewHolder.mCameraDialog = new CameraDialog(getActivity());
        // // }
        // //
        // // if (mViewHolder.mCameraDialog.isShowing()) {
        // // mViewHolder.mCameraDialog.dismiss();
        // // } else {
        // // mViewHolder.mCameraDialog.show();
        // //
        // // }
        // }
        // });

        mViewHolder.tabRadioGroup
                .setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        for (int ix = 0; ix < COUNT_OF_FRAGMENTS; ++ix) {
                            if (TAB_RADIO_BUTTONS_ID[ix] == checkedId) {
                                mViewHolder.viewPager.setCurrentItem(ix);
                                break;
                            }
                        }
                    }
                });

        mViewHolder.viewPager
                .setOnPageChangeListener(new OnPageChangeListener() {
                    int mCursorMoving = Utils.dpToPx(getActivity(), 98);

                    @Override
                    public void onPageScrollStateChanged(int index) {
                        // Do Nothing
                    }

                    @Override
                    public void onPageScrolled(int arg0, float arg1, int arg2) {
                        // Do Nothing
                    }

                    @Override
                    public void onPageSelected(int index) {
                        mViewHolder.tabRadioGroup
                                .check(TAB_RADIO_BUTTONS_ID[index]);

                        Animation animation = null;

                        switch (index) {
                            case 0:
                                if (Constants.CURRENT_RECENTPAGE_TAB == 1) {
                                    animation = new TranslateAnimation(
                                            mCursorMoving, 0, 0, 0);
                                    animation.setFillAfter(true);
                                    animation.setDuration(300);
                                    mViewHolder.mCursor.setAnimation(animation);
                                } else if (Constants.CURRENT_RECENTPAGE_TAB == 2) {
                                    animation = new TranslateAnimation(
                                            mCursorMoving * 2, 0, 0, 0);
                                    animation.setFillAfter(true);
                                    animation.setDuration(300);
                                    mViewHolder.mCursor.setAnimation(animation);
                                }
                                break;

                            case 1:
                                if (Constants.CURRENT_RECENTPAGE_TAB == 0) {
                                    animation = new TranslateAnimation(0,
                                            mCursorMoving, 0, 0);
                                    animation.setFillAfter(true);
                                    animation.setDuration(300);
                                    mViewHolder.mCursor.setAnimation(animation);
                                } else if (Constants.CURRENT_RECENTPAGE_TAB == 2) {
                                    animation = new TranslateAnimation(
                                            mCursorMoving * 2, mCursorMoving, 0, 0);
                                    animation.setFillAfter(true);
                                    animation.setDuration(300);
                                    mViewHolder.mCursor.setAnimation(animation);
                                }
                                break;
                            case 2:
                                if (Constants.CURRENT_RECENTPAGE_TAB == 1) {
                                    animation = new TranslateAnimation(mCursorMoving,
                                            mCursorMoving * 2, 0, 0);
                                    animation.setFillAfter(true);
                                    animation.setDuration(300);
                                    mViewHolder.mCursor.setAnimation(animation);
                                } else if (Constants.CURRENT_RECENTPAGE_TAB == 0) {
                                    animation = new TranslateAnimation(0,
                                            mCursorMoving * 2, 0, 0);
                                    animation.setFillAfter(true);
                                    animation.setDuration(300);
                                    mViewHolder.mCursor.setAnimation(animation);
                                }
                                break;
                            default:
                                break;
                        }
                        // 设置首页当前tab
                        Constants.CURRENT_RECENTPAGE_TAB = index;
                    }
                });
    }

    private void showCameraPopupwindow(View view) {
        if (null == mViewHolder.cameraPopupwindow) {
            mViewHolder.cameraPopupwindow = new CameraPopupwindow(getActivity());
        }

        mViewHolder.cameraPopupwindow.showCameraPopupwindow(view);
    }

    /**
     * 保存视图组件，避免视图的重复加载
     *
     * @author Rayal
     */
    private static class ViewHolder {
        ViewGroup mParentView;
        View mView;
        RecentPageAsksFragment mRecentPageAsksFragment;
        RecentPageWorksFragment mRecentPageWorksFragment;
        RecentPageActFragment mRecentPageActFragment;

        ImageButton mCameraBtn;
        private CameraPopupwindow cameraPopupwindow;
        Dialog mCameraDialog;
        ViewPager viewPager;
        RadioGroup tabRadioGroup;
        RadioButton tabRadioButtonAsk;
        RadioButton tabRadioButtonWorks;
        RadioButton tabRadioButtonAct;
        // RadioGroup下方游标
        ImageView mCursor;
    }
}
