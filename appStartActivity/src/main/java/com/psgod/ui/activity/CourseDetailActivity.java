package com.psgod.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.ui.adapter.HomePageAdapter;
import com.psgod.ui.fragment.CourseDetailDetailFragment;
import com.psgod.ui.fragment.CourseDetailWorkFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/1/18 0018.
 */
public class CourseDetailActivity extends PSGodBaseActivity {
    private static final String TAG = CourseDetailActivity.class.getSimpleName();
    private Context mContext;

    private final int COUNT_OF_FRAGMENTS = 2;
    private final int[] TAB_RADIO_BUTTONS_ID = {
            R.id.fragment_course_detail_radio_btn,
            R.id.fragment_course_work_radio_btn};

    private HomePageAdapter mCoursePagerAdapter;
    private CourseDetailDetailFragment mDetailFragment;
    private CourseDetailWorkFragment mWorkFragment;
    private List<Fragment> mFragments;

    private ImageView mCursor;
    private RadioGroup mTabRadioGroup;
    private RadioButton mCourseDetailBtn;
    private RadioButton mCourseWorkBtn;
    private ViewPager mViewPager;
    private ImageView mUploadWork;

    // 游标偏移距离
    private int mCurSorOffset;
    // 游标宽度
    private int mCursorWidth;

    private long id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);
        mContext = this;

        Intent intent = getIntent();
        if(intent.hasExtra("id")){
            id = intent.getLongExtra("id",0);
        }

        initViews();
        initCursor();
        mFragments = new ArrayList<>();
        if (mDetailFragment == null) {
            mDetailFragment = new CourseDetailDetailFragment(id);
        }
        if (mWorkFragment == null) {
            mWorkFragment = new CourseDetailWorkFragment(id);
        }
        mFragments.add(mDetailFragment);
        mFragments.add(mWorkFragment);
        mCoursePagerAdapter = new HomePageAdapter(this.getSupportFragmentManager(),
                mFragments);
        mViewPager.setAdapter(mCoursePagerAdapter);
        initEvents();
    }

    private void initViews() {
        mCursor = (ImageView) findViewById(R.id.fragment_course_cursor);
        mTabRadioGroup = (RadioGroup) findViewById(R.id.fragment_course_tab_radio_group);
        mCourseDetailBtn = (RadioButton) findViewById(R.id.fragment_course_detail_radio_btn);
        mCourseWorkBtn = (RadioButton) findViewById(R.id.fragment_course_work_radio_btn);
        mViewPager = (ViewPager) findViewById(R.id.fragment_course_view_pager);
        mUploadWork = (ImageView) findViewById(R.id.ic_create_course);
    }

    // 初始化顶部RadioGroup游标
    private void initCursor() {
        mCursorWidth = Utils.dpToPx(mContext, 38);
        // 游标左侧偏移量
        mCurSorOffset = Constants.WIDTH_OF_SCREEN / 2 - mCursorWidth
                - Utils.dpToPx(mContext, 40);
        Matrix matrix = new Matrix();
        matrix.setTranslate(mCurSorOffset, 0);
        mCursor.setImageMatrix(matrix);
    }

    private void initEvents() {
        mUploadWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CourseDetailActivity.this, CourseWorkActivity.class);
                intent.putExtra(CourseWorkActivity.ID,"");
                startActivity(intent);
            }
        });

        mTabRadioGroup
                .setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        for (int ix = 0; ix < COUNT_OF_FRAGMENTS; ++ix) {
                            if (TAB_RADIO_BUTTONS_ID[ix] == checkedId) {
                                mViewPager.setCurrentItem(ix);
                            }
                        }
                    }
                });

        mViewPager
                .setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    int mCursorMoving = Utils.dpToPx(mContext, 108);

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
                        mTabRadioGroup
                                .check(TAB_RADIO_BUTTONS_ID[index]);

                        Animation animation = null;

                        switch (index) {
                            case 0:
                                if (Constants.CURRENT_COURSE_TAB == 1) {
                                    animation = new TranslateAnimation(
                                            mCursorMoving, 0, 0, 0);
                                    animation.setFillAfter(true);
                                    animation.setDuration(300);
                                    mCursor.setAnimation(animation);
                                }
                                break;

                            case 1:
                                if (Constants.CURRENT_COURSE_TAB == 0) {
                                    animation = new TranslateAnimation(0,
                                            mCursorMoving, 0, 0);
                                    animation.setFillAfter(true);
                                    animation.setDuration(300);
                                    mCursor.setAnimation(animation);
                                }
                                break;
                            default:
                                break;
                        }
                        // 设置首页当前tab
                        Constants.CURRENT_COURSE_TAB = index;
                    }
                });
    }

}
