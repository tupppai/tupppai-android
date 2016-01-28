package com.psgod.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/1/28 0028.
 */
public class Transactions implements Serializable{
    private String id;
    private String uid;
    private double balance;
    private String type;
    private double amount;
    private String memo;
    private String status;
    private String deleted_at;
    private String created_at;
    private String updated_at;

    public void setId(String id) {
        this.id = id;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDeleted_at(String deleted_at) {
        this.deleted_at = deleted_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }

    public double getBalance() {
        return balance;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getMemo() {
        return memo;
    }

    public String getStatus() {
        return status;
    }

    public String getDeleted_at() {
        return deleted_at;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }
}
