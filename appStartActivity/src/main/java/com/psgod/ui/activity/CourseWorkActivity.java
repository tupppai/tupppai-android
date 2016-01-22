package com.psgod.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.psgod.CustomToast;
import com.psgod.R;
import com.psgod.UpLoadUtils;
import com.psgod.Utils;
import com.psgod.model.SelectFolder;
import com.psgod.model.SelectImage;
import com.psgod.ui.adapter.MultiImageSelectAdapter;
import com.psgod.ui.widget.StopGridView;
import com.psgod.ui.widget.dialog.FolderPopupWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/1/18 0018.
 */
public class CourseWorkActivity extends PSGodBaseActivity {

    private TextView mSureTxt;
    private EditText mEdit;
    private StopGridView mGrid;
    private MultiImageSelectAdapter mImageAdapter;
    private List<SelectImage> mImages = new ArrayList<>();
    private List<SelectImage> mResultImages = new ArrayList<>();

    private ImageView mScrollHandler;
    private LinearLayout mScrollArea;
    private RelativeLayout mParent;

    private TextView mAlbumTxt;
    private TextView mAlbumSureTxt;
    private FolderPopupWindow mFolderPopupWindow;

    private int originMarginY = 345;
    private long id;
    public static final String ID = "id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_work);

        Intent intent = getIntent();
        if (intent.hasExtra(ID)) {
            id = intent.getLongExtra(ID, 0);
        }

