package com.psgod.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.psgod.PsGodImageLoader;
import com.psgod.Constants;
import com.psgod.R;
import com.psgod.Utils;
import com.psgod.model.SearchUserData;
import com.psgod.model.SearchUserReplies;
import com.psgod.model.User;
import com.psgod.network.request.ActionCollectionRequest;
import com.psgod.network.request.ActionFollowRequest;
import com.psgod.network.request.PSGodErrorListener;
import com.psgod.network.request.PSGodRequestQueue;
import com.psgod.ui.activity.UserProfileActivity;
import com.psgod.ui.widget.AvatarImageView;
import com.psgod.ui.widget.FollowImage;
import com.psgod.ui.widget.dialog.CarouselPhotoDetailDialog;

import java.util.List;

public class SearchUserAdapter extends MyBaseAdapter<SearchUserData> {
    private Context mContext;

    public SearchUserAdapter(Context context, List<SearchUserData> searchUsers) {
        super(context, searchUsers);
        mContext = context;
    }

    private static ViewHolder viewHolder;

    @Override
    View initView(int position, View view, ViewGroup parent) {
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(
                    R.layout.item_search_user, parent, false);
            viewHolder.mAvatar = (AvatarImageView) view
                    .findViewById(R.id.item_search_user_avatar_img);
            viewHolder.mName = (TextView) view
                    .findViewById(R.id.item_search_user_name_txt);
            viewHolder.mWork = (TextView) view
                    .findViewById(R.id.item_search_user_work_txt);
            viewHolder.mFollow = (TextView) view
                    .findViewById(R.id.item_search_user_follow_txt);
            viewHolder.mFollowing = (TextView) view
                    .findViewById(R.id.item_search_user_following_txt);
            viewHolder.mFollowed = (FollowImage) view
                    .findViewById(R.id.item_search_user_follow_img);
            viewHolder.mFollowed.setIsHideFollow(false);
            viewHolder.mGrid = (GridView) view
                    .findViewById(R.id.item_search_user_grid);
            viewHolder.mClick = (RelativeLayout) view
                    .findViewById(R.id.item_search_user_click_layout);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        SearchUserData data = list.get(position);
        PsGodImageLoader.getInstance().displayImage(data.getAvatar(),
                viewHolder.mAvatar, Constants.DISPLAY_IMAGE_OPTIONS_AVATAR);
        viewHolder.mAvatar.setTag(Long.toString(data.getUid()));
        viewHolder.mAvatar.setUser(new User(data));

        // 设置关注按钮状态
        viewHolder.mFollowed.setUser(data.getUid(), data.getIs_follow(), data.getIs_fan());
        viewHolder.mWork.setText(data.getReply_count() + "作品");
        viewHolder.mFollow.setText(data.getFans_count() + "粉丝");
        viewHolder.mFollowing.setText(data.getFellow_count() + "关注");
        viewHolder.mFollowed.setTag(data);
        viewHolder.mName.setText(data.getNickname());
        viewHolder.mClick.setTag(data.getUid());
        viewHolder.mClick.setOnClickListener(avatarClick);
        SearchUserGridAdapter adapter = new SearchUserGridAdapter(context,
                data.getReplies());
        viewHolder.mGrid.setAdapter(adapter);
        return view;
    }

    private OnClickListener avatarClick = new OnClickListener() {

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra(Constants.IntentKey.USER_ID,
                    Long.parseLong(view.getTag().toString()));
            context.startActivity(intent);
        }
    };

    private class ViewHolder {
        AvatarImageView mAvatar;
        TextView mName;
        TextView mWork;
        TextView mFollow;
        TextView mFollowing;
        FollowImage mFollowed;
        RelativeLayout mClick;
        GridView mGrid;
    }

    private class SearchUserGridAdapter extends
            MyBaseAdapter<SearchUserReplies> {

        public SearchUserGridAdapter(Context context,
                                     List<SearchUserReplies> list) {
            super(context, list);
        }

        @Override
        public int getCount() {
            return list.size() > 4 ? 4 : list.size();
        }

        @Override
        View initView(int position, View view, ViewGroup parent) {
            if (view == null) {
                holder = new ViewHolder();
                view = LayoutInflater.from(context).inflate(
                        R.layout.item_search_user_grid, parent, false);
                holder.mImage = (ImageView) view
                        .findViewById(R.id.item_search_user_grid_img);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            SearchUserReplies replies = list.get(position);
            holder.mImage.setTag(replies.getImage_url());
            PsGodImageLoader.getInstance().displayImage(replies.getImage_url(),
                    holder.mImage, Constants.DISPLAY_IMAGE_OPTIONS);
            holder.mImage.setTag(replies.getAsk_id() + "tupai"
                    + replies.getId());
            holder.mImage.setOnClickListener(gridImgClick);

            return view;
        }

        ViewHolder holder;

        class ViewHolder {
            ImageView mImage;
        }

        OnClickListener gridImgClick = new OnClickListener() {

            @Override
            public void onClick(View view) {
                String[] tags = view.getTag().toString().split("tupai");
                Long aid = Long.parseLong(tags[0].toString());
                Long id = Long.parseLong(tags[1].toString());
//				CarouselPhotoDetailActivity.startActivity(context, aid, id);
//				new CarouselPhotoDetailDialog(context,aid,id).show();
                Utils.skipByObject(mContext, aid == id ?
                                Constants.IntentKey.ASK_ID : Constants.IntentKey.REPLY_ID,
                        id);
            }
        };

    }

}
