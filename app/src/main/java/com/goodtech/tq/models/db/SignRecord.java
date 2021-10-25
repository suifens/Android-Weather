package com.goodtech.tq.models.db;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * com.goodtech.tq.models
 */
public class SignRecord implements Parcelable {

    //  日期 "1970-01-01"
    private String dateDay;
    //  早安打卡、晚安打卡类型
    private String signType;
    //  创建时间
    private long createTime;
    //  连续打卡次数
    private int continueSign;

    public String getDateDay() {
        return dateDay;
    }

    public void setDateDay(String dateDay) {
        this.dateDay = dateDay;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getContinueSign() {
        return continueSign;
    }

    public void setContinueSign(int continueSign) {
        this.continueSign = continueSign;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.dateDay);
        dest.writeString(this.signType);
        dest.writeLong(this.createTime);
        dest.writeInt(this.continueSign);
    }

    public void readFromParcel(Parcel source) {
        this.dateDay = source.readString();
        this.signType = source.readString();
        this.createTime = source.readLong();
        this.continueSign = source.readInt();
    }

    public SignRecord() {
    }

    protected SignRecord(Parcel in) {
        this.dateDay = in.readString();
        this.signType = in.readString();
        this.createTime = in.readLong();
        this.continueSign = in.readInt();
    }

    public static final Parcelable.Creator<SignRecord> CREATOR = new Parcelable.Creator<SignRecord>() {
        @Override
        public SignRecord createFromParcel(Parcel source) {
            return new SignRecord(source);
        }

        @Override
        public SignRecord[] newArray(int size) {
            return new SignRecord[size];
        }
    };
}
