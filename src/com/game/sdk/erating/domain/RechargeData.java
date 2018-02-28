package com.game.sdk.erating.domain;

/**
 * Created by lucky on 2018/2/26.
 */
public class RechargeData extends Header {
    public RechargeData() {
        super(0);
    }

    private Integer detail_id;
    private Integer user_id;
    private Integer subject_id;
    private Integer amount;
    private Integer Pad;
    private Integer charge_time;
    private Integer total_amount;
    private Integer balance;
    private String attach_code;

    public Integer getDetail_id() {
        return detail_id;
    }

    public void setDetail_id(Integer detail_id) {
        this.detail_id = detail_id;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public Integer getSubject_id() {
        return subject_id;
    }

    public void setSubject_id(Integer subject_id) {
        this.subject_id = subject_id;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getPad() {
        return Pad;
    }

    public void setPad(Integer pad) {
        Pad = pad;
    }

    public Integer getCharge_time() {
        return charge_time;
    }

    public void setCharge_time(Integer charge_time) {
        this.charge_time = charge_time;
    }

    public Integer getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(Integer total_amount) {
        this.total_amount = total_amount;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public String getAttach_code() {
        return attach_code;
    }

    public void setAttach_code(String attach_code) {
        this.attach_code = attach_code;
    }
}
