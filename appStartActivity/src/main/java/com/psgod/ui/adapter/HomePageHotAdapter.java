package com.psgod.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.psgod.PsGodImageLoader;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.model.BannerData;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.BaseRequest;
import com.psgod.ui.activity.ChannelActivity;
import com.psgod.ui.activity.RecentActActivity;
import com.psgod.ui.activity.WebBrowserActivity;
import com.psgod.ui.view.PhotoItemView;
import com.psgod.ui.view.PhotoItemView.PhotoListType;

import java.util.ArrayList;
import java.util.List;

import cn.trinea.android.view.autoscrollviewpager.AutoScrollViewPager;
import m.framework.utils.Utils;

/**
 * 首页－热门 带banner
 *
 * @author ZouMengyuan
 */
public class HomePageHotAdapter extends BaseExpandableListAdapter {
    private static final String TAG = HomePageHotAdapter.class.getSimpleName();
    private DisplayImageOptions mOptions = Constants.DISPLAY_BANNER_OPTIONS;

    private static final int TYPE_BANNER = 0;
    private static final int TYPE_PHOTO_ITEM = 1;

    private Context mContext;
    private PhotoListType mPhotoListType;
    private List<PhotoItem> mPhotoItems;
    private List<BannerData> mBannerItems;

    private RelativeLayout mEmptyView;
    private View bannerView;
    private AutoScrollViewPager mBannerViewPager;
    private BannerOnPageChangeListener bannerListener = new BannerOnPageChangeListener();
    private ImageView[] mScrollViews = new ImageView[6];

    public HomePageHotAdapter(Context context, PhotoListType photoListType,
                              List<PhotoItem> photoItems, List<BannerData> bannerItems) {
        mContext = context;
        mPhotoListType = photoListType;
        mPhotoItems = photoItems;
        mBannerItems = bannerItems;
    }