        initView();
        initListener();
    }

    private void initView() {
        originMarginY = Utils.getUnrealScreenHeightPx(this) -
                Utils.getScreenWidthPx(this) / 3 * 2 - Utils.dpToPx(this, 45 + 30);
        mScrollHandler = (ImageView)
                findViewById(R.id.activity_course_work_scollhandle);
        mScrollArea = (LinearLayout) findViewById(R.id.activity_course_work_scollarea);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) mScrollArea.getLayoutParams();
        params.setMargins(0, originMarginY, 0, 0);
        mScrollArea.setLayoutParams(params);
        mParent = (RelativeLayout) findViewById(R.id.activity_course_work_parent);
        mFolderPopupWindow = new FolderPopupWindow(this);
        mFolderPopupWindow.setWidth(-1);
        mFolderPopupWindow.setHeight(-1);
        mAlbumTxt = (TextView) findViewById(R.id.activity_course_work_album);
        mAlbumSureTxt = (TextView) findViewById(R.id.activity_course_work_album_srue);
        mSureTxt = (TextView) findViewById(R.id.activity_course_work_sure);
        mEdit = (EditText) findViewById(R.id.activity_course_work_edit);
        mGrid = (StopGridView) findViewById(R.id.activity_course_work_grid);
        mImageAdapter = new MultiImageSelectAdapter(this);
        mImageAdapter.setHasCamera(false);
        mImageAdapter.setData(mImages);
        mGrid.setAdapter(mImageAdapter);
    }

    private void initListener() {
        mScrollHandler.setOnTouchListener(new View.OnTouchListener() {
            float downY;
            float moveY = 0;
            int oH = originMarginY;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downY = motionEvent.getRawY();
                        moveY = 0;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        moveY = motionEvent.getRawY() - downY;
                        RelativeLayout.LayoutParams params =
                                (RelativeLayout.LayoutParams) mScrollArea.getLayoutParams();
                        params.setMargins(0,
                                (int) (oH + moveY), 0, 0);
                        mScrollArea.setLayoutParams(params);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!isScrollTop) {
                            scrollToTop((int) (oH + moveY));
                            oH = 0;
                        } else {
                            scrollToBottom((int) (oH + moveY));
                            oH = originMarginY;
                        }
                        break;
                }
                return true;
            }
        });

        mParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.hideInputPanel(CourseWorkActivity.this, view);
            }
        });

        mFolderPopupWindow.setOnFolderChangeListener(new FolderPopupWindow.OnFolderChangeListener() {
            @Override
            public void onClick(SelectFolder folder) {
                mAlbumTxt.setText(folder.name);
            }

            @Override
            public void onDataChanger(List<SelectImage> data) {
                mImages.clear();
                mImages.addAll(mResultImages);
                mImages.addAll(data);
                notifyDataTopShow();
                mImageAdapter.notifyDataSetChanged();
                mGrid.smoothScrollToPosition(0);
            }
        });

        mAlbumTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFolderPopupWindow.
                        showAtLocation(mParent, Gravity.CENTER, 0, 0);
            }
        });

        mAlbumSureTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollToBottom(0);
            }
        });

        mGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView,
                                    View view, int i, long l) {
                SelectImage image = (SelectImage) adapterView
                        .getAdapter().getItem(i);
                selectImageFromGrid(image);
            }
        });

        mGrid.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @SuppressWarnings("deprecation")
                    @Override
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onGlobalLayout() {

                        final int width = mGrid.getWidth();
                        final int height = mGrid.getHeight();

                        final int desireSize = getResources()
                                .getDimensionPixelOffset(
                                        R.dimen.multi_image_slect_size);
                        final int numCount = width / desireSize;
                        mGrid.setNumColumns(numCount);
                        final int columnSpace = getResources()
                                .getDimensionPixelOffset(
                                        R.dimen.multi_image_select_space_size);
                        int columnWidth = (width - columnSpace * (numCount - 1))
                                / numCount;
                        mImageAdapter.setItemSize(columnWidth);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            mGrid.getViewTreeObserver()
                                    .removeOnGlobalLayoutListener(this);
                        } else {
                            mGrid.getViewTreeObserver()
                                    .removeGlobalOnLayoutListener(this);
                        }
                    }
                });

        mSureTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                new WorkShareDialog(CourseWorkActivity.this).show();
                if (mEdit.getText().toString().length() < 3) {
                    CustomToast.show(CourseWorkActivity.this, "描述请大于三个字", Toast.LENGTH_SHORT);
                } else if (mResultImages.size() == 0) {
                    CustomToast.show(CourseWorkActivity.this, "请选择要上传的图片", Toast.LENGTH_SHORT);
                } else {
                    List<String> thumb = new ArrayList<String>();
                    thumb.add(mResultImages.get(0).path);
                    UpLoadUtils.getInstance(CourseWorkActivity.this).
                            upLoad(mEdit.getText().toString(), thumb, id,
                                    UpLoadUtils.TYPE_REPLY_UPLOAD);
                }

            }
        });
    }

    private void notifyDataTopShow() {
        int resultLength = mResultImages.size();
        int dataLenght = mImages.size();
        mImages.addAll(0, mResultImages);
        for (int i = 0; i < resultLength; i++) {
            for (int j = resultLength; j < dataLenght; j++) {
                if (mImages.get(j).path.equals(mResultImages.get(i).path)) {
                    mImages.remove(j);
                    j--;
                    dataLenght--;
                }
            }
        }
        mImageAdapter.notifyDataSetChanged();
    }

    /**
     * 选择图片操作
     *
     * @param image
     */
    private void selectImageFromGrid(SelectImage image) {
        if (image != null) {
            // 多选模式
            if (mResultImages.contains(image)) {
                mResultImages.remove(image);
                // 移除
            } else {
                // 判断选择数量问题
                if (1 == mResultImages.size()) {
                    CustomToast.show(CourseWorkActivity.this,
                            "最多选择" + 1 + "张作业",
                            Toast.LENGTH_SHORT);
                    return;
                }

                mResultImages.add(image);
            }
            mImageAdapter.select(image);
        }
    }

    private boolean isAnimEnd = true;
    private boolean isScrollTop = false;

    private void scrollToTop(int fromY) {
        if (isAnimEnd) {
            isAnimEnd = false;
            final AnimatorSet anim = new AnimatorSet();
            anim.setDuration(250);
            ValueAnimator yAnim = ValueAnimator.ofInt(fromY, 0);
            yAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Integer value = (Integer) valueAnimator.getAnimatedValue();
                    RelativeLayout.LayoutParams params =
                            (RelativeLayout.LayoutParams) mScrollArea.getLayoutParams();
                    params.setMargins(0, value, 0, 0);
                    mScrollArea.setLayoutParams(params);
                }
            });
            yAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    isAnimEnd = true;
                    isScrollTop = true;
                    mGrid.setCanScroll(true);
                    mAlbumSureTxt.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            anim.playTogether(yAnim);
            anim.start();
        }
    }

    private void scrollToBottom(int fromY) {
        if (isAnimEnd) {
            isAnimEnd = false;
            final AnimatorSet anim = new AnimatorSet();
            anim.setDuration(250);
            ValueAnimator yAnim = ValueAnimator.ofInt(fromY, originMarginY);
            yAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Integer value = (Integer) valueAnimator.getAnimatedValue();
                    RelativeLayout.LayoutParams params =
                            (RelativeLayout.LayoutParams) mScrollArea.getLayoutParams();
                    params.setMargins(0, value, 0, 0);
                    mScrollArea.setLayoutParams(params);
                }
            });
            yAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    mGrid.setCanScroll(false);
                    mAlbumSureTxt.setVisibility(View.GONE);
                    notifyDataTopShow();
                    mGrid.smoothScrollToPosition(0);
//                    RelativeLayout.LayoutParams params =
//                            (RelativeLayout.LayoutParams) mGrid.getLayoutParams();
//                    params.
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    isAnimEnd = true;
                    isScrollTop = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            anim.playTogether(yAnim);
            anim.start();
        }
    }
}
