package com.game.sdk.erating.domain;

import com.game.sdk.erating.NodeName;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by lucky on 2018/2/5.
 * 增值道具消费
 */

public class ItemDetailData extends Header {
    @NodeName(name = "detail_id")
    private long detailId;
    @NodeName(name = "user_id")
    private int userId;
    @NodeName(name = "role_id")
    private int roleId;
    @NodeName(name = "role_gender")
    private int roleGender;
    @NodeName(name = "role_occupation")
    private int roleOccupation;
    @NodeName(name = "role_level")
    private int roleLevel;
    @NodeName(name = "rating_id")
    private int ratingId;
    @NodeName(name = "ib_code")
    private String ibCode;
    @NodeName(name = "package_flag")
    private int packageFlag;
    @NodeName(name = "count")
    private int count;
    @NodeName(name = "pay_time")
    private long payTime;
    @NodeName(name = "user_ip")
    private long userIp;
    @NodeName(name = "price")
    private int price;
    @NodeName(name = "discount_price")
    private int discountPrice;
    @NodeName(name = "subject_info_list", object = true)
    private List<SubjectInfo> infoList;

    public ItemDetailData(int command_id) {
        super(command_id);
        infoList = Lists.newArrayList();
    }

    public long getDetailId() {
        return detailId;
    }

    public void setDetailId(long detailId) {
        this.detailId = detailId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleOccupation(byte roleOccupation) {
        this.roleOccupation = roleOccupation;
    }

    public int getRoleLevel() {
        return roleLevel;
    }

    public void setRoleLevel(int roleLevel) {
        this.roleLevel = roleLevel;
    }

    public int getRatingId() {
        return ratingId;
    }

    public void setRatingId(int ratingId) {
        this.ratingId = ratingId;
    }

    public String getIbCode() {
        return ibCode;
    }

    public void setIbCode(String ibCode) {
        this.ibCode = ibCode;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getPayTime() {
        return payTime;
    }

    public void setPayTime(long payTime) {
        this.payTime = payTime;
    }

    public long getUserIp() {
        return userIp;
    }

    public void setUserIp(long userIp) {
        this.userIp = userIp;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(int discountPrice) {
        this.discountPrice = discountPrice;
    }

    public List<SubjectInfo> getInfoList() {
        return infoList;
    }

    public void setInfoList(List<SubjectInfo> infoList) {
        this.infoList = infoList;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public int getRoleGender() {
        return roleGender;
    }

    public void setRoleGender(int roleGender) {
        this.roleGender = roleGender;
    }

    public int getRoleOccupation() {
        return roleOccupation;
    }

    public void setRoleOccupation(int roleOccupation) {
        this.roleOccupation = roleOccupation;
    }

    public int getPackageFlag() {
        return packageFlag;
    }

    public void setPackageFlag(int packageFlag) {
        this.packageFlag = packageFlag;
    }
}