    private int getGroupViewType(int groupPosition) {
        if (groupPosition == 0) {
            return TYPE_BANNER;
        } else {
            return TYPE_PHOTO_ITEM;
        }
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        int type = getGroupViewType(groupPosition);
        switch (type) {
            case TYPE_BANNER:
                return mBannerItems.get(childPosition);
            case TYPE_PHOTO_ITEM:
                return mPhotoItems.get(childPosition);
        }
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        Object obj = getChild(groupPosition, childPosition);
        if (obj instanceof PhotoItem) {
            PhotoItem photoItem = (PhotoItem) obj;
            return photoItem.getPid();
        } else {
            return -1;
        }
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        PhotoItemView photoItemView = null;
        int groupType = getGroupViewType(groupPosition);
        if (groupType == TYPE_BANNER) {
            if (mEmptyView == null) {
                mEmptyView = new RelativeLayout(mContext);
            }
            return mEmptyView;
        } else {
            try {
                if (convertView == null
                        || !(convertView instanceof PhotoItemView)) {
                    convertView = LayoutInflater.from(mContext).inflate(
                            R.layout.widget_photo_item, null);
                    photoItemView = (PhotoItemView) convertView;
                    // photoItemView.initialize(mPhotoListType);
                } else {
                    photoItemView = (PhotoItemView) convertView;
                }

            } catch (ClassCastException e) {
                return convertView;
            } catch (Exception e) {
                return null;
            }
            PhotoItem photoItem = (PhotoItem) getChild(groupPosition,
                    childPosition);
            photoItemView.setIsHomePageHot(true);
            if ((mPhotoListType == PhotoListType.RECENT_REPLY)
                    || (mPhotoListType == PhotoListType.SINGLE_ASK)
                    || (mPhotoListType == PhotoListType.SINGLE_REPLY)) {
                photoItemView.initialize(mPhotoListType);
            } else if (photoItem.getType() == PhotoItem.TYPE_ASK) {
                photoItemView.initialize(PhotoListType.HOT_FOCUS_ASK);
            } else {
                photoItemView.initialize(PhotoListType.HOT_FOCUS_REPLY);
            }

            photoItemView.setPhotoItem(photoItem);
            photoItemView.setOnFollowChangeListener(onFollowChangeListener);// 关注接口回调
            return photoItemView;
        }
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        int type = getGroupViewType(groupPosition);
        switch (type) {
            case TYPE_BANNER:
                return mBannerItems.size();
            case TYPE_PHOTO_ITEM:
                return mPhotoItems.size();
            default:
                return -1;
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        int type = getGroupViewType(groupPosition);
        switch (type) {
            case TYPE_PHOTO_ITEM:
                return mPhotoItems;
            case TYPE_BANNER:
                return mBannerItems;
            default:
                return null;
        }
    }

    @Override
    public int getGroupCount() {
        return 2;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        int type = getGroupViewType(groupPosition);
        if (type == TYPE_PHOTO_ITEM) {
            if (mEmptyView == null) {
                mEmptyView = new RelativeLayout(mContext);
            }
            return mEmptyView;
        } else {
            if (mBannerViewPager != null) {
                mBannerViewPager.setOnPageChangeListener(null);
            }
            if (mEmptyView == null) {
                mEmptyView = new RelativeLayout(mContext);
            }
            if ((mBannerItems == null)) {
                return mEmptyView;
            } else if (mBannerItems.size() == 0) {
                return mEmptyView;
            }
            return getBannerView();
        }
    }

    public View getBannerView() {
        bannerView = LayoutInflater.from(mContext).inflate(
                R.layout.homepage_hot_banner_view, null);

        mBannerViewPager = (AutoScrollViewPager) bannerView
                .findViewById(R.id.hot_banner_viewpager);

        mScrollViews[0] = (ImageView) bannerView.findViewById(R.id.scroll_dot0);
        mScrollViews[1] = (ImageView) bannerView.findViewById(R.id.scroll_dot1);
        mScrollViews[2] = (ImageView) bannerView.findViewById(R.id.scroll_dot2);
        mScrollViews[3] = (ImageView) bannerView.findViewById(R.id.scroll_dot3);
        mScrollViews[4] = (ImageView) bannerView.findViewById(R.id.scroll_dot4);
        mScrollViews[5] = (ImageView) bannerView.findViewById(R.id.scroll_dot5);

        for (int i = 0; i < mBannerItems.size(); i++) {
            if (i == 0) {
                mScrollViews[i].setImageDrawable(mContext.getResources()
                        .getDrawable(R.drawable.shape_scroll_banner_select));
            } else {
                mScrollViews[i].setImageDrawable(mContext.getResources()
                        .getDrawable(R.drawable.shape_scroll_banner_unselect));
            }
        }

        if (mBannerViewPager != null) {
            mBannerViewPager.setOnPageChangeListener(null);
        }

        initAdapter();
        mBannerViewPager.setOnPageChangeListener(bannerListener);
        mBannerViewPager.setInterval(3000);  // 设置自动滚动的间隔时间，单位为毫秒
        mBannerViewPager.startAutoScroll();  // 启动自动滚动

        return bannerView;
    }

    private void initAdapter() {
        // viewpager滚动页面
        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                Utils.dipToPx(mContext, 166));

        PsGodImageLoader imageLoader = PsGodImageLoader.getInstance();
        final ArrayList<View> bannerListViews = new ArrayList<View>();
        for (int i = 0; i < mBannerItems.size(); i++) {

            final BannerData bannerData = mBannerItems.get(i);
            ImageView bannerImage = new ImageView(mContext);
            bannerImage.setLayoutParams(imageParams);
            bannerImage.setScaleType(ImageView.ScaleType.FIT_XY);
            imageLoader.displayImage(bannerData.getSmall_pic(), bannerImage, mOptions);
            bannerListViews.add(bannerImage);

            bannerImage.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (bannerData.getUrl().indexOf("http") != -1) {
                        Intent intent = new Intent(mContext,
                                WebBrowserActivity.class);
                        intent.putExtra(WebBrowserActivity.KEY_URL, bannerData.getUrl());
                        intent.putExtra(WebBrowserActivity.KEY_DESC, bannerData.getDesc());
                        mContext.startActivity(intent);
                    } else if (bannerData.getUrl().indexOf("tupppai://") == -1) {
                        Intent intent = new Intent(mContext,
                                WebBrowserActivity.class);
                        intent.putExtra(WebBrowserActivity.KEY_URL,
                                BaseRequest.PSGOD_BASE_URL + bannerData.getUrl());
                        intent.putExtra(WebBrowserActivity.KEY_DESC, bannerData.getDesc());
                        mContext.startActivity(intent);
                    } else {
                        String[] s = bannerData.getUrl().split("tupppai://");
                        if(s.length == 2){
                            String[] thumb = s[1].split("/");
                            if(thumb.length == 2){
                                Intent intent = new Intent();
                                if(thumb[0].equals("activity")){
                                    intent.setClass(mContext, RecentActActivity.class);
                                    intent.putExtra(RecentActActivity.INTENT_ID,thumb[1]);
                                }else{
                                    intent.setClass(mContext, ChannelActivity.class);
                                    intent.putExtra(ChannelActivity.INTENT_ID, thumb[1]);
                                    intent.putExtra(ChannelActivity.INTENT_TITLE,bannerData.getDesc());
                                }
                                mContext.startActivity(intent);
                            }
                        }
                    }
                }
            });

            mScrollViews[i].setVisibility(View.VISIBLE);
        }

        PagerAdapter mPagerAdapter = new PagerAdapter() {

            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            @Override
            public int getCount() {
                return bannerListViews.size();
            }

            @Override
            public void destroyItem(View container, int position, Object object) {
                ((AutoScrollViewPager) container).removeView(bannerListViews
                        .get(position));
            }

            @Override
            public Object instantiateItem(View container, int position) {
                ((AutoScrollViewPager) container).addView(bannerListViews
                        .get(position));
                return bannerListViews.get(position);
            }
        };

        mBannerViewPager.setAdapter(mPagerAdapter);
    }

    public class BannerOnPageChangeListener implements OnPageChangeListener {
        @Override
        public void onPageSelected(int page) {
            for (int i = 0; i < mBannerItems.size(); i++) {
                if (page == i) {
                    mScrollViews[i].setImageDrawable(mContext.getResources()
                            .getDrawable(R.drawable.shape_scroll_banner_select));
                } else {
                    mScrollViews[i].setImageDrawable(mContext.getResources()
                            .getDrawable(R.drawable.shape_scroll_banner_unselect));
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // TODO Auto-generated method stub

        }
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public void notifyDataSetChanged() {
        if (mBannerViewPager != null) {
            mBannerViewPager.setOnPageChangeListener(null);
        }
        super.notifyDataSetChanged();
    }

    PhotoItemView.OnFollowChangeListener onFollowChangeListener = new PhotoItemView.OnFollowChangeListener() {
        @Override
        public void onFocusChange(long uid, boolean focusStatus) {
            for (int i = 0; i < mPhotoItems.size(); i++) {
                if (mPhotoItems.get(i).getUid() == uid) {
                    mPhotoItems.get(i).setIsFollowed(focusStatus);
                }
            }

            HomePageHotAdapter.this.notifyDataSetChanged();
        }
    };

}
