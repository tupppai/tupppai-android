package com.psgod.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.psgod.Constants;
import com.psgod.PSGodApplication;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 当前登录用户
 *
 * @author Rayal
 */
public class LoginUser {
    public static interface SPKey {
        String UID = "Uid";
        String NICKNAME = "Nickname";
        String AVATAR_URL = "AvatarUrl";
        String GENDER = "Gender";
        String PROVINCE_ID = "ProvinceId";
        String CITY_ID = "CityId";
        String ASK_COUNT = "AskCount";
        String REPLY_COUNT = "ReplyCount";
        String FOLLOWER_COUNT = "FollowerCount";
        String FOLLOWING_COUNT = "FollowingCount";
        String LIKED_COUNT = "LikedCount";
        String IN_PROGRESS_COUNT = "InProgressCount";
        String COLLECTION_COUNT = "CollectionCount";
        String IS_BOUND_WECHAT = "IsBoundWechat";
        String IS_BOUND_QQ = "IsBoundQQ";
        String IS_BOUND_WEIBO = "IsBoundWeibo";
        String IS_BOUND_MOBILE = "IsBoundMobile";
        String PHONE_NUM = "PhoneNum";
        String IS_STAR = "IsStar";
        String BALANCE = "balance";
    }

    private static LoginUser mInstance = null;

    protected long mUid;
    protected String mAvatarImageUrl;
    protected String mNickname;
    protected int mGender;
    protected int mProvinceId;
    protected int mCityId;
    protected int mAskCount;
    protected int mReplyCount;
    protected int mFollowerCount;
    protected int mFollowingCount;
    protected int mLikedCount;
    protected int mInprogressCount;
    protected int mCollectionCount;
    protected boolean mIsBoundWechat;
    protected boolean mIsBoundQQ;
    protected boolean mIsBoundWeibo;
    protected boolean mIsBoundMobile;
    protected boolean mIsInitialized;
    protected String mPhoneNum;
    protected boolean mIsStar;
    protected double mBalance;

    public static synchronized LoginUser getInstance() {
        if (mInstance == null) {
            mInstance = getLocalUserData();
        }
        return mInstance;
    }

    //更改资料后，刷新数据
    public void refreshData() {
        if (mInstance != null) {
            SharedPreferences sp = PSGodApplication.getAppContext()
                    .getSharedPreferences(Constants.SharedPreferencesKey.NAME,
                            Context.MODE_PRIVATE);

            mInstance.mUid = sp.getLong(SPKey.UID, -1);
            mInstance.mNickname = sp.getString(SPKey.NICKNAME, "");
            mInstance.mPhoneNum = sp.getString(SPKey.PHONE_NUM, "");
            mInstance.mAvatarImageUrl = sp.getString(SPKey.AVATAR_URL, "");
            mInstance.mGender = sp.getInt(SPKey.GENDER, 1);
            mInstance.mProvinceId = sp.getInt(SPKey.PROVINCE_ID, 11);
            mInstance.mCityId = sp.getInt(SPKey.CITY_ID, 1);
            mInstance.mAskCount = sp.getInt(SPKey.ASK_COUNT, 0);
            mInstance.mReplyCount = sp.getInt(SPKey.REPLY_COUNT, 0);
            mInstance.mFollowerCount = sp.getInt(SPKey.FOLLOWER_COUNT, 0);
            mInstance.mFollowingCount = sp.getInt(SPKey.FOLLOWING_COUNT, 0);
            mInstance.mLikedCount = sp.getInt(SPKey.LIKED_COUNT, 0);
            mInstance.mInprogressCount = sp.getInt(SPKey.IN_PROGRESS_COUNT, 0);
            mInstance.mCollectionCount = sp.getInt(SPKey.COLLECTION_COUNT, 0);
            mInstance.mIsBoundWechat = sp.getBoolean(SPKey.IS_BOUND_WECHAT, false);
            mInstance.mIsBoundQQ = sp.getBoolean(SPKey.IS_BOUND_QQ, false);
            mInstance.mIsBoundWeibo = sp.getBoolean(SPKey.IS_BOUND_WEIBO, false);
            mInstance.mIsStar = sp.getBoolean(SPKey.IS_STAR, false);
            mInstance.mBalance = Double.longBitsToDouble(sp.getLong(SPKey.BALANCE, 0));
        }
    }

