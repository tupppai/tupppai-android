package com.psgod.model;

import java.util.List;

/**
 * Created by remilia on 2015/11/18.
 */
public class ActivitiesAct {

    private String id;
    private String ask_id;
    private int type;
    private boolean is_follow;
    private boolean is_download;
    private boolean uped;
    private boolean collected;
    private String avatar;
    private int sex;
    private String uid;
    private String nickname;
    private String upload_id;
    private String create_time;
    private String update_time;
    private String desc;
    private String up_count;
    private int comment_count;
    private int collect_count;
    private String click_count;
    private int inform_count;
    private String share_count;
    private String weixin_share_count;
    private int reply_count;
    private String image_url;
    private int image_width;
    private int image_height;
    private String image_ratio;

    private List<AskUploadsEntity> ask_uploads;

    public void setId(String id) {
        this.id = id;
    }

    public void setAsk_id(String ask_id) {
        this.ask_id = ask_id;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setIs_follow(boolean is_follow) {
        this.is_follow = is_follow;
    }

    public void setIs_download(boolean is_download) {
        this.is_download = is_download;
    }

    public void setUped(boolean uped) {
        this.uped = uped;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setUpload_id(String upload_id) {
        this.upload_id = upload_id;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setUp_count(String up_count) {
        this.up_count = up_count;
    }

    public void setComment_count(int comment_count) {
        this.comment_count = comment_count;
    }

    public void setCollect_count(int collect_count) {
        this.collect_count = collect_count;
    }

    public void setClick_count(String click_count) {
        this.click_count = click_count;
    }

    public void setInform_count(int inform_count) {
        this.inform_count = inform_count;
    }

    public void setShare_count(String share_count) {
        this.share_count = share_count;
    }

    public void setWeixin_share_count(String weixin_share_count) {
        this.weixin_share_count = weixin_share_count;
    }

    public void setReply_count(int reply_count) {
        this.reply_count = reply_count;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public void setImage_width(int image_width) {
        this.image_width = image_width;
    }

    public void setImage_height(int image_height) {
        this.image_height = image_height;
    }

    public void setImage_ratio(String image_ratio) {
        this.image_ratio = image_ratio;
    }

    public void setAsk_uploads(List<AskUploadsEntity> ask_uploads) {
        this.ask_uploads = ask_uploads;
    }

    public String getId() {
        return id;
    }

    public String getAsk_id() {
        return ask_id;
    }

    public int getType() {
        return type;
    }

    public boolean isIs_follow() {
        return is_follow;
    }

    public boolean isIs_download() {
        return is_download;
    }

    public boolean isUped() {
        return uped;
    }

    public boolean isCollected() {
        return collected;
    }

    public String getAvatar() {
        return avatar;
    }

    public int getSex() {
        return sex;
    }

    public String getUid() {
        return uid;
    }

    public String getNickname() {
        return nickname;
    }

    public String getUpload_id() {
        return upload_id;
    }

    public String getCreate_time() {
        return create_time;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public String getDesc() {
        return desc;
    }

    public String getUp_count() {
        return up_count;
    }

    public int getComment_count() {
        return comment_count;
    }

    public int getCollect_count() {
        return collect_count;
    }

    public String getClick_count() {
        return click_count;
    }

    public int getInform_count() {
        return inform_count;
    }

    public String getShare_count() {
        return share_count;
    }

    public String getWeixin_share_count() {
        return weixin_share_count;
    }

    public int getReply_count() {
        return reply_count;
    }

    public String getImage_url() {
        return image_url;
    }

    public int getImage_width() {
        return image_width;
    }

    public int getImage_height() {
        return image_height;
    }

    public String getImage_ratio() {
        return image_ratio;
    }

    public List<AskUploadsEntity> getAsk_uploads() {
        return ask_uploads;
    }

    public static class AskUploadsEntity {
        private String image_url;
        private int image_width;
        private int image_height;
        private String image_ratio;

        public void setImage_url(String image_url) {
            this.image_url = image_url;
        }

        public void setImage_width(int image_width) {
            this.image_width = image_width;
        }

        public void setImage_height(int image_height) {
            this.image_height = image_height;
        }

        public void setImage_ratio(String image_ratio) {
            this.image_ratio = image_ratio;
        }

        public String getImage_url() {
            return image_url;
        }

        public int getImage_width() {
            return image_width;
        }

        public int getImage_height() {
            return image_height;
        }

        public String getImage_ratio() {
            return image_ratio;
        }
    }
}
