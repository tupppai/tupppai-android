package com.psgod.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.psgod.Logger;
import com.psgod.ThreadManager;
import com.psgod.db.CommentDao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@DatabaseTable(tableName = "photo_item")
public class PhotoItem implements Serializable, Cloneable {
    // 求P
    public static final byte TYPE_ASK = 1;
    // 回复
    public static final byte TYPE_REPLY = 2;

    // public static final int FROM_HOT = 0;
    // public static final int FROM_RECENT = 1;
    // public static final int FROM_FOCUS = 2;

    public static final int TYPE_HOME_FOCUS = 0;
    public static final int TYPE_HOME_HOT = 1;
    public static final int TYPE_RECENT_ASK = 2;
    public static final int TYPE_RECENT_WORK = 3;
    public static final int TYPE_RECENT_ACT = 4;

    @DatabaseField(id = true, columnName = "pid")
    private long mPid;
    @DatabaseField(columnName = "uid")
    private long mUid;
    @DatabaseField(columnName = "nickname")
    private String mNickname;
    @DatabaseField(columnName = "update_time")
    private long mUpdateTime;
    private String mUpdateTimeStr = null; // 视图上显示的创建时间，不需要存DB
    @DatabaseField(columnName = "avatar_url")
    private String mAvatarURL;
    // 0: 女 1: 男
    private boolean mIsStar;
    @DatabaseField(columnName = "gender")
    private int mGender;
    // 照片是否已经被下载过
    @DatabaseField(columnName = "is_downloaded")
    private boolean mIsDownloaded;
    // 照片是否已经点赞
    @DatabaseField(columnName = "is_liked")
    private boolean mIsLiked;
    // 是否是我的粉丝
    // @DatabaseField(columnName="is_followed")
    private boolean mIsFollowed;
    // 我是否关注了这个人
    private boolean mIsFollow;
    // 是否被收藏
    @DatabaseField(columnName = "is_collected")
    private boolean mIsCollected;
    @DatabaseField(columnName = "image_url")
    private String mImageURL;
    @DatabaseField(columnName = "image_width")
    private int mImageWidth;
    @DatabaseField(columnName = "image_height")
    private int mImageHeight;
    // 点赞数
    @DatabaseField(columnName = "like_count")
    private int mLikeCount;
    // 评论数
    @DatabaseField(columnName = "comment_count")
    private int mCommentCount;
    // 总分享数
    @DatabaseField(columnName = "share_count")
    private int mShareCount;
    // 微信分享数
    @DatabaseField(columnName = "weixing_share_count")
    private int mWeixingShareCount;
    // 回复作品数
    @DatabaseField(columnName = "reply_count")
    private int mReplyCount;
    // 收藏数量
    @DatabaseField(columnName = "collect_count")
    private int mCollectCount;
    // 图片描述
    @DatabaseField(columnName = "desc")
    private String mDesc;

    // 作品类型 TYPE_ASK | TYPE_REPLY
    @DatabaseField(columnName = "type")
    private int mType;

    @DatabaseField(columnName = "from")
    private int mFrom;

    // 评论内容 用于我评论过的页面
    private String mCommentContent;
    private Long mCommentTime;

    public void setType(int type) {
        this.mType = type;
    }

    // TODO
    // 求p的ask_id
    @DatabaseField(columnName = "ask_id")
    private long mAskId;

    // 关注动态中 求P的id
    @DatabaseField(columnName = "reply_id")
    private long mReplyId;

    private Long mCategoryId = -1l;
    private String mCategoryName = "";
    private String mCategoryType = "";

    private String mTitle;

    private String mDescription;

//    private int mHasSharedToWechat;

//    private double mPaidAmount;

    private int mHasBought;
    private int mHasUnlocked;

    //自己点赞次数
    private int mLoveCount = 0;

    private int mClickCount;

    public int getClickCount() {
        return mClickCount;
    }

    public void setClickCount(int clickCount) {
        this.mClickCount = clickCount;
    }

    public int getHasBought() {
        return mHasBought;
    }

    public void setHasBought(int hasBought) {
        this.mHasBought = hasBought;
    }

    public int getHasUnlocked() {
        return mHasUnlocked;
    }

    public void setHasUnlocked(int hasUnlocked) {
        this.mHasUnlocked = hasUnlocked;
    }

