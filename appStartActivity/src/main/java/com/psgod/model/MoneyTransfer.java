package com.psgod.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/1/27 0027.
 */
public class MoneyTransfer implements Serializable {

    private String id;
    private String object;
    private String type;
    private int created;
    private Object time_transferred;
    private boolean livemode;
    private String status;
    private String app;
    private String channel;
    private String order_no;
    private int amount;
    private String currency;
    private String recipient;
    private String description;
    private String transaction_no;

    private Extra extra;

    public void setId(String id) {
        this.id = id;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public void setTime_transferred(Object time_transferred) {
        this.time_transferred = time_transferred;
    }

    public void setLivemode(boolean livemode) {
        this.livemode = livemode;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setOrder_no(String order_no) {
        this.order_no = order_no;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTransaction_no(String transaction_no) {
        this.transaction_no = transaction_no;
    }

    public void setExtra(Extra extra) {
        this.extra = extra;
    }

    public String getId() {
        return id;
    }

    public String getObject() {
        return object;
    }

    public String getType() {
        return type;
    }

    public int getCreated() {
        return created;
    }

    public Object getTime_transferred() {
        return time_transferred;
    }

    public boolean isLivemode() {
        return livemode;
    }

    public String getStatus() {
        return status;
    }

    public String getApp() {
        return app;
    }

    public String getChannel() {
        return channel;
    }

    public String getOrder_no() {
        return order_no;
    }

    public int getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getDescription() {
        return description;
    }

    public String getTransaction_no() {
        return transaction_no;
    }

    public Extra getExtra() {
        return extra;
    }

    public static class Extra implements Serializable{
        private String user_name;
        private boolean force_check;

        public void setUser_name(String user_name) {
            this.user_name = user_name;
        }

        public void setForce_check(boolean force_check) {
            this.force_check = force_check;
        }

        public String getUser_name() {
            return user_name;
        }

        public boolean isForce_check() {
            return force_check;
        }
    }
}
