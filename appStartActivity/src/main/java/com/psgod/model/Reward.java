package com.psgod.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/1/26 0026.
 */
public class Reward implements Serializable {

    private double amount;
    private int type;
    private double balance;

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getAmount() {
        return amount;
    }

    public int getType() {
        return type;
    }

    public double getBalance() {
        return balance;
    }
}
