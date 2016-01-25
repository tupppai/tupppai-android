package com.psgod.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.eventbus.RefreshEvent;
import com.psgod.model.Channel;
import com.psgod.model.PhotoItem;
import com.psgod.network.request.ActivitiesActRequest;
import com.psgod.network.request.ChannelRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.network.request.PhotoListRequest;
import com.psgod.ui.adapter.PhotoListAdapter;
import com.psgod.ui.view.PhotoItemView;
import com.psgod.ui.view.PullToRefreshSwipeMenuListView;
import com.psgod.ui.widget.dialog.CustomProgressingDialog;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2016/1/18 0018.
 */
public class CourseDetailWorkFragment extends BaseFragment {
    private static final String TAG = CourseDetailWorkFragment.class.getSimpleName();
    private Context mContext;
    private ViewHolder mViewHolder;
    private View mFooterView;

    private List<PhotoItem> mPhotoItems = new ArrayList<>();
    private PhotoListAdapter mAdapter;

    private long id;

    private CustomProgressingDialog progressingDialog;
    private String mSpKey;
    private int mPage = 1;
    private boolean canLoadMore = false;

    private View mEmptyView;

    public void onEventMainThread(RefreshEvent event) {
        if(event.className.equals(this.getClass().getName())){
            mViewHolder.mListView.setRefreshing();
        }
    }


    // 上次刷新时间
    private long mLastUpdatedTime;

    public CourseDetailWorkFragment(long id) {
        this.id = id;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mContext = getActivity();
        mViewHolder = new ViewHolder();
        mViewHolder.mView = inflater.inflate(R.layout.fragment_course_detail_work, null);
        mViewHolder.mListView = (PullToRefreshListView) mViewHolder.mView.findViewById(R.id.course_detail_work_list_listview);
        mViewHolder.mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        listListener = new PhotoListListener(getActivity());
        mViewHolder.mListView.setOnRefreshListener(listListener);
        mViewHolder.mListView.setOnLastItemVisibleListener(listListener);
        mAdapter = new PhotoListAdapter(getActivity(), PhotoItemView.PhotoListType.RECENT_REPLY, mPhotoItems);
        mViewHolder.mListView.setAdapter(mAdapter);
        mFooterView = LayoutInflater.from(mContext).inflate(R.layout.footer_load_more, null);
        mViewHolder.mListView.getRefreshableView().addFooterView(mFooterView);
        mFooterView.setVisibility(View.INVISIBLE);
        mSpKey = Constants.SharedPreferencesKey.CHANNEL_LIST_LAST_REFRESH_TIME;
        mEmptyView = inflater.inflate(R.layout.view_empty, null);

        progressingDialog = new CustomProgressingDialog(getActivity());
        mViewHolder.mListView.setRefreshing();
        return mViewHolder.mView;
    }

    private static class ViewHolder {
        private View mView;
        private PullToRefreshListView mListView;
    }

    private PhotoListListener listListener;

    private class PhotoListListener implements PullToRefreshBase.OnLastItemVisibleListener,
            PullToRefreshBase.OnRefreshListener<ListView> {
        private Context mContext;
        private static final long DEFAULT_LAST_REFRESH_TIME = -1;

        public PhotoListListener(Context context) {
            mContext = context;
            SharedPreferences sp = mContext.getSharedPreferences(
                    Constants.SharedPreferencesKey.NAME, Context.MODE_PRIVATE);
            mSpKey = Constants.SharedPreferencesKey.RECENT_PHOTO_LIST_LAST_REFRESH_TIME;


            mLastUpdatedTime = sp.getLong(mSpKey, DEFAULT_LAST_REFRESH_TIME);
        }

        @Override
        public void onLastItemVisible() {
            if (canLoadMore) {
                mPage += 1;
                mFooterView.setVisibility(View.VISIBLE);

                PhotoListRequest.Builder builder = new PhotoListRequest.Builder()
                        .setPage(mPage).setLastUpdated(mLastUpdatedTime)
                        .setType(PhotoItem.TYPE_RECENT_WORK).setAskId(id)
                        .setListener(loadMoreListener)
                        .setErrorListener(errorListener);

                PhotoListRequest request = builder.build();
                request.setTag(TAG);
                RequestQueue requestQueue = PSGodRequestQueue.getInstance(
                        getActivity()).getRequestQueue();
                requestQueue.add(request);
            }
        }

        @Override
        public void onRefresh(PullToRefreshBase<ListView> refreshView) {
            mPage = 1;
            // 上次刷新时间
            if (mLastUpdatedTime == DEFAULT_LAST_REFRESH_TIME) {
                mLastUpdatedTime = System.currentTimeMillis();
            }

            PhotoListRequest.Builder builder = new PhotoListRequest.Builder()
                    .setPage(mPage).setLastUpdated(mLastUpdatedTime)
                    .setType(PhotoItem.TYPE_RECENT_WORK).setAskId(id)
                    .setListener(refreshListener)
                    .setErrorListener(errorListener);

            PhotoListRequest request = builder.build();
            request.setTag(TAG);
            RequestQueue requestQueue = PSGodRequestQueue.getInstance(getActivity())
                    .getRequestQueue();
            requestQueue.add(request);
        }
    }


    private Response.ErrorListener errorListener = new PSGodErrorListener() {
        @Override
        public void handleError(VolleyError error) {
            // TODO Auto-generated method stub
            mViewHolder.mListView.onRefreshComplete();
            mFooterView.setVisibility(View.INVISIBLE);

            if (progressingDialog.isShowing()) {
                progressingDialog.dismiss();
            }
        }
    };

    private Response.Listener<List<PhotoItem>> loadMoreListener = new Response.Listener<List<PhotoItem>>() {
        @Override
        public void onResponse(final List<PhotoItem> items) {
            mPhotoItems.addAll(items);
            mAdapter.notifyDataSetChanged();
            mFooterView.setVisibility(View.INVISIBLE);

            if (items.size() < 15) {
                canLoadMore = false;
            } else {
                canLoadMore = true;
            }

            if (progressingDialog.isShowing()) {
                progressingDialog.dismiss();
            }
        }
    };

    private Response.Listener<List<PhotoItem>> refreshListener = new Response.Listener<List<PhotoItem>>() {
        @Override
        public void onResponse(List<PhotoItem> items) {
            mPhotoItems.clear();
            mPhotoItems.addAll(items);
            mAdapter.notifyDataSetChanged();

            mViewHolder.mListView.onRefreshComplete();


            if (items.size() < 15) {
                canLoadMore = false;
            } else {
                canLoadMore = true;
            }
            // 保存本次刷新时间到sp
            mLastUpdatedTime = System.currentTimeMillis();
            if (android.os.Build.VERSION.SDK_INT >= 9) {
                getActivity()
                        .getSharedPreferences(
                                Constants.SharedPreferencesKey.NAME,
                                Context.MODE_PRIVATE).edit()
                        .putLong(mSpKey, mLastUpdatedTime).apply();
            } else {
                getActivity().getSharedPreferences(
                        Constants.SharedPreferencesKey.NAME,
                        Context.MODE_PRIVATE).edit()
                        .putLong(mSpKey, mLastUpdatedTime).commit();
            }

            if (progressingDialog.isShowing()) {
                progressingDialog.dismiss();
            }

            if(mPhotoItems == null || mPhotoItems.size() == 0){
                mViewHolder.mListView.setEmptyView(mEmptyView);
                mEmptyView.setVisibility(View.VISIBLE);
            }else{
                mEmptyView.setVisibility(View.GONE);
            }
        }
    };

}