    // 从SP中读取数据 并返回LoginUser实例
    private static LoginUser getLocalUserData() {
        LoginUser user = new LoginUser();
        SharedPreferences sp = PSGodApplication.getAppContext()
                .getSharedPreferences(Constants.SharedPreferencesKey.NAME,
                        Context.MODE_PRIVATE);

        user.mUid = sp.getLong(SPKey.UID, -1);
        user.mNickname = sp.getString(SPKey.NICKNAME, "");
        user.mPhoneNum = sp.getString(SPKey.PHONE_NUM, "");
        user.mAvatarImageUrl = sp.getString(SPKey.AVATAR_URL, "");
        user.mGender = sp.getInt(SPKey.GENDER, 1);
        user.mProvinceId = sp.getInt(SPKey.PROVINCE_ID, 11);
        user.mCityId = sp.getInt(SPKey.CITY_ID, 1);
        user.mAskCount = sp.getInt(SPKey.ASK_COUNT, 0);
        user.mReplyCount = sp.getInt(SPKey.REPLY_COUNT, 0);
        user.mFollowerCount = sp.getInt(SPKey.FOLLOWER_COUNT, 0);
        user.mFollowingCount = sp.getInt(SPKey.FOLLOWING_COUNT, 0);
        user.mLikedCount = sp.getInt(SPKey.LIKED_COUNT, 0);
        user.mInprogressCount = sp.getInt(SPKey.IN_PROGRESS_COUNT, 0);
        user.mCollectionCount = sp.getInt(SPKey.COLLECTION_COUNT, 0);
        user.mIsBoundWechat = sp.getBoolean(SPKey.IS_BOUND_WECHAT, false);
        user.mIsBoundQQ = sp.getBoolean(SPKey.IS_BOUND_QQ, false);
        user.mIsBoundWeibo = sp.getBoolean(SPKey.IS_BOUND_WEIBO, false);
        user.mIsStar = sp.getBoolean(SPKey.IS_STAR, false);
        user.mBalance = Double.longBitsToDouble(sp.getLong(SPKey.BALANCE, 0));
        // user.mIsBoundMobile = sp.getBoolean(SPKey.IS_BOUND_MOBILE, false);
        return user;
    }

    public double getBalance() {
        return mBalance;
    }

    public void setBalance(double balance) {
        this.mBalance = balance;
    }

    public String getPhoneNum() {
        return mPhoneNum;
    }

    public void setPhoneNum(String num) {
        this.mPhoneNum = num;
    }

    public int getInprogressCount() {
        return mInprogressCount;
    }

    public void setInprogressCount(int inprogressCount) {
        this.mInprogressCount = inprogressCount;
    }

    public int getCollectionCount() {
        return mCollectionCount;
    }

    public void setCollectionCount(int collectionCount) {
        this.mCollectionCount = collectionCount;
    }

    public boolean isBoundWechat() {
        return mIsBoundWechat;
    }

    public void setBoundWechat(boolean isBoundWechat) {
        this.mIsBoundWechat = isBoundWechat;
    }

    public boolean isBoundQQ() {
        return mIsBoundQQ;
    }

    public void setBoundQQ(boolean isBoundQQ) {
        this.mIsBoundQQ = isBoundQQ;
    }

    public boolean isBoundWeibo() {
        return mIsBoundWeibo;
    }

    public void setBoundWeibo(boolean isBoundWeibo) {
        this.mIsBoundWeibo = isBoundWeibo;
    }

    public boolean isBoundMobile() {
        return mIsBoundMobile;
    }

    public void setBoundMobile(boolean isBoundMobile) {
        this.mIsBoundMobile = isBoundMobile;
    }