    //    public int getHasSharedToWechat() {
//        return mHasSharedToWechat;
//    }
//
//    public void setHasSharedToWechat(int hasSharedToWechat) {
//        this.mHasSharedToWechat = hasSharedToWechat;
//    }
//
//    public double getPaidAmount() {
//        return mPaidAmount;
//    }
//
//    public void setPaidAmount(double paidAmount) {
//        this.mPaidAmount = paidAmount;
//    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public void setLoveCount(int upedNum) {
        this.mLoveCount = upedNum;
    }

    public int getLoveCount() {
        return mLoveCount;
    }

    public String getCategoryType() {
        return mCategoryType;
    }

    public String getCategoryName() {
        return mCategoryName;
    }

    // TODO 数据库问题
    private List<Comment> mHotCommentList = new ArrayList<Comment>();
    private List<Comment> mCommentList = new ArrayList<Comment>();
    // 回复作品信息（若有）
    private List<PhotoItem> mReplyPhotoItemsList = new ArrayList<PhotoItem>();

    // 原始求P图片
    private List<ImageData> mUploadImages = new ArrayList<ImageData>();

    public PhotoItem() {
        // needed by ormlite
    }

    public static PhotoItem createPhotoItem(JSONObject jsonObj, String url)
            throws JSONException {
        PhotoItem photoItem = createPhotoItem(jsonObj);
        if (url.indexOf("&category_id=") != -1) {
            String[] thumbs = url.split("&category_id=");
            if (thumbs.length >= 2) {
                String categoryId;
                if (thumbs[1].indexOf("&") != -1) {
                    String[] str = thumbs[1].split("&");
                    categoryId = str[0];
                } else {
                    categoryId = thumbs[1];
                }
                try {
                    photoItem.setCategoryId(Long.parseLong(categoryId));
                } catch (Exception e) {
                    Log.e("", "");
                }
            }
        }
        return photoItem;
    }

    public static PhotoItem createPhotoItem(JSONObject jsonObj)
            throws JSONException {
        PhotoItem item = new PhotoItem();

        if(jsonObj.has("click_count")){
            item.mClickCount = jsonObj.getInt("click_count");
        }

        if (jsonObj.has("description")) {
            item.mDescription = jsonObj.getString("description");
        }
//        if (jsonObj.has("has_shared_to_wechat")) {
//            item.mHasSharedToWechat = jsonObj.getInt("has_shared_to_wechat");
//        }
//        if (jsonObj.has("paid_amount")) {
//            item.mPaidAmount = jsonObj.getDouble("paid_amount");
//        }\
        if(jsonObj.has("has_unlocked")){
            item.mHasUnlocked = jsonObj.getInt("has_unlocked");
        }
        if(jsonObj.has("has_bought")){
            item.mHasBought = jsonObj.getInt("has_bought");
        }
        if (jsonObj.has("title")) {
            item.mTitle = jsonObj.getString("title");
        }
        if (jsonObj.has("id")) {
            item.mPid = jsonObj.getLong("id");
        }
        if (jsonObj.has("uid")) {
            item.mUid = jsonObj.getLong("uid");
        }
        if (jsonObj.has("nickname")) {
            item.mNickname = jsonObj.getString("nickname");
        }
        if (jsonObj.has("avatar")) {
            item.mAvatarURL = jsonObj.getString("avatar");
        }
        if (jsonObj.has("is_star")) {
            item.mIsStar = jsonObj.getBoolean("is_star");
        }
        if (jsonObj.has("sex")) {
            item.mGender = jsonObj.getInt("sex");
        }
        if (jsonObj.has("is_download")) {
            item.mIsDownloaded = jsonObj.getBoolean("is_download");
        }
        if (jsonObj.has("collected")) {
            item.mIsCollected = jsonObj.getBoolean("collected");
        }
        if (jsonObj.has("is_fan")) {
            item.mIsFollowed = jsonObj.getBoolean("is_fan");
        }
        if (jsonObj.has("is_follow")) {
            item.mIsFollow = jsonObj.getBoolean("is_follow");
        }

        if (jsonObj.has("uped")) {
            item.mIsLiked = jsonObj.getBoolean("uped");
        }

        if (jsonObj.has("image_url")) {
            item.mImageURL = jsonObj.getString("image_url");
        }

        if (jsonObj.has("image_width")) {
            item.mImageWidth = jsonObj.getInt("image_width");
        }

        if (jsonObj.has("image_height")) {
            item.mImageHeight = jsonObj.getInt("image_height");
        }

        if (jsonObj.has("desc")) {
            item.mDesc = jsonObj.getString("desc");
        }
        if (jsonObj.has("content")) {
            item.mCommentContent = jsonObj.getString("content");
        }
        if (jsonObj.has("comment_time")) {
            item.mCommentTime = jsonObj.getLong("comment_time");
        }

        if (jsonObj.has("love_count")) {
            item.mLoveCount = jsonObj.getInt("love_count");
        }

        try {
            item.mLikeCount = jsonObj.getInt("up_count");
        } catch (Exception e) {
            item.mLikeCount = 0;
        }

        if (jsonObj.has("comment_count")) {
            item.mCommentCount = jsonObj.getInt("comment_count");
        }

        if (jsonObj.has("share_count")) {
            item.mShareCount = jsonObj.getInt("share_count");
        }

        if (jsonObj.has("weixin_share_count")) {
            item.mWeixingShareCount = jsonObj.getInt("weixin_share_count");
        }

        if (jsonObj.has("collect_count")) {
            item.mCollectCount = jsonObj.getInt("collect_count");
        }

        if (jsonObj.has("reply_count")) {
            item.mReplyCount = jsonObj.getInt("reply_count");
        } else {
            item.mReplyCount = 0;
        }

        if (jsonObj.has("create_time")) {
            item.mUpdateTime = jsonObj.getLong("create_time");
        }
        // photoItem 是求P还是回复
        if (jsonObj.has("type")) {
            item.mType = jsonObj.getInt("type");
        }

        if (jsonObj.has("ask_id")) {
            item.mAskId = jsonObj.getLong("ask_id");
        }

        if (jsonObj.has("reply_id")) {
            item.mReplyId = jsonObj.getLong("reply_id");
        }

        if (jsonObj.has("category_id")) {
            item.mCategoryId = Long.parseLong(jsonObj.getString("category_id").equals("") ? "-1"
                    : jsonObj.getString("category_id"));
        }

        if (jsonObj.has("category_name")) {
            item.mCategoryName = jsonObj.getString("category_name");
        }

        if (jsonObj.has("category_type")) {
            item.mCategoryType = jsonObj.getString("category_type");
        }

        // 获取原图
        if (jsonObj.has("ask_uploads")) {
            JSONArray uploadArray = jsonObj.getJSONArray("ask_uploads");
            int uploadSize = uploadArray.length();

            if (uploadSize > 0) {
                for (int i = 0; i < uploadSize; i++) {
                    JSONObject uploadImage = uploadArray.getJSONObject(i);
                    item.mUploadImages.add(new ImageData(uploadImage
                            .getInt("image_width"), uploadImage
                            .getInt("image_height"), uploadImage
                            .getString("image_url")));
                }
            }
        }

        if (jsonObj.has("hot_comments")) {
            JSONArray hotComments = jsonObj.getJSONArray("hot_comments");
            int sizeOfHotComments = hotComments.length();

            if (sizeOfHotComments > 0) {
                for (int ix = 0; ix < sizeOfHotComments; ++ix) {
                    JSONObject comment = hotComments.getJSONObject(ix);
                    item.mHotCommentList.add(Comment.createComment(comment));
                }
            }
        }

        if (jsonObj.has("replies")) {
            JSONArray replyItems = jsonObj.getJSONArray("replies");
            int sizeOfReplyItems = replyItems.length();

            if (sizeOfReplyItems > 0) {
                for (int n = 0; n < sizeOfReplyItems; n++) {
                    JSONObject photoItem = replyItems.getJSONObject(n);
                    item.mReplyPhotoItemsList.add(PhotoItem
                            .createPhotoItem(photoItem));
                }
            }
        }

        return item;
    }

    // 获取实例的克隆
    @Override
    public PhotoItem clone() {
        PhotoItem photoItem = null;
        try {
            photoItem = (PhotoItem) super.clone();
        } catch (Exception e) {
        }
        return photoItem;
    }

    /**
     * 获取热门评论列表
     *
     * @return
     */
    public List<Comment> getHotCommentList() {
        return mHotCommentList;
    }

    public void setCategoryId(Long categoryId) {
        this.mCategoryId = categoryId;
    }

    public String getDesc() {
        return mDesc;
    }

    public List<ImageData> getUploadImagesList() {
        return mUploadImages;
    }

    public List<Comment> getCommentList() {
        return mCommentList;
    }

    public List<PhotoItem> getReplyItems() {
        return mReplyPhotoItemsList;
    }

    public long getPid() {
        return mPid;
    }

    public long getUid() {
        return mUid;
    }

    public int getType() {
        return mType;
    }

    public String getNickname() {
        return mNickname;
    }

    public String getAvatarURL() {
        return mAvatarURL;
    }

    public Long getCreatedTime() {
        return mUpdateTime;
    }

    public String getCommentContent() {
        return mCommentContent;
    }

    public Long getCategoryId() {
        return mCategoryId;
    }

    public static String toLocalTime(String unix) {
        // Long timestamp = Long.parseLong(unix) * 1000;
        String date = new SimpleDateFormat("MM月dd日 HH:mm")
                .format(new java.util.Date(Long.parseLong(unix + "000")));
        return date;
    }

    public String getUpdateTimeStr() {
        if (TextUtils.isEmpty(mUpdateTimeStr)) {
            StringBuffer sb = new StringBuffer();
            long time = System.currentTimeMillis() - (mUpdateTime * 1000);
            long mill = (long) Math.ceil(time / 1000);// 秒前
            long minute = (long) Math.ceil(time / 60 / 1000.0f);// 分钟前
            long hour = (long) Math.ceil(time / 60 / 60 / 1000.0f);// 小时
            long day = (long) Math.ceil(time / 24 / 60 / 60 / 1000.0f);// 天前

            if (day - 1 > 0) {
                if (day > 7) {
                    sb.append(toLocalTime(Long.toString(mUpdateTime)));
                } else {
                    sb.append(day + "天");
                }
            } else if (hour - 1 > 0) {
                if (hour >= 24) {
                    sb.append("1天");
                } else {
                    sb.append(hour + "小时");
                }
            } else if (minute - 1 > 0) {
                if (minute == 60) {
                    sb.append("1小时");
                } else {
                    sb.append(minute + "分钟");
                }
            } else if (mill - 1 > 0) {
                if (mill == 60) {
                    sb.append("1分钟");
                } else {
                    sb.append(mill + "秒");
                }
            } else {
                sb.append("刚刚");
            }
            if (!sb.toString().equals("刚刚") && (day <= 7)) {
                sb.append("前");
            }
            mUpdateTimeStr = sb.toString();
        }
        return mUpdateTimeStr;
    }

    public static String getUpdateTimeStr(Long updataTime) {
        StringBuffer sb = new StringBuffer();
        long time = System.currentTimeMillis() - (updataTime * 1000);
        long mill = (long) Math.ceil(time / 1000);// 秒前
        long minute = (long) Math.ceil(time / 60 / 1000.0f);// 分钟前
        long hour = (long) Math.ceil(time / 60 / 60 / 1000.0f);// 小时
        long day = (long) Math.ceil(time / 24 / 60 / 60 / 1000.0f);// 天前

        if (day - 1 > 0) {
            if (day > 7) {
                sb.append(toLocalTime(Long.toString(updataTime)));
            } else {
                sb.append(day + "天");
            }
        } else if (hour - 1 > 0) {
            if (hour >= 24) {
                sb.append("1天");
            } else {
                sb.append(hour + "小时");
            }
        } else if (minute - 1 > 0) {
            if (minute == 60) {
                sb.append("1小时");
            } else {
                sb.append(minute + "分钟");
            }
        } else if (mill - 1 > 0) {
            if (mill == 60) {
                sb.append("1分钟");
            } else {
                sb.append(mill + "秒");
            }
        } else {
            sb.append("刚刚");
        }
        if (!sb.toString().equals("刚刚") && (day <= 7)) {
            sb.append("前");
        }
        return sb.toString();
    }

    /**
     * 获取用户的性别 0: 女 1: 男
     *
     * @return
     */
    public int getGender() {
        return mGender;
    }

    public boolean isDownloaded() {
        return mIsDownloaded;
    }

    public void setIsDownloaded(boolean isDownloaded) {
        this.mIsDownloaded = isDownloaded;
    }

    public boolean isLiked() {
        return mIsLiked;
    }

    public void setIsLiked(boolean isLiked) {
        mIsLiked = isLiked;
    }

    public boolean isCollected() {
        return mIsCollected;
    }

    public void setIsCollected(boolean collected) {
        mIsCollected = collected;
    }

    public boolean isFollowed() {
        return mIsFollowed;
    }

    public void setIsFollowed(boolean followed) {
        mIsFollowed = followed;
    }

    public boolean isFollowing() {
        return mIsFollow;
    }

    public void setmIsFollow(boolean mIsFollow) {
        this.mIsFollow = mIsFollow;
    }

    public void setDesc(String desc) {
        mDesc = desc;
    }

    public void setUploadImages(List<ImageData> images) {
        this.mUploadImages = images;
    }

    public String getImageURL() {
        return mImageURL;
    }

    public void setImageURL(String imageURL) {
        this.mImageURL = imageURL;
    }

    public boolean getmIsStar() {
        return mIsStar;
    }

    public void setmIsStar(boolean mIsStar) {
        this.mIsStar = mIsStar;
    }

    public int getImageWidth() {
        return mImageWidth;
    }

    public void setImageWidth(int width) {
        this.mImageWidth = width;
    }

    public int getImageHeight() {
        return mImageHeight;
    }

    public void setImageHeight(int height) {
        this.mImageHeight = height;
    }

    public int getLikeCount() {
        return mLikeCount;
    }

    public void setLikeCount(int likeCount) {
        this.mLikeCount = likeCount;
    }

    public int getCommentCount() {
        return mCommentCount;
    }

    public void setCommentCount(int commentCount) {
        this.mCommentCount = commentCount;
    }

    public int getCollectCount() {
        return mCollectCount;
    }

    public void setCollectCount(int collectCount) {
        this.mCollectCount = collectCount;
    }

    public int getShareCount() {
        return mShareCount;
    }

    public void setShareCount(int shareCount) {
        this.mShareCount = shareCount;
    }

    public int getReplyCount() {
        return this.mReplyCount;
    }

    public void setFrom(int from) {
        this.mFrom = from;
    }

    public int getFrom() {
        return this.mFrom;
    }

    public long getAskId() {
        return this.mAskId;
    }

    public void setAskId(long id) {
        this.mAskId = id;
    }

    public long getReplyId() {
        return this.mReplyId;
    }

    public void setReplyId(long id) {
        this.mReplyId = id;
    }

    public void setCommentTime(long commentTime) {
        this.mCommentTime = commentTime;
    }

    public long getCommentTime() {
        return this.mCommentTime;
    }

    public void setHotCommentList(List<Comment> hotCommentList) {
        this.mHotCommentList = hotCommentList;
    }

    /**
     * TODO 临时放在这里
     *
     * @param context
     * @param photoItemDao
     * @param items
     * @param from
     */
    public static void savePhotoList(final Context context,
                                     final Dao<PhotoItem, Long> photoItemDao,
                                     final List<PhotoItem> items, final int from) {
        ThreadManager.executeOnFileThread(new Runnable() {
            @Override
            public void run() {
                // DatabaseConnection connection = null;
                try {
                    // mPhotoItemDao.delete(mDbPhotoItems);
                    // connection = photoItemDao.startThreadConnection();
                    // photoItemDao.setAutoCommit(connection, false);
                    for (PhotoItem item : items) {
                        item.setFrom(from);
                        // photoItemDao.create(item);
                        photoItemDao.createOrUpdate(item);
                    }
                    // photoItemDao.commit(connection);

                    CommentDao commentDao = new CommentDao(context);
                    for (PhotoItem item : items) {
                        List<Comment> hotComments = item.getHotCommentList();
                        for (Comment hotComment : hotComments) {
                            hotComment.setPhotoItem(item);
                            commentDao.add(hotComment);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_DEV,
                            "PSGodDatabase", e.getMessage());
                }
                // finally {
                // if (connection != null) {
                // try {
                // photoItemDao
                // .endThreadConnection(connection);
                // } catch (SQLException e) {
                // e.printStackTrace();
                // }
                // }
                // }
            }
        });
    }

    public void setPid(long mPid) {
        this.mPid = mPid;
    }

}