    public void initFromJSONObject(JSONObject obj) {
        try {
            if (obj.has("balance")) {
                mBalance = obj.getDouble("balance");
            }
            mUid = obj.getLong("uid");
            mNickname = obj.getString("nickname");
            mGender = obj.getInt("sex");
            mPhoneNum = obj.getString("phone");
            mAvatarImageUrl = obj.getString("avatar");
            mFollowerCount = obj.getInt("fans_count");
            mFollowingCount = obj.getInt("fellow_count");
            mLikedCount = obj.getInt("uped_count");
            mAskCount = obj.getInt("ask_count");
            mReplyCount = obj.getInt("reply_count");
            mInprogressCount = obj.getInt("inprogress_count");
            mCollectionCount = obj.getInt("collection_count");
            if (obj.has("is_star")) {
                mIsStar = obj.getBoolean("is_star");
            }

            if ((obj.get("city")) instanceof String) {
                String s = obj.getString("city");
                mCityId = Integer.parseInt(s == null || s.equals("") ? "0" : s);
            } else {
                mCityId = obj.getInt("city");
            }
            if ((obj.get("province")) instanceof String) {
                String s = obj.getString("province");
                mProvinceId = Integer.parseInt(s == null || s.equals("") ? "0" : s);
            } else {
                mProvinceId = obj.getInt("province");
            }
            // mProvinceId = Integer.parseInt(obj.getString("province"));
            mIsBoundWechat = (obj.getInt("is_bound_weixin") != 0);
            mIsBoundQQ = (obj.getInt("is_bound_qq") != 0);
            mIsBoundWeibo = (obj.getInt("is_bound_weibo") != 0);
            // mIsBoundMobile = (obj.getInt("is_bound_mobile") != 0);
            mIsInitialized = true;

            saveData();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void saveData() {
        SharedPreferences.Editor editor = PSGodApplication
                .getAppContext()
                .getSharedPreferences(Constants.SharedPreferencesKey.NAME,
                        Context.MODE_PRIVATE).edit();

        editor.putLong(SPKey.UID, this.mUid);
        editor.putString(SPKey.NICKNAME, this.mNickname);
        editor.putString(SPKey.AVATAR_URL, this.mAvatarImageUrl);
        editor.putInt(SPKey.GENDER, this.mGender);
        editor.putInt(SPKey.PROVINCE_ID, this.mProvinceId);
        editor.putInt(SPKey.CITY_ID, this.mCityId);
        editor.putInt(SPKey.ASK_COUNT, this.mAskCount);
        editor.putInt(SPKey.REPLY_COUNT, this.mReplyCount);
        editor.putInt(SPKey.FOLLOWER_COUNT, this.mFollowerCount);
        editor.putInt(SPKey.FOLLOWING_COUNT, this.mFollowingCount);
        editor.putInt(SPKey.LIKED_COUNT, this.mLikedCount);
        editor.putInt(SPKey.IN_PROGRESS_COUNT, this.mInprogressCount);
        editor.putInt(SPKey.COLLECTION_COUNT, this.mCollectionCount);
        editor.putBoolean(SPKey.IS_BOUND_WECHAT, this.mIsBoundWechat);
        editor.putBoolean(SPKey.IS_BOUND_QQ, this.mIsBoundQQ);
        editor.putBoolean(SPKey.IS_BOUND_WEIBO, this.mIsBoundWeibo);
        editor.putBoolean(SPKey.IS_STAR, this.mIsStar);
        // editor.putBoolean(SPKey.IS_BOUND_MOBILE, this.mIsBoundMobile);
        editor.putString(SPKey.PHONE_NUM, this.mPhoneNum);
        editor.putLong(SPKey.BALANCE, Double.doubleToRawLongBits(this.mBalance));

        if (android.os.Build.VERSION.SDK_INT >= 9) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    /**
     * 判断本地sp中是否有用户数据 若存有 则返回true
     */
    public boolean canReadData() {
        SharedPreferences sp = PSGodApplication.getAppContext()
                .getSharedPreferences(Constants.SharedPreferencesKey.NAME,
                        Context.MODE_PRIVATE);

        this.mUid = sp.getLong(SPKey.UID, -1);
        this.mIsInitialized = (mUid != -1);

        return mIsInitialized;
    }

    public boolean isStar() {
        return mIsStar;
    }

    public long getUid() {
        return mUid;
    }

    public void setUid(long uid) {
        this.mUid = uid;
    }

    public String getAvatarImageUrl() {
        return mAvatarImageUrl;
    }

    public void setAvatarImageUrl(String avatarImageUrl) {
        this.mAvatarImageUrl = avatarImageUrl;
    }

    public String getNickname() {
        return mNickname;
    }

    public void setNickname(String nickname) {
        this.mNickname = nickname;
    }

    public int getGender() {
        return mGender;
    }

    public void setGender(int gender) {
        this.mGender = gender;
    }

    public int getCityId() {
        return mCityId;
    }

    public int getProvinceId() {
        return mProvinceId;
    }

    public void setCityId(int cityId) {
        this.mCityId = cityId;
    }

    public void setProvinceId(int provinceId) {
        this.mProvinceId = provinceId;
    }

    public int getAskCount() {
        return mAskCount;
    }

    public void setAskCount(int askCount) {
        this.mAskCount = askCount;
    }

    public int getReplyCount() {
        return mReplyCount;
    }

    public void setReplyCount(int replyCount) {
        this.mReplyCount = replyCount;
    }

    public int getFollowerCount() {
        return mFollowerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.mFollowerCount = followerCount;
    }

    public int getFollowingCount() {
        return mFollowingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.mFollowingCount = followingCount;
    }

    public int getLikedCount() {
        return mLikedCount;
    }

    public void setLikedCount(int likedCount) {
        this.mLikedCount = likedCount;
    }

    public boolean isInitialized() {
        return this.mIsInitialized;
    }
}
